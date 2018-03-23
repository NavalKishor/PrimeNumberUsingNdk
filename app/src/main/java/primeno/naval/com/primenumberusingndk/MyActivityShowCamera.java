package primeno.naval.com.primenumberusingndk;




/****
 Show Camera on the Screen
1 Edit Manifest
    1.1 Add permission to allow camera.
    1.2 Support various screen sizes and adaptability of camera.
    1.3 Use specific features of the camera.
2 Add a custom Layout show_camera.xml to display camera in it.
3 Edit MyActivityShowCamera.java
    3.1 Import required android classes
    3.2 Import OpenCV classes
    3.3 Connect to OpenCV manager
    3.4 Initiate LogCat to log events of out app
    3.5 SHOW CAMERA ON LAYOUT
    3.6 Corrected Orientation (portrait)
*/
/* 3.1 Import Android Class*/
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

/* 3.2 Import OpenCV Class*/
import org.opencv.android.JavaCameraView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyActivityShowCamera extends AppCompatActivity
{
    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    //This variable acts as a bridge between camera and OpenCV library.
    @BindView(R.id.show_camera_activity_java_surface_view)
     CameraBridgeViewBase mOpenCvCameraView;

    // Used in Camera selection from menu (when implemented)
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    /****
     * Now, lets call OpenCV manager to help our app communicate with android phone to make OpenCV work
     * you may want to perform some actions
     * */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_my_show_camera);
        ButterKnife.bind(this);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(new CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height)
            {
                /***
                 * Receive Image Data when the camera preview starts on your screen
                 * */
                mRgba = new Mat(height, width, CvType.CV_8UC4);
                mRgbaF = new Mat(height, width, CvType.CV_8UC4);
                mRgbaT = new Mat(width, width, CvType.CV_8UC4);

            }

            @Override
            public void onCameraViewStopped()
            {
                /****
                 * Destroy image data when you stop camera preview on your phone screen
                 * */
                mRgba.release();
            }

            @Override
            public Mat onCameraFrame(CvCameraViewFrame inputFrame)
            {
                /***
                 * Now, this one is interesting! OpenCV orients the camera to left by 90 degrees.
                 * So if the app is in portrait more, camera will be in -90 or 270 degrees orientation.
                 * We fix that in the next and the most important function. There you go!
                 * ***/
                // TODO Auto-generated method stub
                mRgba = inputFrame.rgba();
                // Rotate mRgba 90 degrees
                Core.transpose(mRgba, mRgbaT);
                Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
                Core.flip(mRgbaF, mRgba, 1 );

                return mRgba; // This function must return
            }
        });

    }
    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }



}
