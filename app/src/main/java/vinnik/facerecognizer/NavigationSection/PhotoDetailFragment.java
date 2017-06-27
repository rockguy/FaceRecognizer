package vinnik.facerecognizer.NavigationSection;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import Models.Person;
import Models.PhotoDetail;
import Support.HelpClass;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vinnik.facerecognizer.R;

import static Support.HelpClass.UpdateNeeded;
import static Support.HelpClass.currentStatus;
import static Support.HelpClass.personList;
import static vinnik.facerecognizer.NavigationSection.MainActivity.BackNavigate;
import static vinnik.facerecognizer.NavigationSection.MainActivity.Navigate;
import static vinnik.facerecognizer.NavigationSection.MainActivity.context;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link PhotoDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoDetailFragment extends Fragment {

    PhotoDetail photoDetail;
    String ShortName;
    TextView textView;

    public PhotoDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PhotoDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhotoDetailFragment newInstance(PhotoDetail param1) {
        PhotoDetailFragment fragment = new PhotoDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("PhotoDetail", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photoDetail = (PhotoDetail) getArguments().getSerializable("PhotoDetail");
            ShortName = photoDetail.ShortName;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_detail, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.image_item);
        imageView.setImageBitmap(BitmapFactory.decodeFile(photoDetail.filePath));

        textView = (TextView) getActivity().findViewById(R.id.face_detail_short_name);
        textView.setText(ShortName);
        Button makeMainButton = (Button) getActivity().findViewById(R.id.make_main_button);
        makeMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.service.MakeMainPhoto(photoDetail.Id).enqueue(new retrofit2.Callback<String>() {
                    @Override
                    public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                        Toast.makeText(getContext(), "Теперь фото главное ;-)", Toast.LENGTH_SHORT).show();
                        if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                            Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                            return;
                        }
                        HelpClass.UpdateNeeded = true;
                        BackNavigate();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<String> call, Throwable t) {
                        if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                            Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                            return;
                        }
                        BackNavigate();
                    }
                });
            }
        });

        Button deletePhotoButton = (Button) getActivity().findViewById(R.id.delete_photo_button);
        deletePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.service.DeletePhoto(photoDetail.Id).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Toast.makeText(getContext(), "Теперь фото больше нет ;-)", Toast.LENGTH_SHORT).show();
                        if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                            Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                            return;
                        }
                        HelpClass.UpdateNeeded = true;
                        BackNavigate();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                            Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                            return;
                        }
                        BackNavigate();
                    }
                });
            }
        });

        Button SaveButton = (Button) getActivity().findViewById(R.id.face_detail_save_button);
        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShortName.equals(textView.getText().toString())) {
                    if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                        Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                        return;
                    }
                    BackNavigate();
                } else {
                    if (photoDetail.Id != 0) {
                        MainActivity.service.ChangeOwner(photoDetail.Id, textView.getText().toString()).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                                    int i = HelpClass.personList.indexOf(photoDetail);
                                    PhotoDetail namedPhoto = personList.get(i - 1);
                                    namedPhoto.ShortName = textView.getText().toString();
                                    Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                                    return;
                                }
                                HelpClass.UpdateNeeded = true;
                                BackNavigate();
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                            }
                        });
                    } else {
                        File file = new File(photoDetail.filePath);
                        RequestBody requestFile = RequestBody.create(MediaType.parse("data:image/jpg;base64"), file);
                        // MultipartBody.Part is used to send also the actual file name
                        MultipartBody.Part body = MultipartBody.Part.createFormData("photo", "", requestFile);

                        String descriptionString = textView.getText().toString();
                        RequestBody description =
                                RequestBody.create(
                                        okhttp3.MultipartBody.FORM, descriptionString);
                        MainActivity.service.addPhoto(description, body).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                Toast.makeText(getContext(), response.body(), Toast.LENGTH_SHORT).show();
                                if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                                    final int i;
                                    int i2 = 0;
                                    for (PhotoDetail n : HelpClass.personList
                                            ) {
                                        if (n.filePath.equals(photoDetail.filePath)) {
                                            break;
                                        }
                                        i2++;
                                    }
                                    i = i2;
                                    final int id;
                                    try {
                                        id = Integer.parseInt(response.body());
                                    } catch (Exception e) {
                                        Toast.makeText(context, response.body(), Toast.LENGTH_SHORT);
                                        return;
                                    }

                                    MainActivity.service.GetPersonDetail(textView.getText().toString()).enqueue(new Callback<Person>() {
                                        @Override
                                        public void onResponse(Call<Person> call, Response<Person> response) {
                                            PhotoDetail photoDetail = HelpClass.personList.get(i);
                                            photoDetail.OwnerId = response.body().Id;
                                            photoDetail.LongName = response.body().toString();
                                            photoDetail.ShortName = response.body().ShortName;
                                            photoDetail.Id = id;
                                            Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                                            return;
                                        }

                                        @Override
                                        public void onFailure(Call<Person> call, Throwable t) {

                                        }
                                    });

                                    return;
                                }
                                UpdateNeeded = true;
                                MainActivity.Navigate(MainActivity.ListOfFragments.ListOfPeople, null);
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                                    Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                                    return;
                                }
                            }
                        });
                    }

                }
            }
        });

        if (currentStatus == HelpClass.CurrentStatus.NewPhotos && photoDetail.Id == 0) {
            makeMainButton.setVisibility(View.GONE);
            deletePhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                        HelpClass.personList.remove(photoDetail);
                        Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                        return;
                    }
                }
            });
        }
        Button toServer = (Button) getActivity().findViewById(R.id.add_photo_to_server_button);
        toServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(photoDetail.filePath);
                RequestBody requestFile = RequestBody.create(MediaType.parse("data:image/jpg;base64"), file);
                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part body = MultipartBody.Part.createFormData("photo", "", requestFile);

                String descriptionString = textView.getText().toString();
                RequestBody description =
                        RequestBody.create(
                                okhttp3.MultipartBody.FORM, descriptionString);
                MainActivity.service.addPhoto(description, body).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Toast.makeText(getContext(), response.body(), Toast.LENGTH_SHORT).show();
                        if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                            Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                            return;
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                            Navigate(MainActivity.ListOfFragments.ListOfNewPeople, null);
                            return;
                        }
                    }
                });
            }
        });
    }


}
