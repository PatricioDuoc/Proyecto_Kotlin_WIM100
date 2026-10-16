package com.example.appsemestral.data.local

import android.content.Context
import com.example.appsemestral.di.AppContainer
import com.example.appsemestral.model.Candidata
import com.example.appsemestral.model.Criterio
import com.example.appsemestral.nativo.ArchivoHelper

/**
 * Precarga la base de datos con datos 100 % SINTÉTICOS para demostrar el flujo completo.
 * Ningún nombre, correo, teléfono, CV ni perfil corresponde a una persona real.
 */
object DatosSemilla {

    /** Matriz WIM provisional (sujeta a validación del Socio Formador). Pesos suman 100. */
    val criterios = listOf(
        Criterio(nombre = "Trayectoria técnica", descripcion = "Experiencia y profundidad técnica en ciberseguridad.", peso = 25, orden = 1),
        Criterio(nombre = "Liderazgo e influencia", descripcion = "Capacidad de liderar equipos, proyectos o iniciativas.", peso = 20, orden = 2),
        Criterio(nombre = "Aporte a la comunidad", descripcion = "Mentorías, charlas, voluntariado y promoción de mujeres en el área.", peso = 20, orden = 3),
        Criterio(nombre = "Innovación y logros", descripcion = "Logros destacados, investigación, certificaciones o reconocimientos.", peso = 20, orden = 4),
        Criterio(nombre = "Comunicación y visibilidad", descripcion = "Claridad para comunicar y presencia en el ecosistema.", peso = 15, orden = 5)
    )

    private data class Demo(
        val nombre: String, val correo: String, val telefono: String, val area: String,
        val anios: Int, val resumen: String, val linkedin: String
    )

    private val candidatas = listOf(
        Demo("Ignacia Valdés Soto", "ignacia.valdes@ejemplo.cl", "+56 9 1111 2201", Candidata.AREAS[0], 9,
            "Pentester líder en una consultora ficticia. Coordina ejercicios de Red Team y forma a analistas junior en técnicas de intrusión ética.",
            "https://www.linkedin.com/in/ignacia-valdes-demo"),
        Demo("Martina Herrera Lagos", "martina.herrera@ejemplo.cl", "+56 9 1111 2202", Candidata.AREAS[1], 7,
            "Jefa de un SOC ficticio. Diseñó playbooks de detección y redujo los tiempos de respuesta a incidentes en su organización simulada.",
            "https://www.linkedin.com/in/martina-herrera-demo"),
        Demo("Fernanda Castillo Rivas", "fernanda.castillo@ejemplo.cl", "+56 9 1111 2203", Candidata.AREAS[2], 12,
            "Especialista en gobierno de seguridad e ISO 27001 en una empresa ficticia del sector financiero. Mentora en comunidades técnicas.",
            "https://www.linkedin.com/in/fernanda-castillo-demo"),
        Demo("Josefa Navarro Pinto", "josefa.navarro@ejemplo.cl", "+56 9 1111 2204", Candidata.AREAS[3], 5,
            "Ingeniera de seguridad OT en una planta industrial simulada. Implementó segmentación de redes y monitoreo de sistemas SCADA.",
            "https://www.linkedin.com/in/josefa-navarro-demo"),
        Demo("Daniela Bravo Ortiz", "daniela.bravo@ejemplo.cl", "+56 9 1111 2205", Candidata.AREAS[4], 4,
            "Analista forense digital. Participa en respuesta a incidentes y en la preservación de evidencia para casos ficticios de estudio.",
            "https://www.linkedin.com/in/daniela-bravo-demo"),
        Demo("Renata Salinas Mora", "renata.salinas@ejemplo.cl", "+56 9 1111 2206", Candidata.AREAS[5], 3,
            "Investigadora académica en criptografía aplicada. Publica artículos de divulgación y dirige un semillero estudiantil ficticio.",
            "https://www.linkedin.com/in/renata-salinas-demo")
    )

    suspend fun poblarSiVacia(context: Context, container: AppContainer) {
        container.evaluacionRepository.asegurarCriterios(criterios)
        if (container.candidataRepository.contar() > 0) return

        val ids = candidatas.map { d ->
            val (uri, nombreArchivo) = ArchivoHelper.generarCvDemo(context, d.nombre, d.correo, d.area, d.anios, d.resumen)
            container.candidataRepository.registrar(
                Candidata(
                    nombre = d.nombre, correo = d.correo, telefono = d.telefono, area = d.area,
                    aniosExperiencia = d.anios, resumenExperiencia = d.resumen, linkedinUrl = d.linkedin,
                    cvUri = uri.toString(), cvNombre = nombreArchivo,
                    registradaPor = "Sistema (datos semilla)"
                )
            )
        }

        // Evaluaciones de ejemplo: muestran estados "Evaluada" y "En evaluación" y el ranking
        val c = container.evaluacionRepository.obtenerCriterios().map { it.id }
        fun p(vararg v: Int): Map<Long, Int> = c.zip(v.toList()).toMap()
        val ev = container.evaluacionRepository

        ev.registrar(ids[0], "Evaluadora Demo Uno", p(5, 5, 4, 5, 4), emptyMap(), "Perfil técnico muy sólido con liderazgo comprobado.")
        ev.registrar(ids[0], "Evaluadora Demo Dos", p(5, 4, 4, 4, 5), emptyMap(), "Destaca su rol formador y su comunicación clara.")
        ev.registrar(ids[1], "Evaluadora Demo Uno", p(4, 5, 3, 4, 3), emptyMap(), "Gran gestión del SOC; aporte a la comunidad en desarrollo.")
        ev.registrar(ids[1], "Evaluadora Demo Dos", p(4, 4, 4, 3, 4), emptyMap(), "Buen equilibrio entre liderazgo y trayectoria técnica.")
        ev.registrar(ids[2], "Evaluadora Demo Uno", p(5, 4, 5, 4, 4), emptyMap(), "Amplia trayectoria en GRC y mentorías constantes.")
    }
}
