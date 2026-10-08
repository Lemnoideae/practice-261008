package com.programmers.be14.practice_261008.wisesaying.dto.update

import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying
import java.time.LocalDateTime

data class WiseSayingUpdateResponse(
    val id: Long?,
    val content: String,
    val author: String,
    val modifiedAt: LocalDateTime?
) {
    companion object {
        fun from(wisesaying: WiseSaying) = WiseSayingUpdateResponse(
            id = wisesaying.id,
            content = wisesaying.content,
            author = wisesaying.author,
            modifiedAt = wisesaying.modifiedAt
        )
    }
}
