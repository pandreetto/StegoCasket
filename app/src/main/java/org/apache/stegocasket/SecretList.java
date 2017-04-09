package org.apache.stegocasket;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SecretList extends AppCompatActivity {

    private static final String TAG = "SecretList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                TODO implement
                 */
            }
        });

        Intent intent = this.getIntent();
        String rootUUID = intent.getStringExtra(CasketConstants.ROOT_UUID);

        RecyclerView.LayoutManager secLayoutManager = new LinearLayoutManager(this);

        RecyclerView.Adapter secAdapter = new SecretListAdapter(this, rootUUID);

        RecyclerView secRecyclerView = (RecyclerView) findViewById(R.id.secrets_recycler);
        assert secRecyclerView != null;
        secRecyclerView.setLayoutManager(secLayoutManager);
        secRecyclerView.setAdapter(secAdapter);

        ItemTouchHelper.SimpleCallback sCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                Log.d(TAG, "Called move in sCallback");
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                Log.d(TAG, "Called onSwiped in sCallback");
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(sCallback);
        itemTouchHelper.attachToRecyclerView(secRecyclerView);

    }

    public void openSecret(View sView) {
        TextView uView = (TextView) sView.findViewById(R.id.secret_name);
        String sUUID = uView.getTag().toString();
        Intent intent = new Intent(this, SecretCard.class);
        intent.putExtra(CasketConstants.SEC_UUID, sUUID);
        startActivity(intent);
    }

}
