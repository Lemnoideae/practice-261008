package com.programmers.be14.practice_261008.wisesaying.dto.create

import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying

data class WiseSayingCreateRequest(
    val content: String,
    val author: String
) {
    fun toEntity(): WiseSaying {
        return WiseSaying(
            content = this.content,
            author = this.author
        )
    }
}
