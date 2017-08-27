package app.test.firebasechat.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.test.firebasechat.Adapter.MessageAdapter;
import app.test.firebasechat.Model.ChatMessage;
import app.test.firebasechat.R;

/**
 * Created by kimok_000 on 2017-02-12.
 */

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_PERMISSION = 1;

    private SharedPreferences sharedPreferences;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference dbServerOffset;
    private DatabaseReference dbConnection;
    private DatabaseReference dbRoot;
    private DatabaseReference dbMessage;
    private StorageReference stRoot;
    private StorageReference stMessage;

    private MessageAdapter messageAdapter;
    private LinearLayoutManager linearLayoutManager;

    private RecyclerView recyclerMessages;
    private ImageView imgPicture;
    private ImageView imgSend;
    private EditText editMessage;

    private ArrayList<ChatMessage> messageList = new ArrayList<>();
    private String roomId = "", roomName = "";
    private String myName = "", myProfileUrl = "";
    private boolean isConnected = true;
    private double serverOffset = 0;

    private void setDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRoot = firebaseDatabase.getReference().getRoot();                                         //DB 루트 경로
        dbMessage = dbRoot.child("RoomList").child(roomId).child("messages");                       //DB 현재 room의 messages경로
        dbMessage.orderByChild("time").addChildEventListener(new ChildEventListener() {         //DB 변경 사항을 시간순으로 수신
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {                         //새 메시지 추가됨
                final ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
                String sender = message.getSender();
                String receiver = message.getReceiver();

                if (sender == null) sender = "";
                if (receiver == null) receiver = "";

                if (message != null) {
                    if (receiver.equals("") || receiver.equals(myName) || (!receiver.equals(myName) && sender.equals(myName))) {                            //receiver가 나거나 지정되어 있지 않으면 표시
                        messageList.add(message);

                        messageAdapter.notifyItemInserted(messageList.size());                      //recyclerView에 데이터 추가 알림
                        recyclerMessages.smoothScrollToPosition(messageList.size());                //recyclerView 맨 밑으로 스크롤
                    } else
                        return;                                                                   //receiver가 자신이 아니면 표시하지 않습니다.
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageAdapter.notifyDataSetChanged();
    }

    private void setStorage() {
        firebaseStorage = FirebaseStorage.getInstance();
        stRoot = firebaseStorage.getReference().getRoot();                                          //storage 루트 경로
        stMessage = stRoot.child("RoomList").child(roomId).child("messages");                       //storage 현재 room의 messages 경로
    }

    private void setStatus() {
        dbConnection = firebaseDatabase.getReference(".info/connected");                            //DB 연결 상태 수신
        dbConnection.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isConnected = dataSnapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dbServerOffset = firebaseDatabase.getReference(".info/serverTimeOffset");                   //서버 딜레이 보정 시간 수신
        dbServerOffset.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                serverOffset = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String sendMessage(ChatMessage message) {                                               //메시지 전송
        final DatabaseReference newKey = dbMessage.push();                                          //새 메시지 생성
        newKey.setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null)
                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT);    //에러 처리
            }
        });

        return newKey.getKey();
    }

    private void showGallery() {                                                                    //갤러리 열기
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 0);
    }

    private void sendOffline(ChatMessage message) {                                                 //오프라인 메시지 처리
        messageList.add(message);
        messageAdapter.notifyItemInserted(messageList.size());
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(                                                      //권한 확인
                ChatActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    ChatActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);                    //권한 요청
        } else
            showGallery();                                                                          //갤러리 열기
    }

    private String getImageNameToUri(Uri data) {                                                    //갤러리에서 가져온 이미지 이름 반환
        String[] proj = {MediaStore.Images.Media.DATA};

        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);

        return imgName;
    }

    //메시지 바디를 가져와 귓속말 패턴을 찾습니다. 임시로 #이름
    private String extractReceiver(String body) {
        Pattern p = Pattern.compile("\\#([0-9a-zA-Z가-힣]*)");
        Matcher m = p.matcher(body);

        while (m.find()) {
            String extracted = m.group();
            if (extracted != null) {
                extracted = extracted.replace("#","");
                return extracted;
            }
        }
        return "";
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        roomId = getIntent().getStringExtra("roomId");
        roomName = getIntent().getStringExtra("roomName");
        getSupportActionBar().setTitle(roomName);                                               //roomName으로 타이틀 설정

        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        myName = sharedPreferences.getString("name", "");
        myProfileUrl = sharedPreferences.getString("profile", "");

        linearLayoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, false);
        messageAdapter = new MessageAdapter(ChatActivity.this, messageList);

        imgPicture = (ImageView) findViewById(R.id.chat_img_picture);
        imgPicture.setOnClickListener(this);
        imgSend = (ImageView) findViewById(R.id.chat_img_send);
        imgSend.setOnClickListener(this);
        editMessage = (EditText) findViewById(R.id.chat_edit_message);
        recyclerMessages = (RecyclerView) findViewById(R.id.chat_recycler_messaages);
        recyclerMessages.setLayoutManager(linearLayoutManager);
        recyclerMessages.setAdapter(messageAdapter);

        setDatabase();
        setStorage();
        setStatus();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chat_img_picture: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    checkPermissions();                                                             //마시멜로부터 권한 체크
                else showGallery();
                break;
            }

            case R.id.chat_img_send: {
                final String text = editMessage.getText().toString().trim();

                if (!text.equals("")) {
                    long time = System.currentTimeMillis() + (long) serverOffset;                   //보정 시간을 더해 시간 측정
                    if (!isConnected)
                        time = 0;                                                     //오프라인이면 0

                    final ChatMessage message = new ChatMessage(myName, extractReceiver(text), text, "NOPHOTO", myProfileUrl, time);

                    if (isConnected) sendMessage(message);
                    else
                        sendOffline(message);                                                      //오프라인이면 실제로 보내지 않음

                    editMessage.setText("");
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GALLERY) {
            if (resultCode == RESULT_OK) {
                final Uri imageUri = data.getData();
                final String name = getImageNameToUri(data.getData());
                final String text = editMessage.getText().toString().trim();

                stMessage.child(name).putFile(imageUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {          //Storage에 사진 업로드
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {                                                  //성공
                                    final String photoUrl = task.getResult().getDownloadUrl().toString();   //업로드된 url 가져옴
                                    long time = System.currentTimeMillis() + (long) serverOffset;           //보정된 시간 측정
                                    if (!isConnected)
                                        time = 0;                                             //오프라인이면 시간을 0으로

                                    final ChatMessage message = new ChatMessage(myName, extractReceiver(text), text, photoUrl, myProfileUrl, time);

                                    if (isConnected) sendMessage(message);
                                    else
                                        sendOffline(message);                                       //오프라인이면 실제로 전송하지 않음

                                } else
                                    Toast.makeText(getApplicationContext(), "Couldn't upload image", Toast.LENGTH_SHORT);        //실패
                            }
                        });

                editMessage.setText("");
            } else
                Toast.makeText(getApplicationContext(), "Couldn't get image", Toast.LENGTH_SHORT);  //갤러리에서 이미지 가져기 실패
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showGallery();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
