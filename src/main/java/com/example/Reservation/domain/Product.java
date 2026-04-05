package com.example.Reservation.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeName;     // 가게 이름 (예: 원조 장충동 할매 족발)
    private double rating;        // 별점
    private String reviewCount;   // 리뷰 수 (예: "1,240+")
    private String menuName;      // 메뉴 이름
    private String imageUrl;      // 음식 사진 URL

    private int originalPrice;    // 원가
    private int discountPrice;    // 할인가
    private int discountRate;     // 할인율

    public void updateDiscount(int discountPrice) {
        this.discountPrice = discountPrice;
    }
}