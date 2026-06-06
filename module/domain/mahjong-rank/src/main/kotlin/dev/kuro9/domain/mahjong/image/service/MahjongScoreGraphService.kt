package dev.kuro9.domain.mahjong.image.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.awt.BasicStroke
import java.awt.Color
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@Service
class MahjongScoreGraphService(
    @Value("classpath:asset/nyan.png") nyanGlassImg: Resource,
    @Value("classpath:asset/background.png") backgroundImg: Resource,
) {
    private val xAxisSequence = generateSequence(37) { it + 82 }
    private val yAxisSequence = generateSequence(195) { it - 46 }

    private val nyanGlassBufferedImage = nyanGlassImg.inputStream.use { ImageIO.read(it) }
    private val backgroundBufferedImage = backgroundImg.inputStream.use { ImageIO.read(it) }

    /**
     * 패널 위에 그래프를 그립니다.
     *
     * @param recentData (rank, isNyan) : 순위 (범위 : [1, 4]), 냥글라스 여부 ( true / false )
     * @return 생성 파일 ( 사용 즉시 삭제 권장 )
     */
    fun scoreGraphGen(recentData: List<Pair<Int, Boolean>>): File {
        require(recentData.size <= 100) { "recentData size must be less than 100" }
        require(recentData.all { it.first in 1..4 }) { "recentData rank must be in [1, 4]" }

        val image = BufferedImage(831, 251, BufferedImage.TYPE_INT_RGB)
        val g2 = image.createGraphics()

        g2.drawImage(backgroundBufferedImage, 0, 0, null)
        g2.color = Color(0xD6, 0x5F, 0x2A) // d65f2a
        g2.stroke = BasicStroke(6f, BasicStroke.CAP_BUTT, 0)

        // 그래프 직선
        for (i in 0..(recentData.size - 2)) { // 1->3    2->2    3->1    4->0
            val nowRank = recentData.getOrNull(i)?.first?.takeIf { it in 1..4 } ?: break
            val nextRank = recentData.getOrNull(i + 1)?.first?.takeIf { it in 1..4 } ?: break

            g2.draw(
                Line2D.Double(
                    (xAxisSequence.elementAt(i) + 26).toDouble(), yAxisSequence.elementAt(4 - nowRank).toDouble(),
                    (xAxisSequence.elementAt(i + 1) + 26).toDouble(), yAxisSequence.elementAt(4 - nextRank).toDouble()
                )
            )
        }

        g2.color = Color(0xFF, 0xC0, 0x29) // ffc029

        // 그래프 노드 및 냥글라스 이미지
        for ((i, datum) in recentData.withIndex()) {
            val (rank, isNyan) = datum

            g2.fillOval(xAxisSequence.elementAt(i) + 18, yAxisSequence.elementAt(4 - rank) - 7, 15, 15)
            if (!isNyan) continue
            g2.drawImage(
                nyanGlassBufferedImage,
                xAxisSequence.elementAt(i) - 14,
                yAxisSequence.elementAt(4 - rank) - 30,
                null
            )
        }

        return File.createTempFile("mj_score_graph_", ".png").apply {
            deleteOnExit()
            ImageIO.write(image, "png", this)
        }
    }
//
//    fun statGraphGen() {
//        val data = mapOf(
//            "rank" to listOf(31.58, 26.32, 23.68, 18.42),
//        )
//        val range by columnOf("1위", "2위", "3위", "4위")
//        val share by columnOf(0.31, 0.26, 0.23, 0.18)
//        val df = dataFrameOf(range, share)
//
//        df.plot {
//            pie {
//                slice(share)
//                fillColor(range) {
//                    scale = continuous(KColor.RED..KColor.PURPLE)
//                }
//                size = 25.0
//            }
//            layout {
//                style(Style.Void)
//            }
//        }
//    }
}