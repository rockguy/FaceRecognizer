package vinnik.facerecognizer.NavigationSection;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import Models.InteractiveService;
import Models.NamedPhoto;
import Models.Person;
import Support.HelpClass;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vinnik.facerecognizer.R;

public class MainActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomePageFragment.OnFragmentInteractionListener,
        ListOfPersonFragment.OnFragmentInteractionListener,
        FaceDetailFragment.OnFragmentInteractionListener {


    private int START_SUM_CODE = 1;

    private static FragmentManager manager;
    private static FragmentTransaction transaction;
    static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10000, TimeUnit.SECONDS)
            .readTimeout(10000, TimeUnit.SECONDS).build();
    public static Retrofit retrofit = new Retrofit.Builder().baseUrl("http://f86d202a.ngrok.io/").
            client(client).addConverterFactory(GsonConverterFactory.create()).build();
    public static InteractiveService service = retrofit.create(InteractiveService.class);

    public static enum CameraState {AddPerson, RecognizePerson}

    ;

    // Declaration  of fragments
    private MainAppBarFragment mainAppBar;

    private static OpenImageFragment openImage = new OpenImageFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout. activity_main);

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
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == START_SUM_CODE && resultCode == RESULT_OK) {
            try {
                List<Person> persons = (List<Person>) data.getSerializableExtra("Persons");
                List<String> photoes = (List<String>) data.getSerializableExtra("Photoes");
                List<NamedPhoto> namedPhoto = new ArrayList<>();
                for (int i = 0; i < persons.size(); i++) {
                    NamedPhoto foo = new NamedPhoto();
                    foo.id = persons.get(i).Id;
                    foo.name = persons.get(i).toString();
                    foo.filePath = photoes.get(i);
                    namedPhoto.add(foo);
                }
                HelpClass.personList = namedPhoto;
                MainActivity.Navigate(ListOfFragments.ListOfPeople, null);

            } catch (Exception e) {
                if ((List<Person>) data.getSerializableExtra("Persons") == null) {
                    Toast.makeText(this, "Никто не узнан", Toast.LENGTH_SHORT);
                    return;
                }
            }
        } else if (requestCode == 0 && resultCode == RESULT_OK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final LinearLayout view = (LinearLayout) getLayoutInflater()
                    .inflate(R.layout.short_name, null);
            builder.setView(view);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText ShortName = (EditText) view.findViewById(R.id.short_name);
                    File file = new File(data.getStringExtra("FaceData"));
                    RequestBody requestFile = RequestBody.create(MediaType.parse("data:image/jpg;base64"), file);
                    // MultipartBody.Part is used to send also the actual file name
                    MultipartBody.Part body = MultipartBody.Part.createFormData("photo", "", requestFile);

                    String descriptionString = ShortName.getText().toString();
                    RequestBody description =
                            RequestBody.create(
                                    okhttp3.MultipartBody.FORM, descriptionString);
                    MainActivity.service.addPhoto(description, body).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Toast.makeText(getBaseContext(), "Добавлено", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(getBaseContext(), "Photo failed", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create();
            builder.show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
                MainActivity.Navigate(ListOfFragments.Home, null);
                break;
            case R.id.nav_gallery:

                final List<String>[] files = new List[]{new ArrayList<>()};
                final List<Person> persons = new ArrayList<>();
                service.allPersons().enqueue(new Callback<List<Person>>() {
                    @Override
                    public void onResponse(Call<List<Person>> call, Response<List<Person>> response) {
                        persons.addAll(response.body());
                        service.collectivePhoto().enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... voids) {
                                            files[0] = writeResponseBodyToDisk(this, response.body(), null);

                                            List<NamedPhoto> namedPhoto = new ArrayList<>();
                                            for (int i = 0; i < persons.size(); i++) {
                                                NamedPhoto foo = new NamedPhoto();
                                                foo.id = persons.get(i).Id;
                                                foo.name = persons.get(i).toString();
                                                foo.filePath = files[0].get(i);
                                                namedPhoto.add(foo);
                                            }
                                            HelpClass.personList = namedPhoto;
                                            MainActivity.Navigate(ListOfFragments.ListOfPeople, null);
                                            return null;
                                        }
                                    }.execute();
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


                break;
            case R.id.nav_open_image:
                MainActivity.Navigate(ListOfFragments.OpenImage, null);
                break;
            case R.id.nav_add_person_camera:
                Navigate(ListOfFragments.Add_Person_Camera, this);
                break;
            case R.id.nav_recognize_person_camera:
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

    public static enum ListOfFragments {Add_Person_Camera, Recognize_Person_Camera, Home, ListOfPeople, OpenImage, FaceDetail}

    public static void Navigate(ListOfFragments finalFragment, Object data) {

        switch (finalFragment) {
            case Home:
                transaction = manager.beginTransaction();
                transaction.replace(R.id.content_contaiter, HomePageFragment.newInstance());
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case ListOfPeople:
                transaction = manager.beginTransaction();
                transaction.replace(R.id.content_contaiter, ListOfPersonFragment.newInstance());
                transaction.commitAllowingStateLoss();
                break;
            case OpenImage:
                transaction = manager.beginTransaction();
                transaction.replace(R.id.content_contaiter, openImage);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case FaceDetail:
                final NamedPhoto p = (NamedPhoto) data;
                MainActivity.service.GetPersonDetail(p.id).enqueue(new Callback<Person>() {
                    @Override
                    public void onResponse(Call<Person> call, Response<Person> response) {
                        Person person = response.body();
                        person.LocalImageFile = p.filePath;
                        transaction = manager.beginTransaction();
                        transaction.replace(R.id.content_contaiter, FaceDetailFragment.newInstance(person));
                        transaction.addToBackStack(null);
                        transaction.commit();
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
                (obj).startActivityForResult(intent, 0);
                break;
            }
            case Recognize_Person_Camera: {
                Activity obj = (Activity) data;
                Intent intent = new Intent((obj).getBaseContext(), CameraLayout.class);
                intent.putExtra("CameraState", CameraState.RecognizePerson);
                (obj).startActivityForResult(intent, 1);
                break;
            }
            default:
                return;
        }
    }

    private static List<String> writeResponseBodyToDisk(AsyncTask<Void, Void, Void> asyncTask, ResponseBody body, Object o) {
        try {
            final File path = new File(Environment.getExternalStorageDirectory() + "/Temp/");
            for (String s : path.list()
                    ) {
                File f = new File(path + "//" + s);
                f.delete();
            }
            InputStream inputStream = null;
            OutputStream outputStream = null;
            List<String> Files = new ArrayList<>();
            try {
                inputStream = body.byteStream();

                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
                for (int i = 0; i < bmp.getWidth() / 100; i++) {
                    Bitmap b = Bitmap.createBitmap(bmp, 100 * i, 0, 100, 100);
                    FileOutputStream fos = new FileOutputStream(path + "Face " + i + ".jpg");
                    Files.add(path + "Face " + i + ".jpg");
                    b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                }
                return Files;
            } catch (IOException e) {
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static void BackNavigate() {
        manager.popBackStack();
    }
}
