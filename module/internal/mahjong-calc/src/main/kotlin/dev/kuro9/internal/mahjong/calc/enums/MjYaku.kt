package dev.kuro9.internal.mahjong.calc.enums

enum class MjYaku(val han: Int, val onlyMenzen: Boolean, val kuiSagari: Boolean, val isYakuman: Boolean) {
    RIICHI(1, true, false, false),
    IPPATSU(1, true, false, false),
    TSUMO(1, true, false, false),

    YAKU_BAKASE(1, false, false, false),
    YAKU_ZIKASE(1, false, false, false),
    YAKU_HAKU(1, false, false, false),
    YAKU_HATSU(1, false, false, false),
    YAKU_CHUU(1, false, false, false),

    TANYAO(1, false, false, false),
    PINFU(1, true, false, false),
    IPECO(1, true, false, false),
    CHANKAN(1, false, false, false),
    HAITEI(1, false, false, false),
    HOUTEI(1, false, false, false),


    DOUBLE_RIICHI(2, true, false, false),
    CHANTA(2, false, true, false),
    HONROUTOU(2, false, false, false),
    SANSHOKU_DOUJUU(2, false, true, false),
    SANSHOKU_DOUKOU(2, false, false, false),
    ITTKITSUKAN(2, false, true, false),
    TOITOI(2, false, false, false),
    SANANKOU(2, false, false, false),
    SANKANTSU(2, false, false, false),
    CHITOITSU(2, true, false, false),
    SHOUSANGEN(2, false, false, false),

    JUNCHANTA(3, false, true, false),
    HONITSU(3, false, true, false),
    RYANPEKO(3, true, false, false),


    CHINITSU(6, false, true, false),

    TENHOU(13, true, false, true),
    CHIHOU(13, true, false, true),
    SUANKOU(13, true, false, true),
    KOKUSHI(13, true, false, true),
    DAISANGEN(13, false, false, true),
    TSUISO(13, false, false, true),
    RYUISO(13, false, false, true),
    CHINROTO(13, false, false, true),
    SYOUSUSI(13, false, false, true),
    CHUREN(13, true, false, true),
    SUKANTSU(13, false, false, true),

    DAISUSI(26, false, false, true),
    SUANKOU_TANKI(26, true, false, true),
    KOKUSHI_13MEN(26, true, false, true),
    CHUREN_9MEN(26, true, false, true),
}
