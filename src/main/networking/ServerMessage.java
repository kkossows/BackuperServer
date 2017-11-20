package main.networking;

/**
 * Created by kkossowski on 18.11.2017.
 */
public enum ServerMessage {
    GET_USERNAME,
    GET_PASSWORD,
    USER_CREATED,
    USER_EXISTS,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    SENDING_BACKUP_FILES_LIST,
    SENDING_BACKUP_FILES_LIST_FINISHED,
    GET_FILE_PATH,
    SENDING_FILE_VERSIONS,
    SENDING_FILE_VERSIONS_FINISHED,
    FILE_REMOVED,
    GET_FILE_VERSION,
    FILE_VERSION_REMOVED,
    DELETE_USER_FINISHED,
    LOG_OUT_FINISHED,
    GET_FILE_SIZE,
    GET_FILE_CONTENT,
    SENDING_FILE,
    SENDING_FILE_SIZE,
    SENDING_FILE_FINISHED
}
