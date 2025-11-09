package com.codex.backend.domain.user;

import com.codex.backend.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 用户实体：保存账号、基础资料与交易统计信息。
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 191)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(length = 100)
    private String location;

    @Column(length = 500)
    private String bio;

    @Column(nullable = false)
    private double rating;

    @Column(nullable = false)
    private int dealsCount;

    protected User() {
        // JPA only
    }

    public User(String email, String passwordHash, String displayName) {
        this(email, passwordHash, displayName, null, null, 0.0, 0);
    }

    public User(
            String email,
            String passwordHash,
            String displayName,
            String location,
            String bio,
            double rating,
            int dealsCount) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.location = location;
        this.bio = bio;
        this.rating = rating;
        this.dealsCount = dealsCount;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLocation() {
        return location;
    }

    public String getBio() {
        return bio;
    }

    public double getRating() {
        return rating;
    }

    public int getDealsCount() {
        return dealsCount;
    }

    public void updateProfile(String displayName, String location, String bio, double rating, int dealsCount) {
        this.displayName = displayName;
        this.location = location;
        this.bio = bio;
        this.rating = rating;
        this.dealsCount = dealsCount;
    }
}
