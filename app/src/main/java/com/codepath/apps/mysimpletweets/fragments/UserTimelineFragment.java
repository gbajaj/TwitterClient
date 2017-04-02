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

public class UserTimelineFragment extends TweetsListFragment {

    public static final String USER = "USER";

    User user;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = Parcels.unwrap(getArguments().getParcelable(USER));
    }

    public static UserTimelineFragment newInstance(User user) {

        Bundle args = new Bundle();
        UserTimelineFragment fragment = new UserTimelineFragment();
        args.putParcelable(USER, Parcels.wrap(user));
        fragment.setArguments(args);
        return fragment;
    }

    protected void populateTimeLine(boolean scroll, String maxId, String sinceId, JsonHttpResponseHandler jsonHttpResponseHandler) {
        String screenName = getArguments().getString(USER);
        restClient.getUserTimeLine(screenName, maxId, sinceId, jsonHttpResponseHandler);
    }

    @Override
    protected void addTweet(Tweet tweet) {
        if (tweet.getUser().getId() == user.getId()) {
            super.addTweet(tweet);
        }
    }

    protected void fetchSavedTweets(QueryTransaction.QueryResultCallback<Tweet> callback) {
        //Fetch data from local DB
        SQLite.select()
                .from(Tweet.class)
                .where(Tweet_Table.user_id.is(user.getId()))
                .orderBy(Tweet_Table.id, false)
                .async()
                .queryResultCallback(callback).execute();

    }

}
