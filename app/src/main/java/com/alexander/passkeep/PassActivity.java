package com.alexander.passkeep;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.alexander.passkeep.Tools.TripleDesHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;


public class PassActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setDisplayUseLogoEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setLogo(R.drawable.ic_launcher);

        password = getIntent().getStringExtra("password");
        main = new File(getFilesDir().getPath() + "/mainFile.keep");

        passList = (ListView) findViewById(R.id.passView);
        LoadList();
    }

    private void LoadList() {
        try {
            FileInputStream fis = new FileInputStream(main);
            byte[] data = new byte[(int) main.length()];
            int read = fis.read(data);
            fis.close();

            if (read != main.length())
                Log.e("PassKeep", "Unable to read full File!");

            String file = new String(data, "UTF-8");
            String[] parts = file.split("\n");

            passMatrix = new Hashtable<>();
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < (parts.length / 4); i++) {
                String key = parts[i * 4];
                String[] values = new String[3];
                for (int j = 0; j < 3; j++) {
                    int value = i * 4 + j + 1;

                    values[j] = parts[value];
                }
                list.add(key);
                passMatrix.put(key, values);
            }

            listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list);

            passList.setAdapter(listAdapter);
            passList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    OnListItemSelected(parent, view, position, id);
                }
            });
            passList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    OnListItemLongClick(parent, view, position, id);
                    return true;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void OnListItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final String itemName = (String) passList.getItemAtPosition(position);

        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(itemName);
        ad.setMessage("Do you want to delete this item?");
        ad.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listAdapter.remove(itemName);
                passMatrix.remove(itemName);
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        ad.create().show();
    }

    private void OnListItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String itemName = (String) passList.getItemAtPosition(position);

        String[] subitems = passMatrix.get(itemName);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(itemName);
        builder.setMessage("Username: " + subitems[0] + "\nPassword: " + subitems[1] + "\nNotes: " + subitems[2]);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    private void SaveData() {
        try {
            FileOutputStream fos = new FileOutputStream(new File(getFilesDir(), "mainFile.keep"));

            String finalWrite = "";

            for (int i = 0; i < listAdapter.getCount(); i++) {
                String title = listAdapter.getItem(i);

                String[] subStrings = passMatrix.get(title);
                finalWrite += title + "\n" + subStrings[0] + "\n" + subStrings[1] + "\n" +
                        subStrings[2] + "\n";
            }

            fos.write(finalWrite.getBytes("UTF-8"));
            fos.flush();

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void AddItem() {
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.add_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Add a Password to List");
        builder.setView(layout);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText titleBox = (EditText) layout.findViewById(R.id.title_box);
                EditText userBox = (EditText) layout.findViewById(R.id.username_box);
                EditText passBox = (EditText) layout.findViewById(R.id.password_add_box);
                EditText noteBox = (EditText) layout.findViewById(R.id.note_box);

                String note = noteBox.getText().toString();

                note = note.equals("") ? "[None]" : note;

                String title = titleBox.getText().toString();
                String[] subitems = new String[]{userBox.getText().toString(), passBox.getText().toString(), note};

                listAdapter.add(title);
                passMatrix.put(title, subitems);

                Toast.makeText(PassActivity.this, title + " Added", Toast.LENGTH_SHORT).show();
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pass, menu);
        return true;
    }

    public void ExportFile() {
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View layout = inflater.inflate(R.layout.custom_dialog, null);
        builder.setView(layout);
        builder.setTitle("Export to .keep File");
        builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText pathText = (EditText) layout.findViewById(R.id.fileDir);
                File file = new File(pathText.getText().toString());

                if (file.getParentFile().exists()) {
                    try {
                        file.createNewFile();

                        FileInputStream fis = openFileInput("mainFile.keep");
                        FileOutputStream fos = new FileOutputStream(file);

                        int read = 0;
                        byte[] buffer = new byte[1024];
                        while ((read = fis.read(buffer)) != -1)
                            fos.write(buffer, 0, read);

                        fos.flush();
                        fis.close();
                        fos.close();

                        Toast.makeText(PassActivity.this, "File Successfully Exported", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(PassActivity.this, "Directory doesn't exist!", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    private void ChangeLoginPassword(){
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.change_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Change Login Password");
        builder.setView(layout);
        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText original = (EditText) layout.findViewById(R.id.originalPassword);
                EditText newPass = (EditText) layout.findViewById(R.id.newPassword);
                EditText checkPass = (EditText) layout.findViewById(R.id.newPasswordCheckd);

                String originalS = original.getText().toString();
                String newS = newPass.getText().toString();
                String checkS = checkPass.getText().toString();

                if (originalS.equals(password)) {
                    if (newS.equals(checkS)) {
                        password = newS;
                        Toast.makeText(PassActivity.this, "Password Changed.", Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(PassActivity.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(PassActivity.this, "Original Password not Correct!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id)
        {
            case R.id.action_addPass:
                AddItem();
                return true;

            case R.id.action_savePass:
                SaveData();
                Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_exportPass:
                ExportFile();
                return true;

            case R.id.action_changePass:
                ChangeLoginPassword();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ExitActivity();
    }

    private void ExitActivity()
    {
        SaveData();
        TripleDesHandler.Encrypt(password, main);

        Toast.makeText(this, "Data Encrypted and Saved", Toast.LENGTH_SHORT).show();

        finish();
    }

    protected String password;
    protected File main;
    protected ListView passList;
    protected Hashtable<String,String[]> passMatrix;
    protected ArrayAdapter<String> listAdapter;
}
