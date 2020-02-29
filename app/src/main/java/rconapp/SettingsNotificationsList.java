package rconapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import com.example.rconapp.R;
import java.util.List;

public class SettingsNotificationsList {

    public static ListView listViewNotifications;

    public static class NotificationsListAdapter extends ArrayAdapter<NotificationsItem> {

        private int resourceLayout;
        private Context mContext;

        public NotificationsListAdapter(Context context, int resource, List<NotificationsItem> items) {
            super(context, resource, items);
            this.resourceLayout = resource;
            this.mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(mContext);
                v = vi.inflate(resourceLayout, null);
            }

            final NotificationsItem item = getItem(position);
            final TextView text = v.findViewById(R.id.layout_notifications_item_text);
            final CheckBox notify = v.findViewById(R.id.layout_notifications_item_notify);
            final CheckBox vibration = v.findViewById(R.id.layout_notifications_item_vibration);
            final CheckBox sound = v.findViewById(R.id.layout_notifications_item_sound);

            Button del = v.findViewById(R.id.layout_notifications_item_del);

            del.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Config.getConfig().getNotifications().remove(item);
                    Config.saveConfig();
                    UpdateNotificationsList();
                    return true;
                }
            });

            text.setText(item.getText());
            notify.setChecked(item.isNotify());
            vibration.setChecked(item.hasVibration());
            sound.setChecked(item.hasSound());

            text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        item.SetText(text.getText().toString());
                        Config.saveConfig();
                    }
                }
            });

            notify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.SetNotify(isChecked);
                    Config.saveConfig();
                }
            });

            vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.SetVibration(isChecked);
                    Config.saveConfig();
                }
            });

            sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.SetSound(isChecked);
                    Config.saveConfig();
                }
            });

            return v;
        }

    }

    public static void UpdateNotificationsList() {
        listViewNotifications =
                SettingsActivity.Instance.findViewById(R.id.activity_settings_listview_notifications);
        List<NotificationsItem> list = Config.getConfig().getNotifications();
        NotificationsListAdapter customAdapter =
                new NotificationsListAdapter(SettingsActivity.Instance.getApplicationContext(),
                        R.layout.layout_notifications_item, list);
        listViewNotifications.setAdapter(customAdapter);
    }

    public static void HandleButtonAddNotification(){
        Button add = SettingsActivity.Instance.findViewById(R.id.layout_notifications_add_new);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.getConfig().getNotifications().add(new NotificationsItem("Text..."));
                Config.saveConfig();
                UpdateNotificationsList();
            }
        });
    }

}
