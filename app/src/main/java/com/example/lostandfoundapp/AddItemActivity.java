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

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import android.util.Log;


import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {
    // ID for matching the permission dialog response
    private static final int REQ_LOCATION_PERMISSION = 1001;

    private EditText editTextName, editTextPhone, editTextDescription;
    private EditText editTextDate, editTextLocation;
    private RadioGroup radioGroupPostType;
    private Spinner spinnerCategory;
    private Button buttonSaveItem;

    private Button buttonSelectImage;
    private Button buttonCurrentLocation;
    private ImageView imagePreview;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private ActivityResultLauncher<Intent> autocompleteLauncher;
    private FusedLocationProviderClient fusedLocationClient;


    // Where we keep the picked coords until save. saveItem() rejects if these are still 0.
    private double selectedLatitude = 0;
    private double selectedLongitude = 0;

    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        databaseHelper = new DatabaseHelper(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // places SDK needs a one-time init before Autocomplete works.
        // read the key from manifest meta-data so the real key stays in local.properties.
        if (!Places.isInitialized()) {
            try {
                android.content.pm.ApplicationInfo ai = getPackageManager()
                        .getApplicationInfo(getPackageName(),
                                PackageManager.GET_META_DATA);
                String apiKey = ai.metaData.getString("com.google.android.geo.API_KEY");
                Places.initialize(getApplicationContext(), apiKey);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load API key",
                        Toast.LENGTH_SHORT).show();
            }
        }


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
        buttonCurrentLocation = findViewById(R.id.buttonCurrentLocation);


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

        // Catches the place the user picks from the fullscreen Autocomplete screen
        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK
                            && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        editTextLocation.setText(place.getAddress());
                        if (place.getLatLng() != null) {
                            selectedLatitude = place.getLatLng().latitude;
                            selectedLongitude = place.getLatLng().longitude;
                        }
                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR
                            && result.getData() != null) {
                        // Places SDK returns this when something blew up.
                        // Most common cause: API key not authorised or Places API not enabled.
                        Status status = Autocomplete.getStatusFromIntent(result.getData());
                        Log.e("PlacesError", "code=" + status.getStatusCode()
                                + " msg=" + status.getStatusMessage());
                        Toast.makeText(this,
                                "Places error: " + status.getStatusMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
        editTextLocation.setOnClickListener(v -> launchAutocomplete());
        buttonCurrentLocation.setOnClickListener(v -> fetchCurrentLocation());

        buttonSaveItem.setOnClickListener(v -> saveItem());
    }

    private void launchAutocomplete() {
        // Only ask for the fields we need — Places API charges per field
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).build(this);
        autocompleteLauncher.launch(intent);
    }

    private void fetchCurrentLocation() {
        // 1 Permission check — if missing, pop the dialog and bail
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION_PERMISSION);
            return;
        }

        // 2 Grab the cached last known location (fast, occasionally null on fresh emulator)
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        Toast.makeText(this,
                                "Location not available. Try moving outside.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedLatitude = location.getLatitude();
                    selectedLongitude = location.getLongitude();
                    // 3 Reverse-geocode the coords into a readable address
                    try {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        List<Address> addrs = geocoder.getFromLocation(
                                selectedLatitude, selectedLongitude, 1);
                        if (addrs != null && !addrs.isEmpty()) {
                            editTextLocation.setText(addrs.get(0).getAddressLine(0));
                        } else {
                            editTextLocation.setText(
                                    selectedLatitude + ", " + selectedLongitude);
                        }
                    } catch (Exception e) {
                        // Geocoder can throw IOException offline; just show raw coords
                        editTextLocation.setText(
                                selectedLatitude + ", " + selectedLongitude);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_LOCATION_PERMISSION
                && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            // User said OK — re-run so they don't have to tap the button again
            fetchCurrentLocation();
        } else {
            Toast.makeText(this, "Location permission denied",
                    Toast.LENGTH_SHORT).show();
        }
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

        // No coords = can't put a marker on the map, so just block the save
        if (selectedLatitude == 0 && selectedLongitude == 0) {
            Toast.makeText(this,
                    "Please pick a location from autocomplete or current location",
                    Toast.LENGTH_SHORT).show();
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
                selectedImageUri.toString(),
                selectedLatitude,
                selectedLongitude
        );

        if (inserted) {
            Toast.makeText(this, "Advert saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save advert", Toast.LENGTH_SHORT).show();
        }
    }
}