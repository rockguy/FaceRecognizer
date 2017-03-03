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
        return FirstName + " " + LastName;
    }
}
