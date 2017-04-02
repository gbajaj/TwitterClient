package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.SampleFragmentPagerAdapter;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.databinding.ActivityTimelineBinding;
import com.codepath.apps.mysimpletweets.fragments.ComposeDialogFragment;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.models.User_Table;
import com.codepath.apps.mysimpletweets.storage.UserPreferences;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ComposeDialogFragment.ComposeTweet, TweetsArrayAdapter.TweetAction {
    public static final String TAG = TimelineActivity.class.getSimpleName();
    public static final String TAG_COMPOSE_FRAGMENT = ComposeDialogFragment.class.getSimpleName();
    ActivityTimelineBinding activityTimelineBinding;
    UserPreferences userPreferences = new UserPreferences();
    SampleFragmentPagerAdapter sampleFragmentPagerAdapter;
    RecyclerView.OnScrollListener onScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Date binding
        activityTimelineBinding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);
        //Set toolbar
        setSupportActionBar(activityTimelineBinding.toolbar);

        //Compose icon clicked
        activityTimelineBinding.composeFab.setOnClickListener(v -> {
            launchCompose(null);
        });

        //Set Brand Icon
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activityTimelineBinding.toolbar.setTitle("");
        activityTimelineBinding.toolbar.setSubtitle("");
        onScrollListener = new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0 || dy < 0 && activityTimelineBinding.composeFab.isShown()) {
                    activityTimelineBinding.composeFab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    activityTimelineBinding.composeFab.show();
                }
            }
        };
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        sampleFragmentPagerAdapter = new SampleFragmentPagerAdapter(getSupportFragmentManager(),
                TimelineActivity.this, onScrollListener);
        activityTimelineBinding.viewpager.setAdapter(sampleFragmentPagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(activityTimelineBinding.viewpager);

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
        } else {
            User user = SQLite.select()
                    .from(User.class)
                    .where(User_Table.id.is(userPreferences.getUserId()))
                    .querySingle();
            TwitterApplication.instance().setCurrentUser(user);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.timeline_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTweetCreated(Tweet tweet) {
        //a new tweet is just create by the user
        //Save to the local db
        tweet.save();
        //add to the time line and notfity the adapter
        sampleFragmentPagerAdapter.addTweet(tweet);
    }

    public void onProfile(MenuItem mi) {
        Intent i = new Intent(this, ProfileActivity.class);
        User user = TwitterApplication.instance().getCurrentUser();

        i.putExtra(ProfileActivity.USER, Parcels.wrap(user));
        startActivity(i);
    }

    private void notifyNoNetwork() {
        showToast("Network Not Connected");
    }

    private void showToast(String text) {
        if (TextUtils.isEmpty(text) == false) {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
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
    public void reply(Tweet tweet) {
        //Put tweet to be replied in the bundle
        Bundle bundle = new Bundle();
        bundle.putParcelable(ComposeDialogFragment.REPLY_TWEET, Parcels.wrap(tweet));
        //launch compose fragment
        launchCompose(bundle);
    }

    @Override
    public void userSelected(User user) {
        Intent i = new Intent(this, ProfileActivity.class);

        i.putExtra(ProfileActivity.USER, Parcels.wrap(user));
        startActivity(i);
    }


}
