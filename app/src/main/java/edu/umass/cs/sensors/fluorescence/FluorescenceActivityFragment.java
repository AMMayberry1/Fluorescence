package edu.umass.cs.sensors.fluorescence;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class FluorescenceActivityFragment extends Fragment implements Button.OnClickListener {
    // Activity result key for camera
    static final int REQUEST_TAKE_PHOTO = 11111;

    private ImageView mImageView = null;

    public FluorescenceActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This is needed to be able to keep the image between screen rotations
        // Because of this, onCreate() is only called once for this fragment (not called on configuration change)
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fluorescence, container, false);
        mImageView = (ImageView)view.findViewById(R.id.photoView);
        Button captureBtn = (Button)view.findViewById(R.id.captureBtn);

        // Check if there is a camera.
        Context context = getActivity();
        PackageManager packageManager = context.getPackageManager();
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false){
            Toast.makeText(getActivity(), "This device does not have a rear-facing camera. App cannot function.", Toast.LENGTH_SHORT)
                    .show();
            return view;
        }

        captureBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FluorescenceActivity activity = (FluorescenceActivity) getActivity();
        Bitmap retainedImage = activity.getRetainedImageBitmap();

        if (retainedImage != null) {
            mImageView.setImageBitmap(retainedImage);
        }
    }

    protected void dispatchTakePictureIntent() {

        // Camera exists? Then proceed...
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        FluorescenceActivity activity = (FluorescenceActivity) getActivity();
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            // If you don't do this, you may get a crash in some devices.
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast toast = Toast.makeText(activity, "There was a problem saving the photo...", Toast.LENGTH_SHORT);
                toast.show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri fileUri = Uri.fromFile(photoFile);
                activity.setCapturedImageURI(fileUri);
                activity.setCurrentPhotoPath(fileUri.getPath());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        activity.getCapturedImageURI());
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    protected File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        FluorescenceActivity activity = (FluorescenceActivity)getActivity();
        activity.setCurrentPhotoPath("file:" + image.getAbsolutePath());
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            addPhotoToGallery();
            FluorescenceActivity activity = (FluorescenceActivity)getActivity();

            // Show the full sized image.
            setFullImageFromFilePath(activity.getCurrentPhotoPath(), mImageView);
        } else {
            Toast.makeText(getActivity(), "Image Capture Failed", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Add the picture to the photo gallery.
     * Must be called on all camera images or they will
     * disappear once taken.
     */
    protected void addPhotoToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        FluorescenceActivity activity = (FluorescenceActivity)getActivity();
        File f = new File(activity.getCurrentPhotoPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.getActivity().sendBroadcast(mediaScanIntent);
    }

    /**
     * Scale the photo down and fit it to our image views.
     *
     * "Drastically increases performance" to set images using this technique.
     * Read more:http://developer.android.com/training/camera/photobasics.html
     */
    private void setFullImageFromFilePath(String imagePath, ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        // TODO: Look at using bmOptions.inBitmap to potentially reduce memory consumption (old code used bmOptions.inPurgeable)

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        imageView.setImageBitmap(bitmap);

        FluorescenceActivity activity = (FluorescenceActivity)getActivity();
        activity.setRetainedImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.captureBtn:
                dispatchTakePictureIntent();
                break;

            default:
                break;
        }
    }
}
