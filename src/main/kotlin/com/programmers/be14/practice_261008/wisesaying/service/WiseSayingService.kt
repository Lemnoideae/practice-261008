package com.programmers.be14.practice_261008.wisesaying.service

import com.programmers.be14.practice_261008.wisesaying.dto.create.WiseSayingCreateRequest
import com.programmers.be14.practice_261008.wisesaying.dto.update.WiseSayingUpdateRequest
import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying
import com.programmers.be14.practice_261008.wisesaying.repository.WiseSayingRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WiseSayingService(
	private val repository: WiseSayingRepository
) {
	
	@Transactional
	fun create(request: WiseSayingCreateRequest): WiseSaying {

		val newWiseSaying = request.toEntity()
		return repository.save(newWiseSaying)
	}
	
	@Transactional(readOnly = true)
	fun findAll(): List<WiseSaying> {

		return repository.findAll()
	}
	
	@Transactional(readOnly = true)
	fun findWiseSayingById(id: Long): WiseSaying {

		return repository.findById(id).orElseThrow { 
			EntityNotFoundException("해당 ID의 명언은 존재하지 않습니다.") 
		}
	}
	
	@Transactional
	fun update(id: Long, request: WiseSayingUpdateRequest): WiseSaying {

		val founded = repository.findById(id).orElseThrow {
			EntityNotFoundException("해당 ID의 명언은 존재하지 않습니다.")
		}
		founded.update(request.content, request.author)
		return repository.save(founded)
	}
	
	@Transactional
	fun delete(id: Long) {
		repository.deleteById(id)
	}
}