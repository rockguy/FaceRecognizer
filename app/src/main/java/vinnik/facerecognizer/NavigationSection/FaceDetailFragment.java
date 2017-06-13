package vinnik.facerecognizer.NavigationSection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import Models.NamedPhoto;
import Models.Person;
import Support.HelpClass;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vinnik.facerecognizer.R;

import static Support.HelpClass.UpdateNeeded;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FaceDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FaceDetailFragment extends Fragment {
    public Person person;
    private EditText FirstName;
    private EditText MiddleName;
    private EditText LastName;
    private EditText ShortName;
    private EditText City;
    private ImageView Face;
    private Button SaveButton;
    private Button BackButton;
    private Button DeleteButton;
    private Button AllPhotoButton;
    private static final String ARG_PARAM1 = "param1";

    private OnFragmentInteractionListener mListener;

    public FaceDetailFragment() {
        // Required empty public constructor
    }

    public static FaceDetailFragment newInstance(Person person) {
        FaceDetailFragment fragment = new FaceDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, person);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            person = (Person) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_face_detail, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        SaveButton = (Button) getActivity().findViewById(R.id.face_detail_save_button);
        BackButton = (Button) getActivity().findViewById(R.id.face_detail_back_button);
        DeleteButton = (Button) getActivity().findViewById(R.id.face_detail_delete_button);
        AllPhotoButton = (Button) getActivity().findViewById(R.id.face_detail_show_all_photos);
        Face = (ImageView) getActivity().findViewById(R.id.face_detail_face);
        FirstName = (EditText) getActivity().findViewById(R.id.face_detail_first_name);
        MiddleName = (EditText) getActivity().findViewById(R.id.face_detail_middle_name);
        LastName = (EditText) getActivity().findViewById(R.id.face_detail_last_name);
        ShortName = (EditText) getActivity().findViewById(R.id.face_detail_short_name);
        City = (EditText) getActivity().findViewById(R.id.face_detail_city);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Face.setImageBitmap(BitmapFactory.decodeFile(person.LocalImageFile, options));

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                person.FirstName = FirstName.getText().toString();
                person.MiddleName = MiddleName.getText().toString();
                person.LastName = LastName.getText().toString();
                person.ShortName = ShortName.getText().toString();
                person.City = City.getText().toString();

                if (person.Id != 0) {
                    MainActivity.service.UpdatePerson(person).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                            NamedPhoto foo = new NamedPhoto();
                            foo.id = person.Id;
                            int i = HelpClass.personList.indexOf(foo);
                            foo = HelpClass.personList.get(i);
                            foo.name = person.toString();
                            HelpClass.personList.set(i, foo);
                            MainActivity.BackNavigate();
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(getContext(), "ВСЕ ПРОПАЛО!", Toast.LENGTH_SHORT).show();
                            MainActivity.BackNavigate();
                        }
                    });
                }
//                else{
//                    File file = new File(person.LocalImageFile);
//                    RequestBody requestFile = RequestBody.create(MediaType.parse("data:image/jpg;base64"), file);
//                    // MultipartBody.Part is used to send also the actual file name
//                    MultipartBody.Part body = MultipartBody.Part.createFormData("photo", "", requestFile);
//
//                    String descriptionString = person.toString();
//                    RequestBody description =
//                            RequestBody.create(
//                                    okhttp3.MultipartBody.FORM, descriptionString);
//                    MainActivity.service.addPhoto(description, body).enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(Call<String> call, Response<String> response) {
//                            Toast.makeText(getContext(), response.body(), Toast.LENGTH_SHORT).show();
//                            UpdateNeeded = true;
//                            MainActivity.Navigate(MainActivity.ListOfFragments.ListOfPeople, null);
//                        }
//
//                        @Override
//                        public void onFailure(Call<String> call, Throwable t) {
//
//                        }
//                    });
//                }
            }
        });

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.BackNavigate();
            }
        });

        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.service.DeletePerson(person).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        File f = new File(person.LocalImageFile);
                        f.delete();
                        NamedPhoto foo = new NamedPhoto();
                        foo.id = person.Id;
                        HelpClass.personList.remove(foo);
                        UpdateNeeded = true;
                        MainActivity.BackNavigate();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
            }
        });

        AllPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.Navigate(MainActivity.ListOfFragments.ListOfPersonPhotos, person.Id);
            }
        });

        FirstName.setText(person.FirstName);
        MiddleName.setText(person.MiddleName);
        LastName.setText(person.LastName);
        ShortName.setText(person.ShortName);
        City.setText(person.City);

        super.onStart();
    }
}
