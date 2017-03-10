package Migration;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by vinnik on 06.03.2017.
 */

public class MyMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            RealmObjectSchema photoSchema = schema.get("Photo");

            photoSchema
                    .addField("IsTheBest", Boolean.class);

            oldVersion++;
        }

        if (oldVersion == 1) {
            RealmObjectSchema photoSchema = schema.get("Photo");

            photoSchema
                    .addField("RecFace", Byte[].class);

            oldVersion++;
        }
    }
}
