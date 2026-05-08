{{/*
Generate init containers that wait for dependencies to be reachable.
Usage: {{ include "portfolio.wait-for" (list "postgres:5432" "rabbitmq:5672") }}
Each entry is "host:port".
*/}}
{{- define "portfolio.wait-for" -}}
{{- range . }}
{{- $parts := splitList ":" . }}
{{- $host := index $parts 0 }}
{{- $name := index (splitList "." $host) 0 }}
- name: wait-for-{{ $name }}
  image: busybox
  securityContext:
    allowPrivilegeEscalation: false
    readOnlyRootFilesystem: true
    capabilities:
      drop:
        - ALL
  command: ['sh', '-c', 'until nc -z {{ $host }} {{ index $parts 1 }}; do echo waiting for {{ $name }}; sleep 2; done']
{{- end }}
{{- end -}}
