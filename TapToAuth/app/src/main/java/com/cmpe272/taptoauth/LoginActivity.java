package com.cmpe272.taptoauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        APIHandler apiHandler = new APIHandler(getApplicationContext());

        findViewById(R.id.button_login).setOnClickListener(view -> {
            String studentId = ((EditText) findViewById(R.id.field_student_id)).getText().toString();
            String passkey = ((EditText) findViewById(R.id.field_passkey)).getText().toString();
            apiHandler.postAuth(passkey, studentId, obj -> {
                getSharedPreferences("tap-n-auth", MODE_PRIVATE).edit().putString("passkey", passkey).apply();
                startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                return null;
            });
        });
    }
}