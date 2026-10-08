package com.programmers.be14.practice_261008.wisesaying.dto.get

import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying
import java.time.LocalDateTime

data class WiseSayingGetResponse(
    val id: Long?,
    val content: String,
    val author: String,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?
) {
    companion object {
        fun from(wisesaying: WiseSaying) = WiseSayingGetResponse(
            id = wisesaying.id,
            content = wisesaying.content,
            author = wisesaying.author,
            createdAt = wisesaying.createdAt,
            modifiedAt = wisesaying.modifiedAt
        )
    }
}
