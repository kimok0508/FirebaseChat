package app.test.firebasechat.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import app.test.firebasechat.Adapter.RoomAdapter;
import app.test.firebasechat.Model.ChatRoom;
import app.test.firebasechat.R;
import app.test.firebasechat.Service.NotifyService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dbRoot;
    private DatabaseReference dbRoomList;

    private RoomAdapter roomAdapter;
    private ListView listRooms;
    private EditText editName, editDesc;
    private Button btnMake;

    private ArrayList<Pair<String, ChatRoom>> roomList = new ArrayList<>();

    private void refreshList() {
        roomAdapter = new RoomAdapter(MainActivity.this, roomList);
        listRooms.setAdapter(roomAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //알림 서비스 실행
        final Intent notifyIntent = new Intent(MainActivity.this, NotifyService.class);
        startService(notifyIntent);

        editName = (EditText) findViewById(R.id.main_edit_name);
        editDesc = (EditText) findViewById(R.id.main_edit_desc);
        btnMake = (Button) findViewById(R.id.main_btn_make);
        btnMake.setOnClickListener(this);
        listRooms = (ListView) findViewById(R.id.main_list_rooms);
        listRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {                //room 클릭하면 ChatActivity로 넘어감
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                final String key = roomList.get(i).first;
                final String roomName = roomList.get(i).second.getName();
                intent.putExtra("roomId", key);                                                     //roomKey
                intent.putExtra("roomName", roomName);                                              //roomName

                startActivity(intent);
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRoot = firebaseDatabase.getReference().getRoot();                                         //DB 루트 경로
        dbRoomList = dbRoot.child("RoomList");                                                      //DB RoomList 경로
        dbRoomList.addChildEventListener(new ChildEventListener() {                                 //RoomList 변경사항 수신
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {                         //room이 추가되었을 때
                final ChatRoom room = dataSnapshot.getValue(ChatRoom.class);
                final String key = dataSnapshot.getKey();                                           //roomId 추출

                roomList.add(new Pair<>(key, room));
                refreshList();
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

        refreshList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_btn_make: {
                final String name = editName.getText().toString().trim();
                final String desc = editDesc.getText().toString().trim();

                if (name.equals("") || desc.equals("")) {                                           //빈 칸이 있으면 넘김
                    Toast.makeText(getApplicationContext(), "빈 칸 없이 채워주세요", Toast.LENGTH_SHORT);
                    return;
                }

                //프로그래스 다이얼로그 띄움
                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("채팅방을 생성중입니다...");
                progressDialog.show();

                final ChatRoom newRoom = new ChatRoom(name, desc);
                dbRoomList.push().setValue(newRoom, new DatabaseReference.CompletionListener() {    //새로운 room 생성
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        progressDialog.dismiss();

                        if (databaseError != null)                                                  //에러 처리
                            Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT);
                        else
                            refreshList();                                                          //에러 없으면 새로 고침
                    }
                });

                break;
            }
        }
    }
}
