package Models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import io.realm.RealmObject;

/**
 * Created by vinnik on 03.03.2017.
 */

public class Photo extends RealmObject {
    public int Id;
    public byte[] Face;
    public Person Owner;

    public Bitmap getBitmap() {
        return BitmapFactory.decodeByteArray(Face, 0, Face.length);
    }
}
