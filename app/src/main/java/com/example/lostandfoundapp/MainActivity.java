package com.example.lostandfoundapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import com.example.lostandfoundapp.adapter.LostItemAdapter;
import com.example.lostandfoundapp.data.LostItem;
import com.example.lostandfoundapp.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewItems;
    private LostItemAdapter adapter;
    private ArrayList<LostItem> itemList;
    private Spinner spinnerFilter;
    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));

        spinnerFilter = findViewById(R.id.spinnerFilter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload list when returning from AddItemActivity
        loadItems();


        setupFilterSpinner();
    }

    private void loadItems() {
        // get all saved adverts from SQL
        itemList = databaseHelper.getAllItems();

        adapter = new LostItemAdapter(itemList);
        recyclerViewItems.setAdapter(adapter);
    }


    // setup category filter spinner
    private void setupFilterSpinner() {

        // Category options
        String[] filterOptions = {
                "All",
                "Electronics",
                "Pets",
                "Wallets",
                "Documents",
                "Keys",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filterOptions
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFilter.setAdapter(adapter);

        // Filter items when category is selected
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view,
                                       int position, long id) {

                String selectedCategory = parent.getItemAtPosition(position).toString();

                if (selectedCategory.equals("All")) {
                    loadItems();
                } else {
                    filterItems(selectedCategory);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    // Filter function
    private void filterItems(String category) {

        ArrayList<LostItem> filteredList = new ArrayList<>();

        // add matching items to filtered list
        for (LostItem item : itemList) {

            if (item.getCategory().equals(category)) {
                filteredList.add(item);
            }
        }

        // update cecyclerview with filtered items
        adapter = new LostItemAdapter(filteredList);
        recyclerViewItems.setAdapter(adapter);
    }


}