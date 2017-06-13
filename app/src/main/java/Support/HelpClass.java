package Support;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import Models.NamedPhoto;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import vinnik.facerecognizer.NavigationSection.MainActivity;
import vinnik.facerecognizer.R;

import static org.opencv.core.Core.hconcat;
import static org.opencv.imgproc.Imgproc.resize;

/**
 * Created by vinnik on 06.04.2017.
 */

public class HelpClass {
    private static int absoluteFaceSize = 50;

    public static boolean UpdateNeeded = false;

    private static CascadeClassifier cascadeClassifier;

    public static void setCascadeClassifier(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
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
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static String AddPerson(File path, Mat currentImage, Rect[] facesArray) {
        if (facesArray.length != 1) return "";
        File file = getOneFile(path, currentImage, facesArray);
        return file.getName();
    }

    public static Object[] RecognizePersons(final File path, final Mat currentImage, final Rect[] facesArray) {

        final Object[] result = new Object[2];
        File file = getOneFile(path, currentImage, facesArray);
        if (file == null) return null;
        RequestBody requestFile = RequestBody.create(MediaType.parse("data:image/jpg;base64"), file);
        // MultipartBody.Part is used to send also the actual file name
        final MultipartBody.Part body = MultipartBody.Part.createFormData("photo", "", requestFile);


        final List<String>[] persons = new List[1];
        final Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                result[0] = getAllFileNames(path, currentImage, facesArray);
            }
        };
        thread.start();
        //result.add(getAllFileNames(path, currentImage, facesArray));
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    result[1] = MainActivity.service.Recognize(body).execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread1.start();

        try {
            thread.join();
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Rect[] DetectFaces(Mat currentImage) {
        MatOfRect faces = new MatOfRect();
        // Use the classifier to detect faces
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(currentImage, faces, 1.1, 1, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size(currentImage.width(), currentImage.height()));
        }
        return faces.toArray();
    }

    public static File getOneFile(File path, Mat currentImage, Rect[] facesArray) {
        if (facesArray.length == 0) return null;
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

        }

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

    public static List<String> getAllFileNames(File path, Mat currentImage, Rect[] facesArray) {
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

    public static List<String> writeResponseBodyToDisk(Context context, ResponseBody body, Object o, File path) {
        clearDir(context, path);
        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            List<String> Files = new ArrayList<>();
            try {
                inputStream = body.byteStream();

                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
                for (int i = 0; i < bmp.getWidth() / 100; i++) {
                    Bitmap b = Bitmap.createBitmap(bmp, 100 * i, 0, 100, 100);
                    FileOutputStream fos = new FileOutputStream(path + "//Face " + i + ".jpg");
                    Files.add(path + "//Face " + i + ".jpg");
                    b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                }
                return Files;
            } catch (IOException e) {
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            return null;
        }

    }

    public static void clearDir(Context context, File path) {
        try {
            for (String s : path.list()
                    ) {
                File f = new File(path + "//" + s);
                f.delete();
            }
        } catch (Exception e) {
            Toast.makeText(context, "НЕ удалось очистить директорию", Toast.LENGTH_SHORT).show();
        }
    }


    public static List<NamedPhoto> personList;

    public enum CurrentStatus {NewPhotos, GalleryOfPeople, GalleryOfPerson, Neutral}

    public static CurrentStatus currentStatus;



}
