package vinnik.facerecognizer.NavigationSection;

import android.app.Activity;
import android.content.Intent;
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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import Migration.MyMigration;
import Models.Person;
import Support.PersonRecognizer;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import vinnik.facerecognizer.R;

public class MainActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private int START_SUM_CODE = 1;

    private static FragmentManager manager;
    private static FragmentTransaction transaction;

    // Declaration  of fragments
    private MainAppBarFragment mainAppBar;
    private static HomePageFragment homePage = new HomePageFragment();
    private static ListOfPeopleFragment listOfPeople = new ListOfPeopleFragment();
    private static OpenImageFragment openImage = new OpenImageFragment();
    private static FaceDetailFragment faceDetail = new FaceDetailFragment();
    // Declaration of Realm
    private Realm realm;
    public static RealmConfiguration realmConfig = new RealmConfiguration.Builder().name("Migration.realm")
            .schemaVersion(2)
            .migration(new MyMigration())
            .build();


    public static PersonRecognizer personRecognizer = new PersonRecognizer(Environment.getExternalStorageDirectory() + "/frames/data");

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

        // Initialization of Realm
        realm = configRealm();

        // Imalementation of fragments
        manager = getSupportFragmentManager();
        mainAppBar = new MainAppBarFragment();

        //Open Start Page
        transaction = manager.beginTransaction();
        transaction.add(R.id.content_contaiter,homePage);
        transaction.commit();
    }

    public Realm configRealm() {
        realm = Realm.getInstance(realmConfig);

        try {
            realm.beginTransaction();
            realm.delete(Models.Photo.class);
            realm.delete(Models.Person.class);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
        return realm;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_SUM_CODE && resultCode == RESULT_OK) {
            listOfPeople.inputState = ListOfPeopleFragment.InputState.CameraFragment;
            transaction.addToBackStack(null);
            transaction = manager.beginTransaction();
            transaction.replace(R.id.content_contaiter, listOfPeople, listOfPeople.TAG);
            transaction.commitAllowingStateLoss();
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

        if (id == R.id.nav_camera) {
            Navigate(ListOfFragments.Camera, ListOfFragments.Home, this);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        switch (id) {
            case R.id.nav_home:
                MainActivity.Navigate(ListOfFragments.Home, ListOfFragments.Home, null);
                break;
            case R.id.nav_gallery:
                MainActivity.Navigate(ListOfFragments.ListOfPeople, ListOfFragments.Home, null);
                break;
            case R.id.nav_open_image:
                MainActivity.Navigate(ListOfFragments.OpenImage, ListOfFragments.Home, null);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static enum ListOfFragments {Camera, Home, ListOfPeople, OpenImage, FaceDetail}

    ;

    public static void Navigate(ListOfFragments finalFragment, ListOfFragments backFragment, Object data) {
        transaction = manager.beginTransaction();
        switch (finalFragment) {
            case Home:
                if (manager.getFragments().size() > 0) {
                    transaction.replace(R.id.content_contaiter, homePage);
                }
                break;
            case ListOfPeople:
                if (manager.getFragments().size() > 0) {
                    listOfPeople.inputState = ListOfPeopleFragment.InputState.HomeFragment;
                    if (manager.findFragmentByTag(ListOfPeopleFragment.TAG) != null) {
                        manager.getFragments().remove(manager.findFragmentByTag(ListOfPeopleFragment.TAG));
                    }
                    transaction.replace(R.id.content_contaiter, listOfPeople, ListOfPeopleFragment.TAG);
                }
                break;
            case OpenImage:
                if (manager.getFragments().size() > 0) {
                    transaction.replace(R.id.content_contaiter, openImage);
                }
                break;
            case FaceDetail:
                if (manager.getFragments().size() > 0) {
                    faceDetail.person = (Person) data;
                    transaction.replace(R.id.content_contaiter, faceDetail);
                }
                break;
            case Camera:
                Activity obj = (Activity) data;
                Intent intent = new Intent((obj).getBaseContext(), CameraLayout.class);
                (obj).startActivityForResult(intent, 1);
                break;
            default:
                return;

        }
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static void BackNavigate() {
        manager.popBackStack();
    }
}
