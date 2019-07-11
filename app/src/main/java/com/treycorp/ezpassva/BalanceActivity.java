package com.treycorp.ezpassva;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.treycorp.ezpassva.item.DebugItemAdapter;
import com.treycorp.ezpassva.item.TransactionItemAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Handshake;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BalanceActivity extends AppCompatActivity {

    TextView balanceView;
    TextView nameView;
    TextView statusView;
    SwipeRefreshLayout swipeRefreshLayout;

    String balance = "$0.00";
    String name = "--";
    String status = "--";
    String json;

    Drawer drawer;
    PrimaryDrawerItem drawerBalance = new PrimaryDrawerItem().withIdentifier(1).withName("Balance").withTag("balance");//.withIcon(GoogleMaterial.Icon.gmd_monetization_on);
    PrimaryDrawerItem drawerTransactions = new PrimaryDrawerItem().withIdentifier(2).withName("Transactions").withTag("transactions");//.withIcon(GoogleMaterial.Icon.gmd_description);
    SecondaryDrawerItem debugDrawer = new SecondaryDrawerItem().withName("Debug").withTag("debug");

    LocalPlugin localPlugin;
    HashMap<String, String> debugDataset = new HashMap<>();
    ArrayList transDataset = new ArrayList();


    private RecyclerView recyclerView;
    private TransactionItemAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Virginia E-ZPass");

        Intent intent = getIntent();
        json = intent.getStringExtra("JSON");

        //TODO: AUTO RESIZE balanceView
        balanceView = findViewById(R.id.balanceView);
        nameView = (TextView) findViewById(R.id.nameView);
        statusView = (TextView) findViewById(R.id.statusView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        localPlugin = new LocalPlugin();
        localPlugin.init(BalanceActivity.this);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateData(json);
                    }
                }
        );

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this,
         //       DividerItemDecoration.VERTICAL));

        mAdapter = new TransactionItemAdapter(this, transDataset);
        recyclerView.setAdapter(mAdapter);

        int drawerWidth = (int) (localPlugin.getScreenWidth() * .50);
        /*drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        //new DividerDrawerItem(),
                        drawerBalance,
                        drawerTransactions,
                        new DividerDrawerItem(),
                        debugDrawer
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item
                        String tag = drawerItem.getTag().toString();
                        switch (tag) {
                            case "debug":
                                Intent myIntent = new Intent(BalanceActivity.this, DebugActivity.class);
                                myIntent.putExtra("dataset", debugDataset);
                                startActivity(myIntent);
                                break;
                        }
                        return true;
                    }
                })
                //.withDrawerWidthDp(drawerWidth)
                .build();*/

        parseData(json);
    }

    private void updateTransactions(String json) {
        try {
            JSONObject account = new JSONObject(json);
            if (account.getBoolean("success")) {

                JSONArray Transactions = account.getJSONArray("Transactions");

                for (int i = 0; i < Transactions.length(); i++) {
                    JSONObject Transaction = Transactions.getJSONObject(i);

                    String title;
                    String body = "";
                    String toll;
                    String balance;
                    String posted;

                    title = Transaction.getString("Plaza Facility");
                    if (title.equals("")) {
                        title = Transaction.getString("Transaction");
                    } else {
                        body = Transaction.getString("Transaction");
                    }



                    toll = Transaction.getString("Toll Paid");
                    balance = Transaction.getString("Balance");
                    posted = Transaction.getString("Date Posted");

                    HashMap row = new HashMap();
                    row.put("title", title);
                    row.put("body", body);
                    row.put("toll", toll);
                    row.put("balance", balance);
                    row.put("posted", posted);

                    row.put("entryDT", Transaction.getString("Entry Date and Time"));
                    row.put("exitDT", Transaction.getString("Exit Date and Time"));

                    if (Transaction.getString("Entry Date and Time").length() > 0 && Transaction.getString("Exit Date and Time").length() > 0) {
                        row.remove("body");
                        row.put("body", "");
                    } else {
                        if (Transaction.getString("Entry Date and Time").length() > 0) {
                            row.remove("body");
                            row.put("body", Transaction.getString("Entry Date and Time"));
                        } else if (Transaction.getString("Exit Date and Time").length() > 0) {
                            row.remove("body");
                            row.put("body", Transaction.getString("Exit Date and Time"));
                        }
                    }

                    transDataset.add(row);
                }
                mAdapter.notifyDataSetChanged();

            } else {
                BalanceActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(BalanceActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                });
                //TODO: Send back to login screen
            }

        } catch (JSONException e) {
            e.printStackTrace();
            BalanceActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(BalanceActivity.this, "Invalid response from server, check internet connection", Toast.LENGTH_SHORT).show();
                }
            });
            //TODO: Do something? Retry? Send to login?
        }
    }

    private void updateData(String json) {
        JSONObject account = null;
        try {
            account = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject UserInfo = null;
        try {
            UserInfo = account.getJSONObject("ProvidedInfo");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();
        if (localPlugin.getBoolean("Chuck Enabled")) {
            clientBuilder = clientBuilder.addInterceptor(new ChuckerInterceptor(this));
        }
        OkHttpClient client = clientBuilder.build();

        RequestBody requestBody = null;
        try {
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", UserInfo.getString("username"))
                    .addFormDataPart("password", UserInfo.getString("password"))
                    .build();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder().url(getString(R.string.AccountURL)).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Toast.makeText(BalanceActivity.this, "Failed to connect to server, check internet connection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Handshake handshake = response.handshake();
                Certificate cert = handshake.peerCertificates().get(0);
                debugDataset.put("Certificate Type", cert.getType());
                //debugDataset.put("Certificate", cert.toString());



                if (!response.isSuccessful()) {
                    BalanceActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(BalanceActivity.this, "Server responded unexpectedly", Toast.LENGTH_SHORT).show();
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                } else {
                    // do something wih the result
                    String result = response.body().string();


                    //TODO: Remove this
                    //Log.d("Account", result);

                    try {
                        JSONObject account = new JSONObject(result);

                        if (account.getBoolean("success")) {

                            //final JSONObject AccountInfo = account.getJSONObject("AccountInfo");
                            parseData(result);

                        } else {
                            BalanceActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(BalanceActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                                }
                            });
                            //TODO: Return to login?
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        BalanceActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(BalanceActivity.this, "Invalid response from server, check internet connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    private void parseData(final String json) {
        try {
            JSONObject account = new JSONObject(json);
            if (account.getBoolean("success")) {

                JSONObject AccountInfo = account.getJSONObject("AccountInfo");
                balance = AccountInfo.getString("Available Balance");
                name = AccountInfo.getString("Account Name");
                status = AccountInfo.getString("Account Status");

                debugDataset.put("Available Balance", balance);
                debugDataset.put("Account Name", name);
                debugDataset.put("Account Status", status);

                BalanceActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        balanceView.setText(balance);
                        nameView.setText(name);
                        statusView.setText(status);
                        updateTransactions(json);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            } else {
                BalanceActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(BalanceActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                });
                //TODO: Send back to login screen
            }

        } catch (JSONException e) {
            e.printStackTrace();
            BalanceActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(BalanceActivity.this, "Invalid response from server, check internet connection", Toast.LENGTH_SHORT).show();
                }
            });
            //TODO: Do something? Retry? Send to login?
        }

    }
}
