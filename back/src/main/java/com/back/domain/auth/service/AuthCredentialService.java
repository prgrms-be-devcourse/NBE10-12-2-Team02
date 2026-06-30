package com.back.domain.auth.service;

import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthCredentialService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void checkPassword(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new ServiceException(ErrorCode.AUTH_PASSWORD_MISMATCH);
    }

    public User findByLoginId(String id) {
        return userRepository.findByLoginIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }
}
