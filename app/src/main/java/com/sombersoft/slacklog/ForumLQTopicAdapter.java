package com.sombersoft.slacklog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

public class ForumLQTopicAdapter extends RecyclerView.Adapter<ForumLQTopicAdapter.ViewHolder> {

    private ArrayList<String> array;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTopic;
        private TextView tvAuthor;
        private TextView tvLastDate;
        private TextView tvLastAuthor;
        private TextView tvSummary;
        private TextView tvnPosts;

        public ViewHolder(View v) {
            super(v);

            tvTopic = v.findViewById(R.id.tvTopic);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvLastDate = v.findViewById(R.id.tvLastDate);
            tvLastAuthor = v.findViewById(R.id.tvLastAuthor);
            tvSummary = v.findViewById(R.id.tvSummary);
            tvnPosts = v.findViewById(R.id.tvnPosts);
        }
    }

    public ForumLQTopicAdapter(Context c, ArrayList<String> a) {

        mContext = c;
        this.array = a;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_topic_lq, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final String topic = array.get(position);

        if (!topic.contains(mContext.getString(R.string.downswipe))) {

            final String threadTitle = topic.substring(topic.indexOf("id=\"thread_title_"));
            holder.tvTopic.setText(threadTitle.substring(threadTitle.indexOf("\">") + 2, threadTitle.indexOf("</a>")));
            holder.tvTopic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // opening thread from the start
                    String lineWithUrl = topic.substring(topic.indexOf("<a href=\"/questions"));
                    Document doc = Jsoup.parse(lineWithUrl);
                    Element url = doc.select("a").first();
                    String relHref = url.attr("href");
                    Uri uriFomUrl = Uri.parse("https://www.linuxquestions.org" + relHref);
                    Intent openUrl = new Intent(Intent.ACTION_VIEW, uriFomUrl);
                    //Log.d("ONCLICK", "URL: http://www.linuxquestions.org" + uriFomUrl.toString());
                    mContext.startActivity(openUrl);
                }
            });
            if (topic.contains("Replies:"))  // sometimes topics are moved in other forum thread, number of topics can't be retrieved
                holder.tvnPosts.setText(topic.substring(topic.indexOf("Replies: ") + 9, topic.indexOf(", Views:")));
            else
                holder.tvnPosts.setText("-");
            String creator = topic.substring(topic.indexOf("'_self')\">") + 10);
            holder.tvAuthor.setText(creator.substring(0, creator.indexOf("</span>")));

            // if topic is moved, doesn't contain a valid date nor last user who posted, and without any check app may crash
            // even summary is empty
            if (topic.contains("<div class=\"smallfont\" style=\"text-align:right; white-space:nowrap\">")) {
                holder.tvSummary.setText(topic.substring(topic.indexOf("title=\"") + 7, topic.indexOf("<div>") - 6));
                String lastDate = topic.substring(topic.indexOf("<div class=\"smallfont\" style=\"text-align:right; white-space:nowrap\">"));
                holder.tvLastDate.setText((lastDate.substring(lastDate.indexOf(">") + 1,
                        lastDate.indexOf("<span class=\"time\">"))).trim());
                holder.tvLastDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String lineWithUrl = topic.substring(topic.indexOf("href=\"/questions/user/"));
                        Document doc = Jsoup.parse(lineWithUrl);
                        Element url = doc.select("a").first();
                        String relHref = url.attr("href");
                        //Log.d("ONCLICK", "https://www.linuxquestions.org" + relHref);

                        Uri uriFomUrl = Uri.parse("https://www.linuxquestions.org" + relHref);
                        Intent openUrl = new Intent(Intent.ACTION_VIEW, uriFomUrl);
                        mContext.startActivity(openUrl);
                    }
                });

                String lastAuthor = topic.substring(topic.indexOf("href=\"/questions/user/"));
                holder.tvLastAuthor.setText(lastAuthor.substring(lastAuthor.indexOf(">") + 1, lastAuthor.indexOf("</a>")));

            } else {
                holder.tvLastDate.setText("-");
                holder.tvLastAuthor.setText("-");
                holder.tvSummary.setText("");
            }
            // no file exists so make it all disappear
        } else {
            holder.tvTopic.setGravity(Gravity.CENTER);
            holder.tvTopic.setText(topic);
            holder.tvLastDate.setVisibility(View.GONE);
            holder.tvnPosts.setVisibility(View.GONE);
            holder.tvAuthor.setVisibility(View.GONE);
            holder.tvLastAuthor.setVisibility(View.GONE);
            holder.tvSummary.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return array.size();
    }
}
