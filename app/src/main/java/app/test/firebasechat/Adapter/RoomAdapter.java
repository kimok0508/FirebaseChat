package app.test.firebasechat.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import app.test.firebasechat.Model.ChatRoom;
import app.test.firebasechat.R;

/**
 * Created by kimok_000 on 2017-02-12.
 */

public class RoomAdapter extends ArrayAdapter<Pair<String, ChatRoom>> {
    private LayoutInflater inflater;

    public RoomAdapter(Context context, ArrayList<Pair<String, ChatRoom>> arrayList) {
        super(context, 0, arrayList);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            view = inflater.inflate(R.layout.layout_room, null);
        } else view = convertView;

        final ChatRoom room = this.getItem(position).second;
        TextView textName = (TextView) view.findViewById(R.id.room_text_name);
        TextView textDesc = (TextView) view.findViewById(R.id.room_text_desc);
        textName.setText(room.getName().trim());
        textDesc.setText(room.getDesc().trim());

        return view;
    }
}
