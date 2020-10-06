package com.rachanaxc.appingchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PersonalChatActivity extends AppCompatActivity {

    private String mConvId = "0";
    private final String LOG_TAG = "ACPersonal";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private DatabaseReference statusReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private ListView messageListView;
    private Button sendButton;
    private EditText messageEditText;
    private MessageAdapter mMessageAdapter;
    private DatabaseReference user1Reference, user2Reference;
    private String user1ID = "", user2ID="";
    private TextView toolbar_name;
    private TextView toolbar_status;
    List<Message> messagesList;
    List<String> messagesIDs;

    private Timer timer = new Timer();
    private final int DELAY = 500; //milliseconds of delay for timer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar_name = findViewById(R.id.toolbar_name);
        toolbar_status = findViewById(R.id.toolbar_status);

        Intent intent = getIntent();
        if(intent.hasExtra("convId"))
            mConvId = intent.getStringExtra("convId");
        if(intent.hasExtra("username"))
            toolbar_name.setText(intent.getStringExtra("username"));
        if(intent.hasExtra("user1ID"))
            user1ID = intent.getStringExtra("user1ID");
        if(intent.hasExtra("user2ID"))
            user2ID = intent.getStringExtra("user2ID");
        //Toast.makeText(this, "The convid is: "+ mConvId, Toast.LENGTH_SHORT).show();



        messageListView = findViewById(R.id.messageListView);
        sendButton = findViewById(R.id.sendButton);
        messageEditText = findViewById(R.id.messageEditText);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("conversationsDB").child(mConvId);
        user1Reference = mMessagesDatabaseReference.getParent().getParent().child("users").child(user1ID).child("conversations").child(user2ID).child("lastMessage");
        user2Reference = mMessagesDatabaseReference.getParent().getParent().child("users").child(user2ID).child("conversations").child(user1ID).child("lastMessage");
        statusReference = mFirebaseDatabase.getReference().child("userInfo").child(user2ID).child("status");


        //Log.d(LOG_TAG, mMessagesDatabaseReference.getKey());
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        messagesIDs = new ArrayList<>();
        messagesList = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.message_item, messagesList);
        messageListView.setAdapter(mMessageAdapter);

        attachDatabaseListener();

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                statusReference.getParent().getParent().child(user1ID).child("status").setValue("Typing...");
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                timer.cancel();
                timer = new Timer();

                timer.schedule(

                        new TimerTask() {
                            @Override
                            public void run() {
                                statusReference.getParent().getParent().child(user1ID).child("status").setValue("");
                            }
                        },
                        DELAY

                );
                //
            }
        });
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});

        // Send button sends a message and clears the EditText
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click
                Date currentTime = Calendar.getInstance().getTime();
                String textMsg = messageEditText.getText().toString();
                Message message = new Message(currentUser.getUid(),textMsg , currentTime.toString());
                message.setRead(false);
                message.setPrimary(true);
                mMessagesDatabaseReference.push().setValue(message);
                // Clear input box
                user1Reference.setValue(textMsg);
                user2Reference.setValue(textMsg);

                messageEditText.setText("");

            }
        });

        statusReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue().toString();
                toolbar_status.setText(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseListener();
        mMessageAdapter.clear();
        //Clear ADAPTER
    }

    @Override
    protected void onResume() {

        super.onResume();
        attachDatabaseListener();
    }

    private void detachDatabaseListener() {
        if(mChildEventListener!=null){
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void attachDatabaseListener() {

        if(mChildEventListener==null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //Log.d(LOG_TAG, dataSnapshot.getValue().toString());
                    Message message = dataSnapshot.getValue(Message.class);
                    if(message.getSender().equals(currentUser.getUid())){
                        message.setPrimary(true);
                    }else{
                        message.setPrimary(false);
                        if(!message.isRead()){
                            DatabaseReference msgReadRef = mFirebaseDatabase.getReference().child("conversationsDB").child(mConvId).child(dataSnapshot.getKey()).child("read");
                            msgReadRef.setValue(true);
                            message.setRead(true);
                        }
                    }
                    messagesIDs.add(dataSnapshot.getKey());
                    mMessageAdapter.add(message);
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String key = dataSnapshot.getKey();
                    Message message = dataSnapshot.getValue(Message.class);
                    if(messagesIDs.contains(key)){
                        int position = messagesIDs.indexOf(key);
                        if(position>-1){
                            messagesList.set(position, message);
                        }
                    }
                    mMessageAdapter.notifyDataSetChanged();
                    Log.d(LOG_TAG, "updating adapter ");
                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }
}
