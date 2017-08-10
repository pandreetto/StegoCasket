package org.apache.stegocasket;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.apache.stegocasket.core.RenderableSecret;
import org.apache.stegocasket.core.SecretManagerContract;

public class SecretCard extends AppCompatActivity {

    private static final String TAG = "SecretCard";

    private RecyclerView.Adapter secAdapter;

    private String secretUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_card);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabadd);
        assert fab != null;
        fab.setOnClickListener(new ChooserOnClickListener());

        Intent intent = this.getIntent();
        secretUUID = intent.getStringExtra(CasketConstants.SEC_UUID);

        RecyclerView.LayoutManager secLayoutManager = new LinearLayoutManager(this);

        secAdapter = new SecretCardAdapter(this, secretUUID);

        RecyclerView secRecyclerView = (RecyclerView) findViewById(R.id.items_recycler);
        assert secRecyclerView != null;
        secRecyclerView.setLayoutManager(secLayoutManager);
        secRecyclerView.setAdapter(secAdapter);

        CardTouchCallback sCallback = new CardTouchCallback();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(sCallback);
        itemTouchHelper.attachToRecyclerView(secRecyclerView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendFlushCommand();
    }

    public void modifySecret(View sView) {
        /*
        TODO implement modify
         */
    }

    private void sendFlushCommand() {
        String cntPrefix = "content://" + SecretManagerContract.AUTHORITY;
        Uri rootTableURI = Uri.parse(cntPrefix + SecretManagerContract.ROOT_PATH);
        ContentValues values = new ContentValues();
        values.put(SecretManagerContract.CMD_FIELD, SecretManagerContract.FLUSH_CMD);

        if (!TAG.equals("")) {
            // temporary disable flush
            return;
        }

        int result = this.getContentResolver().update(rootTableURI, values, "", new String[0]);
        if (result == 0) {
            /*
            TODO error
             */
        }

    }

    private class CardTouchCallback extends ItemTouchHelper.SimpleCallback {

        public CardTouchCallback() {

            super(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);

        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
            final int position = viewHolder.getAdapterPosition();
            AlertDialog.Builder builder = new AlertDialog.Builder(SecretCard.this);
            builder.setMessage(R.string.ok_delete_sec);

            builder.setPositiveButton(R.string.remove_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    TextView tView = (TextView) viewHolder.itemView.findViewById(R.id.prop_key);
                    String secName = tView.getText().toString();

                    String secURI = "content://" + SecretManagerContract.AUTHORITY+ "/" + secretUUID;

                    int num = SecretCard.this.getContentResolver().delete(Uri.parse(secURI),
                            SecretManagerContract.SEC_KEY_FIELD, new String[]{secName});

                    if (num > 0) {
                        Log.d(TAG, "Deleted item " + secName);
                        secAdapter.notifyItemRemoved(position);
                    }

                }
            }).setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    secAdapter.notifyItemRemoved(position + 1);
                    secAdapter.notifyItemRangeChanged(position, secAdapter.getItemCount());
                }
            }).show();
        }
    }

    private class ChooserOnClickListener implements View.OnClickListener {

        private LayoutInflater inflater;

        public ChooserOnClickListener() {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SecretCard.this);

            final View dialogView = inflater.inflate(R.layout.dialog_sec_chooser, null);
            builder.setView(dialogView);
            builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int rDefId = 0;

                    RadioButton textRadio = (RadioButton) dialogView.findViewById(R.id.textRadioBtn);
                    RadioButton mailRadio = (RadioButton) dialogView.findViewById(R.id.mailRadioBtn);
                    RadioButton callRadio = (RadioButton) dialogView.findViewById(R.id.callRadioBtn);
                    RadioButton linkRadio = (RadioButton) dialogView.findViewById(R.id.linkRadioBtn);

                    if (textRadio.isChecked()) {
                        rDefId = R.layout.dialog_newprop;
                    } else if (mailRadio.isChecked()) {
                        rDefId = R.layout.dialog_newmail;
                    } else if (callRadio.isChecked()) {
                        rDefId = R.layout.dialog_newcall;
                    } else if (linkRadio.isChecked()) {
                        rDefId = R.layout.dialog_newlink;
                    } else {
                        Log.d(TAG, "Unknown radio option");
                        return;
                    }

                    AlertDialog.Builder eBuilder = new AlertDialog.Builder(SecretCard.this);
                    final View dialogView = inflater.inflate(rDefId, null);

                    eBuilder.setView(dialogView);
                    eBuilder.setPositiveButton(R.string.ok_btn, new EditorOnClickListener(dialogView));
                    eBuilder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Nothing to do
                        }
                    }).show();

                }
            }).setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Nothing to do
                }
            }).show();
        }
    }

    private class EditorOnClickListener implements DialogInterface.OnClickListener {

        private View dialogView;

        public EditorOnClickListener(View dView) {
            dialogView = dView;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            EditText pNameView = (EditText) dialogView.findViewById(R.id.new_name_cnt);
            EditText pValueView = (EditText) dialogView.findViewById(R.id.new_value_cnt);

            String secURI = "content://" + SecretManagerContract.AUTHORITY + "/" + secretUUID;
            ContentValues values = new ContentValues();
            values.put(SecretManagerContract.SEC_KEY_FIELD, pNameView.getText().toString());
            values.put(SecretManagerContract.SEC_VALUE_FIELD, pValueView.getText().toString());
            values.put(SecretManagerContract.SEC_TYPE_FIELD, pNameView.getTag().toString());

            Uri result = SecretCard.this.getContentResolver().insert(Uri.parse(secURI), values);
            if (result!=null) {

                secAdapter.notifyDataSetChanged();

            }

        }
    }

}
