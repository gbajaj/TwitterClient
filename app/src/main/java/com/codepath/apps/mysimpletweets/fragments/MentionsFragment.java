package com.codepath.apps.mysimpletweets.fragments;

import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.Tweet_Table;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

/**
 * Created by gauravb on 3/30/17.
 */

public class MentionsFragment extends TweetsListFragment {
    protected void populateTimeLine(boolean scroll, String maxId, String sinceId, JsonHttpResponseHandler jsonHttpResponseHandler) {
        restClient.getMentionTimeline("25", maxId, sinceId, jsonHttpResponseHandler);
    }

    protected void fetchSavedTweets(QueryTransaction.QueryResultCallback<Tweet> callback) {
        //Fetch data from local DB
        SQLite.select()
                .from(Tweet.class)
                .where(Tweet_Table.mentioned.is(true))
                .orderBy(Tweet_Table.id, false)
                .async()
                .queryResultCallback(callback).execute();

    }


    @Override
    protected void addTweet(Tweet tweet) {
        if (tweet.getMentioned()) {
            super.addTweet(tweet);
        }
    }
}
