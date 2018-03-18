package primeno.naval.com.primenumberusingndk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;

import android.support.v7.widget.AppCompatSeekBar;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CtrlBrightnessActivity extends AppCompatActivity
{
    static String TAG="TagCtrlBrightness";
    //opencv can be loaded from one place in memory that it
//    static {
//        if(!OpenCVLoader.initDebug()){
//            Log.d(TAG, "OpenCV not loaded");
//        } else {
//            Log.d(TAG, "OpenCV loaded");
//        }
//    }
    @BindView(R.id.iv_image)
     ImageView iv_image;
    @BindView(R.id.sb_brightness)
     AppCompatSeekBar sb_brightness;
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ctrl_brigthness);
        ButterKnife.bind(this);
        image = BitmapFactory.decodeResource(getResources(),R.drawable.brighttest);
        iv_image.setImageBitmap(image);
        sb_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                Bitmap edited = increaseBrightness(image,progress);
                iv_image.setImageBitmap(edited);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
    }
    /***
     * The brightness is changed using increaseBrightness() method. We cant use the bitmap directly with OpenCV.
     * It should be converted to Mat variable.
     * OpenCV process all the images as Mat (Matrix). New Mat object is created with size of bitmap.
     *
     * Mat src = new Mat(bitmap.getHeight(),bitmap.getWidth(), CvType.CV_8UC1);
     The Bitmap is copied to Mat object using Utils.bitmapToMat() method.
     To change the brightness we need to use the convertTo() method on the Mat object.
     The destination Mat object, rtype , alpha and beta values should be passed.
     Each pixel of the image is multiplied with alpha value and beta value is added. rtype is the output matrix type.
     Then we convert the Mat object back to Bitmap by using Utils.matToBitmap() method and return it.
     * **/
    private Bitmap increaseBrightness(Bitmap bitmap, int value){

        Mat src = new Mat(bitmap.getHeight(),bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap,src);
        src.convertTo(src,-1,1,value);
        Bitmap result = Bitmap.createBitmap(src.cols(),src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,result);
        return result;
    }
}
