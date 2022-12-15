package com.cmpe272.taptoauth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        APIHandler apiHandler = new APIHandler(getApplicationContext());

        String passkey = getSharedPreferences("tap-n-auth", MODE_PRIVATE).getString("passkey", "");

        apiHandler.getUser(passkey, obj -> {
            try {
                JSONArray array = new JSONArray(obj.toString());
                ((TextView) findViewById(R.id.view_student_id)).setText(array.getString(0));
                ((TextView) findViewById(R.id.view_user)).setText(array.getString(1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        });

        findViewById(R.id.button_logout).setOnClickListener(view -> {
            getSharedPreferences("tap-n-auth", MODE_PRIVATE).edit().remove("passkey").apply();
            finish();
        });

//        findViewById(R.id.button_change_passkey).setOnClickListener(view -> {
//            new AlertDialog.Builder()
//        });
    }
}