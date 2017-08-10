package org.apache.stegocasket;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.stegocasket.core.Secret;
import org.apache.stegocasket.core.SecretManagerContract;

class SecretCardAdapter extends RecyclerView.Adapter<SecretCardAdapter.ViewHolder> {

    private static final String TAG = SecretCardAdapter.class.getName();

    private SecretCard context;

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout secLayout;

        ViewHolder(LinearLayout view) {
            super(view);
            secLayout = view;
        }
    }

    private Cursor cardCursor;

    SecretCardAdapter(SecretCard ctx, String sUUID) {
        context = ctx;
        Uri secretTable = Uri.parse("content://" + SecretManagerContract.AUTHORITY + "/" + sUUID);
        cardCursor = context.getContentResolver().query(secretTable, new String[]{}, "", new String[]{}, "");
    }

    @Override
    public SecretCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        LinearLayout lView = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.secretprop, parent, false);
        return new ViewHolder(lView);
    }

    @Override
    public void onBindViewHolder(SecretCardAdapter.ViewHolder holder, int position) {
        cardCursor.moveToPosition(position);
        TextView kView = (TextView) holder.secLayout.findViewById(R.id.prop_key);
        kView.setText(cardCursor.getString(cardCursor.getColumnIndex(SecretManagerContract.SEC_KEY_FIELD)));
        TextView vView = (TextView) holder.secLayout.findViewById(R.id.prop_value);
        vView.setText(cardCursor.getString(cardCursor.getColumnIndex(SecretManagerContract.SEC_VALUE_FIELD)));
        vView.setLongClickable(true);
        vView.setOnLongClickListener(new EditOnLongClickListener());
    }

    @Override
    public int getItemCount() {
        return cardCursor.getCount();
    }

    private class EditOnLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View view) {
            context.modifySecret(view);
            return true;
        }
    }

}
