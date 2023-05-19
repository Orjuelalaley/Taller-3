package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ActivityMainBinding binding;
    TextInputEditText email, password;
    Button submit;
    TextView signUp;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        email = binding.editTextEmail;
        password = binding.editTextPassword;
        submit = binding.buttonSubmit;
        signUp = binding.textSignUp;
        isUserSignedIn(auth.getCurrentUser());
    }

    public void onSignInClicked(View view) {
        if (validateIfFieldIsEmpty(email) && validateIfFieldIsEmpty(password))
            firebaseAuthentication(Objects.requireNonNull(email.getText()).toString(), Objects.requireNonNull(password.getText()).toString());
    }

    public void onSignUpClicked(View view) {
        startActivity(new Intent(MainActivity.this, SignUpActivity.class));
    }

    private boolean validateIfFieldIsEmpty(TextInputEditText input) {
        boolean flag = true;
        String value = Objects.requireNonNull(input.getText()).toString();
        if (value.isEmpty()) {
            input.setError("Please write something");
            input.requestFocus();
            flag = false;
        }
        return flag;
    }

    private void firebaseAuthentication(String email, String password) {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) updateUi(auth.getCurrentUser());
            else
                Toast.makeText(MainActivity.this, "Authentication failure", Toast.LENGTH_SHORT).show();
        });
    }

    private void isUserSignedIn(FirebaseUser user) {
        if (user != null)
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
    }

    private void updateUi(FirebaseUser user) {
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()){
                        progressDialog.dismiss();
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    }
                }
            });
        } else {
           binding.editTextEmail.setText("");
           binding.editTextPassword.setText("");
        }
    }
}