package com.back.team11.domain.cafe.entity;

import java.util.Arrays;

public enum Franchise {
    STARBUCKS("스타벅스"),
    MEGA_COFFEE("메가커피"),
    EDIYA("이디야"),
    COMPOSE("컴포즈"),
    TWOSOME("투썸플레이스"),
    PAIK_DABANG("빽다방"),
    THE_VENTI("더벤티"),
    NONE(null);

    private final String keyword;

    Franchise(String keyword) {
        this.keyword = keyword;
    }

    public static Franchise from(String cafeName) {
        return Arrays.stream(values())
                .filter(f -> f.keyword != null && cafeName.contains(f.keyword))
                .findFirst()
                .orElse(NONE);
    }
}

