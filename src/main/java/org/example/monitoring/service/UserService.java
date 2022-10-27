package org.example.monitoring.service;

import lombok.RequiredArgsConstructor;
import org.example.monitoring.entity.User;
import org.example.monitoring.exception.BusinessException;
import org.example.monitoring.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findUser(String login) {
        return userRepository.findById(login)
                .orElseThrow(() -> new BusinessException(String.format("User %s not found", login),
                        HttpStatus.NOT_FOUND));
    }

    @Transactional
    public User getOrCreateUser(String login) {
        return userRepository.findById(login)
                .orElseGet(() -> createUser(login));
    }

    private User createUser(String login) {
        User user = new User();
        user.setLogin(login);
        return userRepository.save(user);
    }
}
