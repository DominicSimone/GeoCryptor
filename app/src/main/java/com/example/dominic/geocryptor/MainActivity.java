package com.example.dominic.geocryptor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ListView myListView;
    File myDirectory;
    ArrayList<File> fileList;
    LocationFinder locFinder;
    byte[] fileBytes;
    boolean externalGoAhead;

    private static final int FILE_SELECT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Requesting permissions right off the bat
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //Floating action button, when tapped, brings user to the creation activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateActivity();
            }
        });

        //List of files on the home activity
        myListView = (ListView) findViewById(R.id.listView);

        //Making/getting  the directory that contains the encrypted files
        myDirectory = new File(getFilesDir(), "mydirectory");
        myDirectory.mkdir();

        //Creating the list of files
        fileList = new ArrayList<File>();

        locFinder = new LocationFinder(this, this);

        //Populating the list of files from the directory made earlier
        for(File f : myDirectory.listFiles()){
            fileList.add(f);
        }

        //Creating an adapter to port the list of files into the listview
        FileAdapter adapter = new FileAdapter(this, fileList);
        myListView.setAdapter(adapter);

        //Context needed here to be used inside the click listener below
        final Context context = this;
        //Listens for clicks on an item, and pulls that item data into a separate activity
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                File selectedFile = fileList.get(position);

                Intent fileIntent = new Intent(context, ViewFile.class);
                fileIntent.putExtra("title", selectedFile.getName());
                startActivity(fileIntent);
            }
        });
        //Listens for long clicks on an item
        myListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                showDeleteAlert(fileList.get(position));
                return true;
            }
        });
    }

    //When long clicking a file, this alerts the user and asks for permission to delete to prevent unwanted deletions
    public void showDeleteAlert(final File file){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Set message and title
        builder.setMessage("Are you sure you want to delete this file?");
        builder.setTitle("Warning");

        //Add button
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Deletes the file from storage and the fileList and refreshes the ListView
                        fileList.remove(file);
                        file.delete();
                        myListView.invalidateViews();
                    }
                });
        builder.setNegativeButton("Cancel", null);

        //create and show alert
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Starts the creation activity
    public void startCreateActivity(){
        Intent intent = new Intent(this, CreateActivity.class);
        startActivity(intent);
    }

    //test file is in /storage/emulated/0/Downloads
    public void handleExternalFile(Uri uri){
        if(isExternalStoragePermissionGranted()) {
            //Get the filename from the uri
            String filename = uri.getLastPathSegment();
            filename = uri.getLastPathSegment().substring(filename.lastIndexOf("/") + 1);

            //Makes/Accesses the directory GeoCryptor
            File geoDirectory = new File(Environment.getExternalStorageDirectory(), "GeoCryptor");
            if (!geoDirectory.exists()) {
                geoDirectory.mkdirs();

            }

            //If the file is already encrypted, we want to decrypt it
            if(filename.length() > 9 && filename.substring(filename.length() - 9).equals(".geocrypt"))
            {
                //removes the .geocrypt at the end of the file
                filename = filename.substring(0, filename.length() - 9);

                File decrypted = new File(geoDirectory, filename);

                //Tries to open an inputstream of the uri path provided to this method
                //Converts the file's contents to an array of bytes with an apache library's method
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    fileBytes = IOUtils.toByteArray(is);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Opening stream failed");
                }

                //Creates the new encryption/decryption object
                GeoCryptor geo = new GeoCryptor();
                //Begins getting the location
                locFinder.getLocation();
                try {
                    //encrypting the bytes
                    byte[] decryptedFileBytes = geo.decryptBytes(fileBytes, locFinder.getLocationKey());
                    FileOutputStream fos = new FileOutputStream(decrypted, false);
                    fos.write(decryptedFileBytes);
                    fos.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Encrypting file failed");
                }

            }
            //File is not encrypted yet, so we want to encrypt it
            else {
                filename = filename + ".geocrypt";

                //Create the new, empty file to place the encrypted bytes in
                File encrypted = new File(geoDirectory, filename);

                //Tries to open an inputstream of the uri path provided to this method
                //Converts the file's contents to an array of bytes with an apache library's method
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    fileBytes = IOUtils.toByteArray(is);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Opening stream failed");
                }

                //Creates the new encryption/decryption object
                GeoCryptor geo = new GeoCryptor();
                //Begins getting the location
                locFinder.getLocation();
                try {
                    //encrypting the bytes
                    byte[] encryptedFileBytes = geo.encryptBytes(fileBytes, locFinder.getLocationKey());
                    FileOutputStream fos = new FileOutputStream(encrypted, false);
                    fos.write(encryptedFileBytes);
                    fos.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Encrypting file failed");
                }
            }
        }
    }

    //Displays a file chooser if the user wants to work with an external file
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        //We want to start getting location early so it can get a good reading by the time they choose a file (hopefully thats how that works)
        if(locFinder.startUpdatingLocation()) {
            try {
                startActivityForResult(
                        Intent.createChooser(intent, "Select a File to Upload"),
                        FILE_SELECT_CODE);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(this, "Please install a File Manager.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    handleExternalFile(data.getData());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean isExternalStoragePermissionGranted(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            externalGoAhead = true;
        }
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
            showFileChooser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

