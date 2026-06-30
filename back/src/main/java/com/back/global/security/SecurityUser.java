package com.back.global.security;

import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class SecurityUser extends User {
    private final Long id;
    private final String name;

    public SecurityUser(
            Long id,
            String name
    ) {
        super(String.valueOf(id), "", List.of()); // 우리의 시나리오(REST API)에서는 이 객체의 비밀번호 필드를 활용할 일이 없다.
        this.id = id;
        this.name = name;
    }
}