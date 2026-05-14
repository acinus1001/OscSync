package dev.kuro9.module.front.application.homepage.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import dev.kuro9.module.front.application.homepage.network.AuthResourceApiService
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.*
import org.koin.compose.koinInject

@Composable
fun Contact() {
    val resourceService: AuthResourceApiService = koinInject()

    val additionalContactInfoList = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        val data = resourceService.getJsonResources<List<String>>(
            "b06b9ccc-1987-40e0-a217-315dc31f43b3",
            nullOn401Or403 = true
        )
            ?: return@LaunchedEffect
        additionalContactInfoList.clear()
        additionalContactInfoList.addAll(data)
    }

    Div {
        Text("연락 가능 수단")
        Ul {
            Li {
                A(attrs = { onClick { window.open("mailto:admin@kuro9.dev") } }) {
                    Text("EMAIL : admin@kuro9.dev")
                }
            }
            for (info in additionalContactInfoList) {
                Li { Text(info) }
            }
        }
    }
}