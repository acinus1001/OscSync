package dev.kuro9.internal.chess.api.structure

import io.ktor.resources.*

@Resource("/pub")
class ChessComApi {

    @Resource("/player")
    class Player(val parent: ChessComApi = ChessComApi()) {

        @Resource("/{userName}")
        class User(val parent: Player = Player(), val userName: String) {

            @Resource("/stats")
            class Stats(val parent: User)
        }
    }
}