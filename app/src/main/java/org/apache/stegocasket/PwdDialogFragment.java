package org.apache.stegocasket;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class PwdDialogFragment extends DialogFragment {

    private CasketLogin context;

    private Uri pictureURI;

    private View dialogView;

    private boolean pwdMode;

    public PwdDialogFragment() {
        super();
    }

    public PwdDialogFragment setPictureURI(Uri pURI) {
        pictureURI = pURI;
        return this;
    }

    public PwdDialogFragment setCasketLogin(CasketLogin ctx) {
        context = ctx;
        return this;
    }

    public PwdDialogFragment enablePwdMode() {
        pwdMode = true;
        return this;
    }

    public PwdDialogFragment enableNewPwdMode() {
        pwdMode = false;
        return this;
    }

    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (pwdMode) {
            dialogView = inflater.inflate(R.layout.dialog_pwd, null);
        } else {
            dialogView = inflater.inflate(R.layout.dialog_newpwd, null);
        }

        /*
        TODO inject picture name
         */
        builder.setView(dialogView);

        builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (pwdMode) {
                    EditText pwdValue = (EditText) dialogView.findViewById(R.id.pwd_value);
                    context.run(pictureURI, pwdValue.getText().toString(), true);
                } else {
                    EditText pwdValue = (EditText) dialogView.findViewById(R.id.newpwd_value);
                    EditText repwdValue = (EditText) dialogView.findViewById(R.id.repwd_value);
                    /*
                    TODO implement check
                     */
                    context.run(pictureURI, pwdValue.getText().toString(), false);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Nothing to do
            }
        });

        return builder.create();
    }


}
