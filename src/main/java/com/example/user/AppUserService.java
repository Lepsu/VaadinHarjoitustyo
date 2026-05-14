package com.example.user;

import com.example.base.EmailService;
import com.example.base.PushNotificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class AppUserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final PushNotificationService pushService;
    private final EmailService emailService;

    // Admin-sähköposti – vaihda omaan
    private static final String ADMIN_EMAIL =
            "admin@eventapp.com";

    public AppUserService(AppUserRepository repository,
                          PasswordEncoder passwordEncoder,
                          PushNotificationService pushService,
                          EmailService emailService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.pushService = pushService;
        this.emailService = emailService;
    }

    public List<AppUser> findAll() {
        return repository.findAll();
    }

    public Optional<AppUser> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public Optional<AppUser> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public boolean usernameExists(String username) {
        return repository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return repository.existsByEmail(email);
    }

    public AppUser createUser(String username, String email,
                              String plainPassword,
                              Set<Role> roles,
                              String firstName,
                              String lastName) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(plainPassword));
        user.setRoles(roles);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        AppUser saved = repository.save(user);

        // Push-ilmoitus adminille
        pushService.broadcast(
                "Uusi käyttäjä rekisteröityi: " + username);

        // Sähköposti-ilmoitus ylläpitäjälle
        emailService.sendNewUserNotification(
                ADMIN_EMAIL, username, email);

        return saved;
    }

    public AppUser save(AppUser user) {
        return repository.save(user);
    }

    public void delete(AppUser user) {
        repository.delete(user);
    }
}