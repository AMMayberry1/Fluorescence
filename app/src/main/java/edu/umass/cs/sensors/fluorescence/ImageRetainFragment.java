package edu.umass.cs.sensors.fluorescence;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;

/**
 * Created by ammayber on 5/28/15.
 */
public class ImageRetainFragment extends Fragment {
    private Bitmap currentImage = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setCurrentImage(Bitmap image) {
        currentImage = image;
    }

    public Bitmap getCurrentImage() {
        return currentImage;
    }
}
