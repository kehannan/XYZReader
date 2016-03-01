package com.example.xyzreader.remote;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * An API using Retrofit to get the Articles
 */
public interface ArticlesApi {

    @GET("/u/231329/xyzreader_data/data.json")
    void getArticles (Callback<List<Article>> callback);
}
