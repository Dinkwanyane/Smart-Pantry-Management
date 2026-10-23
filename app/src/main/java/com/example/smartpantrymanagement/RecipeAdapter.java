package com.example.smartpantrymanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter
        extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();

    private final OnRecipeClickListener clickListener;

    public RecipeAdapter(OnRecipeClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setRecipes(List<Recipe> newRecipes) {
        recipes.clear();

        if (newRecipes != null) {
            recipes.addAll(newRecipes);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        TextView textView = new TextView(parent.getContext());

        int padding = (int) (
                16 * parent.getResources().getDisplayMetrics().density
        );

        textView.setPadding(padding, padding, padding, padding);
        textView.setTextSize(16);
        textView.setTextColor(
                android.graphics.Color.rgb(15, 23, 42)
        );

        textView.setBackgroundResource(
                android.R.drawable.list_selector_background
        );

        textView.setLayoutParams(
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        return new RecipeViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecipeViewHolder holder,
            int position) {

        Recipe recipe = recipes.get(position);

        holder.textRecipeName.setText(
                recipe.getName() + "\nTap to view cooking instructions"
        );

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        TextView textRecipeName;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            textRecipeName = (TextView) itemView;
        }
    }
}
