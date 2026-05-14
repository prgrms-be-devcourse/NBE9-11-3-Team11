package com.back.team11.domain.wishlist.dto;

import com.back.team11.domain.wishlist.entity.Wishlist;

import java.time.LocalDateTime;

public record WishlistResponse(
        Long wishlistId,
        Long cafeId,
        String cafeName,
        LocalDateTime createAt
) {
    public static WishlistResponse from(Wishlist wishlist){
        return new WishlistResponse(
                wishlist.getId(),
                wishlist.getCafe().getId(),
                wishlist.getCafe().getName(),
                wishlist.getCreatedAt()
        );
    }
}
