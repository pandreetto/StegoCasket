package org.apache.stegocasket;

import android.content.Context;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

public class SecretWidgetFactory {

    public static View getWidget(Context ctx, String type) {

        int rDefId = 0;
        if (type.equals("PropertySecret")) {
            rDefId = R.layout.dialog_newprop;
        } else if (type.equals("PhoneSecret")) {
            rDefId = R.layout.dialog_newcall;
        } else if (type.equals("MailSecret")) {
            rDefId = R.layout.dialog_newmail;
        } else if (type.equals("LinkSecret")) {
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
}
