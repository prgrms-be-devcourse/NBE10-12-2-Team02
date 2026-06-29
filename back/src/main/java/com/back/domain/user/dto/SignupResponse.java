package com.back.domain.user.dto;

import com.back.domain.user.entity.User;

public record SignupResponse(
        Long userId,
        String loginType
) {
    public SignupResponse(User user) {
        this(user.getUserId(), user.getLoginType().name());
    }
}