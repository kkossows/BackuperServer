package main.networking;

/**
 * Created by kkossowski on 18.11.2017.
 */
public enum  ClientMessage {
    LOG_IN,
    REGISTER,
    GET_BACKUP_FILES_LIST,
    GET_ALL_FILE_VERSIONS,
    REMOVE_FILE,
    REMOVE_FILE_VERSION,
    DELETE_USER,
    LOG_OUT,
    BACKUP_FILE,
    BACKUP_FILE_FINISHED,
    RESTORE_FILE
}
