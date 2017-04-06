package Support;

import java.util.List;

import Models.NamedPhoto;

/**
 * Created by vinnik on 06.04.2017.
 */

public class HelpClass {
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static List<NamedPhoto> personList;

}
