package com.codepath.apps.mysimpletweets.models;

import com.codepath.apps.mysimpletweets.storage.MyTwitterDataBase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by gauravb on 3/22/17.
 */
@Table(database = MyTwitterDataBase.class)
@Parcel(analyze = {Mention.class})
public class Mention extends BaseModel {
    @Column
    String body;

    public String getBody() {
        return body;
    }

    public Long getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Column
    @PrimaryKey
    Long id;
    @ForeignKey(saveForeignKeyModel = false)
    User user;
    @Column
    String createdAt;
    @Column
    boolean favorited;
    @Column
    int retweetCount;
    @Column
    Boolean hasRetweetStatus;
    @Column
    String reTweetText;
    @ForeignKey(saveForeignKeyModel = false)
    User retweetUser;
    //Deserialize

    public boolean isFavorited() {
        return favorited;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public static Mention fromJSON(JSONObject jsonObject) throws JSONException {
        Mention mention = new Mention();
        mention.body = jsonObject.getString("text");
        mention.id = jsonObject.getLong("id");
        mention.createdAt = jsonObject.getString("created_at");
        mention.favorited = jsonObject.getBoolean("favorited");
        mention.retweetCount = jsonObject.getInt("retweet_count");
        mention.hasRetweetStatus = Boolean.FALSE;
        if (jsonObject.has("retweeted_status")) {
            JSONObject retweetStatus = jsonObject.getJSONObject("retweeted_status");
            if (retweetStatus != null) {
                mention.hasRetweetStatus = true;
                mention.reTweetText = retweetStatus.getString("text");
                mention.retweetUser = User.fromJSON(retweetStatus.getJSONObject("user"));
            }
        }

        mention.user = User.fromJSON(jsonObject.getJSONObject("user"));
        return mention;
    }

    public static ArrayList<Mention> fromJSONArray(JSONArray jsonArray) throws JSONException {
        ArrayList<Mention> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(fromJSON(jsonArray.getJSONObject(i)));
        }
        return list;
    }

    public User getUser() {
        return user;
    }

    public Boolean getHasRetweetStatus() {
        return hasRetweetStatus;
    }

    public String getReTweetText() {
        return reTweetText;
    }

    public User getRetweetUser() {
        return retweetUser;
    }
}
