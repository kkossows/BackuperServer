package main.config;

import java.io.*;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class ConfigDataManager {

    public static boolean isAppDirExists(){
        return new File(Properties.appDataDir).exists();
    }
    public static boolean createAppDir(){
        return new File(Properties.appDataDir).mkdir();
    }

    public static boolean isGlobalConfigFileExists(){
        return new File(Properties.globalConfigFile).exists();
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

}
