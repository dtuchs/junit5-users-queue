package com.dtuchs.domain;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

/**
 * Класс для хранения данных юзера для теста в очереди
 *
 * @see com.dtuchs.jupiter.UserExtension#resolveParameter(ParameterContext, ExtensionContext)
 */
public class User {

    public enum UserType {
        PARTICIPANT, ORGANIZER
    }

    private String login;
    private String password;
    private UserType userType;

    public UserType getUserType() {
        return userType;
    }

    public User setUserType(UserType userType) {
        this.userType = userType;
        return this;
    }

    public String getLogin() {
        return login;
    }

    public User setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }
}
