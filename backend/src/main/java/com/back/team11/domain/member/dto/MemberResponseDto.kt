package com.back.team11.domain.member.dto

import com.back.team11.domain.member.entity.Member

data class MemberResponseDto(
    val memberId: Long,
    val nickname: String,
    val email: String,
    val role: String
) {
    companion object {
        fun from(member: Member): MemberResponseDto {
            return MemberResponseDto(
                member.id,
                member.nickname,
                member.email,
                member.role.name
            )
        }
    }
}
