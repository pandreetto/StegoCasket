package org.apache.stegocasket;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SecretCard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_card);

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
        String secretUUID = intent.getStringExtra(CasketConstants.SEC_UUID);

        RecyclerView.LayoutManager secLayoutManager = new LinearLayoutManager(this);

        RecyclerView.Adapter secAdapter = new SecretCardAdapter(this, secretUUID);

        RecyclerView secRecyclerView = (RecyclerView) findViewById(R.id.items_recycler);
        assert secRecyclerView != null;
        secRecyclerView.setLayoutManager(secLayoutManager);
        secRecyclerView.setAdapter(secAdapter);

    }

}
