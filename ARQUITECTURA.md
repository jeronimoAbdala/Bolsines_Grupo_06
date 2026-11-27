# Bolsines — Arquitectura en capas + JPA (Hibernate)

> El proyecto está separado en `backend/` (API REST con Spring Boot) y `frontend/` (React + TypeScript). Tiene solo el CU 28, como el ejemplo de la cátedra: una Pantalla y un Gestor; los datos de prueba se cargan solos al arrancar. Ver [README.md](README.md) para levantarlo y [CASO_DE_USO_RECEPCION.md](CASO_DE_USO_RECEPCION.md) para el recorrido del CU 28.

## Capas

```
Frontend (React) ──HTTP──▶ Pantalla (Presentation) → Gestor → Repository (JpaRepository) → Hibernate → H2
                           (CU 28: entre la Pantalla y el Gestor viajan DTO; el Gestor trabaja con las entidades)
```

| Capa | Paquete | Rol |
|---|---|---|
| **Presentation** | `Presentation/Api` | `PantallaRegistrarRecepcionBolsin`: la Pantalla del diagrama del CU 28 es un `@RestController`. Cada mensaje del actor es un endpoint y cada mensaje Gestor → Pantalla es un método privado que arma la respuesta con los DTO que le da el Gestor (no toca entidades). Además, el manejo de errores (`ManejadorErrores`, `ApiException`). Sin lógica de negocio. |
| **Gestor** | `Gestores` | Lógica de los casos de uso, sin persistencia: `GestorRecepcionBolsin` (CU 28) y `GestorNotificarRecepcionBolsin` (CU 29, lo incluye el paso 12), los dos `@Service`. El del CU 28 es una sola instancia que guarda el estado del CU entre pedidos: lee la base una sola vez al iniciar el CU (`findAll()`) y escribe una sola vez al confirmar (`save()`), siempre con métodos de los repositorios; no usa `findById` ni `Optional`. Si hay una interfaz, tiene que haber un service: el Gestor es el `@Service` que usa las interfaces de los repositorios. |
| **Dto** | `Dto` | Records que entrega el Gestor y devuelve la API (`VentanaRecepcionDto`, `BolsinParaSeleccionDto`, `DetalleRecepcionDto`, `BolsinDto`, `RecepcionRegistradaDto`, `UsuarioDto`, …) y el cuerpo del POST de la opción de recepción (`Requests.OpcionDeRecepcion`). |
| **Repository** | `Repositories` | Una interfaz `@Repository` por entidad que extiende `JpaRepository<Entidad, Clave>`, sin código propio, como en el ejemplo de la cátedra: `IBolsinRepository`, `IComisionMedicaRepository`, `IEmpleadoRepository`, `IEstadoRepository`, `ITipoDocumentoRepository`, `IUsuarioRepository`. Spring Data las implementa (`findAll()`, `save()`, `findById()`, cada uno con su transacción). El Gestor usa las de Bolsin, Empleado y Estado (`findAll()` y `save()`); `CargadorDatosDePrueba` usa todas (`save()` y `findAll()`) e `InicioDeSesion` la de Usuario (`findById(...).orElse(null)`). |
| **Entities** | `Domain/Entities` | Modelo de dominio, mapeado a tablas con anotaciones JPA (`@Entity`). |
| **Iterator** | `Domain/Iterator` | Patrón Iterator del paso 6 del CU 28: interfaces `IIterador` e `IAgregado` e iteradores concretos `IteradorRemito` e `IteradorDetalle` (clases comunes, no se persisten). |
| **Config** | `Config` | `AppConfig` (CORS), `CargadorDatosDePrueba` e `InicioDeSesion` (tareas de arranque). |

**Regla**: la Pantalla le habla directo al Gestor y el Gestor a los repositorios; Spring inyecta las dependencias por constructor. La Pantalla no toca entidades: solo recibe DTO del Gestor. JPA queda en las anotaciones de las entidades y en las interfaces de los repositorios.

## Persistencia

**Spring Data JPA + Hibernate** (ORM) sobre **H2 embebido en modo archivo** (starter `spring-boot-starter-data-jpa`). Los repositorios son interfaces que Spring Data implementa; no hay consultas escritas a mano.

- Base: `jdbc:h2:file:./db/bolsines` → `backend/db/bolsines.mv.db` (relativa a la carpeta desde la que se levanta el backend; está en `.gitignore`).
- Esquema: lo crea y actualiza Hibernate a partir de las anotaciones de las entidades (`spring.jpa.hibernate.ddl-auto=update`): no hay SQL a mano.
- **Lecturas, una sola vez al iniciar el CU**: `opRegistrarRecepcionBolsin()` llama primero a `cargarDatos()` (privado), que carga en atributos del Gestor los empleados, los bolsines y los estados (`findAll()` de cada repositorio). La selección, la opción y la confirmación trabajan con esos objetos en memoria; la única escritura es `bolsinRepository.save(bolsin)` en el paso 11.
- **Listas EAGER + SUBSELECT**: el Gestor guarda los objetos de un pedido al siguiente, cuando ya no hay sesión de JPA para cargar algo que falte. Por eso las listas que usan el Iterator y `recibir()` (`Bolsin.remitos`, `Bolsin.cambiosEstado`, `Remito.detalleRemito`, `Documentacion.cambiosEstado`) son `@OneToMany(fetch = FetchType.EAGER)` + `@Fetch(FetchMode.SUBSELECT)`: `findAll()` trae los bolsines y, con una consulta más por lista, todas sus colecciones. `SUBSELECT` evita la `MultipleBagFetchException` que darían dos `List` EAGER en la misma entidad.
- `hibernate.default_batch_fetch_size=16`: las documentaciones de los detalles se cargan de a 16 en vez de una por una. `/iniciar` (los 3 `findAll()`) baja de **24 a 11 sentencias**.
- `spring.jpa.open-in-view=false`: la sesión de JPA dura lo que cada método del repositorio.
- `save(bolsin)` hace `merge` (la clave natural ya está puesta): como el bolsín viene de otro pedido, Hibernate hace sus propios SELECT para compararlo con la base antes de los UPDATE/INSERT (**10 para el 1003 y 13 para el 1001**). Son internos de `save()`, no consultas del Gestor; el batch no los reduce.
- Datos de prueba: `CargadorDatosDePrueba` los carga al iniciar, **solo si la base está vacía**, usando `save()` de los repositorios.
- Sesión: `InicioDeSesion` crea la `Sesion` del usuario configurado en `bolsines.usuario-logueado` (no hay login).
- Para reiniciar los datos: apagar el backend y borrar `backend/db/`. También hay que borrarla si cambia una clave: `ddl-auto=update` no migra claves primarias ni foráneas.
- Tests: `mvn test` usa una H2 **en memoria** (perfil `test`).

**Tablas (11)**: `estado`, `tipo_documento`, `comision_medica`, `usuario`, `empleado`, `bolsin`, `cambio_estado_bolsin`, `remito`, `detalle_remito`, `documentacion`, `cambio_estado_documentacion`.

**Claves**: ninguna entidad tiene id técnico ni `@GeneratedValue`; todas usan su clave natural, simple o compuesta con una clase `XxxId` + `@IdClass`, como en el ejemplo de la cátedra.

| Entidad | Clave primaria | Clase de la clave |
|---|---|---|
| `Bolsin`, `Remito`, `Documentacion` | `numero` | — |
| `ComisionMedica` | `codigo` | — |
| `Usuario` | `nombre_usuario` | — |
| `TipoDocumento` | `nombre` | — |
| `Estado` | `ambito` + `nombre` (el nombre solo no alcanza: "Enviado" es de bolsín y de remito) | `EstadoId` |
| `Empleado` | `nombre` + `apellido` | `EmpleadoId` |
| `CambioEstadoBolsin` | `bolsin_numero` + `fecha_hora_inicio` | `CambioEstadoBolsinId` |
| `CambioEstadoDocumentacion` | `documentacion_numero` + `fecha_hora_inicio` | `CambioEstadoDocumentacionId` |
| `DetalleRemito` | `remito_numero` + `documentacion_numero` | `DetalleRemitoId` |

En los cambios de estado y en el detalle, la clave incluye columnas de otras entidades, como el Laboreo de la cátedra: al recibir, varias documentaciones reciben un cambio con la misma fecha/hora, estado y empleado, y las distingue el número de su documentación. Esas columnas son `@Id` simples del hijo (el hijo no conoce a su dueño: solo guarda su número, que le pasa el constructor) y las escribe el hijo; por eso la `@JoinColumn` del `@OneToMany` del dueño es `insertable = false, updatable = false`, y en `DetalleRemito` también la de la `@ManyToOne documentacion`.

**Relaciones y cascadas**

| Relación | Mapeo | Cascada |
|---|---|---|
| Bolsin → Remito (composición) | `@OneToMany` EAGER + SUBSELECT, FK `remito.bolsin_numero` | ALL |
| Remito → DetalleRemito (composición) | `@OneToMany` EAGER + SUBSELECT, FK `detalle_remito.remito_numero` (parte de la clave del detalle) | ALL |
| DetalleRemito → Documentacion | `@ManyToOne` (FK `documentacion_numero`, parte de la clave del detalle) | PERSIST, MERGE |
| Bolsin → CambioEstadoBolsin (historial) | `@OneToMany` EAGER + SUBSELECT, FK `cambio_estado_bolsin.bolsin_numero` (parte de la clave del cambio) | ALL |
| Documentacion → CambioEstadoDocumentacion (historial) | `@OneToMany` EAGER + SUBSELECT, FK `cambio_estado_documentacion.documentacion_numero` (parte de la clave del cambio) | ALL |
| Empleado → Usuario | `@OneToOne` (FK `usuario_nombre`, única) | PERSIST, MERGE |
| → Estado (FK `estado_ambito` + `estado_nombre`), ComisionMedica, TipoDocumento (FK `tipo_documento_nombre`), Empleado (FK `empleado_nombre` + `empleado_apellido`, en los cambios de estado) | `@ManyToOne` | ninguna (catálogos / datos maestros) |

Con estas cascadas, un solo `bolsinRepository.save(bolsin)` persiste el paso 11 completo (estados de bolsín, remitos y documentación, y sus historiales) en una transacción.

## Entidades

| Entidad | Atributos clave | Métodos clave |
|---|---|---|
| `Estado` | ambito (BOL/REM/DOC), nombre, descripcion | `esEnviado()`, `esAmbitoBolsin/Remito/Documentacion()`, `esEstadoRecibidoEnCMDestino()`, `esEstadoRecibidoYAceptado()`, `esEstadoRecibidaYAceptada()` |
| `Bolsin` (`implements IAgregado`) | numero, fechaGeneracion, nroPrecinto, peso, estado, cmOrigen, cmDestino, remitos, cambiosEstado | `sosEnviado()` (por historial), `esTuCM()`, `crearIterador()` y `obtenerInformacionRemito(): String` (paso 6, con `IteradorRemito`), `recibir()`, `crearNuevoCE()` |
| `CambioEstadoBolsin` | bolsinNumero (clave), fechaHoraInicio, fechaHoraFin, estado, empleado | `sosActual()`, `sosEnviado()`, `setFechaHoraFin()` |
| `Remito` (`implements IAgregado`) | numero, fecha, estado, cmOrigen, cmDestino, detalleRemito (sin historial) | `crearIterador()` y `buscarDocumentacion(): String` (paso 6, con `IteradorDetalle`), `aceptar()` |
| `DetalleRemito` | remitoNumero y documentacionNumero (clave), areaCMDestino, documentacion | `getDocumentacion(): String` (asunto y tipo, paso 6), `getObjetoDocumentacion()` (el objeto, para el DTO), `aceptarDocumentacion()` |
| `Documentacion` | numero, fechaPase, asunto, descripcion, estado, tipoDocumento, cambiosEstado | `sosEnviado()` (por historial), `esRecibidaYAceptada()` (transición), `crearNuevoCE()`, `mostrarTipoDocumentacion()` |
| `CambioEstadoDocumentacion` | documentacionNumero (clave), fechaHoraInicio, fechaHoraFin, estado, empleado | `sosActual()`, `sosEnviado()`, `setFechaHoraFin()` |
| `TipoDocumento` | nombre, descripcion | — |
| `ComisionMedica` | codigo, nombre, direccion, email, telefono | — |
| `Empleado` | nombre, apellido, mail, comisionMedica, usuario | `esTuUsuario()`, `getCM()` |
| `Usuario` | nombre de usuario, password, fechaAlta | — |
| `Sesion` (no se persiste) | usuario, fechaHoraInicio | `getUsuario()` |

`cantRemitos` no es un atributo: es derivado y lo calcula el DTO (`remitos.size()`).

## Patrón Iterator (paso 6 del CU 28)

El paso 6 ("para cada remito muestra el número y, para cada documentación, el asunto y el tipo") se resuelve con el patrón **Iterator**, como en el diagrama de secuencia.

| Rol | Clase | Responsabilidad |
|---|---|---|
| Iterator | `IIterador` | `primero()`, `siguiente()`, `haFinalizado()`, `elementoActual(): Object` |
| ConcreteIterator | `IteradorRemito`, `IteradorDetalle` | Guardan su arreglo (`Remito[]` / `DetalleRemito[]`) y la `posicionActual`. El constructor recibe el `Object[]` de `toArray()` y lo copia al arreglo del tipo (`Arrays.copyOf`) |
| Aggregate | `IAgregado` | `crearIterador(elementos: Object[]): IIterador` |
| ConcreteAggregate | `Bolsin`, `Remito` | Crean su iterador concreto y recorren con él: `primero()`; `while (!haFinalizado())` { `elementoActual()` (con cast); …; `siguiente()` } |
| Client (del agregado) | `GestorRecepcionBolsin` | `obtenerInformacionRemito()` le delega a `bolsinSeleccionado.obtenerInformacionRemito()` y guarda el texto en `informacionRemito`; no conoce la colección ni los iteradores |

- **Recorrido anidado**: `Bolsin.obtenerInformacionRemito()` recorre los remitos con un `IteradorRemito`; por cada uno, `Remito.buscarDocumentacion()` recorre sus detalles con un `IteradorDetalle` propio y le pide a cada `DetalleRemito.getDocumentacion()` (que consulta a su Documentacion y esta a su TipoDocumento).
- El iterador es una **variable local** del método que lo crea: al terminar el recorrido queda sin referencias (la X del diagrama de secuencia).
- Las colecciones de las entidades siguen siendo `List` (JPA no mapea arreglos) y se pasan al iterador con `toArray()`. El patrón no cambia el esquema: el atributo `Remito.detalleRemito` se mapea con `@JoinColumn(name = "remito_numero")`.
- La API devuelve el texto en `informacionRemito` (`POST /api/recepcion/seleccion/{n}`, método `mostrarDatosRemito()` de la Pantalla) y el frontend lo muestra con `DatosRemito.tsx` en el panel del bolsín seleccionado. El modal de recepción exitosa sigue usando los DTO (estados nuevos de remitos y documentación).
- Se usa **solo en el paso 6** (lo acordado con el tutor); los demás recorridos (paso 3, paso 11, estados) siguen con `for` sobre `List`.
- En los datos de prueba el remito 500 tiene 3 documentaciones (un `DetalleRemito` por cada una), así el loop Detalles da varias vueltas.

**Filtro del paso 4 (flujos A1 y A2)**: se resuelve en el **frontend**, sobre la lista de bolsines pendientes que ya devuelve `POST /api/recepcion/iniciar` (`filtrarBolsines()` en `features/recepcion/utils/filtroBolsines.ts`, formulario `FiltroBolsines`). No hay método en el Gestor ni endpoint, porque no están en los diagramas.

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
    │   ├── Presentation/Api/          (PantallaRegistrarRecepcionBolsin, errores)
    │   ├── Dto/                       (records de la API y cuerpos de los POST)
    │   ├── Gestores/                  (CU 28 y CU 29)
    │   ├── Repositories/              (interfaces I…Repository que extienden JpaRepository)
    │   ├── Domain/Entities/           (entidades JPA y las clases de clave XxxId)
    │   └── Domain/Iterator/           (patrón Iterator: IIterador, IAgregado, IteradorRemito, IteradorDetalle)
    ├── main/resources/application.properties
    └── test/                          (RecepcionBolsinFlujoTest, tests unitarios de Domain/Iterator y
                                        del recorrido anidado en Domain/Entities, application-test.properties)
frontend/
└── src/
    ├── features/                      (recepcion: el CU 28; inicio: portada sin llamadas a la API)
    │   └── <feature>/{components,hooks,services,types}   (recepcion/utils: filtro del paso 4, A1 y A2)
    ├── shared/                        (componentes UI, axios, queryClient, utils)
    ├── router/                        (rutas)
    └── config/                        (variables de entorno)
```

## Pendiente

- Del CU 28: las opciones de recepción 2, 3 y 4 (A3, A4, A5).
- CU 29: no se mandan mails reales; la notificación queda en el log del backend.
