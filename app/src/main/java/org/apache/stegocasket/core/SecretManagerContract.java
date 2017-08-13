package org.apache.stegocasket.core;

public final class SecretManagerContract {

    public static final String AUTHORITY = "org.apache.stegocasket.provider";

    public static final String REGISTER_URI = "content://" + AUTHORITY + "/status";

    public static final String STATUS_FIELD = "status";

    public static final String PICTURE_FIELD = "picture";

    public static final String PWD_FIELD = "password";

    public static final String LOAD_FIELD = "loadOnInit";

    public static final String ROOT_ID_FIELD = "rootUUID";

    public static final String SEC_ID_FIELD = "_ID";

    public static final String SEC_NAME_FIELD = "name";

    public static final String SEC_KEY_FIELD = "key";

    public static final String SEC_VALUE_FIELD = "value";

    public static final String SEC_TYPE_FIELD = "type";

    /*
    Control commands
     */
    public static final String INIT_INTENT = "org.apache.stegocasket.init";

    public static final String FLUSH_INTENT = "org.apache.stegocasket.flush";

    /*
    Status codes
     */

    public static final int STATUS_OK = 0;

    public static final int STATUS_ERR = 1;

}
