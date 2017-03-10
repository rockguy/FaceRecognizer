package vinnik.facerecognizer.NavigationSection;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import Models.Person;
import Models.Photo;
import Support.MyAdapterWithEmptyNames;
import Support.MyListOfPersonAdapter;
import io.realm.Realm;
import vinnik.facerecognizer.R;

/**
 * Created by vinnik on 02.03.2017.
 */

public class ListOfPeopleFragment extends Fragment {

    public static final String TAG = "ListOfPeopleFragmentTag";

    public static enum InputState {CameraFragment, HomeFragment, PictureGalleryFragment}

    ;
    public InputState inputState;
    ListView listView;
    List<Person> persons;
    List<Photo> photos;
    Realm realm;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_of_people,null);
    }

    private void init() {
        realm = Realm.getInstance(MainActivity.realmConfig);
        listView = (ListView) getActivity().findViewById(R.id.list_of_best_people);

        persons = new ArrayList<>();
        photos = new ArrayList<>();

        switch (inputState) {
            case HomeFragment:
                if (realm.where(Person.class).isNotEmpty("Faces").findAll().size() > 0) {
                    persons = realm.where(Person.class)
                            .isNotEmpty("Faces")
                            .notEqualTo("FirstName", "defaultName")
                            .equalTo("Faces.IsTheBest", true)
                            .findAll();

                } else {
//                    super.onStart();
                    return;
                }

                listView.setAdapter(new MyListOfPersonAdapter(getContext(), persons));
                break;

            case CameraFragment://Todo: здесь же будет распознавание
                if (realm.where(Person.class).equalTo("FirstName", "defaultName").findAll().size() > 0) {
                    Person person = realm.where(Person.class).equalTo("FirstName", "defaultName").findAll().first();
                    for (Photo p : person.Faces
                            ) {
                        String s = MainActivity.personRecognizer.predict(p.getMat());
                        if (!s.isEmpty()) {
                            int q = s.indexOf(" ");
                            int q0 = s.indexOf("-");
                            if (realm.where(Person.class).equalTo("FirtstName", s.substring(0, q)).equalTo("LastName", s.substring(q, q0)).count() > 0) {
                                persons.add(realm.where(Person.class).equalTo("FirtstName", s.substring(0, q)).equalTo("LastName", q0).findFirst());
                            } else {
                                realm.beginTransaction();
                                Person man = realm.createObject(Person.class);
                                man.FirstName = s.substring(0, q);
                                man.LastName = s.substring(q, q0);
                                man.Faces.add(p);
                                realm.commitTransaction();
                            }
                        }
                    }

                    persons.add(person);
                }
                listView.setAdapter(new MyAdapterWithEmptyNames(getContext(), persons));
                break;
            case PictureGalleryFragment:
                break;
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.Navigate(MainActivity.ListOfFragments.FaceDetail, MainActivity.ListOfFragments.ListOfPeople, persons.get(position));
            }
        });
    }

    @Override
    public void onStart() {
        //init();
        super.onStart();
    }

    @Override
    public void onResume() {
        init();
        super.onResume();
    }
}
