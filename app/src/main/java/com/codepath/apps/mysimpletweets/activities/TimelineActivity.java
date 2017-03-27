package com.codepath.apps.mysimpletweets.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.fragments.ComposeDialogFragment;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.adapters.TweetsAdaptersEndlessScrollListener;
import com.codepath.apps.mysimpletweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.databinding.ActivityTimelineBinding;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.Tweet_Table;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.network.helper.NetworkConnectivityHelper;
import com.codepath.apps.mysimpletweets.storage.UserPreferences;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ComposeDialogFragment.ComposeTweet, TweetsArrayAdapter.TweetAction, SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = TimelineActivity.class.getSimpleName();
    public static final String TAG_COMPOSE_FRAGMENT = ComposeDialogFragment.class.getSimpleName();
    RecyclerView recyclerView;
    TweetsArrayAdapter aTweets;
    private ArrayList<Tweet> tweets;
    UserPreferences userPreferences = new UserPreferences();
    ActivityTimelineBinding activityTimelineBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Date binding
        activityTimelineBinding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);
        //Set toolbar
        setSupportActionBar(activityTimelineBinding.toolbar);

        //Set Recyclerview
        recyclerView = activityTimelineBinding.rvTweets;
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(this, tweets);
        recyclerView.setAdapter(aTweets);
        // Set layout manager to position the items
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        //Fetch Fresh Tweets since last time
        fetchTweetsSince();

        //Fetch data from local DB
        SQLite.select()
                .from(Tweet.class)
                .orderBy(Tweet_Table.id, false)
                .async()
                .queryResultCallback(new QueryTransaction.QueryResultCallback<Tweet>() {
                    @Override
                    public void onQueryResult(QueryTransaction<Tweet> transaction, @NonNull CursorResult<Tweet> tResult) {
                        tweets.addAll(tResult.toList());
                        aTweets.notifyDataSetChanged();
                    }
                }).execute();

        if (userPreferences.getUserId() == -1) {
            TwitterApplication.getRestClient().verifyCredentials(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        User currentUser = User.fromJSON(response);
                        userPreferences.setUserId(currentUser.getId());
                        TwitterApplication.instance().setCurrentUser(currentUser);
                        //Save to the datbase
                        currentUser.save();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                }
            });
        }
        recyclerView.addOnScrollListener(new TweetsAdaptersEndlessScrollListener(linearLayoutManager) {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 || dy < 0 && activityTimelineBinding.composeFab.isShown()) {
                    activityTimelineBinding.composeFab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    activityTimelineBinding.composeFab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (NetworkConnectivityHelper.isNetworkAvailable() == false) {
                    notifyNoNetwork();
                    return;
                }
                if (tweets.size() > 0 && tweets.get(tweets.size() - 1) == null) {
                    return;
                }
                tweets.add(null);
                aTweets.notifyItemInserted(tweets.size());
                fetchOlderTweets();
            }
        });

        //Compose icon clicked
        activityTimelineBinding.composeFab.setOnClickListener(v -> {
            launchCompose(null);
        });

        //Set Brand Icon
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activityTimelineBinding.toolbar.setTitle("");
        activityTimelineBinding.toolbar.setSubtitle("");

        //Added divider between line items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        //Swipe to Refresh
        activityTimelineBinding.swipeContainer.setOnRefreshListener(this);
    }

    @Override
    public void onTweetCreated(Tweet tweet) {
        //a new tweet is just create by the user
        //Save to the local db
        tweet.save();

        //add to the time line and notfity the adapter
        tweets.add(0, tweet);
        aTweets.notifyItemInserted(0);

        //Scoll the top of the list
        recyclerView.smoothScrollToPosition(0);
    }

    private void notifyNoNetwork() {
        showToast("Network Not Connected");
    }

    private void showToast(String text) {
        if (TextUtils.isEmpty(text) == false) {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchOlderTweets() {
        TwitterApplication.getRestClient().getOlderTweets("25", "" + (userPreferences.getOldestTweetId() - 1), new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                try {
                    //Remove the null object to remove the progress bar
                    tweets.remove(tweets.size() - 1);
                    aTweets.notifyItemRemoved(tweets.size());
                    ArrayList<Tweet> ret = Tweet.fromJSONArray(response);
                    Collections.sort(ret, (o1, o2) -> {
                        return o2.getId().compareTo(o1.getId());
                    });
                    for (Tweet t : ret) {
                        t.getUser().save();
                        t.save();
                    }

                    //Save the time of the oldest tweet to calculate "max_id"
                    if (ret != null && ret.isEmpty() == false) {
                        Long val = userPreferences.getOldestTweetId();
                        Tweet tweet = ret.get(ret.size() - 1);
                        if (val != null && tweet.getId() < val) {
                            userPreferences.setOldestTweetId(tweet.getId());
                        }
                    }
                    //Add tweets to the time line and notify the adapter
                    tweets.addAll(ret);
                    aTweets.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            /**
             * Returns when request failed
             *
             * @param statusCode    http response status line
             * @param headers       response headers if any
             * @param throwable     throwable describing the way request failed
             * @param errorResponse parsed response if any
             */
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(TimelineActivity.this, "Failed to Load", Toast.LENGTH_SHORT).show();
                //Remove the null object to remove the progress bar
                tweets.remove(tweets.size() - 1);
                aTweets.notifyItemRemoved(tweets.size());
            }
        });
    }

    private void fetchTweetsSince() {
        if (NetworkConnectivityHelper.isNetworkAvailable() == false) {
            notifyNoNetwork();
            return;
        }
        TwitterApplication.getRestClient().getTimeline("25", userPreferences.getMostRecentTweetId(), new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                try {
                    ArrayList<Tweet> ret = Tweet.fromJSONArray(response);
                    //// TODO: 3/25/17 on worket thread
                    Collections.sort(ret, (o1, o2) -> {
                        return o2.getId().compareTo(o1.getId());
                    });

                    for (Tweet t : ret) {
                        t.getUser().save();
                        t.save();
                    }
                    if (ret != null && ret.isEmpty() == false) {
                        userPreferences.setMostRecentTweetId("" + ret.get(0).getId());
                        Long val = userPreferences.getOldestTweetId();
                        Tweet tweet = ret.get(ret.size() - 1);
                        if (val != null && tweet.getId() < val) {
                            userPreferences.setOldestTweetId(tweet.getId());
                        }
                    }

                    //Add the latest tweet to head
                    tweets.addAll(0, ret);
                    aTweets.notifyDataSetChanged();

                    //Stop pull to refresh spinner
                    activityTimelineBinding.swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            /**
             * Returns when request failed
             *
             * @param statusCode    http response status line
             * @param headers       response headers if any
             * @param throwable     throwable describing the way request failed
             * @param errorResponse parsed response if any
             */
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (activityTimelineBinding.swipeContainer.isRefreshing()) {
                    activityTimelineBinding.swipeContainer.setRefreshing(false);
                    Toast.makeText(TimelineActivity.this, "Error Refreshing", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    @Override
    public void reply(Tweet tweet) {
        //Put tweet to be replied in the bundle
        Bundle bundle = new Bundle();
        bundle.putParcelable(ComposeDialogFragment.REPLY_TWEET, Parcels.wrap(tweet));
        //launch compose fragment
        launchCompose(bundle);
    }

    private void launchCompose(Bundle b) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialogFragment composeDialogFragment = new ComposeDialogFragment();
        if (b != null) {
            composeDialogFragment.setArguments(b);
        }
        composeDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
        composeDialogFragment.show(fm, TAG_COMPOSE_FRAGMENT);
    }

    @Override
    public void onRefresh() {
        //User triggered pull to refresh
        fetchTweetsSince();
    }
}
