package com.example.security;
import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain vaadinSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Allow all access to /images/
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/images/*.png", "/line-awesome/**", "/*.css", "/aura/**").permitAll())
                .with(vaadin(), vaadin -> vaadin.loginView("login"))
                .build();
    }

}