package com.programmers.be14.practice_261008.wisesaying.repository

import com.programmers.be14.practice_261008.wisesaying.entity.WiseSaying
import org.springframework.data.jpa.repository.JpaRepository

interface WiseSayingRepository: JpaRepository<WiseSaying, Long>