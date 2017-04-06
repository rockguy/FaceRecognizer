package Models;

import java.io.Serializable;

import Support.HelpClass;


/**
 * Created by vinnik on 03.03.2017.
 */

public class Person implements Serializable {
    public int Id;
    public String FirstName;
    public String MiddleName;
    public String LastName;
    public String City;
    public String ShortName;
    public String LocalImageFile;

    @Override
    public String toString() {
        if (HelpClass.isNullOrEmpty(FirstName) || HelpClass.isNullOrEmpty(LastName))
            return ShortName;
        else return FirstName + " " + LastName;
    }
}
