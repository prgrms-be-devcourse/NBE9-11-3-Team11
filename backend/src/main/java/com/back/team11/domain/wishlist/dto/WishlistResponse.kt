package com.back.team11.domain.wishlist.dto

import com.back.team11.domain.wishlist.entity.Wishlist
import java.time.LocalDateTime

data class WishlistResponse(
    val wishlistId: Long,
    val cafeId: Long,
    val cafeName: String,
    val createAt: LocalDateTime?
) {
    companion object {
        @JvmStatic
        fun from(wishlist: Wishlist): WishlistResponse {
            return WishlistResponse(
                wishlistId = wishlist.id,
                cafeId = wishlist.cafe?.id ?: 0,
                cafeName = wishlist.cafe?.name ?: "",
                createAt = wishlist.createdAt
            )
        }
    }
}
