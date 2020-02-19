package com.example.rconapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class EditServer {

    public static ColorDrawable transparentDrawableColor = new ColorDrawable(android.graphics.Color.TRANSPARENT);

    public static void EditServer(final Context context, final Config.Server server) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.view_server_edit);

        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(transparentDrawableColor);

        final EditText eName = dialog.findViewById(R.id.server_edit_name);
        final EditText eIp =  dialog.findViewById(R.id.server_edit_ip);
        final EditText ePort =  dialog.findViewById(R.id.server_edit_port);
        final EditText ePassword =  dialog.findViewById(R.id.server_edit_pass);
        Button butOk =  dialog.findViewById(R.id.server_edit_ok);
        Button butDelete =  dialog.findViewById(R.id.server_edit_delete);

        if (server != null) {
            butDelete.setVisibility(View.VISIBLE);
            eName.setText(server.Name);
            eIp.setText(server.IP);
            ePort.setText(server.Port);
            ePassword.setText(server.Password);
        }

        butDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                RconManager.remove(server);
                Config.getConfig().ServerList.remove(server);
                MainActivity.Instance.UpdateMenu();
                onDone(context, dialog);
                return false;
            }
        });

        butOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = eName.getText().toString();
                String ip = eIp.getText().toString();
                String port = ePort.getText().toString();
                String pass = ePassword.getText().toString();

                if (!name.isEmpty() && !ip.isEmpty() && !port.isEmpty() && !pass.isEmpty()){

                    if (server != null){
                        RconManager.remove(server);
                        Config.getConfig().ServerList.remove(server);
                    }
                    Config.Server newServer = new Config.Server(name,ip,port,pass);

                    // Move away this check
                    boolean isExists = false;
                    for (int i = 0; i < Config.getConfig().ServerList.size(); i++){
                        Config.Server server = Config.getConfig().ServerList.get(i);
                        if (newServer.IP.equals(server.IP) &&
                            newServer.Port.equals(server.Port)){
                            isExists = true;
                            break;
                        }
                    }
                    if (!isExists) {
                        Config.getConfig().ServerList.add(newServer);
                        MainActivity.Instance.UpdateMenu();
                        RconManager.add(newServer);
                    }
                }

                onDone(context, dialog);
            }
        });

        dialog.show();
    }

    private static void onDone(Context context, Dialog dialog){
        Config.saveConfig();
        // Hide a keyboard
        View view = dialog.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        dialog.dismiss();
        SettingsActivity.Instance.UpdateServersList();
    }

}
