package com.rachanaxc.appingchat;

import android.app.Activity;
import android.content.Context;
import android.os.TestLooperManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ChatScreenAdapter extends ArrayAdapter<User> {

    public ChatScreenAdapter(Context context, int resource,List<User> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.chat_item, parent, false);
        }

        TextView uName =(TextView)convertView.findViewById(R.id.tv_uname);

        User user = getItem(position);

        uName.setText(user.getName());


        TextView uLastmsg = (TextView) convertView.findViewById(R.id.tv_lastmsg);
        uLastmsg.setText(user.getLastMsg());

        return convertView;
    }
}
