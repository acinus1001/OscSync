package dev.kuro9.module.front.application.homepage.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.kuro9.module.front.application.homepage.network.AuthResourceApiService
import dev.kuro9.multiplatform.common.network.ServerInfo
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.w3c.dom.HTMLImageElement

@Composable
fun PrivateImg(
    id: String,
    serverInfo: ServerInfo = koinInject(),
    alt: String = "",
    attrs: (AttrsScope<HTMLImageElement>.() -> Unit)? = null
) {
    Img(src = "$serverInfo/resources/images/$id", alt = alt, attrs = attrs)
}

@Composable
fun PrivateText(
    id: String,
    resourceService: AuthResourceApiService = koinInject(),
) {
    val text = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val data = resourceService.getStringResource(
            id = id,
            nullOn401Or403 = true
        ) ?: return@LaunchedEffect
        text.value = data
    }

    if (text.value == null) return

    for (line in text.value!!.split("\n")) {
        Text(line)
        Br()
    }
}