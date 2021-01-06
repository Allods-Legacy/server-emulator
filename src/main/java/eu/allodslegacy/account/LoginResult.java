package eu.allodslegacy.account;

import eu.allodslegacy.io.serialization.SerializationDataInput;
import eu.allodslegacy.io.serialization.SerializationException;

public enum LoginResult {

    LOGIN_SUCCESS,
    OTHER_CLIENT_IN_GAME,
    WRONG_VERSION,
    SERVER_ERROR,
    UNEXPECTED_DATA,
    WRONG_AUTH_INFO,
    BANNED,
    TIMEOUT,
    SERVER_IS_OVERLOADED,
    ACCOUNT_INACTIVE,
    ACCOUNT_INACTIVE_TEMPORARY,
    WRONG_CERTIFICATE,
    CLIENT_VALIDATION_FAILED;

    public static LoginResult readCpp(SerializationDataInput inputStream) throws Exception {
        int value = inputStream.readByte();
        if (value > 0 && value < values().length) {
            return values()[value];
        } else {
            throw new SerializationException();
        }
    }
}
