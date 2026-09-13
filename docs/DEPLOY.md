# Deployment

The demo runs on **Google Cloud Run** (scale-to-zero container) with a **Neon** serverless PostgreSQL.
A Cloud Scheduler job resets the demo data nightly. Kubernetes manifests for a local kind/minikube
cluster are kept under `deploy/`.

All configuration is environment-driven (see `src/main/resources/application.yml`):

| Variable                     | Purpose                                                        |
|------------------------------|----------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`      | JDBC URL, e.g. `jdbc:postgresql://host/db?sslmode=require`     |
| `SPRING_DATASOURCE_USERNAME` | DB user                                                        |
| `SPRING_DATASOURCE_PASSWORD` | DB password                                                    |
| `SPRING_PROFILES_ACTIVE`     | `demo` seeds the sample accounts and enables the login hints   |
| `DEMO_RESET_TOKEN`           | Enables `POST /internal/demo/reset` (header `X-Reset-Token`)   |
| `COOKIE_SECURE`              | `true` behind HTTPS (Cloud Run)                                |
| `PORT`                       | Listen port (Cloud Run injects `8080`)                         |

## 1. Cloud Run + Neon

### 1.1 Database (Neon)

1. Create a Neon project and a database named `hospital` (any region close to `europe-west3`).
2. Copy the pooled connection string. The JDBC form is
   `jdbc:postgresql://<host>/hospital?sslmode=require`.
   Flyway creates the schema on first start (`db/migration/V1..V3`); nothing to run by hand.

### 1.2 Google Cloud project

```bash
export GCP_PROJECT=<your-project-id>
export REGION=europe-west3
gcloud config set project "$GCP_PROJECT"
gcloud services enable run.googleapis.com artifactregistry.googleapis.com \
  secretmanager.googleapis.com cloudscheduler.googleapis.com iamcredentials.googleapis.com

# image repository used by the workflow
gcloud artifacts repositories create apps --repository-format=docker --location="$REGION"
```

### 1.3 Secrets (Secret Manager)

The deploy workflow mounts these four secrets as environment variables:

```bash
printf '%s' 'jdbc:postgresql://<neon-host>/hospital?sslmode=require' | gcloud secrets create hospital-ms-db-url      --data-file=-
printf '%s' '<neon-user>'                                            | gcloud secrets create hospital-ms-db-user     --data-file=-
printf '%s' '<neon-password>'                                        | gcloud secrets create hospital-ms-db-password --data-file=-
openssl rand -hex 32                                                 | gcloud secrets create hospital-ms-reset-token --data-file=-
```

Grant the Cloud Run runtime service account access (the default compute SA, or a dedicated one):

```bash
RUNTIME_SA="$(gcloud iam service-accounts list --filter='displayName:Compute Engine default service account' --format='value(email)')"
for s in hospital-ms-db-url hospital-ms-db-user hospital-ms-db-password hospital-ms-reset-token; do
  gcloud secrets add-iam-policy-binding "$s" --member="serviceAccount:$RUNTIME_SA" --role=roles/secretmanager.secretAccessor
done
```

### 1.4 First manual deploy (from source)

Useful before CI/CD is wired up; Cloud Build uses the `Dockerfile`:

```bash
gcloud run deploy hospital-ms --source . --region "$REGION" \
  --allow-unauthenticated --port 8080 --memory 512Mi --cpu 1 --cpu-boost \
  --min-instances 0 --max-instances 1 --concurrency 40 \
  --set-env-vars SPRING_PROFILES_ACTIVE=demo,COOKIE_SECURE=true \
  --set-secrets SPRING_DATASOURCE_URL=hospital-ms-db-url:latest,SPRING_DATASOURCE_USERNAME=hospital-ms-db-user:latest,SPRING_DATASOURCE_PASSWORD=hospital-ms-db-password:latest,DEMO_RESET_TOKEN=hospital-ms-reset-token:latest

URL="$(gcloud run services describe hospital-ms --region "$REGION" --format 'value(status.url)')"
curl -fsS "$URL/actuator/health"
```

### 1.5 Continuous deployment (GitHub Actions, Workload Identity Federation)

`.github/workflows/deploy-cloud-run.yml` runs on pushes to `master` that touch `src/**`, `pom.xml` or the
`Dockerfile`. It builds the image, pushes it to Artifact Registry and deploys it, then smoke-tests
`/actuator/health`. No long-lived keys: GitHub authenticates through WIF.

```bash
# one-off: pool + provider + deployer service account
gcloud iam workload-identity-pools create github --location=global
gcloud iam workload-identity-pools providers create-oidc github \
  --location=global --workload-identity-pool=github \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" \
  --attribute-condition="assertion.repository=='MartinStefanov20/hospital-management-system'"

gcloud iam service-accounts create github-deployer
DEPLOYER="github-deployer@${GCP_PROJECT}.iam.gserviceaccount.com"
for role in roles/run.admin roles/artifactregistry.writer roles/iam.serviceAccountUser; do
  gcloud projects add-iam-policy-binding "$GCP_PROJECT" --member="serviceAccount:$DEPLOYER" --role="$role"
done
POOL_ID="$(gcloud iam workload-identity-pools describe github --location=global --format='value(name)')"
gcloud iam service-accounts add-iam-policy-binding "$DEPLOYER" \
  --role=roles/iam.workloadIdentityUser \
  --member="principalSet://iam.googleapis.com/${POOL_ID}/attribute.repository/MartinStefanov20/hospital-management-system"
```

GitHub repository secrets:

| Secret                | Value                                                                                      |
|-----------------------|--------------------------------------------------------------------------------------------|
| `GCP_PROJECT`         | project id                                                                                 |
| `WIF_PROVIDER`        | `projects/<number>/locations/global/workloadIdentityPools/github/providers/github`         |
| `WIF_SERVICE_ACCOUNT` | `github-deployer@<project>.iam.gserviceaccount.com`                                        |

### 1.6 Nightly demo reset (Cloud Scheduler)

`POST /internal/demo/reset` wipes user-generated data and re-seeds the demo set. It is protected by the
shared secret in `X-Reset-Token`.

```bash
TOKEN="$(gcloud secrets versions access latest --secret=hospital-ms-reset-token)"
gcloud scheduler jobs create http hospital-ms-nightly-reset \
  --location="$REGION" --schedule="0 3 * * *" --time-zone="Europe/Berlin" \
  --uri="$URL/internal/demo/reset" --http-method=POST \
  --headers="X-Reset-Token=$TOKEN" --attempt-deadline=120s
```

Trigger it once to verify: `gcloud scheduler jobs run hospital-ms-nightly-reset --location="$REGION"`
(expect HTTP 204).

## 2. Kubernetes (kind / minikube)

`deploy/kustomization.yaml` deploys an in-cluster PostgreSQL (hostPath volume; local clusters only) and the
application with liveness/readiness probes, resource limits and a non-root security context.

```bash
kind create cluster            # or: minikube start
kubectl create secret generic hms-db-credentials \
  --from-literal=POSTGRES_USER=postgres \
  --from-literal=POSTGRES_PASSWORD="$(openssl rand -hex 12)" \
  --from-literal=SPRING_DATASOURCE_USERNAME=postgres \
  --from-literal=SPRING_DATASOURCE_PASSWORD="<same password>" \
  --from-literal=DEMO_RESET_TOKEN="$(openssl rand -hex 16)"
kubectl apply -k deploy/
kubectl rollout status deploy/hms
kubectl port-forward svc/hms 8080:80
```

The Deployment references `ghcr.io/martinstefanov20/hospital-management-system:latest`; build and load a
local image instead with `docker build -t hms:local . && kind load docker-image hms:local` and point
`deploy/k8s/deployment.yaml` at it. Validate manifests with
`docker run --rm -v "$PWD/deploy/k8s:/m" ghcr.io/yannh/kubeconform:latest -strict -summary /m`.

## 3. Local run

### 3.1 Docker Compose (app + PostgreSQL)

```bash
cp .env.example .env            # set POSTGRES_PASSWORD (and optionally DEMO_RESET_TOKEN)
docker compose up --build
open http://localhost:8080
```

### 3.2 From the IDE / Maven against an existing PostgreSQL

The `local` profile targets `jdbc:postgresql://localhost:5433/hospital` (user `postgres`, password
`password`) and pulls in the `demo` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Override with `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` if your database differs. Health is at
`/actuator/health`, the API docs at `/swagger-ui.html`.

### 3.3 Running the container image locally

```bash
docker build -t hms:local .
docker run --rm -p 8090:8080 \
  -e SPRING_PROFILES_ACTIVE=demo \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/hospital \
  -e SPRING_DATASOURCE_USERNAME=postgres -e SPRING_DATASOURCE_PASSWORD=password \
  -e DEMO_RESET_TOKEN=t hms:local
```
