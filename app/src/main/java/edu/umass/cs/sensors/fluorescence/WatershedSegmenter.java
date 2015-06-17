package edu.umass.cs.sensors.fluorescence;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by ammayber on 6/17/15.
 */
public class WatershedSegmenter {
    public Mat markers;

    public void setMarkers(Mat markerImage)
    {
        markerImage.convertTo(markers, CvType.CV_32S);
    }

    public Mat process(Mat image)
    {
        Imgproc.watershed(image, markers);
        markers.convertTo(markers,CvType.CV_8U);
        return markers;
    }
}
