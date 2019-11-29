package com.suman.schedulesilent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class CreateOrEditActivity extends AppCompatActivity {

    ListView listViewCreateOrEdit;
    int startHour, endHour, startMinute, endMinute;
    String title, startTime, endTime;
    ArrayList<HashMap<String, String>> listViewData = new ArrayList<>();
    SimpleAdapter profileAdapter;
    Button buttonDelete, buttonSave, buttonDiscard;
    String[] titleArray;
    String[] itemArray;
    boolean newProfile;
    SQLiteDatabase profileDatabase;
    String query, profileTitle, profileCountry, profilePostCode;
    int profileStartTime, profileEndTime, profileRepeatDays;
    Intent intent;

    public void discardProfile(View view){
        //this.finish();
        Toast.makeText(this,"Profile Discarded", Toast.LENGTH_LONG).show();
        super.onBackPressed();
        //return;
    }

    public void saveProfile(View view){
        //this.finish();
        if(newProfile){
            insertData();
        }
        else{
            updateData();
        }
        Toast.makeText(this,"Profile Saved Successfully", Toast.LENGTH_LONG).show();
        super.onBackPressed();
        //return;
    }

    public void deleteProfile(View view){
        //this.finish();
        //itemArray[3] = "Change";
        //setData();
        //profileAdapter.notifyDataSetChanged();
        Toast.makeText(this,"Profile Deleted Successfully", Toast.LENGTH_LONG).show();
        //super.onBackPressed();
        //return;
    }

    public void createNewProfile(){
        setTitle("Create New Profile");
        buttonDelete.setVisibility(View.INVISIBLE);
        /*ArrayList<String> profileItem = new ArrayList<>();
        profileItem.add("Title");
        profileItem.add("Start Time");
        profileItem.add("End Time");
        profileItem.add("Repeat");
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, profileItem);
        listViewCreateOrEdit.setAdapter(arrayAdapter);*/
        //String formatted = String.format("%03d", currentTime.get(Calendar.HOUR_OF_DAY));

        Calendar currentTime = Calendar.getInstance();
        startHour = currentTime.get(Calendar.HOUR_OF_DAY);
        startMinute = currentTime.get(Calendar.MINUTE);
        startTime = String.format("%02d", startHour) + ":" + String.format("%02d", startMinute);
        currentTime.add(Calendar.MINUTE, 5);
        endHour = currentTime.get(Calendar.HOUR_OF_DAY);
        endMinute = currentTime.get(Calendar.MINUTE);
        endTime = String.format("%02d", endHour) + ":" + String.format("%02d", endMinute);

        titleArray = new String[]{"Title", "Start Time", "End Time", "Repeat", "Country", "Post Code"};
        itemArray = new String[]{"Title", startTime, endTime, "Repeat", "Select Country", "Enter Post Code"};

        setData();
    }

    public void editProfile(){

        setTitle("Edit Profile");
        buttonDelete.setVisibility(View.VISIBLE);

        int cursorPosition = intent.getIntExtra("position", 0);

        Cursor cursor = profileDatabase.rawQuery("SELECT * FROM profiles", null);

        Log.i("CheckView", String.valueOf(cursorPosition));

        setData();
    }

    public void setData(){
        listViewData.clear();
        for(int i=0;i <titleArray.length; i++){
            HashMap<String,String> datum = new HashMap<>();
            datum.put("Title", titleArray[i]);
            datum.put("Text", itemArray[i]);
            listViewData.add(datum);
        }

        profileAdapter = new SimpleAdapter(this, listViewData, android.R.layout.simple_list_item_2, new String[] {"Title", "Text"}, new int[] {android.R.id.text1, android.R.id.text2});
        listViewCreateOrEdit.setAdapter(profileAdapter);

        listViewCreateOrEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("ListClick", String.valueOf(position));
            }
        });

        profileTitle = itemArray[0];
        profileStartTime = startHour*60 + startMinute;
        profileEndTime = endHour*60 + endMinute;
        profileRepeatDays = 0; //implement later
        profileCountry = itemArray[4];
        profilePostCode = itemArray[5];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit);

        listViewCreateOrEdit = (ListView)findViewById(R.id.listViewCreateOrEdit);
        //Calendar currentTime = Calendar.getInstance();
        //startHour = currentTime.get(Calendar.HOUR_OF_DAY);
        //startMinute = currentTime.get(Calendar.MINUTE);

        buttonDelete = (Button)findViewById(R.id.buttonDelete);

        intent = getIntent();
        newProfile = intent.getBooleanExtra("new", true);

        profileDatabase = this.openOrCreateDatabase("Profiles", MODE_PRIVATE, null);

        if(newProfile) {
            //setTitle("Create New Profile");
            createNewProfile();
        }
        else{
            //editProfile();
            createNewProfile();
        }

    }

    public void insertData(){
        try{
            query = "INSERT INTO profiles (title, startTime, endTime, weekDays, country, postCode) " +
                    "VALUES (" + "'" + profileTitle + "', "
                    + profileStartTime + ", "
                    + profileEndTime + ", "
                    + profileRepeatDays + ", "
                    + "'" + profileCountry + "', "
                    + "'" + profilePostCode + "'" + ")";

            profileDatabase.execSQL(query);
            //profileDatabase.execSQL("INSERT INTO profiles (title, startTime, endTime, weekDays, country, postCode) VALUES ('Test3', 630, 640, 0, 'Germany', '51469')");
            Log.i("Test Database", "Insert Successful");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Test Database", "Insert Failed");
        }

    }

    public void updateData(){

    }
}
