package com.example.panicalert;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.panicalert.databinding.ActivitySignupMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupMain extends AppCompatActivity {

    private ActivitySignupMainBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        binding.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = binding.signupuser.getText().toString().trim();
                String phone = binding.signupphone.getText().toString().trim();
                String email = binding.signupemail.getText().toString().trim();
                String password = binding.signuppassword.getText().toString().trim();

                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(phone) ||
                        TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(SignupMain.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidEmail(email)) {
                    return;
                }

                progressDialog.setMessage("Signing Up...");
                progressDialog.show();

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                progressDialog.dismiss();
                                addUserToFirestore(user, phone, email);
                                startActivity(new Intent(SignupMain.this, LoginMain.class));
                                finish(); // Finish this activity so user can't go back to signup screen
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(SignupMain.this, "Sign Up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        binding.loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupMain.this, LoginMain.class));
                finish(); // Finish this activity so user can't go back to signup screen
            }
        });
    }

    private boolean isValidEmail(String email) {
        // Use regex to check if email is in the correct format
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.endsWith("@gmail.com")) {
            return true;
        } else {
            Toast.makeText(SignupMain.this, "Invalid Email Format!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void addUserToFirestore(String user, String phone, String email) {
        // Add user details to Firestore
        UserModel userModel = new UserModel(user, phone, email);
        firebaseFirestore.collection("User")
                .document(firebaseAuth.getUid())
                .set(userModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully added user to Firestore
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add user to Firestore
                        Toast.makeText(SignupMain.this, "Failed to add user to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
