package dev.kuro9.multiplatform.common.strings

private val CHOSUNG = charArrayOf(
    'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
    'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
)
private val JOUNGSUNG = charArrayOf(
    'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
    'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
)

// 가장 처음 빈 문자는 받침 없음을 의미
private val JONGSUNG = charArrayOf(
    ' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
    'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
    'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
)

/**
 * @return 한글의 경우 풀어쓰기된 string
 */
fun disassembleHangul(text: String): String = buildString {
    for (ch in text) {
        // 한글 유니코드 범위 (가 ~ 힣)
        if (ch !in '가'..'힣') {
            // 한글이 아니라 영문, 숫자, 이미 분리된 자음/모음일 경우 그대로 유지
            append(ch)
            continue
        }

        val unicode = ch.code - 0xAC00
        val cho = unicode / (21 * 28)
        val jung = (unicode % (21 * 28)) / 28
        val jong = unicode % 28

        append(CHOSUNG[cho])
        append(JOUNGSUNG[jung])
        if (jong != 0) { // 받침이 있는 경우만 추가
            append(JONGSUNG[jong])
        }
    }
}