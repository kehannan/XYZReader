package com.example.xyzreader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;
import com.example.xyzreader.remote.Article;
import com.example.xyzreader.remote.ArticlesApi;
import java.util.ArrayList;
import java.util.List;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.xyzreader.intent.extra.REFRESHING";

    //Rest adapter for retrofit
    protected RestAdapter mRestAdapter;

    //end point
    public static final String API_URL = "https://dl.dropboxusercontent.com";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Time time = new Time();

        final ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        final Uri dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        // Rest adapter
        mRestAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(API_URL)
                .build();

        // Articles api
        ArticlesApi api = mRestAdapter.create(ArticlesApi.class);

        // Get Articles
        api.getArticles(
                new Callback<List<Article>>() {

                    @Override
                    public void success(List<Article> list, Response response) {


                        // Iterates through the returned Article list to get
                        // the article and add to ContentValues.
                        for (int i = 0; i < list.size(); i++) {
                            Article article = list.get(i);

                            ContentValues values = new ContentValues();

                            values.put(ItemsContract.Items.SERVER_ID, article.getId());
                            values.put(ItemsContract.Items.AUTHOR, article.getAuthor());
                            values.put(ItemsContract.Items.TITLE, article.getTitle());
                            values.put(ItemsContract.Items.BODY, article.getBody());
                            values.put(ItemsContract.Items.THUMB_URL, article.getThumb());
                            values.put(ItemsContract.Items.PHOTO_URL, article.getPhoto());
                            values.put(ItemsContract.Items.ASPECT_RATIO, article.getAspectRatio());
                            time.parse3339(article.getPublishedDate());
                            values.put(ItemsContract.Items.PUBLISHED_DATE, time.toMillis(false));
                            cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
                        }

                        try {
                            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);
                        } catch (RemoteException | OperationApplicationException e) {
                            Log.e(TAG, "Error updating content.", e);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.v(TAG, "failure()");
                        Log.v(TAG, error.toString());
                    }
                });
    }
}