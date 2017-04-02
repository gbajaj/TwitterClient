package com.codepath.apps.mysimpletweets;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
    public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
    public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
    public static final String REST_CONSUMER_KEY = "6XEpqdgmX5OysIwtIuB1uUJB9";       // Change this
    public static final String REST_CONSUMER_SECRET = "SioNNjfQ89MaOLprcYRoE3Si6Y9qwDC7tIsMkg3Cp3GAJsA32p"; // Change this
    public static final String REST_CALLBACK_URL = "oauth://cpsimpletweets"; // Change this (here and in manifest)


    public TwitterClient(Context context) {
        super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
    }

    public void getTimeline(String count, String max_id, String sinceId, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", count);
        if (max_id != null) {
            params.put("max_id", max_id);
        }
        if (sinceId != null) {
            params.put("since_id", sinceId);
        }
        getClient().get(apiUrl, params, httpResponseHandler);
    }

    public void getMentionTimeline(String count, String max_id, String sinceId, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", count);
        if (max_id != null) {
            params.put("max_id", max_id);
        }
        if (sinceId != null) {
            params.put("since_id", sinceId);
        }
        getClient().get(apiUrl, params, httpResponseHandler);
    }
    public void getFavoriteTimeline(String screenName, String count, String max_id, String sinceId, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("favorites/list.json");
        RequestParams params = new RequestParams();
        params.put("count", count);
        if (max_id != null) {
            params.put("max_id", max_id);
        }
        if (sinceId != null) {
            params.put("since_id", sinceId);
        }
        params.put("screen_name", screenName);
        getClient().get(apiUrl, params, httpResponseHandler);
    }

    public void getUserTimeLine(String screenName, String maxId, String sinceId, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");

        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put("screen_name", screenName);
        if (maxId != null) {
            params.put("max_id", maxId);
        }
        if (sinceId != null) {
            params.put("since_id", sinceId);
        }
        getClient().get(apiUrl, params, httpResponseHandler);
    }

    public void verifyCredentials(JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        RequestParams params = new RequestParams();
        getClient().get(apiUrl, params, httpResponseHandler);
    }

    public void tweet(String text, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", text);
        getClient().post(apiUrl, params, httpResponseHandler);
    }

    public void getUserTimeLineSince(String screenName, String sinceId, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");

        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put("screen_name", screenName);
        params.put("since_id", sinceId);
        getClient().get(apiUrl, params, httpResponseHandler);
    }

    public void getUserTimeLineMax(String screenName, String maxid, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");

        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put("screen_name", screenName);
        params.put("max_id", maxid);
        getClient().get(apiUrl, params, httpResponseHandler);
    }

    public void getUserFavoriteTweetsSince(String screenName, String sinceId, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("favorites/list.json");

        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put("screen_name", screenName);
        params.put("since_id", sinceId);
        getClient().get(apiUrl, params, httpResponseHandler);
    }

    public void getUserFavoriteTweetsMaxId(String screenName, String maxid, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("favorites/list.json");

        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put("screen_name", screenName);
        params.put("max_id", maxid);
        getClient().get(apiUrl, params, httpResponseHandler);
    }
    public void follow(String userId, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("friendships/create.json");

        RequestParams params = new RequestParams();
        params.put("user_id", userId);
        params.put("follow", true);
        getClient().post(apiUrl, params, httpResponseHandler);
    }

    public void unfollow(String userId, JsonHttpResponseHandler httpResponseHandler) {
        String apiUrl = getApiUrl("friendships/destroy.json");

        RequestParams params = new RequestParams();
        params.put("user_id", userId);
        getClient().post(apiUrl, params, httpResponseHandler);
    }

}
