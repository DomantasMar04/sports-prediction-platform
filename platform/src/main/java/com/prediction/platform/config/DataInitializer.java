package com.prediction.platform.config;

import com.prediction.platform.user.User;
import com.prediction.platform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        // Sukuriame default user su id=1 jei dar nėra
        if (userRepository.count() == 0) {
            User user = new User();
            user.setUsername("default");
            user.setEmail("default@prediction.lt");
            user.setPassword("password123");
            user.setRole(User.Role.USER);
            userRepository.save(user);
            System.out.println("Default vartotojas sukurtas: id=1, username=default");
        }
    }
}