package com.back.team11.domain.cafe.entity;

import com.back.team11.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder  // cafe-CRUD 추가: 필드명 기반 명시적 생성, 선택 필드 생략 가능
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // createdAt, updatedAt 자동 관리
@Table(name = "cafe")
public class Cafe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 7)
    private BigDecimal longitude;

    @Column(length = 20)
    private String phone;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CafeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Franchise franchise;

    @Column(nullable = false)
    private Boolean hasToilet;

    @Column(nullable = false)
    private Boolean hasOutlet;

    @Column(nullable = false)
    private Boolean hasWifi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FloorCount floorCount;

    @Column(nullable = false)
    private Boolean hasSeparateSpace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CongestionLevel congestionLevel;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CafeStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    // ─────────────────────────────────────────────
    // 정적 팩토리 메서드 - new 대신 static 사용 (필수 값 누락을 방지), 도메인 로직을 엔티티에 캡슐화
    // member 없이 생성 - 관리자가 직접 등록하기에 필요 X
    // ─────────────────────────────────────────────
    public static Cafe createByAdmin(
            String name,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String phone,
            String description,
            CafeType type,
            Franchise franchise,
            Boolean hasToilet,
            Boolean hasOutlet,
            Boolean hasWifi,
            FloorCount floorCount,
            Boolean hasSeparateSpace,
            CongestionLevel congestionLevel,
            String imageUrl
    ) {
        return Cafe.builder()
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .phone(phone)
                .description(description)
                .type(type)
                .franchise(franchise)
                .hasToilet(hasToilet)
                .hasOutlet(hasOutlet)
                .hasWifi(hasWifi)
                .floorCount(floorCount)
                .hasSeparateSpace(hasSeparateSpace)
                .congestionLevel(congestionLevel)
                .imageUrl(imageUrl)
                .status(CafeStatus.APPROVED) // status는 APPROVED 고정: 관리자 직접 등록 → 즉시 승인
                .build();
    } // @Builder 사용으로 생성자 대신 정적 팩토리 메서드로 명시적 생성, 필드명 기반, 선택 필드 생략 가능


    // ─────────────────────────────────────────────
    // PATCH 수정 메서드 - null인 필드는 기존값 유지
    // member, status, createdAt, updatedAt는 수정 대상 아님 (status = APPROVED 고정)
    // 엔티티 자신의 필드를 변경하는 메서드. 변경 결과를 반환할 필요 X
    // ─────────────────────────────────────────────
    public void updateByAdmin(
            String name,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String phone,
            String description,
            CafeType type,
            Franchise franchise,
            Boolean hasToilet,
            Boolean hasOutlet,
            Boolean hasWifi,
            FloorCount floorCount,
            Boolean hasSeparateSpace,
            CongestionLevel congestionLevel,
            String imageUrl
    ) {
        if (name != null)              this.name = name;
        if (address != null)           this.address = address;
        if (latitude != null)          this.latitude = latitude;
        if (longitude != null)         this.longitude = longitude;
        if (phone != null)             this.phone = phone;
        if (description != null)       this.description = description;
        if (type != null)              this.type = type;
        if (franchise != null)         this.franchise = franchise;
        if (hasToilet != null)         this.hasToilet = hasToilet;
        if (hasOutlet != null)         this.hasOutlet = hasOutlet;
        if (hasWifi != null)           this.hasWifi = hasWifi;
        if (floorCount != null)        this.floorCount = floorCount;
        if (hasSeparateSpace != null)  this.hasSeparateSpace = hasSeparateSpace;
        if (congestionLevel != null)   this.congestionLevel = congestionLevel;
        if (imageUrl != null)          this.imageUrl = imageUrl;
    }


    // ─────────────────────────────────────────────
    // 정적 팩토리 메서드 - 사용자 제보
    // member 연결, status는 PENDING 고정 (관리자 심사 대기)
    // ─────────────────────────────────────────────
    public static Cafe createByUser(
            Member member,
            String name, String address,
            BigDecimal latitude, BigDecimal longitude,
            String phone, String description,
            CafeType type, Franchise franchise,
            Boolean hasToilet, Boolean hasOutlet, Boolean hasWifi,
            FloorCount floorCount, Boolean hasSeparateSpace,
            CongestionLevel congestionLevel, String imageUrl
    ) {
        return Cafe.builder()
                .member(member)
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .phone(phone)
                .description(description)
                .type(type)
                .franchise(franchise)
                .hasToilet(hasToilet)
                .hasOutlet(hasOutlet)
                .hasWifi(hasWifi)
                .floorCount(floorCount)
                .hasSeparateSpace(hasSeparateSpace)
                .congestionLevel(congestionLevel)
                .imageUrl(imageUrl)
                .status(CafeStatus.PENDING) // 사용자 제보 → 관리자 심사 대기
                .build();
    }


    // 승인 처리
    public void approve() {
        this.status = CafeStatus.APPROVED;
    }

    // 거절 처리
    public void reject() {
        this.status = CafeStatus.REJECTED;
    }

}
