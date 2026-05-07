package dev.kuro9.internal.music.connecter.resource

import io.ktor.resources.*

@Resource("/api")
class MusicClientResource {

    @Resource("/music")
    class Music(val parent: MusicClientResource = MusicClientResource()) {

        @Resource("/now")
        class Now(val parent: Music = Music()) {

            @Resource("/pause")
            class Pause(val parent: Now = Now())

            @Resource("/resume")
            class Resume(val parent: Now = Now())

            @Resource("/skip")
            class Skip(val parent: Now = Now())
        }

        @Resource("/queue")
        class QueueGet(val parent: Music = Music())

        @Resource("/queue")
        class QueuePut(val iTunesId: Long, val parent: Music = Music())

    }

    @Resource("/health")
    class HealthCheck(val parent: MusicClientResource = MusicClientResource())
}