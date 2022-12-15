package com.cmpe272.taptoauth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.nio.charset.Charset;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static String SSID = "tap-n-auth-ap";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_profile).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String hash;
        if ((hash = getSharedPreferences("tap-n-auth", MODE_PRIVATE).getString("passkey", null)) == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            return;
        }

        APIHandler apiHandler = new APIHandler(getApplicationContext());

        // randomly generated access code
        Random random = new Random();
        String accessCode = random.ints(48, 122 + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(7)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        // [post] send generated access code
        apiHandler.postAddCode(accessCode, hash, obj -> {
            NetworkSpecifier networkSpecifier  = new WifiNetworkSpecifier.Builder()
                    .setSsid(SSID)
                    .setIsHiddenSsid(true)
                    .build();

            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(networkSpecifier)
                    .build();

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            connectivityManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
//                    while (connectivityManager.getActiveNetwork() == null) {
//                        try { Thread.sleep(1000); continue; }
//                        catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    }
                    connectivityManager.bindProcessToNetwork(network);

//                    connectivityManager.getActiveNetworkInfo();

                    // [post] send generated access code to iot
                    apiHandler.postAddCodeIot(accessCode, obj1 -> {
                        // [post] send generated hash to iot
                        apiHandler.postHashIot(
                                hash,
                                obj2 -> {
                                    Toast.makeText(MainActivity.this, "Door is being unlocked ...", Toast.LENGTH_SHORT).show();
                                    return null;
                                },
                                error -> {
                                    Toast.makeText(MainActivity.this, "Invalid user!", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                        return null;
                    });
                }

                @Override
                public void onLosing(@NonNull Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    Toast.makeText(MainActivity.this, "Connection with auth device dropping ...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    Toast.makeText(MainActivity.this, "Connection with auth device dropped", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Log.e("TAP_N_AUTH_AP", "Network unavailable.");
                }
            });

            return null;
        });
    }
}