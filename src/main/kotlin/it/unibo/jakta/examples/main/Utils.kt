package it.unibo.jakta.examples.main

import java.io.File

fun saveErrorMapToCsv(data: Map<String, Double>, outputFile: File) {
    outputFile.writeText("time,error\n")
    for ((t, err) in data) {
        outputFile.appendText("$t,$err\n")
    }
}
