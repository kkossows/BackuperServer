package main.config;

import java.io.Serializable;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class UserCredentials implements Serializable{
    private String username;
    private String password;

    public UserCredentials(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
}
