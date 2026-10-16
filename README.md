# WIM 100 · Postulación y evaluación (MVP Android)

Aplicación Android en **Kotlin + Jetpack Compose (Material Design 3)** que digitaliza el flujo de
postulación y evaluación del programa **WIM 100 – DYM Cyber**. Proyecto de la asignatura
**DSY1105 Desarrollo de Aplicaciones Móviles** (Duoc UC), Evaluación Parcial 2.

> ⚠️ MVP académico. Todos los datos son **ficticios/sintéticos**. No se integra la API real de LinkedIn
> y la matriz de evaluación es **provisional**, sujeta a validación del Socio Formador.

## Integrantes

| Nombre | Usuario GitHub | Responsabilidades principales |
|---|---|---|
| _Nombre Apellido 1_ | _@usuario1_ | _completar_ |
| _Nombre Apellido 2_ | _@usuario2_ | _completar_ |

Tablero Trello: _enlace_

## Funcionalidades

| Requisito del caso | Implementación |
|---|---|
| Registrar / consultar candidaturas | Formulario validado + lista con búsqueda y filtro por estado |
| Revisar antecedentes | CV en PDF (visor integrado), experiencia y enlace ficticio a perfil profesional |
| Matriz de criterios | 5 criterios ponderados (pesos suman 100 %), puntaje 1–5, total 0–100 en vivo |
| Puntajes, observaciones y evaluadora | Observación por criterio (obligatoria si el puntaje es ≤ 2) + observación general; se guarda el nombre de la evaluadora |
| Evitar evaluaciones duplicadas | Verificación en el Repository **y** índice único `(candidataId, evaluadoraClave)` en Room |
| Trazabilidad | Tabla `historial`: inicio/cierre de sesión, registro, evaluación, intento duplicado, resolución, consulta de CV/perfil, compartir |
| Estado y resultados consolidados | Estados Postulada → En evaluación → Evaluada (≥ 2 evaluaciones) → Seleccionada / No seleccionada; ranking por promedio |
| Perfiles | **Evaluadora** (registra y evalúa) y **Dirección** (consulta y resuelve) |

## Requisitos técnicos (rúbrica) → dónde está en el código

| Requisito | Archivos |
|---|---|
| Interfaz M3 y navegación | `ui/theme/*`, `navigation/WimNavHost.kt`, `ui/WimApp.kt` (barra inferior) |
| Formularios con ícono y error visual | `ui/components/CampoValidado.kt`, `ui/screens/CandidataFormScreen.kt`, `EvaluacionScreen.kt` |
| Validación fuera de los composables | `validation/CandidataValidator.kt`, `validation/EvaluacionValidator.kt`, ViewModels |
| Animaciones | Transiciones del NavHost, `sacudir()` en botones con error, selector de puntaje con rebote (spring), `PuntajeCircular` animado, `animateItem()` en listas, tarjetas expandibles (`animateContentSize`), escudo con pulso infinito, barras de ranking que crecen |
| MVVM | `model/` · `data/local/` (Room) · `data/repository/` · `ui/viewmodel/` · `ui/screens/` + `ui/components/` |
| Persistencia Room | `data/local/WimDatabase.kt`, `data/local/dao/*`, `data/local/DatosSemilla.kt` |
| Recursos nativos (4) | `nativo/` → ver tabla siguiente |

### Recursos nativos y permisos

| Recurso | Permiso | Gestión |
|---|---|---|
| **Biometría / credencial del dispositivo** (ingreso) | `USE_BIOMETRIC` (normal) | Se consulta `BiometricManager.canAuthenticate`; si no hay bloqueo configurado, se informa y se permite ingresar |
| **Notificaciones locales** (al registrar una evaluación) | `POST_NOTIFICATIONS` (en tiempo de ejecución, Android 13+) | Diálogo explicativo → diálogo del sistema; si se rechaza, la app sigue funcionando y Perfil ofrece abrir Ajustes |
| **Selector de documentos + PdfRenderer** (CV) | Ninguno (Storage Access Framework) | Solo se accede al PDF elegido; se persiste el permiso de lectura del URI |
| **Intents** (abrir perfil, abrir PDF con otra app, compartir) | Ninguno | Se controla `ActivityNotFoundException` |

La pantalla **Perfil** muestra el estado real de cada permiso y permite probar cada recurso (útil para la defensa).

## Arquitectura

```
com.example.appsemestral
├── model/              Entidades Room, enums y reglas de negocio puras (CalculadoraPuntaje)
├── data/
│   ├── local/          WimDatabase, Converters, DAOs (dao/), DatosSemilla (datos sintéticos)
│   └── repository/     CandidataRepository, EvaluacionRepository (transacción + anti-duplicados), HistorialRepository
├── validation/         Validadores en Kotlin puro (testeables sin emulador)
├── navigation/         Rutas y NavHost con transiciones animadas
├── nativo/             Biometría, notificaciones, archivos/PDF, intents, UI de permisos
├── di/                 AppContainer (inyección de dependencias manual)
└── ui/
    ├── viewmodel/      Estado con StateFlow + AppViewModelProvider (fábrica)
    ├── screens/        Pantallas (Composables)
    ├── components/     Componentes reutilizables (CampoValidado, PuntajeCircular…)
    └── theme/          Material Design 3 (colores, tipografía, tema claro/oscuro)
```

Flujo de datos: **Screen** (observa `StateFlow` con `collectAsStateWithLifecycle`) → llama funciones del
**ViewModel** → el ViewModel valida y llama al **Repository** → el Repository usa los **DAO** de Room →
Room emite un nuevo `Flow` → la UI se recompone sola.

## Cómo ejecutar

1. Android Studio reciente (compatible con AGP 9.2) con JDK 21 (el que trae Android Studio).
2. `File > Open` y elegir la carpeta del proyecto. Esperar la sincronización de Gradle
   (AGP 9.2.1, Kotlin 2.2.10, KSP 2.2.10-2.0.2, Room 2.8.4, Gradle 9.4.1).
3. Ejecutar en un emulador con **API 24+** (recomendado API 34 o superior con Google Play).
4. Para probar la biometría en el emulador: Ajustes → Seguridad → configurar un PIN y una huella
   (en el panel *Extended controls → Fingerprint* se simula el toque).
5. Tests unitarios de validación: `./gradlew test`.

### Datos de prueba precargados

- 6 candidatas sintéticas con CV de prueba en PDF generado por la app.
- Evaluaciones de **“Evaluadora Demo Uno”** y **“Evaluadora Demo Dos”**.
  Ingresa como *Evaluadora Demo Uno* y abre a *Ignacia Valdés Soto* para ver el bloqueo de duplicados.
- Ingresa como **Dirección** para resolver candidaturas con estado *Evaluada*.

## Guion sugerido para la demo

1. Ingresar como Evaluadora (biometría) → aceptar notificaciones.
2. Registrar una candidata con datos inválidos (ver íconos y mensajes) y luego válidos (adjuntar PDF).
3. Abrir la candidata → ver CV y perfil → evaluar (puntaje ≤ 2 exige justificación) → notificación.
4. Intentar evaluar de nuevo → bloqueo de duplicado (queda en el historial).
5. Cerrar sesión, entrar como Dirección → Resultados → resolver → compartir ranking.
6. Cerrar y reabrir la app: los datos siguen ahí (Room).
