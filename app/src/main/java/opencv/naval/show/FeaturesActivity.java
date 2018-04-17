package opencv.naval.show;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import primeno.naval.com.primenumberusingndk.R;

public class FeaturesActivity extends AppCompatActivity
{


    private final int SELECT_PHOTO = 1;
    private Mat originalMat;
    private Bitmap currentBitmap;
    @BindView(R.id.ivImage)
    ImageView ivImage;
//    @BindView(R.id.ivImageProcessed)
//    ImageView ivImageProcessed;
    Mat src;
    static int ACTION_MODE = 0;
    static String TAG="TagImages";
//    static
//    {
//        if (OpenCVLoader.initDebug())
//        {
//            Log.d(TAG, "static initializer:OpenCV Loaded");
//        }
//        else
//        {
//            Log.d(TAG, "static initializer:OpenCV not Loaded");
//        }
//    }
    BaseLoaderCallback baseLoaderCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                    // our work is here
                    Log.i(TAG, "OpenCV loaded successfully");
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };
    private String picturePath;
    ArrayList<org.opencv.core.Point> corners=new ArrayList<org.opencv.core.Point>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if(intent.hasExtra("ACTION_MODE"))
        {
            ACTION_MODE = intent.getIntExtra("ACTION_MODE", 0);
        }
        if (ContextCompat.checkSelfPermission(FeaturesActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("permission", "request READ_EXTERNAL_STORAGE");
            ActivityCompat.requestPermissions(FeaturesActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_EXTERNAL_STORAGE);
        }else {
            Log.i("permission", "READ_EXTERNAL_STORAGE already granted");
            read_external_storage_granted = true;
        }
        ivImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(sampledImage==null) return false;
                Log.i(TAG, "event.getX(), event.getY(): " + event.getX() +" "+ event.getY());
                int projectedX = (int)((double)event.getX() * ((double)sampledImage.width()/(double)view.getWidth()));
                int projectedY = (int)((double)event.getY() * ((double)sampledImage.height()/(double)view.getHeight()));
                org.opencv.core.Point corner = new org.opencv.core.Point(projectedX, projectedY);
                corners.add(corner);
                Imgproc.circle(sampledImage, corner, (int) 5, new Scalar(0,0,255),2);
                displayImage(sampledImage);
                return false;
            }
        });
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,baseLoaderCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_border, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_load_image) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            return true;
        }else
        if (id== R.id.DoG)
        {
            DifferenceOfGaussian();
            return true;
        }
        else if (id== R.id.CannyEdges)
        {
            Canny();
            return true;
        }else if (id== R.id.SobelFilter)
        {
            Sobel();
            return true;
        }else
        if (id== R.id.SobelFilter)
        {
            Sobel();
            return true;
        }
        else if (id == R.id.HarrisCorners) {
            //Apply Harris Corners
            HarrisCorner();
        } else if (id == R.id.HoughLines) {
            //Apply Hough Lines
            HoughLines();
        } else if (id == R.id.HoughCircles) {
            //Apply Hough Circles
            HoughCircles();
        } else if (id == R.id.Contours) {
            //Apply contours
            Contours();
        }else if (id == R.id.reset) {
            //Apply to rest the image to original image
            createOriginalBitmap(picturePath);
        }
        else if(id==R.id.action_rigidscan)
        {
            //1. The first step is to make sure that the user has already loaded an image:
            if(originalMat==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                loadImage(picturePath);
                return true;
            }
            //2. Convert the input image to a grayscale image:
            Mat gray = new Mat();
            Imgproc.cvtColor(originalMat, gray, Imgproc.COLOR_RGB2GRAY);
            //3. Use the Canny edge detector to build the edge image:
            Mat edgeImage=new Mat();
            Imgproc.Canny(gray, edgeImage, 100, 200);
          //  4. After building the edge image, we need to detect lines, by using the probabilistic Hough line transform:
            Mat lines = new Mat();
            int threshold = 50;
            Imgproc.HoughLinesP(edgeImage, lines, 1, Math.PI/180, threshold,60,10);
            //5. Declare and initialize the variables to find up to four bounding
            boolean [] include=new boolean[lines.cols()];
            double maxTop=edgeImage.rows();
            double maxBottom=0;
            double maxRight=0;
            double maxLeft=edgeImage.cols();
            int leftLine=0;
            int rightLine=0;
            int topLine=0;
            int bottomLine=0;
            ArrayList<org.opencv.core.Point> points=new ArrayList<org.opencv.core.Point>();
            // 6. In the for loop, we test every line to find the left-most border line of the
            //object of interest. Once it is found, we set its corresponding include array element to
            //true to avoid selecting the same line again when we search for a different bounding
            //line:
            for (int i = 0; i < lines.cols(); i++)
            {
                double[] line = lines.get(0, i);
                double xStart = line[0], xEnd = line[2];
                if(xStart<maxLeft && !include[i])
                {
                    maxLeft=xStart;
                    leftLine=i;
                }
                if(xEnd<maxLeft && !include[i])
                {
                    maxLeft=xEnd;
                    leftLine=i;
                }
            }
            include[leftLine]=true;
//            7. Once the line is found, we add its two points to the points array list. This array list
//            will be used later when we estimate the bounding rectangle:
            double[] line = lines.get(0, leftLine);
            double xStartleftLine = line[0],
                    yStartleftLine = line[1],
                    xEndleftLine = line[2],
                    yEndleftLine = line[3];
            org.opencv.core.Point lineStartleftLine = new
                    org.opencv.core.Point(xStartleftLine, yStartleftLine);
            org.opencv.core.Point lineEndleftLine = new
                    org.opencv.core.Point(xEndleftLine, yEndleftLine);
            points.add(lineStartleftLine);
            points.add(lineEndleftLine);
           // 8. We do the same to find the right-most bounding line:
            for (int i = 0; i < lines.cols(); i++)
            {
                line = lines.get(0, i);
                double xStart = line[0], xEnd = line[2];
                if(xStart>maxRight && !include[i])
                {
                    maxRight=xStart;
                    rightLine=i;
                }if(xEnd>maxRight && !include[i])
                {
                    maxRight=xEnd;
                    rightLine=i;
                }
            }include[rightLine]=true;
            //9. Add the points that belong to the right-most border line to the points array list:
            line = lines.get(0, rightLine);
            double xStartRightLine = line[0],
            yStartRightLine = line[1],
            xEndRightLine = line[2],
            yEndRightLine = line[3];
            org.opencv.core.Point lineStartRightLine = new
            org.opencv.core.Point(xStartRightLine, yStartRightLine);
            org.opencv.core.Point lineEndRightLine = new
            org.opencv.core.Point(xEndRightLine, yEndRightLine);
            points.add(lineStartRightLine);
            points.add(lineEndRightLine);
//            10. Find the top border line:
        for (int i = 0; i < lines.cols(); i++)
        {
            line = lines.get(0, i);
            double yStart = line[1],yEnd = line[3];
            if(yStart<maxTop && !include[i])
            {
                maxTop=yStart;
                topLine=i;
            }if(yEnd<maxTop && !include[i])
            {
                maxTop=yEnd;
                topLine=i;
            }
        }include[topLine]=true;
//            11. Add the points that belong to the top border line to the points array list:
        line = lines.get(0, topLine);
            double xStartTopLine = line[0],
                    yStartTopLine = line[1],
                    xEndTopLine = line[2],
                    yEndTopLine = line[3];
            org.opencv.core.Point lineStartTopLine = new
                    org.opencv.core.Point(xStartTopLine, yStartTopLine);
            org.opencv.core.Point lineEndTopLine = new org.opencv.core.Point(xEndTopLine, yEndTopLine);
            points.add(lineStartTopLine);
            points.add(lineEndTopLine);
//            12. Find the bottom border line:
        for (int i = 0; i < lines.cols(); i++)
        {
            line = lines.get(0, i);
            double yStart = line[1],yEnd = line[3];
            if(yStart>maxBottom && !include[i])
            {
                maxBottom=yStart;
                bottomLine=i;
            }if(yEnd>maxBottom && !include[i])
            {
                maxBottom=yEnd;
                bottomLine=i;
            }
        }include[bottomLine]=true;
//            13. Add the bottom line points to the points array list:
            line = lines.get(0, bottomLine);
            double xStartBottomLine = line[0],
                    yStartBottomLine = line[1],
                    xEndBottomLine = line[2],
                    yEndBottomLine = line[3];
            org.opencv.core.Point lineStartBottomLine = new
                    org.opencv.core.Point(xStartBottomLine, yStartBottomLine);
            org.opencv.core.Point lineEndBottomLine = new
                    org.opencv.core.Point(xEndBottomLine, yEndBottomLine);
            points.add(lineStartBottomLine);
            points.add(lineEndBottomLine);
//            14. We initialize a matrix of points, MatOfPoint2f object, with the list of points that we
//            selected from the detected border lines:
            MatOfPoint2f mat=new MatOfPoint2f();
            mat.fromList(points);
//            15. We find the bounding rectangle ,now to find a rectangle
//that fits a set of points and has the minimum area of all the possible rectangles
            RotatedRect rect= Imgproc.minAreaRect(mat);
//            16. Now, we extract the four corner points of the estimated rectangle to an array of
//            points:
            org.opencv.core.Point rect_points[]=new org.opencv.core.Point [4];
            rect.points(rect_points);
//            17. Initialize a new image that will be used to display the object of interest after doing
//the perspective correction.We will also use this image’s four corners to find the
//transformation that will minimize the distance between these corners and the
//corresponding object of interest’s corners.
            Mat correctedImage=new Mat(originalMat.rows(),originalMat.cols(),originalMat.type());
//            18. Now, we initialize two Mat objects, one to store the four corners of the object of
//            interest and the other one to store the corresponding corners of the image in which we
//            will display the object of interest after the perspective correction:
            Mat srcPoints= Converters.vector_Point2f_to_Mat(Arrays.asList(rect_points));
            Mat destPoints=Converters.vector_Point2f_to_Mat(Arrays.asList(new org.opencv.core.Point[]{
                    new org.opencv.core.Point(0, correctedImage.rows()),
                    new org.opencv.core.Point(0, 0),
                    new org.opencv.core.Point(correctedImage.cols(),0),
                    new org.opencv.core.Point(correctedImage.cols(), correctedImage.rows())
            }));
//            19. We calculate the needed transformation matrix by calling
//            Imgproc.getPerspectiveTransform() and passing it to the source and destination
//            corner points:
            Mat transformation=Imgproc.getPerspectiveTransform(srcPoints,
                    destPoints);
//            20. below Finally, we apply the transformation that we calculated
            Imgproc.warpPerspective(originalMat, correctedImage, transformation, correctedImage.size());
//            21. The last step is to display our object of interest after applying the appropriate
//transformation:
            displayImage(correctedImage);

        }
        else if(id==R.id.action_flexscan)
        {
            if (sampledImage == null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                loadImage(picturePath);
                return true;
            }
            Mat gray = new Mat();
            Imgproc.cvtColor(sampledImage, gray, Imgproc.COLOR_RGB2GRAY);
            Imgproc.GaussianBlur(gray, gray, new Size(7,7), 0);
            Mat edgeImage=new Mat();
            Imgproc.Canny(gray, edgeImage, 100, 300);
            Mat lines = new Mat();
            int threshold = 100;
            Imgproc.HoughLinesP(edgeImage, lines, 1, Math.PI/180, threshold,60,10);
            ArrayList<org.opencv.core.Point> corners=new ArrayList<org.opencv.core.Point>();
//            for (int i = 0; i < lines.cols(); i++)
            for (int i = 0; i < lines.rows(); i++)
            {
//                for (int j = i+1; j < lines.cols(); j++)
                for (int j = i+1; j < lines.rows(); j++)
                {
//                    org.opencv.core.Point intersectionPoint = getLinesIntersection(lines.get(0, i), lines.get(0, j));
                    org.opencv.core.Point intersectionPoint = getLinesIntersection(lines.get(i, 0), lines.get(j, 0));
                    if(intersectionPoint!=null)
                    {
                        corners.add(intersectionPoint);
                    }
                }
            }
            MatOfPoint2f cornersMat=new MatOfPoint2f();
            cornersMat.fromList(corners);
            MatOfPoint2f approxConrers=new MatOfPoint2f();
            try
            {
                Imgproc.approxPolyDP(cornersMat, approxConrers,Imgproc.arcLength(cornersMat, true)*0.02, true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if(approxConrers.rows()<4)
            {
                Context context = getApplicationContext();
                CharSequence text = "Couldn't detect an object with four corners!";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                return true;
            }
            corners.clear();
            Converters.Mat_to_vector_Point2f(approxConrers,corners);
            org.opencv.core.Point centroid=new org.opencv.core.Point(0,0);
            for(org.opencv.core.Point point:corners)
            {
                centroid.x+=point.x;
                centroid.y+=point.y;
            }
            centroid.x/=corners.size();
            centroid.y/=corners.size();
            ArrayList<org.opencv.core.Point> top=new
                    ArrayList<org.opencv.core.Point>();
            ArrayList<org.opencv.core.Point> bottom=new
                    ArrayList<org.opencv.core.Point>();
            for (int i = 0; i < corners.size(); i++)
            {
//                if (corners.get(i).y < center.y)
                if (corners.get(i).y < centroid.y)
                    top.add(corners.get(i));
                else
                    bottom.add(corners.get(i));
            }
            org.opencv.core.Point topLeft = top.get(0).x > top.get(1).x ?
                    top.get(1) : top.get(0);
            org.opencv.core.Point topRight = top.get(0).x > top.get(1).x ?
                    top.get(0) : top.get(1);
            org.opencv.core.Point bottomLeft = bottom.get(0).x > bottom.get(1).x ?
                    bottom.get(1) :bottom.get(0);
            org.opencv.core.Point bottomRight = bottom.get(0).x > bottom.get(1).x ?
                    bottom.get(0) : bottom.get(1);
            corners.clear();
            corners.add(topLeft);
            corners.add(topRight);
            corners.add(bottomRight);
            corners.add(bottomLeft);
            Mat correctedImage=new
                    Mat(sampledImage.rows(),sampledImage.cols(),sampledImage.type());
            Mat srcPoints=Converters.vector_Point2f_to_Mat(corners);
            Mat destPoints=Converters.vector_Point2f_to_Mat(Arrays.asList(new
                    org.opencv.core.Point[]{
                    new org.opencv.core.Point(0, 0),
                    new org.opencv.core.Point(correctedImage.cols(), 0),
                    new
                            org.opencv.core.Point(correctedImage.cols(),correctedImage.rows()),new
                    org.opencv.core.Point(0,correctedImage.rows())}));
            Mat transformation=Imgproc.getPerspectiveTransform(srcPoints,
                    destPoints);
            Imgproc.warpPerspective(sampledImage, correctedImage, transformation,
                    correctedImage.size());
            displayImage(correctedImage);
        }
        else if(id==R.id.action_manScan)
        {
            if(sampledImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                loadImage(picturePath);
                return true;
            }
            if(corners.size()!=4)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to select four corners!";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                corners.clear();
                return true;
            }
            //find the centroid of the polygon to order the found corners
            org.opencv.core.Point centroid=new org.opencv.core.Point(0,0);
            for(org.opencv.core.Point point:corners)
            {
                centroid.x+=point.x;
                centroid.y+=point.y;
            }
            centroid.x/=corners.size();
            centroid.y/=corners.size();

            sortCorners(corners,centroid);
            Mat correctedImage=new Mat(sampledImage.rows(),sampledImage.cols(),sampledImage.type());
            Mat srcPoints=Converters.vector_Point2f_to_Mat(corners);

            Mat destPoints=Converters.vector_Point2f_to_Mat(Arrays.asList(new org.opencv.core.Point[]{
                    new org.opencv.core.Point(0, 0),
                    new org.opencv.core.Point(correctedImage.cols(), 0),
                    new org.opencv.core.Point(correctedImage.cols(),correctedImage.rows()),
                    new org.opencv.core.Point(0,correctedImage.rows())}));

            Mat transformation=Imgproc.getPerspectiveTransform(srcPoints, destPoints);
            Imgproc.warpPerspective(sampledImage, correctedImage, transformation, correctedImage.size());

            displayImage(correctedImage);
        }


        return super.onOptionsItemSelected(item);
    }

    private Point getLinesIntersections(double[] doubles, double[] doubles1)
    {
        //IPx=(((x1y2-y1x2)(x2-x4)-(x1-x2)(x2y4-y2x4))/((x1-x2)(y2-y4)-(y1-y2)(x2-x4)))
        //IPy=(((x1y2-y1x2)(y8-y4)-(y1-y2)(x8y4-y8x4))/((x1-x2)(y8-y4)-(y1-y2)(x8-x4)))


        return null;
    }
    private org.opencv.core.Point getLinesIntersection(double [] firstLine, double [] secondLine)
    {
        double FX1=firstLine[0],FY1=firstLine[1],FX2=firstLine[2],FY2=firstLine[3];
        double SX1=secondLine[0],SY1=secondLine[1],SX2=secondLine[2],SY2=secondLine[3];
        org.opencv.core.Point intersectionPoint=null;
        //Make sure the we will not divide by zero
        double denominator=(FX1-FX2)*(SY1-SY2)-(FY1-FY2)*(SX1-SX2);
        if(denominator!=0)
        {
            intersectionPoint=new org.opencv.core.Point();
            intersectionPoint.x=((FX1*FY2-FY1*FX2)*(SX1-SX2)-(FX1-FX2)*(SX1*SY2-SY1*SX2))/denominator;
            intersectionPoint.y=((FX1*FY2-FY1*FX2)*(SY1-SY2)-(FY1-FY2)*(SX1*SY2-SY1*SX2))/denominator;
            if(intersectionPoint.x<0 || intersectionPoint.y<0)
                return null;
        }
        return intersectionPoint;
    }
    void sortCorners(ArrayList<org.opencv.core.Point> corners, org.opencv.core.Point center)
    {
        ArrayList<org.opencv.core.Point> top=new ArrayList<org.opencv.core.Point>();
        ArrayList<org.opencv.core.Point> bottom=new ArrayList<org.opencv.core.Point>();

        for (int i = 0; i < corners.size(); i++)
        {
            if (corners.get(i).y < center.y)
                top.add(corners.get(i));
            else
                bottom.add(corners.get(i));
        }

        double topLeft=top.get(0).x;
        int topLeftIndex=0;
        for(int i=1;i<top.size();i++)
        {
            if(top.get(i).x<topLeft)
            {
                topLeft=top.get(i).x;
                topLeftIndex=i;
            }
        }

        double topRight=0;
        int topRightIndex=0;
        for(int i=0;i<top.size();i++)
        {
            if(top.get(i).x>topRight)
            {
                topRight=top.get(i).x;
                topRightIndex=i;
            }
        }

        double bottomLeft=bottom.get(0).x;
        int bottomLeftIndex=0;
        for(int i=1;i<bottom.size();i++)
        {
            if(bottom.get(i).x<bottomLeft)
            {
                bottomLeft=bottom.get(i).x;
                bottomLeftIndex=i;
            }
        }

        double bottomRight=bottom.get(0).x;
        int bottomRightIndex=0;
        for(int i=1;i<bottom.size();i++)
        {
            if(bottom.get(i).x>bottomRight)
            {
                bottomRight=bottom.get(i).x;
                bottomRightIndex=i;
            }
        }

        org.opencv.core.Point topLeftPoint = top.get(topLeftIndex);
        org.opencv.core.Point topRightPoint = top.get(topRightIndex);
        org.opencv.core.Point bottomLeftPoint = bottom.get(bottomLeftIndex);
        org.opencv.core.Point bottomRightPoint = bottom.get(bottomRightIndex);

        corners.clear();
        corners.add(topLeftPoint);
        corners.add(topRightPoint);
        corners.add(bottomRightPoint);
        corners.add(bottomLeftPoint);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case SELECT_PHOTO:
                if(resultCode==RESULT_OK)
                {
                    try{
                        //Code to load image into a Bitmap and convert it to a Mat for processing.
                        final Uri imageUri = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                          picturePath = cursor.getString(columnIndex);
                        cursor.close();
                        // String picturePath contains the path of selected Image
//                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                        src = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
//                        Utils.bitmapToMat(selectedImage, src);
//                        switch (ACTION_MODE){
//                        //Add different cases here depending   on the required operation
//                            case HomeActivity.MEAN_BLUR:
//                                // original 3,3 -  13,13 not bad too
//                                Imgproc.blur(src, src, new Size(15,15));
//                                break;
//                            case HomeActivity.GAUSSIAN_BLUR:
//                                Imgproc.GaussianBlur(src, src, new Size(3,3), 0);
//                                break;
//                            case HomeActivity.MEDIAN_BLUR:
//                                Imgproc.medianBlur(src, src, 3);
//                                break;
//                            case HomeActivity.SHARPEN:
//                                Mat kernel = new Mat(3,3,CvType.CV_16SC1);
//                                kernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);
//                                Imgproc.filter2D(src, src, src.depth(), kernel);
//                                break;
//                            case HomeActivity.DILATE:
//                                Mat kernelDilate = Imgproc.getStructuringElement(
//                                        Imgproc.MORPH_RECT, new Size(3, 3));
//                                Imgproc.dilate(src, src, kernelDilate);
//                                break;
//                            case HomeActivity.ERODE:
//                                Mat kernelErode = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
//                                Imgproc.erode(src, src, kernelErode);
//                                break;
//                            case HomeActivity.THRESHOLD:
//                                Imgproc.threshold(src, src, 100, 255, Imgproc.THRESH_BINARY);
//                                break;
//                            case HomeActivity.ADAPTIVE_THRESHOLD:
//                                Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
//                                Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
//                                        Imgproc.THRESH_BINARY, 3, 0);
//                                break;
//                        }


                        //To speed up loading of image
                        createOriginalBitmap(picturePath);
//                        BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inSampleSize = 2;
//                        Bitmap temp = BitmapFactory.decodeFile(picturePath, options);
//                        //Get orientation information
//                        int orientation = 0;
//                        try {
//                            ExifInterface imgParams = new ExifInterface(picturePath);
//                            orientation = imgParams.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        //Rotating the image to get the correct orientation
//                        Matrix rotate90 = new Matrix();
//                        rotate90.postRotate(orientation);
//                        Bitmap originalBitmap = rotateBitmap(temp,orientation);
//                        //Convert Bitmap to Mat
//                        Bitmap tempBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
//                        originalMat = new Mat(tempBitmap.getHeight(), tempBitmap.getWidth(), CvType.CV_8U);
//                        Utils.bitmapToMat(tempBitmap, originalMat);
//                        currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,false);
//                        loadImageToImageView();
                        //dealing with OOM
                      //  System.gc();
                        //Code to convert Mat to Bitmap to load in an ImageView.
                        // Also load original image in imageView
//                        Bitmap processedImage = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
//                        Utils.matToBitmap(src, processedImage);

                        //ivImage.setImageBitmap(currentBitmap);
                        //ivImage.setImageBitmap(selectedImage);
                       // ivImageProcessed.setImageBitmap(processedImage);
                        loadImage(picturePath);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
//    Now, we are ready to display the image using the image view component:
    private void displayImage(Mat image)
    {
// create a bitMap
        Bitmap bitMap = Bitmap.createBitmap(image.cols(),
                image.rows(),Bitmap.Config.RGB_565);
// convert to bitmap:
        Utils.matToBitmap(image, bitMap);
// find the imageview and draw it!
//        ImageView iv = (ImageView) findViewById(R.id.IODarkRoomImageView);
        ivImage.setImageBitmap(bitMap);
    }
    /////
//    Once we have the path ready, we call the loadImage() method:
    Mat sampledImage,originalImage;
    private void loadImage(String path)
    {
//        originalImage = Highgui.imread(path);
        originalImage = Imgcodecs.imread(path);
        Mat rgbImage=new Mat();
        Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);
        Display display = getWindowManager().getDefaultDisplay();
//This is "android graphics Point" class
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        sampledImage=new Mat();
        double downSampleRatio= calculateSubSampleSize(rgbImage,width,height);
        Imgproc.resize(rgbImage, sampledImage, new Size(),downSampleRatio,downSampleRatio,Imgproc.INTER_AREA);
        try {
//            ExifInterface exif = new ExifInterface(selectedImagePath);
            ExifInterface exif = new ExifInterface(picturePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    1);
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
//get the mirrored image
                    sampledImage=sampledImage.t();
//flip on the y-axis
                    Core.flip(sampledImage, sampledImage, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
//get up side down image
                    sampledImage=sampledImage.t();
//Flip on the x-axis
                    Core.flip(sampledImage, sampledImage, 0);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    ////

    ///
    private static double calculateSubSampleSize(Mat srcImage, int reqWidth,
            int reqHeight) {
// Raw height and width of image
        final int height = srcImage.height();
        final int width = srcImage.width();
        double inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
// Calculate ratios of requested height and width to the raw
//height and width
            final double heightRatio = (double) reqHeight / (double) height;
            final double widthRatio = (double) reqWidth / (double) width;
// Choose the smallest ratio as inSampleSize value, this will
//guarantee final image with both dimensions larger than or
//equal to the requested height and width.
            inSampleSize = heightRatio<widthRatio ? heightRatio :widthRatio;
        }
        return inSampleSize;
    }
    ///
    public void createOriginalBitmap(String picturePath)
    {
        if(picturePath==null) return;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap temp = BitmapFactory.decodeFile(picturePath, options);
        //Get orientation information
        int orientation = 0;
        try {
            ExifInterface imgParams = new ExifInterface(picturePath);
            orientation = imgParams.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Rotating the image to get the correct orientation
        Matrix rotate90 = new Matrix();
        rotate90.postRotate(orientation);
        Bitmap originalBitmap = rotateBitmap(temp,orientation);
        //Convert Bitmap to Mat
        Bitmap tempBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        originalMat = new Mat(tempBitmap.getHeight(), tempBitmap.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(tempBitmap, originalMat);
        currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,false);
        loadImageToImageView();
        loadImage(picturePath);
    }

    private void loadImageToImageView()
    {
       // ImageView imgView = (ImageView) findViewById(R.id.image_view);
        ivImage.setImageBitmap(currentBitmap);
    }
    //Canny Edge Detection
    public void Canny()
    {
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        //Converting the image to grayscale
        Imgproc.cvtColor(originalMat,grayMat,Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayMat, cannyEdges,10, 100);
        //Converting Mat back to Bitmap
        Utils.matToBitmap(cannyEdges, currentBitmap);
        loadImageToImageView();
    }
    public void DifferenceOfGaussian()
    {
        Mat grayMat = new Mat();
        Mat blur1 = new Mat();
        Mat blur2 = new Mat();
//Converting the image to grayscale
        Imgproc.cvtColor(originalMat,grayMat,Imgproc.COLOR_BGR2GRAY);
//Bluring the images using two different blurring radius
        Imgproc.GaussianBlur(grayMat,blur1,new Size(15,15),5);
        Imgproc.GaussianBlur(grayMat,blur2,new Size(21,21),5);
//Subtracting the two blurred images
        Mat DoG = new Mat();
        Core.absdiff(blur1, blur2,DoG);
//Inverse Binary Thresholding
        Core.multiply(DoG,new Scalar(100), DoG);
        Imgproc.threshold(DoG,DoG,50,255,Imgproc.THRESH_BINARY_INV);
//Converting Mat back to Bitmap
        Utils.matToBitmap(DoG, currentBitmap);
        loadImageToImageView();
    }
    //Sobel Operator
    void Sobel()
    {
        Mat grayMat = new Mat();
        Mat sobel = new Mat(); //Mat to store the result
//Mat to store gradient and absolute gradient respectively
        Mat grad_x = new Mat();
        Mat abs_grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_y = new Mat();
//Converting the image to grayscale
        Imgproc.cvtColor(originalMat
                ,grayMat,Imgproc.COLOR_BGR2GRAY);
//Calculating gradient in horizontal direction
        Imgproc.Sobel(grayMat, grad_x,CvType.CV_16S, 1,0,3,1,0);
//Calculating gradient in vertical direction
        Imgproc.Sobel(grayMat, grad_y,CvType.CV_16S, 0,1,3,1,0);
//Calculating absolute value of gradients in both the direction
        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);
//Calculating the resultant gradient
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 1, sobel);
//Converting Mat back to Bitmap
        Utils.matToBitmap(sobel, currentBitmap);
        loadImageToImageView();
    }

    void HarrisCorner() {
        Mat grayMat = new Mat();
        Mat corners = new Mat();
//Converting the image to grayscale
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Mat tempDst = new Mat();
//finding corners
    Imgproc.cornerHarris(grayMat, tempDst, 2, 3, 0.04);
//Normalizing harris corner's output
        Mat tempDstNorm = new Mat();
        Core.normalize(tempDst, tempDstNorm, 0, 255, Core.NORM_MINMAX);
        Core.convertScaleAbs(tempDstNorm, corners);
//Drawing corners on a new image
        Random r = new Random();
        for (int i = 0; i < tempDstNorm.cols(); i++) {
            for (int j = 0; j < tempDstNorm.rows(); j++) {
                double[] value = tempDstNorm.get(j, i);
                if (value[0] > 150)
//                    Core.circle(corners, new Point(i, j), 5, new Scalar(r.nextInt(255)), 2);
                     Imgproc.circle(corners, new Point(i, j), 5, new Scalar(r.nextInt(255)), 2);
            }
        }
//Converting Mat back to Bitmap
        Utils.matToBitmap(corners, currentBitmap);
        loadImageToImageView();
    }

    void HoughLines()
    {
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat lines = new Mat();
//Converting the image to grayscale
        Imgproc.cvtColor(originalMat,grayMat,Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayMat, cannyEdges,10, 100);
        Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI/180, 50, 20, 20);
        Mat houghLines = new Mat();
        houghLines.create(cannyEdges.rows(), cannyEdges.cols(),CvType.CV_8UC1);

//Drawing lines on the image
        for(int i = 0 ; i < lines.cols() ; i++)
        {
            double[] points = lines.get(0,i);
            double x1, y1, x2, y2;
            x1 = points[0];
            y1 = points[1];
            x2 = points[2];
            y2 = points[3];
            Point pt1 = new Point(x1, y1);
            Point pt2 = new Point(x2, y2);
//Drawing lines on an image
//            Core.line(houghLines, pt1, pt2, new Scalar(255, 0, 0), 1);
            Imgproc.line(houghLines, pt1, pt2, new Scalar(255, 0, 0), 1);
        }
//Converting Mat back to Bitmap
        Utils.matToBitmap(houghLines, currentBitmap);
        loadImageToImageView();
    }

    void HoughCircles()
    {
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat circles = new Mat();
//Converting the image to grayscale
        Imgproc.cvtColor(originalMat,grayMat,Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayMat, cannyEdges,10, 100);
        Imgproc.HoughCircles(cannyEdges, circles, Imgproc.CV_HOUGH_GRADIENT,1, cannyEdges.rows() / 15);
//, grayMat.rows() / 8);
        Mat houghCircles = new Mat();
        houghCircles.create(cannyEdges.rows(),cannyEdges.cols(),CvType.CV_8UC1);
//Drawing lines on the image
        for(int i = 0 ; i < circles.cols() ; i++)
        {
            double[] parameters = circles.get(0,i);
            double x, y;
            int r;
            x = parameters[0];
            y = parameters[1];
            r = (int)parameters[2];
            Point center = new Point(x, y);
//Drawing circles on an image
//            Core.circle(houghCircles,center,r, new Scalar(255,0,0),1);
            Imgproc.circle(houghCircles,center,r, new Scalar(255,0,0),1);
        }
//Converting Mat back to Bitmap
        Utils.matToBitmap(houghCircles, currentBitmap);
        loadImageToImageView();
    }

    void Contours()
    {
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contourList = new ArrayList<MatOfPoint>();
//A list to store all the contours

//Converting the image to grayscale
        Imgproc.cvtColor(originalMat,grayMat,Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayMat, cannyEdges,10, 100);
//finding contours
        Imgproc.findContours(cannyEdges,contourList,hierarchy,Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//Drawing contours on a new image
        Mat contours = new Mat();
        contours.create(cannyEdges.rows(),cannyEdges.cols(),CvType.CV_8UC3);
        Random r = new Random();
        for(int i = 0; i < contourList.size(); i++)
        {
            Imgproc.drawContours(contours
                    ,contourList,i,new Scalar(r.nextInt(255)
                            ,r.nextInt(255),r.nextInt(255)), -1);
        }
//Converting Mat back to Bitmap
        Utils.matToBitmap(contours, currentBitmap);
        loadImageToImageView();
    }
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }

    }
    static int REQUEST_READ_EXTERNAL_STORAGE = 0;
    static boolean read_external_storage_granted = false;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                Log.i("permission", "READ_EXTERNAL_STORAGE granted");
                read_external_storage_granted = true;
            } else {
                // permission denied
                Log.i("permission", "READ_EXTERNAL_STORAGE denied");
            }
        }
    }

}