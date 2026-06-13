# Image Storage (S3/MinIO)

## Upload Flow

1. Operator uploads via `POST /operations/products/{id}/images` (multipart)
2. honey-manager validates file (type, size)
3. Generates object key: `{tenantId}/products/{productId}/{uuid}.{extension}`
4. Uploads to MinIO/S3 bucket
5. Stores public URL in `product_image` table
6. Returns image metadata

## Serving

Public-read bucket — images served directly via stored URL. No presigned URLs needed; product images are not sensitive. Simple, fast, cacheable.

Frontend handles responsive display via CSS (`object-fit`, `srcset` if needed later). No server-side image processing or thumbnail generation.

## Deletion

Immediate delete from S3 when image removed via operations API. No soft-delete.

## Validation

- Max file size: 5MB
- Allowed MIME types: JPEG, PNG, WebP
- Max images per product: 10

## Object Key Structure

```
{tenantId}/products/{productId}/{uuid}.{extension}
```

## Configuration

```yaml
app:
  storage:
    endpoint: ${MINIO_ENDPOINT}
    access-key: ${MINIO_ACCESS_KEY}
    secret-key: ${MINIO_SECRET_KEY}
    bucket: honey-images
    max-file-size: 5MB
    allowed-types: image/jpeg, image/png, image/webp
```

## Infrastructure

- **Local/Docker:** MinIO container in `docker-compose.yml` — S3-compatible API
- **Production/K8s:** MinIO deployment or AWS S3 — swap via config, no code change
- Same SDK (`S3Client`) works for both environments
