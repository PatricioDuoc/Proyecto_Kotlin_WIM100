# Historias de usuario sugeridas para Trello

Repartan las tarjetas entre ambas integrantes y hagan commits desde la cuenta de quien la desarrolla.

| # | Historia | Capas | Sugerencia de commit |
|---|---|---|---|
| HU01 | Como evaluadora quiero ingresar con mi nombre y verificación del dispositivo | Login, SesionViewModel, BiometriaHelper | `feat: agrega login con biometría` |
| HU02 | Como evaluadora quiero registrar una candidatura con datos validados | Form, Validator, Repository, DAO | `feat: agrega validación de correo ficticio en CandidataValidator` |
| HU03 | Como evaluadora quiero adjuntar y ver el CV de prueba | ArchivoHelper, VisorCvDialog | `feat: integra selector de documentos y visor PDF` |
| HU04 | Como evaluadora quiero aplicar la matriz WIM con puntajes y observaciones | EvaluacionScreen/ViewModel/Validator | `feat: agrega matriz ponderada con puntaje en vivo` |
| HU05 | Como sistema quiero impedir evaluaciones duplicadas | EvaluacionRepository, índice único | `feat: bloquea evaluaciones duplicadas por evaluadora` |
| HU06 | Como dirección quiero ver resultados consolidados y resolver | Resultados, Detalle | `feat: agrega ranking consolidado y resolución` |
| HU07 | Como equipo queremos trazabilidad de acciones | HistorialRepository, HistorialScreen | `feat: registra historial de acciones` |
| HU08 | Como usuaria quiero recibir un aviso al registrar una evaluación | NotificacionHelper, PermisosUi | `feat: notificación local con permiso en tiempo de ejecución` |
| HU09 | Como usuaria quiero compartir y abrir enlaces | IntentsHelper | `feat: compartir ranking por intent` |
| HU10 | Pulir UI, animaciones y README | theme, components | `style: animaciones de transición y feedback de error` |
