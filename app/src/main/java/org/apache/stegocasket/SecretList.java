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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.stegocasket.core.SecretManagerContract;

import java.util.UUID;

public class SecretList extends AppCompatActivity {

    private static final String TAG = "SecretList";

    private RecyclerView secRecyclerView;

    private SecretListAdapter secAdapter;

    private String rootUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new EditorOnClickListener());

        Intent intent = this.getIntent();
        rootUUID = intent.getStringExtra(CasketConstants.ROOT_UUID);

        RecyclerView.LayoutManager secLayoutManager = new LinearLayoutManager(this);

        secAdapter = new SecretListAdapter(this);

        secRecyclerView = (RecyclerView) findViewById(R.id.secrets_recycler);
        assert secRecyclerView != null;
        secRecyclerView.setLayoutManager(secLayoutManager);
        secRecyclerView.setAdapter(secAdapter);

        ItemTouchHelper.SimpleCallback sCallback = new ListTouchCallback();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(sCallback);
        itemTouchHelper.attachToRecyclerView(secRecyclerView);

    }

    public String getRootUUID() {
        return rootUUID;
    }

    public void openSecret(View sView) {
        TextView uView = (TextView) sView.findViewById(R.id.secret_name);
        String sUUID = uView.getTag().toString();
        Intent intent = new Intent(this, SecretCard.class);
        intent.putExtra(CasketConstants.SEC_UUID, sUUID);
        startActivity(intent);
    }

    private void sendFlushCommand() {
        String cntPrefix = "content://" + SecretManagerContract.AUTHORITY;
        Uri rootTableURI = Uri.parse(cntPrefix + SecretManagerContract.ROOT_PATH);
        ContentValues values = new ContentValues();
        values.put(SecretManagerContract.CMD_FIELD, SecretManagerContract.FLUSH_CMD);

        int result = this.getContentResolver().update(rootTableURI, values, "", new String[0]);
        if (result == 0) {
            /*
            TODO error
             */
        }

    }

    private class EditorOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            AlertDialog.Builder builder = new AlertDialog.Builder(SecretList.this);
            final View dialogView = inflater.inflate(R.layout.dialog_newsec, null);

            builder.setView(dialogView);
            builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText secText = (EditText) dialogView.findViewById(R.id.newsec_value);

                    String sgUUID = UUID.randomUUID().toString();
                    String rootURI = "content://" + SecretManagerContract.AUTHORITY + "/" + rootUUID;

                    ContentValues values = new ContentValues();
                    values.put(SecretManagerContract.SEC_ID_FIELD, sgUUID);
                    values.put(SecretManagerContract.SEC_NAME_FIELD, secText.getText().toString());

                    Uri result = SecretList.this.getContentResolver().insert(Uri.parse(rootURI), values);

                    if (result != null) {

                        secAdapter.reset();
                        secAdapter.notifyDataSetChanged();

                        Intent intent = new Intent(SecretList.this, SecretCard.class);
                        intent.putExtra(CasketConstants.SEC_UUID, sgUUID);
                        startActivity(intent);
                    }
                }

            }).setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Nothing to do
                }
            }).show();
        }
    }

    private class ListTouchCallback extends ItemTouchHelper.SimpleCallback {

        public ListTouchCallback() {

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
            AlertDialog.Builder builder = new AlertDialog.Builder(SecretList.this);
            builder.setMessage(R.string.ok_delete_sec);

            builder.setPositiveButton(R.string.remove_btn, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    TextView tView = (TextView) viewHolder.itemView.findViewById(R.id.secret_name);
                    String sgUUID = tView.getTag().toString();

                    String rootURI = "content://" + SecretManagerContract.AUTHORITY + "/" + rootUUID;

                    int num = SecretList.this.getContentResolver().delete(Uri.parse(rootURI),
                            SecretManagerContract.SEC_ID_FIELD, new String[]{sgUUID});

                    if (num > 0) {

                        SecretList.this.sendFlushCommand();

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

}
