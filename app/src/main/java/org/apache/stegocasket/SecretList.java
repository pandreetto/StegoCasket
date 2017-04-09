package org.apache.stegocasket;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class SecretList extends AppCompatActivity {


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

    }

    public void openSecret(View sView) {
        TextView uView = (TextView) sView.findViewById(R.id.secret_name);
        String sUUID = uView.getHint().toString();
        Intent intent = new Intent(this, SecretCard.class);
        intent.putExtra(CasketConstants.SEC_UUID, sUUID);
        startActivity(intent);
    }

}
