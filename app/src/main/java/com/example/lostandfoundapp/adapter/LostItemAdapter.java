package com.example.lostandfoundapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostandfoundapp.R;
import com.example.lostandfoundapp.data.LostItem;

import java.util.ArrayList;

import android.content.Intent;
import com.example.lostandfoundapp.ItemDetailActivity;

public class LostItemAdapter extends RecyclerView.Adapter<LostItemAdapter.ItemViewHolder> {

    private ArrayList<LostItem> itemList;

    // Constructor receives the list from MainActivity
    public LostItemAdapter(ArrayList<LostItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create one row view for the RecyclerView
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lost_found, parent, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // put data into each row
        LostItem item = itemList.get(position);

        holder.textViewItemName.setText(item.getName());
        holder.textViewPostType.setText(item.getPostType());
        holder.textViewItemCategory.setText(item.getCategory());
        holder.textViewItemDate.setText(item.getDate());

        // open detail screen when item is clicked
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), ItemDetailActivity.class);

            intent.putExtra("id", item.getId());
            intent.putExtra("postType", item.getPostType());
            intent.putExtra("name", item.getName());
            intent.putExtra("phone", item.getPhone());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("date", item.getDate());
            intent.putExtra("location", item.getLocation());
            intent.putExtra("category", item.getCategory());
            intent.putExtra("imageUri", item.getImageUri());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // Holds views for one row item
    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView textViewItemName;
        TextView textViewPostType;
        TextView textViewItemCategory;
        TextView textViewItemDate;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewPostType = itemView.findViewById(R.id.textViewPostType);
            textViewItemCategory = itemView.findViewById(R.id.textViewItemCategory);
            textViewItemDate = itemView.findViewById(R.id.textViewItemDate);
        }
    }
}