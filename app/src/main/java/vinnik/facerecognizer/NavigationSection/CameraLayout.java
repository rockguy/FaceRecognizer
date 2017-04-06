package vinnik.facerecognizer.NavigationSection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Models.Person;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vinnik.facerecognizer.R;

import static org.opencv.core.Core.hconcat;
import static org.opencv.core.Core.rectangle;
import static org.opencv.imgproc.Imgproc.resize;

public class CameraLayout extends Activity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.

    private static final String TAG = "MainActivity";
    Mat currentImage;
    Mat currentImage2;
    Mat currentImageGray;
    Rect[] facesArray;


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
        public void onClick(final View view) {
            final File path = new File(Environment.getExternalStorageDirectory() + "/Temp/");
            if (!path.exists()) {
                path.mkdir();
            }
            for (String s : path.list()
                    ) {
                File f = new File(path + "//" + s);
                f.delete();
            }

            if (getIntent().getSerializableExtra("CameraState").equals(MainActivity.CameraState.RecognizePerson)) {
                File file = getOneFile(path, currentImage2, facesArray);
                RequestBody requestFile = RequestBody.create(MediaType.parse("data:image/jpg;base64"), file);
                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part body = MultipartBody.Part.createFormData("photo", "", requestFile);

                final List<String>[] persons = new List[1];
                final Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        persons[0] = getAllFileNames(path, currentImage2, facesArray);
                    }
                };
                thread.start();

                MainActivity.service.Recognize(body).enqueue(new Callback<List<Person>>() {
                    @Override
                    public void onResponse(Call<List<Person>> call, Response<List<Person>> response) {
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent();
                        intent.putExtra("Persons", (Serializable) response.body());
                        intent.putExtra("Photoes", (Serializable) persons[0]);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<List<Person>> call, Throwable t) {
                        finish();
                    }
                });
                Toast.makeText(getBaseContext(), "Photo is Sending", Toast.LENGTH_LONG).show();


            } else if (getIntent().getSerializableExtra("CameraState").equals(MainActivity.CameraState.AddPerson)) {
                if (facesArray.length != 1) return;
                final LinearLayout sn = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.short_name, null);

                File file = getOneFile(path, currentImage2, facesArray);
                Intent intent = new Intent();
                intent.putExtra("FaceData", path + "//" + file.getName());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            ;
        }
    };

    private File getOneFile(File path, Mat currentImage, Rect[] facesArray) {
        Mat finalImg = new Mat();
        List<Mat> res = new ArrayList<>();
        for (int i = 0; i < facesArray.length; i++) {
            Mat face = new Mat(currentImage, facesArray[i]);
            resize(face, face, new Size(100, 100));
            res.add(face);
        }
        hconcat(res, finalImg);
        File file = null;
        FileOutputStream stream2 = null;
        Bitmap bmp2 = null;
        try {
            bmp2 = Bitmap.createBitmap(finalImg.cols(), finalImg.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(finalImg, bmp2);
        } catch (CvException e) {
            Log.d(TAG, e.getMessage());
        }
        //currentImage.release();

        int j = path.list().length;
        file = new File(path, "Face " + j + ".jpg");

        try {
            stream2 = new FileOutputStream(file);

            bmp2.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
            stream2.flush();
            stream2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream2 != null) {
                    stream2.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private List<String> getAllFileNames(File path, Mat currentImage, Rect[] facesArray) {
        List<String> result = new ArrayList<String>() {
        };
        for (int i = 0; i < facesArray.length; i++) {
            File file;
            Mat face = new Mat(currentImage, facesArray[i]);
            resize(face, face, new Size(400, 400));
            Bitmap bmp = null;
            try {
                bmp = Bitmap.createBitmap(face.cols(), face.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(face, bmp);
            } catch (CvException e) {
                Log.d(TAG, e.getMessage());
            }
            face.release();

            int j = path.list().length;
            file = new File(path, "Face " + j + ".jpg");

            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                stream.flush();
                stream.close();
                result.add(path + "//" + file.getName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

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
        absoluteFaceSize = (int) (100);
    }


    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame aInputFrame) {
        currentImage = aInputFrame.rgba();
        currentImage2 = currentImage.clone();
        currentImageGray = aInputFrame.gray();
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
