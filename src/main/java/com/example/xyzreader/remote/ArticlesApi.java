package com.example.xyzreader.remote;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by khannan on 2/25/16.
 */
public interface ArticlesApi {

    @GET("/u/231329/xyzreader_data/data.json")
    void getArticles (Callback<List<Article>> callback);
}
