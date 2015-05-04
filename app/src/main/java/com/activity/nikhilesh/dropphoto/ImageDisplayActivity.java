package com.activity.nikhilesh.dropphoto;

import com.activity.nikhilesh.dropphoto.util.SystemUiHider;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.session.dropphoto.UserSingleton;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ImageDisplayActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    private final static String IMAGE_FILE_NAME = "dbroulette.png";

    private FileOutputStream mFos;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_display);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final ImageView contentView = (ImageView)findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;


                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        String path = getIntent().getStringExtra("PATH");
        new DownloadImage(contentView).execute(path,getIntent().getStringExtra("DIR"));
        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {

        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    public class DownloadImage extends AsyncTask<String, Long, String>{
        ImageView v;
        public DownloadImage(ImageView v){
            this.v = v;
        }



        @Override
        protected String doInBackground(String... params) {
            String path = params[0];
            String imgName = path.split("/")[2];
            String imgDirName = params[1];
            String pathCheck = imgDirName+File.separator+imgName;
            File f = new File(pathCheck);
            if(!f.exists()) {
                UserSingleton user = UserSingleton.getInstance();
                String cachePath = getApplicationContext().getCacheDir().getAbsolutePath() + "/" + IMAGE_FILE_NAME;
                try {
                    mFos = new FileOutputStream(cachePath);
                } catch (FileNotFoundException e) {
                    //mErrorMsg = "Couldn't create a local file to store the image";

                }
                try {
                    user.getmApi().getThumbnail(params[0], mFos, DropboxAPI.ThumbSize.BESTFIT_1024x768, DropboxAPI.ThumbFormat.JPEG, null);
                } catch (DropboxUnlinkedException e) {
                    // The AuthSession wasn't properly authenticated or user unlinked.
                } catch (DropboxPartialFileException e) {
                    // We canceled the operation
                    //mErrorMsg = "Download canceled";
                } catch (DropboxServerException e) {
                    // Server-side exception.  These are examples of what could happen,
                    // but we don't do anything special with them here.
                    if (e.error == DropboxServerException._304_NOT_MODIFIED) {
                        // won't happen since we don't pass in revision with metadata
                    } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                        // Unauthorized, so we should unlink them.  You may want to
                        // automatically log the user out in this case.
                    } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                        // Not allowed to access this
                    } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                        // path not found (or if it was the thumbnail, can't be
                        // thumbnailed)
                    } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                        // too many entries to return
                    } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                        // can't be thumbnailed
                    } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                        // user is over quota
                    } else {
                        // Something else
                    }
                    // This gets the Dropbox error, translated into the user's language
                    //mErrorMsg = e.body.userError;
                    //if (mErrorMsg == null) {
                    //    mErrorMsg = e.body.error;
                    //}
                } catch (DropboxIOException e) {
                    // Happens all the time, probably want to retry automatically.
                    //mErrorMsg = "Network error.  Try again.";
                } catch (DropboxParseException e) {
                    // Probably due to Dropbox server restarting, should retry
                    //mErrorMsg = "Dropbox error.  Try again.";
                } catch (DropboxException e) {
                    // Unknown error
                    //mErrorMsg = "Unknown error.  Try again.";
                }
                return cachePath;
            }
            else{
                return pathCheck;
            }

        }

        @Override
        protected void onPostExecute(String result) {
           File imgFile = new File(result);
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            v.setImageBitmap(myBitmap);
        }
    }
}
