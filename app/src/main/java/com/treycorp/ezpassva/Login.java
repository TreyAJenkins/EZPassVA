package com.treycorp.ezpassva;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class Login extends AppCompatActivity {

    private Button loginButton;
    private EditText usernameBox;
    private EditText passwordBox;

    private String username;
    private String password;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = (Button) findViewById(R.id.login);
        usernameBox = (EditText) findViewById(R.id.username);
        passwordBox = (EditText) findViewById(R.id.password);

        getSupportActionBar().setTitle(getString(R.string.LoginTitle));

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                username = usernameBox.getText().toString();
                password = passwordBox.getText().toString();
                //Toast.makeText(Login.this, username + ":" + password, Toast.LENGTH_LONG).show();
                startLogin();
            }
        });
    }

    private void startLogin() {

        dialog = ProgressDialog.show(Login.this, "Logging in...",
                "Please wait...", true);

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .build();

        Request request = new Request.Builder().url(getString(R.string.AccountURL)).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                dialog.dismiss();
                Toast.makeText(Login.this, "Failed to connect to server, check internet connection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    dialog.dismiss();
                    Login.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(Login.this, "Server responded unexpectedly", Toast.LENGTH_SHORT).show();
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

                            Intent myIntent = new Intent(Login.this, BalanceActivity.class);
                            myIntent.putExtra("JSON", result); //Optional parameters
                            Login.this.startActivity(myIntent);

                        } else {
                            Login.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(Login.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Login.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(Login.this, "Invalid response from server, check internet connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    dialog.dismiss();
                }
            }
        });

    }

}
