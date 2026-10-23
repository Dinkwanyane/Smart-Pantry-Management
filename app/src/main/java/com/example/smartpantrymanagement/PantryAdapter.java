package com.example.smartpantrymanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PantryAdapter
        extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface OnEditClickListener {
        void onEditClick(PantryItem item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(PantryItem item);
    }

    private final List<PantryItem> pantryItems = new ArrayList<>();

    private final OnEditClickListener editClickListener;
    private final OnDeleteClickListener deleteClickListener;

    public PantryAdapter(
            OnEditClickListener editClickListener,
            OnDeleteClickListener deleteClickListener) {

        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
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
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);

        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PantryViewHolder holder,
            int position) {

        PantryItem item = pantryItems.get(position);

        holder.textItemName.setText(item.getName());

        holder.textItemQuantity.setText(
                String.format(
                        Locale.getDefault(),
                        "Quantity: %s %s",
                        String.valueOf(item.getQuantity()),
                        item.getUnit()
                )
        );

        String expiryDate = item.getExpiryDate();

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            holder.textItemExpiry.setText("Expiry date: Not provided");
        } else {
            holder.textItemExpiry.setText("Expiry date: " + expiryDate);
        }

        holder.buttonEditIngredient.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(item);
            }
        });

        holder.buttonDeleteIngredient.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {

        TextView textItemName;
        TextView textItemQuantity;
        TextView textItemExpiry;

        Button buttonEditIngredient;
        Button buttonDeleteIngredient;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);

            textItemName = itemView.findViewById(R.id.textItemName);
            textItemQuantity =
                    itemView.findViewById(R.id.textItemQuantity);
            textItemExpiry =
                    itemView.findViewById(R.id.textItemExpiry);

            buttonEditIngredient =
                    itemView.findViewById(R.id.buttonEditIngredient);
            buttonDeleteIngredient =
                    itemView.findViewById(R.id.buttonDeleteIngredient);
        }
    }
}
