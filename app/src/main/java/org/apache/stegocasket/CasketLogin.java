package org.apache.stegocasket;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.apache.stegocasket.core.SecretManagerContract;

import java.util.UUID;

public class CasketLogin extends AppCompatActivity {

    private static final String TAG = "CasketLogin";

    private static final int LOAD_REQUEST_CODE = 90;

    private static final int NEW_REQUEST_CODE = 91;

    private static final int PERM_REQUEST_CODE = 92;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_casket_login);

        String permTag = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (this.checkSelfPermission(permTag) == PackageManager.PERMISSION_DENIED) {
            if (this.shouldShowRequestPermissionRationale(permTag)) {
                Log.i(TAG, "Need rationale");
            } else {
                this.requestPermissions(new String[]{permTag}, PERM_REQUEST_CODE);
            }
        }

        FloatingActionButton fabNew = (FloatingActionButton) findViewById(R.id.fab_new);
        assert fabNew != null;
        fabNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");

                startActivityForResult(intent, NEW_REQUEST_CODE);
            }
        });

        FloatingActionButton fabLoad = (FloatingActionButton) findViewById(R.id.fab_load);
        assert fabLoad != null;
        fabLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");

                startActivityForResult(intent, LOAD_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode != PERM_REQUEST_CODE || grantResults.length == 0
                || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Cannot work, permissions denied");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_casket_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (resultCode != Activity.RESULT_OK || resultData == null) {
            return;
        }

        if (requestCode != NEW_REQUEST_CODE && requestCode != LOAD_REQUEST_CODE) {
            return;
        }

        Uri pictureURI = resultData.getData();
        Log.i(TAG, "Selected image " + pictureURI.toString());

        PwdDialogFragment pwdDialog = new PwdDialogFragment();
        pwdDialog.setCasketLogin(this).setPictureURI(pictureURI);

        if (requestCode == NEW_REQUEST_CODE) {

            pwdDialog.enableNewPwdMode().show(getFragmentManager(), "NEW_PASSWORD");

        } else {

            pwdDialog.enablePwdMode().show(getFragmentManager(), "ASK_PASSWORD");
        }

    }

    public void run(Uri picURI, String pwd, boolean loadMode) {

        /*
        Initialize or reset the content provider
         */
        String rootUUID = UUID.randomUUID().toString();

        Intent provInit = new Intent(SecretManagerContract.INIT_INTENT);
        provInit.putExtra(SecretManagerContract.PICTURE_FIELD, picURI.toString());
        provInit.putExtra(SecretManagerContract.PWD_FIELD, pwd);
        provInit.putExtra(SecretManagerContract.LOAD_FIELD, loadMode);
        provInit.putExtra(SecretManagerContract.ROOT_ID_FIELD, rootUUID);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.sendBroadcastSync(provInit);

        /*
        Check the status of the content provider
         */
        Uri registerURI = Uri.parse(SecretManagerContract.REGISTER_URI);
        Cursor cursor = this.getContentResolver().query(registerURI, new String[]{}, "", new String[]{}, "");
        cursor.moveToFirst();
        int statusCode = cursor.getInt(cursor.getColumnIndex(SecretManagerContract.STATUS_FIELD));

        if (statusCode == SecretManagerContract.STATUS_OK) {
            Intent intent = new Intent(this, SecretList.class);
            intent.putExtra(CasketConstants.ROOT_UUID, rootUUID);
            startActivity(intent);
        } else {
            /*
            TODO implement dialog error
             */
        }

    }

}
