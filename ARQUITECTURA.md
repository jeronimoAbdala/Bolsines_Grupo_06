# Bolsines — Clean Architecture + SQLite

> El proyecto está separado en `backend/` (API REST con Spring Boot) y `frontend/` (React + TypeScript). Ver [README.md](README.md) para levantarlo.

## Arquitectura

```
Frontend (React) ──HTTP──▶ Presentation (REST) → Controller → Service (Gestor) → Repository → Datasource → Entities
```

| Capa | Paquete | Rol |
|---|---|---|
| **Presentation** | `Presentation/Api` | Endpoints REST (`@RestController`) y DTOs. Traducen HTTP ↔ controller, sin lógica de negocio. |
| **Controller** | `Controllers` | Intermediario delgado. `RecepcionBolsinController` expone solo los mensajes pantalla → gestor del CU 28; `AdministracionController` las altas de apoyo. |
| **Service** | `Gestores` | `GestorRecepcionBolsin`: control del CU 28, un método por mensaje del diagrama de secuencia. `GestorAdministracion`: altas y consultas de apoyo. |
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

**Tablas**: `comision_medica`, `empleado`, `usuario`, `estado`, `bolsin`, `cambio_estado_bolsin`, `remito`, `detalle_remito`, `documento`, `cambio_estado_documentacion`

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

El código sigue el diagrama de secuencia: el gestor coordina y cada entidad hace su parte (Experto).

1. `GET /api/recepcion/sesion` → `opRegistrarRecepcionBolsin()`: `buscarUsuarioLogueado()` (`actualSesion.getUsuario()` + `*esTuUsuario()` a cada empleado) y `getCM()` (`logueado.getCM()`)
2. `GET /api/recepcion/bolsines` → `buscarBolsinesEnviados()`: por cada bolsín `sosEnviado()` (`*sosActual()` → `actual.sosEnviado()` → `estado.esEnviado()`) y `esTuCM()`
3. `GET /api/recepcion/bolsines/{n}` → `tomarSeleccionBolsin()` + `obtenerInformacionRemito()`: Bolsín → `remito.getNumero()` / `remito.buscarDocumentacion()` → `detalle.getDocumentacion()` → `getAsunto()` / `mostrarTipoDocumentacion()`
4. `POST /api/recepcion/bolsines/{n}/recibir` → `tomarOpcionDeRecepcion()` + `tomarConfirmacion()`:
   - `obtenerEstadoRecibidoEnCMDestino()`, `obtenerEstadoRecibidoYAceptado()`, `obtenerEstadoRecibidaYAceptada()`: recorren los estados del repositorio preguntando `esAmbitoX()` y `esEstadoX()`
   - `getFechaYHoraActual()` y `registrarRecepcionBolsin()` → `bolsin.mostrarRecibido(...)`: cierra el cambio de estado actual (`setFechaHoraFin`), `crearNuevoCE()` y por cada remito `aceptar()` → `detalle.aceptarDocumentacion()` → `documentacion.aceptar()` (cierra su CE actual y crea uno nuevo)
   - `llamarCU29()` y `finCU()`
5. Se persiste el bolsín con sus remitos, documentación y cambios de estado (con fecha y empleado)

Las entidades nunca hacen `new Estado(...)`: los estados se buscan en la tabla `estado` y se pasan por parámetro. El estado de `Bolsin` y `Documento` es el de su cambio de estado actual, como en el diagrama de clases.

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
