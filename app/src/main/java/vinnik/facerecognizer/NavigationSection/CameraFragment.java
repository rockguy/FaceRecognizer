package vinnik.facerecognizer.NavigationSection;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import vinnik.facerecognizer.R;

import static org.opencv.core.Core.rectangle;

/**
 * Created by vinnik on 02.03.2017.
 */

public class CameraFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener {

    public static final String TAG = "CameraFragmentTag";
    Mat currentImage;
    Rect[] facesArray;
    private CameraBridgeViewBase openCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private int absoluteFaceSize;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this.getContext()) {
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
        try {
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = this.getContext().getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }

        openCvCameraView.enableView();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("lol", "lol");
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.camera_fragment,null);
    }

    @Override
    public void onStart() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        openCvCameraView = new JavaCameraView(this.getContext(), 1);
        openCvCameraView.setOnClickListener(onClickListener);
        LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.content_contaiter);
        ll.addView(openCvCameraView);

        openCvCameraView.setCvCameraViewListener(this);

        super.onStart();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        absoluteFaceSize = (int) (height * 0.01);
    }

//    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame aInputFrame) {
//        currentImage = aInputFrame.gray();
//        // Create a grayscale image
//        //Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
//        MatOfRect faces = new MatOfRect();
//        // Use the classifier to detect faces
//        if (cascadeClassifier != null) {
//            cascadeClassifier.detectMultiScale(currentImage, faces, 1.1, 2, 2,
//                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
//        }
//        facesArray = faces.toArray();
//        for (int i = 0; i <facesArray.length; i++)
//            rectangle(currentImage, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
//
//        return currentImage;
//    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        currentImage = inputFrame;
        // Create a grayscale image
        //Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
        MatOfRect faces = new MatOfRect();
        // Use the classifier to detect faces
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(currentImage, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            rectangle(currentImage, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

        return currentImage;
    }
}
