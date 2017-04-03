package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;

import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.Tweet_Table;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import org.parceler.Parcels;

/**
 * Created by gauravb on 4/1/17.
 */

public class FavoritesTweetsFragment extends TweetsListFragment {

    public static final String USER = "USER";
    User user;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = Parcels.unwrap(getArguments().getParcelable(USER));
    }

    public static FavoritesTweetsFragment newInstance(User user) {

        Bundle args = new Bundle();
        FavoritesTweetsFragment fragment = new FavoritesTweetsFragment();
        args.putParcelable(USER, Parcels.wrap(user));
        fragment.setArguments(args);
        return fragment;
    }

    protected void populateTimeLine(boolean scroll, String maxId, String sinceId, JsonHttpResponseHandler jsonHttpResponseHandler) {
        restClient.getFavoriteTimeline(user.getScreenName(), "25", maxId, sinceId, jsonHttpResponseHandler);
    }

    @Override
    protected void addTweet(Tweet tweet) {
        if (tweet.isFavorited()) {
            super.addTweet(tweet);
        }
    }

    protected void fetchSavedTweets(QueryTransaction.QueryResultCallback<Tweet> callback) {
        //Fetch data from local DB
        SQLite.select()
                .from(Tweet.class)
                .where(Tweet_Table.favorited.is(true))
                .and(Tweet_Table.user_id.is(user.getId()))
                .orderBy(Tweet_Table.id, false)
                .async()
                .queryResultCallback(callback).execute();

    }
}
