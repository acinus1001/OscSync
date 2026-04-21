package dev.kuro9.internal.itunes.network

import io.ktor.resources.*

class ItunesApiResource {

    @Resource("/search")
    class Search(
        val term: String,
        val entity: String = "song",
        val country: String = "jp",
        val limit: Int? = null
    )

    @Resource("/lookup")
    class Lookup(val id: Long)


}