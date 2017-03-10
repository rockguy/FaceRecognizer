package vinnik.facerecognizer.NavigationSection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import Models.Person;
import io.realm.Realm;
import vinnik.facerecognizer.R;

import static org.opencv.core.Core.rectangle;
import static org.opencv.imgproc.Imgproc.resize;

public class CameraLayout extends Activity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.

    private static final String TAG = "MainActivity";
    Mat currentImage;
    Mat currentImageGray;
    Rect[] facesArray;
    Realm realm;

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

//            final Person defaultPerson = realm.where(Person.class).equalTo("FirstName","defaultPerson").findAll().first();
//            realm.executeTransaction(new Realm.Transaction() {
//                @Override
//                public void execute(Realm realm) {
//                    defaultPerson.Faces.deleteAllFromRealm();
//                }
//            });
            for (int i = 0; i < facesArray.length; i++) {
                Mat face = new Mat(currentImageGray, facesArray[i]);
                //Стандартизация размера лиц
                resize(face, face, new Size(100, 100));
                Mat face2 = new Mat(currentImage, facesArray[i]);
                resize(face2, face2, new Size(100, 100));
                Bitmap bmp = null;
                try {
                    bmp = Bitmap.createBitmap(face.cols(), face.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(face, bmp);
                } catch (CvException e) {
                    Log.d(TAG, e.getMessage());
                }
                Bitmap bmp2 = null;
                try {
                    bmp2 = Bitmap.createBitmap(face2.cols(), face2.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(face2, bmp2);
                } catch (CvException e) {
                    Log.d(TAG, e.getMessage());
                }
                face2.release();

                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

                final ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
                //final int finalI = i;
                if (realm.where(Person.class).equalTo("FirstName", "defaultName").equalTo("LastName", "defaultName").findAll().size() <= 0) {
                    realm.beginTransaction();
                    Person p = realm.createObject(Person.class);
                    p.FirstName = "defaultName";
                    p.LastName = "defaultName";
                    realm.commitTransaction();
                }

                Person p = realm.where(Person.class).equalTo("FirstName", "defaultName").equalTo("LastName", "defaultName").findAll().first();

                realm.beginTransaction();
                Models.Photo photo = realm.createObject(Models.Photo.class);
                photo.RecFace = stream.toByteArray();
                photo.Face = stream2.toByteArray();
                p.Faces.add(photo);
                photo.Owner = p;
                realm.commitTransaction();

            }

            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        realm = Realm.getInstance(MainActivity.realmConfig);

        openCvCameraView = new JavaCameraView(this, -1);
        openCvCameraView.setOnClickListener(onClickListener);
        setContentView(openCvCameraView);
        openCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        absoluteFaceSize = (int) (100);
    }


    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame aInputFrame) {
        currentImage = aInputFrame.rgba();
        currentImageGray = aInputFrame.gray();
        // Create a grayscale image
        //Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
        MatOfRect faces = new MatOfRect();
        // Use the classifier to detect faces
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(currentImageGray, faces, 1.1, 2, 2,
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
