package opencv.naval.show;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
//import org.opencv.highgui.Highgui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import primeno.naval.com.primenumberusingndk.R;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by m1035364 on 24/3/18.
 */

public class DocumentScanningActivity extends AppCompatActivity
{
    @BindView(R.id.bClickImage)
    Button bClickImage;
    @BindView(R.id.bLoadImage)
    Button bLoadImage;
    @BindView(R.id.ivImage)
    ImageView ivImage;
    String TAG="TagDocumentScanning";
    String errorMsg=null,FILE_LOCATION;
    final int CLICK_PHOTO=111,LOAD_PHOTO=222;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_scanning);
        ButterKnife.bind(this);

        FILE_LOCATION= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        bClickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(DocumentScanningActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(DocumentScanningActivity.this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                    return;
                }
                if (ContextCompat.checkSelfPermission(DocumentScanningActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(DocumentScanningActivity.this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                    return;
                }

//                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//                StrictMode.setVmPolicy(builder.build());
                // create Intent to take a picture and return control to the calling application
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                errorMsg = null;
//                File imagesFolder = new File(FILE_LOCATION,"Naval");
//                if(!imagesFolder.exists())
//                    imagesFolder.mkdirs();
//                File image = new File(imagesFolder, "image_"+ System.currentTimeMillis()+".jpg");
//                Uri fileUri = Uri.fromFile(image);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//                {
//                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    fileUri = FileProvider.getUriForFile(DocumentScanningActivity.this, "primeno.naval.com.primenumberusingndk.fileProvider", image);
//
//                }
//                MimeTypeMap mime = MimeTypeMap.getSingleton();
//                String ext = image.getName().substring(image.getName().lastIndexOf(".") + 1);
//                String type = mime.getMimeTypeFromExtension(ext);
//                intent.setDataAndType(fileUri, type);
//                Log.d(TAG, "File URI = " + fileUri.toString());
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                // start the image capture Intent
                startActivityForResult(intent, CLICK_PHOTO);




              //  fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
               // intents.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                // start the image capture Intent
              //  startActivityForResult(intent, CLICK_PHOTO);

            }
        });
        bLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                errorMsg = null;
                startActivityForResult(intent, LOAD_PHOTO);
            }
        });
    }
    static int scaleFactor;
    Mat src;
    Mat srcOrig;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, requestCode + " " + CLICK_PHOTO +" " + resultCode + " " + RESULT_OK);
        //string path of image
        switch(requestCode) {
            case CLICK_PHOTO:
                if(resultCode == RESULT_OK)
                {
                    try
                    {
                       Bitmap bitmap= onCaptureImageResult(data);
                        final Uri fileUri = data.getData();
                        ivImage.setImageBitmap(bitmap);
                       // Log.d(TAG, fileUri.toString());
                        final InputStream imageStream = getContentResolver().openInputStream(fileUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                         srcOrig = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
                         src = new Mat();
                        Utils.bitmapToMat(selectedImage, srcOrig);
                         scaleFactor = calcScaleFactor(srcOrig.rows(), srcOrig.cols());
                        Imgproc.resize(srcOrig, src, new Size(srcOrig.rows()/scaleFactor, srcOrig.cols()/scaleFactor));
                        getPage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case LOAD_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        InputStream stream = getContentResolver().openInputStream(data.getData());
                        final Bitmap selectedImage = BitmapFactory.decodeStream(stream);
                        stream.close();
                        ivImage.setImageBitmap(selectedImage);
                         srcOrig = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
                        Imgproc.cvtColor(srcOrig, srcOrig, Imgproc.COLOR_BGR2RGB);
                        Utils.bitmapToMat(selectedImage, srcOrig);
                         scaleFactor = calcScaleFactor(srcOrig.rows(), srcOrig.cols());
                         src = new Mat();
                        Imgproc.resize(srcOrig, src, new Size(srcOrig.rows()/scaleFactor, srcOrig.cols()/scaleFactor));
                        Imgproc.GaussianBlur(src, src, new Size(5,5), 1);
                        getPage();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

        }
    }

    private void getPage()
    {
        MyAsyncTask myAsyncTask=new MyAsyncTask();
        myAsyncTask.setOnBitmapProcessed(new MyAsyncTask.OnBitmapProcessed() {
            @Override
            public void processed(Bitmap bitmap)
            {
                ivImage.setImageBitmap(bitmap);
            }
        });
        myAsyncTask.setSrc(src);
        myAsyncTask.execute();
    }

    private static int calcScaleFactor(int rows, int cols){
        int idealRow, idealCol;
        if(rows<cols){
            idealRow = 240;
            idealCol = 320;
        } else {
            idealCol = 240;
            idealRow = 320;
        }
        int val = Math.min(rows / idealRow, cols / idealCol);
        if(val<=0){
            return 1;
        } else {
            return val;
        }
    }




    static void sortCorners(ArrayList<Point> corners)
    {
        ArrayList<Point> top, bottom;
        top = new ArrayList<Point>();
        bottom = new ArrayList<Point>();
        Point center = new Point();
        for(int i=0; i<corners.size(); i++){
            center.x += corners.get(i).x/corners.size();
            center.y += corners.get(i).y/corners.size();
        }
        for (int i = 0; i < corners.size(); i++)
        {
            if (corners.get(i).y < center.y)
                top.add(corners.get(i));
            else
                bottom.add(corners.get(i));
        }
        corners.clear();
        if (top.size() == 2 && bottom.size() == 2){
            Point top_left = top.get(0).x > top.get(1).x ?
                    top.get(1) : top.get(0);
            Point top_right = top.get(0).x > top.get(1).x ?
                    top.get(0) : top.get(1);
            Point bottom_left = bottom.get(0).x > bottom.get(1).x
                    ? bottom.get(1) : bottom.get(0);
            Point bottom_right = bottom.get(0).x > bottom.get(1).x
                    ? bottom.get(0) : bottom.get(1);
            top_left.x *= scaleFactor;
            top_left.y *= scaleFactor;
            top_right.x *= scaleFactor;
            top_right.y *= scaleFactor;
            bottom_left.x *= scaleFactor;
            bottom_left.y *= scaleFactor;
            bottom_right.x *= scaleFactor;
            bottom_right.y *= scaleFactor;
            corners.add(top_left);
            corners.add(top_right);
            corners.add(bottom_right);
            corners.add(bottom_left);
        }
    }

    /// start
    public static void correctPerspective() {

        String fileName = "IMG_20141024_132131.jpg";
//        fileName="!@#$%^&*()-+";fileName="\\";
//        Mat imgSource = Highgui.imread(fileName, Highgui.CV_LOAD_IMAGE_UNCHANGED);
        Mat imgSource = Imgcodecs.imread(fileName, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        // convert the image to black and white does (8 bit)
        Imgproc.Canny(imgSource.clone(), imgSource, 50, 50);

        // apply gaussian blur to smoothen lines of dots
        Imgproc.GaussianBlur(imgSource, imgSource, new org.opencv.core.Size(5, 5), 5);

        // find the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imgSource, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        MatOfPoint temp_contour = contours.get(0); // the largest is at the
        // index 0 for starting
        // point
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            // compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                // check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                int contourSize = (int) temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize * 0.05, true);
                if (approxCurve_temp.total() == 4) {
                    maxArea = contourarea;
                    approxCurve = approxCurve_temp;
                }
            }
        }

        Imgproc.cvtColor(imgSource, imgSource, Imgproc.COLOR_BayerBG2RGB);
//        Mat sourceImage = Highgui.imread(fileName, Highgui.CV_LOAD_IMAGE_UNCHANGED);
        Mat sourceImage = Imgcodecs.imread(fileName, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        double[] temp_double;
        temp_double = approxCurve.get(0, 0);
        Point p1 = new Point(temp_double[0], temp_double[1]);
        // Core.circle(imgSource,p1,55,new Scalar(0,0,255));
        // Imgproc.warpAffine(sourceImage, dummy, rotImage,sourceImage.size());
        temp_double = approxCurve.get(1, 0);
        Point p2 = new Point(temp_double[0], temp_double[1]);
        // Core.circle(imgSource,p2,150,new Scalar(255,255,255));
        temp_double = approxCurve.get(2, 0);
        Point p3 = new Point(temp_double[0], temp_double[1]);
        // Core.circle(imgSource,p3,200,new Scalar(255,0,0));
        temp_double = approxCurve.get(3, 0);
        Point p4 = new Point(temp_double[0], temp_double[1]);
        // Core.circle(imgSource,p4,100,new Scalar(0,0,255));
        List<Point> source = new ArrayList<Point>();
        source.add(p1);
        source.add(p2);
        source.add(p3);
        source.add(p4);
        Mat startM = Converters.vector_Point2f_to_Mat(source);
        Mat result = warp(sourceImage, startM);

//        Highgui.imwrite("corrected.jpg", result);
        Imgcodecs.imwrite("corrected.jpg", result);
    }

    public static Mat warp(Mat inputMat, Mat startM) {

        int resultWidth = 1200;
        int resultHeight = 680;

        Point ocvPOut4 = new Point(0, 0);
        Point ocvPOut1 = new Point(0, resultHeight);
        Point ocvPOut2 = new Point(resultWidth, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, 0);

        if (inputMat.height() > inputMat.width()) {
            // int temp = resultWidth;
            // resultWidth = resultHeight;
            // resultHeight = temp;

            ocvPOut3 = new Point(0, 0);
            ocvPOut4 = new Point(0, resultHeight);
            ocvPOut1 = new Point(resultWidth, resultHeight);
            ocvPOut2 = new Point(resultWidth, 0);
        }

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);

        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new Size(resultWidth, resultHeight), Imgproc.INTER_CUBIC);

        return outputMat;
    }
    /// end


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                bClickImage.setEnabled(true);
            }
        }
    }

    public Bitmap onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 60, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                "image_"+System.currentTimeMillis() + ".jpg");
        data.setData(Uri.fromFile(destination));
//        data.putExtra("imgPath",destination.getAbsolutePath());
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
            String url = MediaStore.Images.Media.insertImage(getContentResolver(), destination.getAbsolutePath(), destination.getName(), destination.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return thumbnail;
    }

}
