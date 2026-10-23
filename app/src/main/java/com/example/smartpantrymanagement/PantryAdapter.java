package com.example.smartpantrymanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PantryAdapter
        extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private final List<PantryItem> pantryItems = new ArrayList<>();

    public PantryAdapter() {
    }

    public void setPantryItems(List<PantryItem> items) {
        pantryItems.clear();

        if (items != null) {
            pantryItems.addAll(items);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);

        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PantryViewHolder holder, int position) {

        PantryItem item = pantryItems.get(position);

        holder.textItemName.setText(item.getName());

        holder.textItemQuantity.setText(
                "Quantity: " + item.getQuantity()
                        + " " + item.getUnit());

        String expiryDate = item.getExpiryDate();

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            expiryDate = "Not specified";
        }

        holder.textItemExpiry.setText("Expiry: " + expiryDate);
    }

    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {

        TextView textItemName;
        TextView textItemQuantity;
        TextView textItemExpiry;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);

            textItemName = itemView.findViewById(R.id.textItemName);
            textItemQuantity =
                    itemView.findViewById(R.id.textItemQuantity);
            textItemExpiry =
                    itemView.findViewById(R.id.textItemExpiry);
        }
    }
}
