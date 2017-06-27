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
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import Models.PhotoDetail;
import Support.HelpClass;
import Support.MyListOfPersonAdapter;
import vinnik.facerecognizer.R;

import static Support.HelpClass.currentStatus;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListOfPersonFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListOfPersonFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    List<PhotoDetail> namedPhotos;

    public ListOfPersonFragment() {
        // Required empty public constructor
    }

    public static ListOfPersonFragment newInstance() {
        ListOfPersonFragment fragment = new ListOfPersonFragment();


        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_of_person, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (HelpClass.UpdateNeeded && HelpClass.currentStatus != HelpClass.CurrentStatus.NewPhotos) {
            HelpClass.UpdateNeeded = false;
            MainActivity.Navigate(MainActivity.ListOfFragments.ListOfPeople, null);
        }
        refreshData();
    }

    private void refreshData() {
        namedPhotos = new ArrayList<>();

        for (int i = 0; i < HelpClass.personList.size(); i++) {
            PhotoDetail foo = new PhotoDetail();
            foo.LongName = HelpClass.personList.get(i).LongName;

            if (!HelpClass.isNullOrEmpty(HelpClass.personList.get(i).filePath)) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                foo.bitmap = BitmapFactory.decodeFile(HelpClass.personList.get(i).filePath, options);
            } else {
                foo.bitmap = HelpClass.personList.get(i).bitmap;
            }
            namedPhotos.add(foo);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ListView listView = (ListView) getActivity().findViewById(R.id.list_of_person);

        refreshData();
        ListAdapter listAdapter = new MyListOfPersonAdapter(getContext(), namedPhotos);
        listView.setAdapter(listAdapter);
        if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MainActivity.Navigate(MainActivity.ListOfFragments.PhotoDetail, HelpClass.personList.get(position));
                }
            });
            //currentStatus = HelpClass.CurrentStatus.Neutral;
        } else {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MainActivity.Navigate(MainActivity.ListOfFragments.FaceDetail, HelpClass.personList.get(position));
                }
            });
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

