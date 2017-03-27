package com.codepath.apps.mysimpletweets.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;

/**
 * Created by gauravb on 3/17/17.
 */

public class UserPreferences {

    SharedPreferences sharedPreferences = TwitterApplication.instance().getDefaultSharedPreferences();
    SharedPreferences.Editor editor = sharedPreferences.edit();
    Context context = TwitterApplication.instance();

    public String getMostRecentTweetId() {
        return sharedPreferences.getString(context.getString(R.string.tweet_data_most_recent_tweet_id), "1");
    }

    public void setMostRecentTweetId(String tweetId) {
        if (TextUtils.isEmpty(tweetId)) {
            throw new IllegalArgumentException("Tweet Id can't be null or empty!");
        }
        editor.putString(context.getString(R.string.tweet_data_most_recent_tweet_id), tweetId);
        editor.commit();
    }

    public Long getOldestTweetId() {
        return sharedPreferences.getLong(context.getString(R.string.tweet_data_oldest_tweet_id), Long.MAX_VALUE);
    }

    public void setOldestTweetId(Long tweetId) {
        if (tweetId == null) {
            throw new IllegalArgumentException("Tweet Id can't be null");
        }
        editor.putLong(context.getString(R.string.tweet_data_oldest_tweet_id), tweetId);
        editor.commit();
    }

    public void resetDraft() {
        editor.putString(context.getString(R.string.tweet_data_draft_tweet), "");
        editor.commit();
    }

    public void saveDraft(String tweetText) {
        editor.putString(context.getString(R.string.tweet_data_draft_tweet), tweetText);
        editor.commit();
    }

    public String getDraftedTweet() {
        return sharedPreferences.getString(context.getString(R.string.tweet_data_draft_tweet), "");
    }

    public void setUserId(long currentUserId) {
        editor.putLong(context.getString(R.string.user_data_current_user_id), currentUserId);
        editor.commit();
    }

    public long getUserId() {
        return sharedPreferences.getLong(context.getString(R.string.user_data_current_user_id), -1);
    }
}
