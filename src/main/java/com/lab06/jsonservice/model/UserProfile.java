package com.lab06.jsonservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ================================================================
 * UserProfile - Profile database entity
 * ================================================================
 * Энэ entity нь JSON Service-ийн өөрийн Profile DB-д хадгалагдана.
 * SOAP Service-ийн Auth DB-с ТУСДАА - Option 2 Independent DB.
 *
 * Агуулах мэдээлэл (Facebook-тэй төстэй профайл):
 *   - userId    : SOAP service-ийн userId-тэй холбогдох гол key
 *   - name      : Бүтэн нэр
 *   - email     : И-мэйл (reference болгон хадгална)
 *   - bio       : Товч танилцуулга
 *   - phone     : Утасны дугаар
 *   - location  : Байршил
 *   - avatarUrl : Профайл зургийн URL
 * ================================================================
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SOAP Service-ийн userId - хоёр service-ийг холбох гол key
     * Register хийхэд SOAP-аас авсан userId-г энд хадгална.
     * Unique байх ёстой - нэг auth user = нэг profile
     */
    @Column(nullable = false, unique = true)
    private Long userId;

    // Хэрэглэгчийн харуулах нэр (Display name)
    @Column(nullable = false)
    private String name;

    // И-мэйл - SOAP-ийн auth DB-тэй sync байх reference
    @Column
    private String email;

    // Товч намтар / танилцуулга
    @Column(length = 500)
    private String bio;

    // Утасны дугаар
    @Column
    private String phone;

    // Байршил (хот, улс)
    @Column
    private String location;

    // Профайл зургийн URL
    @Column
    private String avatarUrl;

    // Профайл үүсгэсэн огноо
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Сүүлд шинэчилсэн огноо
    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================================================================
    // Getters & Setters
    // ================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}