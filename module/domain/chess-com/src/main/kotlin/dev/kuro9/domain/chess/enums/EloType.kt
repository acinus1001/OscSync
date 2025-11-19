package dev.kuro9.domain.chess.enums

enum class EloType(val displayName: String) {
    RAPID("래피드"),
    BULLET("불렛"),
    BLITZ("블리츠"),
    DAILY("일일"),
    DAILY960("일일960");
}