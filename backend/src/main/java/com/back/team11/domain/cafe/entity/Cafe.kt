package com.back.team11.domain.cafe.entity

import com.back.team11.domain.member.entity.Member
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class) // createdAt, updatedAt 자동 관리
@Table(name = "cafe")
class Cafe(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false)
    var address: String,

    @Column(nullable = false, precision = 10, scale = 7)
    var latitude: BigDecimal,

    @Column(nullable = false, precision = 11, scale = 7)
    var longitude: BigDecimal,

    @Column(length = 20)
    var phone: String? = null,

    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: CafeType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var franchise: Franchise,

    @Column(nullable = false)
    var hasToilet: Boolean,

    @Column(nullable = false)
    var hasOutlet: Boolean,

    @Column(nullable = false)
    var hasWifi: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var floorCount: FloorCount,

    @Column(nullable = false)
    var hasSeparateSpace: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var congestionLevel: CongestionLevel,

    @Column(length = 500)
    var imageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: CafeStatus,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null,
) {
    companion object {
        // ─────────────────────────────────────────────
        // 정적 팩토리 메서드 - 관리자 직접 등록
        // member 없이 생성, status는 APPROVED 고정
        // ─────────────────────────────────────────────
        fun createByAdmin(
            name: String,
            address: String,
            latitude: BigDecimal,
            longitude: BigDecimal,
            phone: String?,
            description: String?,
            type: CafeType,
            franchise: Franchise,
            hasToilet: Boolean,
            hasOutlet: Boolean,
            hasWifi: Boolean,
            floorCount: FloorCount,
            hasSeparateSpace: Boolean,
            congestionLevel: CongestionLevel,
            imageUrl: String?,
        ): Cafe = Cafe(
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            phone = phone,
            description = description,
            type = type,
            franchise = franchise,
            hasToilet = hasToilet,
            hasOutlet = hasOutlet,
            hasWifi = hasWifi,
            floorCount = floorCount,
            hasSeparateSpace = hasSeparateSpace,
            congestionLevel = congestionLevel,
            imageUrl = imageUrl,
            status = CafeStatus.APPROVED, // 관리자 직접 등록 → 즉시 승인
        )

        // ─────────────────────────────────────────────
        // 정적 팩토리 메서드 - 사용자 제보
        // member 연결, status는 PENDING 고정 (관리자 심사 대기)
        // ─────────────────────────────────────────────
        fun createByUser(
            member: Member,
            name: String,
            address: String,
            latitude: BigDecimal,
            longitude: BigDecimal,
            phone: String?,
            description: String?,
            type: CafeType,
            franchise: Franchise,
            hasToilet: Boolean,
            hasOutlet: Boolean,
            hasWifi: Boolean,
            floorCount: FloorCount,
            hasSeparateSpace: Boolean,
            congestionLevel: CongestionLevel,
            imageUrl: String?,
        ): Cafe = Cafe(
            member = member,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            phone = phone,
            description = description,
            type = type,
            franchise = franchise,
            hasToilet = hasToilet,
            hasOutlet = hasOutlet,
            hasWifi = hasWifi,
            floorCount = floorCount,
            hasSeparateSpace = hasSeparateSpace,
            congestionLevel = congestionLevel,
            imageUrl = imageUrl,
            status = CafeStatus.PENDING, // 사용자 제보 → 관리자 심사 대기
        )
    }

    // ─────────────────────────────────────────────
    // PATCH 수정 메서드 - null인 필드는 기존값 유지
    // member, status, createdAt, updatedAt는 수정 대상 아님
    // ─────────────────────────────────────────────
    fun updateByAdmin(
        name: String?,
        address: String?,
        latitude: BigDecimal?,
        longitude: BigDecimal?,
        phone: String?,
        description: String?,
        type: CafeType?,
        franchise: Franchise?,
        hasToilet: Boolean?,
        hasOutlet: Boolean?,
        hasWifi: Boolean?,
        floorCount: FloorCount?,
        hasSeparateSpace: Boolean?,
        congestionLevel: CongestionLevel?,
        imageUrl: String?,
    ) {
        name?.let { this.name = it }
        address?.let { this.address = it }
        latitude?.let { this.latitude = it }
        longitude?.let { this.longitude = it }
        phone?.let { this.phone = it }
        description?.let { this.description = it }
        type?.let { this.type = it }
        franchise?.let { this.franchise = it }
        hasToilet?.let { this.hasToilet = it }
        hasOutlet?.let { this.hasOutlet = it }
        hasWifi?.let { this.hasWifi = it }
        floorCount?.let { this.floorCount = it }
        hasSeparateSpace?.let { this.hasSeparateSpace = it }
        congestionLevel?.let { this.congestionLevel = it }
        imageUrl?.let { this.imageUrl = it }
    }

    // 승인 처리
    fun approve() { status = CafeStatus.APPROVED }

    // 거절 처리
    fun reject() { status = CafeStatus.REJECTED }
}