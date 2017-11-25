package main.config;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class Properties {
    public static String appName			        = "backuper-server";
    public static String appDataDir 		        = System.getenv("APPDATA") + "\\" + appName + "\\";
    public static String globalConfigFile	        = "globalConfig.dat";
    public static String usersLoginCredentialsFile  = "usersConfig.dat";

    public static String defaultServerIpAddress     = "127.0.0.1";
    public static int defaultServerPortNumber       = 9889;

    public static int bufferSize                    = 8192;

    public static int codeGeneratorRange                = 100;
}
