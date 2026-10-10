package com.programmers.be14.practice_261008.wisesaying.controller

import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying
import com.programmers.be14.practice_261008.wisesaying.repository.WiseSayingRepository
import com.programmers.be14.practice_261008.wisesaying.service.WiseSayingService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@ExtendWith(OutputCaptureExtension::class)
class WiseSayingControllerTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val wiseSayingRepository: WiseSayingRepository
) {

    @MockitoSpyBean
    private lateinit var wiseSayingService: WiseSayingService

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

    @Test
    @DisplayName("A11: 비숫자 ID 경로 변수로 요청 시 400 Bad Request와 공통 오류 구조를 반환한다")
    fun requestWithNonNumericIdReturnsBadRequest() {
        mockMvc.perform(get("/api/v1/wisesaying/not-a-number"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("요청 파라미터 또는 경로 변수의 타입이 올바르지 않습니다."))

        mockMvc.perform(
            patch("/api/v1/wisesaying/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content": "내용"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))

        mockMvc.perform(delete("/api/v1/wisesaying/invalid-id"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
    }

    @Test
    @DisplayName("A11: 문법이 잘못된 malformed JSON 요청 시 400 Bad Request를 반환하고 내부 예외를 노출하지 않는다")
    fun requestWithMalformedJsonReturnsBadRequest() {
        val malformedBodies = listOf(
            """{"content": "내용", "author": }""",
            """{"content": "닫히지 않은 문자열""",
            """{not-a-json}"""
        )

        for (body in malformedBodies) {
            mockMvc.perform(
                post("/api/v1/wisesaying")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("요청 본문 형식이 올바르지 않거나 필수 필드가 누락되었습니다."))
        }
    }

    @Test
    @DisplayName("A08: 생성 시 필수 필드가 누락되거나 null인 경우 400 Bad Request를 반환하고 레코드가 생성되지 않는다")
    fun createWithMissingOrNullRequiredFields() {
        val initialCount = wiseSayingRepository.count()

        val invalidBodies = listOf(
            """{"content": "내용만 있고 작가 누락"}""",
            """{"author": "작가만 있고 내용 누락"}""",
            """{"content": "내용", "author": null}""",
            """{"content": null, "author": "작가"}""",
            """{"content": null, "author": null}""",
            """{}"""
        )

        for (body in invalidBodies) {
            mockMvc.perform(
                post("/api/v1/wisesaying")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
        }

        assertEquals(initialCount, wiseSayingRepository.count())
    }

    @Test
    @DisplayName("A07, A12: 존재하지 않는 ID에 대한 GET, PATCH 및 빈 PATCH 요청 시 404 Not Found와 공통 오류 구조를 반환한다")
    fun requestWithNonExistentIdReturnsNotFound() {
        val nonExistentId = 999999L

        mockMvc.perform(get("/api/v1/wisesaying/$nonExistentId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.timestamp").isNotEmpty)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("해당 ID의 명언은 존재하지 않습니다."))

        mockMvc.perform(
            patch("/api/v1/wisesaying/$nonExistentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content": "새 내용", "author": "새 작가"}""")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("해당 ID의 명언은 존재하지 않습니다."))

        mockMvc.perform(
            patch("/api/v1/wisesaying/$nonExistentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("해당 ID의 명언은 존재하지 않습니다."))

        mockMvc.perform(
            patch("/api/v1/wisesaying/$nonExistentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content": null, "author": null}""")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("해당 ID의 명언은 존재하지 않습니다."))
    }

    @Test
    @DisplayName("U3-R1: 매핑되지 않은 URL(오타 경로, 누락된 하위 경로) 요청 시 404 Not Found와 공통 오류 구조를 반환하고 ERROR 로그를 남기지 않는다")
    fun unmappedUrlsReturnNotFoundWithoutErrorLog(output: CapturedOutput) {
        val unmappedPaths = listOf(
            "/api/v1/wisesayings",
            "/api/v1/wisesaying/1/missing"
        )
        for (path in unmappedPaths) {
            mockMvc.perform(get(path))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.timestamp").isNotEmpty)
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("요청한 리소스를 찾을 수 없습니다."))
        }

        assertFalse(
            output.all.contains("서버 내부 오류 발생"),
            "매핑되지 않은 URL 요청은 서버 내부 오류(ERROR)로 기록되지 않아야 합니다."
        )
    }

    @Test
    @DisplayName("A13: 지원하지 않는 HTTP 메서드(PUT) 요청 시 405 Method Not Allowed와 Allow 헤더를 반환한다")
    fun unsupportedMethodReturnsMethodNotAllowedWithAllowHeader() {
        mockMvc.perform(
            put("/api/v1/wisesaying/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content": "내용", "author": "작가"}""")
        )
            .andExpect(status().isMethodNotAllowed)
            .andExpect(header().exists("Allow"))
            .andExpect(jsonPath("$.status").value(405))
            .andExpect(jsonPath("$.error").value("Method Not Allowed"))
            .andExpect(jsonPath("$.message").value("지원하지 않는 HTTP 메서드입니다."))

        mockMvc.perform(put("/api/v1/wisesaying"))
            .andExpect(status().isMethodNotAllowed)
            .andExpect(header().exists("Allow"))
            .andExpect(jsonPath("$.status").value(405))
            .andExpect(jsonPath("$.error").value("Method Not Allowed"))
            .andExpect(jsonPath("$.message").value("지원하지 않는 HTTP 메서드입니다."))
    }

    @Test
    @DisplayName("A13: 지원하지 않는 Content-Type(text/plain) 요청 시 415 Unsupported Media Type을 반환한다")
    fun unsupportedContentTypeReturnsUnsupportedMediaType() {
        mockMvc.perform(
            post("/api/v1/wisesaying")
                .contentType(MediaType.TEXT_PLAIN)
                .content("content=hello&author=world")
        )
            .andExpect(status().isUnsupportedMediaType)
            .andExpect(jsonPath("$.status").value(415))
            .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
            .andExpect(jsonPath("$.message").value("지원하지 않는 미디어 타입입니다."))
    }

    @Test
    @DisplayName("A14: 예상치 못한 서버 내부 예외 발생 시 500과 일반 메시지를 반환하고 내부 예외 정보는 노출하지 않는다")
    fun unexpectedExceptionReturnsInternalServerErrorWithSafeMessage() {
        val secretErrorMessage = "치명적 DB 연결 실패: SECRET_DB_PASSWORD_1234"
        Mockito.doThrow(RuntimeException(secretErrorMessage))
            .`when`(wiseSayingService).findAll()

        try {
            val result = mockMvc.perform(get("/api/v1/wisesaying"))
                .andExpect(status().isInternalServerError)
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andReturn()

            val responseBody = result.response.contentAsString
            assertFalse(
                responseBody.contains(secretErrorMessage),
                "내부 예외 메시지가 클라이언트에 노출되어서는 안 됩니다."
            )
            assertFalse(
                responseBody.contains("RuntimeException"),
                "예외 클래스 이름이 클라이언트에 노출되어서는 안 됩니다."
            )
        } finally {
            Mockito.reset(wiseSayingService)
        }
    }
}
