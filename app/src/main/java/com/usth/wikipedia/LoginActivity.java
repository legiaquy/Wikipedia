package com.usth.wikipedia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends Activity {
    EditText editUsername, editPass;
    String svURL = "https://en.wikipedia.org/w/api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void Create_new_account(View view) {
        Intent intent = new Intent(this, CreateNewAccountActivity.class);
        startActivity(intent);
    }

    public void Forgot_password(View view) {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    public void sendRequestLogin(String url, final String token, final String username, final String pass) {
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);
                        try {
                            JSONObject objUser = new JSONObject(response);
                            JSONObject a = objUser.optJSONObject("clientlogin");
                            String status = "Dsad";
                            Log.d("status", a.toString());

                            if (status == "PASS") {
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                i.putExtra("status", status);
                                i.putExtra("username", username);
                                startActivity(i);
                            } else {
                                String message = objUser.optJSONObject("clientlogin").optString("message");

                                Toast toast = Toast.makeText(LoginActivity.this,
                                        message, Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("LoginActivity", "onErrorResponse");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("username", username);
                params.put("password", pass);
                params.put("logintoken", token);
                params.put("action", "clientlogin");
                params.put("format", "json");
                params.put("loginreturnurl", "https://example.com/");

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    public void Log_in(View view) {
        editUsername = findViewById(R.id.username);
        editPass = findViewById(R.id.password);

        final String username = editUsername.getText().toString();
        final String pass = editPass.getText().toString();

        String authenUrl = svURL + "?action=query&meta=tokens&format=json&type=login";

        StringRequest request = new StringRequest(Request.Method.GET, authenUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String loginURL = svURL;
                            JSONObject obj = new JSONObject(response);
                            String token = obj.optJSONObject("query").optJSONObject("tokens").
                                    optString("logintoken");
                            Log.d("token", token);

                            sendRequestLogin(loginURL, token, username, pass);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("LoginActivity", "onErrorResponse");
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
}
