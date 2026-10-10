package com.programmers.be14.practice_261008.wisesaying

import com.jayway.jsonpath.JsonPath
import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying
import com.programmers.be14.practice_261008.wisesaying.repository.WiseSayingRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.auditing.AuditingHandler
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Optional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WiseSayingApiIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val wiseSayingRepository: WiseSayingRepository,
    private val entityManager: EntityManager,
    private val auditingHandler: AuditingHandler
) {

    @AfterEach
    fun tearDown() {
        auditingHandler.setDateTimeProvider(null)
    }

    @Test
    @DisplayName("A02~A06, A15: 실제 H2 기반 전체 CRUD 흐름 검증 및 flush/clear 후 DB 재조회 대조 (POST -> GET -> PATCH -> DELETE -> 404)")
    fun fullCrudFlowAndDatabaseDatePersistence() {
        val createTime = LocalDateTime.of(2026, 10, 10, 10, 0, 0)
        auditingHandler.setDateTimeProvider(DateTimeProvider { Optional.of(createTime) })

        // 1. 등록 (POST)
        val postRequest = """
            {
                "content": "시작이 반이다.",
                "author": "아리스토텔레스"
            }
        """.trimIndent()

        val postResult = mockMvc.perform(
            post("/api/v1/wisesaying")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.content").value("시작이 반이다."))
            .andExpect(jsonPath("$.author").value("아리스토텔레스"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty)
            .andReturn()

        val postJson = postResult.response.contentAsString
        val createdId = JsonPath.read<Number>(postJson, "$.id").toLong()
        val postCreatedAtStr = JsonPath.read<String>(postJson, "$.createdAt")
        val postCreatedAt = LocalDateTime.parse(postCreatedAtStr)

        // 영속성 컨텍스트 비우기 (메모리 1차 캐시 배제)
        entityManager.flush()
        entityManager.clear()

        // DB에서 직접 재조회하여 응답 및 Auditing 시각과 대조 (A02)
        val dbAfterCreate = wiseSayingRepository.findById(createdId).orElseThrow()
        assertEquals(createdId, dbAfterCreate.id)
        assertEquals("시작이 반이다.", dbAfterCreate.content)
        assertEquals("아리스토텔레스", dbAfterCreate.author)
        assertEquals(
            createTime.truncatedTo(ChronoUnit.SECONDS),
            dbAfterCreate.createdAt?.truncatedTo(ChronoUnit.SECONDS)
        )
        assertEquals(
            postCreatedAt.truncatedTo(ChronoUnit.SECONDS),
            dbAfterCreate.createdAt?.truncatedTo(ChronoUnit.SECONDS)
        )

        // 2. 단건 조회 (GET /{id})
        mockMvc.perform(get("/api/v1/wisesaying/$createdId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(createdId))
            .andExpect(jsonPath("$.content").value("시작이 반이다."))
            .andExpect(jsonPath("$.author").value("아리스토텔레스"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty)
            .andExpect(jsonPath("$.modifiedAt").isNotEmpty)

        // 3. 목록 조회 (GET) - 방금 등록한 ID 포함 여부 확인
        val listResult = mockMvc.perform(get("/api/v1/wisesaying"))
            .andExpect(status().isOk)
            .andReturn()

        val listJson = listResult.response.contentAsString
        val listIds = JsonPath.read<List<Number>>(listJson, "$[*].id").map { it.toLong() }
        val listContents = JsonPath.read<List<String>>(listJson, "$[*].content")
        assertTrue(listIds.contains(createdId))
        assertTrue(listContents.contains("시작이 반이다."))

        // 4. 부분 수정 (PATCH /{id}) - 수정 시각 변경 및 생성 시각 유지 확인 (A03, A05)
        val updateTime = LocalDateTime.of(2026, 10, 10, 11, 30, 0)
        auditingHandler.setDateTimeProvider(DateTimeProvider { Optional.of(updateTime) })

        val patchRequest = """
            {
                "content": "수정된 내용: 시작이 전부다."
            }
        """.trimIndent()

        val patchResult = mockMvc.perform(
            patch("/api/v1/wisesaying/$createdId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(createdId))
            .andExpect(jsonPath("$.content").value("수정된 내용: 시작이 전부다."))
            .andExpect(jsonPath("$.author").value("아리스토텔레스"))
            .andExpect(jsonPath("$.modifiedAt").isNotEmpty)
            .andReturn()

        val patchJson = patchResult.response.contentAsString
        val patchModifiedAtStr = JsonPath.read<String>(patchJson, "$.modifiedAt")
        val patchModifiedAt = LocalDateTime.parse(patchModifiedAtStr)

        // 영속성 컨텍스트 비우기
        entityManager.flush()
        entityManager.clear()

        // DB에서 직접 재조회하여 응답과 대조 및 날짜 영속성 확인
        val dbAfterPatch = wiseSayingRepository.findById(createdId).orElseThrow()
        assertEquals("수정된 내용: 시작이 전부다.", dbAfterPatch.content)
        assertEquals("아리스토텔레스", dbAfterPatch.author)
        // 생성 시각 유지 확인
        assertEquals(
            createTime.truncatedTo(ChronoUnit.SECONDS),
            dbAfterPatch.createdAt?.truncatedTo(ChronoUnit.SECONDS)
        )
        // 수정 시각 갱신 확인 및 응답과 대조
        assertEquals(
            updateTime.truncatedTo(ChronoUnit.SECONDS),
            dbAfterPatch.modifiedAt?.truncatedTo(ChronoUnit.SECONDS)
        )
        assertEquals(
            patchModifiedAt.truncatedTo(ChronoUnit.SECONDS),
            dbAfterPatch.modifiedAt?.truncatedTo(ChronoUnit.SECONDS)
        )

        // 5. 삭제 (DELETE /{id}) -> 204 No Content
        mockMvc.perform(delete("/api/v1/wisesaying/$createdId"))
            .andExpect(status().isNoContent)

        // 영속성 컨텍스트 비우기
        entityManager.flush()
        entityManager.clear()

        // DB에서 실제로 삭제되었는지 확인
        assertFalse(wiseSayingRepository.findById(createdId).isPresent)

        // 6. 삭제 후 단건 조회 -> 404 Not Found
        mockMvc.perform(get("/api/v1/wisesaying/$createdId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
    }

    @Test
    @DisplayName("A08, A10: 유효하지 않은 생성 요청 시 400 Bad Request를 반환하고 실제 DB 레코드 수는 증가하지 않는다")
    fun invalidCreateRequestsDoNotPersistToDatabase() {
        val initialCount = wiseSayingRepository.count()

        val invalidBodies = listOf(
            // 빈 문자열 및 공백
            """{"content": "", "author": "작가"}""",
            """{"content": "   ", "author": "작가"}""",
            """{"content": "\u3000", "author": "작가"}""",
            """{"content": "내용", "author": ""}""",
            """{"content": "내용", "author": "   "}""",
            """{"content": "내용", "author": "\u2003"}""",
            // 누락 및 null
            """{"content": "내용만 있음"}""",
            """{"author": "작가만 있음"}""",
            """{"content": null, "author": "작가"}""",
            """{"content": "내용", "author": null}""",
            """{}""",
            // 길이 초과 (content 2001자, author 256자)
            """{"content": "${"a".repeat(2001)}", "author": "작가"}""",
            """{"content": "내용", "author": "${"b".repeat(256)}"}"""
        )

        for (body in invalidBodies) {
            mockMvc.perform(
                post("/api/v1/wisesaying")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
        }

        entityManager.flush()
        entityManager.clear()

        assertEquals(initialCount, wiseSayingRepository.count(), "무효 요청 후 DB 레코드 수가 변하지 않아야 합니다.")
    }

    @Test
    @DisplayName("A09, A10: 유효하지 않은 수정 요청 시 400 Bad Request를 반환하고 실제 DB의 기존 데이터는 변경되지 않는다")
    fun invalidUpdateRequestsDoNotModifyExistingDatabaseRecord() {
        val original = wiseSayingRepository.save(
            WiseSaying(content = "원래 내용", author = "원래 작가")
        )
        val originalId = original.id!!
        entityManager.flush()
        entityManager.clear()

        val invalidUpdateBodies = listOf(
            """{"content": ""}""",
            """{"content": "   "}""",
            """{"content": "\u3000"}""",
            """{"author": ""}""",
            """{"author": "   "}""",
            """{"author": "\u2003"}""",
            """{"content": "${"a".repeat(2001)}"}""",
            """{"author": "${"b".repeat(256)}"}"""
        )

        for (body in invalidUpdateBodies) {
            mockMvc.perform(
                patch("/api/v1/wisesaying/$originalId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
        }

        entityManager.flush()
        entityManager.clear()

        val unchanged = wiseSayingRepository.findById(originalId).orElseThrow()
        assertEquals("원래 내용", unchanged.content)
        assertEquals("원래 작가", unchanged.author)
    }

    @Test
    @DisplayName("A07: 빈 객체({}) 및 명시적 null 필드 PATCH 시 200 OK이며 실제 DB 데이터가 유지된다")
    fun emptyAndNullPatchKeepExistingValuesInDatabase() {
        val original = wiseSayingRepository.save(
            WiseSaying(content = "보존할 내용", author = "보존할 작가")
        )
        val originalId = original.id!!
        entityManager.flush()
        entityManager.clear()

        // 1. 빈 객체 PATCH
        mockMvc.perform(
            patch("/api/v1/wisesaying/$originalId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("보존할 내용"))
            .andExpect(jsonPath("$.author").value("보존할 작가"))

        entityManager.flush()
        entityManager.clear()

        var dbEntity = wiseSayingRepository.findById(originalId).orElseThrow()
        assertEquals("보존할 내용", dbEntity.content)
        assertEquals("보존할 작가", dbEntity.author)

        // 2. 명시적 null 필드 PATCH
        mockMvc.perform(
            patch("/api/v1/wisesaying/$originalId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content": null, "author": null}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("보존할 내용"))
            .andExpect(jsonPath("$.author").value("보존할 작가"))

        entityManager.flush()
        entityManager.clear()

        dbEntity = wiseSayingRepository.findById(originalId).orElseThrow()
        assertEquals("보존할 내용", dbEntity.content)
        assertEquals("보존할 작가", dbEntity.author)

        // 3. content-only PATCH
        mockMvc.perform(
            patch("/api/v1/wisesaying/$originalId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content": "새로운 내용만"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("새로운 내용만"))
            .andExpect(jsonPath("$.author").value("보존할 작가"))

        entityManager.flush()
        entityManager.clear()

        dbEntity = wiseSayingRepository.findById(originalId).orElseThrow()
        assertEquals("새로운 내용만", dbEntity.content)
        assertEquals("보존할 작가", dbEntity.author)
    }

    @Test
    @DisplayName("A12, A16: 존재하지 않는 ID에 대한 GET/PATCH 404 및 DELETE 204 계약 검증")
    fun nonExistentIdContractVerification() {
        val nonExistentId = 999999L

        // 없는 ID GET -> 404 Not Found
        mockMvc.perform(get("/api/v1/wisesaying/$nonExistentId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))

        // 없는 ID PATCH -> 404 Not Found
        mockMvc.perform(
            patch("/api/v1/wisesaying/$nonExistentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content": "새 내용"}""")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))

        // 없는 ID DELETE -> 204 No Content (기존 계약 유지)
        mockMvc.perform(delete("/api/v1/wisesaying/$nonExistentId"))
            .andExpect(status().isNoContent)
    }
}
