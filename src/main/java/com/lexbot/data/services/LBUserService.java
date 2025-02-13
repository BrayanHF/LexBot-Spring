package com.lexbot.data.services;

import com.lexbot.data.firestore_dao.LBUser;
import com.lexbot.data.firestore_dao.LBUserStatus;
import com.lexbot.data.repositories.LBUserRepository;
import com.lexbot.utils.validations.SimpleValidation;
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
        SimpleValidation.validateNotNulls(user);
        SimpleValidation.validateEmail(user.getEmail());
        SimpleValidation.validateStrings(user.getUsername(), user.getPassword());

        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        user.setStatus(LBUserStatus.UNVERIFIED);

        return userRepository.addUser(user);
    }

    public Mono<Void> updateUser(String userId, Map<String, Object> updates) {
        SimpleValidation.validateStrings(userId);
        SimpleValidation.validateUpdates(LBUser.class, updates);

        return userRepository.updateUser(userId, updates);
    }

    public Mono<LBUser> getUserByEmail(String email) {
        SimpleValidation.validateEmail(email);

        return userRepository.getUserByEmail(email);
    }

    public Mono<Void> deleteUserById(String userId) {
        SimpleValidation.validateStrings(userId);

        return userRepository.deleteUserById(userId);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        SimpleValidation.validateStrings(rawPassword, encodedPassword);

        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}
