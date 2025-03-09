package dev.kuro9.module.front.discord.app.coroutines

import kotlin.coroutines.CoroutineContext

expect val MainCoroutineContext: CoroutineContext
expect val IoCoroutineContext: CoroutineContext
expect val UnconfinedCoroutineContext: CoroutineContext