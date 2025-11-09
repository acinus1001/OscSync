package dev.kuro9.application.chess.controller

import dev.kuro9.application.chess.service.VrcChessService
import dev.kuro9.domain.chess.integration.vrc.enums.ChessTurn
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chess")
class VrcChessApiController(
    private val chessService: VrcChessService,
) {

    @GetMapping("/new")
    suspend fun startNewGame(
        @RequestParam elo: Int,
        @RequestParam userTurn: String,
        request: HttpServletRequest,
    ): ResponseEntity<String?> {
        val ip = request.getRequestIp()

        // 봇이 백일 경우에만 값 존재
        val botMove: String? = chessService.makeBotGame(
            userIp = ip,
            userTurn = ChessTurn.valueOf(userTurn),
            botELO = elo,
        )

        return ResponseEntity.ok().body(botMove)
    }

    @GetMapping("/eval")
    suspend fun calculate(
        @RequestParam move: String,
        request: HttpServletRequest,
    ): ResponseEntity<String> {
        val ip = request.getRequestIp()

        val bestMove = chessService.doMove(
            userIp = ip,
            userMove = move,
        )

        return ResponseEntity.ok(bestMove)
    }

    private fun HttpServletRequest.getRequestIp(): String {
        val request = this
        var ip: String? = request.getHeader("X-Forwarded-For")
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }


        // X-Forwarded-For는 여러 IP가 쉼표로 나올 수 있음 → 첫 번째가 실제 클라이언트 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
        }

        return ip!!
    }
}