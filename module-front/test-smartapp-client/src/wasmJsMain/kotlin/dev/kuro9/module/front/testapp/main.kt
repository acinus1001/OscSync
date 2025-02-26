package dev.kuro9.module.front.testapp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.kuro9.multiplatform.common.serialization.prettyJson
import dev.kuro9.multiplatform.common.types.testapp.TestSerializableClass
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val test = TestSerializableClass(name = "hello", num = -21)
    val json = prettyJson.encodeToString(mapOf("obj" to test))

    document.body!!.appendChild(document.createTextNode(json))
    ComposeViewport(document.body!!) {
        App()
    }
}