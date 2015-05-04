package com.activity.nikhilesh.dropphoto;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.session.dropphoto.UserSingleton;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.utils.dropphoto.UploadToDropBox;


public class PictureActivity extends ActionBarActivity {
    Button revoke,upload;
    private String mCameraFileName;
    private static final int NEW_PICTURE = 1;
    UserSingleton user = UserSingleton.getInstance();
    private final String PHOTO_DIR = "/DropPhoto/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        revoke = (Button)findViewById(R.id.revoke);
        revoke.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                DropboxAPI<AndroidAuthSession> mApi = user.getmApi();
                //Toast.makeText(getApplicationContext(),""+mApi.getSession().isLinked(),Toast.LENGTH_LONG).show();
                mApi.getSession().unlink();
                Toast.makeText(getApplicationContext(), "congrats", Toast.LENGTH_LONG).show();
                Intent in = new Intent(PictureActivity.this,LoginActivity.class);
                startActivity(in);
            }
        });
        upload = (Button)findViewById(R.id.camera);
        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                // Picture from camera
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                Date date = new Date();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss", Locale.US);

                String newPicFile = df.format(date) + ".jpg";
                String outPath = new File(Environment.getExternalStorageDirectory(), newPicFile).getPath();
                File outFile = new File(outPath);

                mCameraFileName = outFile.toString();
                Uri outuri = Uri.fromFile(outFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                Log.i("Picture Activity", "Importing New Picture: " + mCameraFileName);
                try {
                    startActivityForResult(intent, NEW_PICTURE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(),"There doesn't seem to be a camera.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_picture, menu);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_PICTURE) {
            // return from file upload
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }
                if (uri == null && mCameraFileName != null) {
                    uri = Uri.fromFile(new File(mCameraFileName));
                }
                File file = new File(mCameraFileName);

                if (uri != null) {
                    UploadToDropBox upload = new UploadToDropBox(this, user.getmApi(), PHOTO_DIR, file);
                    upload.execute();
                }
            } else {
                Log.w("Picture Activity", "Unknown Activity Result from mediaImport: "
                        + resultCode);
            }
        }
    }
}
