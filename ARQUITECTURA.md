# Bolsines — Clean Architecture + SQLite

> El proyecto está separado en `backend/` (API REST con Spring Boot) y `frontend/` (React + TypeScript). Ver [README.md](README.md) para levantarlo.

## Arquitectura

```
Frontend (React) ──HTTP──▶ Presentation (REST) → Controller → Service (Gestor) → Repository → Datasource → Entities
```

| Capa | Paquete | Rol |
|---|---|---|
| **Presentation** | `Presentation/Api` | Endpoints REST (`@RestController`) y DTOs. Traducen HTTP ↔ controller, sin lógica de negocio. |
| **Controller** | `Controllers` | Intermediario delgado. Delega todo al gestor. |
| **Service** | `Gestores` | Lógica de negocio del caso de uso. |
| **Repository** | `Domain/Repositories` (interfaces) | Contratos de acceso a datos. |
| **Datasource** | `Infrastructure/Datasources` | Implementación concreta (SQLite). |
| **Entities** | `Domain/Entities` | Modelo de dominio. Serializable. |

**Regla**: cada capa solo conoce a la inferior. Dependencies → constructor injection.

## Persistencia

SQLite embebido via `org.xerial:sqlite-jdbc` (dependencia Maven).

- DB: `backend/bolsines.db` (relativa al directorio desde donde se levanta el backend)
- Schema creado automáticamente en `DatabaseManager.inicializar()`
- Datos de prueba: `cargarDatosDePrueba()` se ejecuta solo si las tablas están vacías
- Para reiniciar: borrar `bolsines.db`

**Tablas**: `comision_medica`, `empleado`, `usuario`, `bolsin`, `remito`, `detalle_remito`, `documento`

## Entidades (alineadas al UML)

| Entidad | Atributos clave | Métodos clave del UML |
|---|---|---|
| `Estado` | ambito, nombre, descripcion | `esEnviado()`, `esAmbitoBolsin/Remito/Doc()`, `esEstadoRecibido*()` |
| `Bolsin` | numero, cantRemitos, nroPrecinto | `sosEnviado()`, `esTuCM()`, `obtenerInformacionRemito()`, `crearNuevoCEO()` |
| `Remito` | numero, fecha, cmOrigen, cmDestino | `buscarDocumentacion()`, `aceptar()` |
| `DetalleRemito` | areaCMDestino | `aceptarDocumentacion()` |
| `Documento` | fechaPase, asunto | `crear()`, `sosEnviado()`, `esRecibidaYAceptada()`, `rechazar()`, `mostrarTipoDocumentacion()` |
| `CambioEstado*` | fechaHoraInicio/Fin | `sosActual()`, `setFechaHoraFin()`, `cerrar()` |
| `ComisionMedica` | codigo, nombre, telefono | — |
| `Sesion` | + comisionMedica | — |

## Caso de uso: Registrar Recepción

1. `GET /api/recepcion/sesion` → la pantalla muestra usuario logueado y CM
2. `GET /api/recepcion/bolsines` → lista bolsines ENVIADO hacia la CM del usuario
3. Usuario selecciona uno → `GET /api/recepcion/bolsines/{n}` → info de remitos (origen, precinto, docs)
4. Opción Recibir/Rechazar + modal de confirmación → `POST /api/recepcion/bolsines/{n}/recibir`
5. Gestor cambia estados en cascada: Bolsín→RECIBIDO, Remitos→RECIBIDO_ACEPTADO, Docs→RECIBIDA_ACEPTADA
6. Persiste a SQLite
7. Al siguiente reinicio solo aparecen bolsines pendientes

## Compilar y ejecutar

```bash
cd backend && mvn spring-boot:run      # API en http://localhost:8080
cd frontend && npm install && npm run dev   # UI en http://localhost:5173
```

## Estructura

```
backend/
├── pom.xml
├── bolsines.db
└── src/main/java/main/
    ├── BolsinesApplication.java       (arranque Spring Boot)
    ├── Config/AppConfig.java          (composition root: arma las capas)
    ├── Presentation/Api/              (RestControllers + Dto)
    ├── Controllers/                   (intermediario)
    ├── Gestores/                      (caso de uso)
    ├── Domain/Entities/               (12 entidades UML)
    ├── Domain/Repositories/           (interfaces)
    └── Infrastructure/                (DatabaseManager, Datasources, RepositoryImpl)
frontend/
└── src/
    ├── features/                      (recepcion, bolsines, comisiones, usuarios, inicio)
    │   └── <feature>/{components,hooks,services,types}
    ├── shared/                        (componentes UI, axios, queryClient, utils)
    ├── router/                        (rutas)
    └── config/                        (variables de entorno)
```
