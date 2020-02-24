package rconapp;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class History {
    public static void add(Message message) {
        SQLData.getInstance().insert(message.getSQLContentValues());
    }

    public static List<Message> getSQLData(){

        List<Message> list = new ArrayList<>();

        Cursor cursor = SQLData.getInstance().getCursor();
        if (cursor != null){
            while(cursor.moveToNext()) {
                String text = cursor.getString(cursor.getColumnIndex(SQLContract.DataEntry.COLUMN_MESSAGE));
                String date = cursor.getString(cursor.getColumnIndex(SQLContract.DataEntry.COLUMN_DATE));
                Integer isnotify = cursor.getInt(cursor.getColumnIndex(SQLContract.DataEntry.COLUMN_ISNOTIFY));
                Integer ischat = cursor.getInt(cursor.getColumnIndex(SQLContract.DataEntry.COLUMN_ISCHAT));
                Message message = new Message(text, date, isnotify.equals(1), ischat.equals(1));
                list.add(message);
            }
            cursor.close();

        }

        return list;
    }

    public static void sendDataMessages(){
        // Fitch history from service rcons
        List<Message> historyMessages = History.getSQLData();
        for (int i = 0; i < historyMessages.size(); i++){
            Message m = historyMessages.get(i);
            MainActivity.Output(m);
        }
        AppService.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.getInstance().fullScrollChat();
                MainActivity.getInstance().fullScrollConsole();
            }
        });
        History.clear();
    }

    public static void clear(){
        Notifications.lastMessageAmount = 0;
        SQLData.getInstance().clear();
    }
}