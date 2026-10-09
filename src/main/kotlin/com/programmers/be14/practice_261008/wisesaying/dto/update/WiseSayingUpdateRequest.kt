package com.programmers.be14.practice_261008.wisesaying.dto.update

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class WiseSayingUpdateRequest(
    @field:Pattern(regexp = """(?s)^.*\P{javaWhitespace}.*$""", message = "명언 내용은 공백일 수 없습니다.")
    @field:Size(max = 2000, message = "명언 내용은 최대 2000자까지 가능합니다.")
    val content: String? = null,

    @field:Pattern(regexp = """(?s)^.*\P{javaWhitespace}.*$""", message = "작성자는 공백일 수 없습니다.")
    @field:Size(max = 255, message = "작성자는 최대 255자까지 가능합니다.")
    val author: String? = null
)
