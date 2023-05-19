package com.example.myapplication.util;

import android.util.Patterns;

import com.google.android.material.textfield.TextInputEditText;

public class Validations {
    public static boolean validateEmptyField(TextInputEditText input) {
        boolean flag = true;
        String value = input.getText().toString();
        if (value.isEmpty()) {
            input.setError("This field is required");
            input.requestFocus();
            flag = false;
        }
        return flag;
    }

    public static boolean validateEmailFormat(TextInputEditText input) {
        boolean flag = true;
        String value = input.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            input.setError("Invalid format");
            input.requestFocus();
            flag = false;
        }
        return flag;
    }
}
