package vinnik.facerecognizer.NavigationSection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import Models.InteractiveService;
import Models.NamedPhoto;
import Models.Person;
import Models.PhotoDetail;
import Support.HelpClass;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vinnik.facerecognizer.R;

import static Support.HelpClass.DetectFaces;
import static Support.HelpClass.RecognizePersons;
import static Support.HelpClass.clearDir;
import static Support.HelpClass.currentStatus;
import static Support.HelpClass.setCascadeClassifier;
import static Support.HelpClass.writeResponseBodyToDisk;

public class MainActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomePageFragment.OnFragmentInteractionListener,
        ListOfPersonFragment.OnFragmentInteractionListener,
        FaceDetailFragment.OnFragmentInteractionListener {

    private int START_SUM_CODE = 1;
    private static final int ADD_PERSON = 0;
    private static final int RECOGNIZE_PERSON = 1;
    private static final int OPEN_IMAGE_FROM_GALLERY = 2;

    private static FragmentManager manager;
    private static FragmentTransaction transaction;
    static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10000, TimeUnit.SECONDS)
            .readTimeout(10000, TimeUnit.SECONDS).build();
    public static Retrofit retrofit;
    public static InteractiveService service;
    public static Context context;
    static SharedPreferences sPref;

    public static enum CameraState {AddPerson, RecognizePerson}

    public static final File path = new File(Environment.getExternalStorageDirectory() + "//Temp//");


    // Declaration  of fragments
    private MainAppBarFragment mainAppBar;
    Mat currentImage;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("", "Opencv successfully loaded");
        } else {
            Log.d("", "OpenCV not loaded");
        }
    }

    public static void RefreshUrl() {
        try {
            String url = sPref.getString("Url", "http://localhost:8181");
            retrofit = new Retrofit.Builder().baseUrl(url).
                    client(client).addConverterFactory(GsonConverterFactory.create()).build();
            service = retrofit.create(InteractiveService.class);

            Toast.makeText(context, "Корректный Url :-)", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Не корректный Url", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout. activity_main);
        sPref = getPreferences(MODE_PRIVATE);
        context = getApplicationContext();
        RefreshUrl();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Imalementation of fragments
        manager = getSupportFragmentManager();
        mainAppBar = new MainAppBarFragment();

        //Open Start Page
        Navigate(ListOfFragments.Home, null);
        if (!path.exists()) {
            path.mkdir();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == RECOGNIZE_PERSON && resultCode == RESULT_OK) {
            try {
                List<Person> persons = (List<Person>) data.getSerializableExtra("Persons");
                List<String> photos = (List<String>) data.getSerializableExtra("Photos");
                List<NamedPhoto> namedPhoto = new ArrayList<>();
                for (int i = 0; i < persons.size(); i++) {
                    NamedPhoto foo = new NamedPhoto();
                    foo.id = persons.get(i).Id;
                    foo.name = persons.get(i).toString();
                    foo.filePath = photos.get(i);
                    namedPhoto.add(foo);
                }
                HelpClass.personList = namedPhoto;
                currentStatus = HelpClass.CurrentStatus.NewPhotos;
                MainActivity.Navigate(ListOfFragments.ListOfPeople, null);

            } catch (Exception e) {

            }
        } else if (requestCode == RECOGNIZE_PERSON && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Никто не узнан", Toast.LENGTH_SHORT).show();
            return;
        } else if (requestCode == ADD_PERSON && resultCode == RESULT_OK) {
            currentStatus = HelpClass.CurrentStatus.NewPhotos;
            PhotoDetail photoDetail = new PhotoDetail();
            photoDetail.Img = data.getStringExtra("FaceData");
            Navigate(ListOfFragments.PhotoDetail, photoDetail);
        } else if (requestCode == OPEN_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bmp = BitmapFactory.decodeFile(picturePath);

            if (bmp == null) {
                Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show();
                return;
            }

            currentImage = new Mat();
            Utils.bitmapToMat(bmp, currentImage);
            setCascadeClassifier(getBaseContext());
            Rect[] facesArray = DetectFaces(currentImage);
            Object[] data0 = RecognizePersons(path, currentImage, facesArray);

            if (data0 == null) {
                Toast.makeText(getBaseContext(), "Лицо было не найдено", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Person> persons = (List<Person>) data0[1];
            List<String> photos = (List<String>) data0[0];
            List<NamedPhoto> namedPhoto = new ArrayList<>();
            for (int i = 0; i < persons.size(); i++) {
                NamedPhoto foo = new NamedPhoto();
                foo.id = persons.get(i).Id;
                foo.name = persons.get(i).toString();
                foo.filePath = photos.get(i);
                namedPhoto.add(foo);
            }
            HelpClass.personList = namedPhoto;
            currentStatus = HelpClass.CurrentStatus.NewPhotos;
            MainActivity.Navigate(ListOfFragments.ListOfPeople, null);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (manager.getBackStackEntryCount() > 0) {
            BackNavigate();
            return;
        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                currentStatus = HelpClass.CurrentStatus.Neutral;
                MainActivity.Navigate(ListOfFragments.Home, null);

                break;
            case R.id.nav_gallery:
                currentStatus = HelpClass.CurrentStatus.Neutral;
                MainActivity.Navigate(ListOfFragments.ListOfPeople, null);
                break;
            case R.id.nav_open_image:
                currentStatus = HelpClass.CurrentStatus.Neutral;
                MainActivity.Navigate(ListOfFragments.OpenImage, this);
                break;
            case R.id.nav_add_person_camera:
                currentStatus = HelpClass.CurrentStatus.Neutral;
                Navigate(ListOfFragments.Add_Person_Camera, this);
                break;
            case R.id.nav_recognize_person_camera:
                currentStatus = HelpClass.CurrentStatus.Neutral;
                Navigate(ListOfFragments.Recognize_Person_Camera, this);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    public static enum ListOfFragments {
        Add_Person_Camera, Recognize_Person_Camera, Home,
        ListOfPeople, OpenImage, FaceDetail, ListOfPersonPhotos, PhotoDetail, ListOfNewPeople
    }

    public static void Navigate(ListOfFragments finalFragment, Object data) {
        switch (finalFragment) {
            case Home:
                transaction = manager.beginTransaction();
                transaction.replace(R.id.content_contaiter, HomePageFragment.newInstance());
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case ListOfPeople:
                final List<String>[] files = new List[]{new ArrayList<>()};
                final List<Person> persons = new ArrayList<>();
                if (currentStatus == HelpClass.CurrentStatus.NewPhotos) {
                    transaction = manager.beginTransaction();
                    transaction.replace(R.id.content_contaiter, ListOfPersonFragment.newInstance());
                    transaction.commitNowAllowingStateLoss();
                } else {
                    currentStatus = HelpClass.CurrentStatus.GalleryOfPeople;
                    try {
                        service.allPersons().enqueue(new Callback<List<Person>>() {
                            @Override
                            public void onResponse(Call<List<Person>> call, Response<List<Person>> response) {
                                if (response != null) {
                                    persons.addAll(response.body());
                                } else {
                                    return;
                                }
                                service.collectivePhoto().enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                                        if (response.isSuccessful()) {
                                            try {
                                                new AsyncTask<Void, Void, Void>() {
                                                    @Override
                                                    protected Void doInBackground(Void... voids) {
                                                        files[0] = writeResponseBodyToDisk(context, response.body(), null, path);
                                                        if (files[0] == null) {
                                                            return null;
                                                        }
                                                        List<NamedPhoto> namedPhoto = new ArrayList<>();
                                                        for (int i = 0; i < persons.size(); i++) {
                                                            NamedPhoto foo = new NamedPhoto();
                                                            foo.id = persons.get(i).Id;
                                                            foo.name = persons.get(i).toString();
                                                            foo.filePath = files[0].get(i);
                                                            namedPhoto.add(foo);
                                                        }
                                                        HelpClass.personList = namedPhoto;
                                                        transaction = manager.beginTransaction();
                                                        transaction.addToBackStack(null);
                                                        transaction.replace(R.id.content_contaiter, ListOfPersonFragment.newInstance());
                                                        transaction.commit();
                                                        return null;
                                                    }
                                                }.execute();
                                            } catch (Exception e) {

                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<List<Person>> call, Throwable t) {
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(context, "На сервере нет изображений", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case OpenImage: {
                clearDir(context, path);
                Activity obj = (Activity) data;
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                (obj).startActivityForResult(i, OPEN_IMAGE_FROM_GALLERY);
                break;
            }
            case FaceDetail:
                final NamedPhoto p = (NamedPhoto) data;
                MainActivity.service.GetPersonDetail(p.id).enqueue(new Callback<Person>() {
                    @Override
                    public void onResponse(Call<Person> call, Response<Person> response) {
                        if (response.body() != null) {
                            Person person = response.body();
                            person.LocalImageFile = p.filePath;
                            transaction = manager.beginTransaction();
                            transaction.replace(R.id.content_contaiter, FaceDetailFragment.newInstance(person));
                            transaction.addToBackStack(null);
                            transaction.commit();
                        } else {
                            Person person = new Person();
                            person.LocalImageFile = p.filePath;
                            transaction = manager.beginTransaction();
                            transaction.replace(R.id.content_contaiter, FaceDetailFragment.newInstance(person));
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    }

                    @Override
                    public void onFailure(Call<Person> call, Throwable t) {
                    }
                });
                break;
            case Add_Person_Camera: {
                Activity obj = (Activity) data;
                Intent intent = new Intent((obj).getBaseContext(), CameraLayout.class);
                intent.putExtra("CameraState", CameraState.AddPerson);
                (obj).startActivityForResult(intent, ADD_PERSON);
                break;
            }
            case Recognize_Person_Camera: {
                Activity obj = (Activity) data;
                Intent intent = new Intent((obj).getBaseContext(), CameraLayout.class);
                intent.putExtra("CameraState", CameraState.RecognizePerson);
                (obj).startActivityForResult(intent, RECOGNIZE_PERSON);
                break;
            }
            case ListOfPersonPhotos:
                transaction = manager.beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.content_contaiter, ListOfPersonsFacesFragment.newInstance((int) data));
                transaction.commit();
                break;
            case PhotoDetail:
                transaction = manager.beginTransaction();
                //transaction.addToBackStack(null);
                transaction.replace(R.id.content_contaiter, PhotoDetailFragment.newInstance((PhotoDetail) data));
                transaction.commitNowAllowingStateLoss();
                break;
            case ListOfNewPeople:
                transaction = manager.beginTransaction();
                transaction.replace(R.id.content_contaiter, ListOfPersonFragment.newInstance());
                transaction.commitNowAllowingStateLoss();
                break;
            default:
                return;
        }
    }



    public static void BackNavigate() {
        manager.popBackStack();
    }
}
