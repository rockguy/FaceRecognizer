package vinnik.facerecognizer.NavigationSection;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
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

public class CameraLayout extends Activity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.

    private static final String TAG = "MainActivity";
    Mat currentImage;
    Mat currentImage2;
    Rect[] facesArray;
    SQLiteDatabase db;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "Opencv successfully loaded");
        } else {
            Log.d(TAG, "OpenCV not loaded");
        }
    }

    private CameraBridgeViewBase openCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private int absoluteFaceSize;


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
        try {
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
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
//
//            File sd = new File(Environment.getExternalStorageDirectory() + "/frames/tempBitmap");
//
//            //Очистка предыдущих временных фоток
//            for (File f:sd.listFiles()
//                 ) {
//                f.delete();
//            }
//
//            for (int i = 0; i < facesArray.length; i++) {
//                Mat face = new Mat(currentImage2, facesArray[i]);
//                Bitmap bmp = null;
//                try {
//                    bmp = Bitmap.createBitmap(face.cols(), face.rows(), Bitmap.Config.ARGB_8888);
//                    Utils.matToBitmap(face, bmp);
//                } catch (CvException e) {
//                    Log.d(TAG, e.getMessage());
//                }
//                face.release();
//                saveToSDCard(bmp,"tempBitmap");
//            }
//            finish();
        }
    };

//    void saveToSDCard(Bitmap bmp, String filename){
//        FileOutputStream out = null;
//        File sd = new File(Environment.getExternalStorageDirectory() + "/frames/"+filename);
//        boolean success = true;
//        if (!sd.exists()) {
//            success = sd.mkdir();
//        }
//        if (success) {
//            filename=filename+sd.list().length;
//            File dest = new File(sd, filename+".png");
//            try {
//                out = new FileOutputStream(dest);
//                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (out != null) {
//                        out.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        openCvCameraView = new JavaCameraView(this, -1);
        openCvCameraView.setOnClickListener(onClickListener);
        setContentView(openCvCameraView);
        openCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        absoluteFaceSize = (int) (height * 0.01);
    }


    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame aInputFrame) {
        currentImage = aInputFrame.gray();
        currentImage2 = aInputFrame.rgba();
        // Create a grayscale image
        //Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
        MatOfRect faces = new MatOfRect();
        // Use the classifier to detect faces
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(currentImage, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        facesArray = faces.toArray();
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
