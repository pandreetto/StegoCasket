package org.apache.stegocasket;


import android.database.Cursor;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.stegocasket.core.SecretManagerContract;

class SecretCardAdapter extends RecyclerView.Adapter<SecretCardAdapter.ViewHolder> {

    private static final String TAG = SecretCardAdapter.class.getName();

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout secLayout;

        ViewHolder(LinearLayout view) {
            super(view);
            secLayout = view;
        }
    }

    private SecretCard secretCard;

    private Cursor cardCursor;

    SecretCardAdapter(SecretCard ctx) {
        secretCard = ctx;
        cardCursor = null;
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

        loadCursor();
        cardCursor.moveToPosition(position);

        String secKey = cardCursor.getString(cardCursor.getColumnIndex(SecretManagerContract.SEC_KEY_FIELD));
        String secVal = cardCursor.getString(cardCursor.getColumnIndex(SecretManagerContract.SEC_VALUE_FIELD));
        String secCls = cardCursor.getString(cardCursor.getColumnIndex(SecretManagerContract.SEC_TYPE_FIELD));

        TextView kView = (TextView) holder.secLayout.findViewById(R.id.prop_key);
        kView.setText(secKey);
        TextView vView = (TextView) holder.secLayout.findViewById(R.id.prop_value);
        vView.setText(secVal);

        WrapperClickListener wListener = new WrapperClickListener(secKey, secVal, secCls);
        vView.setLongClickable(true);
        vView.setOnClickListener(wListener);
        vView.setOnLongClickListener(wListener);

    }

    @Override
    public int getItemCount() {

        loadCursor();
        return cardCursor.getCount();

    }

    private class WrapperClickListener implements View.OnLongClickListener, View.OnClickListener {

        private String secKey;
        private String secValue;
        private String secClass;

        WrapperClickListener(String kName, String vName, String cName) {
            secKey = kName;
            secValue = vName;
            secClass = cName;
        }

        @Override
        public void onClick(View view) {

            SecretWidgetFactory.useSecret(secretCard, secClass, secKey, secValue);

        }

        @Override
        public boolean onLongClick(View view) {
            secretCard.changeSecret(secKey, secValue, secClass);
            return true;
        }
    }

    public synchronized void reset() {

        /*
        TODO improve
         */
        cardCursor = null;

    }

    private synchronized void loadCursor() {

        if (cardCursor == null) {
            Uri secretTable = Uri.parse("content://" + SecretManagerContract.AUTHORITY + "/"
                    + secretCard.getSecretUUID());
            cardCursor = secretCard.getContentResolver().query(secretTable, new String[]{}, "",
                    new String[]{}, "");
        }
    }
}
