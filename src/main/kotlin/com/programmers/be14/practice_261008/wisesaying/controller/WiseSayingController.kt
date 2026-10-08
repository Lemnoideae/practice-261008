package com.programmers.be14.practice_261008.wisesaying.controller

import com.programmers.be14.practice_261008.wisesaying.dto.create.WiseSayingCreateRequest
import com.programmers.be14.practice_261008.wisesaying.dto.create.WiseSayingCreateResponse
import com.programmers.be14.practice_261008.wisesaying.dto.get.WiseSayingGetResponse
import com.programmers.be14.practice_261008.wisesaying.dto.update.WiseSayingUpdateRequest
import com.programmers.be14.practice_261008.wisesaying.dto.update.WiseSayingUpdateResponse
import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying
import com.programmers.be14.practice_261008.wisesaying.service.WiseSayingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/wisesaying")
class WiseSayingController(
    private val service: WiseSayingService
) {
    
    @GetMapping
    fun getAll(): ResponseEntity<List<WiseSayingGetResponse>> {
        val list = service.findAll().map { 
            WiseSayingGetResponse.from(it) 
            }
        return ResponseEntity.ok(list)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable(value = "id") id: Long): 
        ResponseEntity<WiseSayingGetResponse> {

        val founded = service.findWiseSayingById(id)
        return ResponseEntity.ok(
            WiseSayingGetResponse.from(founded))
    }
    
    @PostMapping
    fun create(request: WiseSayingCreateRequest): 
        ResponseEntity<WiseSayingCreateResponse> {

        val created = service.create(request)
        return ResponseEntity.ok(
            WiseSayingCreateResponse.from(created)
        )
    }
    
    @PatchMapping("/{id}")
    fun update(@PathVariable(value = "id") id: Long, request: WiseSayingUpdateRequest):
        ResponseEntity<WiseSayingUpdateResponse> {

        val updated = service.update(id, request)
        return ResponseEntity.ok(
            WiseSayingUpdateResponse.from(updated)
        )
    }
    
    @DeleteMapping("/{id}")
    fun delete(@PathVariable(value = "id") id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}