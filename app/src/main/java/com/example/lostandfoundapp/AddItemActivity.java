package com.example.lostandfoundapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.net.Uri;
import android.widget.ImageView;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lostandfoundapp.database.DatabaseHelper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;



public class AddItemActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextDescription;
    private EditText editTextDate, editTextLocation;
    private RadioGroup radioGroupPostType;
    private Spinner spinnerCategory;
    private Button buttonSaveItem;

    private Button buttonSelectImage;
    private ImageView imagePreview;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String[]> imagePickerLauncher;

    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        databaseHelper = new DatabaseHelper(this);

        // Connect XML views to Java variables
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        editTextLocation = findViewById(R.id.editTextLocation);
        radioGroupPostType = findViewById(R.id.radioGroupPostType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        buttonSaveItem = findViewById(R.id.buttonSaveItem);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        imagePreview = findViewById(R.id.imagePreview);

        setupCategorySpinner();

        // Open gallery and keep permission for the selected image
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;

                        // Keep permission to use this image later
                        getContentResolver().takePersistableUriPermission(
                                selectedImageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        // Show selected image preview
                        imagePreview.setImageURI(selectedImageUri);
                    }
                }
        );

        // open image picker when button is clicked
        buttonSelectImage.setOnClickListener(v -> {
            imagePickerLauncher.launch(new String[]{"image/*"});
        });

        buttonSaveItem.setOnClickListener(v -> saveItem());
    }

    private void setupCategorySpinner() {
        // Load category list from strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.item_categories,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void saveItem() {
        // Get selected post type
        int selectedId = radioGroupPostType.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        String postType = selectedRadioButton.getText().toString();

        // Get user input values
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        // validation
        if (name.isEmpty() || phone.isEmpty() || description.isEmpty()
                || date.isEmpty() || location.isEmpty()) {

            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if image was selected
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = databaseHelper.insertItem(
                postType,
                name,
                phone,
                description,
                date,
                location,
                category,
                selectedImageUri.toString()
        );

        if (inserted) {
            Toast.makeText(this, "Advert saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save advert", Toast.LENGTH_SHORT).show();
        }
    }
}