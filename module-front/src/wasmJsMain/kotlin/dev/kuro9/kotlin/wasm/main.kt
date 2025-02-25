package dev.kuro9.kotlin.wasm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.kuro9.common.serialization.prettyJson
import dev.kuro9.common.types.TestMultiplatformObj
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val test = TestMultiplatformObj(data = 1)
    val json = prettyJson.encodeToString(mapOf("obj" to test))

    document.body!!.appendChild(document.createTextNode(json))
    ComposeViewport(document.body!!) {
        App()
    }
}