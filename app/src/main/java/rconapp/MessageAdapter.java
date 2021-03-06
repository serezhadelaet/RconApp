package rconapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rconapp.R;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    private List<Message> messages = new ArrayList<>();
    private Context context;

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void add(Message message) {
        this.messages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        convertView = messageInflater.inflate(R.layout.layout_message, null);

        TextView body = convertView.findViewById(R.id.message_body);
        TextView date = convertView.findViewById(R.id.message_date);
        date.setText(message.getDate());
        body.setText(message.getText());
        if (message.isNotification()){
            convertView.setBackgroundColor(convertView.getResources().getColor(R.color.colorNotify));
        }
        return convertView;
    }
}