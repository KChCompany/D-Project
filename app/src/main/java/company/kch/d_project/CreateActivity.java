package company.kch.d_project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

import company.kch.d_project.model.ChatModel;

public class CreateActivity extends AppCompatActivity {

    EditText chatName;
    String userName;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        chatName = findViewById(R.id.editText2);
        reference = FirebaseDatabase.getInstance().getReference("chats");
        userName = getIntent().getStringExtra("UserName");


    }

    public void createButton (View view) {
        Date currentTime = Calendar.getInstance().getTime();
        DatabaseReference ref = reference.push();
        ref.setValue(new ChatModel(userName, chatName.getText().toString(), currentTime, getIntent().getDoubleExtra("Lat", 0), getIntent().getDoubleExtra("Long", 0)));
        Intent intent = new Intent(CreateActivity.this, ChatActivity.class);
        intent.putExtra("chatID", ref.getKey());
        intent.putExtra("UserName", userName);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CreateActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
