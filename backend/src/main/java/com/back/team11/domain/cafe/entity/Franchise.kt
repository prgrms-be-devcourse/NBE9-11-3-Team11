package com.back.team11.domain.cafe.entity


enum class Franchise(val keyword: String?) {
    STARBUCKS("스타벅스"),
    MEGA_COFFEE("메가커피"),
    EDIYA("이디야"),
    COMPOSE("컴포즈"),
    TWOSOME("투썸플레이스"),
    PAIK_DABANG("빽다방"),
    THE_VENTI("더벤티"),
    NONE(null);

    companion object {
        fun from(cafeName: String): Franchise =
            entries.firstOrNull{ it.keyword != null && cafeName.contains(it.keyword) }
                ?: NONE
    }
}

