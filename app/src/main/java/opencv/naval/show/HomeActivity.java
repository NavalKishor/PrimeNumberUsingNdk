package opencv.naval.show;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import primeno.naval.com.primenumberusingndk.R;

public class HomeActivity extends AppCompatActivity
{

    public static final int MEAN_BLUR = 1;
    public static final int GAUSSIAN_BLUR = 2;
    public static final int MEDIAN_BLUR = 3;
    public static final int SHARPEN = 4;
    public static final int DILATE = 5;
    public static final int ERODE = 6;
    public static final int THRESHOLD = 7;
    public static final int ADAPTIVE_THRESHOLD = 8;
    static String TAG="TagHome";

    @BindView(R.id.bMean)
    Button bMean;
    @BindView(R.id.bGaussian)
    Button bGaussian;
    @BindView(R.id.bMedian)
    Button  bMedian;
    @BindView(R.id.bSharpen)
    Button  bSharpen;
    @BindView(R.id.bDilate)
    Button  bDilate;
    @BindView(R.id.bErode)
    Button  bErode;
    @BindView(R.id.bThreshold)
    Button  bThreshold;
    @BindView(R.id.bAdaptiveThreshold)
    Button  bAdaptiveThreshold;
    @BindView(R.id.brotation)
    Button  brotation;
    @BindView(R.id.bShare)
    Button  bShare;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        bMean.setOnClickListener(onClickListener);
        bGaussian.setOnClickListener(onClickListener);
        bMedian.setOnClickListener(onClickListener);
        bSharpen.setOnClickListener(onClickListener);
        bDilate.setOnClickListener(onClickListener);
        bErode.setOnClickListener(onClickListener);
        bThreshold.setOnClickListener(onClickListener);
        bAdaptiveThreshold.setOnClickListener(onClickListener);
        brotation.setOnClickListener(onClickListener);
        bShare.setOnClickListener(onClickListener);
        getApkName(this);

    }


    View.OnClickListener onClickListener= new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        int mode=-1;
        switch(v.getId()){
            case R.id.bMean:
                mode=MEAN_BLUR;
                break;
            case R.id.bGaussian :
                mode=GAUSSIAN_BLUR;
                break;
                case R.id.bMedian :
                    mode=MEDIAN_BLUR;
                break;
                case R.id.bSharpen :
                    mode=SHARPEN;
                break;
                case R.id.bDilate :
                    mode=DILATE;
                break;
                case R.id.bErode :
                    mode=ERODE;
                break;
                case R.id.bThreshold :
                    mode=THRESHOLD;
                break;
                case R.id.bAdaptiveThreshold :
                    mode=ADAPTIVE_THRESHOLD;
                break;
                case R.id.brotation :
                   // mode=ADAPTIVE_THRESHOLD;
                Intent i = new Intent(getApplicationContext(), FeaturesActivity.class);
                i.putExtra("ACTION_MODE", mode);
                startActivity(i);
                return;
            case R.id.bShare:
                shareApplication();
                return;


        }
        Intent i = new Intent(getApplicationContext(), ImagesActivity.class);
        i.putExtra("ACTION_MODE", mode);
        startActivity(i);
    }
};


    /**
     * Get the apk path of this application.
     * @param context any context (e.g. an Activity or a Service)
     * @return full apk file path, or null if an exception happened (it should not happen)
     */
    public static String getApkName(Context context) {
        String packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            String apk = ai.publicSourceDir;
            Log.d(TAG, "getApkName: "+ai.toString());
            Log.d(TAG, "getApkName: "+ai.sourceDir+ai.backupAgentName+ai.className+ai.dataDir+ai.manageSpaceActivityName
                    +ai.nativeLibraryDir+ai.publicSourceDir+ai.processName+ai.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                Log.d(TAG, "getApkName: "+ Arrays.toString(ai.splitPublicSourceDirs));
            }
            return apk;
        } catch (Throwable x) {
            x.printStackTrace();
        }

        return null;
    }

    private void shareApplication() {
        ApplicationInfo app = getApplicationContext().getApplicationInfo();
        String filePath = app.sourceDir;

        Intent intent = new Intent(Intent.ACTION_SEND);

        // MIME of .apk is "application/vnd.android.package-archive".
        // but Bluetooth does not accept this. Let's use "*/*" instead.
        intent.setType("*/*");

        // Append file and send Intent
        File originalApk = new File(filePath);

        try {
            //Make new directory in new location
            File tempFile = new File(getExternalCacheDir() + "/ExtractedApk");
            //If directory doesn't exists create new
            if (!tempFile.isDirectory())
                if (!tempFile.mkdirs())
                    return;
            //Get application's name and convert to lowercase
            tempFile = new File(tempFile.getPath() + "/" + getString(app.labelRes).replace(" ","").toLowerCase() + ".apk");
            //If file doesn't exists create new
            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return;
                }
            }
            //Copy file to new location
            InputStream in = new FileInputStream(originalApk);
            OutputStream out = new FileOutputStream(tempFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            System.out.println("File copied.");
            //Open share dialog
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempFile));
            startActivity(Intent.createChooser(intent, "Share app via"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
