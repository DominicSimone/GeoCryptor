package com.example.dominic.geocryptor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Dominic on 7/19/2017.
 */

public class CreateActivity extends AppCompatActivity {

    private GeoCryptor geo;
    private LocationFinder locFinder;
    private EditText content;
    private EditText title;

    private String locationKey = "";
    private String formatLat = "";
    private String formatLong = "";

    private byte[] filetext;

    private boolean fileNameFound;

    private File myDirectory;
    private File file;
    private FileOutputStream fos;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //old was content_create
        setContentView(R.layout.content_create);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        geo = new GeoCryptor();
        title = (EditText) findViewById(R.id.fileTitle);
        content = (EditText) findViewById(R.id.fileContent);

        //Start requesting updates to location
        final Context context = this;
        locFinder = new LocationFinder(this, context);
        locFinder.startUpdatingLocation();


        //Floating action button that 'locks' the information provided
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_lock);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileNameFound = true;
                myDirectory = new File(getFilesDir(), "mydirectory");
                myDirectory.mkdir();

                //Checks to make sure the file name is not null and also is not already taken
                if(title.getText().toString() != "" && content.getText().toString() != "") {
                    try {
                        for (File f : myDirectory.listFiles()) {
                            if (title.getText().toString().equals(f.getName())) {
                                showAlert("File not created", "That title was already used, please choose a different title.", context);
                                fileNameFound = false;
                            }
                        }
                    }
                    catch(Exception e){
                        System.out.println(e.getMessage());
                    }

                    //If the filename is not already used, continue
                    if(fileNameFound) {
                        //This block formats the current best location to be used as a key later
                        try {
                            locationKey = locFinder.getLocationKey();

                        } catch (Exception e) {
                            System.out.print(e.getMessage());
                        }

                        //This block encrypts the entered text
                        try {
                            byte[] encryptedBytes = geo.encryptText(content.getText().toString(), locationKey);
                            filetext = encryptedBytes;
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }

                        //This block saves the title and encrypted text to a file
                        //files located in data/data/com.example.dominic.geocryptor
                        try {
                            file = new File(myDirectory, title.getText().toString());
                            fos = new FileOutputStream(file, false);
                            fos.write(filetext);
                            fos.close();
                            //-----
                            System.out.println(file.getPath());
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }

                        //stop requesting updates to location here
                        locFinder.stopUpdatingLocation();

                        //Back to the main screen!
                        Intent backToMain = new Intent(context, MainActivity.class);
                        startActivity(backToMain);
                    }
                }
            }
        });
    }


    /**
     * Creates a popup
     * @param title Message box title
     * @param content Message box content
     * @param context Context of the message box
     */
    public static void showAlert(String title, String content, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //Set message and title
        builder.setMessage(content);
        builder.setTitle(title);

        //Add button
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //OK Button does nothing
                    }
                });

        //create and show alert
        AlertDialog dialog = builder.create();
        dialog.show();
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
        if (id == R.id.action_external_file) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    //Resume updating location if the application is restarted (EX. coming back from location settings)
    public void onRestart(){
        super.onRestart();
        locFinder.updateContext(this, this);
        locFinder.startUpdatingLocation();
    }
}
