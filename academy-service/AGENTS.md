# AGENTS.md — colegio-academy-service

## Stack

- **Spring Boot 4.0.6 / Java 21**, compilación con Maven (`mvnw` wrapper en `academy-service/`)
- **PostgreSQL** vía JPA + Liquibase (`ddl-auto=validate` — todo el schema en `db/changelog/db.changelog.sql`)
- **Spring Security** con validación JWT (jjwt 0.11.5, HS256), misma clave secreta compartida con `user-service`
- **Lombok**, `@RequiredArgsConstructor` en todo el proyecto excepto donde se indica
- **WebClient** (Spring WebFlux) para comunicación HTTP con `user-service`
- **Puerto 9092**

## Inicio rápido

```bash
cd academy-service
./mvnw clean package -DskipTests   # compilar
./mvnw test                         # ejecutar tests (~60 tests)
./mvnw spring-boot:run              # servidor de desarrollo
```

## API

### Cursos (`/api/v1/cursos/`)
| Método | Ruta | Auth | Notas |
|--------|------|------|-------|
| POST | `` | ADMIN | Crea curso, valida duplicado (nivel+letra) |
| GET | `` | Cualquiera autenticado | Lista todos los cursos |
| PUT | `/{id}` | ADMIN | Actualiza curso, valida duplicado |
| DELETE | `/{id}` | ADMIN | **Solo si no tiene asignaturas ni matrículas** |

### Asignaturas (`/api/v1/asignaturas/`)
| Método | Ruta | Auth | Notas |
|--------|------|------|-------|
| POST | `` | ADMIN | Verifica que el docente existe via user-service |
| GET | `/docente/{idDocente}` | Cualquiera autenticado | Lista por docente |
| GET | `` | Cualquiera autenticado | Lista todas |
| PUT | `/{id}` | ADMIN | Verifica docente + curso existen |
| DELETE | `/{id}` | ADMIN | **Solo si no tiene calificaciones** |

### Matrículas (`/api/v1/matriculas/`)
| Método | Ruta | Auth | Notas |
|--------|------|------|-------|
| POST | `` | ADMIN | Verifica estudiante existe via user-service |
| GET | `/curso/{cursoId}` | Cualquiera autenticado | Alumnos de un curso |
| GET | `` | Cualquiera autenticado | Lista todas |
| PUT | `/{id}` | ADMIN | Verifica estudiante + curso |
| DELETE | `/{id}` | ADMIN | **Eliminación física** |

### Calificaciones (`/api/v1/calificaciones/`)
| Método | Ruta | Auth | Notas |
|--------|------|------|-------|
| POST | `` | DOCENTE/ADMIN | Valida nota 1.0-7.0, alumno matriculado en el curso |
| GET | `/estudiante/{estudianteId}` | Cualquiera autenticado | Notas de un estudiante |
| GET | `` | Cualquiera autenticado | Lista todas |
| GET | `/mis-calificaciones` | DOCENTE | Notas creadas por el docente autenticado |
| GET | `?estudianteId=X&asignaturaId=Y` | Cualquiera autenticado | Filtro por estudiante y asignatura |
| PUT | `/{id}` | DOCENTE | **Solo el docente que creó la calificación** |
| DELETE | `/{id}` | DOCENTE | **Solo el docente que creó la calificación** |

## Arquitectura

- Paquete base `com.proyecto.academy_service`
- Arquitectura por capas: Controller → Service → Repository → Database
- **Sin DTOs separados por request/response**: cada entidad tiene su `DTO` con métodos `toModel()` y `fromModel()`
- **Referencias lógicas**: `docenteId` y `estudianteId` referencian a `user-service` (sin FK física)
- **WebClient** para verificar existencia de usuarios en `user-service` (`/api/v1/usuarios/{id}/exists`)
- **Seguridad**: JWT extraído de cookie (`jwt-cookie`) o header `Authorization: Bearer`, validado con clave HMAC-SHA256 compartida
- **Roles en controllers**: verificación manual via `SecurityContextHolder` (no `@PreAuthorize`)
- Manejo de errores: `GlobalExceptionHandler` (`@RestControllerAdvice`) mapea a DTO `ErrorResponse`
- Excepciones: `BusinessRuleException` → 400, `ResourceNotFoundException` → 404, otras → 500

## Modelo de datos

### Tabla `cursos`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | BIGINT PK | Autoincremental |
| nivel | VARCHAR | ej: "1 Medio" |
| letra | VARCHAR | ej: "A" |

### Tabla `asignaturas`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | BIGINT PK | Autoincremental |
| nombre | VARCHAR | ej: "Matematicas" |
| docente_id | BIGINT NOT NULL | Ref. lógica a user-service |
| curso_id | BIGINT NOT NULL | FK física a `cursos` |

### Tabla `matriculas`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | BIGINT PK | Autoincremental |
| estudiante_id | BIGINT NOT NULL | Ref. lógica a user-service |
| curso_id | BIGINT NOT NULL | FK física a `cursos` |

### Tabla `calificaciones`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | BIGINT PK | Autoincremental |
| estudiante_id | BIGINT NOT NULL | Ref. lógica a user-service |
| asignatura_id | BIGINT NOT NULL | FK física a `asignaturas` |
| nota | NUMERIC(3,1) | 1.0 a 7.0 |
| descripcion | VARCHAR | |
| docente_id | BIGINT | Ref. lógica a user-service |

## Reglas de dominio

| Entidad | Regla |
|---------|-------|
| Curso | No se puede crear/actualizar con mismo nivel+letra |
| Curso | No se puede eliminar si tiene asignaturas o matrículas |
| Asignatura | El `docenteId` debe existir en user-service (vía WebClient) |
| Asignatura | No se puede eliminar si tiene calificaciones |
| Matrícula | El `estudianteId` debe existir en user-service |
| Matrícula | No se puede duplicar estudiante+curso |
| Calificación | Nota debe estar entre 1.0 y 7.0 |
| Calificación | El alumno debe estar matriculado en el curso de la asignatura |
| Calificación | Solo el docente que la creó puede editarla o eliminarla |

## JWT

- Claims: `roles` (lista de cadenas de autoridad), `userId` (Long)
- El token se lee de cookie `jwt-cookie` o header `Authorization: Bearer`
- Clave secreta: codificada en Base64 en `application.properties` (compartida con todos los servicios)
- `SecurityContextHolder`: `principal` = email, `credentials` = userId (Long)

## Configuración

Toda la configuración en `src/main/resources/application.properties`:
- Puerto: `9092`
- BD: `academy_db` en AWS RDS (`db-fullstack3.*.us-east-1.rds.amazonaws.com`)
- JWT secret: `586B633834416E396D7436753879382F423F4428482B4C6250655367566B5970`
- user-service URL: `http://user-service:9091` (docker) / `/api/v1/usuarios/%d/exists`

## Tests

**~60 tests**, 11 archivos en `src/test/java/`:

| Archivo | Tests | Tipo | Notas |
|---------|-------|------|-------|
| `CursoServiceTest` | 12 | Unitario (Mockito) | CRUD + reglas de negocio |
| `MatriculaServiceTest` | 7 | Unitario (Mockito) | CRUD + WebClient mockeado + duplicados |
| `AsignaturaServiceTest` | 8 | Unitario (Mockito) | CRUD + WebClient mockeado + factory |
| `CalificacionServiceTest` | 10 | Unitario (Mockito) | CRUD + rango nota + autorización docente |
| `CursoControllerTest` | 4 | `@WebMvcTest` | Endpoints + roles |
| `MatriculaControllerTest` | 3 | `@WebMvcTest` | Endpoints + roles |
| `AsignaturaControllerTest` | 4 | `@WebMvcTest` | Endpoints + roles |
| `CalificacionControllerTest` | 4 | `@WebMvcTest` | Endpoints + roles, SecurityContext manual |
| `GlobalExceptionHandlerTest` | 4 | Unitario (Mockito) | Todos los handlers de error |
| `JwtValidationFilterTest` | 4 | Unitario (Mockito) | Token válido en cookie/header, inválido, sin token |

**Frameworks:** JUnit 5, Mockito (strict mode con `lenient()` para stubs compartidos), AssertJ.

**Patrones:** AAA (Arrange-Act-Assert), `@BeforeEach` con builders, `@WebMvcTest` con `@MockitoBean` y `@WithMockUser`.

**Ejecución:**
```bash
./mvnw test   # ~60 tests, todos pasan
```

## Docker

```bash
docker build -t academy-service academy-service/
```

Compilación multi-etapa: Maven 3.9.6-eclipse-temurin-21 (build) → eclipse-temurin:21-jre-alpine (run). Expone puerto 9092.

## Integración con otros servicios

- **user-service**: consulta `GET /api/v1/usuarios/{id}/exists` para validar docentes y estudiantes
- **api-gateway**: enrutado en `http://academy-service:9092`, rutas `/api/v1/cursos/**`, `/api/v1/asignaturas/**`, `/api/v1/matriculas/**`, `/api/v1/calificaciones/**`
- **frontend**: páginas ADMIN para CRUD de cursos/asignaturas/matrículas/calificaciones, páginas DOCENTE para registro de notas

## JWT compartido

Misma clave secreta que `user-service`, `records-service` y `api-gateway`:
```
586B633834416E396D7436753879382F423F4428482B4C6250655367566B5970
```
