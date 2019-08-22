package com.sombersoft.slacklog;

import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MultilibAdapter extends RecyclerView.Adapter<MultilibAdapter.ViewHolder> {
    private ArrayList<String> array;

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMultilib;

        ViewHolder(View itemView) {
            super(itemView);
            tvMultilib = itemView.findViewById(R.id.textViewMultilib);
        }
    }

    MultilibAdapter(ArrayList<String> a) {
        array = a;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_multilib,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String line = array.get(position);
        holder.tvMultilib.setTextColor(Color.DKGRAY);
        holder.tvMultilib.setGravity(Gravity.START);
        holder.tvMultilib.setTextSize(15);
        holder.tvMultilib.setBackgroundColor(Color.TRANSPARENT);

        if (line.contains(" UTC ")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.tvMultilib.setElevation(7);
            }
            holder.tvMultilib.setGravity(Gravity.CENTER);
            holder.tvMultilib.setTextSize(16);
            holder.tvMultilib.setTextColor(Color.WHITE);
            holder.tvMultilib.setBackgroundResource(R.color.blue_700);
        }

        if (line.contains("scorri")) {
            holder.tvMultilib.setPadding(0, 50, 0, 0);
            holder.tvMultilib.setTextColor(Color.GRAY);
            holder.tvMultilib.setGravity(Gravity.CENTER);
            holder.tvMultilib.setTextSize(15);
        }
        holder.tvMultilib.setText(line);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }
}