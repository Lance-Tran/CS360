package com.example.lancetran_firstapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private final Context context;
    private final List<InventoryItem> itemList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public InventoryAdapter(Context context, List<InventoryItem> itemList, OnItemClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grid_row, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);

        holder.textItemName.setText(item.getName());
        holder.textItemQty.setText(String.valueOf(item.getQuantity()));
        holder.textItemLocation.setText(
                (item.getStorageLocation() == null || item.getStorageLocation().isEmpty())
                        ? "General"
                        : item.getStorageLocation()
        );

        // Tap entire row to trigger modal popup
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView textItemName;
        TextView textItemQty;
        TextView textItemLocation;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            textItemQty = itemView.findViewById(R.id.textItemQty);
            textItemLocation = itemView.findViewById(R.id.textItemLocation);
        }
    }
}