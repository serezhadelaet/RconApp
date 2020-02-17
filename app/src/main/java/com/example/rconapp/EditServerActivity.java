package com.example.rconapp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class EditServerActivity extends AppCompatActivity {

    public static Config.Server serverToEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_server_edit);

        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#DAE4E5\">Edit</font>"));

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        EditText eName = (EditText)findViewById(R.id.server_edit_name);
        eName.setText(serverToEdit.Name);

        EditText eIp = (EditText)findViewById(R.id.server_edit_ip);
        eIp.setText(serverToEdit.IP);

        EditText ePort = (EditText)findViewById(R.id.server_edit_port);
        ePort.setText(serverToEdit.Port);

        EditText ePassword = (EditText)findViewById(R.id.server_edit_pass);
        ePassword.setText(serverToEdit.Password);

        Button butOk = (Button)findViewById(R.id.server_edit_ok);
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

                    RconManager.remove(serverToEdit);

                    serverToEdit.Name = name;
                    serverToEdit.Password = pass;
                    serverToEdit.IP = ip;
                    serverToEdit.Port = port;

                    MainActivity.Instance.UpdateMenu();
                    RconManager.add(serverToEdit);
                }
                Config.saveConfig();
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
