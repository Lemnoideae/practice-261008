package com.programmers.be14.practice_261008.wisesaying.dto.create

import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class WiseSayingCreateRequest(
    @field:NotBlank(message = "명언 내용은 필수입니다.")
    @field:Size(max = 2000, message = "명언 내용은 최대 2000자까지 가능합니다.")
    val content: String,

    @field:NotBlank(message = "작성자는 필수입니다.")
    @field:Size(max = 255, message = "작성자는 최대 255자까지 가능합니다.")
    val author: String
) {
    fun toEntity(): WiseSaying {
        return WiseSaying(
            content = this.content,
            author = this.author
        )
    }
}
