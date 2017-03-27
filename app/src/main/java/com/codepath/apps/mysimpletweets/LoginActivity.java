package com.codepath.apps.mysimpletweets;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;

import com.codepath.apps.mysimpletweets.databinding.ActivityLoginBinding;
import com.codepath.oauth.OAuthLoginActionBarActivity;

public class LoginActivity extends OAuthLoginActionBarActivity<TwitterClient> {
    Handler handler = new Handler();
    ActivityLoginBinding activityLoginBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        // Click handler method for the button used to start OAuth flow
        // Uses the client to initiate OAuth authorization
        // This should be tied to a button used to login
        activityLoginBinding.activityLoginButton.setOnClickListener(v -> getClient().connect());

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getClient() != null && getClient().isAuthenticated()) {
            activityLoginBinding.activityLoginButton.setVisibility(View.INVISIBLE);
        }
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    // OAuth authenticated successfully, launch primary authenticated activity
    // i.e Display application "homepage"
    @Override
    public void onLoginSuccess() {
        activityLoginBinding.activityLoginButton.setVisibility(View.INVISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(LoginActivity.this, TimelineActivity.class);
                startActivity(i);
                finish();
            }
        }, 1000);
    }

    // OAuth authentication flow failed, handle the error
    // i.e Display an error dialog or toast
    @Override
    public void onLoginFailure(Exception e) {
        e.printStackTrace();
    }
}
