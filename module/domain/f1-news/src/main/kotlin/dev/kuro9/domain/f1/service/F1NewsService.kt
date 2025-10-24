package dev.kuro9.domain.f1.service

import dev.kuro9.domain.f1.repository.table.F1NewsRepo
import org.springframework.stereotype.Service

@Service
class F1NewsService(private val repo: F1NewsRepo) {

    fun getNewsById(id: Long) {
        val news = repo.findById(id)
    }
}