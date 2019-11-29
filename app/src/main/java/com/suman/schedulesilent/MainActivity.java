package com.suman.schedulesilent;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    AudioManager audioManager;
    int LOCATION_REFRESH_TIME = 1000;
    int LOCATION_REFRESH_DISTANCE = 5;
    int ringMode;
    SQLiteDatabase profileDatabase;
    ListView mainListView;
    TextView mainTextView;
    ArrayList<String> arrayList = new ArrayList<>();
    int numOfRows;
    ArrayAdapter arrayAdapter;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.GET_PERMISSIONS){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainListView = (ListView)findViewById(R.id.mainListView);
        mainTextView = (TextView)findViewById(R.id.mainTextView);
        /*ListView listView = (ListView)findViewById(R.id.mainListView);
        final ArrayList<String> friends = new ArrayList<>();

        friends.add("Nazmul");
        friends.add("Sajib");
        friends.add("Sunny");
        friends.add("Abir");

        ArrayAdapter arrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, friends);
        listView.setAdapter(arrayAdapter);*/

        profileDatabase = this.openOrCreateDatabase("Profiles", MODE_PRIVATE, null);
        profileDatabase.execSQL("CREATE TABLE IF NOT EXISTS profiles (" +
                "title VARCHAR, " +
                "startTime INT(4), " +
                "endTime INT(4), " +
                "weekDays INT(3), " +
                "country VARCHAR, " +
                "postCode VARCHAR)");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                Intent intent = new Intent(getApplicationContext(), CreateOrEditActivity.class);
                intent.putExtra("new", true);

                startActivity(intent);
            }
        });

        populateList();

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("ListClick", String.valueOf(position));
                Intent intent = new Intent(getApplicationContext(), CreateOrEditActivity.class);
                intent.putExtra("new", false);
                intent.putExtra("position", position);

                Log.i("Check", "Clicked");

                startActivity(intent);
            }
        });

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Log.i ("DeviceLocation" , location.toString());
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    Log.i("DeviceLocation", listAddresses.get(0).toString());

                    String postalCode ="";
                    String countryName = "";
                    if (listAddresses != null && listAddresses.size() > 0) {
                        if (listAddresses.get(0).getPostalCode() != null) {
                            postalCode = listAddresses.get(0).getPostalCode();
                            Log.i ("DeviceLocation" , postalCode);
                        }

                        if (listAddresses.get(0).getCountryName() != null) {
                            countryName = listAddresses.get(0).getCountryName();
                            Log.i ("DeviceLocation" , countryName);
                        }
                        //test
                        //postalCode = "50672";
                        //countryName = "Germany";

                        changeRingMode(postalCode, countryName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //we have permission
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.i("CreateOrResume", "Resume");
        populateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void populateList(){
        try {
            /*profileDatabase = this.openOrCreateDatabase("Profiles", MODE_PRIVATE, null);
            profileDatabase.execSQL("CREATE TABLE IF NOT EXISTS profiles (" +
                    "title VARCHAR, " +
                    "startTime INT(4), " +
                    "endTime INT(4), " +
                    "weekDays INT(3), " +
                    "country VARCHAR, " +
                    "postCode VARCHAR)");*/

            //profileDatabase.execSQL("INSERT INTO profiles (title, startTime, endTime, weekDays, country, postCode) VALUES ('Test2', 630, 640, 0, 'Germany', '51469')");

            Cursor cursor = profileDatabase.rawQuery("SELECT * FROM profiles", null);
            int titleIndex = cursor.getColumnIndex("title");

            //cursor.moveToFirst();
            arrayList.clear();
            numOfRows = cursor.getCount();

            if(cursor != null && cursor.moveToFirst()) {
                do{
                    arrayList.add(cursor.getString(titleIndex));
                    //numOfRows++;
                    //cursor.moveToNext();
                }while(cursor.moveToNext());
            }

            if(numOfRows == 0){
                //hideList();
                mainTextView.setVisibility(View.VISIBLE);
                mainListView.setVisibility(View.INVISIBLE);
                //mainListView.setClickable(false);
            }else{
                mainTextView.setVisibility(View.INVISIBLE);
                mainListView.setVisibility(View.VISIBLE);

                if (arrayAdapter == null) {
                    arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);
                    mainListView.setAdapter(arrayAdapter);
                } else {
                    arrayAdapter.notifyDataSetChanged();
                }
            }
            //Log.i("Check", "Clicked");
            // showList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //mainListView.setFocusable(false);
        //mainListView.setFocusableInTouchMode(false);
        Log.i("ListClick", "before Click");

    }

    public void changeRingMode(String postalCode, String countryName){
        //int ringMode;
        if(postalCode.equals("50679") && countryName.equals("Germany")){
            ringMode = 1;
            //audioManager.setRingerMode(ringMode);
        }
        else{
            ringMode = 2;
            //audioManager.setRingerMode(ringMode);
        }
        if(ringMode == 0 || audioManager.getRingerMode() == 0){
            NotificationManager notificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !notificationManager.isNotificationPolicyAccessGranted()) {

                Intent intent = new Intent(
                        android.provider.Settings
                                .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

                startActivity(intent);
            }
        }
        try {
            audioManager.setRingerMode(ringMode);
            Log.i("RingMode", "" + ringMode);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
