package com.nettydemo.common.entities;

import java.io.Serializable;

/**
 * Entity object for a login message
 * Contains user name and password
 * While printing (to log or console) password is replaced with ****
 */
public class LoginMessage implements Serializable {
    private String login;
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginMessage{" +
                "login='" + login + '\'' +
                ", password=*****" +
                '}';
    }
}
