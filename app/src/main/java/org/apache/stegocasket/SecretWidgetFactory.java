package org.apache.stegocasket;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

/*
This is the entry point class for
handling different type of secret
 */

public class SecretWidgetFactory {

    private static final String TAG = SecretWidgetFactory.class.getName();

    private static final String PROP_SEC = "PropertySecret";

    private static final String PHONE_SEC = "PhoneSecret";

    private static final String MAIL_SEC = "MailSecret";

    private static final String LINK_SEC = "LinkSecret";

    public static View getWidget(Context ctx, String type) {

        int rDefId = 0;
        if (type.equals(PROP_SEC)) {
            rDefId = R.layout.dialog_newprop;
        } else if (type.equals(PHONE_SEC)) {
            rDefId = R.layout.dialog_newcall;
        } else if (type.equals(MAIL_SEC)) {
            rDefId = R.layout.dialog_newmail;
        } else if (type.equals(LINK_SEC)) {
            rDefId = R.layout.dialog_newlink;
        }

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(rDefId, null);

    }

    public static RadioCombo getRadioCombo(Context ctx) {
        return new RadioCombo(ctx);
    }

    public static class RadioCombo {

        private LayoutInflater inflater;
        private View radioCombo;

        protected RadioCombo(Context ctx) {
            inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            radioCombo = inflater.inflate(R.layout.dialog_sec_chooser, null);
        }

        public View getView() {
            return radioCombo;
        }

        public View getSelectedDialog() {

            RadioButton textRadio = (RadioButton) radioCombo.findViewById(R.id.textRadioBtn);
            RadioButton mailRadio = (RadioButton) radioCombo.findViewById(R.id.mailRadioBtn);
            RadioButton callRadio = (RadioButton) radioCombo.findViewById(R.id.callRadioBtn);
            RadioButton linkRadio = (RadioButton) radioCombo.findViewById(R.id.linkRadioBtn);

            int rDefId = 0;

            if (textRadio.isChecked()) {
                rDefId = R.layout.dialog_newprop;
            } else if (mailRadio.isChecked()) {
                rDefId = R.layout.dialog_newmail;
            } else if (callRadio.isChecked()) {
                rDefId = R.layout.dialog_newcall;
            } else if (linkRadio.isChecked()) {
                rDefId = R.layout.dialog_newlink;
            } else {
                return null;
            }

            return inflater.inflate(rDefId, null);
        }
    }

    public static void useSecret(Context ctx, String type, String key, String value) {

        switch (type) {
            case PROP_SEC:

                ClipboardManager clipBoard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);

                String clipLabel = "StegoCasket: " + key;
                ClipData clipData = ClipData.newPlainText(clipLabel, value);
                clipBoard.setPrimaryClip(clipData);

                Toast.makeText(ctx, clipLabel, Toast.LENGTH_SHORT).show();
                break;

            case PHONE_SEC:

                Uri number = Uri.parse("tel:" + value);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                ctx.startActivity(callIntent);
                break;

            case MAIL_SEC:
                /*
                TODO send intent to mail agent
                 */
                break;

            case LINK_SEC:
                /*
                TODO send intent to browser
                 */
                break;

            default:
                Log.e(TAG, "Unknown type " + type);
        }
    }
}
