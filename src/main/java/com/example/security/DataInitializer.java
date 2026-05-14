package com.example.security;

import com.example.user.AppUserService;
import com.example.user.AppUserRepository;
import com.example.user.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserService userService;
    private final AppUserRepository userRepository;

    public DataInitializer(AppUserService userService,
                           AppUserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // Luodaan vain jos ei vielä ole
        if (userRepository.count() == 0) {
            userService.createUser(
                    "admin", "admin@eventapp.com", "admin123",
                    Set.of(Role.ADMIN, Role.SUPER, Role.USER),
                    "Admin", "Pääkäyttäjä"
            );
            userService.createUser(
                    "user", "user@eventapp.com", "user123",
                    Set.of(Role.USER),
                    "Pertti", "Peruskäyttäjä"
            );
        }
    }
}