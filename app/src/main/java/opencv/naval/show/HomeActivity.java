package opencv.naval.show;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

        }
        Intent i = new Intent(getApplicationContext(), ImagesActivity.class);
        i.putExtra("ACTION_MODE", mode);
        startActivity(i);
    }
};
}
