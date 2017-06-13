package vinnik.facerecognizer.NavigationSection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.io.Serializable;

import Support.HelpClass;

import static Support.HelpClass.AddPerson;
import static Support.HelpClass.DetectFaces;
import static Support.HelpClass.RecognizePersons;
import static Support.HelpClass.clearDir;
import static Support.HelpClass.currentStatus;
import static Support.HelpClass.setCascadeClassifier;
import static org.opencv.core.Core.rectangle;
import static vinnik.facerecognizer.NavigationSection.MainActivity.path;

public class CameraLayout extends Activity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.

    private static final String TAG = "MainActivity";
    Mat currentImage;
    Mat currentImage2;
    Mat currentImageGray;
    Rect[] facesArray;
    private Context context;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "Opencv successfully loaded");
        } else {
            Log.d(TAG, "OpenCV not loaded");
        }
    }

    private CameraBridgeViewBase openCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void initializeOpenCVDependencies() {
        setCascadeClassifier(getBaseContext());
        openCvCameraView.enableView();
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (facesArray.length == 0) {
//                setResult(Activity.RESULT_CANCELED);
//                finish();
                Toast.makeText(context, "Нет лиц на фотографии", Toast.LENGTH_SHORT).show();
                return;
            }

            clearDir(getApplicationContext(), path);

            if (getIntent().getSerializableExtra("CameraState").equals(MainActivity.CameraState.RecognizePerson)) {
                Toast.makeText(getBaseContext(), "Photo is Sending", Toast.LENGTH_LONG).show();
                Object[] data = RecognizePersons(path, currentImage2, facesArray);
                currentStatus = HelpClass.CurrentStatus.Neutral;
                Intent intent = new Intent();
                intent.putExtra("Photos", (Serializable) data[0]);
                intent.putExtra("Persons", (Serializable) data[1]);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else if (getIntent().getSerializableExtra("CameraState").equals(MainActivity.CameraState.AddPerson)) {
                if (facesArray.length != 1) {
                    Toast.makeText(context, "На фотографии должно быть только одно лицо", Toast.LENGTH_SHORT).show();
                    return;
                }
                String filePath = AddPerson(path, currentImage2, facesArray);
                if (filePath == "") return;
                Intent intent = new Intent();
                intent.putExtra("FaceData", path + "//" + filePath);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            ;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        openCvCameraView = new JavaCameraView(this, -1);
        openCvCameraView.setOnClickListener(onClickListener);
        setContentView(openCvCameraView);
        openCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }


    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame aInputFrame) {
        currentImage = aInputFrame.rgba();
        currentImage2 = currentImage.clone();
        currentImageGray = aInputFrame.gray();

        facesArray = DetectFaces(currentImageGray);
        for (Rect aFacesArray : facesArray)
            rectangle(currentImage, aFacesArray.tl(), aFacesArray.br(), new Scalar(0, 255, 0, 255), 3);

        return currentImage;
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }
}
