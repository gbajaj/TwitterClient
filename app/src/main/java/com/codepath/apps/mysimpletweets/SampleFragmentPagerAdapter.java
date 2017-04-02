package com.codepath.apps.mysimpletweets;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.RecyclerView;

import com.codepath.apps.mysimpletweets.fragments.TweetsListFragment;
import com.codepath.apps.mysimpletweets.models.Tweet;

/**
 * Created by gauravb on 3/30/17.
 */

public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[]{"HOME", "MENTIONS"};
    private TweetsListFragment tweetsListFragments[] = new TweetsListFragment[]{null,null};
    private Context context;
    RecyclerView.OnScrollListener onScrollListener;

    public SampleFragmentPagerAdapter(FragmentManager fm, Context context, RecyclerView.OnScrollListener onScrollListener) {
        super(fm);
        this.context = context;
        this.onScrollListener = onScrollListener;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        tweetsListFragments[position] = TweetsListFragment.newInstance(onScrollListener, null, position + 1);
        return tweetsListFragments[position];
    }

    public void addTweet(Tweet tweet) {
        for (TweetsListFragment tweetsListFragment:tweetsListFragments) {
            tweetsListFragment.onTweetCreated(tweet);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
