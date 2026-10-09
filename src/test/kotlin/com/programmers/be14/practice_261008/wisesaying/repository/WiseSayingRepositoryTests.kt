package com.programmers.be14.practice_261008.wisesaying.repository

import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.auditing.AuditingHandler
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Optional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WiseSayingRepositoryTests @Autowired constructor(
    private val wiseSayingRepository: WiseSayingRepository,
    private val entityManager: EntityManager,
    private val auditingHandler: AuditingHandler
) {

    @AfterEach
    fun tearDown() {
        auditingHandler.setDateTimeProvider(null)
    }

    @Test
    @DisplayName("A02: 실제 DB에 저장 후 flush 및 clear를 거쳐 재조회했을 때 ID와 content, author, createdAt, modifiedAt이 영속화된다")
    fun saveAndFindWithAuditingDates() {
        val fixedTime = LocalDateTime.of(2026, 10, 9, 1, 0, 0)
        auditingHandler.setDateTimeProvider(DateTimeProvider { Optional.of(fixedTime) })

        val entity = WiseSaying(
            content = "꿈을 지녀라. 그러면 어려운 현실을 이길 수 있다.",
            author = "월트 디즈니"
        )
        val saved = wiseSayingRepository.save(entity)
        entityManager.flush()
        entityManager.clear()

        val found = wiseSayingRepository.findById(saved.id!!).orElse(null)
        assertNotNull(found)
        assertEquals(saved.id, found.id)
        assertEquals("꿈을 지녀라. 그러면 어려운 현실을 이길 수 있다.", found.content)
        assertEquals("월트 디즈니", found.author)
        assertNotNull(found.createdAt)
        assertNotNull(found.modifiedAt)
        assertEquals(fixedTime, found.createdAt?.truncatedTo(ChronoUnit.SECONDS))
        assertEquals(fixedTime, found.modifiedAt?.truncatedTo(ChronoUnit.SECONDS))
    }

    @Test
    @DisplayName("A03: 실제 내용 수정 후 flush 및 clear 시 createdAt은 유지되고 modifiedAt은 갱신된다")
    fun updateWithAuditingDates() {
        val createTime = LocalDateTime.of(2026, 10, 9, 1, 0, 0)
        auditingHandler.setDateTimeProvider(DateTimeProvider { Optional.of(createTime) })

        val entity = WiseSaying(
            content = "과거의 내용",
            author = "과거의 작가"
        )
        val saved = wiseSayingRepository.save(entity)
        entityManager.flush()
        entityManager.clear()

        val found = wiseSayingRepository.findById(saved.id!!).orElseThrow()
        assertEquals(createTime, found.createdAt?.truncatedTo(ChronoUnit.SECONDS))
        assertEquals(createTime, found.modifiedAt?.truncatedTo(ChronoUnit.SECONDS))

        val updateTime = LocalDateTime.of(2026, 10, 9, 2, 0, 0)
        auditingHandler.setDateTimeProvider(DateTimeProvider { Optional.of(updateTime) })

        found.update(content = "수정된 내용", author = "수정된 작가")
        entityManager.flush()
        entityManager.clear()

        val updated = wiseSayingRepository.findById(saved.id!!).orElseThrow()
        assertEquals("수정된 내용", updated.content)
        assertEquals("수정된 작가", updated.author)
        assertEquals(createTime, updated.createdAt?.truncatedTo(ChronoUnit.SECONDS))
        assertEquals(updateTime, updated.modifiedAt?.truncatedTo(ChronoUnit.SECONDS))
        assertTrue(updated.modifiedAt!!.isAfter(updated.createdAt!!))
    }
}
