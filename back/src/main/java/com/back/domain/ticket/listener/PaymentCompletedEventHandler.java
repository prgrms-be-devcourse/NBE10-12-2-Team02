package com.back.domain.ticket.listener;

import com.back.domain.ticket.event.PaymentCompletedEvent;
import com.back.domain.waiting.service.WaitingQueueManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCompletedEventHandler {

    private final WaitingQueueManager waitingQueueManager;

    @EventListener
    public void handlePaymentCompleted(
            PaymentCompletedEvent event
    ) {
        waitingQueueManager.removeActiveUser(
                event.scheduleId(),
                event.userId()
        );
    }
}