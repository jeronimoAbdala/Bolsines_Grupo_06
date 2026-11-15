# Bolsines — Arquitectura en capas + Spring Data JPA

> El proyecto está separado en `backend/` (API REST con Spring Boot) y `frontend/` (React + TypeScript). Ver [README.md](README.md) para levantarlo y [CASO_DE_USO_RECEPCION.md](CASO_DE_USO_RECEPCION.md) para el recorrido del CU 28.

## Capas

```
Frontend (React) ──HTTP──▶ Presentation (REST) → Controller → Gestor → Repository (interfaz del dominio)
                                                                              ▲
                                                             RepositoryImpl ──┘ → Datasource (Spring Data JPA) → Hibernate → H2
                                                     (las entidades del dominio viajan entre todas las capas)
```

| Capa | Paquete | Rol |
|---|---|---|
| **Presentation** | `Presentation/Api` | Endpoints REST (`@RestController`), DTOs y manejo de errores. Traducen HTTP ↔ controller y validan la entrada, sin lógica de negocio. |
| **Controller** | `Controllers` | Intermediario delgado: delega en el gestor. |
| **Gestor** | `Gestores` | Lógica del caso de uso (`GestorRecepcionBolsin`). |
| **Repository** | `Domain/Repositories` | **Interfaces** del dominio para acceder a los datos (`BolsinRepository`, `UsuarioRepository`, `EmpleadoRepository`, `EstadoRepository`, `TipoDocumentoRepository`). No dependen de Spring. |
| **RepositoryImpl** | `Infrastructure/Repositories` | Capa de repositorio que separa el dominio de la tecnología de persistencia: implementa cada interfaz del dominio usando los datasources. |
| **Datasource** | `Infrastructure/Datasources` | Interfaces Spring Data (`XxxJpaDatasource extends JpaRepository<Entidad, Id>`); Spring genera su implementación. |
| **Entities** | `Domain/Entities` | Modelo de dominio, mapeado a tablas con anotaciones JPA (`@Entity`). |
| **Config** | `Config` | `AppConfig` (arma la cadena de capas con `@Bean`), `CargadorDatosDePrueba` e `InicioDeSesion` (tareas de arranque). |

**Regla**: cada capa conoce solo a la siguiente; las dependencias se inyectan por constructor. El Gestor depende de las **interfaces** del dominio, no de JPA.

## Persistencia

**Spring Data JPA + Hibernate** (ORM) sobre **H2 embebido en modo archivo**.

- Base: `jdbc:h2:file:./db/bolsines` → `backend/db/bolsines.mv.db` (relativa a la carpeta desde la que se levanta el backend; está en `.gitignore`).
- Esquema: lo crea y actualiza Hibernate a partir de las anotaciones de las entidades (`spring.jpa.hibernate.ddl-auto=update`): no hay SQL a mano.
- `spring.jpa.open-in-view=true`: la sesión de JPA queda abierta durante todo el request, así el gestor y los DTO pueden recorrer las colecciones (que se cargan bajo demanda, LAZY).
- Datos de prueba: `CargadorDatosDePrueba` los carga al iniciar, **solo si la base está vacía**, usando los repositorios.
- Sesión: `InicioDeSesion` crea la `Sesion` del usuario configurado en `bolsines.usuario-logueado` (no hay login).
- Para reiniciar los datos: apagar el backend y borrar `backend/db/`.
- Tests: `mvn test` usa una H2 **en memoria** (perfil `test`).

**Tablas (11)**: `estado`, `tipo_documento`, `comision_medica`, `usuario`, `empleado`, `bolsin`, `cambio_estado_bolsin`, `remito`, `detalle_remito`, `documentacion`, `cambio_estado_documentacion`.

**Relaciones y cascadas**

| Relación | Mapeo | Cascada |
|---|---|---|
| Bolsin → Remito (composición) | `@OneToMany` + FK `remito.bolsin_numero` | ALL |
| Remito → DetalleRemito (composición) | `@OneToMany` + FK `detalle_remito.remito_numero` | ALL |
| DetalleRemito → Documentacion | `@ManyToOne` (FK `documentacion_numero`) | PERSIST, MERGE |
| Bolsin → CambioEstadoBolsin (historial) | `@OneToMany` + FK `cambio_estado_bolsin.bolsin_numero` | ALL |
| Documentacion → CambioEstadoDocumentacion (historial) | `@OneToMany` + FK `cambio_estado_documentacion.documentacion_numero` | ALL |
| Empleado → Usuario | `@OneToOne` (FK `usuario_nombre`, única) | PERSIST, MERGE |
| → Estado, ComisionMedica, TipoDocumento, Empleado (en los cambios de estado) | `@ManyToOne` | ninguna (catálogos / datos maestros) |

Con estas cascadas, un solo `guardar(bolsin)` persiste el paso 11 completo (estados de bolsín, remitos y documentación, y sus historiales) en una transacción.

## Entidades

| Entidad | Atributos clave | Métodos clave |
|---|---|---|
| `Estado` | ambito (BOL/REM/DOC), nombre, descripcion | `esEnviado()`, `esAmbitoBolsin/Remito/Documentacion()`, `esEstadoRecibidoEnCMDestino()`, `esEstadoRecibidoYAceptado()`, `esEstadoRecibidaYAceptada()` |
| `Bolsin` | numero, fechaGeneracion, nroPrecinto, peso, estado, cmOrigen, cmDestino, remitos, cambiosEstado | `sosEnviado()` (por historial), `esTuCM()`, `recibir()`, `crearNuevoCE()` |
| `CambioEstadoBolsin` | fechaHoraInicio, fechaHoraFin, estado, empleado | `sosActual()`, `sosEnviado()`, `setFechaHoraFin()` |
| `Remito` | numero, fecha, estado, cmOrigen, cmDestino, detalles (sin historial) | `aceptar()`, `buscarDocumentacion()` |
| `DetalleRemito` | areaCMDestino, documentacion | `aceptarDocumentacion()` |
| `Documentacion` | numero, fechaPase, asunto, descripcion, estado, tipoDocumento, cambiosEstado | `sosEnviado()` (por historial), `esRecibidaYAceptada()` (transición), `crearNuevoCE()`, `mostrarTipoDocumentacion()` |
| `CambioEstadoDocumentacion` | fechaHoraInicio, fechaHoraFin, estado, empleado | `sosActual()`, `sosEnviado()`, `setFechaHoraFin()` |
| `TipoDocumento` | nombre, descripcion | — |
| `ComisionMedica` | codigo, nombre, direccion, email, telefono | — |
| `Empleado` | nombre, apellido, mail, comisionMedica, usuario | `esTuUsuario()`, `getCM()` |
| `Usuario` | nombre de usuario, password, fechaAlta | — |
| `Sesion` (no se persiste) | usuario, fechaHoraInicio | `getUsuario()` |

`cantRemitos` no es un atributo: es derivado y lo calcula el DTO (`remitos.size()`).

**Estados precargados** (nombres de los diagramas):

| Ámbito | Estados |
|---|---|
| BOL | Enviado, RecibidoEnCMDestino |
| REM | Enviado, RecibidoYAceptado |
| DOC | Registrado, EnRemito, EnBolsinSaliente, EnBolsinEnviado, NoRecibida, RecibidaYRechazada, ParaRedirigir, RecibidaYAceptada, Eliminado |

## Compilar y ejecutar

```bash
cd backend && mvn clean spring-boot:run     # API en http://localhost:8080
cd backend && mvn test                      # tests (H2 en memoria)
cd frontend && npm ci && npm run dev        # UI en http://localhost:5173
```

## Estructura

```
backend/
├── pom.xml
├── db/                                (base H2 en archivo, se crea al levantar; en .gitignore)
└── src/
    ├── main/java/main/
    │   ├── BolsinesApplication.java   (arranque Spring Boot)
    │   ├── Config/                    (AppConfig, CargadorDatosDePrueba, InicioDeSesion)
    │   ├── Presentation/Api/          (RestControllers, Dto, errores)
    │   ├── Controllers/               (intermediario)
    │   ├── Gestores/                  (caso de uso)
    │   ├── Domain/Entities/           (entidades JPA)
    │   ├── Domain/Repositories/       (interfaces del dominio)
    │   └── Infrastructure/
    │       ├── Repositories/          (RepositoryImpl)
    │       └── Datasources/           (interfaces Spring Data JPA)
    ├── main/resources/application.properties
    └── test/                          (RecepcionBolsinFlujoTest + application-test.properties)
frontend/
└── src/
    ├── features/                      (recepcion, bolsines, comisiones, usuarios, inicio)
    │   └── <feature>/{components,hooks,services,types}
    ├── shared/                        (componentes UI, axios, queryClient, utils)
    ├── router/                        (rutas)
    └── config/                        (variables de entorno)
```

## Pendiente

- **Patrón Iterator** (acordado para la entrega): todavía **no está implementado**. Hoy los recorridos de colecciones (bolsines, cambios de estado, remitos, detalles, estados) se hacen con `for` sobre `List`.
- Del CU 28: el filtro de bolsines por CM origen o precinto (paso 4, flujos A1 y A2), las opciones de recepción 2, 3 y 4 (A3, A4, A5) y el CU 29 del paso 12 (`llamarCU29()` está vacío).
