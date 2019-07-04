package com.treycorp.ezpassva;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
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
    PrimaryDrawerItem drawerBalance = new PrimaryDrawerItem().withIdentifier(1).withName("Balance").withIcon(GoogleMaterial.Icon.gmd_monetization_on);
    PrimaryDrawerItem drawerTransactions = new PrimaryDrawerItem().withIdentifier(2).withName("Transactions").withIcon(GoogleMaterial.Icon.gmd_description);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.BalanceTitle));

        Intent intent = getIntent();
        json = intent.getStringExtra("JSON");

        //TODO: AUTO RESIZE balanceView
        balanceView = (TextView) findViewById(R.id.balanceView);
        nameView = (TextView) findViewById(R.id.nameView);
        statusView = (TextView) findViewById(R.id.statusView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);


        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateData(json);
                    }
                }
        );

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        //new DividerDrawerItem(),
                        drawerBalance,
                        drawerTransactions,
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Settings")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        return true;
                    }
                })
                .build();

        parseData(json);

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

        OkHttpClient client = new OkHttpClient();

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
                    Log.d("Account", result);

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

    private void parseData(String json) {
        try {
            JSONObject account = new JSONObject(json);
            if (account.getBoolean("success")) {

                JSONObject AccountInfo = account.getJSONObject("AccountInfo");
                balance = AccountInfo.getString("Available Balance");
                name = AccountInfo.getString("Account Name");
                status = AccountInfo.getString("Account Status");

                BalanceActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        balanceView.setText(balance);
                        nameView.setText(name);
                        statusView.setText(status);
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
