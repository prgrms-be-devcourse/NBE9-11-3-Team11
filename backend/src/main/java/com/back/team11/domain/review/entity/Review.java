package com.back.team11.domain.review.entity;

import com.back.team11.domain.cafe.entity.Cafe;
import com.back.team11.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "review",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "cafe_id"})
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    public Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    public Cafe cafe;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    public LocalDateTime updatedAt;

    // 리뷰 수정
    public void update(String content) {
        this.content = content;
    }

    public Review(Member member, Cafe cafe, String content) {
        this.member = member;
        this.cafe = cafe;
        this.content = content;
    }
}
