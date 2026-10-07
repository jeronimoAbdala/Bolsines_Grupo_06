# Sistema Bolsines - Prueba Funcional End-to-End

## Objetivo

Implementar el flujo **"Registrar Recepción de Bolsín"** con persistencia básica, pantalla mejorada y arquitectura por capas limpia. Cada capa conoce únicamente a la capa inmediatamente inferior.

## Arquitectura aplicada

```
Presentation (Screens + Router)
        ↓
Controller (intermediario delgado)
        ↓
Service / Gestor (caso de uso + lógica de negocio)
        ↓
Repository (interface en Domain / impl en Infrastructure)
        ↓
Datasource (implementación concreta en Infrastructure)
        ↓
Domain / Entities
```

## Estructura de paquetes

```
src/
├── Main.java                                          (composition root)
└── main/
    ├── Domain/
    │   ├── Entities/                                  (12 entidades de negocio)
    │   └── Repositories/                              (interfaces: puertos de acceso a datos)
    ├── Infrastructure/
    │   ├── Datasources/
    │   │   ├── MemoryBolsinDatasource.java            (persistencia serializada en .dat)
    │   │   └── MemoryUsuarioDatasource.java
    │   └── Repositories/
    │       ├── BolsinRepositoryImpl.java
    │       └── UsuarioRepositoryImpl.java
    ├── Gestores/
    │   └── GestorRecepcionBolsin.java                 (caso de uso)
    ├── Controllers/
    │   └── RecepcionBolsinController.java
    └── Presentation/
        ├── Router/AppRouter.java
        └── Screens/PantallaRegistrarRecepcionBolsin.java
```

## Persistencia

Los datasources usan **Java Object Serialization** para guardar datos en archivos `.dat` dentro de la carpeta `data/`:

- `data/bolsines.dat` — serialización de la lista de bolsines
- `data/usuarios.dat` — serialización de la lista de usuarios

**Comportamiento:**
- Si el archivo existe → se cargan los datos serializados
- Si no existe o hay error → se generan datos de prueba automáticamente
- Cada `guardar()` escribe el archivo completo (operación atómica simple)
- Para reiniciar los datos → borrar la carpeta `data/`

Todos las entidades implementan `java.io.Serializable` (12 clases).

## Responsabilidad de cada clase

| Clase | Capa | Responsabilidad |
|---|---|---|
| `Main` | Composition root | Construye e inyecta todas las dependencias. |
| `AppRouter` | Presentation | Controla la navegación entre pantallas. |
| `PantallaRegistrarRecepcionBolsin` | Presentation | Interacción por consola con validación de entrada, formato visual y muestra de estados reales desde las entidades. |
| `RecepcionBolsinController` | Controller | Intermediario delgado: delega al gestor. |
| `GestorRecepcionBolsin` | Service | Lógica de negocio: buscar usuario, listar bolsines ENVIADO, registrar recepción cambiando estados en cascada. |
| `BolsinRepository` / `UsuarioRepository` | Domain (interface) | Contratos de acceso a datos (puertos). |
| `*RepositoryImpl` | Infrastructure | Implementación de interfaces usando datasources. |
| `Memory*Datasource` | Infrastructure | Persistencia serializada con ArrayList + archivos `.dat`. |

## Flujo de ejecución

1. `Main` arma la cadena de dependencias y llama `router.iniciar()`.
2. Se muestra el usuario logueado y su CM.
3. Se listan los bolsines con estado `ENVIADO` (origen → destino, fecha, cantidad de remitos).
4. El usuario elige un bolsín con validación de entrada.
5. Confirma la recepción con S/N.
6. El gestor cambia: bolsín → `RECIBIDO`, remitos → `RECIBIDO_ACEPTADO`, documentación → `RECIBIDA_ACEPTADA`.
7. Se persiste a disco.
8. Se muestra el detalle de estados reales desde las entidades (no hardcodeado).
9. Al siguiente reinicio, solo aparecen bolsines aún pendientes (`ENVIADO`).

## Pantalla de ejemplo

```
========================================
           SISTEMA BOLSINES
========================================

  Usuario: Jeronimo Abdala
  CM:      Cordoba

----------------------------------------
  BOLINES ENVIADOS
----------------------------------------

  1) Bolsin #1001
     Buenos Aires -> Cordoba
     Fecha: 2026-09-09
     Remitos: 2
     Estado: ENVIADO

  2) Bolsin #1002
     Buenos Aires -> Cordoba
     Fecha: 2026-09-09
     Remitos: 1
     Estado: ENVIADO

----------------------------------------
  Seleccione un numero: > 1
  Confirmar recepcion del Bolsin #1001? (S/N): > S

========================================
  RECEPCION REGISTRADA EXITOSAMENTE
========================================

  Bolsin #1001: RECIBIDO

  Remito #500: RECIBIDO_ACEPTADO
    Documento #1: RECIBIDA_ACEPTADA
      Asunto: Expediente-cardiológico

  Remito #501: RECIBIDO_ACEPTADO
    Documento #2: RECIBIDA_ACEPTADA
      Asunto: Estudios-complementarios

========================================
=== FIN ===
```

## Cómo ejecutar

Compilar: `javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName`

Ejecutar: `java -cp out Main`

Reiniciar datos: borrar carpeta `data/`.

## Criterios de diseño aplicados

- **SRP**: cada clase tiene una única razón de cambio.
- **Constructor Injection**: todas las dependencias se inyectan por constructor.
- **Encapsulación**: datos accesibles solo por getters; mutaciones vía setters de las entidades.
- **Métodos cortos**: `aceptarRemitos()`, `aceptarDocumentacion()`, `cambiarEstadoDocumentacion()`.
- **Separación por capas**: Domain no conoce Infrastructure; interfaces en Domain, impls en Infrastructure.
- **Persistencia básica**: Serialización Java sin frameworks externos.
- **Validación de entrada**: try-catch para NumberFormatException, valores fuera de rango.
- **Estados reales**: la pantalla lee los estados desde las entidades tras el registro, nunca hardcodea.
