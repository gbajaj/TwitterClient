package com.codepath.apps.mysimpletweets;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Toast;

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

public class TimelineActivity extends AppCompatActivity implements ComposeDialogFragment.ComposeTweet, TweetsArrayAdapter.TweetAction {
    RecyclerView recyclerView;
    TweetsArrayAdapter aTweets;
    private ArrayList<Tweet> tweets;
    UserPreferences userPreferences = new UserPreferences();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityTimelineBinding activityTimelineBinding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);

        recyclerView = activityTimelineBinding.rvTweets;
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(this, tweets);
        recyclerView.setAdapter(aTweets);
        // Set layout manager to position the items
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        fetchTweetsSince();
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
        setSupportActionBar(activityTimelineBinding.toolbar);
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
                showToast("Load More");
                if (NetworkConnectivityHelper.isNetworkAvailable() == false) {
//                            notifyNoNetwork();
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

        activityTimelineBinding.composeFab.setOnClickListener(v -> {
            launchCompose(null);
        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activityTimelineBinding.toolbar.setTitle("");
        activityTimelineBinding.toolbar.setSubtitle("");
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

    }

    @Override
    public void onTweetCreated(Tweet tweet) {
        tweets.add(0, tweet);
        tweet.save();
        aTweets.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
    }

    private void showToast(String text) {
        if (TextUtils.isEmpty(text) == false) {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchOlderTweets() {
        TwitterApplication.getRestClient().getOlderTweets("25", "" + (userPreferences.getOldestTweetId() - 1), new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Toast.makeText(TimelineActivity.this, "Passed", Toast.LENGTH_SHORT).show();

                try {
                    tweets.remove(tweets.size() - 1);
                    aTweets.notifyItemRemoved(tweets.size());

                    ArrayList<Tweet> ret = Tweet.fromJSONArray(response);
                    //// TODO: 3/25/17 on worker thread
                    Collections.sort(ret, (o1, o2) -> {
                        return o2.getId().compareTo(o1.getId());
                    });
                    for (Tweet t : ret) {
                        t.getUser().save();
                        t.save();
                    }
                    if (ret != null && ret.isEmpty() == false) {
                        Long val = userPreferences.getOldestTweetId();
                        Tweet tweet = ret.get(ret.size() - 1);
                        if (val != null && tweet.getId() < val) {
                            userPreferences.setOldestTweetId(tweet.getId());
                        }
                    }
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
                Toast.makeText(TimelineActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                tweets.remove(tweets.size() - 1);
                aTweets.notifyItemRemoved(tweets.size());
            }
        });
    }

    private void fetchTweetsSince() {
        TwitterApplication.getRestClient().getTimeline("25", userPreferences.getMostRecentTweetId(), new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Toast.makeText(TimelineActivity.this, "Passed", Toast.LENGTH_SHORT).show();

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
                    tweets.addAll(0, ret);
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
                Toast.makeText(TimelineActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void reply(Tweet tweet) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ComposeDialogFragment.REPLY_TWEET, Parcels.wrap(tweet));
        launchCompose(bundle);
    }

    private void launchCompose(Bundle b) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialogFragment composeDialogFragment = new ComposeDialogFragment();
        if (b != null) {
            composeDialogFragment.setArguments(b);
        }
        composeDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);

        composeDialogFragment.show(fm, "Tag");
    }
}
