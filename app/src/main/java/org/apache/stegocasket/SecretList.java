package org.apache.stegocasket;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SecretList extends AppCompatActivity {

    private RecyclerView secRecyclerView;
    private RecyclerView.LayoutManager secLayoutManager;
    private RecyclerView.Adapter secAdapter;


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

        secLayoutManager = new LinearLayoutManager(this);

        secAdapter = new SecretListAdapter(this, rootUUID);

        secRecyclerView = (RecyclerView) findViewById(R.id.secrets_recycler);
        secRecyclerView.setLayoutManager(secLayoutManager);
        secRecyclerView.setAdapter(secAdapter);

    }

}
