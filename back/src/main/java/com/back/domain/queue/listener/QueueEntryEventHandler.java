package com.back.domain.queue.listener;

import com.back.domain.queue.constant.QueueEventType;
import com.back.domain.queue.dto.QueueEventResponse;
import com.back.domain.queue.event.QueueRankUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueEntryEventHandler {
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @EventListener
    public void handleQueueRankUpdated(QueueRankUpdatedEvent event) {
        QueueEventResponse<QueueRankUpdatedEvent> response =
                new QueueEventResponse<>(QueueEventType.QUEUE_RANK_UPDATED, event);

        messagingTemplate.convertAndSendToUser(
                event.userId().toString(),
                "/queue/schedules/" + event.scheduleId() + "/status",
                response
        );
    }
}
