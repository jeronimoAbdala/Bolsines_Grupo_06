# Bolsines — Clean Architecture + SQLite

## Arquitectura

```
Presentation → Controller → Service (Gestor) → Repository → Datasource → Entities
```

| Capa | Paquete | Rol |
|---|---|---|
| **Presentation** | `Presentation/Screens`, `Presentation/Router` | UI por consola. Solo muestra y captura input. |
| **Controller** | `Controllers` | Intermediario delgado. Delega todo al gestor. |
| **Service** | `Gestores` | Lógica de negocio del caso de uso. |
| **Repository** | `Domain/Repositories` (interfaces) | Contratos de acceso a datos. |
| **Datasource** | `Infrastructure/Datasources` | Implementación concreta (SQLite). |
| **Entities** | `Domain/Entities` | Modelo de dominio. Serializable. |

**Regla**: cada capa solo conoce a la inferior. Dependencies → constructor injection.

## Persistencia

SQLite embebido via `sqlite-jdbc-3.36.0.3.jar` en `lib/`.

- DB: `bolsines.db` (raíz del proyecto)
- Schema creado automáticamente en `DatabaseManager.inicializar()`
- Datos de prueba: `cargarDatosDePrueba()` se ejecuta solo si las tablas están vacías
- Para reiniciar: borrar `bolsines.db`

**Tablas**: `comision_medica`, `empleado`, `usuario`, `bolsin`, `remito`, `detalle_remito`, `documentacion`

## Entidades (alineadas al UML)

| Entidad | Atributos clave | Métodos clave del UML |
|---|---|---|
| `Estado` | ambito, nombre, descripcion | `esEnviado()`, `esAmbitoBolsin/Remito/Doc()`, `esEstadoRecibido*()` |
| `Bolsin` | numero, cantRemitos, nroPrecinto | `sosEnviado()`, `esTuCM()`, `obtenerInformacionRemito()`, `crearNuevoCEO()` |
| `Remito` | numero, fecha, cmOrigen, cmDestino | `buscarDocumentacion()`, `aceptar()` |
| `DetalleRemito` | areaCMDestino | `aceptarDocumentacion()` |
| `Documentation` | fechaPase, asunto | `crear()`, `sosEnviado()`, `esRecibidaYAceptada()`, `rechazar()`, `mostrarTipoDocumentacion()` |
| `CambioEstado*` | fechaHoraInicio/Fin | `sosActual()`, `setFechaHoraFin()`, `cerrar()` |
| `ComisionMedica` | codigo, nombre, telefono | — |
| `Sesion` | + comisionMedica | — |

## Caso de uso: Registrar Recepción

1. `Pantalla` muestra usuario logueado y CM
2. Lista bolsines ENVIADO hacia la CM del usuario
3. Usuario selecciona uno → se muestra info de remitos (origen, precinto, docs)
4. Opción Recibir/Rechazar + confirmación S/N
5. Gestor cambia estados en cascada: Bolsín→RECIBIDO, Remitos→RECIBIDO_ACEPTADO, Docs→RECIBIDA_ACEPTADA
6. Persiste a SQLite
7. Al siguiente reinicio solo aparecen bolsines pendientes

## Compilar y ejecutar

```bash
# Compilar
javac -cp lib/sqlite-jdbc-3.36.0.3.jar -d out (Get-ChildItem -Recurse -Filter *.java src).FullName

# Ejecutar
java -cp "out;lib/sqlite-jdbc-3.36.0.3.jar" Main

# Reiniciar datos
del bolsines.db
```

## Estructura

```
src/
├── Main.java                          (composition root)
└── main/
    ├── Domain/Entities/               (12 entidades UML)
    ├── Domain/Repositories/           (interfaces)
    ├── Infrastructure/
    │   ├── Database/DatabaseManager   (conexión SQLite)
    │   ├── Datasources/              (Sqlite*Datasource)
    │   └── Repositories/             (RepositoryImpl)
    ├── Gestores/                      (caso de uso)
    ├── Controllers/                   (intermediario)
    └── Presentation/                  (screens + router)
lib/
└── sqlite-jdbc-3.36.0.3.jar
```
