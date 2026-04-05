package com.example.Reservation.controller;

import com.example.Reservation.domain.Product;
import com.example.Reservation.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {
        // 서비스에서 캐싱된 데이터를 가져옵니다.
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
}