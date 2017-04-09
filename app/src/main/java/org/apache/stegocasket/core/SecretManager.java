package org.apache.stegocasket.core;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import java.util.LinkedHashMap;
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

    private LinkedHashMap<String, Secret> secretTable;

    private int cacheStatus;

    public SecretManager() {
        cacheStatus = CACHE_IDLE;
    }

    private void initSecrets() {
        secretTable = new LinkedHashMap<String, Secret>();
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

            secretTable = new LinkedHashMap<String, Secret>(secretList.size());
            for (Secret tmpsec : secretList) {
                secretTable.put(UUID.randomUUID().toString(), tmpsec);
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

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        if (!uri.getAuthority().equals(SecretManagerContract.AUTHORITY)) {
            throw new IllegalArgumentException();
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
            for (Secret tmpsec : gSecrets) {
                RenderableSecret rSecret = (RenderableSecret) tmpsec;
                cursor.addRow(new Object[]{
                        rSecret.getId(),
                        rSecret.getValue(),
                        "" // TODO missing type
                });
            }
            return cursor;
        }

        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int update(@NonNull Uri uri,
                      ContentValues values,
                      String selection,
                      String[] selectionArgs) {

        if (!uri.getAuthority().equals(SecretManagerContract.AUTHORITY)) {
            return 0;
        }

        if (uri.getPath().equals("/root")) {

            pictureURI = Uri.parse(values.getAsString(SecretManagerContract.PICTURE_FIELD));
            pwd = values.getAsString(SecretManagerContract.PWD_FIELD);
            boolean loadOnInit = values.getAsBoolean(SecretManagerContract.LOAD_FIELD);
            rootTable = values.getAsString(SecretManagerContract.ROOT_ID_FIELD);

            try {

                if (loadOnInit) {
                    loadSecrets();
                } else {
                    initSecrets();
                }

                return 1;
            } catch (Exception ex) {
                Log.e(SecretManager.class.getName(), ex.getMessage(), ex);
            }

        }

        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

}