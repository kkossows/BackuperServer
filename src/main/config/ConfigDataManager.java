package main.config;

import java.io.*;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class ConfigDataManager {


    public static boolean isAppDirExists(){
        return new File(Properties.appDataDir).exists();
    }
    public static void createAppDir(){
        new File(Properties.appDataDir).mkdir();
    }


    public static boolean isGlobalConfigFileExists(){
        return new File(Properties.appDataDir + "/" + Properties.globalConfigFile).exists();
    }
    public static void createGlobalConfig(GlobalConfig globalConfig){
        File globalConfigFile = new File(Properties.appDataDir + Properties.globalConfigFile);

        if(globalConfigFile.exists())
            globalConfigFile.delete();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(globalConfigFile));
            oos.writeObject(globalConfig);
            oos.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    public static GlobalConfig readGlobalConfig()  {
        File globalConfigFile = new File(Properties.appDataDir + Properties.globalConfigFile);
        ObjectInputStream ois = null;
        GlobalConfig globalConfig = null;

        try {
            ois = new ObjectInputStream(new FileInputStream(globalConfigFile));
            globalConfig = (GlobalConfig)ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return globalConfig;
    }


    public static boolean isUsersLoginCredentialsFileExists(){
        return new File(Properties.appDataDir + "/" + Properties.usersLoginCredentialsFile).exists();
    }
    public static void createUsersLoginCredentials(UsersLoginCredentials usersLoginCredentials){
        File usersLoginCredentialsFile = new File(
                Properties.appDataDir + "/" + Properties.usersLoginCredentialsFile);

        if(usersLoginCredentialsFile.exists())
            usersLoginCredentialsFile.delete();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(usersLoginCredentialsFile));
            oos.writeObject(usersLoginCredentials);
            oos.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    public static UsersLoginCredentials readUsersLoginCredentials(){
        File usersLoginCredentialsFile = new File(
                Properties.appDataDir + Properties.usersLoginCredentialsFile);
        ObjectInputStream ois = null;
        UsersLoginCredentials usersLoginCredentials = null;

        try {
            ois = new ObjectInputStream(new FileInputStream(usersLoginCredentialsFile));
            usersLoginCredentials = (UsersLoginCredentials)ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return usersLoginCredentials;
    }


    public static boolean isUserConfigFileExists(String username){
        return new File(Properties.appDataDir + username + ".dat").exists();
    }
    public static void createUserConfig(UserConfig userConfig) {
        File userConfigFile = userConfig.getUserConfigFile();

        if (userConfigFile.exists())
            userConfigFile.delete();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userConfigFile));
            oos.writeObject(userConfig);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static UserConfig readUserConfig(String username){
        File userConfigFile = new File(
                Properties.appDataDir + username + ".dat");

        ObjectInputStream ois = null;
        UserConfig userConfig = null;

        try {
            ois = new ObjectInputStream(new FileInputStream(userConfigFile));
            userConfig = (UserConfig)ois.readObject();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return userConfig;
    }
    public static void deleteUserConfig(UserConfig userConfig){
        userConfig.getUserConfigFile().delete();
    }
}
