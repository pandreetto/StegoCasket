package org.apache.stegocasket.core;

public class SecretException extends Exception {

    public final static long serialVersionUID = 1358088018;

    private int strRef;

    public SecretException(int strReference) {
        strRef = strReference;
    }

    public int getMsgRef() {
        return strRef;
    }

}
