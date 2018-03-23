package opencv.naval.show;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import primeno.naval.com.primenumberusingndk.R;

public class ImagesActivity extends AppCompatActivity
{


    private final int SELECT_PHOTO = 1;
    @BindView(R.id.ivImage)
    ImageView ivImage;
    @BindView(R.id.ivImageProcessed)
    ImageView ivImageProcessed;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if(intent.hasExtra("ACTION_MODE"))
        {
            ACTION_MODE = intent.getIntExtra("ACTION_MODE", 0);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        src = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
                        Utils.bitmapToMat(selectedImage, src);
                        switch (ACTION_MODE){
//Add different cases here depending   on the required operation
                            case HomeActivity.MEAN_BLUR:
                                // original 3,3 -  13,13 not bad too
                                Imgproc.blur(src, src, new Size(15,15));
                                break;
                            case HomeActivity.GAUSSIAN_BLUR:
                                Imgproc.GaussianBlur(src, src, new Size(3,3), 0);
                                break;
                            case HomeActivity.MEDIAN_BLUR:
                                Imgproc.medianBlur(src, src, 3);
                                break;
                            case HomeActivity.SHARPEN:
                                Mat kernel = new Mat(3,3,CvType.CV_16SC1);
                                kernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);
                                Imgproc.filter2D(src, src, src.depth(), kernel);
                                break;
                            case HomeActivity.DILATE:
                                Mat kernelDilate = Imgproc.getStructuringElement(
                                        Imgproc.MORPH_RECT, new Size(3, 3));
                                Imgproc.dilate(src, src, kernelDilate);
                                break;
                            case HomeActivity.ERODE:
                                Mat kernelErode = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
                                Imgproc.erode(src, src, kernelErode);
                                break;
                            case HomeActivity.THRESHOLD:
                                Imgproc.threshold(src, src, 100, 255, Imgproc.THRESH_BINARY);
                                break;
                            case HomeActivity.ADAPTIVE_THRESHOLD:
                                Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
                                Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                                        Imgproc.THRESH_BINARY, 3, 0);
                                break;
                        }
                        //dealing with OOM
                        System.gc();
//Code to convert Mat to Bitmap to load in an ImageView.
// Also load original image in imageView
                        Bitmap processedImage = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(src, processedImage);

                        ivImage.setImageBitmap(selectedImage);
                        ivImageProcessed.setImageBitmap(processedImage);
                    }catch (Exception e){

                    }
                }
                break;
        }
    }
}