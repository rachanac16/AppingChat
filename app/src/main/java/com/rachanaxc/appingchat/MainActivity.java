package com.rachanaxc.appingchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mPersonalChatReference;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseUser currentUser;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private ValueEventListener mValueEventListener;
    private ChatScreenAdapter mChatScreenAdapter;

    private ProgressBar progressBar;

    private ListView chatListView;

    private HashMap<String, HashMap<String, String>> mConversations;

    private String mUsername;
    private String ANONYMOUS = "anonymous";
    private int RC_SIGN_IN = 1;
    private final String LOG_TAG = "ACMain";
    private String switchUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        chatListView = findViewById(R.id.chatListView);

        //initialising firebase objects
        mFirebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null){
            mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("users").child(currentUser.getUid());
        }
        mPersonalChatReference = mFirebaseDatabase.getReference().child("conversationsDB");

        /*mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("chat_photos");
        */

        List<User> mUsers = new ArrayList<>();
        mChatScreenAdapter = new ChatScreenAdapter(this, R.layout.chat_item, mUsers);
        chatListView.setAdapter(mChatScreenAdapter);


        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final User clickedUser = mChatScreenAdapter.getItem(position);
                final Intent intent = new Intent(MainActivity.this, PersonalChatActivity.class);
                Log.d(LOG_TAG, mConversations.toString());
                HashMap<String, String> temp = mConversations.get(clickedUser.getuId());
                if(temp.containsKey("convId")){
                    intent.putExtra("convId", mConversations.get(clickedUser.getuId()).get("convId")+"");
                }
                mPersonalChatReference = mFirebaseDatabase.getReference().child("userInfo").child(clickedUser.getuId()).child("name");

                mPersonalChatReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(LOG_TAG, dataSnapshot.getValue().toString());
                        switchUser= dataSnapshot.getValue().toString();
                        intent.putExtra("username", switchUser);
                        Log.d(LOG_TAG, "1:"+currentUser.getUid()+"2:"+clickedUser.getuId());
                        intent.putExtra("user1ID", currentUser.getUid());
                        intent.putExtra("user2ID", clickedUser.getuId());
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    onSignedInInitialize(user.getDisplayName());
                }else{
                    onSignedOutCleanUp();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_send_request:
                        showAddItemDialog(MainActivity.this);
                        break;
                    case R.id.action_view_request:
                        Intent intent = new Intent(MainActivity.this, ViewRequestActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_nearby:
                        break;
                }
                return true;
            }
        });

        //attachValueEventListener();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener !=null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseListener();
        mChatScreenAdapter.clear();
        //Clear ADAPTER
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_signout:
                AuthUI.getInstance().signOut(this);
                return true;
            //case R.id.clear_chat_menu:
                //mMessagesDatabaseReference.updateChildren(null);
                //Log.d(log_tag, mMessagesDatabaseReference.child("messages").push().getKey());
                //return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAddItemDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Send Chat Request")
                .setMessage("Enter email of the friend")
                .setView(taskEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = String.valueOf(taskEditText.getText());
                        if(email.equals(currentUser.getEmail())){
                            Toast.makeText(MainActivity.this, "Enter an email which is not yours", Toast.LENGTH_SHORT).show();
                        }else{
                            sendRequestViaApp(email);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void sendRequestViaApp(final String email) {
        DatabaseReference findEmailRef = FirebaseDatabase.getInstance().getReference().child("userInfo");
        findEmailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean send = false;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    if(user.getEmail().equals(email)){
                        DatabaseReference sendRequest = FirebaseDatabase.getInstance().getReference().child("chatRequests");
                        Request request = new Request(ds.getKey(), currentUser.getUid(),false, user.getName(), currentUser.getDisplayName());
                        sendRequest.push().setValue(request);
                        send = true;
                    }
                }
                if(!send){
                    Toast.makeText(MainActivity.this, "User with this email is not registered", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onSignedOutCleanUp() {
        mUsername = ANONYMOUS;
        //Clear ADAPTER
        mChatScreenAdapter.clear();
        detachDatabaseListener();
    }

    private void detachDatabaseListener() {
        if(mChildEventListener!=null){
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void onSignedInInitialize(String username) {
        mUsername = username;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null){
            mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("users").child(currentUser.getUid());
        }
        Log.d(LOG_TAG, "Started Adding");
        progressBar.setVisibility(View.VISIBLE);
        attachDatabaseListener();

    }

    private void checkUserInfoExist() {
        Log.d(LOG_TAG, "checking started");
        final boolean[] exists = {false};
        final DatabaseReference userInfoRef = mFirebaseDatabase.getReference().child("userInfo");
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.getKey().equals(currentUser.getUid())){
                        exists[0] = true;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d(LOG_TAG, exists[0]+"");
        if(!exists[0]){
            Log.d(LOG_TAG, currentUser.getDisplayName());
            User user = new User(currentUser.getDisplayName(), currentUser.getEmail(), "");
            userInfoRef.child(currentUser.getUid()).setValue(user);
        }
    }

    private void attachDatabaseListener() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChildEventListener==null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    mConversations =  (HashMap<String, HashMap<String, String>>)dataSnapshot.getValue();
                    User user = new User(mConversations);

                    Log.d(LOG_TAG, mConversations.toString());
                    addValuesToAdapter();
                    progressBar.setVisibility(View.GONE);
                    Log.d(LOG_TAG, "Added to adapter");
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
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



    private void addValuesToAdapter() {
        for (final HashMap.Entry<String,HashMap<String, String>> entry : mConversations.entrySet()){
            mPersonalChatReference = FirebaseDatabase.getInstance().getReference().child("userInfo").child(entry.getKey());
            mPersonalChatReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final User toUser= dataSnapshot.getValue(User.class);
                    toUser.setuId(entry.getKey());
                    HashMap<String, String> map = entry.getValue();
                    if(map.containsKey("lastMessage")){
                        String lastMessage = String.valueOf(map.get("lastMessage"));
                        toUser.setLastMsg(lastMessage);
                    }
                    mChatScreenAdapter.add(toUser);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(LOG_TAG, "database error: personalchatreference");
                }
            });


        }

    }

    private void attachValueEventListener(){
        if (mValueEventListener==null){
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(LOG_TAG, "ValueEL: "+dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }else{
            Log.d(LOG_TAG, "In else");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Signed in Successfully", Toast.LENGTH_SHORT).show();
                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                checkUserInfoExist();
            } else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Sign in Failed!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


}
