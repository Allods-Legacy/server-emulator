package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppInSerializable;
import eu.allodslegacy.io.serialization.SerializationDataInput;

public class LoginMsg implements CppInSerializable {

    private String userName;
    private String password;
    private int versionNumber;

    public LoginMsg() {
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    @Override
    public void readCpp(SerializationDataInput in) throws Exception {
        this.password = in.readUTF();
        this.userName = in.readUTF();
        this.versionNumber = in.readInt();
    }
}
