package com.treycorp.ezpassva;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.treycorp.ezpassva.item.DebugItemAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class DebugActivity extends AppCompatActivity implements DebugItemAdapter.ItemClickListener, DebugItemAdapter.ItemLongClickListener {


    private RecyclerView recyclerView;
    private DebugItemAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    LocalPlugin localPlugin;

    ArrayList<ArrayList<String>> myDataset = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        localPlugin = new LocalPlugin();
        localPlugin.init(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        add("Chuck", localPlugin.getBoolean("Chuck Enabled") ? "Enabled" : "Disabled");

        Intent intent = getIntent();
        HashMap<String, String> newDataset = (HashMap<String, String>)intent.getSerializableExtra("dataset");
        for (HashMap.Entry<String, String> entry : newDataset.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            add(key, value);
        }


        // specify an adapter (see also next example)
        mAdapter = new DebugItemAdapter(this, myDataset);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setClickListener(this);
        mAdapter.setLongClickListener(this);

    }

    public void add(String key, String value) {
        boolean set = false;
        for(int i=0; i< myDataset.size(); i++) {
            if (key.equals(myDataset.get(i).get(0))) {
                myDataset.set(i, new ArrayList<String>(Arrays.asList(key, value)));
                set = true;
            }
        }
        if (!set) {
            myDataset.add(new ArrayList<String>(Arrays.asList(key, value)));
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "You clicked " + mAdapter.getItem(position).get(0) + " on row number " + position, Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onItemLongClick(View view, int position) {
        //Toast.makeText(this, "You long clicked " + mAdapter.getItem(position).get(0) + " on row number " + position, Toast.LENGTH_SHORT).show();
        switch (mAdapter.getItem(position).get(0)) {
            case "Chuck":
                Boolean newSetting = (!localPlugin.getBoolean("Chuck Enabled"));
                localPlugin.setBoolean("Chuck Enabled", newSetting);
                Toast.makeText(this, "Chuck " + (localPlugin.getBoolean("Chuck Enabled") ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();

                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
        }
    }
}
