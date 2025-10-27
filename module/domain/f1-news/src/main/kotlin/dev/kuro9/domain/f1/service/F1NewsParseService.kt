package dev.kuro9.domain.f1.service

import dev.kuro9.domain.f1.dto.F1NewsHtmlDto
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jsoup.Jsoup
import org.springframework.stereotype.Service

@Service
class F1NewsParseService {
    private val domain: String = "https://www.formula1.com"
    private val MAX_CHARS: Int = 10000

    private val httpClient = HttpClient {
        install(Logging)
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
        }
        BrowserUserAgent()
        expectSuccess = true
        followRedirects = true
    }

    suspend fun parseNews(path: String): String {
        val url = URLBuilder(domain).apply { path(path) }.build()
        val html = httpClient.get(url).bodyAsText()

        val doc = Jsoup.parse(html)

        // 불필요한 요소 제거
        doc.select("script, style, nav, footer, header, form").forEach { it.remove() }

        // 본문 텍스트 추출 및 정제
        val cleanText = doc.body().text().let {
            if (it.length <= MAX_CHARS) return@let it

            it.substring(0, MAX_CHARS) + "\n... (컨텐츠가 길어서 일부만 잘라냈습니다)"
        }

        return cleanText
    }

    suspend fun parseLatestNews(count: Int = 100): List<F1NewsHtmlDto> {
        require(count > 0) { "count는 1 이상이어야 합니다." }

        val url = URLBuilder(domain).apply { path("/en/latest") }.build()
        val html = httpClient.get(url).bodyAsText()
        val doc = Jsoup.parse(html)

        // with no js
        // /html/body/div/main/div/div/div/div/div[2]/ul/li[1]/span[2]/a

        // with js
        // /html/body/div/main/div/div/div/div/div[3]/ul/li[1]/span[2]/a
        // next element
        // /html/body/div/main/div/div/div/div/div[3]/ul/li[2]/span[2]/a

        return (1..count)
            .mapNotNull { i ->
                val element = doc.selectXpath("/html/body/div/main/div/div/div/div/div[2]/ul/li[$i]").firstOrNull()
                    ?: return@mapNotNull null

                val image = element.select("span > img")
                val content = element.select("span > a")

                F1NewsHtmlDto(
                    imageAlt = image.attr("alt"),
                    imageUrl = image.attr("src"),
                    title = content.text(),
                    path = content.attr("href"),
                    id = content.attr("id"),
                )
            }
    }

}