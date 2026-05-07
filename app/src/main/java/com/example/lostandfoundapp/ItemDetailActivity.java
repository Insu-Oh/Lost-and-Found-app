package com.example.lostandfoundapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.lostandfoundapp.database.DatabaseHelper;
import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView textViewDetailPostType;
    private TextView textViewDetailName;
    private TextView textViewDetailPhone;
    private TextView textViewDetailDescription;
    private TextView textViewDetailDate;
    private TextView textViewDetailLocation;
    private TextView textViewDetailCategory;

    private Button buttonRemove;
    private DatabaseHelper databaseHelper;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        itemId = getIntent().getIntExtra("id", -1);
        databaseHelper = new DatabaseHelper(this);

        // Connect views
        textViewDetailPostType = findViewById(R.id.textViewDetailPostType);
        textViewDetailName = findViewById(R.id.textViewDetailName);
        textViewDetailPhone = findViewById(R.id.textViewDetailPhone);
        textViewDetailDescription = findViewById(R.id.textViewDetailDescription);
        textViewDetailDate = findViewById(R.id.textViewDetailDate);
        textViewDetailLocation = findViewById(R.id.textViewDetailLocation);
        textViewDetailCategory = findViewById(R.id.textViewDetailCategory);
        buttonRemove = findViewById(R.id.buttonRemove);

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

        // Call removeItem() when 'REMOVE' button is clicked
        buttonRemove.setOnClickListener(v -> removeItem());
    }

    private void removeItem() {
        if (itemId == -1) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean deleted = databaseHelper.deleteItem(itemId);

        if (deleted) {
            Toast.makeText(this, "Advert removed", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to remove advert", Toast.LENGTH_SHORT).show();
        }
    }
}