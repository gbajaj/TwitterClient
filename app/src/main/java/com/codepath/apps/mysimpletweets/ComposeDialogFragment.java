package com.codepath.apps.mysimpletweets;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.databinding.ComposeDialogFragmentLayoutBinding;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.models.User_Table;
import com.codepath.apps.mysimpletweets.storage.UserPreferences;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by gauravb on 3/23/17.
 */

public class ComposeDialogFragment extends DialogFragment {
    public static final String TAG = ComposeDialogFragment.class.getSimpleName();
    Context context = TwitterApplication.instance();
    ComposeTweet composeTweet;
    UserPreferences userPreferences = new UserPreferences();
    User currentUser = SQLite.select().from(User.class).where(User_Table.id.eq(userPreferences.getUserId())).querySingle();

    ComposeDialogFragmentLayoutBinding binding;

    interface ComposeTweet {
        void onTweetCreated(Tweet tweet);
    }

    public ComposeDialogFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.compose_dialog_fragment_layout, container, false);
        View view = binding.getRoot();
        if (currentUser != null) {
            Glide.with(context).load(currentUser.getProfileImage()).bitmapTransform(
                    new RoundedCornersTransformation(context, 5, 5)).into(binding.composeDialogProfileImgv);
            binding.composeDialogUserNameTv.setText(currentUser.getName());
            binding.composeDialogScreenNameTv.setText("@" + currentUser.getScreenName());
        }
        String draftTweet = userPreferences.getDraftedTweet();
        binding.composeDialogEdittext.setText(draftTweet);
        updateTweetButton();
        binding.composeDialogTweetCharsCntTv.setText("" + (140 - draftTweet.length()));
        binding.composeDialogCloseImgbtn.setOnClickListener(v -> {
            Editable editable = binding.composeDialogEdittext.getText();
            if (editable.length() > 0) {
                //Save or discard
                final String text = editable.toString();
                new AlertDialog.Builder(getActivity())
                        // set dialog icon
                        // set Dialog Title
                        .setTitle("Save Tweet?")
                        // Set Dialog Message
                        .setMessage("Do you want to save this tweet?")

                        // positive button
                        .setPositiveButton("Save",
                                (dialog, which) -> {
                                    userPreferences.saveDraft(text);
                                    Toast.makeText(context, "Pressed OK", Toast.LENGTH_SHORT).show();
                                })
                        // negative button
                        .setNegativeButton("Discard",
                                (dialog, which) -> {
                                    userPreferences.resetDraft();
                                    Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show();
                                }).create().show();
            }
            dismiss();
        });
        binding.composeDialogTweetBtn.setOnClickListener(v -> {
            Editable editable = binding.composeDialogEdittext.getText();
            TwitterApplication.getRestClient().tweet(editable.toString(), new JsonHttpResponseHandler() {
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Toast.makeText(context, "Tweeted", Toast.LENGTH_SHORT).show();
                    try {
                        Tweet tweet = Tweet.fromJSON(response);
                        ComposeTweet listener = composeTweet;
                        if (listener != null) {
                            listener.onTweetCreated(tweet);
                        }
                        dismiss();
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
                    Toast.makeText(context, "Tweet Failed ", Toast.LENGTH_SHORT).show();
                }
            });
        });

        //inflate view
        // 1) Current's User's information (Close button, Screen Name , user name, and profile pic)
        //2) Edit text
        //3) character count  + Tweet Button to submit
        binding.composeDialogEdittext.addTextChangedListener(new TextWatcher() {
            int count = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: " + s + " start " + start + " count " + count + " " + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Log.d(TAG, "onTextChanged: " + s + " start " + start + " before " + before + " count " + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                int left = 140 - s.toString().length();
                binding.composeDialogTweetCharsCntTv.setText("" + left);
                updateTweetButton();
                Log.d(TAG, "afterTextChanged: " + s + " left " + left);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();

    }

    private void updateTweetButton() {
        if (binding.composeDialogEdittext.getText().length() > 0) {
            binding.composeDialogTweetBtn.setEnabled(true);
        } else {
            binding.composeDialogTweetBtn.setEnabled(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        composeTweet = (ComposeTweet) getActivity();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        composeTweet = null;
    }
}
