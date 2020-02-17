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

public class NewServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_add);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#DAE4E5\">New server</font>"));

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        Button butOk = (Button)findViewById(R.id.server_add_ok);
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
