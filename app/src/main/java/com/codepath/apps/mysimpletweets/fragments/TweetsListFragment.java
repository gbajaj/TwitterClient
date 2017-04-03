package com.codepath.apps.mysimpletweets.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.adapters.TweetsAdaptersEndlessScrollListener;
import com.codepath.apps.mysimpletweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.databinding.FragmentTweetsListBinding;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.network.helper.NetworkConnectivityHelper;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TweetsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TweetsListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = TweetsListFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String TAG_COMPOSE_FRAGMENT = ComposeDialogFragment.class.getSimpleName();
    protected RecyclerView recyclerView;
    protected TweetsArrayAdapter aTweets;
    protected ArrayList<Tweet> tweets;
    protected TweetsAdaptersEndlessScrollListener tweetsAdaptersEndlessScrollListener;
    protected TwitterClient restClient = TwitterApplication.getRestClient();

    public void setOnScrollListener(RecyclerView.OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    private RecyclerView.OnScrollListener onScrollListener;
    FragmentTweetsListBinding binding;
    Long maxId = Long.MAX_VALUE;
    Long sinceId = 1L;
    HashSet<Long> uniqueTweets = new HashSet<>();

    public TweetsListFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param onScrollListener Parameter 1.
     * @param param2           Parameter 2.
     * @return A new instance of fragment TweetsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TweetsListFragment newInstance(RecyclerView.OnScrollListener onScrollListener, String param2, int position) {


        TweetsListFragment fragment = null;
        if (position == 1) {
            fragment = new HomeTimeLineFragment();
        } else if (position == 2) {
            fragment = new MentionsFragment();
        }
        fragment.setOnScrollListener(onScrollListener);
        return fragment;
    }


    protected void addTweet(Tweet tweet) {
        if (uniqueTweets.contains(tweet.getId()) == false) {
            tweets.add(0, tweet);
            uniqueTweets.add(tweet.getId());
            //tweets.add(0, tweet);
            aTweets.notifyItemInserted(0);
            //Scoll the top of the list
            recyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_tweets_list, container, false);
        recyclerView = binding.rvTweets;
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(getActivity(), (TweetsArrayAdapter.TweetAction) getActivity(), tweets);
        recyclerView.setAdapter(aTweets);
        // Set layout manager to position the items
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        fetchSavedTweets(new QueryTransaction.QueryResultCallback<Tweet>() {
            @Override
            public void onQueryResult(QueryTransaction<Tweet> transaction, @NonNull CursorResult<Tweet> tResult) {
                tweets.clear();
                uniqueTweets.clear();
                addUniqueTweets(tweets, tResult.toList(), true);
                aTweets.notifyDataSetChanged();
            }
        });
        if (NetworkConnectivityHelper.isNetworkAvailable()) {
            //Fetch Fresh Tweets since last time
            populateTimeLine(false, null, "" + sinceId, new TweetResponseHandler(false));
        } else {
            NetworkConnectivityHelper.notifyNoNetwork(getActivity());
        }

        recyclerView.addOnScrollListener((tweetsAdaptersEndlessScrollListener = new TweetsAdaptersEndlessScrollListener(linearLayoutManager) {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (onScrollListener != null) {
                    onScrollListener.onScrolled(recyclerView, dx, dy);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (onScrollListener != null) {
                    onScrollListener.onScrollStateChanged(recyclerView, newState);
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
                if (NetworkConnectivityHelper.isNetworkAvailable()) {
                    populateTimeLine(true, "" + maxId, null, new TweetResponseHandler(true));
                } else {
                    NetworkConnectivityHelper.notifyNoNetwork(getActivity());
                }
            }
        }));

        //Set Brand Icon
        //Added divider between line items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        //Swipe to Refresh
        binding.swipeContainer.setOnRefreshListener(this);
        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void onTweetCreated(Tweet tweet) {
        //a new tweet is just create by the user
        addTweet(tweet);
    }

    protected void notifyNoNetwork() {
        showToast("Network Not Connected");
    }

    private void showToast(String text) {
        if (TextUtils.isEmpty(text) == false) {
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        }
    }

    protected void populateTimeLine(boolean scroll, String maxId, String sinceId, JsonHttpResponseHandler jsonHttpResponseHandler) {

    }

    class TweetResponseHandler extends JsonHttpResponseHandler {
        boolean scroll;

        public TweetResponseHandler(boolean scroll) {
            this.scroll = scroll;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            super.onSuccess(statusCode, headers, response);
            try {
                //Remove the null object to remove the progress bar
                if (scroll) {
                    tweets.remove(tweets.size() - 1);
                    aTweets.notifyItemRemoved(tweets.size());
                }
                ArrayList<Tweet> ret = Tweet.fromJSONArray(response, isMentionFragment());
                if (ret.isEmpty() == false) {
                    Collections.sort(ret, (o1, o2) -> {
                        return o2.getId().compareTo(o1.getId());
                    });

                    if (scroll) {
                        maxId = ret.get(ret.size() - 1).getId() - 1;
                        addUniqueTweets(tweets, ret, false);
                        tweets.addAll(ret);
                    } else {
                        sinceId = ret.get(0).getId();
                        addUniqueTweets(tweets, ret, true);
                        if (maxId == Long.MAX_VALUE) {
                            //init
                            maxId = ret.get(ret.size() - 1).getId() - 1;
                        }
                    }

                    saveRow(ret);
                    aTweets.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            binding.swipeContainer.setRefreshing(false);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            super.onFailure(statusCode, headers, throwable, errorResponse);
            binding.swipeContainer.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        if (NetworkConnectivityHelper.isNetworkAvailable()) {
            populateTimeLine(false, null, "" + sinceId, new TweetResponseHandler(false));
        } else {
            binding.swipeContainer.setRefreshing(false);
            NetworkConnectivityHelper.notifyNoNetwork(getActivity());
        }
    }

    protected void fetchSavedTweets(QueryTransaction.QueryResultCallback<Tweet> callback) {

    }

    //    private removeDuplicates() {}
    private void saveRow(ArrayList<Tweet> list) {
        for (Tweet t : list) {
            if (t.getRetweetUser() != null) {
                t.getRetweetUser().save();
            }
            t.getUser().save();
            t.save();
        }
    }

    private void addUniqueTweets(ArrayList<Tweet> tweetsForAdapter, List<Tweet> newTweets, boolean prepend) {
        ArrayList<Tweet> finalList = new ArrayList<>();
        for (Tweet t : newTweets) {
            if (uniqueTweets.contains(t.getId()) == false) {
                finalList.add(t);
            }
        }
        for (Tweet t : finalList) {
            uniqueTweets.add(t.getId());
        }
        if (prepend) {
            tweetsForAdapter.addAll(0, finalList);
        } else {
            tweetsForAdapter.addAll(finalList);
        }
    }

    protected boolean isMentionFragment() {
        return false;
    }
}
