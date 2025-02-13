package com.lexbot.data.services;

import com.lexbot.data.firestore_dao.LBUser;
import com.lexbot.data.firestore_dao.LBUserStatus;
import com.lexbot.data.repositories.LBUserRepository;
import com.lexbot.data.services.validations.Validation;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class LBUserService {

    private final LBUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LBUserService(LBUserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Mono<LBUser> addUser(LBUser user) {
        Validation.validateNotNulls(user);
        Validation.validateEmail(user.getEmail());
        Validation.validateStrings(user.getUsername(), user.getPassword());

        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        user.setStatus(LBUserStatus.UNVERIFIED);

        return userRepository.addUser(user);
    }

    public Mono<Void> updateUser(String userId, Map<String, Object> updates) {
        Validation.validateStrings(userId);
        Validation.validateUpdates(LBUser.class, updates);

        return userRepository.updateUser(userId, updates);
    }

    public Mono<LBUser> getUserByEmail(String email) {
        Validation.validateEmail(email);

        return userRepository.getUserByEmail(email);
    }

    public Mono<Void> deleteUserById(String userId) {
        Validation.validateStrings(userId);

        return userRepository.deleteUserById(userId);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        Validation.validateStrings(rawPassword, encodedPassword);

        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}
