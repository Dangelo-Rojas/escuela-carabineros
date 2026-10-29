package cl.duoc.escuelacarabineros.utils

import java.security.MessageDigest

/** Las contraseñas nunca se guardan en texto plano: se guarda su hash SHA-256. */
object Seguridad {
    fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
