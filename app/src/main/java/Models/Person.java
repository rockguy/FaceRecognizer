package Models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by vinnik on 03.03.2017.
 */

public class Person extends RealmObject {
    public int Id;
    @Required
    public String FirstName;
    public String MiddleName;
    @Required
    public String LastName;
    public String City;
    public RealmList<Photo> Faces;

    @Override
    public String toString() {
        if (FirstName.equals("defaultPerson")) return "";
        else return FirstName + " " + LastName;
    }

    public byte[] getBestPhoto() {
        byte[] b = Faces.where().equalTo("IsTheBest", true).findAll().first().Face;
        return b;
    }
}
