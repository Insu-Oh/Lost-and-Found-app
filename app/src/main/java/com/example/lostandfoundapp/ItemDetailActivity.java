package com.example.lostandfoundapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView textViewDetailPostType;
    private TextView textViewDetailName;
    private TextView textViewDetailPhone;
    private TextView textViewDetailDescription;
    private TextView textViewDetailDate;
    private TextView textViewDetailLocation;
    private TextView textViewDetailCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Connect views
        textViewDetailPostType = findViewById(R.id.textViewDetailPostType);
        textViewDetailName = findViewById(R.id.textViewDetailName);
        textViewDetailPhone = findViewById(R.id.textViewDetailPhone);
        textViewDetailDescription = findViewById(R.id.textViewDetailDescription);
        textViewDetailDate = findViewById(R.id.textViewDetailDate);
        textViewDetailLocation = findViewById(R.id.textViewDetailLocation);
        textViewDetailCategory = findViewById(R.id.textViewDetailCategory);

        // Get data from RecyclerView item
        String postType = getIntent().getStringExtra("postType");
        String name = getIntent().getStringExtra("name");
        String phone = getIntent().getStringExtra("phone");
        String description = getIntent().getStringExtra("description");
        String date = getIntent().getStringExtra("date");
        String location = getIntent().getStringExtra("location");
        String category = getIntent().getStringExtra("category");

        // Display item details
        textViewDetailPostType.setText("Post Type: " + postType);
        textViewDetailName.setText("Name: " + name);
        textViewDetailPhone.setText("Phone: " + phone);
        textViewDetailDescription.setText("Description: " + description);
        textViewDetailDate.setText("Date: " + date);
        textViewDetailLocation.setText("Location: " + location);
        textViewDetailCategory.setText("Category: " + category);
    }
}