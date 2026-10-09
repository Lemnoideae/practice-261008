package com.programmers.be14.practice_261008.wisesaying.controller

import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying
import com.programmers.be14.practice_261008.wisesaying.repository.WiseSayingRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WiseSayingControllerTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val wiseSayingRepository: WiseSayingRepository
) {

    @Test
    @DisplayName("A04: 정상 JSON POST 요청 시 200 OK와 생성된 데이터가 반환된다")
    fun createWithValidJson() {
        val requestBody = """
            {
                "content": "꿈을 지녀라. 그러면 어려운 현실을 이길 수 있다.",
                "author": "월트 디즈니"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/wisesaying")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.content").value("꿈을 지녀라. 그러면 어려운 현실을 이길 수 있다."))
            .andExpect(jsonPath("$.author").value("월트 디즈니"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty)
    }

    @Test
    @DisplayName("A05: 정상 JSON PATCH 요청으로 두 필드를 모두 전달하면 200 OK와 수정된 데이터가 반환된다")
    fun updateBothFields() {
        val existing = wiseSayingRepository.save(
            WiseSaying(content = "기존 내용", author = "기존 작가")
        )

        val requestBody = """
            {
                "content": "수정된 내용",
                "author": "수정된 작가"
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/api/v1/wisesaying/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(existing.id))
            .andExpect(jsonPath("$.content").value("수정된 내용"))
            .andExpect(jsonPath("$.author").value("수정된 작가"))

        val updated = wiseSayingRepository.findById(existing.id!!).orElseThrow()
        assertEquals("수정된 내용", updated.content)
        assertEquals("수정된 작가", updated.author)
    }

    @Test
    @DisplayName("A06: content만 전달된 PATCH 요청 시 content만 변경되고 author는 유지된다")
    fun updateContentOnly() {
        val existing = wiseSayingRepository.save(
            WiseSaying(content = "기존 내용", author = "기존 작가")
        )

        val requestBody = """
            {
                "content": "새로운 내용만"
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/api/v1/wisesaying/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(existing.id))
            .andExpect(jsonPath("$.content").value("새로운 내용만"))
            .andExpect(jsonPath("$.author").value("기존 작가"))

        val updated = wiseSayingRepository.findById(existing.id!!).orElseThrow()
        assertEquals("새로운 내용만", updated.content)
        assertEquals("기존 작가", updated.author)
    }

    @Test
    @DisplayName("A06: author만 전달된 PATCH 요청 시 author만 변경되고 content는 유지된다")
    fun updateAuthorOnly() {
        val existing = wiseSayingRepository.save(
            WiseSaying(content = "기존 내용", author = "기존 작가")
        )

        val requestBody = """
            {
                "author": "새로운 작가만"
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/api/v1/wisesaying/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(existing.id))
            .andExpect(jsonPath("$.content").value("기존 내용"))
            .andExpect(jsonPath("$.author").value("새로운 작가만"))

        val updated = wiseSayingRepository.findById(existing.id!!).orElseThrow()
        assertEquals("기존 내용", updated.content)
        assertEquals("새로운 작가만", updated.author)
    }

    @Test
    @DisplayName("A07: {} 빈 객체 PATCH 요청 시 존재하는 ID는 200 OK와 기존 값이 유지된다")
    fun updateWithEmptyObject() {
        val existing = wiseSayingRepository.save(
            WiseSaying(content = "기존 내용", author = "기존 작가")
        )

        mockMvc.perform(
            patch("/api/v1/wisesaying/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(existing.id))
            .andExpect(jsonPath("$.content").value("기존 내용"))
            .andExpect(jsonPath("$.author").value("기존 작가"))

        val updated = wiseSayingRepository.findById(existing.id!!).orElseThrow()
        assertEquals("기존 내용", updated.content)
        assertEquals("기존 작가", updated.author)
    }

    @Test
    @DisplayName("A07: 명시적 null 필드 PATCH 요청 시 존재하는 ID는 200 OK와 기존 값이 유지된다")
    fun updateWithNullFields() {
        val existing = wiseSayingRepository.save(
            WiseSaying(content = "기존 내용", author = "기존 작가")
        )

        val requestBody = """
            {
                "content": null,
                "author": null
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/api/v1/wisesaying/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(existing.id))
            .andExpect(jsonPath("$.content").value("기존 내용"))
            .andExpect(jsonPath("$.author").value("기존 작가"))

        val updated = wiseSayingRepository.findById(existing.id!!).orElseThrow()
        assertEquals("기존 내용", updated.content)
        assertEquals("기존 작가", updated.author)
    }

    @Test
    @DisplayName("A08: 생성 시 content 또는 author가 빈 값이나 공백이면 400 Bad Request이며 레코드가 생성되지 않는다")
    fun createWithBlankFields() {
        val initialCount = wiseSayingRepository.count()

        val blankContentCases = listOf(
            """{"content": "", "author": "작가"}""",
            """{"content": "   ", "author": "작가"}""",
            """{"content": "\u3000", "author": "작가"}""",
            """{"content": "\u2003", "author": "작가"}"""
        )
        for (body in blankContentCases) {
            mockMvc.perform(
                post("/api/v1/wisesaying")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
        }

        val blankAuthorCases = listOf(
            """{"content": "내용", "author": ""}""",
            """{"content": "내용", "author": "   "}""",
            """{"content": "내용", "author": "\u3000"}""",
            """{"content": "내용", "author": "\u2003"}"""
        )
        for (body in blankAuthorCases) {
            mockMvc.perform(
                post("/api/v1/wisesaying")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
        }

        assertEquals(initialCount, wiseSayingRepository.count())
    }

    @Test
    @DisplayName("A09: 수정 시 비null 빈 값이나 공백(유니코드 포함)을 전달하면 400 Bad Request이며 기존 값이 유지된다")
    fun updateWithBlankFields() {
        val existing = wiseSayingRepository.save(
            WiseSaying(content = "기존 내용", author = "기존 작가")
        )

        val blankCases = listOf(
            // 단일 필드 빈 문자열 및 ASCII 공백
            """{"content": ""}""",
            """{"content": "   "}""",
            """{"author": ""}""",
            """{"author": "   "}""",
            // U2-R1: 유니코드 공백 (U+3000 전각 공백, U+2003 EM SPACE)
            """{"content": "\u3000"}""",
            """{"content": "\u2003"}""",
            """{"author": "\u3000"}""",
            """{"author": "\u2003"}""",
            // 유효 필드 + 공백 필드 조합 (부분 갱신 없이 두 필드 모두 기존 값 유지되어야 함)
            """{"content": "새로운 유효 내용", "author": "\u3000"}""",
            """{"content": "\u2003", "author": "새로운 유효 작가"}""",
            """{"content": "새로운 유효 내용", "author": "   "}""",
            """{"content": "   ", "author": "새로운 유효 작가"}"""
        )

        for (body in blankCases) {
            mockMvc.perform(
                patch("/api/v1/wisesaying/${existing.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))

            val checked = wiseSayingRepository.findById(existing.id!!).orElseThrow()
            assertEquals("기존 내용", checked.content)
            assertEquals("기존 작가", checked.author)
        }

        val refreshed = wiseSayingRepository.findById(existing.id!!).orElseThrow()
        assertEquals("기존 내용", refreshed.content)
        assertEquals("기존 작가", refreshed.author)
    }

    @Test
    @DisplayName("A05: PATCH 시 여러 줄 입력 및 공백을 포함한 정상 문자열은 trim 없이 원문 그대로 수정된다")
    fun updateWithMultilineAndUntrimmedText() {
        val existing = wiseSayingRepository.save(
            WiseSaying(content = "기존 내용", author = "기존 작가")
        )

        val multilineContent = "  첫 번째 줄\n두 번째 줄  \u3000"
        val authorWithSpaces = "  작가 이름  "
        val requestBody = """
            {
                "content": "  첫 번째 줄\n두 번째 줄  \u3000",
                "author": "  작가 이름  "
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/api/v1/wisesaying/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(existing.id))
            .andExpect(jsonPath("$.content").value(multilineContent))
            .andExpect(jsonPath("$.author").value(authorWithSpaces))

        val updated = wiseSayingRepository.findById(existing.id!!).orElseThrow()
        assertEquals(multilineContent, updated.content)
        assertEquals(authorWithSpaces, updated.author)
    }

    @Test
    @DisplayName("A10: 생성 시 content 2000자, author 255자 경계는 성공하고 2001자, 256자 초과는 400 Bad Request를 반환한다")
    fun createLengthBoundaries() {
        val initialCount = wiseSayingRepository.count()

        val validContent = "A".repeat(2000)
        val validAuthor = "B".repeat(255)
        val validBody = """
            {
                "content": "$validContent",
                "author": "$validAuthor"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/wisesaying")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value(validContent))
            .andExpect(jsonPath("$.author").value(validAuthor))

        assertEquals(initialCount + 1, wiseSayingRepository.count())

        val overContentBody = """
            {
                "content": "${"A".repeat(2001)}",
                "author": "작가"
            }
        """.trimIndent()
        mockMvc.perform(
            post("/api/v1/wisesaying")
                .contentType(MediaType.APPLICATION_JSON)
                .content(overContentBody)
        )
            .andExpect(status().isBadRequest)

        val overAuthorBody = """
            {
                "content": "내용",
                "author": "${"B".repeat(256)}"
            }
        """.trimIndent()
        mockMvc.perform(
            post("/api/v1/wisesaying")
                .contentType(MediaType.APPLICATION_JSON)
                .content(overAuthorBody)
        )
            .andExpect(status().isBadRequest)

        assertEquals(initialCount + 1, wiseSayingRepository.count())
    }

    @Test
    @DisplayName("A10: 수정 시 content 2000자, author 255자 경계는 성공하고 2001자, 256자 초과는 400 Bad Request를 반환한다")
    fun updateLengthBoundaries() {
        val existing = wiseSayingRepository.save(
            WiseSaying(content = "기존 내용", author = "기존 작가")
        )

        val validContent = "C".repeat(2000)
        mockMvc.perform(
            patch("/api/v1/wisesaying/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content": "$validContent"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value(validContent))

        val validAuthor = "D".repeat(255)
        mockMvc.perform(
            patch("/api/v1/wisesaying/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"author": "$validAuthor"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.author").value(validAuthor))

        val overContentBody = """{"content": "${"E".repeat(2001)}"}"""
        mockMvc.perform(
            patch("/api/v1/wisesaying/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(overContentBody)
        )
            .andExpect(status().isBadRequest)

        val overAuthorBody = """{"author": "${"F".repeat(256)}"}"""
        mockMvc.perform(
            patch("/api/v1/wisesaying/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(overAuthorBody)
        )
            .andExpect(status().isBadRequest)

        val refreshed = wiseSayingRepository.findById(existing.id!!).orElseThrow()
        assertEquals(validContent, refreshed.content)
        assertEquals(validAuthor, refreshed.author)
    }
}
