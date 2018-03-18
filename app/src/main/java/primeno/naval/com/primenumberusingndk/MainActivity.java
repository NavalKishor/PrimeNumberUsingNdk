package primeno.naval.com.primenumberusingndk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{

    static String TAG="TagMain";
    // Used to load the 'native-lib' library on application startup.
    static
    {
        System.loadLibrary("opencv_java3");
        if (OpenCVLoader.initDebug())
        {
            Log.d(TAG, "static initializer:OpenCV Loaded");
        }
        else
        {
            Log.d(TAG, "static initializer:OpenCV not Loaded");
        }
        System.loadLibrary("native-lib");

    }
    @BindView(R.id.sample_text)
    TextView sample_text;
    @BindView(R.id.number)
    EditText number;
    @BindView(R.id.primenumber)
    Button primenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        // Example of a call to a native method

        sample_text.setText(stringFromJNI());
        sample_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(MainActivity.this,CtrlBrightnessActivity.class));
            }
        });
        primenumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String strno=number.getText().toString();
                if(strno!=null ||!strno.isEmpty())
                {
                    int no=Integer.parseInt(strno);
                    if(isPrime(no))
                    {
                        Toast.makeText(getApplicationContext(),"Prime number",Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"not a Prime number",Toast.LENGTH_LONG).show();

                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please enter number",Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native boolean isPrime(int n);
}
