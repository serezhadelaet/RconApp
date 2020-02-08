package com.example.rconapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class EditServerActivity extends Activity {

    public static Config.Server serverToEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_server_edit);

        EditText eName = (EditText)findViewById(R.id.server_edit_name);
        eName.setText(serverToEdit.Name);

        EditText eIp = (EditText)findViewById(R.id.server_edit_ip);
        eIp.setText(serverToEdit.IP);

        EditText ePort = (EditText)findViewById(R.id.server_edit_port);
        ePort.setText(serverToEdit.Port);

        EditText ePassword = (EditText)findViewById(R.id.server_edit_pass);
        ePassword.setText(serverToEdit.Password);

        Button butOk = findViewById(R.id.server_edit_ok);
        butOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText eName = (EditText)findViewById(R.id.server_edit_name);
                String name = eName.getText().toString();

                EditText eIp = (EditText)findViewById(R.id.server_edit_ip);
                String ip = eIp.getText().toString();

                EditText ePort = (EditText)findViewById(R.id.server_edit_port);
                String port = ePort.getText().toString();

                EditText ePass = (EditText)findViewById(R.id.server_edit_pass);
                String pass = ePass.getText().toString();
                if (!name.isEmpty() && !ip.isEmpty() && !port.isEmpty() && !pass.isEmpty()){

                    MainActivity.rconManager.remove(serverToEdit);

                    serverToEdit.Name = name;
                    serverToEdit.Password = pass;
                    serverToEdit.IP = ip;
                    serverToEdit.Port = port;

                    MainActivity.Instance.UpdateMenu();
                    MainActivity.rconManager.add(serverToEdit);
                }
                MainActivity.Instance.loadOrSaveConfg(true);
                // Hide a keyboard
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                finish();
            }
        });
    }

}
