# Práctica para la defensa individual

La defensa incluye un **cambio en vivo**. Practiquen estos ejercicios (cada uno toca las capas MVVM).

## 1. Agregar un campo con validación e ícono de error (ej.: "Ciudad")
1. `model/Candidata.kt`: agregar `val ciudad: String = ""` y subir `version` en `WimDatabase` (la migración destructiva recrea la BD).
2. `validation/CandidataValidator.kt`: `fun validarCiudad(v: String): ValidationResult`.
3. `ui/viewmodel/CandidataFormViewModel.kt`: agregar `ciudad: CampoState` al UiState, `fun onCiudad(...)`, incluirlo en `validarTodo()`, en `hayErrores` y en `Candidata(...)`.
4. `ui/screens/CandidataFormScreen.kt`: un `CampoValidado(...)` con `Icons.Filled.LocationCity`.
5. Agregar un test en `CandidataValidatorTest`.

## 2. Nueva función de almacenamiento local (DAO + Repository)
- Ejemplo: contar evaluaciones de una evaluadora.
  - `EvaluacionDao`: `@Query("SELECT COUNT(*) FROM evaluaciones WHERE evaluadoraClave = :clave") suspend fun contarPorEvaluadora(clave: String): Int`
  - `EvaluacionRepository`: exponerla.
  - Mostrar el número en `PerfilScreen` mediante un ViewModel.
- Otro ejemplo: eliminar una candidatura (`@Delete`) registrando la acción en el historial.

## 3. Nuevo composable o animación
- Un `Badge` con la cantidad de candidatas "En evaluación" en la barra inferior.
- `AnimatedVisibility` para mostrar un aviso cuando el puntaje total supere 85.

## Preguntas probables
- ¿Por qué la validación no está en el composable? → separación de responsabilidades, tests unitarios, la UI solo muestra el estado.
- ¿Cómo se evita la evaluación duplicada? → doble barrera: consulta previa en una transacción + índice único en Room.
- ¿Qué pasa si rechazo el permiso de notificaciones? → la app sigue funcionando; `tienePermiso()` se verifica antes de notificar.
- ¿Por qué el selector de archivos no pide permisos? → Storage Access Framework: la usuaria elige el archivo y se otorga acceso solo a ese URI.
- ¿StateFlow vs State? → el ViewModel expone `StateFlow`; la UI lo convierte en `State` con `collectAsStateWithLifecycle()`.
