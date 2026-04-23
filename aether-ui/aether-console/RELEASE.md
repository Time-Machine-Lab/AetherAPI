# Aether Console Release

This document describes the tag-driven release pipeline for `aether-console`.

## Trigger

The release workflow runs only when a tag matching `aether-console-v*` is pushed.

```bash
git tag aether-console-v1.0.0
git push origin aether-console-v1.0.0
```

The workflow is defined at `.github/workflows/aether-console-release.yml`.

## Release Gates

The workflow runs these checks before building and pushing a Docker image:

```bash
pnpm install --frozen-lockfile
pnpm lint
pnpm format:check
pnpm type-check
pnpm test
pnpm build
```

If any gate fails, the image is not pushed and the server is not deployed.

## Image

Images are pushed to Docker Hub with both the triggering tag and a moving console tag:

```text
<docker-username>/aether-console:<aether-console-v*>
<docker-username>/aether-console:latest-console
```

The Docker image uses a multi-stage build:

- `node:22-alpine` builds the Vite `dist` output with pnpm.
- `nginx:1.27-alpine` serves the built static files.

## Runtime Ports

The container listens on port `8888`.

```bash
docker run -d \
  --name aether-console \
  --restart unless-stopped \
  --add-host=host.docker.internal:host-gateway \
  -p 8888:8888 \
  -e AETHER_BACKEND_UPSTREAM=http://61.184.13.101:8090 \
  <docker-username>/aether-console:aether-console-v1.0.0
```

The frontend keeps `VITE_API_BASE_URL=/api`, and API files include the version segment in request paths. Browser requests to `/api/v1/**` stay same-origin and Nginx forwards `/api/**` to `AETHER_BACKEND_UPSTREAM`.

The backend service must be reachable from the Nginx container on port `8090`. The default upstream is `http://61.184.13.101:8090`. If the backend process listens elsewhere, set the `AETHER_BACKEND_UPSTREAM` variable to a reachable `http://...:8090` address before releasing `aether-console`.

The release workflow still adds `--add-host=host.docker.internal:host-gateway` when starting the container so deployments can override `AETHER_BACKEND_UPSTREAM` to `http://host.docker.internal:8090` on Linux Docker hosts when needed.

## Required GitHub Secrets

Configure these repository, environment, or organization secrets before pushing a release tag.
`DOCKER_USERNAME` and `DOCKER_PASSWORD` are expected to come from Organization secrets.

| Name                            | Purpose                                                        |
| ------------------------------- | -------------------------------------------------------------- |
| `AETHER_CONSOLE_DEPLOY_HOST`    | Target server hostname or IP.                                  |
| `AETHER_CONSOLE_DEPLOY_USER`    | SSH user on the target server.                                 |
| `AETHER_CONSOLE_DEPLOY_SSH_KEY` | Private SSH key with deployment access.                        |
| `AETHER_CONSOLE_DEPLOY_PORT`    | SSH port. Defaults to `22` when omitted.                       |
| `DOCKER_USERNAME`               | Docker Hub username used by CI and the target server.          |
| `DOCKER_PASSWORD`               | Docker Hub password or access token with push/pull permission. |

## Optional GitHub Variables

| Name                            | Default                     | Purpose                                                    |
| ------------------------------- | --------------------------- | ---------------------------------------------------------- |
| `AETHER_CONSOLE_CONTAINER_NAME` | `aether-console`            | Docker container name on the target server.                |
| `AETHER_BACKEND_UPSTREAM`       | `http://61.184.13.101:8090` | Backend upstream used by Nginx. Must point to port `8090`. |

## First Deployment Checklist

1. Confirm Docker is installed and the SSH user can run `docker ps`.
2. Confirm the server can pull from Docker Hub:

   ```bash
   echo "<password-or-token>" | docker login -u "<docker-username>" --password-stdin
   ```

3. Confirm port `8888` is free:

   ```bash
   docker ps --format '{{.Ports}}' | grep 8888 || true
   ```

4. Confirm the backend upstream is reachable from the Docker network:

   ```bash
   docker run --rm \
     --add-host=host.docker.internal:host-gateway \
     curlimages/curl:8.11.1 \
     -I http://61.184.13.101:8090/api/v1/console/auth/current-session || true
   ```

5. After deployment, verify the frontend and proxy:

   ```bash
   curl -I http://localhost:8888/
   curl -I http://localhost:8888/healthz
   curl -I http://localhost:8888/api/v1/console/auth/current-session || true
   ```

## Rollback

Use the previous `aether-console-v*` image tag and restart the container:

```bash
PREVIOUS_TAG=aether-console-v0.9.0
IMAGE=<docker-username>/aether-console:${PREVIOUS_TAG}

docker pull "$IMAGE"
docker rm -f aether-console 2>/dev/null || true
docker run -d \
  --name aether-console \
  --restart unless-stopped \
  --add-host=host.docker.internal:host-gateway \
  -p 8888:8888 \
  -e AETHER_BACKEND_UPSTREAM=http://61.184.13.101:8090 \
  "$IMAGE"
```

## Local Dry Run

Use these commands to validate the image locally before pushing a release tag:

```bash
docker build -t aether-console:local .
docker run --rm -d \
  --name aether-console-local \
  --add-host=host.docker.internal:host-gateway \
  -p 8888:8888 \
  -e AETHER_BACKEND_UPSTREAM=http://61.184.13.101:8090 \
  aether-console:local
docker exec aether-console-local nginx -t
curl -I http://localhost:8888/
curl -I http://localhost:8888/healthz
docker rm -f aether-console-local
```

Verification for this change was completed with local quality gates and static workflow validation. A remote tag-triggered deployment was not executed from this workspace because it would require real repository secrets and access to the production deployment server.
