package org.apache.stegocasket;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.stegocasket.core.SecretManagerContract;

import java.util.UUID;

public class CasketLogin extends Activity {

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

        FloatingActionButton fabNew = findViewById(R.id.fab_new);
        assert fabNew != null;
        fabNew.setOnClickListener(new SelectOnClickListener(false));

        FloatingActionButton fabLoad = findViewById(R.id.fab_load);
        assert fabLoad != null;
        fabLoad.setOnClickListener(new SelectOnClickListener(true));
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

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        int layRef = requestCode == LOAD_REQUEST_CODE ? R.layout.dialog_pwd : R.layout.dialog_newpwd;
        final View dialogView = inflater.inflate(layRef, null);
        builder.setView(dialogView);

        builder.setPositiveButton(R.string.ok_btn, new PwdOnClickListener(dialogView, pictureURI,
                requestCode == LOAD_REQUEST_CODE));

        builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Nothing to do
            }
        });

        builder.show();
    }

    private void initProvider(Uri picURI, String pwd, boolean loadMode) {

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
        if (cursor != null) {
            cursor.moveToFirst();
            int colIdx = cursor.getColumnIndex(SecretManagerContract.STATUS_FIELD);
            int statusCode = colIdx > 0 ? cursor.getInt(colIdx) : SecretManagerContract.STATUS_ERR;
            cursor.close();

            if (statusCode == SecretManagerContract.STATUS_OK) {
                Intent intent = new Intent(this, SecretList.class);
                intent.putExtra(CasketConstants.ROOT_UUID, rootUUID);
                startActivity(intent);
            } else {

                showError(R.string.login_failed);

            }
        }

    }

    private void showError(int strRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.err_dialmsg);
        builder.setMessage(strRef);
        builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Nothing to do
            }
        });

        AlertDialog errDialog = builder.create();
        errDialog.show();
    }

    private class SelectOnClickListener implements View.OnClickListener {

        boolean loadMode;

        SelectOnClickListener(boolean mode) {
            loadMode = mode;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");

            if (loadMode) {
                startActivityForResult(intent, LOAD_REQUEST_CODE);
            } else {
                startActivityForResult(intent, NEW_REQUEST_CODE);
            }
        }
    }

    private class PwdOnClickListener implements DialogInterface.OnClickListener {

        private View dialogView;

        private Uri pictureURI;

        private boolean loadMode;

        PwdOnClickListener(View view, Uri pURI, boolean mode) {
            dialogView = view;
            pictureURI = pURI;
            loadMode = mode;
        }

        @Override
        public void onClick(DialogInterface dialog, int id) {

            String password;

            if (loadMode) {
                EditText pwdValue = dialogView.findViewById(R.id.pwd_value);
                password = pwdValue.getText().toString();
            } else {
                EditText pwdValue = dialogView.findViewById(R.id.newpwd_value);
                password = pwdValue.getText().toString();
                EditText rePwdValue = dialogView.findViewById(R.id.repwd_value);
                String confirmPwd = rePwdValue.getText().toString();

                if (!password.equals(confirmPwd)) {
                    showError(R.string.pwd_mismatch);
                    return;
                }
            }

            initProvider(pictureURI, password, loadMode);

        }

    }
}
