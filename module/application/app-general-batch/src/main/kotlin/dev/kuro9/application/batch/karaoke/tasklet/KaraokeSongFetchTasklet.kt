package dev.kuro9.application.batch.karaoke.tasklet

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderWriter
import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.repository.KaraokeRepo
import dev.kuro9.domain.karaoke.service.KaraokeNewSongService
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.stereotype.Component

@[StepScope Component]
class KaraokeSongFetchTasklet(
    private val newSongService: KaraokeNewSongService,
    private val karaokeRepo: KaraokeRepo,
) : ItemStreamIterableReaderWriter<KaraokeSongDto> {

    override fun readIterable(p0: ExecutionContext): Iterable<KaraokeSongDto> {
        return runBlocking { newSongService.fetchNewSongs().await() }
    }

    override fun write(chunk: Chunk<out KaraokeSongDto?>) {
        karaokeRepo.batchInsertSongs(chunk)
    }
}