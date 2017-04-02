package com.codepath.apps.mysimpletweets.fragments;

import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.Tweet_Table;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

/**
 * Created by gauravb on 3/30/17.
 */

public class HomeTimeLineFragment extends TweetsListFragment {
    protected void populateTimeLine(boolean scroll, String maxId, String sinceId, JsonHttpResponseHandler jsonHttpResponseHandler) {
        restClient.getTimeline("25", maxId, sinceId, jsonHttpResponseHandler);
    }

    @Override
    protected void addTweet(Tweet tweet) {
        super.addTweet(tweet);
        tweets.add(tweet);
        //tweets.add(0, tweet);
        aTweets.notifyItemInserted(0);
        //Scoll the top of the list
        recyclerView.smoothScrollToPosition(0);
    }

    protected void fetchSavedTweets(QueryTransaction.QueryResultCallback<Tweet> callback) {
        //Fetch data from local DB
        SQLite.select()
                .from(Tweet.class)
                .orderBy(Tweet_Table.id, false)
                .async()
                .queryResultCallback(callback).execute();

    }
}
