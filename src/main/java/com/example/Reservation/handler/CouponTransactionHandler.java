package com.example.Reservation.handler;

import com.example.Reservation.event.CouponIssuedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class CouponTransactionHandler {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAfterCommit(CouponIssuedEvent event) {
        log.info("🎯 [After Commit] DB 성공 확인. 카프카 오프셋 수동 커밋: {}", event.getUserId());

        // 실제 카프카 동기 커밋 수행
        event.getAck().acknowledge();

        log.info("🎉 최종 처리 완료");
    }
}
