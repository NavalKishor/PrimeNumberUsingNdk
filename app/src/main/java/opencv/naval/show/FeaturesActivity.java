package opencv.naval.show;

import android.Manifest;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
        }else if (id == R.id    .reset) {
            //Apply to rest the image to original image
            createOriginalBitmap(picturePath);
        }

        return super.onOptionsItemSelected(item);
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
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

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
        Core.addWeighted(abs_grad_x, 0.5,
                abs_grad_y, 0.5, 1, sobel);
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