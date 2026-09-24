# Control de Gastos Personales — API REST Event-Driven

API REST desarrollada con **Spring Boot 3.5** que implementa el paradigma **Event-Driven** mediante eventos internos de Spring (`ApplicationEventPublisher` + `@EventListener` + `@Async`), empaquetada en **Docker** y desplegable en **Kubernetes**.

> Proyecto académico para la asignatura **Desarrollo de Aplicaciones con Web Frameworks (DWF404)** — Universidad Don Bosco.

---

## 📖 Tabla de contenidos

- [Descripción](#-descripción)
- [Tecnologías](#-tecnologías)
- [Arquitectura](#-arquitectura)
- [Paradigma Event-Driven](#-paradigma-event-driven)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Ejecución local](#-ejecución-local)
- [Ejecución con Docker](#-ejecución-con-docker)
- [Despliegue en Kubernetes](#-despliegue-en-kubernetes)
- [Documentación interactiva](#-documentación-interactiva)
- [Health checks](#-health-checks)
- [Pruebas](#-pruebas)
- [Autores](#-autores)

---

## 📝 Descripción

Muchas personas no llevan un registro organizado de sus ingresos y gastos, lo que dificulta conocer su situación financiera real. Esta API REST permite:

- **Registrar transacciones** (ingresos y gastos) clasificadas por categoría.
- **Consultar el balance general** del usuario.
- **Filtrar transacciones** por categoría.
- **Eliminar transacciones** con recálculo automático del balance en background.

Además, implementa el **paradigma Event-Driven** para desacoplar la lógica de negocio de las tareas secundarias (auditoría, notificaciones, recálculo de balance).

---

## 🚀 Tecnologías

| Categoría | Tecnología |
|-----------|-----------|
| **Lenguaje** | Java 21 |
| **Framework** | Spring Boot 3.5.16 |
| **Persistencia** | Spring Data JPA + Hibernate |
| **Base de datos** | H2 (en memoria) |
| **Documentación** | SpringDoc OpenAPI 2.8.5 |
| **Validaciones** | Jakarta Bean Validation |
| **Monitoreo** | Spring Boot Actuator |
| **Concurrencia** | `@Async` con ThreadPoolTaskExecutor |
| **Empaquetado** | Maven + Docker (multi-etapa) |
| **Orquestación** | Kubernetes (Minikube) |
| **Testing** | JUnit 5 + Mockito |

---

## Arquitectura

La aplicación sigue una **arquitectura por capas** con enfoque Event-Driven:

```
┌──────────────────────────────────────────────────────┐
│                   Cliente (Postman)                  │
└───────────────────────┬──────────────────────────────┘
                        │ HTTP/JSON
                        ▼
┌──────────────────────────────────────────────────────┐
│              Controller Layer (@RestController)      │
│         CategoriaController · TransaccionController  │
└───────────────────────┬──────────────────────────────┘
                        │ DTOs
                        ▼
┌──────────────────────────────────────────────────────┐
│              Service Layer (@Service)                │
│        CategoriaService · TransaccionService         │
│                                                      │
│        ┌──────────────────────────────┐              │
│        │ ApplicationEventPublisher    │              │
│        └──────────────┬───────────────┘              │
└───────────────────────┼──────────────────────────────┘
                        │ Eventos
                        ▼
┌──────────────────────────────────────────────────────┐
│              Event Listener Layer                    │
│  TransaccionEventListener (@EventListener + @Async)  │
└───────────────────────┬──────────────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────────────┐
│              Repository Layer (@Repository)          │
│       CategoriaRepository · TransaccionRepository    │
└───────────────────────┬──────────────────────────────┘
                        │ JPA
                        ▼
┌──────────────────────────────────────────────────────┐
│                   H2 Database                        │
│            (jdbc:h2:mem:testdb)                      │
└──────────────────────────────────────────────────────┘
```

---

## Paradigma Event-Driven

### ¿Qué se implementó?

Al **registrar una transacción** (`POST /api/transacciones`):

1. `TransaccionService` guarda la transacción en H2.
2. Publica un `TransaccionCreadaEvent` mediante `ApplicationEventPublisher`.
3. Un **listener síncrono** registra el evento en los logs para auditoría.
4. Un **listener asíncrono** (`@Async`) simula una notificación sin bloquear la respuesta HTTP.

Al **eliminar una transacción** (`DELETE /api/transacciones/{id}`):

1. Se elimina de la base de datos.
2. Se publica un `TransaccionEliminadaEvent`.
3. Un **listener asíncrono** recalcula el balance en background.

### Flujo visual

```
POST /api/transacciones
    │
    ▼
[TransaccionController] ──► [TransaccionService.registrar()]
                                    │
                ┌───────────────────┼───────────────────┐
                ▼                   ▼                   ▼
        [Guardar en BD]   [Publicar evento]    [Retornar DTO]
                                    │
                                    ▼
                    [ApplicationEventPublisher]
                                    │
                                    ▼
                    [TransaccionCreadaEvent]
                                    │
                    ┌───────────────┼───────────────┐
                    ▼                               ▼
        [Listener Síncrono]            [Listener Asíncrono @Async]
        (auditoría rápida)             (notificación, 2s de delay)
        Hilo HTTP original             Hilo: async-event-N
```

### Beneficios demostrados

- **Desacoplamiento**: el servicio no conoce quién reacciona al evento.
- **No bloqueante**: la respuesta HTTP se devuelve inmediatamente.
- **Escalable**: se pueden agregar nuevos listeners sin modificar el productor.
- **Trazabilidad**: los logs muestran los hilos `async-event-*` ejecutando tareas en paralelo.

---

## Estructura del proyecto

```
control-gastos/
├── .dockerignore
├── .gitignore
├── Dockerfile
├── README.md
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
│
├── k8s/                                    ← Manifiestos de Kubernetes
│   ├── configmap.yaml
│   ├── deployment.yaml
│   ├── service.yaml
│   └── README.md
│
└── src/
    ├── main/
    │   ├── java/sv/edu/udb/
    │   │   ├── ControlGastosApplication.java
    │   │   │
    │   │   ├── config/
    │   │   │   ├── AsyncConfig.java
    │   │   │   └── OpenApiConfig.java
    │   │   │
    │   │   ├── controller/
    │   │   │   ├── CategoriaController.java
    │   │   │   └── TransaccionController.java
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── CategoriaRequestDTO.java
    │   │   │   ├── CategoriaResponseDTO.java
    │   │   │   ├── ErrorResponseDTO.java
    │   │   │   ├── TransaccionRequestDTO.java
    │   │   │   └── TransaccionResponseDTO.java
    │   │   │
    │   │   ├── event/
    │   │   │   ├── TransaccionCreadaEvent.java
    │   │   │   ├── TransaccionEliminadaEvent.java
    │   │   │   └── TransaccionEventListener.java
    │   │   │
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   │
    │   │   ├── model/
    │   │   │   ├── Categoria.java
    │   │   │   └── Transaccion.java
    │   │   │
    │   │   ├── repository/
    │   │   │   ├── CategoriaRepository.java
    │   │   │   └── TransaccionRepository.java
    │   │   │
    │   │   └── service/
    │   │       ├── CategoriaService.java
    │   │       └── TransaccionService.java
    │   │
    │   └── resources/
    │       └── application.properties
    │
    └── test/java/sv/edu/udb/
        └── ControlGastosApplicationTests.java
```

---

## Endpoints de la API

### Categorías

| Método | Endpoint | Descripción | Códigos |
|--------|----------|-------------|---------|
| `POST` | `/api/categorias` | Crear categoría | 201, 400 |
| `GET` | `/api/categorias` | Listar categorías | 200 |
| `GET` | `/api/categorias/{id}` | Obtener por ID | 200, 404 |

### Transacciones

| Método | Endpoint | Descripción | Códigos |
|--------|----------|-------------|---------|
| `POST` | `/api/transacciones` | Registrar (🔔 publica evento) | 201, 400, 404 |
| `GET` | `/api/transacciones` | Listar todas | 200 |
| `GET` | `/api/transacciones/{id}` | Obtener por ID | 200, 404 |
| `GET` | `/api/transacciones/balance` | Balance general | 200 |
| `GET` | `/api/transacciones/categoria/{id}` | Filtrar por categoría | 200 |
| `DELETE` | `/api/transacciones/{id}` | Eliminar (🔔 publica evento) | 204, 404 |

---

## Ejemplos de uso

### 1. Crear una categoría

```bash
curl -X POST http://localhost:8080/api/categorias \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Entretenimiento",
    "tipo": "GASTO"
  }'
```

### 2. Registrar una transacción (dispara evento)

```bash
curl -X POST http://localhost:8080/api/transacciones \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "Pago de nómina",
    "monto": 1500.00,
    "categoriaId": 1
  }'
```

### 3. Consultar balance

```bash
curl http://localhost:8080/api/transacciones/balance
```

**Respuesta**:
```json
{ "balance": 1454.25 }
```

### 4. Probar error 400 (validación)

```bash
curl -X POST http://localhost:8080/api/transacciones \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "",
    "monto": -50,
    "categoriaId": null
  }'
```

**Respuesta**:
```json
{
  "timestamp": "2026-09-22T10:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los datos enviados",
  "path": "/api/transacciones",
  "validationErrors": {
    "descripcion": "La descripción no puede estar vacía",
    "monto": "El monto debe ser mayor que cero",
    "categoriaId": "La categoría es obligatoria"
  }
}
```

---

## Ejecución local

### Requisitos

- Java 21 o superior
- Maven 3.9+
- Git

### Comandos

```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/control-gastos.git
cd control-gastos

# Compilar
./mvnw clean install

# Ejecutar
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

---

## Ejecución con Docker

### 1. Construir la imagen

```bash
docker build -t control-gastos-api:2.0.0 .
```

### 2. Ejecutar el contenedor

```bash
docker run -d -p 8080:8080 --name control-gastos-app control-gastos-api:2.0.0
```

### 3. Verificar

```bash
docker ps
docker logs -f control-gastos-app
```

Accede a `http://localhost:8080/swagger-ui.html`.

### 4. Detener y eliminar

```bash
docker stop control-gastos-app
docker rm control-gastos-app
```

---

## Despliegue en Kubernetes

Consulta la guía completa en [`k8s/README.md`](k8s/README.md).

### Resumen rápido

```bash
# 1. Construir la imagen
docker build -t control-gastos-api:2.0.0 .

# 2. Iniciar Minikube
minikube start --driver=docker

# 3. Cargar la imagen al clúster
minikube image load control-gastos-api:2.0.0

# 4. Aplicar manifiestos
kubectl apply -f k8s/

# 5. Verificar
kubectl get pods
kubectl get services

# 6. Acceder
minikube service control-gastos-service --url
```

---

## Documentación interactiva

Una vez la aplicación esté corriendo:

| Recurso | URL |
|---------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/api-docs |
| **OpenAPI YAML** | http://localhost:8080/api-docs.yaml |

---

## Health checks

| Endpoint | Propósito |
|----------|-----------|
| `/actuator/health` | Estado general |
| `/actuator/health/liveness` | Liveness probe (K8s) |
| `/actuator/health/readiness` | Readiness probe (K8s) |
| `/actuator/info` | Información de la app |
| `/actuator/metrics` | Métricas de rendimiento |

---

## 🧪 Pruebas

```bash
# Ejecutar todas las pruebas
./mvnw test

# Ejecutar una prueba específica
./mvnw test -Dtest=ControlGastosApplicationTests
```

---

## 🐛 Solución de problemas

### La app no arranca

- Verifica que el puerto 8080 esté libre.
- Ejecuta `./mvnw clean install` para regenerar el JAR.

### Swagger no aparece

- Verifica la dependencia `springdoc-openapi-starter-webmvc-ui` en `pom.xml`.
- Reinicia la aplicación.

### Error 500 al listar categorías

- Si ves `LazyInitializationException`, verifica que `CategoriaService` devuelva `CategoriaResponseDTO` en lugar de la entidad.

### Los pods quedan en `ImagePullBackOff`

- Ejecuta `minikube image load control-gastos-api:2.0.0`.

---

## Recursos adicionales

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Events](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events)
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Minikube Documentation](https://minikube.sigs.k8s.io/docs/)
- [SpringDoc OpenAPI](https://springdoc.org/)

---

## Autores

**Equipo UDB** — Desarrollo de Aplicaciones con Web Frameworks (DWF404)

- Alberto Guido, Christopher Adiel — AG252960
- Flores Pérez, Rodrigo Adrián — FP253000
- Mazariego Arteaga, Francisco Manuel — MA252948

**Universidad Don Bosco**

---

## Licencia

Este proyecto es de uso académico para la asignatura **DWF404** de la Universidad Don Bosco.
