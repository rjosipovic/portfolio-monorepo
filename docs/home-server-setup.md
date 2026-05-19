# Home Server Setup — Staging Environment

This document covers the full setup procedure for the home server that serves as the public staging environment for the portfolio project.

**Role:** Pre-production staging. All changes land here via ArgoCD auto-sync before promotion to Hetzner production.

---

## 1. OS Installation

- **OS:** Ubuntu Server 24.04 LTS (minimal install, no desktop)
- **Disk:** Use entire disk, LVM optional
- **User:** Create a non-root user (e.g. `roman`) during install
- **SSH:** Enable OpenSSH server during install

---

## 2. Initial Access & Updates

```bash
# SSH in
ssh roman@<server-local-ip>

# Update system
sudo apt update && sudo apt upgrade -y

# Install essentials
sudo apt install -y curl wget git ufw fail2ban unattended-upgrades
```

---

## 3. Hardening

### SSH — Key-Only Authentication

```bash
# On your local machine: copy your public key
ssh-copy-id roman@<server-local-ip>

# On the server: disable password auth
sudo sed -i 's/#PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
sudo sed -i 's/PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
sudo sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin no/' /etc/ssh/sshd_config
sudo systemctl restart sshd
```

### Firewall (UFW)

```bash
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 80/tcp    # HTTP (cert-manager ACME challenge + redirect)
sudo ufw allow 443/tcp   # HTTPS (ingress)
sudo ufw allow 6443/tcp  # K3s API (for ArgoCD remote cluster management from this server)
sudo ufw enable
sudo ufw status
```

### Fail2ban

```bash
sudo systemctl enable fail2ban
sudo systemctl start fail2ban

# Default config protects SSH. Verify:
sudo fail2ban-client status sshd
```

### Unattended Security Updates

```bash
sudo dpkg-reconfigure -plow unattended-upgrades
# Select "Yes" to enable automatic security updates
```

---

## 4. Networking

### Static Local IP

Configure a static IP on your local network so port forwarding rules don't break.

```bash
# Edit netplan config (file name may vary)
sudo nano /etc/netplan/00-installer-config.yaml
```

Example:
```yaml
network:
  version: 2
  ethernets:
    enp0s3:  # your interface name (check with `ip a`)
      dhcp4: no
      addresses:
        - 192.168.1.100/24
      routes:
        - to: default
          via: 192.168.1.1
      nameservers:
        addresses: [8.8.8.8, 8.8.4.4]
```

```bash
sudo netplan apply
```

### Public Access — Option A: Port Forwarding + Dynamic DNS

1. **Router:** Forward ports 80 and 443 to the server's static local IP
2. **Dynamic DNS:** Use DuckDNS, No-IP, or Cloudflare DNS with a cron job updating your public IP

```bash
# Example: DuckDNS cron (every 5 minutes)
echo "*/5 * * * * curl -s 'https://www.duckdns.org/update?domains=YOUR_DOMAIN&token=YOUR_TOKEN'" | crontab -
```

### Public Access — Option B: Cloudflare Tunnel (Recommended)

No port forwarding needed. Traffic routes through Cloudflare's network.

```bash
# Install cloudflared
curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb -o cloudflared.deb
sudo dpkg -i cloudflared.deb

# Authenticate
cloudflared tunnel login

# Create tunnel
cloudflared tunnel create portfolio-staging

# Configure tunnel (routes traffic to local K3s ingress)
cat > ~/.cloudflared/config.yml << EOF
tunnel: <TUNNEL_ID>
credentials-file: /home/roman/.cloudflared/<TUNNEL_ID>.json

ingress:
  - hostname: staging.roman-josipovic.from.hr
    service: https://localhost:443
    originRequest:
      noTLSVerify: true
  - service: http_status:404
EOF

# Create DNS record
cloudflared tunnel route dns portfolio-staging staging.roman-josipovic.from.hr

# Run as service
sudo cloudflared service install
sudo systemctl enable cloudflared
sudo systemctl start cloudflared
```

**Advantage:** Your home IP stays private, no port forwarding, works behind CGNAT.

---

## 5. K3s Installation

```bash
# Install K3s (single-node, with Traefik as ingress)
curl -sfL https://get.k3s.io | sh -

# Verify
sudo k3s kubectl get nodes

# Make kubectl accessible without sudo
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $(id -u):$(id -g) ~/.kube/config
export KUBECONFIG=~/.kube/config
echo 'export KUBECONFIG=~/.kube/config' >> ~/.bashrc

# Verify
kubectl get nodes
```

### Install Helm

```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
helm version
```

---

## 6. TLS — cert-manager + Let's Encrypt

```bash
# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.17.2/cert-manager.yaml

# Wait for pods
kubectl wait --for=condition=ready pod -l app.kubernetes.io/instance=cert-manager -n cert-manager --timeout=120s
```

Create ClusterIssuer:

```yaml
# cert-manager-issuer.yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: rjosipovic@gmail.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
      - http01:
          ingress:
            class: traefik
```

```bash
kubectl apply -f cert-manager-issuer.yaml
```

---

## 7. ArgoCD Installation

```bash
# Create namespace
kubectl create namespace argocd

# Install ArgoCD
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for pods
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argocd-server -n argocd --timeout=300s

# Get initial admin password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
echo

# Install ArgoCD CLI
curl -sSL -o argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
chmod +x argocd
sudo mv argocd /usr/local/bin/

# Login (port-forward for initial setup)
kubectl port-forward svc/argocd-server -n argocd 8080:443 &
argocd login localhost:8080 --username admin --password <password-from-above> --insecure
```

### Expose ArgoCD UI (optional)

Create an Ingress for the ArgoCD UI if you want public access:

```yaml
# argocd-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: argocd-server
  namespace: argocd
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    traefik.ingress.kubernetes.io/router.tls: "true"
spec:
  ingressClassName: traefik
  tls:
    - hosts:
        - argocd.staging.roman-josipovic.from.hr
      secretName: argocd-tls
  rules:
    - host: argocd.staging.roman-josipovic.from.hr
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: argocd-server
                port:
                  number: 443
```

---

## 8. Configure ArgoCD Application

```bash
# Add the Git repository
argocd repo add https://github.com/rjosipovic/portfolio-monorepo --username <github-user> --password <github-pat>

# Create the staging application
argocd app create portfolio-staging \
  --repo https://github.com/rjosipovic/portfolio-monorepo \
  --path infra/k8s/charts/portfolio \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace portfolio \
  --values values.yaml \
  --values values-staging.yaml \
  --values secrets.yaml \
  --sync-policy automated \
  --auto-prune \
  --self-heal \
  --sync-option ServerSideApply=true

# Ignore immutable StatefulSet volumeClaimTemplates diffs
argocd app set portfolio-staging --ignore-differences 'group=apps,kind=StatefulSet,jsonPointers=[/spec/volumeClaimTemplates]'

# Verify
argocd app get portfolio-staging
```

---

## 9. Deploy Portfolio to Staging

On first setup, create the namespace and secrets:

```bash
# Create namespace
kubectl create namespace portfolio

# Create secrets file for staging (copy from template, fill values)
cp infra/k8s/charts/portfolio/secrets.yaml.template infra/k8s/charts/portfolio/secrets-staging.yaml
# Edit secrets-staging.yaml with staging-specific values

# If not using ArgoCD for initial deploy:
cd infra/k8s/charts/portfolio
helm upgrade --install portfolio . \
  -f values.yaml \
  -f values-staging.yaml \
  -f secrets-staging.yaml \
  --namespace portfolio
```

Once ArgoCD is configured, subsequent deploys happen automatically when CI updates `values-staging.yaml`.

---

## 10. Verification Checklist

- [ ] SSH access works (key-only)
- [ ] UFW active, only 22/80/443/6443 open
- [ ] fail2ban protecting SSH
- [ ] K3s running, `kubectl get nodes` shows Ready
- [ ] Helm installed
- [ ] cert-manager issuing certificates
- [ ] Public URL resolves (via Cloudflare Tunnel or port forwarding)
- [ ] ArgoCD UI accessible
- [ ] ArgoCD Application synced and healthy
- [ ] Portfolio services running on staging
- [ ] CI push → image built → values updated → ArgoCD deploys (full loop)

---

## Maintenance

### Updates
```bash
# OS security updates (automatic via unattended-upgrades)
# K3s upgrade
curl -sfL https://get.k3s.io | sh -

# ArgoCD upgrade
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### Monitoring
```bash
# Check K3s status
sudo systemctl status k3s

# Check pod health
kubectl get pods --all-namespaces

# Check ArgoCD sync status
argocd app list
```

### Troubleshooting
```bash
# K3s logs
sudo journalctl -u k3s -f

# Pod logs
kubectl logs -n portfolio <pod-name>

# ArgoCD app status
argocd app get portfolio-staging
argocd app diff portfolio-staging
```
