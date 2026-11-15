# Decisiones ORM — CU 28 "Registrar recepción de bolsín"

Registro de la migración de la persistencia JDBC/SQLite a un ORM (Spring Data JPA + Hibernate + H2).
Cada paso documenta **qué se cambió, por qué y los conceptos nuevos**, para estudiar para la defensa.

---

## 0. Estado de avance

- **Hechos:** pasos 1 a 11b (cada uno con `mvn -q compile` OK; el 11b además con `tsc -b` y `vite build` OK). Todas las entidades mapeadas (11 tablas), repositorios reales con Spring Data, nombres de estado de los diagramas, paso 3 del CU por historial, paso 11 del CU según el diagrama de secuencia (verificado en memoria con `jshell`), pantallas auxiliares con estados persistidos e historial inicial, y en el frontend las 4 opciones de recepción (A3b), `Badge.tsx` y el texto de `CrearBolsinPage`; el contexto de Spring arranca.
- **Paso 12 hecho:** `CargadorDatosDePrueba` + `InicioDeSesion`; **primera prueba en ejecución con la base de archivo OK** (sesión, pendientes, detalle, recibir con opción 1, 1001 ya no pendiente, 1002 sí, datos persistentes tras reiniciar). Sin errores de Java 26 / ByteBuddy.
- **Paso 13 hecho:** `RecepcionBolsinFlujoTest` (`@SpringBootTest` + MockMvc, H2 en memoria, perfil `test`): **9 tests, 0 fallas** (`mvn test` → BUILD SUCCESS); no toca `backend/db`.
- **Paso 14 hecho (esperando revisión):** README.md, ARQUITECTURA.md y CASO_DE_USO_RECEPCION.md reescritos para la persistencia nueva y el CU 28 web; sin SQLite, sin consola H2 ni SQL en el README, sin patrones de Gamma salvo el Iterator como **pendiente**.
- **Paso 15 hecho — migración terminada.** `mvn test` 9/9; app levantada con base en archivo vacía (instancia de verificación en el puerto 8099 con base aparte, porque el backend y el frontend del usuario estaban corriendo); los 4 endpoints OK; base consultada con la herramienta de H2; reinicio OK (solo queda pendiente el 1002).
- **Plan restante:** ninguno. Quedan los pendientes de la sección 5 (diagramas, tutor y otras tareas).
- **Temporales a quitar:** ninguno (desde el paso 11 no queda ningún `new Estado(...)` ni `new TipoDocumento(...)` en `src`).
- **Forma de trabajo:** un paso por vez, explicando cada anotación, esperando OK; preguntar antes de cualquier decisión de diseño nueva; actualizar este archivo después de cada paso; no tocar git.
- **No mencionar patrones de Gamma (GoF)** en este archivo ni en el código, salvo el Iterator (el acordado para la entrega). Los `RepositoryImpl` se describen como "capa de repositorio que separa el dominio de la tecnología de persistencia".

---

## 1. Objetivo de la tarea

Reemplazar la persistencia JDBC directa sobre SQLite (`SqliteBolsinDatasource`, `SqliteUsuarioDatasource`, `DatabaseManager`) por un **framework de persistencia (ORM)**, manteniendo:

- el comportamiento del CU 28 desde el frontend (mismos endpoints, misma forma de los JSON);
- la separación por capas (interfaces de repositorio en `Domain/Repositories`);
- la lógica del `GestorRecepcionBolsin`, salvo lo necesario para usar JPA y lo que piden los diagramas corregidos.

Fuera de alcance: patrón Iterator y cualquier funcionalidad no listada.

---

## 2. Decisiones

### 2.1 Decisiones iniciales (consigna)

| # | Decisión | Motivo |
|---|---|---|
| 1 | ORM: **Spring Data JPA + Hibernate** | Estándar del ecosistema Spring; Hibernate es la implementación de JPA que trae Spring Boot. |
| 2 | Base: **H2 en modo archivo** | Base embebida (no hay que instalar un servidor) y los datos sobreviven a los reinicios. |
| 3 | **Estado como entidad con tabla propia**, con los estados precargados; Bolsin, Remito y Documentacion lo referencian | Los estados son objetos compartidos (como en el diagrama), no copias nuevas con `new Estado(...)`. |
| 4 | **Persistir el historial** (CambioEstadoBolsin, CambioEstadoDocumentacion) con fecha/hora y el empleado logueado | Paso 11 del CU 28; hoy se pasaba `null` como empleado. |
| 5 | **Datos de prueba con una clase Java** al iniciar, solo si la base está vacía, usando los repositorios | Reemplaza a `cargarDatosDePrueba()` del datasource SQLite sin escribir SQL a mano. |
| 6 | Renombrar **Documento → Documentacion** (clase, DTO, tabla, referencias) | Coincidir con los diagramas de diseño. |
| 7 | Eliminar Sqlite*, DatabaseManager, Memory*, `data/*.dat`, `backend/bolsines.db` y `sqlite-jdbc` | Persistencia vieja que deja de usarse. |
| 8 | Actualizar ARQUITECTURA.md, README.md y CASO_DE_USO_RECEPCION.md | Que la documentación describa la persistencia nueva. |

Restricciones: no modificar el frontend (excepción posterior en A3), mismos endpoints/JSON, no tocar la lógica del gestor salvo lo necesario, colecciones como `List` (JPA no mapea arreglos `Remito[]`), no tocar git.

### 2.2 Cambios de diseño que vienen de los diagramas (secuencia corregida, clases, DME de Documentación)

| Cambio | Motivo |
|---|---|
| Bolsin y Documentacion tienen **atributo `estado`** (se actualiza con `setEstado()`) **y además historial** de cambios de estado | Diagrama de clases. |
| Las **consultas de estado van por el historial**: `Bolsin.sosEnviado()` recorre `*sosActual()`, al actual le pregunta `sosEnviado()`, que le pregunta a su Estado `esEnviado()` | Diagrama de secuencia corregido (paso 3). |
| **Remito tiene solo atributo `estado`, sin historial** | No existe `CambioEstadoRemito` en el diagrama. |
| Paso 2: el Gestor pide `getUsuario()` a la **Sesion actual** y recorre los empleados con `*esTuUsuario(usuario)`; el encontrado es el empleado logueado | Diagrama de secuencia corregido. |
| La relación es **Empleado → Usuario** (Empleado conoce a su Usuario), no al revés | Diagrama de clases. |
| `getCM()`: el Gestor se lo pide a sí mismo, que se lo pide al empleado logueado, que le pide `getNombre()` a su ComisionMedica → **devuelve String** | Diagrama de secuencia (paso 2). |
| Paso 3: el repositorio trae los bolsines y el **Gestor los recorre** con `sosEnviado()` y `esTuCM(cm)`; **nada de filtrar por estado en una @Query** | Diagrama de secuencia (responsabilidad en el dominio, no en la base). |
| Paso 11: `EstadoRepository` devuelve **todos** los estados y los `obtenerEstadoX()` del Gestor los **recorren** preguntando `esAmbitoX()` y `esEstadoY()` (3 loops); nada de buscar por ámbito/nombre en una consulta | Diagrama de secuencia. |
| `Bolsin.mostrarRecibido()` → **`recibir()`**; `Bolsin.crearNuevoCEO()` → **`crearNuevoCE()`** | Nombres del diagrama. |
| `Bolsin.recibir()`: PRIMERO `remito.aceptar(...)` en cada remito; DESPUÉS `setEstado()`, `*sosActual()`, `setFechaHoraFin()` al actual y `crearNuevoCE()` (`new CambioEstadoBolsin(estado, fechaHora, empleado)`) | Orden exacto del diagrama de secuencia. |
| `Remito.aceptar(...)`: `setEstado()` y después `detalle.aceptarDocumentacion(...)` en cada detalle | Diagrama de secuencia. |
| `DetalleRemito.aceptarDocumentacion(...)` delega a la Documentacion | Diagrama de secuencia. |
| La transición de Documentacion a RecibidaYAceptada se llama **`esRecibidaYAceptada()`** (nombre de la transición en la DME): `setEstado()`, `*sosActual()`, `setFechaHoraFin()`, `crearNuevoCE()` | DME de Documentación. |
| Se sacan `aceptarRemitos()`/`aceptarDocumentacion()` del Gestor: los recorridos los hacen Bolsin y Remito | Responsabilidad del experto en información (diagrama). |
| Orden en el Gestor: `tomarConfirmacion()` → `obtenerEstadoRecibidoEnCMDestino()` → `obtenerEstadoRecibidoYAceptado()` → `obtenerEstadoRecibidaYAceptada()` → `getFechaYHoraActual()` → `registrarRecepcionBolsin()` (este llama a `recibir(...)` y guarda) | Diagrama de secuencia corregido (ver P4). |
| **Estados con los nombres de los diagramas**: Documentación (DME): Registrado, EnRemito, EnBolsinSaliente, EnBolsinEnviado, NoRecibida, RecibidaYRechazada, ParaRedirigir, RecibidaYAceptada, Eliminado. Bolsín: Enviado, RecibidoEnCMDestino. Remito: Enviado, RecibidoYAceptado | DME y diagramas; "enviada" para Documentación es EnBolsinEnviado. |

### 2.3 Decisiones de diseño D1–D18

| # | Decisión | Motivo |
|---|---|---|
| D1 | Interfaces del dominio (`Domain/Repositories`) **sin Spring**; interfaces Spring Data (`extends JpaRepository`) en `Infrastructure/Datasources`; los `RepositoryImpl` forman la capa de repositorio que separa el dominio de la tecnología de persistencia | El dominio no depende del framework; se mantiene la cadena Repository → Datasource. |
| D2 | **Claves naturales asignadas** donde existen (`ComisionMedica.codigo`, `Bolsin.numero`, `Remito.numero`, `Documentacion.numero`, `Usuario` = nombre de usuario); **`Long id` generado (IDENTITY)** en Estado, Empleado, DetalleRemito, CambioEstado*, TipoDocumento | Esos números ya los asigna el sistema o la pantalla; las clases sin identificador natural necesitan uno técnico. |
| D3 | Relaciones **unidireccionales** con `@JoinColumn` (Remito no conoce a su Bolsin; Usuario no conoce a su Empleado) | Respetar la navegabilidad de los diagramas sin agregar atributos. |
| D4 | `cascade = ALL` en las composiciones (Bolsin→remitos, Remito→detalles) y en los historiales (Bolsin→cambiosEstado, Documentacion→cambiosEstado). **PERSIST + MERGE** en DetalleRemito→Documentacion y Empleado→Usuario. **Sin cascada** hacia Estado, ComisionMedica, TipoDocumento | Composición = el todo maneja el ciclo de vida de las partes; asociación = no se borra en cascada; los catálogos no se modifican desde otras entidades. |
| D5 | Colecciones **LAZY**, `@ManyToOne`/`@OneToOne` **EAGER** (defaults de JPA) y `spring.jpa.open-in-view=true` | EAGER en varias `List` da `MultipleBagFetchException`; open-in-view deja navegar lo LAZY durante el request (DTO y gestor). |
| D6 | `spring.jpa.hibernate.ddl-auto=update` | Crea/ajusta las tablas sin borrar datos entre reinicios. |
| D7 | Archivo H2 en `jdbc:h2:file:./db/bolsines` (→ `backend/db/bolsines.mv.db`), `backend/db/` en `.gitignore` | Persistencia entre reinicios; los datos no se versionan. |
| D8 | Consola H2 habilitada en `/h2-console` | Inspeccionar tablas e historial con SQL. |
| D9 | **TipoDocumento como entidad** con tabla y catálogo precargado | En el diagrama es una clase asociada a Documentacion. |
| D10 | Los estados se pasan **por parámetro** a las entidades (no más `new Estado(...)` adentro) | Las entidades no acceden a repositorios; el Gestor obtiene los estados persistidos. |
| D11 | Documentacion tiene `crearNuevoCE()`, llamado por `esRecibidaYAceptada()` | Nombres del diagrama; hoy nunca se creaba un CambioEstadoDocumentacion. |
| D12 | **Cerrar el cambio actual** con `setFechaHoraFin()` (Bolsín y Documentación). Datos de prueba con cambio de estado inicial (Bolsín: Enviado; Documentación: EnBolsinEnviado), atributo `estado` coherente y un **empleado de CM Buenos Aires** como responsable | Historial coherente con el diagrama (`*sosActual` + `setFechaHoraFin`). |
| D13 | Remito **sin historial** | No hay `CambioEstadoRemito` en el diagrama (no se inventa). |
| D14 | Estados precargados según P6 | Nombres de la DME y diagramas. |
| D15 | `main.Config.CargadorDatosDePrueba implements ApplicationRunner`; carga todo si no hay ningún Estado | Usa los repositorios; solo con base vacía. |
| D16 | Agregar un test `@SpringBootTest` con H2 **en memoria** | Verificar el CU sin tocar la base de archivo (no había tests). |
| D17 | Dejar `Serializable` en las entidades | Mínimo cambio; no molesta. |
| D18 | No tocar `.idea/dataSources.xml` | Archivo del IDE, fuera de alcance. |

### 2.4 Decisiones P1–P12 y respuestas A1–A5

| # | Decisión | Motivo |
|---|---|---|
| A1 | Se **elimina** el boolean `Documentacion.esRecibidaYAceptada()` (no se usaba) y el nombre pasa a la transición | Evitar dos métodos con el mismo nombre y distinto significado. |
| A2 | `mostrarRecibido()` (devolvía un String, no se usaba) se reemplaza por `recibir()`; se descarta su cuerpo | Nombre y comportamiento del diagrama. |
| A3 | Del frontend **solo** se toca `Badge.tsx` (amarillo: Enviado/EnBolsinEnviado; verde: Recibido…/RecibidaYAceptada) y el texto fijo "estado ENVIADO" de `CrearBolsinPage` | Los nombres de estado cambian de texto; la forma del JSON no. |
| A3b | (después del paso 11) **La opción de recepción la elige el usuario** (paso 7 de la ERS). En el panel del bolsín seleccionado, los botones "Recibir bolsín" y "Rechazar" se reemplazan por las 4 opciones de la ERS y un botón para continuar. Opción 1 → se abre la confirmación existente (paso 9) y al confirmar se hace el POST con la opción; opciones 2, 3, 4 → aviso "Opción no implementada" y nada más. Backend: el POST de recibir recibe la opción y el Controller llama `tomarOpcionDeRecepcion(opcion)` (ya no el 1 fijo); si la opción no es 1 responde un error claro sin cambiar nada. Cancelar en la confirmación sigue siendo el flujo alternativo A6. **Amplía A3**: del frontend se pueden tocar solo los componentes de la pantalla de recepción necesarios para esto, más `Badge.tsx` y `CrearBolsinPage` | Respetar la ERS (paso 7: el usuario elige la opción) y la secuencia `tomarOpcionDeRecepcion` → `solicitarConfirmacion` → `tomarConfirmacion`; la API no debe permitir una opción no implementada aunque se la llame directo. |
| A4 | `UsuarioDto` y `/api/usuarios` se arman desde **Empleado** (que conoce a su Usuario); mismo JSON | Consecuencia de la dirección Empleado → Usuario. |
| A5/P3 | El Gestor filtra con `sosEnviado()` y `esTuCM(cm)`; origen y precinto los lee el DTO; **no** se renombra `getCmOrigen()` | Evitar llamadas cuyo resultado se descarta; el renombre es de otra tarea. |
| P1 | `Sesion` es clase de dominio **no persistida**. `main.Config.InicioDeSesion` (ApplicationRunner, `@Order(2)`, después de `CargadorDatosDePrueba` `@Order(1)`) busca el Usuario de `bolsines.usuario-logueado`, crea `new Sesion(usuario)` y hace `gestor.setSesionActual(sesion)`. El método del Gestor se llama **`buscarUsuarioLogueado()`** (diagrama): `sesionActual.getUsuario()` + recorrido `*esTuUsuario(usuario)` | No hay login; se simula la sesión con el usuario configurado. |
| P2 | Opción (a), literal al diagrama: `Empleado.getCM()` devuelve `String` (`comisionMedica.getNombre()`), `Bolsin.esTuCM(String)` compara por nombre | Diagrama de secuencia; el DTO sigue usando `getComisionMedica()` para el JSON completo. |
| P4 | Orden: `tomarConfirmacion()` → 3 × `obtenerEstado…()` → `getFechaYHoraActual()` → `registrarRecepcionBolsin()` (llama a `recibir(...)` y guarda). Se mantiene `registrarRecepcionBolsin()`. El POST de recibir pasa por `tomarConfirmacion()` | Diagrama de secuencia corregido. |
| P5 | Firmas: `Bolsin.recibir(estBolsin, estRemito, estDoc, empleado, fechaHora)`, `Remito.aceptar(estRemito, estDoc, empleado, fechaHora)`, `DetalleRemito.aceptarDocumentacion(estDoc, empleado, fechaHora)`, `Documentacion.esRecibidaYAceptada(estDoc, fechaHora, empleado)`. Constructores de CambioEstado*: `(estado, fechaHoraInicio, empleado)` | El cierre del cambio actual y el alta del nuevo usan **la misma** fecha/hora del Gestor. |
| P6 | Estados precargados (ver tabla abajo), ámbitos `BOL`/`REM`/`DOC`; `esEnviado()` = `Enviado` o `EnBolsinEnviado`; los `esEstadoX()` comparan **solo el nombre** | El Gestor pregunta el ámbito por separado (`esAmbitoX()`); no duplicar responsabilidad. |
| P7 | `darBaja`, `cancelarRemito`, `rechazar`, `marcarNoRecibida`, `devolverAOrigen` solo pasan a recibir el `Estado` por parámetro (sin historial ni renombres). `Documentacion.sosEnviado()` va por historial | Son de otros CU; no se usan. Consistencia con el punto 1 de los diagramas. |
| P8 | TipoDocumento: `Long id`, `nombre` único, catálogo `EXPEDIENTE`, `INFORME`, `CERTIFICADO`, `OTRO`; `Documentacion → TipoDocumento` `@ManyToOne` sin cascada | Mismos nombres que valida la API y manda el frontend (JSON `tipo` igual). |
| P9 | Empleado de Buenos Aires: **María López**, usuario `mlopez` / `1234` | Responsable de los envíos en los datos de prueba (D12). |
| P10 | Pantallas auxiliares: el **Gestor** construye el Bolsin con estado Bolsín/Enviado + cambio de estado inicial; al agregar remito: Remito/Enviado y Documentación DOC/EnBolsinEnviado + cambio de estado inicial; tipo desde el catálogo; **responsable = empleado logueado**; se elimina `crearBolsin(...)` del repositorio | Toda Documentación y Bolsín nuevos nacen con historial coherente. |
| P11 | PERSIST + MERGE en Empleado→Usuario y DetalleRemito→Documentacion | Ver explicación del paso 5 y paso 6. |
| P12 | Orden de pasos: eliminar la persistencia vieja **al principio** (paso 2) con `RepositoryImpl` esqueleto hasta el paso 8 | Evitar parchear código que se iba a borrar; todo compila en cada paso, pero la app no funciona hasta el paso 12. |

**Estados precargados (P6):**

| Ámbito | Nombre | Descripción |
|---|---|---|
| BOL | Enviado | Bolsín enviado desde comisión de origen |
| BOL | RecibidoEnCMDestino | Bolsín recibido en comisión destino |
| REM | Enviado | Remito enviado |
| REM | RecibidoYAceptado | Remito recibido y aceptado |
| DOC | Registrado | Documentación registrada |
| DOC | EnRemito | Documentación incluida en un remito |
| DOC | EnBolsinSaliente | Documentación en bolsín saliente |
| DOC | EnBolsinEnviado | Documentación en bolsín enviado |
| DOC | NoRecibida | Documentación no recibida |
| DOC | RecibidaYRechazada | Documentación recibida y rechazada |
| DOC | ParaRedirigir | Documentación para redirigir |
| DOC | RecibidaYAceptada | Documentación recibida y aceptada |
| DOC | Eliminado | Documentación eliminada |

### 2.5 Decisiones tomadas durante los pasos

| Paso | Decisión | Motivo |
|---|---|---|
| 1 | `show-sql` + `format_sql` activados mientras desarrollamos | Ver el SQL que genera cada `save()` para aprender. |
| 3 | Renombrar también el atributo `DetalleRemito.documento` → `documentacion` | Coincidir con su getter `getDocumentacion()` y la clase. |
| 4 | `nullable = false` en `Estado.ambito`, `Estado.nombre`, `TipoDocumento.nombre` | Un estado/tipo sin esos datos no tiene sentido. |
| 4/5 | Nombre explícito `uk_tipo_documento_nombre` para el UNIQUE | Legibilidad del esquema (en vez de `UKemy1oj…`). |
| 5 | Multiplicidades **1**: Empleado→ComisionMedica y Empleado→Usuario con `optional = false` + `nullable = false` | Diagrama de clases. |
| 5 | Nuevo `EmpleadoRepository` (dominio); se saca `crearUsuario(...)` de `UsuarioRepository` | El Gestor recorre empleados; el alta guarda el Empleado y la cascada guarda el Usuario. |
| 5 | `buscarUsuarioLogueado()` vuelve a buscar en cada llamada | Cada request HTTP es independiente; el empleado queda en la sesión JPA del request actual. |
| plan | Paso extra para el frontend (A3) después del paso 11 | Cuando ya existen los nombres de estado nuevos. |
| 6 | Multiplicidades **1** (`optional = false` + `nullable = false`): Documentacion→Estado, Documentacion→TipoDocumento, Remito→Estado, Remito→CM origen/destino, DetalleRemito→Documentacion, CambioEstadoDocumentacion→Estado, CambioEstadoDocumentacion→Empleado | Diagrama de clases; con D12 todo cambio de estado tiene empleado. |
| 6 | DetalleRemito → Documentacion es **`@ManyToOne`** (no `@OneToOne`) | En la DME una documentación ParaRedirigir puede ir en otro remito: la misma Documentacion en varios DetalleRemito. |
| 6 | FK de los hijos de colecciones unidireccionales **NOT NULL** (`@JoinColumn(nullable = false)`): `detalle_remito.remito_numero`, `cambio_estado_documentacion.documentacion_numero` | Composición/historial: el hijo no existe sin su dueño; además Hibernate hace un solo `INSERT` por hijo. |
| 6 | `@OrderBy("id")` en `Remito.detalles` y `@OrderBy("fechaHoraInicio")` en `Documentacion.cambiosEstado` | La base no garantiza orden sin `ORDER BY`; detalles en orden de alta, historial cronológico. |
| 7 | Multiplicidades **1** (`optional = false` + `nullable = false`): Bolsin→Estado, Bolsin→cmOrigen, Bolsin→cmDestino, CambioEstadoBolsin→Estado, CambioEstadoBolsin→Empleado | Diagrama de clases. |
| 7 | FK `remito.bolsin_numero` **NOT NULL** | En nuestro diagrama de clases Bolsin → Remito es **composición** y en esta app un remito siempre se crea dentro de un bolsín. (La DME sugiere lo contrario: ver 5.2.) |
| 7 | FK `cambio_estado_bolsin.bolsin_numero` **NOT NULL** | El historial no existe sin su bolsín (igual que en Documentación). |
| 7 | `@OrderBy("numero")` en `Bolsin.remitos` y `@OrderBy("fechaHoraInicio")` en `Bolsin.cambiosEstado` | Remitos por número; historial cronológico. |
| 7 | **Se elimina `cantRemitos` de `Bolsin`** (atributo, `getCantRemitos()`, columna `cant_remitos` y su actualización en el constructor y en `agregarRemito`). `BolsinDto` lo calcula con `b.getRemitos().size()`, así el JSON sigue teniendo `"cantRemitos"` y el frontend no cambia | Es un **atributo derivado** (se calcula con la cantidad de remitos) y el profe lo marcó con "¿por qué?" en el diagrama de clases. Guardarlo duplica información que puede quedar desactualizada. |
| 7 | Finales de línea: se pasaron a **CRLF** 15 archivos que habían quedado en LF en pasos anteriores (y este .md) | Regla de preservar CRLF. En git no cambia nada: con `core.autocrlf=true` el repo guarda LF y la copia de trabajo usa CRLF. |
| 8 | (1a) `RemitoJpaDatasource` y `DocumentacionJpaDatasource` propios, cada uno con su `@Query` de siguiente número | Cada datasource consulta su propia entidad. |
| 8 | (2a) Se mantienen `obtenerComisionesMedicas()`/`crearComisionMedica()` en `BolsinRepository` **y** `UsuarioRepository`; ambas `Impl` delegan en un único `ComisionMedicaJpaDatasource` | No tocar el constructor ni la lógica del Gestor. La duplicación queda como pendiente (5.3). |
| 8 | (3a) `RepositoryImpl`, Gestor y Controller se registran con **`@Bean` en `AppConfig`** (composition root); los `Impl` no llevan anotaciones de Spring | Las dependencias entre capas quedan explícitas en un solo lugar, como antes con `Main`. |
| 8 | (4a) `BolsinRepository.crearBolsin(...)` sigue tirando `UnsupportedOperationException` hasta el paso 11 | Se elimina por P10; hoy arma el bolsín con un `new Estado(...)` no persistido (daría `TransientPropertyValueException`). |
| 8 | `obtenerEnviadosHacia` (temporal hasta el paso 9) se implementa con `findAll()` + filtro en Java | Sostiene el puente sin `@Query` por estado; se borra en el paso 9. |
| 8 | Siguiente número con las **mismas bases** que el SQLite viejo: bolsín `max+1` desde 1000, remito desde 500, documentación desde 0 | Mismo comportamiento (el primer bolsín creado es el 1001). |
| 8 | `findAll()` sin orden en bolsines, CMs, empleados | El SQLite viejo hacía `SELECT *` sin `ORDER BY`; no se agregó comportamiento. |
| 8 | `buscarPorNumero`/`buscarPorNombre` devuelven **`null`** si no existe (`findById(...).orElse(null)`) | Mismo contrato que el datasource SQLite; el Gestor y la API ya manejan el `null`. |
| 9 | `buscarBolsinesEnviados()` pide `getCM()` **una vez antes del recorrido** y le pasa ese nombre a cada `esTuCM(cm)` | `getCM()` vuelve a buscar el empleado logueado (paso 5); pedirlo por cada bolsín repetiría la consulta sin cambiar el resultado. |
| 9 | `sosEnviado()` devuelve `false` si el bolsín/documentación **no tiene cambio de estado actual** | Sin historial no hay estado actual que consultar. Con P10 y D12 todo bolsín y documentación nace con su cambio de estado inicial. |
| 9 | Se elimina `obtenerEnviadosHacia` de `BolsinRepository` y de su `Impl` (y el puente del Gestor) | El filtro lo hace el dominio (diagrama del paso 3). |
| 10 | `buscarBolsinesEnviados()` guarda la lista en el atributo **`bolsinesSeleccionados`** (ahora `List<Bolsin>`) y la devuelve | Es atributo del Gestor en el diagrama de clases (pedido después del paso 9). |
| 10 | `tomarConfirmacion(false)` solo registra la opción y no sigue | El diagrama cubre la confirmación; la API siempre confirma (`true`). |
| 10 | `registrarRecepcionBolsin()` usa `buscarUsuarioLogueado()` como responsable de los cambios de estado | Decisión del paso 5: así el empleado pertenece a la sesión JPA del request del POST (el Gestor es una sola instancia y sus atributos pueden venir de un request anterior). |
| 10 | Cada `obtenerEstado…()` pone su atributo en `null` antes del recorrido y corta (`break`) al encontrarlo | No reutilizar un estado de un request anterior; el recorrido es el del diagrama. |
| 10 | En `recibir()` / `esRecibidaYAceptada()` se cierran **todos** los cambios que `sosActual()` (normalmente uno) | Si por un dato inconsistente hubiera dos "actuales", no quedan abiertos. |
| 10 | Se mantiene `crearNuevoCE()` devolviendo el cambio creado (como `crearNuevoCEO`) | Mínimo cambio; nadie usa el valor devuelto. |
| 10 | `cerrar()` de los CambioEstado* (usa `now()`) no se usa en ningún lado; **no se tocó** | No está en el pedido; el cierre del paso 11 usa `setFechaHoraFin(fechaHora)` del diagrama. |
| 10 | Renombre `llamarCU290()` → **`llamarCU29()`** (sin usos; sigue vacío) | Nombre del diagrama de secuencia. |
| 10 | El POST de recibir llama `tomarSeleccionBolsin(n)` → **`tomarOpcionDeRecepcion(1)`** → `tomarConfirmacion(true)`. `solicitarConfirmacion` es un mensaje a la pantalla (en el frontend), no un método del backend | Secuencia: `tomarOpcionDeRecepcion` → `solicitarConfirmacion` → `tomarConfirmacion`. Opción 1 = "el contenido del bolsín es igual al registrado"; las otras 3 en 5.3. |
| 11 | El Gestor obtiene los estados iniciales recorriendo `estadoRepository.obtenerTodos()` con **métodos que ya existen**: `esAmbitoBolsin() && esEnviado()` (Bolsín/Enviado), `esAmbitoRemito() && esEnviado()` (Remito/Enviado), `esAmbitoDocumentacion() && esEnviado()` (EnBolsinEnviado). Tres métodos **privados** del Gestor | Mismo estilo de recorrido que el paso 11; no se agregan métodos a `Estado`. Como `esEnviado()` es "Enviado" o "EnBolsinEnviado" y cada uno existe en un solo ámbito, la combinación es única. |
| 11 | El tipo de documento se busca recorriendo `tipoDocumentoRepository.obtenerTodos()` y comparando `getNombre()` (método privado `buscarTipoDocumento`) | La API ya valida que el nombre sea uno del catálogo (P8); no se agregan métodos a `TipoDocumento`. |
| 11 | El Gestor recibe `TipoDocumentoRepository` por constructor (y su `@Bean`) | Lo necesita la pantalla de agregar remito. |
| 11 | Una sola `getFechaYHoraActual()` por alta: el bolsín (o la documentación) nace con su cambio de estado inicial con esa fecha/hora y `fechaHoraFin = null`; la fecha del remito sigue siendo `LocalDate.now()` | Igual criterio que el paso 11 del CU; la fecha del remito no cambia de comportamiento. |
| 11 | Se elimina `crearBolsin(...)` de `BolsinRepository` y de su `Impl` (P10) | El Gestor construye el Bolsin y lo guarda con `guardar()`. |
| 11b | La opción viaja en el **cuerpo JSON** del POST de recibir: `{"opcion": 1}` (`Requests.RecibirBolsin(int opcion)`) | Igual que los otros POST de la API (todos usan cuerpo JSON). |
| 11b | (1a) La regla "solo la opción 1" está **en dos capas**: `RecepcionRestController` valida y responde el error; además `Gestor.tomarConfirmacion()` solo sigue si `opcionDeRecepcion == 1` | Aunque alguien llame al Controller sin pasar por la API, no cambia nada. |
| 11b | (2a) Opción 2, 3 o 4 → **501 Not Implemented** "Opcion de recepcion no implementada: N." (nuevo `ApiException.noImplementado`); opción fuera de 1..4 (o `{}`, que llega como 0) → **400**; sin cuerpo → 400 (ya lo manejaba `ManejadorErrores`). La validación va **antes** de buscar el bolsín y de tocar el Gestor | 501 significa justamente "funcionalidad no implementada"; validar primero garantiza "sin cambiar nada". |
| 11b | (3a) 3 reglas CSS nuevas en `index.css` (`.opciones-recepcion`) con los colores y medidas existentes | `index.css` no estaba autorizado; se autorizó para esto. |
| 11b | Panel del bolsín: un grupo de **radios** con las 4 opciones (textos de la ERS, con tildes) y un botón **"Continuar"** deshabilitado hasta elegir; la opción elegida se limpia al cambiar de bolsín. Opción 1 → `ConfirmarRecepcionModal` (sin cambios); 2/3/4 → aviso "Opción no implementada" (el `Alert` informativo que ya existía) | Paso 7 (elegir) → paso 9 (confirmar) de la ERS; un radio es la forma estándar de "elegir una de varias". |
| 11b | `Badge.tsx`: celeste si contiene "Rechaz"; verde si empieza con "Recibido" o es "RecibidaYAceptada"; amarillo si es "Enviado" o "EnBolsinEnviado"; gris el resto. Los nombres se muestran tal cual | A3, con los nombres de la DME. "Rechaz" va primero para que RecibidaYRechazada no quede verde. |
| 11b | Para verificar el frontend se instalaron sus dependencias con `npm ci` (desde `package-lock.json`); `node_modules/` y `dist/` ya están en `.gitignore` | No había `node_modules`; sin ellas no se puede correr `tsc`. |
| 12 | Datos de prueba = lista aprobada: 13 estados (P6), 4 tipos (EXPEDIENTE "Expediente médico", INFORME "Informe", CERTIFICADO "Certificado", OTRO "Otro"), CM 1 Cordoba / 2 Buenos Aires (datos del SQLite viejo), jero/1234 Jeronimo Abdala (Cordoba), mlopez/1234 María López mlopez@mail.com (Buenos Aires), bolsines 1001 (PREC-001, remitos 500/501, docs 1/2) y 1002 (PREC-002, remito 502, doc 3), todos BA → Cordoba, tipo EXPEDIENTE, área destino vacía | Los del SQLite viejo + D12/P8/P9. "Cordoba" sin tilde (igual que antes; `esTuCM` compara por nombre). |
| 12 | Cambios de estado iniciales (Bolsín Enviado / Documentación EnBolsinEnviado) con fecha/hora de inicio **`now()` menos 2 días**, a cargo de María López | Demo realista (pedido del usuario): el envío ocurrió antes de la recepción. |
| 12 | Peso: 1001 → **2,5 kg**, 1002 → **1,2 kg** | Demo realista (pedido del usuario). |
| 12 | (ajuste) `fecha_generacion` de los bolsines, `fecha` de los remitos y `fecha_pase` de las documentaciones = **día del envío** (`now()` − 2 días). Constructores nuevos `Bolsin(numero, cmOrigen, cmDestino, estado, fechaGeneracion)` y `Documentacion(numero, asunto, descripcion, estado, tipoDocumento, fechaPase)`; los existentes **no se tocaron** (siguen con `LocalDate.now()`). `Remito` ya recibía la fecha | Coherencia de la demo: nada puede ser posterior a su envío. |
| 14 | **README.md**: requisitos (JDK 21+, Maven 3.9+, Node 20+), cómo levantar backend (`backend/`, `mvn clean spring-boot:run`) y frontend (`frontend/`, `npm ci` + `npm run dev`), datos de prueba, recorrido del CU 28 (flujo principal opción 1, A6, opciones 2-4), reinicio de datos, `mvn test` y tabla de la API (con el cuerpo `{"opcion":1}` y el 501). **Sin** consola H2, SQL, SQLite ni `bolsines.db` | Pedido del usuario. |
| 14 | **ARQUITECTURA.md**: capas (con RepositoryImpl como "capa de repositorio que separa el dominio de la tecnología de persistencia" y Datasources Spring Data), persistencia (Spring Data JPA + Hibernate + H2 archivo, `ddl-auto`, open-in-view, cargador, sesión, tests en memoria), 11 tablas, relaciones y cascadas, entidades actualizadas, estados, estructura. El Iterator figura **solo en "Pendiente"** (no implementado) | Pedido del usuario; sin patrones de Gamma. |
| 14 | **CASO_DE_USO_RECEPCION.md**: **reescrito** (no borrado) para el CU 28 web: actor, pre/postcondición, tabla paso → pantalla → API → clase y método, árbol del paso 11, flujos alternativos y errores (A6, opciones 2-4, 400, 409, 404), estados antes/después, fuera de alcance | Sirve para la defensa: qué clase/método resuelve cada paso. |
| 14 | (después de la revisión) CASO_DE_USO_RECEPCION.md con los **12 pasos de la ERS** que pasó el usuario y los flujos **A1–A6**; por cada paso, componente de pantalla, endpoint y clase/método. Marcados como **no implementados**: el filtro del paso 4 (y A1/A2), las opciones 2-4 (A3/A4/A5) y el CU 29 del paso 12 | Pedido del usuario, con la numeración real de la ERS. |
| 14 | Comentarios del código alineados con la ERS: elegir la opción = **paso 8** (`tomarOpcionDeRecepcion`, `Requests.RecibirBolsin`, `RecepcionPage`, `useRecepcion`), confirmar = **paso 10** (`RecepcionRestController`, `RecepcionBolsinController`, `tomarConfirmacion`); tests renombrados `paso3y4_...` y `paso5y6_remitosYDocumentacionDelBolsinSeleccionado` | La numeración anterior (paso 7 para elegir, "paso 10"/"paso 5" inventados) no coincidía con la ERS. |
| 14 | (2a) En los comentarios del código se reemplazaron **todas** las referencias a este archivo (P1, P4, P6, P7, P8, P9, P10, D5, D12, D15, D16, "decisión 2a/3a del paso 8", "(paso 12)", "DECISIONES_ORM.md 5.2") por el motivo en palabras (25 reemplazos en 21 archivos) | Este archivo no se commitea: en el repo las referencias quedarían apuntando a algo que no existe. |
| 14 | La captura `docs/capturas/recepcion.png` la reemplazó el usuario; la imagen queda en el README | Respuesta del usuario. |
| 14 | Se corrigieron comentarios con números de paso inventados: `RecepcionRestController` ("confirmada (paso 10)" → "(paso 9)"), `AppConfig` (referencia a pasos de la migración) y el test `paso5_detalle...` → `seleccion_detalleDelBolsinSeleccionado`; el comentario "estado ENVIADO" → "Enviado" | Coherencia con la ERS y con los nombres de estado. `mvn test` sigue 9/9. |
| 15 | La verificación final se hizo con **otra instancia** (mismo código y configuración) en el puerto **8099** y con una base H2 en archivo **aparte** (carpeta temporal, vacía), sin `mvn clean` | El usuario tenía su backend (8080, usando `backend/db`) y su frontend (5173) levantados: no se cortaron sus procesos, no se tocaron sus datos y no se borró `target/` mientras su backend lo usaba. La base de verificación se borró al terminar. |
| 15 | Se reemplazaron 5 comentarios que mencionaban "el SQLite viejo" (`BolsinJpaDatasource`, `BolsinRepositoryImpl`, `UsuarioRepositoryImpl`) por el motivo en palabras | Después del commit ese código ya no existe. |
| 13 | Configuración del test en **`src/test/resources/application-test.properties`** + `@ActiveProfiles("test")` (no un `application.properties` de test) | Un `application.properties` en `src/test/resources` **reemplaza** al de `main` (mismo nombre en el classpath) y se perderían `bolsines.usuario-logueado` y el CORS; el perfil solo pisa la base (`jdbc:h2:mem:bolsines-test`, `create-drop`), `show-sql` y la consola. |
| 13 | **Un test de punta a punta** con MockMvc (API → Controller → Gestor → repositorios → H2) y verificación de la base con `JdbcTemplate` (SQL) | Prueba el CU como lo usa el frontend y lo que quedó guardado de verdad (sin cargar entidades LAZY fuera de un request). |
| 13 | Métodos **ordenados** (`@TestMethodOrder` + `@Order`) que comparten la base: estado inicial → errores de opción → recibir 1001 → historial → pendientes | El CU es una secuencia; recrear el contexto por test (`@DirtiesContext`) costaría ~10 s cada uno. |
| 13 | Los datos del test son los del `CargadorDatosDePrueba` (la base en memoria arranca vacía) | Se prueba también el cargador (incluidas las fechas = día del envío). |
| 12 | `CargadorDatosDePrueba` (`@Order(1)`) e `InicioDeSesion` (`@Order(2)`) en `main.Config`, registrados con **`@Component`** (component scan) | Son "tareas de arranque" (`ApplicationRunner`), no parte de la cadena de capas; `@Order` en la clase es lo que Spring Boot usa para ordenar los runners. |
| 12 | El cargador solo corre si `estadoRepository.obtenerTodos()` está vacío (D15) y guarda en orden: estados → tipos → CM → empleados (con usuario por cascada) → bolsines (con todo lo demás por cascada) | Primero lo que otros referencian (las FK apuntan a filas que ya existen). |
| 12 | `InicioDeSesion`: si el usuario configurado no existe, deja un `WARN` en el log y la app arranca igual (`/sesion` da 404) | No frenar el arranque por un dato de configuración. |
| 12 | Después de probar se **borró `backend/db/`** (creada por la prueba) para que la primera prueba del usuario arranque con 1001 y 1002 pendientes | La base se recrea sola con los datos de prueba al levantar. |

---

## 3. Hallazgos del análisis inicial

- No existía `CambioEstadoRemito` (→ D13).
- Nunca se creaba un `CambioEstadoDocumentacion` (→ D11).
- Había dos `cargarDatosDePrueba()` (Sqlite y Memory) con descripciones distintas; la activa era la de Sqlite.
- `data/` estaba en la raíz del repo, no en `backend/`.
- El datasource SQLite **ignoraba `fecha_generacion` y `fecha_pase` al leer** (las pisaba con `LocalDate.now()`); con JPA se devuelven las fechas guardadas.
- `Documento.asunto` y `DetalleRemito.documento` eran `final` (JPA necesita poder asignarlos).
- El gestor se creaba con `new` dentro del método `@Bean` del controller (no era bean): un `@Transactional` en él no habría tenido efecto; las transacciones las da `save()` de Spring Data. Desde el paso 8 el Gestor **es un bean** (`@Bean` en `AppConfig`).
- No había `src/test` (→ D16).
- El frontend colorea los badges con `startsWith('RECIBID')` / `startsWith('ENVIAD')` (→ A3).
- (paso 9) El Gestor tenía un atributo `ArrayList<Bolsin> bolsinesSeleccionados` que **nunca se usaba**. Como está en el diagrama de clases como atributo del Gestor, desde el paso 10 `buscarBolsinesEnviados()` **guarda ahí la lista que arma y la devuelve**; el tipo pasó a `List<Bolsin>`.
- (paso 12) Con Java 26 aparece al arrancar un `WARNING: A restricted method in java.lang.System has been called ... org.apache.tomcat.jni.Library`. Es un aviso de Tomcat (carga de librería nativa opcional), no un error; **no hubo errores de ByteBuddy** ni de proxies LAZY. No se cambió ninguna versión.
- (paso 14) Comentarios del código que referenciaban este archivo (P1, P4, D16, "decisión 3a del paso 8", "(paso 12)"…): **resueltos**, reemplazados por el motivo en palabras.
- (paso 14) La captura `docs/capturas/recepcion.png` era de la versión anterior: **resuelto**, la reemplazó el usuario.
- (paso 13) En `mvn test` aparece `WARNING: A Java agent has been loaded dynamically (... byte-buddy-agent-1.14.19.jar)`: lo carga **Mockito** (viene con `spring-boot-starter-test`) y en Java 21+ es solo un aviso (en una versión futura de Java habrá que habilitarlo con `-javaagent`). **No es un error** y los tests pasan; no se cambió ninguna versión.
- (paso 12) En el POST de recibir, además de los `INSERT` de los cambios de estado nuevos, Hibernate hace un `UPDATE ... set bolsin_numero=? / documentacion_numero=?` por cada uno: es propio de las `@OneToMany` **unidireccionales** (el dueño de la relación es el padre, que "actualiza" la FK del hijo). No es un error; se evitaría con una relación bidireccional (`mappedBy`), que no está en el diagrama (D3).
- (paso 12) `fecha_generacion` del bolsín, `fecha` del remito y `fecha_pase` de la documentación quedaban en **hoy**, mientras que el envío era de hace 2 días. **Corregido** después del paso 12: los datos de prueba usan el día del envío (constructores nuevos que reciben la fecha).
- (paso 9) Si no hay Sesion actual, `buscarBolsinesEnviados()` falla con `NullPointerException` en `getCM()` (igual que antes de la migración). No pasa en la app porque `InicioDeSesion` (paso 12) siempre registra la sesión; `/api/recepcion/sesion` ya devuelve 404 en ese caso.

---

## 4. Bitácora de pasos

### Paso 1 — Dependencias y configuración

**Qué se cambió**
- `backend/pom.xml`: `spring-boot-starter-data-jpa` y `com.h2database:h2` (`scope runtime`). Versiones resueltas por el parent 3.3.5: Hibernate 6.5.3, Spring Data JPA 3.3.5, H2 2.2.224, ByteBuddy 1.14.19.
- `application.properties`: datasource H2, `ddl-auto`, `open-in-view`, consola H2, `show-sql`.
- `.gitignore`: `backend/db/`.

**Conceptos**
- **JPA** (Jakarta Persistence API): especificación estándar de Java para mapear objetos a tablas. Define las anotaciones (`@Entity`, `@Id`, …) y la interfaz `EntityManager`. Es solo un contrato.
- **Hibernate**: el **ORM** (Object-Relational Mapper) que implementa JPA. Traduce operaciones sobre objetos a SQL (`INSERT`, `SELECT`, …) y filas a objetos.
- **Spring Data JPA**: capa encima de JPA. A partir de una interfaz `extends JpaRepository<Entidad, TipoId>` genera la implementación (`save`, `findAll`, `findById`, …) en tiempo de ejecución.
- **Starter**: dependencia "paquete" de Spring Boot que trae todo lo necesario y lo autoconfigura.
- **`scope runtime`** (H2): el código nunca importa clases de H2; solo hace falta al ejecutar (el driver JDBC se carga por la URL).
- **`spring.datasource.url=jdbc:h2:file:./db/bolsines`**: URL JDBC. `file:` = modo archivo (persistente); `mem:` sería en memoria (se pierde al apagar). La ruta es relativa al **directorio de trabajo**: desde `backend/` crea `backend/db/bolsines.mv.db`.
- **`username=sa`, `password=`**: credenciales por defecto de H2; crea el usuario al conectarse la primera vez.
- **`ddl-auto`**: qué hace Hibernate con el esquema al arrancar. `create` (borra y crea: se pierden los datos), `create-drop` (además borra al apagar), `update` (crea lo que falta y agrega columnas, **no borra datos ni columnas viejas, y no modifica restricciones de columnas que ya existen**), `validate` (solo verifica), `none`. Elegimos `update`. En producción se usa Flyway/Liquibase (migraciones versionadas).
- **`open-in-view=true`**: mantiene abierto el `EntityManager` (la "sesión" de JPA, su caché de objetos) **durante todo el request HTTP**, aunque no haya transacción. Permite que el DTO o el gestor recorran colecciones LAZY después de que el repositorio devolvió el objeto. Spring Boot lo trae activo pero avisa con un warning si no se pone explícito.
- **LAZY vs EAGER** (`fetch`): EAGER = al traer la entidad se trae también la relación (en la misma consulta con JOIN o en otra inmediata). LAZY = la relación se trae recién cuando se la usa (Hibernate pone en su lugar un objeto o una colección "vacíos" que saben cargarse solos). Defaults de JPA: `@ManyToOne`/`@OneToOne` = EAGER; `@OneToMany`/`@ManyToMany` = LAZY. Si se accede a algo LAZY con el `EntityManager` cerrado → `LazyInitializationException` (por eso open-in-view).
- **`show-sql` / `format_sql`**: imprimen e indentan el SQL que ejecuta Hibernate. `format_sql` va con el prefijo `spring.jpa.properties.` (todo lo que sigue se le pasa tal cual a Hibernate).
- **Consola H2** (`/h2-console`): página web para ejecutar SQL contra la base (JDBC URL `jdbc:h2:file:./db/bolsines`, usuario `sa`).

### Paso 2 — Eliminar la persistencia vieja

**Qué se cambió**
- Eliminados: `Infrastructure/Database/DatabaseManager.java`, `Infrastructure/Datasources/{Sqlite,Memory}{Bolsin,Usuario}Datasource.java`, `data/bolsines.dat`, `data/usuarios.dat`, `backend/bolsines.db`, dependencia `sqlite-jdbc`.
- `BolsinRepositoryImpl` / `UsuarioRepositoryImpl`: esqueletos que tiran `UnsupportedOperationException("Pendiente: implementacion JPA (paso 8)")`.
- `AppConfig`: sin `DatabaseManager.inicializar()` ni `cargarDatosDePrueba()`.

**Por qué**: P12 — así los pasos de mapeo pueden cambiar el dominio sin parchear código que se iba a borrar. La app compila pero no funciona hasta el paso 12.

**Conceptos**
- Con JDBC directo nosotros escribíamos el SQL, abríamos la conexión y armábamos cada objeto a mano desde el `ResultSet` (`mapearBolsin`, `mapearRemito`…). Con un ORM eso lo hace Hibernate a partir de las anotaciones.

### Paso 3 — Renombrar Documento → Documentacion

**Qué se cambió**
- `Documento.java` → `Documentacion.java`; `DocumentoDto.java` → `DocumentacionDto.java`; referencias en `DetalleRemito`, `Remito`, `GestorRecepcionBolsin`, `RemitoDto`.
- Atributo `DetalleRemito.documento` → `documentacion`.

**Conceptos**
- **El JSON no cambia**: Jackson usa el **nombre del componente del record** como clave. `RemitoDto(..., List<DocumentacionDto> documentos)` sigue generando `"documentos": [...]`; cambiar el *tipo* no cambia la clave.
- La tabla `documentacion` se define con `@Table` al mapear la entidad (paso 6).

### Paso 4 — Estado y TipoDocumento como entidades

**Qué se cambió**
- `Estado`: `@Entity`, `@Table(name = "estado", uniqueConstraints = uk_estado_ambito_nombre (ambito, nombre))`, `@Id @GeneratedValue(IDENTITY) Long id`, `@Column` en cada atributo, constructor `protected Estado()`.
- `TipoDocumento`: igual, tabla `tipo_documento`, UNIQUE `uk_tipo_documento_nombre (nombre)`.

**SQL generado**
```sql
create table estado (id bigint generated by default as identity, ambito varchar(255) not null,
                     descripcion varchar(255), nombre varchar(255) not null, primary key (id))
alter table estado add constraint uk_estado_ambito_nombre unique (ambito, nombre)
create table tipo_documento (id bigint generated by default as identity, descripcion varchar(255),
                             nombre varchar(255) not null, primary key (id))
alter table tipo_documento add constraint uk_tipo_documento_nombre unique (nombre)
```

**Conceptos**
- **`@Entity`**: la clase se persiste en una tabla; cada objeto = una fila. Sin `@Entity` Hibernate la ignora.
- **`@Table(name = ...)`**: nombre de la tabla (opcional; por defecto el de la clase). Explícito evita que un renombre de clase cambie la tabla.
- **`@UniqueConstraint`**: restricción `UNIQUE` sobre una o varias columnas, declarada en `@Table` (permite darle nombre). `(ambito, nombre)` juntos: "Enviado" puede existir en BOL y en REM, pero no dos veces en BOL.
- **`@Id`**: clave primaria; Hibernate la usa para identificar el objeto y saber si la fila existe.
- **`@GeneratedValue(strategy = IDENTITY)`**: la clave la genera la base (columna autoincremental). Nunca se asigna a mano; después del `INSERT` Hibernate la carga en el objeto. Otras estrategias: `SEQUENCE` (secuencia de la base), `TABLE`, `AUTO`.
- **Clave generada vs. asignada**: generada = la pone la base (`@GeneratedValue`), útil cuando el objeto no tiene identificador natural (Estado). Asignada = la pone nuestro código (sin `@GeneratedValue`), cuando el dominio ya tiene un identificador con significado (número de bolsín, código de CM).
- **`@Column(name, nullable, unique, length)`**: mapea un atributo a una columna. `nullable = false` → `NOT NULL`. Sin `@Column` JPA mapea igual con el nombre del atributo. `String` → `varchar(255)` por defecto.
- **Constructor `protected` sin argumentos**: JPA lo exige. Al leer una fila, Hibernate crea el objeto vacío por reflexión y después le carga los atributos. `protected` para que el resto del código no cree objetos vacíos por error.

### Paso 5 — ComisionMedica, Usuario, Empleado y paso 2 del CU

**Qué se cambió**
- `ComisionMedica`: `@Entity`, tabla `comision_medica`, `@Id codigo` **asignado**.
- `Usuario`: `@Entity`, `@Id` = nombre de usuario (columna `nombre_usuario`); **ya no conoce a su Empleado** (se quitó `empleado` y `getEmpleado()`); constructor `Usuario(nombre, password)`.
- `Empleado`: `@Id @GeneratedValue Long id`; `@ManyToOne(optional = false) @JoinColumn(name = "comision_medica_codigo", nullable = false) ComisionMedica`; `@OneToOne(optional = false, cascade = {PERSIST, MERGE}) @JoinColumn(name = "usuario_nombre", nullable = false, unique = true) Usuario`; métodos `esTuUsuario(Usuario)` y `getCM(): String`.
- `Bolsin.esTuCM(String nombreCM)`.
- Nuevo `Domain/Repositories/EmpleadoRepository` (`obtenerTodos`, `guardar`) + esqueleto `EmpleadoRepositoryImpl`; se quitó `crearUsuario` de `UsuarioRepository`.
- Gestor: `Sesion sesionActual` (reemplaza a `nombreUsuarioLogueado`) + `setSesionActual()`, `Empleado empleadoLogueado`, `buscarUsuarioLogueado()` con el recorrido `*esTuUsuario`, `getCM()` como String, `crearUsuario` guarda el Empleado, `obtenerTodosEmpleados()`.
- `UsuarioDto.de(Empleado)`, `RecepcionRestController.sesion()`, `UsuarioRestController` y `RecepcionBolsinController` trabajan con Empleado.
- Puente **temporal** en `buscarBolsinesEnviados()` hasta el paso 9.

**SQL generado**
```sql
create table comision_medica (codigo integer not null, direccion varchar(255), email varchar(255),
                              nombre varchar(255) not null, telefono varchar(255), primary key (codigo))
create table usuario  (nombre_usuario varchar(255) not null, fecha_alta date,
                       password varchar(255) not null, primary key (nombre_usuario))
create table empleado (id bigint generated by default as identity, apellido varchar(255) not null,
                       mail varchar(255), nombre varchar(255) not null,
                       comision_medica_codigo integer not null, usuario_nombre varchar(255) not null,
                       primary key (id))
alter table empleado add constraint UK... unique (usuario_nombre)
alter table empleado add constraint FK... foreign key (comision_medica_codigo) references comision_medica
alter table empleado add constraint FK... foreign key (usuario_nombre) references usuario
```

**Conceptos**
- **`@ManyToOne`**: "muchos empleados → una CM". En la tabla del lado "muchos" (`empleado`) se guarda **solo la clave** de la CM, no una copia de sus datos.
- **`@JoinColumn(name = ...)`**: nombre de la columna de clave foránea.
- **Clave foránea (FK)**: columna que referencia la PK de otra tabla (`FOREIGN KEY ... REFERENCES comision_medica`). La base impide guardar un código de CM inexistente (integridad referencial).
- **`@OneToOne`**: "un empleado ↔ un usuario". Se mapea igual que `@ManyToOne` (FK en `empleado`) más `unique = true` en la columna, para que dos empleados no compartan usuario.
- **Relación unidireccional**: solo un lado tiene el atributo (Empleado conoce a Usuario; Usuario no conoce a Empleado). La FK está en la tabla del que "conoce". Bidireccional sería agregar `@OneToOne(mappedBy = "usuario")` en Usuario (no lo hacemos: respeta el diagrama).
- **`optional = false` vs `nullable = false`**: `optional = false` es a nivel JPA/Hibernate (valida antes de guardar y permite usar `INNER JOIN` en vez de `LEFT JOIN` al traer la relación). `nullable = false` es a nivel base de datos (genera `NOT NULL`). Se usan juntos para expresar multiplicidad 1. Ojo: con `ddl-auto=update`, si la tabla ya existía, Hibernate **no** agrega el `NOT NULL` a una columna existente.
- **`fetch` en `@ManyToOne`/`@OneToOne`**: EAGER por defecto; al traer un Empleado vienen su CM y su Usuario en la misma consulta (JOIN). Para un solo objeto relacionado es razonable.
- **Cascada**: una operación sobre la entidad "dueña" se repite sobre la relacionada.
  - **`PERSIST`** (guardar algo nuevo): `empleadoRepository.guardar(new Empleado(..., new Usuario(...)))` → `persist(empleado)` → por cascada `persist(usuario)`; Hibernate inserta **primero el usuario y después el empleado** (por la FK). Sin PERSIST: `TransientPropertyValueException: object references an unsaved transient instance`.
  - **`MERGE`** (guardar algo existente "detached", cargado en otro momento): `save()` de un empleado con id hace `merge(empleado)` y por cascada `merge(usuario)`, guardando sus cambios. Sin MERGE, los cambios del usuario se perderían en silencio.
  - **Por qué no `ALL`**: `ALL` incluye `REMOVE` → borrar un empleado borraría su usuario; no se pidió.
  - **Sin cascada a ComisionMedica**: es un dato maestro (se administra en `/api/comisiones`); con MERGE, guardar un empleado podría pisar los datos de la CM.
- **Clave asignada y `save()` de Spring Data**: si el id ya viene cargado (CM con código), Spring Data no sabe si es nueva → hace `merge`: primero `SELECT` y después `INSERT` o `UPDATE` (se ve en el paso 8).
- **Por qué `esTuUsuario` compara por nombre de usuario y no con `==`**: el Usuario de la Sesion se carga al iniciar la app y el del Empleado en cada request (otra sesión de JPA) → son **dos objetos Java distintos que representan la misma fila**. `==` compara referencias (daría `false`); hay que comparar la identidad de negocio (la clave).
- **Por qué `buscarUsuarioLogueado()` busca en cada llamada**: cada request HTTP es independiente; así el empleado siempre pertenece a la sesión JPA del request actual (importa en el paso 11, al asignarlo a los cambios de estado).

### Paso 6 — Documentacion, CambioEstadoDocumentacion, DetalleRemito y Remito

**Qué se cambió** (solo mapeo; el comportamiento del paso 11 va en el paso 10)
- `Documentacion`: `@Entity`, tabla `documentacion`, `@Id numero` asignado; `asunto` deja de ser `final`; `@ManyToOne` a `Estado` (`estado_id`) y a `TipoDocumento` (`tipo_documento_id`), ambos multiplicidad 1 y sin cascada; `@OneToMany(cascade = ALL) @JoinColumn(name = "documentacion_numero", nullable = false) @OrderBy("fechaHoraInicio") List<CambioEstadoDocumentacion> cambiosEstado`.
- `CambioEstadoDocumentacion`: `@Entity`, tabla `cambio_estado_documentacion`, `Long id` generado, `fecha_hora_inicio` NOT NULL, `fecha_hora_fin` nullable (null = es el actual), `@ManyToOne` a `Estado` y a `Empleado` (multiplicidad 1, sin cascada).
- `DetalleRemito`: `@Entity`, tabla `detalle_remito`, `Long id` generado; `documentacion` deja de ser `final`; `@ManyToOne(optional = false, cascade = {PERSIST, MERGE}) @JoinColumn(name = "documentacion_numero", nullable = false)`.
- `Remito`: `@Entity`, tabla `remito`, `@Id numero` asignado; `@ManyToOne` a `Estado`, `cmOrigen`, `cmDestino` (multiplicidad 1); `@OneToMany(cascade = ALL) @JoinColumn(name = "remito_numero", nullable = false) @OrderBy("id") List<DetalleRemito> detalles`; `buscarDocumentacion()` y `getDetalles()` devuelven `List`.
- En las tres clases con colecciones: `ArrayList` → `List`, inicializada en la declaración (`= new ArrayList<>()`), y constructor `protected` vacío.

**SQL generado**
```sql
create table documentacion (numero integer not null, asunto varchar(255) not null, descripcion varchar(255),
    fecha_pase date, estado_id bigint not null, tipo_documento_id bigint not null, primary key (numero))
create table cambio_estado_documentacion (id bigint generated by default as identity,
    fecha_hora_fin timestamp(6), fecha_hora_inicio timestamp(6) not null, empleado_id bigint not null,
    estado_id bigint not null, documentacion_numero integer not null, primary key (id))
create table detalle_remito (id bigint generated by default as identity, area_cm_destino varchar(255),
    documentacion_numero integer not null, remito_numero integer not null, primary key (id))
create table remito (numero integer not null, fecha date, cm_destino_codigo integer not null,
    cm_origen_codigo integer not null, estado_id bigint not null, primary key (numero))
-- FKs: cambio_estado_documentacion -> empleado, estado, documentacion
--      detalle_remito -> documentacion, remito
--      documentacion -> estado, tipo_documento
--      remito -> comision_medica (x2), estado
```

**Conceptos**
- **`@OneToMany`**: "un remito tiene muchos detalles". Se pone sobre una colección (`List<DetalleRemito>`). En la base **no hay ninguna columna en `remito`**: la relación se guarda como una FK en la tabla del lado "muchos" (`detalle_remito.remito_numero`).
- **`@OneToMany` unidireccional + `@JoinColumn`**: el atributo está en el padre (Remito), pero la FK física vive en la tabla del hijo. El `@JoinColumn` le dice a Hibernate qué columna del hijo usar. Sin `@JoinColumn`, JPA crearía una **tabla intermedia** (`remito_detalles`), como si fuera muchos a muchos: por eso lo ponemos.
- **`@JoinColumn(nullable = false)` en una colección**: (1) el hijo no puede existir sin su padre (`NOT NULL`); (2) Hibernate inserta el hijo **ya con la FK**. Si la FK admitiera NULL, en las relaciones unidireccionales Hibernate hace `INSERT` del hijo con FK en NULL y después un `UPDATE` para asignarla (el hijo "no sabe" quién es su padre).
- **Cascada `ALL`** = `PERSIST`, `MERGE`, `REMOVE`, `REFRESH`, `DETACH`. Se usa en **composiciones** e historiales: las partes viven y mueren con el todo. Guardar un remito nuevo inserta sus detalles; borrar un remito borraría sus detalles.
- **`orphanRemoval`** (no lo usamos, D4): si se quita un hijo de la colección (`detalles.remove(x)`), Hibernate lo borra de la base. Sin él, el hijo queda en la base (aunque, con FK NOT NULL, sacar un hijo de la lista daría error al intentar ponerle NULL). En esta tarea nunca se quitan elementos.
- **Asociación vs. composición en DetalleRemito → Documentacion**: la Documentacion existe por sí misma (tiene su ciclo de vida en la DME) y puede aparecer en varios detalles → `@ManyToOne`, **sin REMOVE**.
  - **`PERSIST`**: al agregar un remito desde la pantalla auxiliar se crean juntos `new Remito` → `new DetalleRemito(new Documentacion(...))`; al persistir, la cascada inserta también la Documentacion nueva. Sin PERSIST → `TransientPropertyValueException`.
  - **`MERGE`**: el `save()` de un Bolsin existente hace `merge`, que baja por la cascada Bolsin → remitos (ALL) → detalles (ALL) → **documentacion (MERGE)** → cambiosEstado (ALL). Así, en el paso 11, el nuevo estado de la Documentacion y su nuevo CambioEstadoDocumentacion se guardan con un solo `save(bolsin)`. Sin MERGE en este eslabón, la cadena se corta y si el objeto estuviera "detached" los cambios de la documentación no se guardarían.
- **Por qué no hay cascada hacia Estado, TipoDocumento, ComisionMedica ni Empleado**: son catálogos o datos maestros; un cambio de estado *referencia* un Estado existente, no lo crea ni lo modifica.
- **Colección LAZY y `PersistentBag`**: al cargar un Remito, Hibernate reemplaza nuestra `ArrayList` por una colección propia (`PersistentBag`) que todavía no tiene datos; el primer `getDetalles()` dispara el `SELECT` (por eso necesitamos la sesión abierta: open-in-view). Por eso el atributo se declara como **interfaz `List`** (Hibernate no podría poner su colección en un atributo `ArrayList`).
- **Inicializar la colección en la declaración** (`= new ArrayList<>()`): así está lista tanto si el objeto lo crea nuestro constructor como si lo crea Hibernate con el constructor vacío.
- **Bag**: en Hibernate, una `List` sin columna de índice (`@OrderColumn`) se llama *bag*: colección sin orden propio que admite repetidos.
- **`MultipleBagFetchException`** (por qué no ponemos todo EAGER, D5): si se piden **dos bags EAGER en la misma consulta** (por ejemplo `Bolsin.remitos` y `Bolsin.cambiosEstado`, o `remitos` y `remitos.detalles`), el `JOIN` devuelve el **producto cartesiano**: un bolsín con 2 remitos y 2 cambios da 4 filas `(R500,C1) (R500,C2) (R501,C1) (R501,C2)`. Para reconstruir las listas, Hibernate tendría que adivinar si R500 aparece dos veces por el producto o porque la lista realmente lo tiene repetido (un bag admite repetidos), así que se niega: `cannot simultaneously fetch multiple bags`. Soluciones: dejar las colecciones **LAZY** (nuestra elección: cada colección se trae en su propio `SELECT` cuando se usa), usar `Set`, usar `@OrderColumn`, o traerlas en consultas separadas.
- **N+1 consultas** (consecuencia de LAZY): listar N bolsines y recorrer sus remitos hace 1 consulta para los bolsines + N para los remitos. Con pocos datos no importa; se optimiza con `JOIN FETCH` de **una** colección o con `@BatchSize`.
- **`@OrderBy("fechaHoraInicio")`**: agrega `ORDER BY` al `SELECT` de la colección. Recibe el **nombre del atributo Java** (JPQL), no el de la columna. No convierte la lista en indexada (sigue siendo un bag).
- **Por qué se sacó `final`** de `asunto` y `documentacion`: Hibernate crea el objeto con el constructor vacío y **después** asigna los atributos; un atributo `final` solo puede asignarse en el constructor (la especificación JPA prohíbe campos persistentes `final`).
- **Tipos**: `LocalDate` → `date`, `LocalDateTime` → `timestamp(6)` (microsegundos), `int` → `integer`, `Long` → `bigint`.

### Paso 7 — Bolsin y CambioEstadoBolsin

**Qué se cambió** (solo mapeo; `sosEnviado()` por historial va en el paso 9 y `recibir()`/`crearNuevoCE()` en el paso 10)
- `Bolsin`: `@Entity`, tabla `bolsin`, `@Id numero` asignado; columnas `fecha_generacion`, `nro_precinto`, `peso`; `@ManyToOne` a `Estado` (`estado_id`), `cmOrigen` (`cm_origen_codigo`) y `cmDestino` (`cm_destino_codigo`), los tres multiplicidad 1 y sin cascada.
- `Bolsin.remitos`: `@OneToMany(cascade = ALL) @JoinColumn(name = "bolsin_numero", nullable = false) @OrderBy("numero") List<Remito>` → la FK aparece en la tabla `remito`.
- `Bolsin.cambiosEstado`: `@OneToMany(cascade = ALL) @JoinColumn(name = "bolsin_numero", nullable = false) @OrderBy("fechaHoraInicio") List<CambioEstadoBolsin>`.
- `ArrayList` → `List` (atributos y `getRemitos()`/`getCambiosEstado()`), colecciones inicializadas en la declaración (se sacaron los `new ArrayList<>()` del constructor), constructor `protected Bolsin()`.
- `CambioEstadoBolsin`: `@Entity`, tabla `cambio_estado_bolsin`, `Long id` generado, `fecha_hora_inicio` NOT NULL, `fecha_hora_fin` nullable, `@ManyToOne` a `Estado` y a `Empleado` (multiplicidad 1, sin cascada), constructor `protected`.
- **Ajuste pedido después del paso 7:** se eliminó `cantRemitos` de `Bolsin` (atributo, getter, columna, actualización en constructor y `agregarRemito`); `BolsinDto` lo calcula con `b.getRemitos().size()`. Solo aparecía en esas dos clases (en el backend).
- No hubo que tocar a los que usan `getRemitos()` (el gestor y `BolsinDto` solo la recorren con `for`/`stream`, que funcionan igual con `List`).

**SQL generado**
```sql
create table bolsin (numero integer not null, fecha_generacion date,
    nro_precinto varchar(255), peso float(53), cm_destino_codigo integer not null,
    cm_origen_codigo integer not null, estado_id bigint not null, primary key (numero))
create table cambio_estado_bolsin (id bigint generated by default as identity,
    fecha_hora_fin timestamp(6), fecha_hora_inicio timestamp(6) not null, empleado_id bigint not null,
    estado_id bigint not null, bolsin_numero integer not null, primary key (id))
create table remito (numero integer not null, fecha date, cm_destino_codigo integer not null,
    cm_origen_codigo integer not null, estado_id bigint not null,
    bolsin_numero integer not null,          -- NUEVA: la agrega el @JoinColumn de Bolsin.remitos
    primary key (numero))
-- FKs: bolsin -> comision_medica (x2), estado
--      cambio_estado_bolsin -> empleado, estado, bolsin
--      remito -> bolsin (nueva)
```

**Conceptos**
- **La FK de una relación unidireccional `@OneToMany` aparece en la tabla del hijo aunque la clase hija no cambie**: `Remito.java` no se tocó en este paso, pero la tabla `remito` ganó la columna `bolsin_numero`. La columna la "declara" el padre (`Bolsin`) con su `@JoinColumn`. Esto muestra que **el modelo de objetos y el de tablas no son iguales**: en objetos la flecha va Bolsin → Remito (el bolsín conoce sus remitos); en tablas la referencia va al revés (cada fila de remito apunta a su bolsín), porque una columna solo puede guardar un valor.
- **Dos colecciones con el mismo nombre de `@JoinColumn`** (`bolsin_numero` en `remitos` y en `cambiosEstado`): no hay conflicto porque cada una vive en una tabla distinta (`remito` y `cambio_estado_bolsin`).
- **Tipos**: `double` → `float(53)` (en SQL, `float(53)` es la precisión doble, equivalente a `double precision`). Un primitivo como `double peso` queda **sin** `not null` explícito en el DDL aunque en Java nunca pueda ser null; con `nullable = false` lo agregaríamos (no se pidió).
- **Atributo derivado (`cantRemitos`, eliminado)**: es un dato que se puede **calcular** a partir de otros (acá, `remitos.size()`). Guardarlo como atributo/columna es **redundancia**: hay que acordarse de actualizarlo cada vez que cambia la lista y, si alguien se olvida (o modifica la tabla `remito` a mano), el número guardado deja de coincidir con la realidad. Por eso se calcula cuando se necesita: en el DTO, que es quien arma el JSON. En el diagrama de clases UML, si se quisiera mostrar, se marca con `/` (`/cantRemitos`).
- **El DTO desacopla el JSON del dominio**: el JSON sigue teniendo `"cantRemitos"` aunque la entidad ya no lo tenga, porque el `record BolsinDto` conserva el componente y lo llena con `b.getRemitos().size()`. Ese `size()` dispara la carga LAZY de los remitos (con open-in-view; el DTO igual ya los recorría para armar `remitos`).
- **`ddl-auto=update` no borra columnas**: si la base de archivo hubiera tenido `cant_remitos`, la columna habría quedado ahí, sin uso. No pasa porque `backend/db/` todavía no existe (la app nunca corrió con la base de archivo).
- **Grafo completo de cascadas para el paso 11**: `save(bolsin)` → `merge(bolsin)` → `cambiosEstado` (ALL: inserta el nuevo `CambioEstadoBolsin` y actualiza el `fecha_hora_fin` del anterior) → `remitos` (ALL: actualiza `estado_id` de cada remito) → `detalles` (ALL) → `documentacion` (MERGE: actualiza su `estado_id`) → `cambiosEstado` de la documentación (ALL). **Un solo `save()` guarda todo el paso 11.** Las flechas sin cascada (Estado, ComisionMedica, Empleado) solo copian la clave en la FK.
- **Qué guarda la FK cuando no hay cascada**: al guardar un `CambioEstadoBolsin`, Hibernate solo necesita el **id** del Empleado y del Estado para llenar `empleado_id` y `estado_id`. Por eso esos objetos tienen que existir ya en la base (si fueran nuevos, sin cascada daría `TransientPropertyValueException`): los estados se precargan (paso 12) y el empleado es el logueado.
- **Finales de línea (CRLF vs LF)**: Windows termina cada línea con `\r\n` (CRLF) y Linux con `\n` (LF). Con `core.autocrlf=true`, git guarda LF en el repositorio y entrega CRLF en la copia de trabajo, así que convertir un archivo entre ambos no genera diferencias en `git diff`.

### Paso 8 — Repositorios con Spring Data JPA

**Qué se cambió**
- `Infrastructure/Datasources` (nuevo contenido): 8 interfaces `XxxJpaDatasource extends JpaRepository<Entidad, TipoId>`: `Bolsin` (`Integer`), `Remito` (`Integer`), `Documentacion` (`Integer`), `ComisionMedica` (`Integer`), `Usuario` (`String`), `Empleado` (`Long`), `Estado` (`Long`), `TipoDocumento` (`Long`). Solo `Bolsin`, `Remito` y `Documentacion` tienen un método propio: `@Query("select coalesce(max(x.numero), base) + 1 from X x") int obtenerSiguienteNumero()`.
- `Domain/Repositories`: nuevos `EstadoRepository` y `TipoDocumentoRepository` (`obtenerTodos()`, `guardar()`), sin dependencias de Spring.
- `Infrastructure/Repositories`: `BolsinRepositoryImpl`, `UsuarioRepositoryImpl`, `EmpleadoRepositoryImpl` reales (reciben sus datasources por constructor) y nuevos `EstadoRepositoryImpl`, `TipoDocumentoRepositoryImpl`. `crearBolsin(...)` sigue pendiente (4a) y `obtenerEnviadosHacia` es temporal.
- `AppConfig`: un `@Bean` por repositorio (devuelve la **interfaz del dominio**), uno para `GestorRecepcionBolsin` y uno para `RecepcionBolsinController`.
- Arranque verificado contra H2 en memoria: `Found 8 JPA repository interfaces` y `Started BolsinesApplication`, sin errores.

**Cadena de capas resultante**
```
RestController → RecepcionBolsinController → GestorRecepcionBolsin
   → BolsinRepository (interfaz, Domain) ← BolsinRepositoryImpl (Infrastructure)
      → BolsinJpaDatasource (interfaz Spring Data; la implementación la genera Spring)
         → Hibernate (EntityManager) → JDBC → H2
```

**Conceptos**
- **`JpaRepository<T, ID>`**: interfaz de Spring Data. Con solo declararla (`interface BolsinJpaDatasource extends JpaRepository<Bolsin, Integer>`) Spring crea **en tiempo de ejecución** una clase que la implementa, con `save`, `saveAll`, `findById`, `findAll`, `count`, `delete`… `T` es la entidad y `ID` el tipo de su `@Id`. Nosotros no escribimos ninguna implementación.
- **Cómo la encuentra Spring**: `@SpringBootApplication` está en el paquete `main`, así que Spring Boot busca interfaces de repositorio y clases `@Entity` en `main` y todos sus subpaquetes (por eso el log dice "Found 8 JPA repository interfaces").
- **`findById(id)` devuelve `Optional<T>`**: una "caja" que puede estar vacía. `.orElse(null)` la abre devolviendo `null` si no hay fila (mantenemos el contrato del código viejo).
- **`save(entidad)`**: si la entidad es nueva hace `persist` (`INSERT`), si no `merge`. Para decidir "¿es nueva?" Spring Data mira el `@Id`: si es `null` (o `0` en un primitivo) es nueva. Con **ids generados** (`Long id` null en Empleado) → `persist` directo. Con **claves asignadas** (`numero` del bolsín, `codigo` de la CM) el id nunca es null → siempre `merge`: Hibernate hace un `SELECT` por id y después `INSERT` (si no existía) o `UPDATE` (si existía). Por eso `crearComisionMedica` con un código repetido actualiza, igual que el `INSERT OR REPLACE` del SQLite.
- **`save()` devuelve la entidad**: con `merge` lo que devuelve puede ser **otro objeto** (la copia administrada). En `crearComisionMedica` devolvemos lo que devuelve `save`.
- **Transacciones**: cada método de `JpaRepository` es `@Transactional` por dentro. Un `save(bolsin)` guarda en **una sola transacción** todo lo que llega por las cascadas (paso 11 del CU completo o nada). Por eso no hizo falta poner `@Transactional` en nuestras clases.
- **`@Query` y JPQL**: JPQL (Jakarta Persistence Query Language) se parece a SQL pero trabaja sobre **entidades y atributos** (`from Bolsin b`, `b.numero`), no sobre tablas y columnas; Hibernate lo traduce al SQL de la base. Spring Data **valida las `@Query` al arrancar**: un error de sintaxis o un atributo inexistente hace fallar el inicio (no aparece recién al usarla). `coalesce(max(...), 1000)` devuelve 1000 si la tabla está vacía (`max` de nada es `NULL`).
- **Consultas derivadas** (no las usamos): Spring Data también genera consultas a partir del nombre del método (`findByCmDestino(...)`, `findByEstadoNombre(...)`). No usamos ninguna para filtrar por estado porque el diagrama pone esa responsabilidad en el dominio (`sosEnviado()` y `esTuCM()`).
- **`@Bean` y la inyección de dependencias**: un método `@Bean` dentro de una clase `@Configuration` le dice a Spring "este objeto lo manejás vos". Si el método tiene parámetros (`bolsinRepository(BolsinJpaDatasource ds, ...)`), Spring busca beans de esos tipos y los pasa: eso es **inyección de dependencias por constructor**. Por defecto Spring crea **una sola instancia** de cada bean: hay un solo Gestor para toda la app (como antes).
- **Por qué el `@Bean` devuelve la interfaz del dominio** (`BolsinRepository`) y no `BolsinRepositoryImpl`: el Gestor pide un `BolsinRepository` y no sabe qué implementación recibe (**inversión de dependencias**: el dominio define la interfaz, la infraestructura la implementa). Se podría cambiar H2/JPA por otra cosa sin tocar el Gestor.
- **Por qué el Gestor ahora es bean**: en el paso 12 `InicioDeSesion` lo recibe inyectado para hacer `setSesionActual(sesion)`. Si el Gestor se creara con `new` adentro del controller, nadie más podría llegar a él.
- **`Repository` vs. `Datasource` en nuestra arquitectura**: los `RepositoryImpl` son la **capa de repositorio que separa el dominio de la tecnología de persistencia**. La interfaz del dominio habla el idioma del negocio (`obtenerSiguienteNumeroBolsin`, `agregarRemitoABolsin`) y el *Datasource* el de la tecnología (`findById`, `save`); el `Impl` implementa la primera usando el segundo.

### Paso 9 — Nombres de estado y paso 3 del CU por historial

**Qué se cambió**
- `Estado.esEnviado()`: `"Enviado"` o `"EnBolsinEnviado"` (antes `"ENVIADO"`/`"ENVIADA"`).
- `Estado.esEstadoRecibidoEnCMDestino()`, `esEstadoRecibidoYAceptado()`, `esEstadoRecibidaYAceptada()`: comparan **solo el nombre** (`"RecibidoEnCMDestino"`, `"RecibidoYAceptado"`, `"RecibidaYAceptada"`); antes además miraban el ámbito (P6).
- `Bolsin.sosEnviado()` y `Documentacion.sosEnviado()` **por historial**: recorren `cambiosEstado`, buscan el que `sosActual()` y le preguntan `sosEnviado()`. Antes miraban el atributo `estado`.
- `CambioEstadoDocumentacion.sosEnviado()` (nuevo, igual al de `CambioEstadoBolsin`): le pregunta `esEnviado()` a su Estado.
- `GestorRecepcionBolsin.buscarBolsinesEnviados()`: `getCM()` una vez, `bolsinRepository.obtenerTodos()` y recorrido con `sosEnviado() && esTuCM(cm)`. Se quitó el puente del paso 5.
- `BolsinRepository` / `BolsinRepositoryImpl`: se eliminó `obtenerEnviadosHacia`.
- Sin cambios en la base (solo lógica): el arranque sigue sin errores.

**Secuencia del paso 3 (como el diagrama)**
```
Gestor.buscarBolsinesEnviados()
  cm = getCM()                                   → empleado logueado → ComisionMedica.getNombre()
  para cada Bolsin de bolsinRepository.obtenerTodos():
      bolsin.sosEnviado()
          para cada CambioEstadoBolsin: *sosActual()     (fechaHoraFin == null)
          actual.sosEnviado() → estado.esEnviado()       ("Enviado")
      bolsin.esTuCM(cm)                          → cmDestino.getNombre().equals(cm)
```

**Conceptos**
- **Por qué el estado se consulta por el historial y no por el atributo**: el diagrama hace que cada objeto responda por lo que conoce (*experto en información*): el Bolsin conoce sus cambios de estado, el cambio conoce su Estado y el Estado sabe si "es enviado". Ninguno mira los datos internos de otro.
- **"Cambio de estado actual" = `fechaHoraFin == null`**: el historial es una lista de períodos `[fechaHoraInicio, fechaHoraFin)`; el que todavía no terminó es el vigente. Por eso en el paso 11 hay que **cerrar** el actual (`setFechaHoraFin`) antes de crear el nuevo: si no, habría dos "actuales".
- **Qué SQL dispara este recorrido** (colecciones LAZY): `select ... from bolsin` (1 consulta, con JOIN a estado y comisiones por ser `@ManyToOne` EAGER) y, por cada bolsín, `select ... from cambio_estado_bolsin where bolsin_numero = ? order by fecha_hora_inicio` cuando `sosEnviado()` recorre `cambiosEstado` → el **N+1** del paso 6. Funciona gracias a open-in-view (el Gestor corre dentro del request HTTP). Se ve en el log en el paso 12.
- **Responsabilidad en el dominio vs. en la base**: con una `@Query ... where estado = 'Enviado'` la base filtraría más rápido, pero la regla "qué significa estar enviado" quedaría en un string SQL en la infraestructura. El diagrama la pone en las clases del dominio; con pocos bolsines el costo es despreciable.
- **Los nombres de estado son datos (paso 9)**: `Estado` compara contra los nombres que se precargan en la tabla `estado` (paso 12). Si el texto de la tabla y el del código no coinciden exactamente (mayúsculas incluidas), `esEnviado()` da `false` y el bolsín no aparece. Por eso se usan los mismos nombres de la DME en los dos lugares.

### Paso 10 — Paso 11 del CU: registrar la recepción

**Qué se cambió**
- **Gestor**: recibe `EstadoRepository` por constructor (y su `@Bean` en `AppConfig`). `tomarConfirmacion(true)` dispara, en este orden, `obtenerEstadoRecibidoEnCMDestino()` → `obtenerEstadoRecibidoYAceptado()` → `obtenerEstadoRecibidaYAceptada()` (cada uno recorre `estadoRepository.obtenerTodos()` con `esAmbitoX() && esEstadoY()`) → `getFechaYHoraActual()` → `registrarRecepcionBolsin()`, que llama a `bolsinSeleccionado.recibir(...)` y después `bolsinRepository.guardar(...)`. Se eliminaron `aceptarRemitos()`, `aceptarDocumentacion()` y los tres `new Estado(...)`. `buscarBolsinesEnviados()` guarda en `bolsinesSeleccionados`.
- **Controller**: `registrarRecepcion(n)` = `tomarSeleccionBolsin(n)` + `tomarOpcionDeRecepcion(1)` + `tomarConfirmacion(true)` (antes llamaba directo a `registrarRecepcionBolsin()`). Ajuste posterior: `llamarCU290()` → `llamarCU29()`.
- **Bolsin**: `mostrarRecibido()` → `recibir(estBolsin, estRemito, estDoc, empleado, fechaHora)`; `crearNuevoCEO(estado, empleado)` → `crearNuevoCE(estado, fechaHora, empleado)`.
- **Remito**: `aceptar(estRemito, estDoc, empleado, fechaHora)`.
- **DetalleRemito**: `aceptarDocumentacion(estDoc, empleado, fechaHora)` delega en la Documentacion.
- **Documentacion**: se eliminó el boolean `esRecibidaYAceptada()` (A1); el nombre pasa a la transición `esRecibidaYAceptada(estDoc, fechaHora, empleado)`; nuevo `crearNuevoCE(estado, fechaHora, empleado)`; `darBaja`, `cancelarRemito`, `rechazar`, `marcarNoRecibida`, `devolverAOrigen` reciben el `Estado` por parámetro (P7). (No existía un método `aceptar` en Documentacion: la transición toma el nombre que tenía el boolean.)
- **CambioEstadoBolsin / CambioEstadoDocumentacion**: constructor `(estado, fechaHoraInicio, empleado)`; ya no usan `LocalDateTime.now()`.
- Ninguna entidad crea estados: no queda `new Estado(...)` en `Domain/`.

**Secuencia ↔ código**
```
Diagrama de secuencia                         Código
---------------------------------------------------------------------------------------------
Controller → Gestor: tomarOpcionDeRecepcion() registrarRecepcion(n): tomarSeleccionBolsin(n); tomarOpcionDeRecepcion(1)
Gestor → Pantalla: solicitarConfirmacion()    (frontend: el usuario confirma "Recibir")
Controller → Gestor: tomarConfirmacion()      tomarConfirmacion(true)
Gestor → Gestor: obtenerEstadoRecibidoEnCMDestino()
   *Estado: esAmbitoBolsin(), esEstadoRecibidoEnCMDestino()   for (Estado e : estadoRepository.obtenerTodos()) ...
Gestor → Gestor: obtenerEstadoRecibidoYAceptado()
   *Estado: esAmbitoRemito(), esEstadoRecibidoYAceptado()
Gestor → Gestor: obtenerEstadoRecibidaYAceptada()
   *Estado: esAmbitoDocumentacion(), esEstadoRecibidaYAceptada()
Gestor → Gestor: getFechaYHoraActual()        fechaHoraActual = LocalDateTime.now()
Gestor → Gestor: registrarRecepcionBolsin()
Gestor → Bolsin: recibir(...)                 bolsinSeleccionado.recibir(estB, estR, estD, empleado, fechaHora)
   *Bolsin → Remito: aceptar(...)               for (Remito r : remitos) r.aceptar(estR, estD, empleado, fechaHora)
      Remito → Remito: setEstado()                setEstado(estR)
      *Remito → DetalleRemito: aceptarDocumentacion(...)   for (DetalleRemito d : detalles) ...
         DetalleRemito → Documentacion: esRecibidaYAceptada(...)
            Documentacion: setEstado()              setEstado(estD)
            *CEDoc: sosActual() → setFechaHoraFin() for (...) if (c.sosActual()) c.setFechaHoraFin(fechaHora)
            Documentacion: crearNuevoCE() → new CambioEstadoDocumentacion(estD, fechaHora, empleado)
   Bolsin → Bolsin: setEstado()                 setEstado(estB)
   *Bolsin → CEBolsin: sosActual() → setFechaHoraFin()
   Bolsin → Bolsin: crearNuevoCE() → new CambioEstadoBolsin(estB, fechaHora, empleado)
Gestor → BolsinRepository: guardar(bolsin)    bolsinDatasource.save(bolsin)  (cascadas)
```

**Verificación en memoria** (`jshell`, sin base: bolsín 1001 BA→Córdoba con remitos 500/501 y documentaciones 1/2, cambios iniciales de María López):
```
ANTES  sosEnviado=true esTuCM(Cordoba)=true
DESPUES sosEnviado=false  estado bolsin=RecibidoEnCMDestino
  CE bolsin: Enviado             inicio=2026-11-10T09:00 fin=2026-11-12T15:30 empleado=Maria Lopez
  CE bolsin: RecibidoEnCMDestino inicio=2026-11-12T15:30 fin=null             empleado=Jeronimo Abdala
  Remito 500: RecibidoYAceptado
    Doc 1: RecibidaYAceptada
      CE doc: EnBolsinEnviado    inicio=2026-11-10T09:00 fin=2026-11-12T15:30 empleado=Maria Lopez
      CE doc: RecibidaYAceptada  inicio=2026-11-12T15:30 fin=null             empleado=Jeronimo Abdala
  (Remito 501 / Doc 2: igual)
```

**Conceptos**
- **Por qué los estados se pasan por parámetro** (D10): las entidades no conocen a los repositorios; el Gestor (que sí los conoce) obtiene los Estado **persistidos** y se los entrega. Si la entidad hiciera `new Estado(...)`, sería un objeto nuevo sin id: como no hay cascada hacia Estado, Hibernate daría `TransientPropertyValueException` al guardar (y además duplicaría estados).
- **Por qué una sola fecha/hora para todo**: el Gestor toma `getFechaYHoraActual()` una vez y la usa para cerrar el cambio anterior (`fechaHoraFin`) y abrir el nuevo (`fechaHoraInicio`). Así no quedan "huecos" ni solapamientos entre períodos, y todos los cambios de la misma recepción tienen la misma marca de tiempo. Con `now()` en cada constructor habría microsegundos de diferencia.
- **Orden: primero los remitos, después el bolsín**: lo fija el diagrama. En memoria el resultado es el mismo, y en la base también, porque nada se escribe hasta el `save()` del final: Hibernate junta los cambios y los manda todos en la misma transacción.
- **Qué hace Hibernate con el `save(bolsin)`** (se ve en el paso 12 con `show-sql`): el bolsín está *administrado* (lo cargó `tomarSeleccionBolsin` en este mismo request), así que Hibernate detecta qué cambió comparando con la foto que tomó al cargarlo (**dirty checking**) y genera:
  - `update bolsin set estado_id=?` y `update cambio_estado_bolsin set fecha_hora_fin=?` (el anterior);
  - `insert into cambio_estado_bolsin (...)` (el nuevo, por cascada ALL);
  - `update remito set estado_id=?` por cada remito (cascada ALL);
  - `update documentacion set estado_id=?`, `update cambio_estado_documentacion set fecha_hora_fin=?` e `insert into cambio_estado_documentacion` por cada documentación (cascadas ALL → MERGE → ALL).
  Todo en **una transacción**: si algo falla, no queda nada a medias.
- **Por qué el POST vuelve a hacer `tomarSeleccionBolsin(n)`**: el Gestor es una sola instancia para toda la app, pero cada request HTTP tiene su propia sesión JPA. El `bolsinSeleccionado` del GET anterior pertenece a una sesión que ya se cerró (*detached*); volver a buscarlo en el POST da un objeto administrado por la sesión actual, sobre el que funcionan la carga LAZY y el dirty checking. Por lo mismo, el empleado responsable se busca con `buscarUsuarioLogueado()`.
- **Remito sin historial**: `Remito.aceptar()` solo hace `setEstado()` (no hay `CambioEstadoRemito`, D13).

### Paso 11 — Pantallas auxiliares (P10)

**Qué se cambió**
- `GestorRecepcionBolsin.crearBolsin(...)`: estado Bolsín/Enviado persistido, `getFechaYHoraActual()`, `new Bolsin(...)` + precinto y peso, `crearNuevoCE(estadoEnviado, fechaHora, buscarUsuarioLogueado())` y `bolsinRepository.guardar(bolsin)`.
- `GestorRecepcionBolsin.crearRemitoConDocumentacion(...)`: estados Remito/Enviado y Documentación/EnBolsinEnviado persistidos, tipo desde el catálogo, `doc.crearNuevoCE(estadoDoc, fechaHora, buscarUsuarioLogueado())`, y `agregarRemitoABolsin(...)` como antes. Se quitaron los `new Estado(...)` y el `new TipoDocumento(...)`.
- Gestor: nuevo `TipoDocumentoRepository` por constructor; métodos privados `obtenerEstadoBolsinEnviado()`, `obtenerEstadoRemitoEnviado()`, `obtenerEstadoDocumentacionEnBolsinEnviado()`, `buscarTipoDocumento(nombre)`.
- `BolsinRepository` / `BolsinRepositoryImpl`: se eliminó `crearBolsin(...)`.
- `AppConfig`: el `@Bean` del Gestor recibe también `TipoDocumentoRepository`.
- Sin cambios en los endpoints ni en los JSON.

**Cómo queda un alta** (lo que se guarda)
```
POST /api/bolsines            → bolsin (estado_id = Bolsín/Enviado)
                                + cambio_estado_bolsin (Enviado, inicio = ahora, fin = null, empleado = logueado)
POST /api/bolsines/{n}/remitos → remito (estado_id = Remito/Enviado, bolsin_numero = n)
                                + detalle_remito → documentacion (estado_id = EnBolsinEnviado, tipo del catálogo)
                                + cambio_estado_documentacion (EnBolsinEnviado, inicio = ahora, fin = null, empleado = logueado)
```
Así, un bolsín creado desde la pantalla auxiliar aparece en el paso 3 del CU (`sosEnviado()` por historial = true) y se puede recibir en el paso 11.

**Conceptos**
- **Por qué hacía falta el cambio de estado inicial**: desde el paso 9 `sosEnviado()` mira el historial. Un bolsín sin `CambioEstadoBolsin` no tiene estado "actual" y nunca aparecería como pendiente de recepción, aunque su atributo `estado` diga Enviado. El historial y el atributo tienen que ser coherentes (D12).
- **Referenciar en vez de crear**: el bolsín nuevo apunta a filas que **ya existen** (estado, tipo de documento, comisiones, empleado). Al guardar, Hibernate solo copia sus ids en las FK (`estado_id`, `tipo_documento_id`, `empleado_id`…). Antes, el `new TipoDocumento(...)` habría intentado crear un tipo nuevo por cada documentación (y fallado: sin cascada → `TransientPropertyValueException`; con cascada → violaría el UNIQUE `uk_tipo_documento_nombre`).
- **`save()` de un bolsín nuevo con clave asignada**: como el número ya viene cargado, Spring Data hace `merge`: `SELECT` por número (no encuentra nada) y después `INSERT` del bolsín y, por la cascada ALL, `INSERT` de su cambio de estado inicial.
- **Agregar un remito**: `agregarRemitoABolsin` carga el bolsín, agrega el remito a la lista y hace `save`. Por las cascadas Bolsin → remitos (ALL) → detalles (ALL) → documentacion (PERSIST/MERGE) → cambiosEstado (ALL) se insertan remito, detalle, documentación y su cambio de estado en la misma transacción.

### Paso 11b — Frontend y opciones de recepción (A3, A3b)

**Qué se cambió**
- Backend:
  - `Dto/Requests.java`: `record RecibirBolsin(int opcion)`.
  - `ApiException.noImplementado(mensaje)` → HTTP 501.
  - `RecepcionRestController.recibir(numero, @RequestBody RecibirBolsin)`: valida la opción (400 / 501) **antes** de todo y llama `controller.registrarRecepcion(numero, opcion)`.
  - `RecepcionBolsinController.registrarRecepcion(numero, opcion)`: `tomarSeleccionBolsin(n)` → `tomarOpcionDeRecepcion(opcion)` → `tomarConfirmacion(true)` (ya no el 1 fijo).
  - `GestorRecepcionBolsin.tomarConfirmacion()`: sigue solo si confirmó **y** `opcionDeRecepcion == 1`.
- Frontend (pantalla de recepción + `Badge.tsx` + `CrearBolsinPage` + `index.css`):
  - `recepcion/types/recepcion.types.ts`: `OpcionRecepcion = 1 | 2 | 3 | 4` y `OPCIONES_RECEPCION` (textos de la ERS).
  - `recepcion/services/recepcion.service.ts`: `recibir({ numero, opcion })` manda `{ opcion }`.
  - `recepcion/hooks/useRecepcion.ts`: solo un comentario (el hook ya pasaba el argumento al servicio).
  - `recepcion/components/DetalleRemitoPanel.tsx`: fuera "Rechazar" y "Recibir bolsín"; radios con las 4 opciones + "Continuar"; prop `onContinuar(opcion)`.
  - `recepcion/components/RecepcionPage.tsx`: `continuar(opcion)`: 1 → abre la confirmación guardando `{ numero, opcion }`; 2/3/4 → aviso "Opción no implementada". `confirmar()` manda número y opción. `cancelar()` = flujo alternativo A6 (sin cambios). Se quitó el aviso viejo de "Rechazar".
  - `shared/components/Badge.tsx`: colores para los nombres nuevos.
  - `bolsines/components/CrearBolsinPage.tsx`: "en estado Enviado".
  - `index.css`: `.opciones-recepcion` (borde, leyenda y etiquetas).
- Sin cambios: `ConfirmarRecepcionModal`, `RecepcionExitosaModal`, `BolsinesPendientesTable`, `RemitosList`, `SesionCard`, el resto de los endpoints y JSON.

**Contrato del POST**
```
POST /api/recepcion/bolsines/{numero}/recibir      Content-Type: application/json
{ "opcion": 1 }  → 200  BolsinDto (igual que antes)
{ "opcion": 2 }  → 501  {"mensaje":"Opcion de recepcion no implementada: 2."}
{ "opcion": 9 }  → 400  {"mensaje":"Opcion de recepcion invalida: 9. Valores: 1 a 4."}
{ }              → 400  {"mensaje":"Opcion de recepcion invalida: 0. Valores: 1 a 4."}
(sin cuerpo)     → 400  {"mensaje":"El cuerpo de la peticion no es valido."}
bolsín no pendiente → 409 (como antes)
```
Verificado con `curl` contra el backend levantado en H2 en memoria (puerto 8099): 501, 501, 400, 400, 400. La opción 1 se prueba con datos en el paso 12.

**Secuencia ↔ pantalla**
```
Paso 7  Pantalla: el usuario elige una opción (radio) y "Continuar"   → tomarOpcionDeRecepcion(opcion) (viaja en el POST)
Paso 9  Pantalla: solicitarConfirmacion → ConfirmarRecepcionModal     (solo opción 1; 2/3/4 → "Opción no implementada")
Paso 10 Usuario confirma → POST {opcion}                               → tomarConfirmacion(true) → paso 11
A6      Usuario cancela → se cierra el modal, no se llama a la API
```

**Conceptos**
- **`@RequestBody`**: Spring convierte el JSON del cuerpo en el `record` (Jackson usa los nombres de los componentes: `opcion`). Si falta un campo `int`, queda en `0` (valor por defecto del primitivo); por eso `{}` da "opción inválida: 0".
- **Validar en la capa de presentación y en el Gestor**: la API rechaza la petición con un mensaje claro para el cliente (código HTTP); el Gestor protege la regla del dominio aunque lo llame otro cliente. La validación de la API va antes del `synchronized` y de `tomarSeleccionBolsin`, así un error nunca deja estado a medias en el Gestor.
- **Códigos HTTP usados**: 400 (petición mal formada o valor fuera de rango), 409 (conflicto con el estado: el bolsín ya no está pendiente), 501 (el servidor no implementa esa funcionalidad).
- **Estado de la pantalla en React**: `confirmando` guarda `{ numero, opcion }` para que el modal de confirmación sepa qué mandar; el `useState` de la opción en el panel se reinicia con `useEffect` al cambiar de bolsín.

### Paso 12 — Datos de prueba, sesión y primera prueba en ejecución

**Qué se cambió**
- Nuevo `main.Config.CargadorDatosDePrueba` (`ApplicationRunner`, `@Component`, `@Order(1)`): si no hay estados, carga la lista aprobada usando **solo los repositorios del dominio**.
- Nuevo `main.Config.InicioDeSesion` (`ApplicationRunner`, `@Component`, `@Order(2)`): `usuarioRepository.buscarPorNombre(bolsines.usuario-logueado)` → `new Sesion(usuario)` → `gestor.setSesionActual(sesion)`.
- Ajuste posterior: constructores nuevos de `Bolsin` y `Documentacion` con la fecha (generación / pase); el cargador usa el día del envío para bolsín, remito y documentación.

**Prueba en ejecución** (`mvn clean spring-boot:run` desde `backend/`, base `jdbc:h2:file:./db/bolsines`):
```
Log:  Datos de prueba: base vacia, cargando... / cargados: 13 estados, 4 tipos, 2 CM, 2 empleados, bolsines 1001 y 1002.
      Sesion iniciada para el usuario 'jero'.
GET  /api/recepcion/sesion          → jero, Jeronimo Abdala, CM 1 Cordoba
GET  /api/recepcion/bolsines        → 1001 Enviado (2 remitos, 2,5 kg) · 1002 Enviado (1 remito, 1,2 kg)
GET  /api/recepcion/bolsines/1001   → Origen Buenos Aires, PREC-001, Remito #500 Doc #1, Remito #501 Doc #2
POST /api/recepcion/bolsines/1001/recibir {"opcion":1} → 200: bolsín RecibidoEnCMDestino,
                                       remitos RecibidoYAceptado, documentos RecibidaYAceptada
GET  /api/recepcion/bolsines        → solo 1002
GET  /api/recepcion/bolsines/1001   → 409 "no esta pendiente de recepcion"
Reinicio (mvn spring-boot:run)      → "la base ya tiene datos, no se carga nada"; pendientes: solo 1002
```

**SQL del POST** (todo en una transacción, generado por el dirty checking + cascadas):
```
insert into cambio_estado_bolsin (...)            -- RecibidoEnCMDestino, Jeronimo
insert into cambio_estado_documentacion (...) x2  -- RecibidaYAceptada, Jeronimo
update bolsin set ... estado_id                   -- atributo estado
update cambio_estado_bolsin set ... fecha_hora_fin -- cierra el Enviado
update remito set ... estado_id x2
update documentacion set ... estado_id x2
update cambio_estado_documentacion set ... fecha_hora_fin x2  -- cierra los EnBolsinEnviado
update cambio_estado_bolsin / _documentacion set bolsin_numero / documentacion_numero  x3  -- FK (unidireccional)
```

**Base después de recibir** (consultas de la sección "Cómo probarlo"):
```
bolsin:  1001 PREC-001 2.5 BOL RecibidoEnCMDestino | 1002 PREC-002 1.2 BOL Enviado
cambio_estado_bolsin:
  1001 Enviado             2026-11-10 16:23:03 → 2026-11-12 16:23:20  María López
  1001 RecibidoEnCMDestino 2026-11-12 16:23:20 → null                 Jeronimo Abdala
  1002 Enviado             2026-11-10 16:23:03 → null                 María López
remito:  500, 501 RecibidoYAceptado | 502 Enviado
documentacion: 1, 2 RecibidaYAceptada | 3 EnBolsinEnviado
cambio_estado_documentacion: 1 y 2 → EnBolsinEnviado (María, cerrado) + RecibidaYAceptada (Jeronimo, actual); 3 → EnBolsinEnviado (María, actual)
```

**Conceptos**
- **`ApplicationRunner`**: interfaz de Spring Boot; su `run()` se ejecuta **una vez**, después de que el contexto arrancó (beans creados, base conectada, tablas creadas por `ddl-auto`). `@Order(n)` define el orden entre varios runners (menor primero): por eso los datos existen antes de buscar el usuario de la sesión.
- **`@Component`**: marca la clase para que Spring la encuentre en el *component scan* (paquete `main` y subpaquetes) y la cree como bean, inyectándole por constructor lo que pide.
- **Carga "solo si está vacía"**: con `ddl-auto=update` y base de archivo, los datos sobreviven a los reinicios; sin la condición se intentaría insertar todo de nuevo y fallaría por las claves (bolsín 1001 repetido, UNIQUE de estado).
- **Fuera de un request no hay open-in-view**: en el cargador cada `guardar()` es su propia transacción y, al terminar, los objetos quedan *detached* (con id, sin sesión). Se pueden usar igual como referencia (por ejemplo, el Estado en el bolsín) porque Hibernate solo necesita su id para la FK. Para la CM se usa la instancia que devuelve `save()` (con clave asignada, `save` hace `merge` y devuelve otra instancia).
- **Orden de guardado = orden de las FK**: primero lo que es referenciado (estado, tipo, CM, empleado), después lo que referencia (bolsín y sus partes).
- **Consola H2 embebida**: corre dentro de la app; abre el mismo archivo que la app (mismo proceso). Con la app apagada, el archivo se puede abrir con la herramienta de H2 por línea de comandos.

### Paso 13 — Test automático del CU 28

**Qué se agregó**
- `backend/src/test/resources/application-test.properties`: H2 en memoria `jdbc:h2:mem:bolsines-test;DB_CLOSE_DELAY=-1`, `ddl-auto=create-drop`, sin `show-sql`, sin consola.
- `backend/src/test/java/main/RecepcionBolsinFlujoTest.java`: `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, `@TestMethodOrder(OrderAnnotation)`.
- No hizo falta tocar el `pom.xml` (`spring-boot-starter-test` ya estaba).

**Qué verifica (en orden)**

| # | Test | Qué comprueba |
|---|---|---|
| 1 | `datosDePrueba_fechasIgualesAlEnvio` | `fecha_generacion`, `fecha` del remito, `fecha_pase` y el inicio del cambio de estado = hoy − 2 días |
| 2 | `paso2_sesionDelEmpleadoLogueado` | `GET /sesion` → jero, CM Cordoba |
| 3 | `paso3y4_bolsinesEnviadosALaCMDelEmpleado` | `GET /bolsines` → 1001 y 1002, ambos Enviado |
| 4 | `paso5y6_remitosYDocumentacionDelBolsinSeleccionado` | `GET /bolsines/1001` → 2 remitos, información con Remito #500 y #501 |
| 5 | `opcionNoImplementada_responde501YNoCambiaNada` | opción 2 → 501 "no implementada", opción 9 → 400; el 1001 sigue Enviado con 1 solo cambio de estado |
| 6 | `paso11_recibirConOpcion1` | `POST {"opcion":1}` → RecibidoEnCMDestino, remitos RecibidoYAceptado, documentos RecibidaYAceptada |
| 7 | `paso11_historialDelBolsinConElEmpleadoResponsable` | 2 cambios: Enviado (María López, cerrado) y RecibidoEnCMDestino (Jeronimo Abdala, actual); `fin` del anterior = `inicio` del nuevo |
| 8 | `paso11_historialDeLaDocumentacionYEstadoDeLosRemitos` | docs 1 y 2: EnBolsinEnviado (cerrado) + RecibidaYAceptada (Jeronimo, actual); remitos RecibidoYAceptado; el 1002 y el doc 3 sin tocar |
| 9 | `despuesDeRecibir_soloQuedaPendienteEl1002` | `GET /bolsines` → solo 1002; `GET /bolsines/1001` → 409 |

**Resultado**: `Tests run: 9, Failures: 0, Errors: 0, Skipped: 0` — `BUILD SUCCESS`. `backend/db` no se creó.

**Conceptos**
- **`@SpringBootTest`**: levanta el **contexto completo** de la aplicación (los mismos beans, Hibernate, el cargador y la sesión) dentro del test. Es un test de integración, no unitario.
- **MockMvc** (`@AutoConfigureMockMvc`): simula requests HTTP contra los `@RestController` sin abrir un puerto real; pasa por los mismos filtros e interceptores (incluido open-in-view) y permite verificar el código de estado y el JSON (`jsonPath("$.estado", is(...))`).
- **Perfiles de Spring** (`@ActiveProfiles("test")`): con el perfil `test` activo, Spring lee `application.properties` **y además** `application-test.properties`, cuyas propiedades pisan a las primeras.
- **H2 en memoria + `create-drop`**: la base vive solo mientras corre el test (no deja archivos) y arranca vacía, así que el cargador siempre pone los mismos datos: el test es **repetible**.
- **`JdbcTemplate`**: ejecuta SQL directo para verificar lo que quedó en las tablas (estado, historial con su empleado). Se usa en el test porque fuera de un request no hay sesión JPA abierta para recorrer colecciones LAZY.
- **Orden de los tests**: JUnit no garantiza orden por defecto; `@TestMethodOrder(OrderAnnotation)` + `@Order(n)` lo fija. Es aceptable acá porque se prueba una secuencia (el CU) sobre una base compartida.

### Paso 14 — Documentación

- **README.md**: requisitos, cómo levantar backend y frontend, datos de prueba, recorrido del CU 28 (opción 1, A6, opciones 2-4), reinicio de datos, `mvn test` y API. Sin consola H2, SQL ni SQLite.
- **ARQUITECTURA.md**: capas, persistencia (Spring Data JPA + Hibernate + H2), tablas, relaciones y cascadas, entidades, estados y estructura; el Iterator solo como pendiente.
- **CASO_DE_USO_RECEPCION.md**: los 12 pasos de la ERS y A1–A6 con pantalla, endpoint y clase/método; lo no implementado marcado (filtro del paso 4 / A1 / A2, opciones 2-4 / A3-A5, CU 29).
- Comentarios del código: sin referencias a este archivo ni al SQLite viejo; números de paso alineados con la ERS (elegir opción = 8, confirmar = 10).

### Paso 15 — Verificación final

```
mvn test                                  → Tests run: 9, Failures: 0, Errors: 0 — BUILD SUCCESS
mvn spring-boot:run (8099, base aparte)   → base vacia, cargando... → cargados → Sesion iniciada para 'jero'; 0 excepciones
GET  /api/recepcion/sesion                → jero, Jeronimo Abdala, CM 1 Cordoba
GET  /api/recepcion/bolsines              → 1001 y 1002 Enviado (BA → Cordoba, 2.5 / 1.2 kg, generados 2026-11-10)
GET  /api/recepcion/bolsines/1001         → Origen Buenos Aires, PREC-001, Remito #500 Doc #1, Remito #501 Doc #2
POST /api/recepcion/bolsines/1001/recibir {"opcion":1} → 200: 1001 RecibidoEnCMDestino; remitos RecibidoYAceptado; docs RecibidaYAceptada
GET  /api/recepcion/bolsines              → solo 1002
Herramienta H2 (con la app apagada):
  estado: 13 filas (BOL 2, DOC 9, REM 2)
  bolsin: 1001 2026-11-10 2.5 RecibidoEnCMDestino | 1002 2026-11-10 1.2 Enviado
  cambio_estado_bolsin: 1001 Enviado (María López, 11-10 → 11-12) · 1001 RecibidoEnCMDestino (Jeronimo Abdala, actual) · 1002 Enviado (María López, actual)
  remito: 500, 501 RecibidoYAceptado | 502 Enviado
  documentacion: 1, 2 RecibidaYAceptada | 3 EnBolsinEnviado (fecha_pase 2026-11-10)
  cambio_estado_documentacion: docs 1 y 2 EnBolsinEnviado (María, cerrado) → RecibidaYAceptada (Jeronimo, actual); doc 3 EnBolsinEnviado (actual)
Reinicio                                  → "la base ya tiene datos, no se carga nada"; pendientes: solo 1002
```

### Cómo probarlo (instrucciones)

**1. Backend** (terminal 1):
```
cd backend
mvn clean spring-boot:run
```
Esperar `Started BolsinesApplication`, `Datos de prueba: ... cargados` (o "ya tiene datos") y `Sesion iniciada para el usuario 'jero'`. Queda en `http://localhost:8080`.

**2. Frontend** (terminal 2; la primera vez `npm ci`):
```
cd frontend
npm ci        (solo la primera vez)
npm run dev
```
Abrir `http://localhost:5173` (Vite redirige `/api` al backend).

**3. En la pantalla**: menú **Registrar recepcion** (`/recepcion`) → se ve la sesión (Jeronimo Abdala, CM Cordoba) y los bolsines 1001 y 1002 en amarillo (Enviado) → clic en **1001** → se ve origen, precinto, peso y sus remitos/documentos → elegir la opción **1** → **Continuar** → **Confirmar recepcion** → modal "Recepcion registrada" con el bolsín en verde (RecibidoEnCMDestino) y los documentos RecibidaYAceptada → **Listo** → en la tabla queda solo el **1002**. Probar también: opción 2, 3 o 4 → aviso "Opción no implementada"; opción 1 → **Cancelar** en la confirmación (A6) → no cambia nada.

**4. Consola H2** (con el backend levantado): `http://localhost:8080/h2-console`
- Driver Class: `org.h2.Driver`
- JDBC URL: `jdbc:h2:file:./db/bolsines`
- User Name: `sa` · Password: (vacío)
- **Connect**. (La ruta `./db` es relativa a la carpeta desde la que se levantó el backend: `backend/`.)

**5. Consultas SQL**:
```sql
-- Estados precargados
SELECT id, ambito, nombre, descripcion FROM estado ORDER BY ambito, id;

-- Bolsines con su estado actual (atributo)
SELECT b.numero, b.nro_precinto, b.peso, e.nombre AS estado
FROM bolsin b JOIN estado e ON e.id = b.estado_id ORDER BY b.numero;

-- Historial del bolsín con el empleado responsable
SELECT ce.bolsin_numero AS bolsin, e.nombre AS estado, ce.fecha_hora_inicio, ce.fecha_hora_fin,
       emp.nombre || ' ' || emp.apellido AS responsable
FROM cambio_estado_bolsin ce
JOIN estado e ON e.id = ce.estado_id
JOIN empleado emp ON emp.id = ce.empleado_id
ORDER BY ce.bolsin_numero, ce.fecha_hora_inicio;

-- Remitos (sin historial)
SELECT r.numero AS remito, r.bolsin_numero AS bolsin, e.nombre AS estado
FROM remito r JOIN estado e ON e.id = r.estado_id ORDER BY r.numero;

-- Documentación con su estado y tipo
SELECT d.numero AS doc, d.asunto, e.nombre AS estado, t.nombre AS tipo
FROM documentacion d
JOIN estado e ON e.id = d.estado_id
JOIN tipo_documento t ON t.id = d.tipo_documento_id ORDER BY d.numero;

-- Historial de la documentación con el empleado responsable
SELECT ce.documentacion_numero AS doc, e.nombre AS estado, ce.fecha_hora_inicio, ce.fecha_hora_fin,
       emp.nombre || ' ' || emp.apellido AS responsable
FROM cambio_estado_documentacion ce
JOIN estado e ON e.id = ce.estado_id
JOIN empleado emp ON emp.id = ce.empleado_id
ORDER BY ce.documentacion_numero, ce.fecha_hora_inicio;
```
Un cambio con `fecha_hora_fin` NULL es el **actual**.

**6. Reiniciar los datos**: apagar el backend (Ctrl+C), borrar la carpeta `backend/db/` y volver a levantarlo: al encontrar la base vacía, el cargador vuelve a poner 1001 y 1002 en Enviado. (Desde `backend/` en PowerShell: `Remove-Item -Recurse -Force db`; en Git Bash: `rm -rf db`.)

---

## 5. Pendientes fuera de esta tarea

### 5.1 Cambios a hacer en los diagramas
- Colecciones como `List` en lugar de arreglos (`Remito[]`, etc.): JPA no mapea arreglos de entidades.
- Atributos `id` técnicos (Estado, Empleado, DetalleRemito, CambioEstadoBolsin, CambioEstadoDocumentacion, TipoDocumento) que no están en el diagrama de clases: decidir si se muestran.
- Parámetros de los mensajes del paso 11: `recibir(estBolsin, estRemito, estDoc, empleado, fechaHora)`, `aceptar(estRemito, estDoc, empleado, fechaHora)`, `aceptarDocumentacion(estDoc, empleado, fechaHora)`, `esRecibidaYAceptada(estDoc, fechaHora, empleado)`, `crearNuevoCE(estado, fechaHora, empleado)`: verificar que coincidan con el diagrama.
- Constructores de CambioEstado* con `(estado, fechaHoraInicio, empleado)`.
- `Empleado.getCM(): String`, `Empleado.esTuUsuario(Usuario)`, `Bolsin.esTuCM(String)`.
- Métodos de Documentacion `darBaja`, `cancelarRemito`, `rechazar`, `marcarNoRecibida`, `devolverAOrigen` reciben el `Estado` por parámetro.
- Verificar en el diagrama de clases que DetalleRemito → Documentacion tenga multiplicidad "muchos detalles a una documentación" (implementado como `@ManyToOne`).
- Agregar al diagrama de clases la asociación **Documentacion → Estado (1)** (atributo `estado`, además del historial).
- Agregar al diagrama de clases la asociación **Bolsin → Estado (1)** (atributo `estado`, además del historial).
- **Quitar `cantRemitos` de Bolsin** en el diagrama de clases (atributo derivado, marcado por el profe; paso 7).
- `ARQUITECTURA.md` todavía lista `cantRemitos` en Bolsin: se corrige en el paso 14 (documentación).

### 5.2 Para consultar al tutor
- Paso 3: el diagrama pide `getCMOrigen()`/`getNroPrecinto()` en el Gestor; en la implementación los lee el DTO (P3).
- Cómo se modela la "Sesion actual" sin login (P1).
- Responsable del cambio de estado inicial en las pantallas auxiliares (= empleado logueado, P10).
- Los métodos viejos de Documentacion (BAJA, CANCELADA, DEVUELTA…) no mapean 1 a 1 con la DME (P7).
- `esEstadoX()` compara solo el nombre (el ámbito se pregunta aparte) (P6).
- **¿Un remito puede existir sin bolsín?** La DME de Documentación pasa por `EnRemito` **antes** de `EnBolsinSaliente`, lo que sugiere que un remito podría existir sin estar en un bolsín. Nuestro diagrama de clases modela Bolsin → Remito como composición, y en esta app un remito siempre se crea dentro de un bolsín, así que se implementó la FK `remito.bolsin_numero` **NOT NULL** (paso 7). Si el tutor confirma que el remito existe por sí solo, habría que pasarla a nullable y revisar si sigue siendo composición.

### 5.3 Para otra tarea
- `getCMOrigen` (renombre de `getCmOrigen()`) y el contenido del CU 29: marcados por el profe, no se tocan acá. (`llamarCU290()` sí se renombró a `llamarCU29()` en el paso 10, sigue vacío.)
- **Opciones 2, 3 y 4 de recepción del paso 7 del CU 28**: solo se implementa la opción 1 ("el contenido del bolsín es igual al registrado"). Desde A3b el usuario elige la opción en la pantalla y el POST la manda; las opciones 2, 3 y 4 muestran "Opción no implementada" (y la API responde un error). Su comportamiento real (no se recibe toda la documentación, documentación que no corresponde al destino, redirigir a otra área) queda para otra tarea.
- Frontend: fuera de lo autorizado en A3/A3b (Badge.tsx, CrearBolsinPage y los componentes de la pantalla de recepción necesarios para las opciones), no se toca.
- `obtenerComisionesMedicas()`/`crearComisionMedica()` duplicados en `BolsinRepository` y `UsuarioRepository` (decisión 2a del paso 8): unificarlos en un `ComisionMedicaRepository` cambiaría el Gestor.
