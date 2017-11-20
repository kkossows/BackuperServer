package main.config;

import java.io.Serializable;

/**
 * Created by kkossowski on 20.11.2017.
 */
public class GlobalConfig implements Serializable {
    private String defaultServerIpAddress;
    private int defaultServerPortNumber;

    private String savedServerIpAddress;
    private int savedServerPortNumber;
    private String savedStoragePath;


    public GlobalConfig(){
        this.defaultServerIpAddress = Properties.defaultServerIpAddress;
        this.defaultServerPortNumber = Properties.defaultServerPortNumber;

        this.savedServerIpAddress = "";
        this.savedServerPortNumber = -1;
        this.savedStoragePath = "";
    }


    public String getSavedStoragePath() {
        return this.savedStoragePath;
    }
    public String getSavedServerIpAddress() {
        return this.savedServerIpAddress;
    }
    public int getSavedServerPortNumber() {
        return this.savedServerPortNumber;
    }

    public String getDefaultServerIpAddress() {
        return this.defaultServerIpAddress;
    }
    public int getDefaultServerPortNumber() {
        return this.defaultServerPortNumber;
    }


    public boolean areConfigVariablesDifferentThanDefault() {
        if (savedServerIpAddress != "" && savedServerPortNumber != -1)
            return true;
        else
            return false;
    }
    public boolean isStoragePathChoosen() {
        if (savedStoragePath != "")
            return true;
        else
            return false;
    }
}
