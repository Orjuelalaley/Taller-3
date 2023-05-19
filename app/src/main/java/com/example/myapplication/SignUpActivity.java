package com.example.myapplication;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivitySignUpBinding;
import com.example.myapplication.model.User;
import com.example.myapplication.util.Image;
import com.example.myapplication.util.Permissions;
import com.example.myapplication.util.Validations;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    public static final String LOCATION_PERMISSION_NAME = android.Manifest.permission.ACCESS_FINE_LOCATION;
    public static final int LOCATION_PERMISSION_ID = 1;
    ProgressDialog progressDialog;

    private FirebaseAuth auth;
    Button submit;
    Double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        auth = FirebaseAuth.getInstance();
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        submit = binding.buttonSubmit;
        Permissions.requestPermission(SignUpActivity.this, LOCATION_PERMISSION_NAME, "", LOCATION_PERMISSION_ID);
    }

    public void onSubmitClicked(View view) {
        progressDialog.setMessage("Registering user...");
        progressDialog.show();
        binding.buttonSubmit.setVisibility(View.GONE);
        if (validateFields()) {
            getLocation();
            createFirebaseUser(Objects.requireNonNull(binding.editTextEmail.getText()).toString(), Objects.requireNonNull(binding.editTextPassword.getText()).toString());
        }
    }


    private boolean validateFields() {
        return Validations.validateEmptyField(binding.editTextIdentification) &&
                Validations.validateEmptyField(binding.editTextName) &&
                Validations.validateEmptyField(binding.editTextLastName) &&
                Validations.validateEmptyField(binding.editTextEmail) &&
                Validations.validateEmailFormat(binding.editTextEmail) &&
                Validations.validateEmptyField(binding.editTextPassword);
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(SignUpActivity.this, LOCATION_PERMISSION_NAME) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this,
                    location -> {
                        if (location != null && Permissions.permissionGranted(SignUpActivity.this, LOCATION_PERMISSION_NAME)) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                        }
                    });
        }
    }

    private void createFirebaseUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveUser();
            }
        });
    }

    private void saveUser() {
        User user = createObject();
        FirebaseDatabase.getInstance().getReference("users")
                .child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .setValue(user).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        binding.animationView.resumeAnimation();
                        progressDialog.cancel();
                        binding.animationView.setVisibility(View.VISIBLE);
                        binding.animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                            }
                        });
                    }
                    else
                        Toast.makeText(SignUpActivity.this, "Invalid registration", Toast.LENGTH_SHORT).show();
                });
    }


    private User createObject() {
        return new User(
                Objects.requireNonNull(binding.editTextIdentification.getText()).toString(),
                Objects.requireNonNull(binding.editTextName.getText()).toString(),
                Objects.requireNonNull(binding.editTextLastName.getText()).toString(),
                Objects.requireNonNull(binding.editTextEmail.getText()).toString(),
                Objects.requireNonNull(binding.editTextPassword.getText()).toString(),
                latitude,
                longitude
            );
        }
}