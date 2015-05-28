package edu.umass.cs.sensors.fluorescence;

import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class FluorescenceActivity extends ActionBarActivity {

    // Used to store image data in memory between activity restarts
    private ImageRetainFragment imageRetainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fluorescence);

        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        imageRetainFragment = (ImageRetainFragment) fm.findFragmentByTag("image");

        // create the fragment and data the first time
        if (imageRetainFragment == null) {
            // add the fragment
            imageRetainFragment = new ImageRetainFragment();
            fm.beginTransaction().add(imageRetainFragment, "image").commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fluorescence, menu);
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

    // Storage for camera image URI components
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";

    // Required for camera operations in order to save the image file on resume.
    private String mCurrentPhotoPath = null;
    private Uri mCapturedImageURI = null;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mCurrentPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
        if (mCapturedImageURI != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_URI_KEY, mCapturedImageURI.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_URI_KEY)) {
            mCapturedImageURI = Uri.parse(savedInstanceState.getString(CAPTURED_PHOTO_URI_KEY));
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Getters and setters.
     */

    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public void setCurrentPhotoPath(String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }

    public Uri getCapturedImageURI() {
        return mCapturedImageURI;
    }

    public void setCapturedImageURI(Uri mCapturedImageURI) {
        this.mCapturedImageURI = mCapturedImageURI;
    }

    public void setRetainedImageBitmap(Bitmap bitmap) {
        imageRetainFragment.setCurrentImage(bitmap);
    }

    public Bitmap getRetainedImageBitmap() {
        return imageRetainFragment.getCurrentImage();
    }
}
