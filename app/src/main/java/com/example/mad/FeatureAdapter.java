package com.example.mad;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.ViewHolder> {
    private List<FeatureModel> featureList;
    private OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(int position); }

    public FeatureAdapter(List<FeatureModel> featureList, OnItemClickListener listener) {
        this.featureList = featureList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feature, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeatureModel item = featureList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.ivIcon.setImageResource(item.getIconRes());

        // Apply dynamic colors
        holder.itemView.findViewById(R.id.featureCard).setBackgroundColor(item.getBgColor());
        holder.ivIcon.setColorFilter(item.getAccentColor()); // Tints the icon
        holder.tvTitle.setTextColor(item.getAccentColor());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() { return featureList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}