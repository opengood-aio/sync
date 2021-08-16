package io.opengood.project.sync.task

import org.gradle.api.DefaultTask
import org.gradle.internal.logging.text.StyledTextOutput.Style
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf

open class BaseTask : DefaultTask() {

    private val out = project.serviceOf<StyledTextOutputFactory>().create("colored-output")

    protected fun printBlankLine() =
        println()

    protected fun printComplete(name: String) =
        print(Style.ProgressStatus, true, "Completed $name task!")

    protected fun printDone() =
        print(Style.Success, true, "Done!")

    protected fun printExecute(name: String) =
        print(Style.ProgressStatus, true, "Executing $name task...")

    protected fun printFailure(message: String) =
        print(Style.Failure, false, message)

    protected fun printHeader(name: String) =
        print(
            Style.Header, true,
            "***************************************************",
            "$name task",
            "***************************************************"
        )

    protected fun printInfo(message: String) =
        print(Style.Info, false, message)

    protected fun printProgress(message: String) =
        print(Style.ProgressStatus, false, message)

    protected fun printSuccess(message: String) =
        print(Style.Success, false, message)

    private fun print(style: Style, blankLine: Boolean, vararg messages: String) =
        with(out.style(style)) {
            messages.forEach { println(it) }
            if (blankLine) println()
        }
}
