package com.sombersoft.slacklog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ForumTopicAdapter extends RecyclerView.Adapter<ForumTopicAdapter.ViewHolder> {
    private ArrayList<String> array;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTopic;
        private TextView tvAuthor;
        private TextView tvDate;
        private TextView tvPosts;
        private Drawable drawableDailyTopic;
        private Drawable drawableTopic;

        public ViewHolder(View v) {
            super(v);
            tvTopic = v.findViewById(R.id.tvTopic);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvDate = v.findViewById(R.id.tvDate);
            tvPosts = v.findViewById(R.id.tvnPosts);
        }
    }

    public ForumTopicAdapter(Context c, ArrayList<String> a) {
        mContext = c;
        array = a;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_topic, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get the date current in the form 'day month number, year'
        DateFormat df = new SimpleDateFormat("EEE MMM dd, yyyy",
                Locale.getDefault());
        String date = df.format(Calendar.getInstance().getTime());
        holder.tvTopic.setShadowLayer(1, 0, 0, Color.BLACK);
        final String topic = array.get(position);
        if (!topic.contains(mContext.getString(R.string.downswipe))) {
            holder.tvTopic.setText(topic.substring(0, topic.indexOf("@")));
            // create the Listener to open the link on tap
            holder.tvTopic.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = topic.substring(topic.indexOf("@@") + 2, topic.indexOf("@@@"));
                    Uri lastTopicUri = Uri.parse("https://slacky.eu/forum" +
                            url.replace("./", "/"));
                    Log.d("ADAPTER", "LAST_TOPIC_URI: " + lastTopicUri.toString());
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, lastTopicUri));
                }
            });
            holder.tvPosts.setText(topic.substring(topic.indexOf("@@@") + 3, topic.indexOf("@@@@")));
            holder.tvPosts.setShadowLayer(3, 0, 0, Color.BLACK);
            holder.tvAuthor.setText(topic.substring(topic.indexOf("@@@@") + 4, topic.indexOf("@@@@@")));
            holder.tvAuthor.setShadowLayer(2, 0, 0, Color.BLACK);
            String getPostDateFromTopic;
            try {
                getPostDateFromTopic = topic.substring(topic.indexOf("@") + 1, topic.indexOf("@@"));
            } catch (StringIndexOutOfBoundsException sbe) {
                sbe.printStackTrace();
                getPostDateFromTopic = "????";
            }
            holder.tvDate.setText(getPostDateFromTopic);
            holder.tvDate.setShadowLayer(2, 0, 0, Color.BLACK);
            if (getPostDateFromTopic.contains(date)) {
                // creating a drawable object to use with setCompoundDrawables
                // method to support sdk 9 compatibility
                holder.drawableDailyTopic = mContext.getResources().getDrawable(R.drawable.topic_daily, null);
                // Specify a bounding rectangle for the Drawable. This is where the
                // drawable will draw when its draw() method is called
                if (holder.drawableDailyTopic != null) {
                    holder.drawableDailyTopic.setBounds(0, 0,
                            holder.drawableDailyTopic.getIntrinsicWidth(),
                            holder.drawableDailyTopic.getIntrinsicHeight());
                }
                holder.tvTopic.setCompoundDrawables(holder.drawableDailyTopic, null, null, null);
            } else {
                // creating a drawable object to use with setCompoundDrawables
                // method to support sdk 9 compatibility
                holder.drawableTopic = mContext.getResources().getDrawable(R.drawable.topic_img, null);
                // Specify a bounding rectangle for the Drawable. This is where the
                // drawable will draw when its draw() method is called
                if (holder.drawableTopic != null) {
                    holder.drawableTopic.setBounds(0, 0, holder.drawableTopic.getIntrinsicWidth(),
                            holder.drawableTopic.getIntrinsicHeight());
                }
                holder.tvTopic.setCompoundDrawables(holder.drawableTopic, null, null, null);
            }
        } else {
            holder.tvTopic.setGravity(Gravity.CENTER);
            holder.tvTopic.setText(topic);
            holder.tvDate.setVisibility(View.GONE);
            holder.tvPosts.setVisibility(View.GONE);
            holder.tvAuthor.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return array.size();
    }
}