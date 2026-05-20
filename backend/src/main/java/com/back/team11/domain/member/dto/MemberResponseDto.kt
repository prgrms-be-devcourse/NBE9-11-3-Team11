package com.back.team11.domain.member.dto

import com.back.team11.domain.member.entity.Member

data class MemberResponseDto(
    val memberId: Long,
    val nickname: String,
    val email: String,
    val role: String
) {
    companion object {
        fun from(member: Member): MemberResponseDto = MemberResponseDto(
            memberId = member.id,
            nickname = member.nickname,
            email = member.email,
            role = member.role.name
        )
    }
}
