package com.example.appsemestral.validation

/**
 * Reglas de validación del formulario de candidatura.
 * Es Kotlin puro (sin Android ni Compose): se puede probar con tests unitarios
 * y la UI solo muestra el mensaje que devuelve cada función.
 */
object CandidataValidator {

    const val NOMBRE_MIN = 3
    const val NOMBRE_MAX = 60
    const val ANIOS_MAX = 50
    const val RESUMEN_MIN = 30
    const val RESUMEN_MAX = 500
    const val CV_MAX_BYTES = 5L * 1024 * 1024 // 5 MB

    /** El caso exige datos ficticios: solo se aceptan dominios de prueba. */
    val DOMINIOS_FICTICIOS = listOf("ejemplo.cl", "example.com", "test.cl")

    private val REGEX_NOMBRE = Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' ]+$")
    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val REGEX_TELEFONO = Regex("^(56)?9\\d{8}$")
    private val REGEX_LINKEDIN =
        Regex("^https://(www\\.)?linkedin\\.com/in/[A-Za-z0-9-]{3,100}/?$", RegexOption.IGNORE_CASE)

    fun validarNombre(valor: String): ValidationResult {
        val v = valor.trim()
        return when {
            v.isEmpty() -> ValidationResult.error("El nombre es obligatorio")
            v.length < NOMBRE_MIN -> ValidationResult.error("Debe tener al menos $NOMBRE_MIN caracteres")
            v.length > NOMBRE_MAX -> ValidationResult.error("Máximo $NOMBRE_MAX caracteres")
            !REGEX_NOMBRE.matches(v) -> ValidationResult.error("Solo letras y espacios")
            v.split(Regex("\\s+")).size < 2 -> ValidationResult.error("Ingresa nombre y apellido")
            else -> ValidationResult.Valido
        }
    }

    fun validarCorreo(valor: String): ValidationResult {
        val v = valor.trim()
        return when {
            v.isEmpty() -> ValidationResult.error("El correo es obligatorio")
            !REGEX_CORREO.matches(v) -> ValidationResult.error("Formato de correo inválido")
            v.substringAfter("@").lowercase() !in DOMINIOS_FICTICIOS ->
                ValidationResult.error("Usa un dominio ficticio: @ejemplo.cl o @example.com")
            else -> ValidationResult.Valido
        }
    }

    fun validarTelefono(valor: String): ValidationResult {
        val digitos = valor.filter { it.isDigit() }
        return when {
            valor.isBlank() -> ValidationResult.error("El teléfono es obligatorio")
            valor.any { !it.isDigit() && it !in " +-" } -> ValidationResult.error("Solo números, espacios y +")
            !REGEX_TELEFONO.matches(digitos) -> ValidationResult.error("Formato: +56 9 XXXX XXXX")
            else -> ValidationResult.Valido
        }
    }

    fun validarArea(valor: String?): ValidationResult =
        if (valor.isNullOrBlank()) ValidationResult.error("Selecciona un área de especialidad")
        else ValidationResult.Valido

    fun validarAnios(valor: String): ValidationResult {
        val numero = valor.trim().toIntOrNull()
        return when {
            valor.isBlank() -> ValidationResult.error("Indica los años de experiencia")
            numero == null -> ValidationResult.error("Ingresa un número entero")
            numero < 0 -> ValidationResult.error("No puede ser negativo")
            numero > ANIOS_MAX -> ValidationResult.error("Máximo $ANIOS_MAX años")
            else -> ValidationResult.Valido
        }
    }

    fun validarResumen(valor: String): ValidationResult {
        val v = valor.trim()
        return when {
            v.isEmpty() -> ValidationResult.error("Describe la experiencia de la candidata")
            v.length < RESUMEN_MIN -> ValidationResult.error("Mínimo $RESUMEN_MIN caracteres (${v.length}/$RESUMEN_MIN)")
            v.length > RESUMEN_MAX -> ValidationResult.error("Máximo $RESUMEN_MAX caracteres")
            else -> ValidationResult.Valido
        }
    }

    fun validarLinkedin(valor: String): ValidationResult {
        val v = valor.trim()
        return when {
            v.isEmpty() -> ValidationResult.error("El enlace al perfil es obligatorio")
            !v.startsWith("https://", ignoreCase = true) -> ValidationResult.error("Debe comenzar con https://")
            !REGEX_LINKEDIN.matches(v) ->
                ValidationResult.error("Formato: https://www.linkedin.com/in/usuario-ficticio")
            else -> ValidationResult.Valido
        }
    }

    fun validarCv(nombreArchivo: String?, tamanoBytes: Long?): ValidationResult = when {
        nombreArchivo.isNullOrBlank() -> ValidationResult.error("Adjunta el CV de prueba en PDF")
        !nombreArchivo.endsWith(".pdf", ignoreCase = true) -> ValidationResult.error("El CV debe ser un archivo PDF")
        tamanoBytes != null && tamanoBytes > CV_MAX_BYTES -> ValidationResult.error("El archivo supera los 5 MB")
        else -> ValidationResult.Valido
    }

    /** Normaliza "+56912345678" o "9 1234 5678" a "+56 9 1234 5678". */
    fun formatearTelefono(valor: String): String {
        val d = valor.filter { it.isDigit() }.removePrefix("56")
        return if (d.length == 9) "+56 ${d[0]} ${d.substring(1, 5)} ${d.substring(5)}" else valor.trim()
    }
}
