package org.apache.stegocasket;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.apache.stegocasket.core.SecretManagerContract;

public class SecretCard extends AppCompatActivity {

    private static final String TAG = "SecretCard";

    private SecretCardAdapter secAdapter;

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

        secAdapter = new SecretCardAdapter(this);

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

    public String getSecretUUID() {
        return secretUUID;
    }

    public void useSecret(String key, String value, String className) {
        /*
        TODO implement
        */
    }

    public void changeSecret(String key, String value, String type) {

        final View dialogView = SecretWidgetFactory.getWidget(this, type);

        AlertDialog.Builder builder = new AlertDialog.Builder(SecretCard.this);
        builder.setView(dialogView);

        EditText pNameView = (EditText) dialogView.findViewById(R.id.new_name_cnt);
        pNameView.setText(key);
        pNameView.setInputType(InputType.TYPE_NULL);

        EditText pValueView = (EditText) dialogView.findViewById(R.id.new_value_cnt);
        pValueView.setText(value);
        /*
        TODO set focus on pValueView
         */

        builder.setPositiveButton(R.string.ok_btn, new EditorOnClickListener(dialogView, false));
        builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Nothing to do
            }
        }).show();

    }

    private void sendFlushCommand() {

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.sendBroadcastSync(new Intent(SecretManagerContract.FLUSH_INTENT));

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

                    String secURI = "content://" + SecretManagerContract.AUTHORITY + "/" + secretUUID;

                    int num = SecretCard.this.getContentResolver().delete(Uri.parse(secURI),
                            SecretManagerContract.SEC_KEY_FIELD, new String[]{secName});

                    if (num > 0) {
                        secAdapter.reset();
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

        @Override
        public void onClick(View view) {

            final SecretWidgetFactory.RadioCombo combo = SecretWidgetFactory.getRadioCombo(SecretCard.this);

            AlertDialog.Builder builder = new AlertDialog.Builder(SecretCard.this);
            builder.setView(combo.getView());
            builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    final View dialogView = combo.getSelectedDialog();
                    AlertDialog.Builder eBuilder = new AlertDialog.Builder(SecretCard.this);

                    eBuilder.setView(dialogView);
                    eBuilder.setPositiveButton(R.string.ok_btn, new EditorOnClickListener(dialogView, true));
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

        private boolean insertMode;

        public EditorOnClickListener(View dView, boolean inMode) {

            dialogView = dView;
            insertMode = inMode;
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

            if (insertMode) {

                Uri result = SecretCard.this.getContentResolver().insert(Uri.parse(secURI), values);
                if (result != null) {

                    secAdapter.reset();
                    secAdapter.notifyDataSetChanged();

                }

            } else {

                int result = SecretCard.this.getContentResolver().update(Uri.parse(secURI), values,
                        SecretManagerContract.SEC_KEY_FIELD, new String[]{pNameView.getText().toString()});
                if (result > 0) {

                    secAdapter.reset();
                    secAdapter.notifyDataSetChanged();

                }

            }

        }
    }

}
