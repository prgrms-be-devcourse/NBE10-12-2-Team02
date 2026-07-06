package com.back.domain.waiting.controller;

import com.back.domain.waiting.dto.WaitingQueueRegisterResponse;
import com.back.domain.waiting.service.WaitingQueueService;
import com.back.global.annotation.ApiV1;
import com.back.global.requestcontext.RequestContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ApiV1
@RestController
@RequestMapping("/waiting")
@RequiredArgsConstructor
@Tag(name = "Waiting", description = "Waiting API")
public class WaitingQueueController {
    private final WaitingQueueService waitingQueueService;
    private final RequestContext requestContext;

    @PostMapping("/concerts/{concertId}/schedules/{scheduleId}/waiting-queue")
    public WaitingQueueRegisterResponse registerWaiting(
            @PathVariable Long concertId,
            @PathVariable Long scheduleId
    ) {
        return waitingQueueService.registerWaiting(
                concertId,
                scheduleId,
                requestContext.getActor().getId()
        );
    }
}
