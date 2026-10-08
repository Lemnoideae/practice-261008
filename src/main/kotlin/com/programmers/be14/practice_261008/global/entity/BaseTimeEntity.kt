package com.programmers.be14.practice_261008.global.entity

import jakarta.persistence.EntityListeners
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {
    
    @CreatedDate
    var createdAt: LocalDateTime? = null
        protected set
    
    @LastModifiedDate
    var modifiedAt: LocalDateTime? = null
        protected set
        
} 