package dev.mcd.approachminima

val approaches = listOf(
    Approach("ILS", systemMinima = 200, precision = true),
    Approach("PAR", systemMinima = 200, precision = true),
    Approach("LOC", systemMinima = 250, precision = false),
    Approach("SRA (0.5)", systemMinima = 250, precision = false),
    Approach("SRA (1)", systemMinima = 300, precision = false),
    Approach("SRA (2)", systemMinima = 350, precision = false),
    Approach("LNAV", systemMinima = 300, precision = false),
    Approach("VOR", systemMinima = 300, precision = false),
    Approach("VOR/DME", systemMinima = 250, precision = false),
    Approach("NDB", systemMinima = 350, precision = false),
    Approach("NDB/DME", systemMinima = 300, precision = false),
    Approach("VDF", systemMinima = 350, precision = false),
)

data class Approach(
    val name: String,
    val systemMinima: Int,
    val precision: Boolean,
)