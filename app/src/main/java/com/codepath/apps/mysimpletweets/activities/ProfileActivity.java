package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.ProfileFragmentsPagerAdapter;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.databinding.ActivityProfileBinding;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.network.helper.NetworkConnectivityHelper;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ProfileActivity extends AppCompatActivity implements TweetsArrayAdapter.TweetAction {
    private ActivityProfileBinding binding;
    public static final String SCREEN_NAME = "USER";
    public static final String USER = "USER";
    TwitterClient restClient = TwitterApplication.getRestClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        final User user = Parcels.unwrap(getIntent().getParcelableExtra(USER));
        populateProfile(user);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        binding.viewpager.setAdapter(new ProfileFragmentsPagerAdapter(getSupportFragmentManager(),
                ProfileActivity.this, user));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(binding.viewpager);
    }

    private void populateProfile(final User user) {
        Glide.with(getApplicationContext())
                .load(user.getProfileBannerUrl()).into(binding.activityProfileUserBannerImage);
        Glide.with(getApplicationContext())
                .load(user.getProfileImage()).into(binding.activityProfileUserImage);
        if (user.isFollowing()) {
            binding.activityProfileFollow.setVisibility(View.GONE);
        } else {
            binding.activityProfileFollowed.setVisibility(View.GONE);
        }
        binding.activityProfileFollow.setOnClickListener(v -> {
            if (NetworkConnectivityHelper.isNetworkAvailable() == false) {
                NetworkConnectivityHelper.notifyNoNetwork(getApplicationContext());
                return;
            }

            binding.activityProfileFollow.setVisibility(View.GONE);
            binding.activityProfileFollowProgressBar.setVisibility(View.VISIBLE);
            restClient.follow("" + user.getId(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    binding.activityProfileFollowProgressBar.setVisibility(View.GONE);
                    binding.activityProfileFollow.setVisibility(View.GONE);
                    binding.activityProfileFollowed.setVisibility(View.VISIBLE);
                    user.setFollowing(true);
                    user.save();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    binding.activityProfileFollowProgressBar.setVisibility(View.GONE);
                    binding.activityProfileFollow.setVisibility(View.VISIBLE);
                    Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }

            });
        });
        binding.activityProfileFollowed.setOnClickListener(v -> {
            if (NetworkConnectivityHelper.isNetworkAvailable() == false) {
                NetworkConnectivityHelper.notifyNoNetwork(getApplicationContext());
                return;
            }
            binding.activityProfileFollowed.setVisibility(View.GONE);
            binding.activityProfileFollowProgressBar.setVisibility(View.VISIBLE);
            restClient.unfollow("" + user.getId(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            binding.activityProfileFollowProgressBar.setVisibility(View.GONE);
                            binding.activityProfileFollow.setVisibility(View.VISIBLE);
                            user.setFollowing(false);
                            user.save();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            binding.activityProfileFollowProgressBar.setVisibility(View.GONE);
                            binding.activityProfileFollowed.setVisibility(View.VISIBLE);
                            Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });
        binding.activityProfileUserDescription.setText(user.getDescription());
        binding.activityProfileFollowerCountTv.setText("" + user.getFollowersCount());
        binding.activityProfileFollowingCountTv.setText("" + user.getFollowingCount());
        binding.activityProfileScreenName.setText("@" + user.getScreenName());
        binding.activityProfileUserName.setText(user.getName());
    }

    @Override
    public void reply(Tweet tweet) {

    }

    @Override
    public void userSelected(User user) {
        final Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra(ProfileActivity.USER, Parcels.wrap(user));
        startActivity(i);
    }
}
