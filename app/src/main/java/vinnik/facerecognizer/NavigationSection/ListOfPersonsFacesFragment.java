package vinnik.facerecognizer.NavigationSection;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.IOException;
import java.util.List;

import Models.PhotoDetail;
import Support.HelpClass;
import Support.PhotoAdapter;
import vinnik.facerecognizer.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ListOfPersonsFacesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListOfPersonsFacesFragment extends Fragment {

    GridView gv;
    int id;
    String ShortName;
    List<PhotoDetail> photos;

    public ListOfPersonsFacesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ListOfPersonsFacesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListOfPersonsFacesFragment newInstance(int id/*, String shortName*/) {
        ListOfPersonsFacesFragment fragment = new ListOfPersonsFacesFragment();
        Bundle args = new Bundle();
        args.putInt("Id", id);
        /*args.putString("ShortName", shortName);*/
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            id = getArguments().getInt("Id");
            /*ShortName = getArguments().getString("ShortName");*/
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_of_persons_faces, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        gv = (GridView) getActivity().findViewById(R.id.grid_for_photo);
        refreshData();
    }

    private void refreshData() {
        final Object[] f = new Object[2];
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    f[0] = HelpClass.writeResponseBodyToDisk(getContext(), MainActivity.service.GetPersonPhoto(id).execute().body(), null, MainActivity.path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    f[1] = MainActivity.service.GetPersonPhotoInfo(id).execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> paths = (List<String>) f[0]; //getAllFileNames(path, currentImage, facesArray);
        photos = (List<PhotoDetail>) f[1];
        for (int i = 0; i < photos.size(); i++) {
            photos.get(i).filePath = paths.get(i);
            /*photos.get(i).ShortName = ShortName;*/
        }
        gv.setAdapter(new PhotoAdapter(getContext(), photos));

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.Navigate(MainActivity.ListOfFragments.PhotoDetail, photos.get(i));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (HelpClass.UpdateNeeded) {
            refreshData();
        }
        HelpClass.UpdateNeeded = true;
    }

}
