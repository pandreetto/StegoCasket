package org.apache.stegocasket.core;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.stegocasket.R;
import org.apache.stegocasket.crypto.CryptoUtils;
import org.apache.stegocasket.stego.bitmap.StegoCodec;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

public class SecretManager extends ContentProvider {

    private static final String TAG = SecretManager.class.getName();

    private static final int CACHE_IDLE = 0;

    private static final int CACHE_STABLE = 1;

    private static final int CACHE_TOFLUSH = 2;

    private String rootTable;

    private Uri pictureURI;

    private String pwd;

    private HashMap<String, Secret> secretTable;

    private int cacheStatus;

    private int statusCode;

    public SecretManager() {

        cacheStatus = CACHE_IDLE;
        statusCode = SecretManagerContract.STATUS_OK;
    }

    private void initSecrets() {
        secretTable = new HashMap<>();
        cacheStatus = CACHE_STABLE;
    }

    private void loadSecrets()
            throws SecretException {

        BufferedReader reader = null;

        try {

            byte[] cryptoData = StegoCodec.decode(this.getContext(), pictureURI, this.pwd);

            Cipher cipher = CryptoUtils.setupCipher(Cipher.DECRYPT_MODE, this.pwd);

            ByteArrayInputStream binStream = new ByteArrayInputStream(cryptoData);
            CipherInputStream cIn = new CipherInputStream(binStream, cipher);
            reader = new BufferedReader(new InputStreamReader(cIn));

            SecretParser parser = new SecretParser(reader);
            parser.parse();
            ArrayList<Secret> secretList = parser.getSecrets();

            secretTable = new HashMap<>(secretList.size());
            for (Secret tmpSec : secretList) {
                secretTable.put(UUID.randomUUID().toString(), tmpSec);
            }

            cacheStatus = CACHE_STABLE;

        } catch (FileNotFoundException fEx) {

            Log.e(TAG, fEx.getMessage(), fEx);
            throw new SecretException(R.string.nocryptofile);

        } catch (IOException pEx) {

            Log.e(TAG, pEx.getMessage(), pEx);
            throw new SecretException(R.string.errparsecrypto);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioEx) {
                    Log.e(TAG, ioEx.getMessage(), ioEx);
                }
            }
        }

    }

    private void writeSecrets()
            throws SecretException {
        SecretWriter writer = null;

        if (cacheStatus < CACHE_TOFLUSH)
            return;

        try {
            Cipher cipher = CryptoUtils.setupCipher(Cipher.ENCRYPT_MODE, this.pwd);

            ByteArrayOutputStream boutStream = new ByteArrayOutputStream();
            CipherOutputStream cOut = new CipherOutputStream(boutStream, cipher);
            writer = new SecretWriter(cOut);

            for (String sUUID : secretTable.keySet()) {
                Secret secItem = secretTable.get(sUUID);
                Log.d(SecretManager.class.getName(), secItem.toXML());
                writer.write(secItem);
            }
            writer.close();
            writer = null;

            StegoCodec.encode(this.getContext(), pictureURI, boutStream.toByteArray(), this.pwd);

            cacheStatus = CACHE_STABLE;

        } catch (IOException ioEx) {

            Log.e(TAG, ioEx.getMessage(), ioEx);
            throw new SecretException(R.string.nocryptofile);


        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    @Override
    public boolean onCreate() {
        cacheStatus = CACHE_IDLE;
        rootTable = null;

        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SecretManagerContract.INIT_INTENT);
        iFilter.addAction(SecretManagerContract.FLUSH_INTENT);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this.getContext());
        broadcastManager.registerReceiver(new CommandReceiver(), iFilter);

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        if (!uri.getAuthority().equals(SecretManagerContract.AUTHORITY)) {
            return null;
        }

        if (uri.toString().equals(SecretManagerContract.REGISTER_URI)) {
            String[] columns = new String[]{
                    SecretManagerContract.STATUS_FIELD
            };
            MatrixCursor cursor = new MatrixCursor(columns);
            cursor.addRow(new Object[]{Integer.valueOf(statusCode)});
            return cursor;
        }

        if (cacheStatus == CACHE_IDLE) {
            return null;
        }

        if (uri.getPath().equals("/" + rootTable)) {
            String[] columns = new String[]{
                    SecretManagerContract.SEC_ID_FIELD,
                    SecretManagerContract.SEC_NAME_FIELD
            };
            MatrixCursor cursor = new MatrixCursor(columns);
            for (String sUUID : secretTable.keySet()) {
                cursor.addRow(new Object[]{sUUID, secretTable.get(sUUID).getId()});
            }
            return cursor;
        }

        String secUUID = uri.getPath().substring(1);
        if (secretTable.containsKey(secUUID)) {
            String[] columns = new String[]{
                    SecretManagerContract.SEC_KEY_FIELD,
                    SecretManagerContract.SEC_VALUE_FIELD,
                    SecretManagerContract.SEC_TYPE_FIELD
            };
            MatrixCursor cursor = new MatrixCursor(columns);
            GroupOfSecret gSecrets = (GroupOfSecret) secretTable.get(secUUID);
            /*
            TODO implement sort
             */
            for (Secret tmpsec : gSecrets) {
                RenderableSecret rSecret = (RenderableSecret) tmpsec;
                cursor.addRow(new Object[]{
                        rSecret.getId(),
                        rSecret.getValue(),
                        rSecret.getClass().getName()
                });
            }
            return cursor;
        }

        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        if (!uri.getAuthority().equals(SecretManagerContract.AUTHORITY) || cacheStatus == CACHE_IDLE) {
            return null;
        }

        if (uri.getPath().equals("/" + rootTable)) {
            /*
            Register a new GroupOfSecret
             */
            String gSecUUID = values.getAsString(SecretManagerContract.SEC_ID_FIELD);
            String gSecName = values.getAsString(SecretManagerContract.SEC_NAME_FIELD);

            if (secretTable.containsKey(gSecUUID)) {
                Log.e(TAG, "UUID already exists");
                return null;
            }

            GroupOfSecret gSecrets = new GroupOfSecret();
            gSecrets.setId(gSecName);
            secretTable.put(gSecUUID, gSecrets);
            cacheStatus = CACHE_TOFLUSH;

            return uri;
        }

        String gSecUUID = uri.getPath().substring(1);
        if (!secretTable.containsKey(gSecUUID)) {
            return null;
        }

        /*
        Register a property in a group of secret
         */
        String secKey = values.getAsString(SecretManagerContract.SEC_KEY_FIELD);
        String secValue = values.getAsString(SecretManagerContract.SEC_VALUE_FIELD);
        String secClass = values.getAsString(SecretManagerContract.SEC_TYPE_FIELD);

        try {
            RenderableSecret secItem = (RenderableSecret) Class.forName(secClass).newInstance();
            secItem.setId(secKey);
            secItem.setValue(secValue);

            GroupOfSecret currGroup = (GroupOfSecret) secretTable.get(gSecUUID);
            currGroup.add(secItem);
            cacheStatus = CACHE_TOFLUSH;

            return uri;

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }

        return null;
    }

    @Override
    public int update(@NonNull Uri uri,
                      ContentValues values,
                      String selection,
                      String[] selectionArgs) {

        if (!uri.getAuthority().equals(SecretManagerContract.AUTHORITY) || cacheStatus == CACHE_IDLE) {
            return 0;
        }

        String gSecUUID = uri.getPath().substring(1);
        if (secretTable.containsKey(gSecUUID) && selection.equals(SecretManagerContract.SEC_KEY_FIELD)) {
            /*
            Change a property in a group of secret
             */
            String secValue = values.getAsString(SecretManagerContract.SEC_VALUE_FIELD);
            String secKey = selectionArgs[0];

            GroupOfSecret gSecret = (GroupOfSecret) secretTable.get(gSecUUID);
            RenderableSecret rSecret = (RenderableSecret) gSecret.get(secKey);
            if (rSecret != null) {
                rSecret.setValue(secValue);
                cacheStatus = CACHE_TOFLUSH;
                return 1;
            }

        }

        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      String selection,
                      String[] selectionArgs) {

        if (!uri.getAuthority().equals(SecretManagerContract.AUTHORITY) || cacheStatus == CACHE_IDLE) {
            return 0;
        }

        if (uri.getPath().equals("/" + rootTable) && selection != null &&
                selection.equals(SecretManagerContract.SEC_ID_FIELD)) {
            /*
            Delete a group of secrets
             */
            int count = 0;

            for (String gsUUID : selectionArgs) {
                if (secretTable.containsKey(gsUUID)) {
                    secretTable.remove(gsUUID);
                    count++;
                }
            }

            cacheStatus = CACHE_TOFLUSH;
            return count;
        }

        String gSecUUID = uri.getPath().substring(1);
        if (!secretTable.containsKey(gSecUUID)) {
            return 0;
        }

        if (selection != null && selection.equals(SecretManagerContract.SEC_KEY_FIELD)) {
                /*
                Delete single properties
                 */
            for (String secName : selectionArgs) {
                GroupOfSecret gSecret = (GroupOfSecret) secretTable.get(gSecUUID);
                gSecret.remove(secName);
                Log.d(TAG, "Deleted property " + secName);
            }
            cacheStatus = CACHE_TOFLUSH;
            return selectionArgs.length;

        }

        return 0;
    }

    @Override
    public String getType(@NonNull Uri uri) {

        /*
        TODO implement
         */
        return null;
    }

    private class CommandReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(SecretManagerContract.INIT_INTENT)) {

                pictureURI = Uri.parse(intent.getStringExtra(SecretManagerContract.PICTURE_FIELD));
                pwd = intent.getStringExtra(SecretManagerContract.PWD_FIELD);
                rootTable = intent.getStringExtra(SecretManagerContract.ROOT_ID_FIELD);

                boolean loadOnInit = intent.getBooleanExtra(SecretManagerContract.LOAD_FIELD, true);

                if (loadOnInit) {

                    try {

                        loadSecrets();
                        statusCode = SecretManagerContract.STATUS_OK;

                    } catch (Exception ex) {
                        Log.e(SecretManager.class.getName(), ex.getMessage(), ex);
                        cacheStatus = CACHE_IDLE;
                        statusCode = SecretManagerContract.STATUS_ERR;
                    }

                } else {

                    initSecrets();
                    statusCode = SecretManagerContract.STATUS_OK;

                }


            } else if (intent.getAction().equals(SecretManagerContract.FLUSH_INTENT)) {

                try {

                    writeSecrets();

                } catch (Exception ex) {
                    Log.e(SecretManager.class.getName(), ex.getMessage(), ex);
                    statusCode = SecretManagerContract.STATUS_ERR;
                }

            }
        }

    }
}