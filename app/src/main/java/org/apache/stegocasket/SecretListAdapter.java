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

public class SecretListAdapter extends RecyclerView.Adapter<SecretListAdapter.ViewHolder> {

    private static final String TAG = "SecretListAdapter";

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout secLayout;

        public ViewHolder(LinearLayout view) {
            super(view);
            secLayout = view;
        }
    }

    private Cursor intCursor;

    public SecretListAdapter(Context ctx, String rUUID) {

        Uri mainTable = Uri.parse("content://" + SecretManagerContract.AUTHORITY + "/" + rUUID);
        intCursor = ctx.getContentResolver().query(mainTable, new String[]{}, "", new String[]{}, "");

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
        intCursor.moveToPosition(position);
        TextView tView = (TextView) holder.secLayout.findViewById(R.id.secret_id);
        tView.setText(intCursor.getString(1));
    }

    @Override
    public int getItemCount() {
        return intCursor.getCount();
    }

}