package Models;

import android.graphics.Bitmap;

/**
 * Created by vinnik on 30.03.2017.
 */

public class NamedPhoto {
    public int id;
    public String name;
    public Bitmap bitmap;
    public String filePath;

    @Override
    public boolean equals(Object obj) {
        return id == ((NamedPhoto)obj).id;
    }
}
