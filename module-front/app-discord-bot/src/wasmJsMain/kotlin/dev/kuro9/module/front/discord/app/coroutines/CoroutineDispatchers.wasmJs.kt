package dev.kuro9.module.front.discord.app.coroutines

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual val MainCoroutineContext: CoroutineContext = Dispatchers.Main.immediate
actual val IoCoroutineContext: CoroutineContext = Dispatchers.Main
actual val UnconfinedCoroutineContext: CoroutineContext = Dispatchers.Unconfined