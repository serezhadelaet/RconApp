package rconapp;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.rconapp.R;

public class EditServer {

    public static void EditServer(final Context context, final Server server) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.view_server_edit);

        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(MainActivity.transparentDrawableColor);

        fillData(context, dialog, server);

        dialog.show();
    }

    private static void fillData(final Context context, final Dialog dialog, final Server server){
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
                Config.getConfig().getServerList().remove(server);
                MainActivity.getInstance().UpdateMenu();
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
                        Config.removeServer(server);
                    }
                    Server newServer = new Server(name,ip,port,pass);

                    boolean isAdded = Config.addServer(newServer);

                    if (isAdded) {
                        MainActivity.getInstance().UpdateMenu();
                        RconManager.add(newServer);
                    }
                }

                onDone(context, dialog);
            }
        });
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
