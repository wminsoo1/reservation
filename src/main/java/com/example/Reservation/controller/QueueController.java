package com.example.Reservation.controller;

import com.example.Reservation.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/join")
    public Map<String, String> joinQueue() {
        String userId = queueService.registerQueue();

        Map<String, String> response = new HashMap<>();
        response.put("userId", userId);
        response.put("message", "대기열에 등록되었습니다. 폴링을 시작하세요.");

        return response;
    }

    @GetMapping("/rank")
    public ResponseEntity<?> getRank(@RequestParam String userId) {
        var status = queueService.getStatus(userId);
        return ResponseEntity.ok(status);
    }

}