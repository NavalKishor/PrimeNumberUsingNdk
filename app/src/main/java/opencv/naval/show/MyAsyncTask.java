package opencv.naval.show;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static opencv.naval.show.FeaturesActivity.TAG;

/**
 * Created by m1035364 on 24/3/18.
 */

public class MyAsyncTask extends AsyncTask<Void, Void, Bitmap>
{

    Context context;
    Mat src;
    OnBitmapProcessed onBitmapProcessed;

    public void setOnBitmapProcessed(OnBitmapProcessed onBitmapProcessed)
    {
        this.onBitmapProcessed = onBitmapProcessed;
    }

    public void setSrc(Mat src)
    {
        this.src = src;
    }

    public MyAsyncTask()
    {

    }

    public MyAsyncTask(Context context)
    {
        this.context = context;
    }

    public MyAsyncTask(Mat srcs)
    {
        src = srcs;
    }

    //Main method for the computation of the image
    @Override
    protected Bitmap doInBackground(Void... objects)
    {

        //set-up of the image in the desired format for k-means clustering
        Mat samples = new Mat(src.rows() * src.cols(), 3, CvType.CV_32F);
        for (int y = 0; y < src.rows(); y++)
        {
            for (int x = 0; x < src.cols(); x++)
            {
                for (int z = 0; z < 3; z++)
                {
                    samples.put(x + y * src.cols(), z, src.get(y, x)[z]);
                }
            }
        }

        //applying k-means clustering
        int clusterCount = 2;
        Mat labels = new Mat();
        int attempts = 5;
        Mat centers = new Mat();
        Core.kmeans(samples, clusterCount, labels, new TermCriteria(TermCriteria.MAX_ITER |
                        TermCriteria.EPS, 10000, 0.0001), attempts,
                Core.KMEANS_PP_CENTERS, centers);

        //The image with the colour nearest to white will be considered as foreground
        double dstCenter0 = calcWhiteDist(centers.get(0, 0)[0], centers.get(0, 1)[0], centers.get(0, 2)[0]);
        double dstCenter1 = calcWhiteDist(centers.get(1, 0)[0], centers.get(1, 1)[0], centers.get(1, 2)[0]);
        int paperCluster = (dstCenter0 < dstCenter1) ? 0 : 1;

        //definition of 2 Mat objects needed for next step
        Mat srcRes = new Mat(src.size(), src.type());
        Mat srcGray = new Mat();

        //Performing Segmentation ie displaying all foreground pixels as white and all background pixels as black
        for (int y = 0; y < src.rows(); y++)
        {
            for (int x = 0; x < src.cols(); x++)
            {
                int cluster_idx = (int) labels.get(x + y * src.cols(), 0)[0];
                if (cluster_idx != paperCluster)
                {
                    srcRes.put(y, x, 0, 0, 0, 255);
                }
                else
                {
                    srcRes.put(y, x, 255, 255, 255, 255);
                }
            }
        }

        //Apply canny edge detection and then find contours
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(srcGray, srcGray, 50, 150);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(srcGray, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        //Finding the biggest contour corresponding to the page in the image
        int index = 0;
        double maxim = Imgproc.contourArea(contours.get(0));

        for (int contourIdx = 1; contourIdx < contours.size(); contourIdx++)
        {
            double temp;
            temp = Imgproc.contourArea(contours.get(contourIdx));
            if (maxim < temp)
            {
                maxim = temp;
                index = contourIdx;
            }
        }
        Mat drawing = Mat.zeros(srcRes.size(), CvType.CV_8UC1);
        Log.d(TAG, "number of contours " + contours.get(index));

        Imgproc.drawContours(drawing, contours, index, new Scalar(255), 1);
        //lines corresponding to the biggest contours used to find the intersection points of these lines to find the corners
        Mat lines = new Mat();
        Imgproc.HoughLinesP(drawing, lines, 1, Math.PI / 180, 70, 30, 10);
        Bitmap bitmap1 = Bitmap.createBitmap(drawing.width(), drawing.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(drawing, bitmap1);
        double[] line1 = lines.get(0, 0);
        Log.d(TAG, "Point one" + contours.get(index));
        Point[] contourPoints = contours.get(index).toArray();
        Log.d(TAG, "the points are " + contourPoints);
        return bitmap1;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        super.onPostExecute(bitmap);
        if (onBitmapProcessed != null)
        {
            onBitmapProcessed.processed(bitmap);
        }

    }

    double calcWhiteDist(double r, double g, double b)
    {
        return Math.sqrt(Math.pow(255 - r, 2) +
                Math.pow(255 - g, 2) + Math.pow(255 - b, 2));
    }

    Point findIntersection(double[] line1, double[] line2)
    {
        double start_x1 = line1[0], start_y1 = line1[1],
                end_x1 = line1[2], end_y1 = line1[3], start_x2 =
                line2[0], start_y2 = line2[1], end_x2 = line2[2],
                end_y2 = line2[3];
        double denominator = ((start_x1 - end_x1) * (start_y2 -
                end_y2)) - ((start_y1 - end_y1) * (start_x2 - end_x2));
        if (denominator != 0)
        {
            Point pt = new Point();
            pt.x = ((start_x1 * end_y1 - start_y1 * end_x1) *
                    (start_x2 - end_x2) - (start_x1 - end_x1) *
                    (start_x2 * end_y2 - start_y2 * end_x2)) /
                    denominator;
            pt.y = ((start_x1 * end_y1 - start_y1 * end_x1) *
                    (start_y2 - end_y2) - (start_y1 - end_y1) *
                    (start_x2 * end_y2 - start_y2 * end_x2)) /
                    denominator;
            return pt;
        }
        else
            return new Point(-1, -1);
    }

    boolean exists(ArrayList<Point> corners, Point pt)
    {
        for (int i = 0; i < corners.size(); i++)
        {
            if (Math.sqrt(Math.pow(corners.get(i).x - pt.x,
                    2) + Math.pow(corners.get(i).y - pt.y, 2)) < 10)
            {
                return true;
            }
        }
        return false;
    }

    interface OnBitmapProcessed
    {
        void processed(Bitmap bitmap);
    }
}
