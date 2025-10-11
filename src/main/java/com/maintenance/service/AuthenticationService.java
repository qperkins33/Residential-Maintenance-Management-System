package com.maintenance.service;

import com.maintenance.dao.UserDAO;
import com.maintenance.models.User;
import com.maintenance.models.UserRegistrationData;

public class AuthenticationService {
    private static AuthenticationService instance;
    private User currentUser;
    private final UserDAO userDAO;

    private AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        User user = userDAO.authenticateUser(username, password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getUserType() {
        if (currentUser == null) return null;
        return currentUser.getClass().getSimpleName();
    }

    public boolean isUsernameTaken(String username) {
        return userDAO.isUsernameTaken(username);
    }

    public boolean registerUser(UserRegistrationData registrationData) {
        return userDAO.registerUser(registrationData);
    }
}
