package com.rachanaxc.appingchat;

import android.app.Activity;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RequestAdapter extends ArrayAdapter<Request> {

    private FirebaseDatabase mFirebaseDatabase =FirebaseDatabase.getInstance();
    private DatabaseReference mUpdateRef;

    public RequestAdapter(@NonNull Context context, int resource, @NonNull List<Request> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.request_item, parent, false);
        }

        TextView username = convertView.findViewById(R.id.request_sender);
        Button acceptbtn = convertView.findViewById(R.id.accept_request);
        Button denybtn = convertView.findViewById(R.id.deny_request);

        final Request request = getItem(position);

        username.setText(request.getFrom_name());

        acceptbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request.setAccepted(true);
                remove(request);
                notifyDataSetChanged();
                if(request.getRequestId()!=null && !request.getRequestId().equals("")){
                    mUpdateRef = mFirebaseDatabase.getReference().child("chatRequests").child(request.getRequestId());
                    mUpdateRef.setValue(request);
                }
                String toKey = getAlphaNumericString(20);
                Log.d("ACMain", toKey);
                mFirebaseDatabase.getReference()
                        .child("users")
                        .child(request.to)
                        .child("conversations")
                        .child(request.from)
                        .child("convId")
                        .setValue(toKey);

                mFirebaseDatabase.getReference()
                        .child("users")
                        .child(request.from)
                        .child("conversations")
                        .child(request.to)
                        .child("convId")
                        .setValue(toKey);
            }
        });

        denybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request.setAccepted(false);
                remove(request);
                if(request.getRequestId()!=null && !request.getRequestId().equals("")){
                    mUpdateRef = mFirebaseDatabase.getReference().child("chatRequests").child(request.getRequestId());
                    mUpdateRef.removeValue();
                }
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

        // function to generate a random string of length n
        static String getAlphaNumericString(int n)
        {
            String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "0123456789"
                    + "abcdefghijklmnopqrstuvxyz";
            StringBuilder sb = new StringBuilder(n);
            for (int i = 0; i < n; i++) {
                int index
                        = (int)(AlphaNumericString.length()
                        * Math.random());
                // add Character one by one in end of sb
                sb.append(AlphaNumericString
                        .charAt(index));
            }
            return sb.toString();
        }


    }
