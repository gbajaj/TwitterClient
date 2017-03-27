package com.codepath.apps.mysimpletweets.storage;

import android.content.Intent;

import com.codepath.apps.mysimpletweets.MyDatabase;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Created by gauravb on 3/24/17.
 */

@com.raizlabs.android.dbflow.annotation.Database(name = MyDatabase.NAME, version = MyDatabase.VERSION)
public class MyTwitterDataBase {
    public static final String NAME = "MyTwitterDataBase";

    public static final int VERSION = 1;

//    public static List<Tweet> findRecent(String tweetId) {
//        return new Select().from(Tweet.class).where("id > ?", Integer.parseInt(tweetId)).execute();
//    }
}
