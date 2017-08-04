package org.apache.stegocasket;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.stegocasket.core.SecretManagerContract;

class SecretListAdapter extends RecyclerView.Adapter<SecretListAdapter.ViewHolder> {

    private static final String TAG = SecretListAdapter.class.getName();

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout secLayout;

        ViewHolder(LinearLayout view) {
            super(view);
            secLayout = view;
        }
    }

    private Cursor intCursor;

    private Context context;

    private String rUUID;

    SecretListAdapter(Context ctx, String rUUID) {

        intCursor = null;
        context = ctx;
        this.rUUID = rUUID;

    }

    @Override
    public SecretListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        LinearLayout lView = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.secretitem, parent, false);
        return new ViewHolder(lView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        loadCursor();

        intCursor.moveToPosition(position);
        String sUUID = intCursor.getString(intCursor.getColumnIndex(SecretManagerContract.SEC_ID_FIELD));
        String sName = intCursor.getString(intCursor.getColumnIndex(SecretManagerContract.SEC_NAME_FIELD));

        TextView tView = (TextView) holder.secLayout.findViewById(R.id.secret_name);
        tView.setText(sName);
        tView.setTag(sUUID);
    }

    @Override
    public int getItemCount() {
        loadCursor();
        return intCursor.getCount();
    }

    public void reset() {

        /*
        TODO find a better way
         */
        intCursor = null;
    }

    private void loadCursor() {

        if (intCursor == null) {
            Uri mainTable = Uri.parse("content://" + SecretManagerContract.AUTHORITY + "/" + rUUID);
            intCursor = context.getContentResolver().query(mainTable, new String[]{}, "", new String[]{}, "");
        }

    }

}