package com.rachanaxc.appingchat;

import android.content.Intent;
import android.os.Bundle;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestActivity extends AppCompatActivity {


    private final String LOG_TAG = "ACRequest";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRequestDBRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser currentUSer;
    private TextView emptyTV;
    private ListView requestListView;
    private RequestAdapter mRequestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("View Requests");
        setSupportActionBar(toolbar);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRequestDBRef =  mFirebaseDatabase.getReference().child("chatRequests");
        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUSer = mFirebaseAuth.getCurrentUser();

        emptyTV = findViewById(R.id.emptytv);

        requestListView = findViewById(R.id.requestListView);

        List<Request> requests = new ArrayList<>();
        mRequestAdapter = new RequestAdapter(this, R.layout.request_item ,requests);
        requestListView.setAdapter(mRequestAdapter);

        attachDatabaseListener();
        emptyTV.setVisibility(View.VISIBLE);
    }

    private void attachDatabaseListener() {
        mRequestDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Request request = dataSnapshot.getValue(Request.class);
                request.setRequestId(dataSnapshot.getKey());
                if(request.getTo().equals(currentUSer.getUid()) && !request.isAccepted()){
                    mRequestAdapter.add(request);
                    emptyTV.setVisibility(View.GONE);
                }else{
                    emptyTV.setVisibility(View.VISIBLE);
                }
                Log.d(LOG_TAG, "added");
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(LOG_TAG, "changed: "+dataSnapshot.getValue().toString());
                attachDatabaseListener();
//                Request request = dataSnapshot.getValue(Request.class);
//                mFirebaseDatabase.getReference().child(request.getRequestId()).setValue(request);
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

}
