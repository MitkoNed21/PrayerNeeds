package com.mitkoned.prayerneeds.utilities

import android.annotation.SuppressLint
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Path
import java.util.Comparator
import java.util.function.Predicate
import java.util.function.UnaryOperator

public fun writeFile(file: File, content: String, append: Boolean = false) {
    val exceptions = mutableListOf<Exception>()

    lateinit var writer: FileWriter
    try {
        file.createNewFile()

        writer = FileWriter(file, false)
        writer.write(content)
    } catch (e: Exception) {
        exceptions.add(e)
    } finally {
        try {
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            exceptions.add(e)
        }
    }

    manageExceptions(exceptions)
}

public fun writeFile(file: File, content: Any, append: Boolean = false) {
    return writeFile(file, content.toString(), append)
}

// is Uri
public fun writeFile(path: String, content: String, append: Boolean = false) {
    val file = File(path)
    return writeFile(file, content, append)
}

public fun writeFile(file: Path, content: Any, append: Boolean = false) {
    TODO()
    return writeFile(file.toRealPath(), content.toString(), append)
}


public fun readFile(file: File): String? {
    val exceptions = mutableListOf<Exception>()

    lateinit var reader: FileReader
    try {
        reader = FileReader(file.path)

        return reader.readText()
    } catch (e: Exception) {
        exceptions.add(e)
    } finally {
        try {
            //if (reader != null && reader.ready())
            reader.close()
        } catch (e: Exception) {
            exceptions.add(e)
        }
    }

    manageExceptions(exceptions)
    return null
}

private fun manageExceptions(exceptions: Array<Exception>) {

    when (exceptions.size) {
        0 -> {
            return
        }

        1 -> {
            throw exceptions[0]
        }
    }

    throw CombinedException(
        *exceptions
    ).apply {
        fillInStackTrace()
        exceptions.forEach { it.fillInStackTrace() }
    }
}

private fun manageExceptions(exceptions: List<Exception>) {
    manageExceptions(exceptions.toTypedArray())
}

class CombinedException(vararg exceptions: Exception): Throwable() {
    private val errors = mutableListOf<Exception>()

    init {
        errors.addAll(exceptions)
    }

    val exceptions: List<Exception>
        get() = errors

    override val message: String
        get() = "Multiple exceptions were thrown!\n\n" +
                errors.joinToString("\n\n")
}

fun FAIL() {
    throw Exception("Manually triggered exception.")
}