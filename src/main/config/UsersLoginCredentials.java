package main.config;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class UsersLoginCredentials implements Serializable {

    private ArrayList<UserCredentials> usersCredentialsList;

    public UsersLoginCredentials(ArrayList<UserCredentials> usersCredentialsList){
        this.usersCredentialsList = usersCredentialsList;
    }

    public boolean addUserCredentials(UserCredentials newUserCredentials) {
        //verify whether user already exist or not
        boolean userCredentialsExists = false;

        for(UserCredentials userCredentials : usersCredentialsList){
            if (userCredentials.getUsername().equals(newUserCredentials.getUsername())){
                userCredentialsExists = true;
            }
        }
        if (userCredentialsExists) {
            //user exists
            return false;
        } else {
            //user not exists
            usersCredentialsList.add(newUserCredentials);
            return true;
        }
    }
    public void removeUserCredentials(String username){
        //only username is required
        //cannot register many users with same username and different password

        int index = -1;
        for (int i = 0; i < usersCredentialsList.size(); i++) {
            if (usersCredentialsList.get(i).getUsername().equals(username)) {
                index = i;
                break;
            }
        }
        if(index != -1){
            //user found
            usersCredentialsList.remove(index);
        }
    }
    public boolean authenticateUserCredentials(UserCredentials userCredentialsToVerify){
        //find user
        for (UserCredentials userCredentials : usersCredentialsList){
            if (userCredentials.getUsername().equals(userCredentialsToVerify.getUsername()))
                if (userCredentials.getPassword().equals(userCredentialsToVerify.getPassword()))
                    return true;
        }
        return false;
    }
}
