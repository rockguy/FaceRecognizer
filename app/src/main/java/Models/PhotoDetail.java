package Models;

import java.io.Serializable;

/**
 * Created by vinnik on 18.04.2017.
 */

public class PhotoDetail implements Serializable {
    public int Id;
    public int OwnerId;
    public String ShortName;
    public boolean IsTheBest;
    public String Img; 
}
