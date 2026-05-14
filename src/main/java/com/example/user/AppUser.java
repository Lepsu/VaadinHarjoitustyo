package com.example.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Käyttäjänimi on pakollinen")
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;

    @Email(message = "Virheellinen sähköposti")
    @NotBlank(message = "Sähköposti on pakollinen")
    @Column(unique = true)
    private String email;

    // Tallennetaan VAIN hash, ei koskaan plain text
    @Column(name = "password_hash")
    private String passwordHash;

    @NotBlank(message = "Etunimi on pakollinen")
    @Size(min = 1, max = 50)
    private String firstName;

    @NotBlank(message = "Sukunimi on pakollinen")
    @Size(min = 1, max = 50)
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    private String profileImagePath;

    private boolean enabled = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Getterit ja setterit
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String path) { this.profileImagePath = path; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}