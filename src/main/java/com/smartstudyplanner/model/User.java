package com.smartstudyplanner.model;

/**
 * User entity representing a student account.
 * Demonstrates Encapsulation through private fields with validation.
 */
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private String preferredStrategy; // PRIORITY_BASED or DEADLINE_BASED

    public User() {
        super();
        this.preferredStrategy = "PRIORITY_BASED";
    }

    public User(Long id, String username, String email, String passwordHash, String fullName) {
        super(id);
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.preferredStrategy = "PRIORITY_BASED";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName != null ? fullName.trim() : null;
    }

    public String getPreferredStrategy() {
        return preferredStrategy != null ? preferredStrategy : "PRIORITY_BASED";
    }

    public void setPreferredStrategy(String preferredStrategy) {
        this.preferredStrategy = preferredStrategy;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", preferredStrategy='" + preferredStrategy + '\'' +
                '}';
    }
}

