package com.rachanaxc.appingchat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {

    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item, parent, false);
        }

        TextView tvMessage = convertView.findViewById(R.id.tv_message);
        LinearLayout llmessage = convertView.findViewById(R.id.llmessage);

        Message message = getItem(position);
        tvMessage.setText(message.text);

        ImageView readReciept = (ImageView)convertView.findViewById(R.id.iv_readReceipt);

        if(message.isPrimary()){
            llmessage.setGravity(Gravity.RIGHT);
            readReciept.setVisibility(View.VISIBLE);
            if(message.isRead()){
                readReciept.setImageResource(R.drawable.ic_check_blue);
            }else{
                readReciept.setImageResource(R.drawable.ic_check_white);
            }
        }else{
            Log.d("MessageAdapter", message.sender);
            llmessage.setGravity(Gravity.LEFT);
            readReciept.setVisibility(View.GONE);
        }

        TextView tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        if(message.getTimestamp()!=null){
            String raw_time = message.getTimestamp();
            String[] display_time = raw_time.split("\\s");
            tv_time.setText(display_time[3].substring(0, 5));
        }else{
            tv_time.setText("");
        }


        return convertView;
    }
}
