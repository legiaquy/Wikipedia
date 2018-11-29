package com.usth.wikipedia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.usth.wikipedia.R;

public class CreateNewAccountActivity extends Activity {
    EditText editUsername, editPass, editRepass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_account);
    }

    public void Log_in(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void Handle_create_account(View view) {
        editUsername = findViewById(R.id.username);
        editPass = findViewById(R.id.password);
        editRepass = findViewById(R.id.confirm_pass);

        String username = editUsername.getText().toString();
        String pass = editPass.getText().toString();
        String repass = editRepass.getText().toString();


        Toast toast = Toast.makeText(CreateNewAccountActivity.this,
                "Account created successfully. Please check your email!", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
