package com.contrastsecurity.sheepdog;

import org.apache.http.NameValuePair;

import java.util.List;

public class LoginException extends Exception {

    public LoginException(List<NameValuePair> credentials) {
        super("Invalid credentials " + credentials + " supplied. Did you register an account on WatchDog with these" +
                " credentials?");
    }
}
