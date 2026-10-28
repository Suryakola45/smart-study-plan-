package com.smartstudyplanner.service;

import com.smartstudyplanner.dao.UserDAO;
import com.smartstudyplanner.exception.InvalidStudyDataException;
import com.smartstudyplanner.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Service managing user authentication, registration, and BCrypt security.
 */
public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User register(String username, String email, String password, String fullName)
            throws InvalidStudyDataException {

        if (username == null || username.trim().length() < 3) {
            throw new InvalidStudyDataException("username", "Username must be at least 3 characters long.");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidStudyDataException("email", "Please provide a valid email address.");
        }
        if (password == null || password.length() < 6) {
            throw new InvalidStudyDataException("password", "Password must be at least 6 characters long.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new InvalidStudyDataException("fullName", "Full name cannot be blank.");
        }

        // Check uniqueness
        if (userDAO.findByUsername(username.trim()).isPresent()) {
            throw new InvalidStudyDataException("username", "Username is already taken.");
        }
        if (userDAO.findByEmail(email.trim().toLowerCase()).isPresent()) {
            throw new InvalidStudyDataException("email", "Email is already registered.");
        }

        // BCrypt hash with work factor 10
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(hashed);
        user.setFullName(fullName.trim());
        user.setPreferredStrategy("PRIORITY_BASED");

        return userDAO.save(user);
    }

    public Optional<User> authenticate(String identifier, String password) {
        if (identifier == null || password == null || identifier.isBlank() || password.isBlank()) {
            return Optional.empty();
        }

        Optional<User> userOpt = userDAO.findByUsername(identifier.trim());
        if (userOpt.isEmpty()) {
            userOpt = userDAO.findByEmail(identifier.trim().toLowerCase());
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            try {
                if (BCrypt.checkpw(password, user.getPasswordHash())) {
                    return Optional.of(user);
                }
            } catch (Exception e) {
                // BCrypt check failed (malformed hash or invalid input)
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public void updatePreferredStrategy(Long userId, String strategy) {
        userDAO.updatePreferredStrategy(userId, strategy);
    }
}

