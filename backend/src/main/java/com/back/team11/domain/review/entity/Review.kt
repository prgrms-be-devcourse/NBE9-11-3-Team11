package com.back.team11.domain.review.entity

import com.back.team11.domain.cafe.entity.Cafe
import com.back.team11.domain.member.entity.Member
import jakarta.persistence.*
import lombok.Getter
import lombok.NoArgsConstructor
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "review",
    uniqueConstraints = [UniqueConstraint(columnNames = ["member_id", "cafe_id"])]
)
@EntityListeners(AuditingEntityListener::class)
class Review(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    val cafe: Cafe,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    ) {
    fun update(content: String) {
        this.content = content
    }
}
