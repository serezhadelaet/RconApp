package com.example.rconapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class NewServerActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_add);

        Button butOk = findViewById(R.id.server_add_ok);
        butOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText eName = (EditText)findViewById(R.id.server_add_name);
                String name = eName.getText().toString();

                EditText eIp = (EditText)findViewById(R.id.server_add_ip);
                String ip = eIp.getText().toString();

                EditText ePort = (EditText)findViewById(R.id.server_add_port);
                String port = ePort.getText().toString();

                EditText ePass = (EditText)findViewById(R.id.server_add_pass);
                String pass = ePass.getText().toString();

                if (!name.isEmpty() && !ip.isEmpty() && !port.isEmpty() && !pass.isEmpty()){

                    Config.Server server = new Config.Server(name, ip, port, pass);
                    Config.getConfig().ServerList.add(server);
                    MainActivity.Instance.UpdateMenu();
                    RconManager.add(server);
                    Config.saveConfig();
                }
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
