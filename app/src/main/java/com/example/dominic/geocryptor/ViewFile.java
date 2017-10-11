package com.example.dominic.geocryptor;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

public class ViewFile extends AppCompatActivity {

    private TextView contents;
    private File file;
    private byte[] fileByteContents;
    private String locationKey = "";
    private String decryptedText = "";

    private LocationFinder locFinder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contents = (TextView) findViewById(R.id.fileContent);

        //Acquiring the file that the user selected from the main screen
        String title = this.getIntent().getExtras().getString("title");

        //Getting the directory
        File myDirectory = new File(getFilesDir(), "mydirectory");
        myDirectory.mkdir();

        //Finding the file title within the directory
        for(File f : myDirectory.listFiles()){
            if(f.getName().equals(title)){
                file = f;
            }
        }

        //Attempt to read the file into the fileBytesContents array
        try {
            FileInputStream fis = new FileInputStream(file);
            fileByteContents = new byte[(int)file.length()];
            fis.read(fileByteContents);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        try {
            contents.setText(new String(fileByteContents, "UTF-8"));
        } catch(Exception e){
            System.out.println("Couldn't set initial file encrypted contents");
            System.out.println(e.getMessage());
        }

        //Start requesting updates to location
        final Context context = this;
        locFinder = new LocationFinder(this, context);
        locFinder.startUpdatingLocation();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    locationKey = locFinder.getLocationKey();

                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }

                GeoCryptor geo = new GeoCryptor();

                //This block decrypts the entered text
                try {
                    byte[] decryptedBytes = geo.decryptBytes(fileByteContents, locationKey);
                    decryptedText = new String(decryptedBytes, "UTF-8");
                    if(!decryptedText.equals(""))
                        contents.setText(decryptedText);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    CreateActivity.showAlert("Error", "Decryption failed! decrypt method failed" + "\n" + locationKey , context);
                }
            }
        });
    }

    //Stop updating location if the activity is paused, stopped, or destroyed
    protected void onDestroy(){
        super.onDestroy();
        locFinder.stopUpdatingLocation();
    }
    protected void onStop(){
        super.onStop();
        locFinder.stopUpdatingLocation();
    }
    protected void onPause(){
        super.onPause();
        locFinder.stopUpdatingLocation();
    }

    //Resume updating location if the application is resumed
    public void onRestart(){
        super.onRestart();
        locFinder.updateContext(this, this);
        locFinder.startUpdatingLocation();
    }

}
