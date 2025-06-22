package dev.kuro9.internal.mahjong.image

import dev.kuro9.internal.mahjong.calc.enums.MjKaze
import dev.kuro9.internal.mahjong.calc.enums.PaiType
import dev.kuro9.internal.mahjong.calc.model.*
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

@Service
class MjHandPictureService {

    fun getHandPicture(teHai: MjTeHai, gameInfo: MjGameInfoVo): BufferedImage {
        val contentImage = teHai.getImage()
        val padding = 50      // 상하좌우 여백
        val titleHeight = 60  // 제목을 위한 추가 공간

        // 새로운 크기 계산
        val width = contentImage.width + (padding * 2)
        val height = contentImage.height + (padding * 2) + titleHeight

        // 새 이미지 생성
        val finalImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = finalImage.createGraphics()

        // 배경 그리기
        graphics.color = Color(0x36, 0x39, 0x3F)
        graphics.fillRect(0, 0, width, height)

        // 텍스트 설정 및 그리기
        graphics.color = Color.WHITE
        graphics.font = graphics.font.deriveFont(36f)  // 폰트 크기 설정

        // 텍스트 중앙 정렬을 위한 계산
        val text = "${gameInfo.gameSeq} ${gameInfo.honba}본장 ${gameInfo.zikaze.toKrString()}가, 25000"
        val fontMetrics = graphics.fontMetrics
        val textWidth = fontMetrics.stringWidth(text)
        val textX = (width - textWidth) / 2
        val textY = padding + fontMetrics.ascent

        // 텍스트 그리기
        graphics.drawString(text, textX, textY)

        // 콘텐츠 이미지 그리기
        graphics.drawImage(
            contentImage,
            padding,                    // x 위치
            padding + titleHeight,      // y 위치
            null
        )

        graphics.dispose()

        return finalImage
    }

    private fun MjPai.getImage(): BufferedImage {
        val path = "image/pai/${
            when (this.type) {
                PaiType.M -> "manzu"
                PaiType.P -> "pinzu"
                PaiType.S -> "souzu"
                PaiType.Z -> "zihai"
            }
        }/${num.takeIf { it != 0 } ?: 5}.png"
        return ClassPathResource(path).inputStream.use { ImageIO.read(it) }
    }

    private fun getUraPaiImage(): BufferedImage {
        return ClassPathResource("image/pai/ura.png").inputStream.use { ImageIO.read(it) }
    }

    private fun MjTeHai.getImage(): BufferedImage {
        val marginBetweenGroups = 20
        val height = 100

        // 머리패와 각 몸통 패를 개별적으로 분리
        val groupImageLists = mutableListOf<List<BufferedImage>>()

        // 머리패 추가
        if (head.paiList.isNotEmpty()) {
            groupImageLists.add(head.paiList.map { it.getImage() })
        }

        // 멘젠 몸통들을 개별적으로 추가
        body.filter { it.isMenzen() && it !is KanBody }.forEach { mjBody ->
            val bodyImages = mjBody.paiList.sorted().map { it.getImage() }
            if (bodyImages.isNotEmpty()) {
                groupImageLists.add(bodyImages)
            }
        }

        // 후로 몸통들을 개별적으로 추가
        body.filter { it.isHuro() }.forEach { mjBody ->
            val huroImages = mjBody.getHuroImage()
            if (huroImages.isNotEmpty()) {
                groupImageLists.add(huroImages)
            }
        }

        // 전체 너비 계산 (각 그룹의 너비 합 + 마진)
        val totalWidth = groupImageLists.sumOf { it.sumOf { img -> img.width } } +
                (groupImageLists.size - 1) * marginBetweenGroups

        val merge = BufferedImage(totalWidth, height, BufferedImage.TYPE_INT_ARGB)
        val graphic = merge.getGraphics() as Graphics2D

        graphic.color = Color(0x36, 0x39, 0x3F)
        graphic.fillRect(0, 0, totalWidth, height)

        var widthSum = 0

        // 각 그룹을 순차적으로 그리기
        groupImageLists.forEachIndexed { groupIndex, images ->
            // 현재 그룹의 이미지들 그리기
            images.forEach { image ->
                // 이미지를 아래쪽에 맞춰서 그리기
                val yPosition = height - image.height
                graphic.drawImage(image, widthSum, yPosition, null)
                widthSum += image.width
            }

            // 마지막 그룹이 아니면 마진 추가
            if (groupIndex < groupImageLists.size - 1) {
                widthSum += marginBetweenGroups
            }
        }

        graphic.dispose()
        return merge
    }

    private fun MjBody.getHuroImage(): List<BufferedImage> {

        val paiImageList = this.paiList.map { it.getImage() }.toMutableList()

        when (this) {
            is ShunzuBody, is PongBody -> {
                paiImageList[0] = rotateImage(paiImageList.first())
            }

            is KanBody if this.isHuro() -> {
                paiImageList[0] = rotateImage(paiImageList.first())
            }

            is KanBody -> {
                val uraPaiImage = getUraPaiImage()
                paiImageList[0] = uraPaiImage
                paiImageList[3] = uraPaiImage
            }
        }

        return paiImageList
    }

    private fun rotateImage(original: BufferedImage): BufferedImage {
        val width = original.height
        val height = original.width
        val result = BufferedImage(width, height, original.type)
        val graphics = result.createGraphics()
        graphics.translate(width, 0)
        graphics.rotate(Math.PI / 2)
        graphics.drawImage(original, 0, 0, null)
        graphics.dispose()
        return result
    }

    private fun MjKaze.toKrString(): String = when (this) {
        MjKaze.TOU -> "동"
        MjKaze.NAN -> "남"
        MjKaze.SHA -> "서"
        MjKaze.PEI -> "북"
    }
}