{{/*
Generate init containers that wait for dependencies to be reachable.
Usage: {{ include "portfolio.wait-for" (list "postgres:5432" "rabbitmq:5672") }}
Each entry is "host:port".
*/}}
{{- define "portfolio.wait-for" -}}
{{- range . }}
{{- $parts := splitList ":" . }}
- name: wait-for-{{ index $parts 0 }}
  image: busybox
  securityContext:
    allowPrivilegeEscalation: false
    readOnlyRootFilesystem: true
    capabilities:
      drop:
        - ALL
  command: ['sh', '-c', 'until nc -z {{ index $parts 0 }} {{ index $parts 1 }}; do echo waiting for {{ index $parts 0 }}; sleep 2; done']
{{- end }}
{{- end -}}
