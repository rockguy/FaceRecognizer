package vinnik.facerecognizer.NavigationSection;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import Models.Person;
import io.realm.Realm;
import io.realm.RealmQuery;
import vinnik.facerecognizer.R;

/**
 * Created by vinnik on 02.03.2017.
 */

public class HomePageFragment extends Fragment {

    public static final String TAG = "HomePageFragmentTag";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_page_fragment,null);
    }

    @Override
    public void onStart() {
        TextView t = (TextView) getActivity().findViewById(R.id.HelloWorldText);
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Person> query = realm.where(Person.class);

        Person person = realm.where(Person.class).isNotEmpty("Faces").findAll().first();
        String s = person.toString();
        ImageView image = (ImageView) getActivity().findViewById(R.id.image);
        image.setImageBitmap(person.Faces.first().getBitmap());

        t.setText(s);
        super.onStart();
    }
}
