package Models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import io.realm.RealmObject;

/**
 * Created by vinnik on 03.03.2017.
 */

public class Photo extends RealmObject {
    public int Id;
    public byte[] Face;
    public byte[] RecFace;
    public Person Owner;
    public boolean IsTheBest;

    public Bitmap getBitmap() {
        return BitmapFactory.decodeByteArray(Face, 0, Face.length);
    }

    public Bitmap getRecBitmap() {
        return BitmapFactory.decodeByteArray(RecFace, 0, Face.length);
    }

    public Mat getMat() {
        Mat m = new Mat();
        Utils.bitmapToMat(getRecBitmap(), m);
        return m;
    }
}
