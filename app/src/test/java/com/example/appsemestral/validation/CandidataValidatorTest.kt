package com.example.appsemestral.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests unitarios: la validación es Kotlin puro, se prueba sin emulador. */
class CandidataValidatorTest {

    @Test
    fun nombre_vacio_es_invalido() {
        assertFalse(CandidataValidator.validarNombre("   ").esValido)
    }

    @Test
    fun nombre_sin_apellido_es_invalido() {
        assertEquals("Ingresa nombre y apellido", CandidataValidator.validarNombre("Camila").mensaje)
    }

    @Test
    fun nombre_con_numeros_es_invalido() {
        assertFalse(CandidataValidator.validarNombre("Ana P3rez").esValido)
    }

    @Test
    fun nombre_con_tildes_es_valido() {
        assertTrue(CandidataValidator.validarNombre("Sofía Núñez").esValido)
    }

    @Test
    fun correo_con_dominio_real_es_rechazado() {
        assertFalse(CandidataValidator.validarCorreo("ana@gmail.com").esValido)
    }

    @Test
    fun correo_ficticio_es_valido() {
        assertTrue(CandidataValidator.validarCorreo("ana.perez@ejemplo.cl").esValido)
    }

    @Test
    fun telefono_chileno_con_espacios_es_valido() {
        assertTrue(CandidataValidator.validarTelefono("+56 9 1234 5678").esValido)
        assertTrue(CandidataValidator.validarTelefono("912345678").esValido)
    }

    @Test
    fun telefono_corto_es_invalido() {
        assertFalse(CandidataValidator.validarTelefono("+56 9 1234").esValido)
    }

    @Test
    fun anios_fuera_de_rango_son_invalidos() {
        assertFalse(CandidataValidator.validarAnios("-1").esValido)
        assertFalse(CandidataValidator.validarAnios("51").esValido)
        assertFalse(CandidataValidator.validarAnios("abc").esValido)
        assertTrue(CandidataValidator.validarAnios("7").esValido)
    }

    @Test
    fun linkedin_debe_tener_formato_de_perfil() {
        assertFalse(CandidataValidator.validarLinkedin("http://linkedin.com/in/ana").esValido)
        assertFalse(CandidataValidator.validarLinkedin("https://google.com").esValido)
        assertTrue(CandidataValidator.validarLinkedin("https://www.linkedin.com/in/ana-demo").esValido)
    }

    @Test
    fun cv_debe_ser_pdf_y_menor_a_5mb() {
        assertFalse(CandidataValidator.validarCv(null, null).esValido)
        assertFalse(CandidataValidator.validarCv("cv.docx", 1000).esValido)
        assertFalse(CandidataValidator.validarCv("cv.pdf", 6L * 1024 * 1024).esValido)
        assertTrue(CandidataValidator.validarCv("cv.pdf", 200_000).esValido)
    }

    @Test
    fun telefono_se_formatea() {
        assertEquals("+56 9 1234 5678", CandidataValidator.formatearTelefono("56912345678"))
    }
}
