package com.example.xyzreader.ui;



import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import android.support.v4.content.Loader;


/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;
    CollapsingToolbarLayout toolbarLayout;
    LinearLayout articleBar;
    TextView titleView;
    TextView bylineView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
//        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
//                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        toolbarLayout = (CollapsingToolbarLayout) mRootView
                .findViewById(R.id.collapsing_toolbar_layout);

        // Implementing up arrow on details page
        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);

        return mRootView;
    }

    // called after on load finished
    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        articleBar = (LinearLayout) mRootView.findViewById(R.id.article_bar);
        titleView = (TextView) mRootView.findViewById(R.id.article_title);
        bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);

        //bylineView.setMovementMethod(new LinkMovementMethod());

        //bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {

            Log.v(TAG, "cursor rows" + mCursor.getCount());


            mRootView.setVisibility(View.VISIBLE);


            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),    // published time as long
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,   // current time in MS, hour in Millis
                            DateUtils.FORMAT_ABBREV_ALL).toString()                 // 524288
                            + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
                            //+ "</font>"));

            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));


            Picasso.with(getActivity())
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                   // .into(mPhotoView);
                    .into(myTarget);

        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    private Target myTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // loading of the bitmap was a success
            mPhotoView.setImageBitmap(bitmap);
            setColors(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            // loading of the bitmap failed
            Log.v(TAG, "Bitmap failed");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    public void setColors(Bitmap bitmap) {

        // Do this async on activity
        try {

            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {

                    //set image color
//                    int imageColor = palette.getDarkVibrantColor(Color.TRANSPARENT);
//
//                    if (imageColor != Color.TRANSPARENT) {
//                        articleBar.setBackgroundColor(imageColor);
//                    } else {
//                        imageColor = palette.getLightVibrantColor(Color.TRANSPARENT);
//                    }
//                    Log.v(TAG, "in onGenerated " + imageColor);
//                    articleBar.setBackgroundColor(imageColor);
//                    int textColor = palette.getLightVibrantColor(Color.DKGRAY);
//                    titleView.setTextColor(textColor);
//                    bylineView.setTextColor(textColor);


                    if (palette.getDarkVibrantSwatch() !=null) {
                        setContentColors(palette.getDarkVibrantSwatch());

                    } else if(palette.getLightVibrantSwatch() !=null) {
                        setContentColors(palette.getLightVibrantSwatch());

                    } else if (palette.getLightMutedSwatch() !=null) {
                        setContentColors(palette.getLightMutedSwatch());

                    } else if (palette.getDarkMutedSwatch() != null) {
                        setContentColors(palette.getDarkMutedSwatch());

                    } else if (palette.getVibrantSwatch() != null) {
                        setContentColors(palette.getVibrantSwatch());

                    } else if (palette.getMutedSwatch() != null) {
                        setContentColors(palette.getMutedSwatch());

                    }
                }
            });
        } catch (Exception ex) {
            Log.e("MainActivity", "error in creating palette");
        }
    }

        private void setContentColors(Palette.Swatch swatch) {
            articleBar.setBackgroundColor(swatch.getRgb());
            titleView.setTextColor(swatch.getTitleTextColor());
            bylineView.setTextColor(swatch.getBodyTextColor());

            Drawable upArrow = ContextCompat.getDrawable(getActivity(),
                    R.drawable.abc_ic_ab_back_mtrl_am_alpha);

            upArrow.setColorFilter(swatch.getTitleTextColor(), PorterDuff.Mode.SRC_ATOP);
        }
}