package vinnik.facerecognizer.NavigationSection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import java.io.ByteArrayOutputStream;

import Models.Person;
import Models.Photo;
import io.realm.Realm;
import vinnik.facerecognizer.R;

public class MainActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MainAppBarFragment mainAppBar;
    private HomePageFragment homePage;
    private ListOfPeopleFragment listOfPeople;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private CameraFragment camera;
    private OpenImageFragment openImage;

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

        manager = getSupportFragmentManager();


        mainAppBar = new MainAppBarFragment();
        homePage = new HomePageFragment();
        listOfPeople = new ListOfPeopleFragment();
        camera = new CameraFragment();
        openImage = new OpenImageFragment();

        transaction = manager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.add(R.id.content_contaiter,homePage);
        transaction.commit();

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            Person person = realm.createObject(Person.class);
            person.FirstName = "John";
            person.LastName = "Doe2";
            person.City = "Some City";
            Photo photo = realm.createObject(Photo.class);
            photo.Owner = person;
            //
            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_menu_camera);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.PNG, 100, stream);
            photo.Face = stream.toByteArray();
            person.Faces.add(photo);
            //
            realm.commitTransaction();
        } finally {
            realm.close();
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
        transaction = manager.beginTransaction();

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            if(manager.getFragments().size()>0) {
                transaction.replace(R.id.content_contaiter, homePage);
            }
        } else if (id == R.id.nav_camera) {
//            if(manager.getFragments().size()>0) {
//                transaction.replace(R.id.content_contaiter, camera);
//            }
            Intent intent = new Intent(MainActivity.this, CameraLayout.class);
            startActivityForResult(intent, 101);
        } else if (id == R.id.nav_gallery) {
            if(manager.getFragments().size()>0) {
                transaction.replace(R.id.content_contaiter, listOfPeople);
            }
        } else if (id == R.id.nav_open_image) {
            if(manager.getFragments().size()>0) {
                transaction.replace(R.id.content_contaiter, openImage);
            }
        }

        transaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
