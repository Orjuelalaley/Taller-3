package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.AvailableUsersActivity;
import com.example.myapplication.FollowingUserActivity;
import com.example.myapplication.HomeActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AvailableUserAdapter extends ArrayAdapter<User> {
    Context mContext;
    int mResource;

    public AvailableUserAdapter(@NonNull Context context, int resource, ArrayList<User> availableUsers) {
        super(context, resource, availableUsers);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);
        TextView name = convertView.findViewById(R.id.itemName);
        CircleImageView image = convertView.findViewById(R.id.itemImage);
        Button button = convertView.findViewById(R.id.itemButton);
        name.setText(getItem(position).getName() + " " + getItem(position).getLastName());
        if (getItem(position).getProfileImage() != null)
            image.setImageURI(getItem(position).getProfileImage());
        else
            image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.blank_profile_pic));
        getUserIdByEmail(button, getItem(position).getEmail());
        return convertView;
    }

    private void getUserIdByEmail(Button button, String email) {
        FirebaseDatabase.getInstance().getReference("availableUsers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren())
                    if (data.getValue(User.class).getEmail().equals(email))
                        updateUi(button, data.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateUi(Button button, String id) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FollowingUserActivity.class);
                intent.putExtra("followingUserId", id);
                mContext.startActivity(intent);
            }
        });
    }
}
