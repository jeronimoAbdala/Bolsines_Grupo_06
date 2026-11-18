# CU 28 — Registrar recepción de bolsín

La comisión médica (CM) destino registra que recibió un bolsín enviado desde otra CM, con sus remitos y su documentación. Se usa desde la web (menú **Registrar recepcion**) y cada paso llega al backend por la API REST. Es el único caso de uso del proyecto: los datos de prueba se cargan solos al arrancar.

- **Actor**: empleado de la CM destino (usuario logueado; sin login, se toma de `bolsines.usuario-logueado`).
- **Precondición**: hay una `Sesion` actual (la crea `InicioDeSesion` al arrancar) y existen bolsines en estado Enviado hacia la CM del empleado.
- **Postcondición** (opción 1): el bolsín queda **RecibidoEnCMDestino**, sus remitos **RecibidoYAceptado** y su documentación **RecibidaYAceptada**, con el historial de cambios de estado a cargo del empleado logueado.

Cadena de clases: `RecepcionPage` (frontend) → `PantallaRegistrarRecepcionBolsin` (backend, `@RestController`) → `GestorRecepcionBolsin` → repositorios (`JpaRepository`) / entidades. Cada mensaje del actor es un endpoint de la Pantalla; cada mensaje Gestor → Pantalla es un método privado de la Pantalla que arma la respuesta con los DTO que le da el Gestor (la Pantalla no toca entidades). El Gestor guarda el estado del CU entre pedidos.

## Flujo principal (pasos de la ERS)

EB = empleado de bolsines (actor). ⚠️ = no implementado.

| Paso | ERS | Pantalla web (frontend) | Endpoint | Pantalla Java y Gestor |
|---|---|---|---|---|
| 1 | EB: selecciona la opción para registrar la recepción de un bolsín | Menú **Registrar recepcion** → `RecepcionPage` (el CU empieza acá: la página de Inicio no llama a la API) | `POST /api/recepcion/iniciar` | `PantallaRegistrarRecepcionBolsin.opcRegistrarRecepcionBolsin()` → `habilitarVentana()` → `GestorRecepcionBolsin.opRegistrarRecepcionBolsin()` |
| 2 | Sistema: busca y muestra la CM del usuario logueado | `SesionCard` (usuario, CM y fecha) | (respuesta de `iniciar`: `usuario`) | Gestor: `buscarUsuarioLogueado()`: `Sesion.getUsuario()` y recorrido de empleados con `Empleado.esTuUsuario(usuario)`; la CM: `Empleado.getCM()` → `ComisionMedica.getNombre()`. Pantalla: `mostrarCM()` (toma el `UsuarioDto` del Gestor; 404 si no hay usuario logueado) |
| 3 | Sistema: busca los bolsines en estado enviado para la CM del usuario logueado, y encuentra al menos uno | `BolsinesPendientesTable` (si no hay ninguno muestra un mensaje vacío) | (respuesta de `iniciar`) | Gestor: `buscarBolsinesEnviados(cm)`: recorre `bolsinRepository.findAll()` preguntando `Bolsin.sosEnviado()` y `Bolsin.esTuCM(cm)`; guarda la lista en `bolsinesSeleccionados`. `sosEnviado()` busca el cambio de estado actual (`*CambioEstadoBolsin.sosActual()`) y le pregunta `sosEnviado()`, que le pregunta a su `Estado.esEnviado()`. Con los datos de prueba quedan 1001 a 1005: el 1006 va a Buenos Aires (`esTuCM()` da falso) y el 1007 ya fue recibido (`sosEnviado()` da falso) |
| 4 | Sistema: muestra de cada bolsín su CM origen y número de precinto, habilitando la selección de uno por listado o filtrando por alguno de estos datos | `BolsinesPendientesTable`: primera línea con el texto de `solicitudSeleccion` ("Seleccione un bolsín para registrar su recepción"); columnas Bolsín, Origen, Precinto, Remitos, Estado; filas seleccionables. Arriba de la tabla, `FiltroBolsines`: **Filtrar por** (Número de precinto / CM origen), el dato y los botones **Filtrar** y **Limpiar filtro**. Se filtra en la pantalla con `filtrarBolsines()` (`recepcion/utils/filtroBolsines.ts`): precinto con coincidencia exacta, CM origen con coincidencia parcial (los dos sin distinguir mayúsculas ni tildes); con resultados muestra "Filtrado por …: N de M bolsines" | (respuesta de `iniciar`: `bolsines` y `solicitudSeleccion`; el filtro no llama a la API) | Gestor, en el mismo loop: `getCMOrigen()` y `getNroPrecinto()` de cada bolsín, que entrega sin entidades (`BolsinEnviadoDto`). Pantalla: `mostrarBolsinesParaSeleccion()` arma cada fila (`BolsinParaSeleccionDto`) con `mostrarCMOrigen()` (nombre de la CM origen) y `mostrarNroPrecinto()` ('N/A' si no hay precinto); `solicitarSeleccionBolsin()` devuelve el texto que pide elegir un bolsín. El filtro no tiene método en el Gestor ni endpoint (no está en los diagramas) |
| 5 | EB: selecciona un bolsín del listado | Clic (o Enter) en una fila → `RecepcionPage.seleccionarBolsin(n)` | `POST /api/recepcion/seleccion/{n}` | `PantallaRegistrarRecepcionBolsin.tomarSeleccionBolsin(n)` → `GestorRecepcionBolsin.tomarSeleccionBolsin(n)`: lo busca entre `bolsinesSeleccionados` (`esTuNumero(n)`; si no está, 409), lo vuelve a leer con `findById` y se llama a sí mismo `obtenerInformacionRemito()` (paso 6) |
| 6 | Sistema: para cada remito asociado al bolsín muestra el número de remito, y para cada documentación contenida en el remito muestra el asunto y el tipo de documentación | `DetalleRemitoPanel` → `DatosRemito`: "Remito #N" y, debajo, "Asunto: … \| Tipo: …" por cada documentación. El encabezado (origen, destino, precinto, peso) sale de `BolsinDto` | (respuesta de `seleccion`: `informacionRemito`) | **Patrón Iterator** (ver detalle abajo): `GestorRecepcionBolsin.obtenerInformacionRemito()` le delega a `Bolsin.obtenerInformacionRemito()`, que recorre sus remitos con un `IteradorRemito` (`getNumero()` y `Remito.buscarDocumentacion()`); cada Remito recorre sus detalles con un `IteradorDetalle` y le pide a cada uno `DetalleRemito.getDocumentacion()` (`Documentacion.getAsunto()` y `mostrarTipoDocumentacion()` → `TipoDocumento.getNombre()`). El Gestor guarda el texto en `informacionRemito`. Pantalla: `mostrarDatosRemito()` |
| 7 | Sistema: muestra las opciones de recepción del bolsín y solicita que se seleccione la correspondiente | `DetalleRemitoPanel`, debajo de los datos del paso 6: grupo de 4 radios y botón **Continuar** (deshabilitado hasta elegir) | (respuesta de `seleccion`: `opcionesRecepcion`) | Pantalla: `mostrarOpcionesRecepcion()` (los 4 textos de la ERS) |
| 8 | EB: selecciona la primera opción: "El contenido del bolsín es igual al registrado" | Radio **1** + **Continuar** → `RecepcionPage.continuar()` | `POST /api/recepcion/opcion` con `{"opcion": 1}` | `PantallaRegistrarRecepcionBolsin.tomarOpcionDeRecepcion()` (400 si no es 1..4; 409 sin bolsín seleccionado) → `GestorRecepcionBolsin.tomarOpcionDeRecepcion(1)` |
| 9 | Sistema: pide confirmación de la selección para el registro correspondiente | `ConfirmarRecepcionModal` con el número que devolvió el backend | (respuesta de `opcion`: el número del bolsín) | Pantalla: `solicitarConfirmacion()` |
| 10 | EB: confirma la selección | **Confirmar recepcion** → `RecepcionPage.confirmar()` | `POST /api/recepcion/confirmacion` | `PantallaRegistrarRecepcionBolsin.tomarConfirmacion()` → `GestorRecepcionBolsin.tomarConfirmacion()` |
| 11 | Sistema: actualiza el bolsín a Recibido en CM destino registrando el empleado responsable, los remitos a Recibido y Aceptado, y la documentación a Recibida y Aceptada registrando el empleado responsable | — | (dentro de `confirmacion`) | Gestor: vuelve a leer el bolsín (`findById`), `obtenerEstadoRecibidoEnCMDestino()`, `obtenerEstadoRecibidoYAceptado()`, `obtenerEstadoRecibidaYAceptada()`, `getFechaYHoraActual()`, `Bolsin.recibir(...)` y `bolsinRepository.save()`. Ver detalle abajo |
| 12 | Sistema: se incluye el CU 29 Notificar recepción de bolsín. Informa la ejecución exitosa del caso de uso. Fin del caso de uso | `RecepcionExitosaModal` ("Recepcion registrada", con los estados nuevos) → **Listo**; la lista se vuelve a pedir (`iniciar`) | (respuesta de `confirmacion`: el bolsín recibido) | Gestor: ⚠️ `llamarCU29()` (vacío: el **CU 29 no se implementa**), arma el `BolsinDto` y `finCU()` (vacía la selección y las listas). Pantalla: `informarRecepcion()` (409 si no se registró nada) |

### Paso 6 en detalle: patrón Iterator (orden del diagrama de secuencia)

```
GestorRecepcionBolsin.tomarSeleccionBolsin(n)
├─ *esTuNumero(n) en bolsinesSeleccionados, bolsinRepository.findById(n)   → bolsinSeleccionado
└─ obtenerInformacionRemito()                              (void: guarda el resultado en informacionRemito)
   └─ Bolsin.obtenerInformacionRemito(): String
      ├─ crearIterador(remitos.toArray()) → new IteradorRemito(elementos)
      ├─ primero()
      └─ loop Remitos [while haFinalizado() = false]
         ├─ elementoActual()                               → (Remito)
         ├─ Remito.getNumero()                             "Remito #500"
         ├─ Remito.buscarDocumentacion(): String
         │  ├─ crearIterador(detalleRemito.toArray()) → new IteradorDetalle(elementos)
         │  ├─ primero()
         │  └─ loop Detalles [while haFinalizado() = false]
         │     ├─ elementoActual()                         → (DetalleRemito)
         │     ├─ DetalleRemito.getDocumentacion(): String "  Asunto: ... | Tipo: ..."
         │     │  ├─ Documentacion.getAsunto()
         │     │  └─ Documentacion.mostrarTipoDocumentacion() → TipoDocumento.getNombre()
         │     └─ siguiente()
         └─ siguiente()
```

| Rol del patrón | Clase |
|---|---|
| Iterator | `IIterador` (`primero()`, `siguiente()`, `haFinalizado()`, `elementoActual()`) |
| ConcreteIterator | `IteradorRemito` (recorre `Remito[]`), `IteradorDetalle` (recorre `DetalleRemito[]`) |
| Aggregate | `IAgregado` (`crearIterador(elementos)`) |
| ConcreteAggregate | `Bolsin`, `Remito` |
| Client (del agregado) | `GestorRecepcionBolsin` |

- Los iteradores los **crean y usan los propios agregados**: el Bolsin crea un `IteradorRemito` y con él recorre sus remitos; cada Remito crea un `IteradorDetalle` y con él recorre sus detalles. El Gestor solo le pide `obtenerInformacionRemito()` al bolsín seleccionado: no conoce la colección ni los iteradores.
- El Remito nunca le habla a la Documentacion: se lo pide a su DetalleRemito.
- Texto que arma el Iterator para el bolsín 1001 (datos de prueba). El remito 500 tiene 3 documentaciones de tipos distintos, así que el loop Detalles da 3 vueltas en el 500 y 1 en el 501; el loop Remitos da 2 vueltas:
  ```
  Remito #500
    Asunto: Expediente-cardiológico | Tipo: EXPEDIENTE
    Asunto: Informe-electrocardiograma | Tipo: INFORME
    Asunto: Certificado-de-aptitud | Tipo: CERTIFICADO
  Remito #501
    Asunto: Estudios-complementarios | Tipo: EXPEDIENTE
  ```
- El Iterator se usa solo en el paso 6 (lo acordado con el tutor). Los demás recorridos (paso 3, paso 11 en `Bolsin.recibir()` y `Remito.aceptar()`, estados) siguen siendo `for` sobre `List`.

### Pasos 10 y 11 en detalle (orden del diagrama de secuencia)

```
GestorRecepcionBolsin.tomarConfirmacion(): BolsinDto   (sigue solo si hay bolsín seleccionado y opcionDeRecepcion == 1)
├─ bolsinRepository.findById(numero)    vuelve a leer el bolsín seleccionado en este pedido (persistencia)
├─ obtenerEstadoRecibidoEnCMDestino()   recorre estadoRepository.findAll(): esAmbitoBolsin() && esEstadoRecibidoEnCMDestino()
├─ obtenerEstadoRecibidoYAceptado()     recorre estadoRepository.findAll(): esAmbitoRemito() && esEstadoRecibidoYAceptado()
├─ obtenerEstadoRecibidaYAceptada()     recorre estadoRepository.findAll(): esAmbitoDocumentacion() && esEstadoRecibidaYAceptada()
├─ getFechaYHoraActual()
├─ Bolsin.recibir(estBolsin, estRemito, estDoc, empleadoLogueado, fechaHora)
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
├─ bolsinRepository.save(bolsin)        un solo guardado: las cascadas persisten todo en una transacción
├─ llamarCU29()                         paso 12: vacío (el CU 29 no se implementa)
├─ BolsinDto.de(bolsin)                 los datos que la Pantalla usa en informarRecepcion()
└─ finCU()                              bolsín seleccionado en null y listas vacías
```

- Los estados no se crean: se buscan entre los **estados persistidos** (tabla `estado`) y se pasan por parámetro.
- La **misma fecha/hora** cierra el cambio de estado anterior y abre el nuevo.
- El responsable de los cambios es el **empleado logueado**: el que encontró `buscarUsuarioLogueado()` en el paso 2 (atributo `empleadoLogueado` del Gestor).

## Flujos alternativos (ERS)

| Flujo | ERS | Qué pasa hoy | Dónde |
|---|---|---|---|
| **A1** | No se encuentra bolsín con el número de precinto ingresado | En lugar de la tabla: **No se encontró ningún bolsín con el número de precinto "…".** y el botón **Limpiar filtro**, que vuelve al listado completo (paso 4). Ej.: `PREC-999`. | `BolsinesPendientesTable` + `mensajeSinResultados()` (`filtroBolsines.ts`) |
| **A2** | No se encuentra bolsín con la CM de origen ingresada | En lugar de la tabla: **No se encontró ningún bolsín con la CM de origen "…".** y el botón **Limpiar filtro**. Ej.: `Salta` (`Rosario` sí encuentra el 1003). | `BolsinesPendientesTable` + `mensajeSinResultados()` (`filtroBolsines.ts`) |
| **A3** | Opción 2: no se recibe toda la documentación asociada a los remitos | ⚠️ **No implementado**: la Pantalla Java responde 501 y la web muestra **"Opción no implementada"** y no sigue. | `PantallaRegistrarRecepcionBolsin.tomarOpcionDeRecepcion()` → `RecepcionPage.continuar()` |
| **A4** | Opción 3: existe documentación que no corresponde al destino | ⚠️ **No implementado**: igual que A3. | `PantallaRegistrarRecepcionBolsin.tomarOpcionDeRecepcion()` → `RecepcionPage.continuar()` |
| **A5** | Opción 4: la documentación se debe redirigir a otra área | ⚠️ **No implementado**: igual que A3. | `PantallaRegistrarRecepcionBolsin.tomarOpcionDeRecepcion()` → `RecepcionPage.continuar()` |
| **A6** | El EB no confirma la selección | Se cierra la ventana de confirmación; no se llama a la API y no cambia nada. El Gestor conserva el bolsín y la opción: si se vuelve a tocar **Continuar**, la opción se pisa. | `RecepcionPage.cancelar()` |

A1 y A2 se resuelven en la pantalla: se filtra por **un dato a la vez**, así cada resultado vacío corresponde a un solo flujo alternativo. El filtro se aplica al tocar **Filtrar** (o con Enter), no mientras se escribe.

Para A3, A4 y A5 la Pantalla Java responde **501** "Opción no implementada" sin cambiar nada. El Gestor guarda la opción igual y `tomarConfirmacion()` solo sigue con la opción 1: si después se confirma, responde **409**.

## Errores de la API

| Caso | Respuesta | Dónde |
|---|---|---|
| Opción fuera de 1..4 o sin cuerpo | **400** | `PantallaRegistrarRecepcionBolsin.tomarOpcionDeRecepcion()` |
| El bolsín no está en la lista que se mostró (ya recibido, de otra CM, o sin `iniciar` antes) | **409** "no esta pendiente de recepcion" | `tomarSeleccionBolsin()` |
| Opción o confirmación sin bolsín seleccionado, o confirmación sin la opción 1 | **409** | `tomarOpcionDeRecepcion()`, `informarRecepcion()` |
| No hay usuario logueado | **404** en `POST /iniciar` | `mostrarCM()` |
| Opción 2, 3 o 4 | **501** "Opción no implementada" | `tomarOpcionDeRecepcion()` |

## Estados antes y después (datos de prueba, bolsín 1001)

| Objeto | Antes | Después de recibir (opción 1) |
|---|---|---|
| Bolsín 1001 | Enviado (María López) | **RecibidoEnCMDestino** (Jeronimo Abdala); el cambio Enviado queda cerrado |
| Remitos 500 y 501 | Enviado | **RecibidoYAceptado** |
| Documentación 1, 2 y 3 (remito 500) y 4 (remito 501) | EnBolsinEnviado (María López) | **RecibidaYAceptada** (Jeronimo Abdala); el cambio EnBolsinEnviado queda cerrado |
| Bolsines 1002 a 1005 | Enviado | Enviado (siguen pendientes) |

## No implementado (fuera de alcance)

- Las opciones de recepción 2, 3 y 4 (flujos **A3**, **A4** y **A5**): solo muestran "Opción no implementada".
- Paso 12: el **CU 29** Notificar recepción de bolsín (`GestorRecepcionBolsin.llamarCU29()` está vacío).

## Cómo probarlo

Ver [README.md](README.md): cómo levantar backend y frontend, el recorrido en la pantalla (flujo principal, filtro del paso 4 con A1 y A2, A6 y opciones 2 a 4), cómo reiniciar los datos y cómo correr los tests (`mvn test`).
