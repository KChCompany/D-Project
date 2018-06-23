package company.kch.d_project;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toolbar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import company.kch.d_project.adapter.MessageAdapter;
import company.kch.d_project.model.MessageModel;

public class ChatActivity extends AppCompatActivity {

    public static final String USER_NAME = "UserName";

    RecyclerView recyclerView;
    DatabaseReference reference;
    MessageAdapter adapter;
    Date currentTime;
    private List<MessageModel> result;
    private List<String> resultKey;
    double latitude;
    double longitude;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager lim = new LinearLayoutManager(this);
        lim.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(lim);

        result = new ArrayList<>();
        resultKey = new ArrayList<>();

        adapter = new MessageAdapter(result);
        recyclerView.setAdapter(adapter);

        userName = getIntent().getStringExtra(USER_NAME);

        reference = FirebaseDatabase.getInstance().getReference("chats").child(getIntent().getStringExtra("chatID")).child("messages");
        final EditText editText = findViewById(R.id.editText);
        FloatingActionButton sendButton = findViewById(R.id.sendFAB);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().equals("")) {
                    currentTime = Calendar.getInstance().getTime();
                    reference.push().setValue(new MessageModel(userName, editText.getText().toString(), currentTime, reference.push().getKey()));
                    editText.setText("");
                }
            }
        });

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);

                assert messageModel != null;

                if ((Calendar.getInstance().getTimeInMillis() - messageModel.time.getTime()) > 86400000) {
                    //reference.child(dataSnapshot.getKey()).removeValue();
                } else {
                    result.add(dataSnapshot.getValue(MessageModel.class));
                    resultKey.add(dataSnapshot.getKey());
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(result.size() - 1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);

                int index = getItmeIndex(messageModel);
                if (index >= 0) {
                    result.remove(index);
                    resultKey.remove(index);
                    adapter.notifyItemRemoved(index);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < result.size(); i++) {
                    if ((Calendar.getInstance().getTimeInMillis() - result.get(i).time.getTime()) > 3600000) {
                        reference.child(resultKey.get(i)).removeValue();
                    }
                }
                handler.postDelayed(this, 10000);
            }
        });
    }

    private int getItmeIndex(MessageModel messageModel) {
        int index = -1;

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).key.equals(messageModel.key)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        startActivity(intent);
    }

}
