package com.programmers.be14.practice_261008.wisesaying.dto.create

import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying
import java.time.LocalDateTime

data class WiseSayingCreateResponse(
    val id: Long?,
    val content: String,
    val author: String,
    val createdAt: LocalDateTime?
) {
    companion object {
        fun from(wiseSaying: WiseSaying) = WiseSayingCreateResponse(
            id = wiseSaying.id,
            content = wiseSaying.content,
            author = wiseSaying.author,
            createdAt = wiseSaying.createdAt
        )
    }
}
