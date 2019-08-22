package com.sombersoft.slacklog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<String> arrayList;

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvLog;

        ViewHolder(View itemView) {
            super(itemView);
            tvLog = itemView.findViewById(R.id.tvLog);
        }
    }

    ChangelogAdapter(Context c, ArrayList<String> a) {
        mContext = c;
        arrayList = a;
    }

    @NonNull
    @Override
    public ChangelogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                          int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_changelog, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String rawLine = arrayList.get(position);
        /* groups of packages "a/", "ap/", "d/", "e/", "f/", "k/", "kde/", "kdei/", "l/", "n/", "t/", "tcl/", "x/", "xap/", "xfce/", "y/" */
        final String line = rawLine.replace("Upgraded.", "<font color=\"#ff9832\">" + "Upgraded." + "</font><br>")
                .replace("Rebuilt.", "<font color=\"#a155e7\">" + "Rebuilt." + "</font><br>")
                .replace("Added.", "<font color=\"#7cad4c\">" + "Added." + "</font><br>")
                .replace("Removed.", "<font color=\"#fa7266\">" + "Removed." + "</font><br>")
                .replaceAll("(?<!.)a/", "<br><br>" + "a/")
                .replaceAll("(?<!.)ap/", "<br><br>" + "ap/")
                .replaceAll("(?<!.)d/", "<br><br>" + "d/")
                .replaceAll("(?<!.)e/", "<br><br>" + "e/")
                .replaceAll("(?<!.)f/", "<br><br>" + "f/")
                .replaceAll("(?<!.)k/", "<br><br>" + "k/")
                .replaceAll("(?<!.)kde/", "<br><br>" + "kde/")
                .replaceAll("(?<!.)kdei/", "<br><br>" + "kdei/")
                .replaceAll("(?<!.)l/", "<br><br>" + "l/")
                .replaceAll("(?<!.)n/", "<br><br>" + "n/")
                .replaceAll("(?<!.)t/", "<br><br>" + "t/")
                .replaceAll("(?<!.)tcl/", "<br><br>" + "tcl/")
                .replaceAll("(?<!.)x/", "<br><br>" + "x/")
                .replaceAll("(?<!.)xap/", "<br><br>" + "xap/")
                .replaceAll("(?<!.)xfce/", "<br><br>" + "xfce/")
                .replaceAll("(?<!.)y/", "<br><br>" + "y/")
                .replaceAll("(?<!.)extra/", "<br><br>" + "extra/")
                .replaceAll("(?<!.)isolinux/", "<br><br>" + "isolinux/");
        /*
                (?<!.)XXX - match any XXX that is not(!) preceded(?<) by any(.) character (see: regex Lookbehind)
         */

        holder.tvLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                // set changelog date as title
                dialog.setTitle(line.substring(0, line.indexOf("UTC 20") + 8));
                // message of dialog is the changelog list cutting off the date
                dialog.setMessage(Html.fromHtml(line.substring(line.indexOf("UTC 20") + 8)));
                dialog.setPositiveButton(mContext.getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.create();
                //getWindow() is a method of the dialog class, it is needed for having
                // the background of behind activity blurred
                AlertDialog ad = dialog.show();

                WindowManager.LayoutParams layout = Objects.requireNonNull(ad.getWindow())
                        .getAttributes();
                layout.dimAmount = 0.9F;
                ad.getWindow().setAttributes(layout);
                ad.getWindow().addFlags(WindowManager.LayoutParams.DIM_AMOUNT_CHANGED);
            }
        });

        if (line.contains("UTC")) {
            holder.tvLog.setText(line.substring(0, line.indexOf("UTC 20") + 8));
            holder.tvLog.setShadowLayer(3, 1, 1, Color.BLACK);
        } else
            holder.tvLog.setText(line);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
