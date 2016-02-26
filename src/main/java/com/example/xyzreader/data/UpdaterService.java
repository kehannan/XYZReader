package com.example.xyzreader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.graphics.Movie;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import com.example.xyzreader.remote.Article;
import com.example.xyzreader.remote.RemoteEndpointUtil;
import com.example.xyzreader.remote.ArticlesApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            return;
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        final ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        final Uri dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        // Rest adapter
        mRestAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(API_URL)
                .build();

        // Movie api
        ArticlesApi api = mRestAdapter.create(ArticlesApi.class);

        // Get movies
        api.getArticles(
                new Callback<List<Article>>() {


                    @Override
                    public void success(List<Article> list, Response response) {


                        // Iterates through the returned MovieList to get
                        // the movie and add to the MovieAdapter.
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







        // Don't even inspect the intent, we only do one thing, and that's fetch content.
       // ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        //Uri dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        //cpo.add(ContentProviderOperation.newDelete(dirUri).build());

//        try {
//            JSONArray array = RemoteEndpointUtil.fetchJsonArray();
//            if (array == null) {
//                throw new JSONException("Invalid parsed item array" );
//            }
//
//            for (int i = 0; i < array.length(); i++) {
//                ContentValues values = new ContentValues();
//                JSONObject object = array.getJSONObject(i);
//                values.put(ItemsContract.Items.SERVER_ID, object.getString("id" ));
//                values.put(ItemsContract.Items.AUTHOR, object.getString("author" ));
//                values.put(ItemsContract.Items.TITLE, object.getString("title" ));
//                values.put(ItemsContract.Items.BODY, object.getString("body" ));
//                values.put(ItemsContract.Items.THUMB_URL, object.getString("thumb" ));
//                values.put(ItemsContract.Items.PHOTO_URL, object.getString("photo" ));
//                values.put(ItemsContract.Items.ASPECT_RATIO, object.getString("aspect_ratio" ));
//                time.parse3339(object.getString("published_date"));
//                values.put(ItemsContract.Items.PUBLISHED_DATE, time.toMillis(false));
//                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
//            }
//
//            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);
//
//        } catch (JSONException | RemoteException | OperationApplicationException e) {
//            Log.e(TAG, "Error updating content.", e);
//        }

//        sendStickyBroadcast(
//                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
//    }
}
