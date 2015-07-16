package com.alexander.passkeep;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alexander.passkeep.Tools.TripleDesHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;


public class LoginActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Passkeep - Login");

        login = (Button) findViewById(R.id.login_button);
        pass_box = (EditText) findViewById(R.id.login_passBox);

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Login();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        File file = new File(getFilesDir().getPath() + "/mainFile.keep");
        if (!file.exists())
            NoExistingFile();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        pass_box.setText("");
    }

    public void Login()
    {
        String password = pass_box.getText().toString();
        File main = new File(getFilesDir().getPath() + "/mainFile.keep");

        if (!main.exists()) {
            NoExistingFile();
            return;
        }

        if (password.equals("")) {
            Toast.makeText(this, "No Password Given!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TripleDesHandler.Decrypt(password, main))
        {
            Intent intent = new Intent(this, PassActivity.class);
            intent.putExtra("password", password);
            startActivity(intent);
        }
        else
        {
            pass_box.setText("");

            Toast.makeText(this, "Incorrect Passphrase!", Toast.LENGTH_LONG).show();
        }
    }

    public void NoExistingFile()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Passwords File");
        builder.setMessage("You can either create a new password file or import an existing one.");
        builder.setPositiveButton("Create File", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                try {
                    openFileOutput("mainFile.keep", Context.MODE_PRIVATE);

                    File main = new File(getFilesDir() + "/mainFile.keep");
                    TripleDesHandler.Encrypt("0000", main);

                    Toast.makeText(LoginActivity.this, "File Default Password: 0000", Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNeutralButton("Import File", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                ImportFile();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                arg0.cancel();
            }
        });

        builder.create().show();
    }

    private void ImportFile()
    {
        Intent getFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getFileIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
        getFileIntent.setType("*/*");
        Intent chooser = Intent.createChooser(getFileIntent, "Select File:");
        if (getFileIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(getFileIntent, FILE_REQUEST_CODE);
        else
            Toast.makeText(this, "No Application avaliable to select File!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri fileUri = data.getData();

            try {
                FileInputStream fis = (FileInputStream) getContentResolver().openInputStream(fileUri);
                FileOutputStream fos = openFileOutput("mainFile.keep", Context.MODE_PRIVATE);

                int read = 0;
                byte[] buffer = new byte[1024];
                while ((read = fis.read(buffer)) != -1)
                {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
                fis.close();
                fos.close();
            } catch (IOException e) {
                Log.e("PASSKEEP", e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_deleteFile) {
            File main = new File(getFilesDir().getPath() + "/mainFile.keep");
            main.delete();
        }

        return super.onOptionsItemSelected(item);
    }

    private Button login;
    private EditText pass_box;
    private static final int FILE_REQUEST_CODE = 1;
}
