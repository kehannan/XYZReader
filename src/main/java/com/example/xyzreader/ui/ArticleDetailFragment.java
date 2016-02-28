package com.example.xyzreader.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
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
    CollapsingToolbarLayout toolbarLayout;
    LinearLayout articleBar;
    TextView titleView;
    TextView bylineView;

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

        setHasOptionsMenu(true);
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

        // Implementing up arrow
        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);

        FloatingActionButton mFab = (FloatingActionButton) mRootView.findViewById(R.id.fab);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

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

        if (mCursor != null) {

            mRootView.setVisibility(View.VISIBLE);

            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),    // published time as long
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,   // current time in MS, hour in Millis
                            DateUtils.FORMAT_ABBREV_ALL).toString()                 // 524288
                            + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));

            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            // Load image with Picasso library. Use custom Target object
            // to set the photo view and colors of the views based on
            // the Palette of the photo
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

    // Custom Target used by Picasso to set Image and
    // set background/view colors
    private Target myTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            mPhotoView.setImageBitmap(bitmap);
            setColors(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
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

    // Sets color of views based on Palette on the image.
    // Takes the image (bitmap) as input
    public void setColors(Bitmap bitmap) {

        try {
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {

                // Use the first theme that is non-null and set the colors
                // of the article bar.
                @Override
                public void onGenerated(Palette palette) {

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
            Log.e(TAG, "error in creating palette");
        }
    }

        // Set the colors of the views in the article bar based on the
        // supplied swatch. Also sets the up arrow color.
        private void setContentColors(Palette.Swatch swatch) {
            articleBar.setBackgroundColor(swatch.getRgb());
            titleView.setTextColor(swatch.getTitleTextColor());
            bylineView.setTextColor(swatch.getBodyTextColor());

            Drawable upArrow = ContextCompat.getDrawable(getActivity(),
                    R.drawable.abc_ic_ab_back_mtrl_am_alpha);

            upArrow.setColorFilter(swatch.getTitleTextColor(), PorterDuff.Mode.SRC_ATOP);
        }
}