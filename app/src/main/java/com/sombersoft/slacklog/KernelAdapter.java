package com.sombersoft.slacklog;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class KernelAdapter extends RecyclerView.Adapter<KernelAdapter.Holder> {

    private ArrayList<String> array;

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvKernel;

        public Holder(View itemView) {
            super(itemView);
            tvKernel = itemView.findViewById(R.id.textViewKernel);
        }
    }

    public KernelAdapter(ArrayList<String> objects) {
        array = objects;
    }

    @NonNull
    @Override
    public KernelAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_kernel, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KernelAdapter.Holder holder, int position) {
        Spanned line = Html.fromHtml(array.get(position));

        if (position == 1) {
            holder.tvKernel.setBackgroundColor(Color.parseColor("#81BEF7"));
            holder.tvKernel.setTextColor(Color.parseColor("#FA5858"));
            holder.tvKernel.setShadowLayer(2, 1, 1, Color.DKGRAY);
        } else if ((position & 1) == 0) {
            holder.tvKernel.setBackgroundColor(Color.parseColor("#81BEF7"));
            holder.tvKernel.setTextColor(Color.WHITE);
            holder.tvKernel.setShadowLayer(2, 1, 1, Color.GRAY);
        } else {
            holder.tvKernel.setBackgroundColor(Color.parseColor("#D8F781"));
            holder.tvKernel.setTextColor(Color.BLACK);
            holder.tvKernel.setShadowLayer(2, 1, 1, Color.WHITE);
        }
        holder.tvKernel.setText(line);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }
}
