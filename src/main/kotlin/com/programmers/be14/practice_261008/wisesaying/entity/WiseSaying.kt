package com.programmers.be14.practice_261008.wisesaying.entity

import com.programmers.be14.practice_261008.global.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy

@Entity
@Table(name = "wisesayings")
class WiseSaying(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 2000)
    var content: String,
    
    @Column(nullable = false, length = 255)
    var author: String
    
): BaseTimeEntity() {

    fun update(content: String?, author: String?) {
        content?.let { this.content = it }
        author?.let { this.author = it }
    }

}
