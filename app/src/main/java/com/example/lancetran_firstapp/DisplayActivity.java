package com.example.lancetran_firstapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

    private EditText editItemName;
    private EditText editItemQty;
    private EditText editItemLocation;
    private Button buttonAddItem;
    private RecyclerView recyclerViewInventory;

    private DatabaseHelper dbHelper;
    private InventoryAdapter adapter;
    private List<InventoryItem> itemList;

    private final String alertPhoneNumber = "5555215554";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        dbHelper = new DatabaseHelper(this);

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
    }

    private void loadInventoryData() {
        itemList.clear();
        Cursor cursor = dbHelper.getAllItems();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_NAME));
                String sku = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SKU));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOCATION));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTES));

                itemList.add(new InventoryItem(id, name, sku, location, qty, notes));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void handleAddItem() {
        String name = editItemName.getText().toString().trim();
        String qtyString = editItemQty.getText().toString().trim();
        String location = editItemLocation.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter an item name.", Toast.LENGTH_SHORT).show();
            return;
        }

        // QoL: Default Qty to 1 if empty
        int quantity = 1;
        if (!TextUtils.isEmpty(qtyString)) {
            try {
                quantity = Integer.parseInt(qtyString);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity. Defaulted to 1.", Toast.LENGTH_SHORT).show();
                quantity = 1;
            }
        }

        // QoL: Default location to "General" if empty
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
                Toast.makeText(this, "Item name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            int updatedQty = 1;
            if (!TextUtils.isEmpty(updatedQtyStr)) {
                try {
                    updatedQty = Integer.parseInt(updatedQtyStr);
                } catch (NumberFormatException ignored) {}
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(alertPhoneNumber, null, alertMessage, null, null);
                Toast.makeText(this, "Zero stock SMS alert dispatched!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Could not send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, alertMessage, Toast.LENGTH_LONG).show();
        }
    }
}