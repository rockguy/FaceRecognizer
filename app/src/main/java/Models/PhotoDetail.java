package Models;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by vinnik on 18.04.2017.
 */

public class PhotoDetail implements Serializable {
    public int Id;
    public int OwnerId;
    public String ShortName;
    public String LongName;
    public boolean IsTheBest;
    public Bitmap bitmap;
    public String filePath;

    @Override
    public boolean equals(Object obj) {
        return Id == ((PhotoDetail) obj).Id;
    }
}
