package com.codepath.apps.mysimpletweets.models;

import com.codepath.apps.mysimpletweets.storage.MyTwitterDataBase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by gauravb on 4/1/17.
 */
@Parcel
@Table(database = MyTwitterDataBase.class)
public class Media extends BaseModel {
    @Column
    String id;
    @Column
    @PrimaryKey
    String type;
    @Column
    String mediaUrl;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public static Media fromJSON(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) return null;
        Media user = new Media();
        user.id = jsonObject.getString("id");
        user.type = jsonObject.getString("type");
        user.mediaUrl = jsonObject.getString("media_url");

        return user;
    }

    public static ArrayList<Media> fromJSONArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null) return null;
        ArrayList<Media> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(fromJSON(jsonArray.getJSONObject(i)));
        }
        return list;
    }
}
