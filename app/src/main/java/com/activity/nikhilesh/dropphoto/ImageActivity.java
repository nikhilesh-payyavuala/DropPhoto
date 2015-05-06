package com.activity.nikhilesh.dropphoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.session.dropphoto.UserSingleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class ImageActivity extends AppCompatActivity {

    private final static String IMAGE_FILE_NAME = "dbroulette.png";

    private FileOutputStream mFos;

    private Bitmap myBitmap;

    private SharePhotoContent content;

    ShareDialog dialog;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView contentView = (ImageView)findViewById(R.id.fullscreen_image);
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        FacebookSdk.sdkInitialize(getApplicationContext());
        String path = getIntent().getStringExtra("PATH");
        setTitle(path);
        new DownloadImage(contentView,progressBar).execute(path, getIntent().getStringExtra("DIR"));
        callbackManager = CallbackManager.Factory.create();

        dialog = new ShareDialog(this);
        dialog.registerCallback(callbackManager, shareCallBack);
        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ShareDialog.canShow(SharePhotoContent.class)) {
                    SharePhoto photo = new SharePhoto.Builder()
                            .setBitmap(myBitmap).build();
                    content = new SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .build();
                    //Toast.makeText(getApplicationContext(), "HERE", Toast.LENGTH_LONG).show();
                    dialog.show(content);
                }
            }
        });

    }

    public FacebookCallback<Sharer.Result> shareCallBack = new FacebookCallback<Sharer.Result>() {


        public void onSuccess(Sharer.Result result) {
            Toast.makeText(getApplicationContext(),"Shared Successfully..",Toast.LENGTH_LONG).show();
        }

        public void onCancel() {
        }

        public void onError(FacebookException error) {
            Toast.makeText(getApplicationContext(), "Error Processing request..", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class DownloadImage extends AsyncTask<String, Long, String> {
        ImageView v;
        ProgressBar progressBar;
        public DownloadImage(ImageView v,ProgressBar progressBar){
            this.v = v;
            this.progressBar = progressBar;
        }



        @Override
        protected String doInBackground(String... params) {
            String path = params[0];
            String imgName = path.split("/")[2];
            String imgDirName = params[1];
            String pathCheck = imgDirName+ File.separator+imgName;
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
            progressBar.setVisibility(View.GONE);
            File imgFile = new File(result);
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            v.setImageBitmap(myBitmap);
        }
    }
}
