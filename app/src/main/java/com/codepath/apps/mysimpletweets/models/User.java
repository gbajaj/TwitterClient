package com.codepath.apps.mysimpletweets.models;

import android.text.TextUtils;

import com.codepath.apps.mysimpletweets.storage.MyTwitterDataBase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

/**
 * Created by gauravb on 3/22/17.
 */
/*
    "user": {
            "name": "Raffi Krikorian",
            "profile_sidebar_fill_color": "DDEEF6",
            "profile_background_tile": false,
            "profile_sidebar_border_color": "C0DEED",
            "profile_image_url": "http://a0.twimg.com/profile_images/1270234259/raffi-headshot-casual_normal.png",
            "created_at": "Sun Aug 19 14:24:06 +0000 2007",
            "location": "San Francisco, California",
            "follow_request_sent": false,
            "id_str": "8285392",
            "is_translator": false,
            "profile_link_color": "0084B4",
            "entities": {
            "mediaUrl": {
            "urls": [
            {
            "expanded_url": "http://about.me/raffi.krikorian",
            "mediaUrl": "http://t.co/eNmnM6q",
            "indices": [
            0,
            19
            ],
            "display_url": "about.me/raffi.krikorian"
            }
            ]
            },
            "description": {
            "urls": [

            ]
            }
            },
            "default_profile": true,
            "mediaUrl": "http://t.co/eNmnM6q",
            "contributors_enabled": false,
            "favourites_count": 724,
            "utc_offset": -28800,
            "profile_image_url_https": "https://si0.twimg.com/profile_images/1270234259/raffi-headshot-casual_normal.png",
            "id": 8285392,
            "listed_count": 619,
            "profile_use_background_image": true,
            "profile_text_color": "333333",
            "followers_count": 18752,
            "lang": "en",
            "protected": false,
            "geo_enabled": true,
            "notifications": false,
            "description": "Director of @twittereng's Platform Services. I break things.",
            "profile_background_color": "C0DEED",
            "verified": false,
            "time_zone": "Pacific Time (US & Canada)",
            "profile_background_image_url_https": "https://si0.twimg.com/images/themes/theme1/bg.png",
            "statuses_count": 5007,
            "profile_background_image_url": "http://a0.twimg.com/images/themes/theme1/bg.png",
            "default_profile_image": false,
            "friends_count": 701,
            "following": true,
            "show_all_inline_media": true,
            "screen_name": "raffi"
            },
            "in_reply_to_screen_name": null,
            "in_reply_to_status_id": null
            },
            */
@Parcel
@Table(database = MyTwitterDataBase.class)
public class User extends BaseModel {
    @Column
    String name;
    @Column
    @PrimaryKey
    long id;
    @Column
    String screenName;
    @Column
    String profileImage;
    @Column
    int favouritesCount;
    @Column
    String description;
    @Column
    int followersCount;
    @Column
    int followingCount;
    @Column
    String profileBannerUrl;

    public Boolean isFollowing() {
        return following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }

    @Column
    Boolean following = Boolean.FALSE;


    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public int getFavouritesCount() {
        return favouritesCount;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getDescription() {
        return description;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public String getProfileBannerUrl() {
        return profileBannerUrl;
    }

    public static User fromJSON(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.name = jsonObject.getString("name");
        user.id = jsonObject.getLong("id");
        user.screenName = jsonObject.getString("screen_name");
        user.profileImage = jsonObject.getString("profile_image_url");
        user.favouritesCount = jsonObject.getInt("favourites_count");
        user.description = jsonObject.getString("description");
        user.followersCount = jsonObject.getInt("followers_count");
        user.followingCount = jsonObject.getInt("friends_count");
        if (jsonObject.has("profile_banner_url")) {
            user.profileBannerUrl = jsonObject.getString("profile_banner_url");
        }

        user.following = jsonObject.getBoolean("following");

        return user;
    }

}
