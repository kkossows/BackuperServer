package main.config;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class UserCredentials {
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
