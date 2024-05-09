package com.example.panicalert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final int REQUEST_SMS_PERMISSION = 123;
    private static final int REQUEST_LOCATION_PERMISSION = 124;

    private EditText editText;
    private ImageView log, panic;
    private Button addBtn;
    private Button deleteBtn;
    private ListView listViewContacts;
    private ArrayList<String> contacts = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contactsList;

    private static final String PREFS_NAME = "MyPrefs";
    private static final String PHONE_NUMBERS_KEY = "phoneNumbers";
    private boolean panicClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editTextContact);
        listViewContacts = findViewById(R.id.listViewContacts);
        log = findViewById(R.id.logout);
        panic = findViewById(R.id.get_location_btn);

        addBtn = findViewById(R.id.add_btn);
        deleteBtn = findViewById(R.id.delete_btn);

        contactsList = new ArrayList<>(loadPhoneNumbers());
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        listViewContacts.setAdapter(adapter);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = editText.getText().toString().trim();
                if (!phoneNumber.isEmpty()) {
                    contactsList.add(phoneNumber);
                    savePhoneNumbers(contactsList);
                    adapter.notifyDataSetChanged();
                    editText.setText("");
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = editText.getText().toString().trim();
                if (!phoneNumber.isEmpty()) {
                    if (contactsList.contains(phoneNumber)) {
                        contactsList.remove(phoneNumber);
                        savePhoneNumbers(contactsList);
                        adapter.notifyDataSetChanged();
                        editText.setText("");
                    }
                }
            }
        });

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        panic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panicClicked = true;
                getLastLocation();
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void savePhoneNumbers(List<String> phoneNumbers) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = new HashSet<>(phoneNumbers);
        editor.putStringSet(PHONE_NUMBERS_KEY, set);
        editor.apply();
    }

    private ArrayList<String> loadPhoneNumbers() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(PHONE_NUMBERS_KEY, null);
        if (set == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(set);
        }
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        clearPhoneNumbers();
                        startActivity(new Intent(MainActivity.this, LoginMain.class));
                        finish();
                    }
                })

                .setNegativeButton("No", null)
                .show();
    }

    private void clearPhoneNumbers() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PHONE_NUMBERS_KEY);
        editor.apply();
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (!addresses.isEmpty()) {
                                    String locationText = "Latitude: " + addresses.get(0).getLatitude() +
                                            ", Longitude: " + addresses.get(0).getLongitude() +
                                            ", Address: " + addresses.get(0).getAddressLine(0);

                                    sendLocationToContacts(locationText);
                                } else {
                                    Toast.makeText(MainActivity.this, "Address not found", Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendLocationToContacts(String locationText) {
        if (!panicClicked)
            return;
        for (String phoneNumber : contactsList) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
            } else {
                sendSMS(phoneNumber);
                sendLocationSMS(phoneNumber, locationText);
            }
        }
    }

    private void sendSMS(String phoneNumber) {
        if (!panicClicked)
            return;
        SmsManager smsManager = SmsManager.getDefault();
        String message = "It's Emergency!! I am in Danger!!";
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(this, "Location sent via SMS to " + phoneNumber, Toast.LENGTH_SHORT).show();
    }


    private void sendLocationSMS(String phoneNumber, String locationText) {
        if (!panicClicked)
            return;
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, locationText, null, null);
        Toast.makeText(this, "Message sent via SMS to " + phoneNumber, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Check if the activity is in a valid state to show the dialog
        if (!isFinishing() && !isDestroyed()) {
            // Activity is in a valid state, show the dialog
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Finish the activity when the user clicks "Yes"
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog when the user clicks "No"
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            // Activity is finishing or destroyed, handle back press event without showing the dialog
            super.onBackPressed();
        }
    }
}
