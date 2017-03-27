package com.codepath.apps.mysimpletweets.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.databinding.ProgressItemBinding;
import com.codepath.apps.mysimpletweets.databinding.TimelineAdapterItemTweetBinding;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.utils.DateHelper;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by gauravb on 3/22/17.
 */

public class TweetsArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = TweetsArrayAdapter.class.getSimpleName();
    List<Tweet> list;
    Context context;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    DateHelper dateHelper = new DateHelper();

    public interface TweetAction {
        void reply(Tweet tweet);
    }

    public TweetsArrayAdapter(Context context, List<Tweet> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == VIEW_ITEM) {
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            TimelineAdapterItemTweetBinding binding =
                    DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.timeline_adapter_item_tweet, parent, false);
            // Return a new holder instance
            viewHolder = new TweetViewHolder(binding);
        } else {
            ProgressItemBinding binding =
                    DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.progress_item, parent, false);

            viewHolder = new ProgressViewHolder(binding);
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof TweetViewHolder) {
            TweetViewHolder holder = (TweetViewHolder) viewHolder;
            final Tweet tweet = list.get(position);
            holder.binding.itemTweetUserName.setText(tweet.getUser().getName());
            holder.binding.itemTweetUserScreenName.setText("@" + tweet.getUser().getScreenName());
            holder.binding.itemTweetRetweetCount.setText("" + tweet.getRetweetCount());
            holder.binding.itemTweetStarredCnt.setText("" + tweet.getUser().getFavouritesCount());
            holder.binding.itemTweetBody.setText(tweet.getBody());

            String time = dateHelper.getRelativeTimeAgo(tweet.getCreatedAt());
            holder.binding.itemTweetRelativeTime.setText(time);
            String profileImage = tweet.getUser().getProfileImage();
            if (TextUtils.isEmpty(profileImage) == false) {
                Glide.with(context).load(profileImage).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Log.d(TAG, "Glide onException: ");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Log.d(TAG, "Glide downloaded : " + model);
                        return false;
                    }
                }).bitmapTransform(
                        new RoundedCornersTransformation(context, 5, 5)).into(holder.binding.itemTweetUserImage);
            }
            holder.binding.itemTweetReplyImgv.setOnClickListener(v -> {
                if (context instanceof TweetAction) {
                    ((TweetAction) context).reply(tweet);
                }
            });
        } else {
            ((ProgressViewHolder) viewHolder).binding.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class TweetViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        TimelineAdapterItemTweetBinding binding;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public TweetViewHolder(TimelineAdapterItemTweetBinding binding) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it


                    // We can access the data within the views
                    /* No longer needed this code As we are Using Chrome Tab now

                    Intent i = new Intent(context, ArticleActivity.class);
                    i.putExtra("article", Parcels.wrap(article));
                    context.startActivity(i);
                    Log.d(TAG, "Message " + article + " clicked");

                    */

                }
            });
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressItemBinding binding;

        public ProgressViewHolder(ProgressItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
