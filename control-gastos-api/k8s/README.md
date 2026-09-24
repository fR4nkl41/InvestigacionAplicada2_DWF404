# Despliegue en Kubernetes — Control de Gastos API

Guía paso a paso para desplegar la API REST `control-gastos-api` en un clúster de Kubernetes (Minikube).

---

## Requisitos previos

Antes de comenzar, asegúrate de tener instalado y configurado:

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| **Docker Desktop** | 20.10+ | `docker --version` |
| **Minikube** | 1.30+ | `minikube version` |
| **kubectl** | 1.27+ | `kubectl version --client` |

> **Nota**: Docker Desktop debe estar en ejecución y configurado para usar contenedores Linux.

---

## Paso 1 — Construir la imagen Docker

Desde la **raíz del proyecto** (donde está el `Dockerfile`), ejecuta:

```bash
docker build -t control-gastos-api:2.0.0 .
```

### Verificar que la imagen se creó

```bash
docker images | grep control-gastos
```

**Salida esperada**:
```
control-gastos-api   2.0.0   abc123def456   2 minutes ago   180MB
```

---

## Paso 2 — Iniciar el clúster de Minikube

Si es la primera vez, inicia Minikube con el driver de Docker:

```bash
minikube start --driver=docker
```

### Verificar que el clúster está activo

```bash
kubectl get nodes
```

**Salida esperada**:
```
NAME       STATUS   ROLES           AGE   VERSION
minikube   Ready    control-plane   1m    v1.30.0
```

---

## Paso 3 — Cargar la imagen en Minikube

Minikube tiene su propio registro interno. Debes cargar la imagen:

```bash
minikube image load control-gastos:2.0.0
```

### Verificar que la imagen está disponible dentro del clúster

```bash
minikube image ls | grep control-gastos-api
```

**Salida esperada**:
```
docker.io/library/control-gastos-api:2.0.0
```

---

## Paso 4 — Aplicar los manifiestos de Kubernetes

Aplica los tres manifiestos en el orden correcto (ConfigMap → Deployment → Service):

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### Alternativa: aplicar toda la carpeta de una vez

```bash
kubectl apply -f k8s/
```

**Salida esperada**:
```
configmap/control-gastos-config created
deployment.apps/control-gastos-api created
service/control-gastos-service created
```

---

## Paso 5 — Verificar el despliegue

### 5.1 Verificar los pods

```bash
kubectl get pods
```

**Salida esperada** (2 pods Running por las 2 réplicas):
```
NAME                                READY   STATUS    RESTARTS   AGE
control-gastos-api-5f7b8c9d6-abcde   1/1     Running   0          30s
control-gastos-api-5f7b8c9d6-fghij   1/1     Running   0          30s
```

> Si aparecen en `ContainerCreating`, espera unos segundos y repite el comando.

### 5.2 Verificar el deployment

```bash
kubectl get deployments
```

**Salida esperada**:
```
NAME                 READY   UP-TO-DATE   AVAILABLE   AGE
control-gastos-api    2/2     2            2           45s
```

### 5.3 Verificar el service

```bash
kubectl get services
```

**Salida esperada**:
```
NAME                      TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
control-gastos-service    NodePort    10.96.123.456   <none>        8080:30080/TCP   45s
```

### 5.4 Ver el estado detallado de un pod

```bash
kubectl describe pod <nombre-del-pod>
```

Revisa las secciones `Liveness` y `Readiness` para confirmar que los probes están activos.

### 5.5 Ver los logs en tiempo real

```bash
kubectl logs -f deployment/control-gastos-api
```

Aquí podrás ver los logs de Spring Boot y los eventos `[EVENT-SYNC]` y `[EVENT-ASYNC]`.

---

## Paso 6 — Acceder a la API

### Opción A: Abrir en el navegador automáticamente

```bash
minikube service control-gastos-service
```

Esto abrirá el navegador con la URL del servicio.

### Opción B: Obtener la URL manualmente

```bash
minikube service control-gastos-service --url
```

**Salida esperada**:
```
http://127.0.0.1:54321 o similares
```

> **Importante**: Debes mantener la terminal abierta mientras usas la URL. Si la cierras, el túnel se cierra. Ya sea Terminal de Windows o de IntellJIDEA

### Probar los endpoints

Con la URL obtenida, accede a:

- **Swagger UI**: `http://127.0.0.1:54321/swagger-ui.html`
- **Health check**: `http://127.0.0.1:54321/actuator/health`
- **API de categorías**: `http://127.0.0.1:54321/api/categorias`
- **API de transacciones**: `http://127.0.0.1:54321/api/transacciones`

> **Nota**: Modificar la ip de la dirección según muestre la Terminal.

---

## Paso 7 — Verificar los Health Checks

Kubernetes usa los endpoints de Actuator para saber si tu aplicación está viva y lista.

### Probar el endpoint de salud directamente desde un pod

```bash
kubectl exec -it <nombre-del-pod> -- wget -qO- http://localhost:8080/actuator/health
```

**Salida esperada**:
```json
{"status":"UP"}
```

---

## Paso 8 — Escalar el despliegue (demostración de escalabilidad)

Para demostrar que Kubernetes puede escalar tu aplicación:

```bash
kubectl scale deployment control-gastos-api --replicas=4
```

Verifica que ahora hay 4 pods:

```bash
kubectl get pods
```

Para volver a 2 réplicas:

```bash
kubectl scale deployment control-gastos-api --replicas=2
```

---

## Comandos útiles de diagnóstico

| Comando | Propósito |
|---------|-----------|
| `kubectl get all` | Ver todos los recursos |
| `kubectl logs -f deployment/control-gastos-api` | Ver logs en tiempo real |
| `kubectl describe pod <nombre>` | Detalles de un pod |
| `kubectl exec -it <nombre> -- sh` | Entrar a la terminal del pod |
| `kubectl port-forward svc/control-gastos-service 8080:8080` | Túnel directo (alternativa) |
| `kubectl get events --sort-by='.lastTimestamp'` | Ver eventos del clúster |

---

## Paso 9 — Limpiar recursos

### Eliminar el despliegue

```bash
kubectl delete -f k8s/
```

### Detener el clúster (sin eliminarlo)

```bash
minikube stop
```

### Eliminar el clúster completamente

```bash
minikube delete
```

---

## Solución de problemas comunes

### 1. Los pods quedan en `ImagePullBackOff`

**Causa**: La imagen no está disponible en el clúster.

**Solución**:
```bash
minikube image load control-gastos:2.0.0
kubectl rollout restart deployment control-gastos-api
```

### 2. Los pods quedan en `CrashLoopBackOff`

**Causa**: La aplicación no arranca correctamente.

**Solución**: Revisa los logs:
```bash
kubectl logs <nombre-del-pod>
```

Si el problema es que los probes fallan, verifica que en `application.properties` esté:
```properties
management.endpoint.health.probes.enabled=true
```

### 3. No puedo acceder a la URL del servicio

**Causa**: El túnel se cerró al cerrar la terminal.

**Solución**: Vuelve a ejecutar:
```bash
minikube service control-gastos-service --url
```

### 4. `minikube start` falla con el driver Docker

**Causa**: Docker Desktop no está corriendo o no está en modo Linux.

**Solución**: Abre Docker Desktop → Settings → General → activa "Use the WSL 2 based engine".

### 5. `kubectl` no encuentra el clúster

**Causa**: Minikube no está corriendo.

**Solución**:
```bash
minikube status
minikube start
```

---

## Arquitectura del despliegue

```
┌─────────────────────────────────────────────────────────┐
│                    Clúster Kubernetes                    │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │        Service: control-gastos-service         │    │
│  │              (NodePort 30080)                  │    │
│  └────────────────────┬───────────────────────────┘    │
│                       │                                 │
│         ┌─────────────┴─────────────┐                   │
│         │                           │                   │
│         ▼                           ▼                   │
│  ┌──────────────┐            ┌──────────────┐          │
│  │   Pod #1     │            │   Pod #2     │          │
│  │  (replica)   │            │  (replica)   │          │
│  │              │            │              │          │
│  │ control-     │            │ control-     │          │
│  │ gastos:2.0.0 │            │ gastos:2.0.0 │          │
│  │              │            │              │          │
│  │  Port 8080   │            │  Port 8080   │          │
│  └──────────────┘            └──────────────┘          │
│         ▲                           ▲                   │
│         │                           │                   │
│         └─────────────┬─────────────┘                   │
│                       │                                 │
│              ┌────────┴─────────┐                       │
│              │   ConfigMap      │                       │
│              │ control-gastos-  │                       │
│              │     config       │                       │
│              └──────────────────┘                       │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## Notas adicionales

- **Base de datos**: H2 en memoria. Los datos se pierden al reiniciar los pods.
- **Réplicas**: 2 por defecto (alta disponibilidad).
- **Probes**:
  - `readinessProbe`: `/actuator/health/readiness`
  - `livenessProbe`: `/actuator/health/liveness`
- **Resources**:
  - Requests: 256Mi RAM / 250m CPU
  - Limits: 512Mi RAM / 500m CPU
- **Puerto**: 8080 interno, 30080 expuesto por NodePort.

---

## Autores

- Alberto Guido, Christopher Adiel — AG252960
- Flores Pérez, Rodrigo Adrián — FP253000
- Mazariego Arteaga, Francisco Manuel — MA252948

**Universidad Don Bosco — Desarrollo de Aplicaciones con Web Frameworks (DWF404)**
