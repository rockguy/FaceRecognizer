package vinnik.facerecognizer.NavigationSection;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import Models.Person;
import Models.Photo;
import io.realm.Realm;
import vinnik.facerecognizer.R;

/**
 * Created by vinnik on 06.03.2017.
 */

public class FaceDetailFragment extends Fragment {
    public Person person;
    private EditText FirstName;
    private EditText MiddleName;
    private EditText LastName;
    private EditText City;
    private ImageView Face;
    private Button SaveButton;

    Realm realm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.face_detail_fragment, null);
    }


    @Override
    public void onStart() {
        realm = Realm.getInstance(MainActivity.realmConfig);

        SaveButton = (Button) getActivity().findViewById(R.id.face_detail_save_button);
        Face = (ImageView) getActivity().findViewById(R.id.face_detail_face);
        FirstName = (EditText) getActivity().findViewById(R.id.face_detail_first_name);
        MiddleName = (EditText) getActivity().findViewById(R.id.face_detail_middle_name);
        LastName = (EditText) getActivity().findViewById(R.id.face_detail_last_name);
        City = (EditText) getActivity().findViewById(R.id.face_detail_city);

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //MyRecognizer faceRecognizer = new MyRecognizer(getId());
                //faceRecognizer.load(Environment.getExternalStorageDirectory() + "/frames/data");


                realm.beginTransaction();
                Person p = realm.where(Person.class).equalTo("Id", person.Id).findFirst();
                p.FirstName = FirstName.getText().toString();
                p.MiddleName = MiddleName.getText().toString();
                p.LastName = LastName.getText().toString();
                p.City = City.getText().toString();
                realm.commitTransaction();
                realm.beginTransaction();
                Photo face = realm.where(Photo.class).equalTo("Id", person.Faces.first().Id).findFirst();
                face.Face = person.Faces.first().Face;
                face.RecFace = person.Faces.first().RecFace;
                face.Owner = p;
                p.Faces.add(face);
                realm.commitTransaction();
                MainActivity.BackNavigate();


                MainActivity.personRecognizer.add(face.getMat(), p.toString());
                MainActivity.personRecognizer.train();
//                Mat m = new Mat();
//                Utils.bitmapToMat(face.getRecBitmap(), m);
//                List<Mat> ls = new ArrayList<Mat>();
//                ls.add(m);

                //faceRecognizer.train(ls,person.toString());
                //faceRecognizer.save(Environment.getExternalStorageDirectory() + "/frames/data");
            }
        });

        Face.setImageBitmap(person.Faces.first().getBitmap());
        FirstName.setText(person.FirstName);
        MiddleName.setText(person.MiddleName);
        LastName.setText(person.LastName);
        City.setText(person.City);

        super.onStart();
    }

}
