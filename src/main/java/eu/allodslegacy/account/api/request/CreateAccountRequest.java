package eu.allodslegacy.account.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateAccountRequest {

    public final String login;
    public final String password;

    @JsonCreator
    public CreateAccountRequest(@JsonProperty(value = "login", required = true) String login, @JsonProperty(value = "password", required = true) String password) {
        this.login = login;
        this.password = password;
    }

}
