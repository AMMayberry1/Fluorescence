package edu.umass.cs.sensors.fluorescence;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

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
    private Bitmap image = null;
    View FluorescenceView = null;

    public FluorescenceActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This is needed to be able to keep the image between screen rotations
        // Because of this, onCreate() is only called once for this fragment (not called on configuration change)
        setRetainInstance(true);

        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            while(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FluorescenceView = inflater.inflate(R.layout.fragment_fluorescence, container, false);
        mImageView = (ImageView)FluorescenceView.findViewById(R.id.photoView);
        Button captureBtn = (Button)FluorescenceView.findViewById(R.id.captureBtn);
        Button processBtn = (Button)FluorescenceView.findViewById(R.id.processBtn);

        // Check if there is a camera.
        Context context = getActivity();
        PackageManager packageManager = context.getPackageManager();
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false){
            Toast.makeText(getActivity(), "This device does not have a rear-facing camera. App cannot function.", Toast.LENGTH_SHORT)
                    .show();
            return FluorescenceView;
        }

        captureBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);

        return FluorescenceView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FluorescenceActivity activity = (FluorescenceActivity) getActivity();
        image = activity.getRetainedImageBitmap();

        if (image != null) {
            mImageView.setImageBitmap(image);
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

        image = BitmapFactory.decodeFile(imagePath, bmOptions);
        imageView.setImageBitmap(image);

        FluorescenceActivity activity = (FluorescenceActivity)getActivity();
        activity.setRetainedImageBitmap(image);
    }

    private void watershedImage() {
        TextView countText = (TextView)FluorescenceView.findViewById(R.id.countText);

        Mat thresh = new Mat(), cvImage = new Mat(), opening = new Mat(), sureBg = new Mat();
        Mat sureFg = new Mat(), distTransform = new Mat(), unknown = new Mat();
        Mat labels = new Mat(), stats = new Mat(), centroids = new Mat(), centerMarked = new Mat();

        Point defaultPoint = new Point(-1, -1);

        Utils.bitmapToMat(image, cvImage);

        Imgproc.cvtColor(cvImage, thresh, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(thresh, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

//        Utils.matToBitmap(thresh, image);
//        mImageView.setImageBitmap(image);

        Mat kernel = new Mat( 3, 3, CvType.CV_8UC1);
        byte kernelData[] = { 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        kernel.put(0, 0, kernelData);

        Imgproc.morphologyEx(thresh, opening, Imgproc.MORPH_OPEN, kernel, defaultPoint, 2);
        Imgproc.dilate(opening, sureBg, kernel, defaultPoint, 3);
        Imgproc.distanceTransform(opening, distTransform, Imgproc.DIST_L2, 5);

//        Mat imDisp = new Mat();
//        distTransform.convertTo(imDisp, CvType.CV_8UC1);
//        Utils.matToBitmap(imDisp, image);
//        mImageView.setImageBitmap(image);

        double transformMax = Core.minMaxLoc(distTransform).maxVal;
        Imgproc.threshold(distTransform, sureFg, 0.7 * transformMax, 255, Imgproc.THRESH_BINARY);

        sureFg.convertTo(sureFg, CvType.CV_8UC1);
        Core.subtract(sureBg, sureFg, unknown);

//        Utils.matToBitmap(sureFg, image);
//        mImageView.setImageBitmap(image);

        Imgproc.connectedComponentsWithStats(sureFg, labels, stats, centroids, 8, CvType.CV_16UC1);

        centerMarked = cvImage.clone();
        Scalar red = new Scalar(255, 0, 0);

        // Skip first element b/c it is centroid of background
        for (int i = 1; i < centroids.rows(); i++) {
            int x = (int)centroids.get(i, 0)[0];
            int y = (int)centroids.get(i, 1)[0];

            Imgproc.circle(centerMarked, new Point(x, y), 3, red, -1);
        }

        countText.setText("Cell Count: " + Integer.toString(centroids.rows() - 1));

        Utils.matToBitmap(centerMarked, image);
        mImageView.setImageBitmap(image);

//        Mat fg = new Mat(cvImage.size(), CvType.CV_8U);
//        Imgproc.erode(thresh,fg,new Mat(),new Point(-1,-1),2);
//
//        Mat bg = new Mat(cvImage.size(),CvType.CV_8U);
//        Imgproc.dilate(thresh,bg,new Mat(),new Point(-1,-1),3);
//        Imgproc.threshold(bg,bg,1, 128,Imgproc.THRESH_BINARY_INV);
//
//        Mat markers = new Mat(cvImage.size(),CvType.CV_8U, new Scalar(0));
//        Core.add(fg, bg, markers);
//
//        WatershedSegmenter segmenter = new WatershedSegmenter();
//        segmenter.setMarkers(markers);
//        Mat result = segmenter.process(cvImage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.captureBtn:
                dispatchTakePictureIntent();
                break;

            case R.id.processBtn:
                watershedImage();
                break;

            default:
                break;
        }
    }
}
