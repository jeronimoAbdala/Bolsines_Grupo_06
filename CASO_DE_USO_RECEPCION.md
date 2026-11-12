# CU 28 — Registrar recepción de bolsín

La comisión médica (CM) destino registra que recibió un bolsín enviado desde otra CM, con sus remitos y su documentación. Se usa desde la web (menú **Registrar recepcion**) y cada paso llega al backend por la API REST.

- **Actor**: empleado de la CM destino (usuario logueado; sin login, se toma de `bolsines.usuario-logueado`).
- **Precondición**: hay una `Sesion` actual (la crea `InicioDeSesion` al arrancar) y existen bolsines en estado Enviado hacia la CM del empleado.
- **Postcondición** (opción 1): el bolsín queda **RecibidoEnCMDestino**, sus remitos **RecibidoYAceptado** y su documentación **RecibidaYAceptada**, con el historial de cambios de estado a cargo del empleado logueado.

Cadena de clases: `RecepcionPage` (frontend) → `RecepcionRestController` → `RecepcionBolsinController` → `GestorRecepcionBolsin` → repositorios / entidades.

## Flujo principal (pasos de la ERS)

EB = empleado de bolsines (actor). ⚠️ = no implementado.

| Paso | ERS | Pantalla (frontend) | API | Clase y método |
|---|---|---|---|---|
| 1 | EB: selecciona la opción para registrar la recepción de un bolsín | Menú **Registrar recepcion** → `RecepcionPage` | — | — |
| 2 | Sistema: busca y muestra la CM del usuario logueado | `SesionCard` (usuario, CM y fecha) | `GET /api/recepcion/sesion` | `GestorRecepcionBolsin.buscarUsuarioLogueado()`: `Sesion.getUsuario()` y recorrido de empleados con `Empleado.esTuUsuario(usuario)`; la CM: `getCM()` → `Empleado.getCM()` → `ComisionMedica.getNombre()` |
| 3 | Sistema: busca los bolsines en estado enviado para la CM del usuario logueado, y encuentra al menos uno | `BolsinesPendientesTable` (si no hay ninguno muestra un mensaje vacío) | `GET /api/recepcion/bolsines` | `GestorRecepcionBolsin.buscarBolsinesEnviados()`: `getCM()` y recorrido de `bolsinRepository.obtenerTodos()` preguntando `Bolsin.sosEnviado()` y `Bolsin.esTuCM(cm)`; guarda la lista en `bolsinesSeleccionados`. `sosEnviado()` busca el cambio de estado actual (`*CambioEstadoBolsin.sosActual()`) y le pregunta `sosEnviado()`, que le pregunta a su `Estado.esEnviado()` |
| 4 | Sistema: muestra de cada bolsín su CM origen y número de precinto, habilitando la selección de uno por listado o filtrando por alguno de estos datos | `BolsinesPendientesTable`: columnas Bolsín, Origen, Precinto, Remitos, Estado; filas seleccionables. ⚠️ **El filtro por CM origen o por precinto no está implementado** | (misma respuesta del paso 3) | `BolsinDto` (origen con `getCmOrigen()`, precinto con `getNroPrecinto()`) |
| 5 | EB: selecciona un bolsín del listado | Clic (o Enter) en una fila | `GET /api/recepcion/bolsines/{n}` | `RecepcionRestController.seleccionarBolsin(n)` (verifica que siga pendiente) → `GestorRecepcionBolsin.tomarSeleccionBolsin(n)` |
| 6 | Sistema: para cada remito asociado al bolsín muestra el número de remito, y para cada documentación contenida en el remito muestra el asunto y el tipo de documentación | `DetalleRemitoPanel` + `RemitosList` (Remito #N; Doc #N · asunto · tipo · estado) | (misma respuesta del paso 5) | `GestorRecepcionBolsin.obtenerInformacionRemito()` (texto con origen, precinto, remitos y asuntos) y `BolsinDto`/`RemitoDto`/`DocumentacionDto` (número de remito, asunto y `Documentacion.mostrarTipoDocumentacion()`); la pantalla usa los DTO |
| 7 | Sistema: muestra las opciones de recepción del bolsín y solicita que se seleccione la correspondiente | `DetalleRemitoPanel`: grupo de 4 radios (`OPCIONES_RECEPCION`) y botón **Continuar** (deshabilitado hasta elegir) | — | — (las opciones son fijas de la ERS) |
| 8 | EB: selecciona la primera opción: "El contenido del bolsín es igual al registrado" | Radio **1** + **Continuar** → `RecepcionPage.continuar(1)` | (la opción viaja en el POST del paso 10) | `GestorRecepcionBolsin.tomarOpcionDeRecepcion(1)` |
| 9 | Sistema: pide confirmación de la selección para el registro correspondiente | `ConfirmarRecepcionModal` (`solicitarConfirmacion`) | — | — |
| 10 | EB: confirma la selección | **Confirmar recepcion** → `RecepcionPage.confirmar()` | `POST /api/recepcion/bolsines/{n}/recibir` con `{"opcion": 1}` | `RecepcionRestController.recibir()` (valida la opción) → `RecepcionBolsinController.registrarRecepcion(n, opcion)` → `tomarSeleccionBolsin(n)` → `tomarOpcionDeRecepcion(opcion)` → `GestorRecepcionBolsin.tomarConfirmacion(true)` |
| 11 | Sistema: actualiza el bolsín a Recibido en CM destino registrando el empleado responsable, los remitos a Recibido y Aceptado, y la documentación a Recibida y Aceptada registrando el empleado responsable | — | (dentro del POST) | `obtenerEstadoRecibidoEnCMDestino()`, `obtenerEstadoRecibidoYAceptado()`, `obtenerEstadoRecibidaYAceptada()`, `getFechaYHoraActual()`, `registrarRecepcionBolsin()` → `Bolsin.recibir(...)` → `bolsinRepository.guardar()`. Ver detalle abajo |
| 12 | Sistema: se incluye el CU 29 Notificar recepción de bolsín. Informa la ejecución exitosa del caso de uso. Fin del caso de uso | `RecepcionExitosaModal` ("Recepcion registrada", con los estados nuevos) → **Listo**; la tabla se actualiza | (respuesta del POST: el bolsín actualizado) | ⚠️ **CU 29 no implementado**: `GestorRecepcionBolsin.llamarCU29()` está vacío y no se invoca. `finCU()` existe pero el flujo web no lo llama (cada request vuelve a tomar la selección) |

### Pasos 10 y 11 en detalle (orden del diagrama de secuencia)

```
GestorRecepcionBolsin.tomarConfirmacion(true)          (sigue solo si opcionDeRecepcion == 1)
├─ obtenerEstadoRecibidoEnCMDestino()   recorre estadoRepository.obtenerTodos(): esAmbitoBolsin() && esEstadoRecibidoEnCMDestino()
├─ obtenerEstadoRecibidoYAceptado()     recorre estadoRepository.obtenerTodos(): esAmbitoRemito() && esEstadoRecibidoYAceptado()
├─ obtenerEstadoRecibidaYAceptada()     recorre estadoRepository.obtenerTodos(): esAmbitoDocumentacion() && esEstadoRecibidaYAceptada()
├─ getFechaYHoraActual()
└─ registrarRecepcionBolsin()
   ├─ Bolsin.recibir(estBolsin, estRemito, estDoc, empleado, fechaHora)
   │  ├─ *Remito.aceptar(estRemito, estDoc, empleado, fechaHora)          PRIMERO los remitos
   │  │  ├─ Remito.setEstado(estRemito)                                    (el remito no tiene historial)
   │  │  └─ *DetalleRemito.aceptarDocumentacion(estDoc, empleado, fechaHora)
   │  │     └─ Documentacion.esRecibidaYAceptada(estDoc, fechaHora, empleado)
   │  │        ├─ setEstado(estDoc)
   │  │        ├─ *CambioEstadoDocumentacion.sosActual() → setFechaHoraFin(fechaHora)
   │  │        └─ crearNuevoCE() → new CambioEstadoDocumentacion(estDoc, fechaHora, empleado)
   │  ├─ Bolsin.setEstado(estBolsin)                                       DESPUÉS el bolsín
   │  ├─ *CambioEstadoBolsin.sosActual() → setFechaHoraFin(fechaHora)
   │  └─ crearNuevoCE() → new CambioEstadoBolsin(estBolsin, fechaHora, empleado)
   └─ bolsinRepository.guardar(bolsin)       un solo guardado: las cascadas persisten todo en una transacción
```

- Los estados no se crean: se buscan entre los **estados persistidos** (tabla `estado`) y se pasan por parámetro.
- La **misma fecha/hora** cierra el cambio de estado anterior y abre el nuevo.
- El responsable de los cambios es el **empleado logueado** (`buscarUsuarioLogueado()`).

## Flujos alternativos (ERS)

| Flujo | ERS | Qué pasa hoy | Dónde |
|---|---|---|---|
| **A1** | No se encuentra bolsín con el número de precinto ingresado | ⚠️ **No implementado**: depende del filtro del paso 4, que no existe. | — |
| **A2** | No se encuentra bolsín con la CM de origen ingresada | ⚠️ **No implementado**: depende del filtro del paso 4, que no existe. | — |
| **A3** | Opción 2: no se recibe toda la documentación asociada a los remitos | ⚠️ **No implementado**: la pantalla muestra **"Opción no implementada"** y no sigue. | `RecepcionPage.continuar()` |
| **A4** | Opción 3: existe documentación que no corresponde al destino | ⚠️ **No implementado**: igual que A3. | `RecepcionPage.continuar()` |
| **A5** | Opción 4: la documentación se debe redirigir a otra área | ⚠️ **No implementado**: igual que A3. | `RecepcionPage.continuar()` |
| **A6** | El EB no confirma la selección | Se cierra la ventana de confirmación; no se llama a la API y no cambia nada. | `RecepcionPage.cancelar()` |

Para A3, A4 y A5, si alguien llama a la API directo con la opción 2, 3 o 4, responde **501** sin cambiar nada; además el Gestor solo sigue con la opción 1 (`GestorRecepcionBolsin.tomarConfirmacion()`).

## Errores de la API

| Caso | Respuesta | Dónde |
|---|---|---|
| Opción fuera de 1..4 o sin cuerpo | **400** | `RecepcionRestController.recibir()` |
| El bolsín ya no está pendiente (ya recibido o de otra CM) | **409** "no esta pendiente de recepcion" | `RecepcionRestController.buscarPendiente()` |
| No hay usuario logueado | **404** en `GET /sesion` | `RecepcionRestController.sesion()` |
| Opción 2, 3 o 4 | **501** "Opcion de recepcion no implementada" | `RecepcionRestController.recibir()` |

## Estados antes y después (datos de prueba, bolsín 1001)

| Objeto | Antes | Después de recibir (opción 1) |
|---|---|---|
| Bolsín 1001 | Enviado (María López) | **RecibidoEnCMDestino** (Jeronimo Abdala); el cambio Enviado queda cerrado |
| Remitos 500 y 501 | Enviado | **RecibidoYAceptado** |
| Documentación 1 y 2 | EnBolsinEnviado (María López) | **RecibidaYAceptada** (Jeronimo Abdala); el cambio EnBolsinEnviado queda cerrado |
| Bolsín 1002 | Enviado | Enviado (sigue pendiente) |

## No implementado (fuera de alcance)

- Paso 4: el **filtro** de bolsines por CM origen o por número de precinto, y por lo tanto los flujos **A1** y **A2**.
- Las opciones de recepción 2, 3 y 4 (flujos **A3**, **A4** y **A5**): solo muestran "Opción no implementada".
- Paso 12: el **CU 29** Notificar recepción de bolsín (`GestorRecepcionBolsin.llamarCU29()` está vacío).
- El patrón Iterator para los recorridos (todavía no implementado: los recorridos son `for` sobre `List`).

## Cómo probarlo

Ver [README.md](README.md): cómo levantar backend y frontend, el recorrido en la pantalla (flujo principal, A6 y opciones 2 a 4), cómo reiniciar los datos y cómo correr los tests (`mvn test`).
