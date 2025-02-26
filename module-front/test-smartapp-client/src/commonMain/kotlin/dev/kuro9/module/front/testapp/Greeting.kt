package dev.kuro9.module.front.testapp

class Greeting {
    private val platform = Platform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}