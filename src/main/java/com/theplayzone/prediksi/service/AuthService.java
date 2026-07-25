package com.theplayzone.prediksi.service;

import com.theplayzone.prediksi.dao.UserDAO;
import com.theplayzone.prediksi.model.User;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) throws SQLException {
        User u = userDAO.findByUsername(username);
        if (u == null) {
            return null;
        }
        String hash = PasswordUtil.sha256(password);
        return hash.equals(u.getPasswordHash()) ? u : null;
    }
}
