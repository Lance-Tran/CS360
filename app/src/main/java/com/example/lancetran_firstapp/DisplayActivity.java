package com.example.lancetran_firstapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class DisplayActivity extends AppCompatActivity implements InventoryAdapter.OnItemClickListener {

    private static final int SMS_PERMISSION_CODE = 101;
    private static final String PREFS_NAME = "InvenTrackerPrefs";
    private static final String KEY_ALERT_PHONE = "sms_alert_phone_number";
    private static final String DEFAULT_ALERT_PHONE = "5555215554";

    private EditText editItemName;
    private EditText editItemQty;
    private EditText editItemLocation;
    private Button buttonAddItem;
    private RecyclerView recyclerViewInventory;

    private DatabaseHelper dbHelper;
    private InventoryAdapter adapter;
    private List<InventoryItem> itemList;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize default SMS recipient in SharedPreferences on first launch
        if (!sharedPreferences.contains(KEY_ALERT_PHONE)) {
            sharedPreferences.edit().putString(KEY_ALERT_PHONE, DEFAULT_ALERT_PHONE).apply();
        }

        editItemName = findViewById(R.id.editItemName);
        editItemQty = findViewById(R.id.editItemQty);
        editItemLocation = findViewById(R.id.editItemLocation);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);

        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        adapter = new InventoryAdapter(this, itemList, this);
        recyclerViewInventory.setAdapter(adapter);

        loadInventoryData();
        checkAndRequestSmsPermission();

        buttonAddItem.setOnClickListener(v -> handleAddItem());

        // Long-press Add button to configure externalized SMS recipient number
        buttonAddItem.setOnLongClickListener(v -> {
            showSmsSettingsDialog();
            return true;
        });
    }

    private void loadInventoryData() {
        itemList.clear();
        itemList.addAll(dbHelper.getAllInventoryItems());
        adapter.notifyDataSetChanged();
    }

    private void handleAddItem() {
        String name = editItemName.getText().toString().trim();
        String qtyString = editItemQty.getText().toString().trim();
        String location = editItemLocation.getText().toString().trim();

        // Defensive validation: Require non-empty item name
        if (TextUtils.isEmpty(name)) {
            editItemName.setError("Item name is required");
            editItemName.requestFocus();
            Toast.makeText(this, "Please enter an item name.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Defensive validation: Require valid non-negative integer quantity
        if (TextUtils.isEmpty(qtyString)) {
            editItemQty.setError("Quantity is required");
            editItemQty.requestFocus();
            Toast.makeText(this, "Please enter a valid quantity.", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyString);
            if (quantity < 0) {
                editItemQty.setError("Quantity cannot be negative");
                editItemQty.requestFocus();
                Toast.makeText(this, "Quantity must be 0 or greater.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            editItemQty.setError("Enter a whole number");
            editItemQty.requestFocus();
            Toast.makeText(this, "Invalid quantity. Please enter a numeric value.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Default optional location cleanly
        if (TextUtils.isEmpty(location)) {
            location = "General";
        }

        boolean isInserted = dbHelper.insertItem(name, "N/A", location, quantity, "");

        if (isInserted) {
            Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();
            editItemName.setText("");
            editItemQty.setText("");
            editItemLocation.setText("");
            loadInventoryData();

            if (quantity == 0) {
                triggerZeroStockAlert(name, location);
            }
        } else {
            Toast.makeText(this, "Failed to add item.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(InventoryItem item) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_item, null);
        dialog.setContentView(dialogView);

        EditText editDialogName = dialogView.findViewById(R.id.editDialogName);
        EditText editDialogQty = dialogView.findViewById(R.id.editDialogQty);
        EditText editDialogLocation = dialogView.findViewById(R.id.editDialogLocation);
        Button buttonDialogSave = dialogView.findViewById(R.id.buttonDialogSave);
        Button buttonDialogDelete = dialogView.findViewById(R.id.buttonDialogDelete);
        Button buttonCloseDialog = dialogView.findViewById(R.id.buttonCloseDialog);

        editDialogName.setText(item.getName());
        editDialogQty.setText(String.valueOf(item.getQuantity()));
        editDialogLocation.setText(item.getStorageLocation());

        buttonCloseDialog.setOnClickListener(v -> dialog.dismiss());

        buttonDialogSave.setOnClickListener(v -> {
            String updatedName = editDialogName.getText().toString().trim();
            String updatedQtyStr = editDialogQty.getText().toString().trim();
            String updatedLoc = editDialogLocation.getText().toString().trim();

            if (TextUtils.isEmpty(updatedName)) {
                editDialogName.setError("Item name cannot be empty");
                editDialogName.requestFocus();
                Toast.makeText(this, "Item name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(updatedQtyStr)) {
                editDialogQty.setError("Quantity is required");
                editDialogQty.requestFocus();
                Toast.makeText(this, "Please enter a quantity.", Toast.LENGTH_SHORT).show();
                return;
            }

            int updatedQty;
            try {
                updatedQty = Integer.parseInt(updatedQtyStr);
                if (updatedQty < 0) {
                    editDialogQty.setError("Quantity cannot be negative");
                    editDialogQty.requestFocus();
                    Toast.makeText(this, "Quantity must be 0 or greater.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                editDialogQty.setError("Enter a whole number");
                editDialogQty.requestFocus();
                Toast.makeText(this, "Invalid quantity. Enter a valid number.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(updatedLoc)) {
                updatedLoc = "General";
            }

            boolean updated = dbHelper.updateItemDetails(
                    item.getId(),
                    updatedName,
                    item.getSku(),
                    updatedLoc,
                    updatedQty,
                    item.getNotes()
            );

            if (updated) {
                Toast.makeText(this, "Item updated!", Toast.LENGTH_SHORT).show();
                loadInventoryData();
                if (updatedQty == 0) {
                    triggerZeroStockAlert(updatedName, updatedLoc);
                }
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDialogDelete.setOnClickListener(v -> {
            boolean deleted = dbHelper.deleteItem(item.getId());
            if (deleted) {
                Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
                loadInventoryData();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Delete failed.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private String getAlertPhoneNumber() {
        return sharedPreferences.getString(KEY_ALERT_PHONE, DEFAULT_ALERT_PHONE);
    }

    private void showSmsSettingsDialog() {
        EditText inputPhone = new EditText(this);
        inputPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        inputPhone.setText(getAlertPhoneNumber());

        new AlertDialog.Builder(this)
                .setTitle("SMS Alert Recipient")
                .setMessage("Configure phone number for zero-stock alerts:")
                .setView(inputPhone)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newNumber = inputPhone.getText().toString().trim();
                    if (TextUtils.isEmpty(newNumber) || !newNumber.matches("\\+?[0-9]{7,15}")) {
                        Toast.makeText(this, "Enter a valid phone number (7-15 digits).", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sharedPreferences.edit().putString(KEY_ALERT_PHONE, newNumber).apply();
                    Toast.makeText(this, "SMS alert number updated.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS notifications enabled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied. In-app alerts enabled.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void triggerZeroStockAlert(String itemName, String location) {
        String alertMessage = "ALERT: Stock for " + itemName + " (" + location + ") is empty!";
        String recipientPhone = getAlertPhoneNumber();

        if (TextUtils.isEmpty(recipientPhone)) {
            Toast.makeText(this, "No SMS recipient configured. " + alertMessage, Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(recipientPhone, null, alertMessage, null, null);
                Toast.makeText(this, "Zero stock SMS alert dispatched!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Could not send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, alertMessage, Toast.LENGTH_LONG).show();
        }
    }
}