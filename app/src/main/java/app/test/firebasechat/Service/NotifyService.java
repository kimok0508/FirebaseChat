package app.test.firebasechat.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.test.firebasechat.Model.ChatMessage;
import app.test.firebasechat.R;

/**
 * Created by kimok_000 on 2017-02-12.
 */

public class NotifyService extends Service {
    private static final int REQUEST_NOTIFICATION = 0;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dbRoot;
    private ArrayList<DatabaseReference> dbNotifyTargets = new ArrayList<>();

    private NotificationManager notificationManager;
    private PowerManager powerManager;
    private PowerManager.WakeLock cpuWakeLock;
    private PowerManager.WakeLock displayWakeLock;
    private Vibrator vibrator;

    private boolean isNotifyEnabled = true;
    private boolean hasVibrator = false;
    private long[] vibreatePattern = new long[]{200, 100, 200};
    private String myName = "";
    private ArrayList<String> roomKeys = new ArrayList<>();                                         //알림을 받을 룸키. 실제 서비스에서는 본인이 포함된 룸키를 가지고 있어야됨

    private ChildEventListener notifyListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {                             //새로운 메시지가 추가됨
            final ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);

            if (newMessage != null) {
                //메시지에서 값을 가져옴
                String sender = newMessage.getSender();
                String receiver = newMessage.getReceiver();
                String body = newMessage.getBody();
                String profileUrl = newMessage.getPhotoUrl();
                String photoUrl = newMessage.getPhotoUrl();

                //nullpointer 처리
                if (sender == null) sender = "";
                if (receiver == null) receiver = "";
                if (body == null) body = "";
                if (profileUrl == null) profileUrl = "";
                if (photoUrl == null) photoUrl = "";

                if (sender.equals(myName) || !receiver.equals(myName)) {                            //본인 메시지이거나 리시버가 자신이 아니면 넘김
                    return;
                } else {                                                                            //타인 메시지
                    if(sender.equals(myName)) return;
                    if (isNotifyEnabled) displayNotification(sender, receiver, body);
                    else if (isTagged(myName)) {                                                    //태그됨
                        displayNotification(sender, receiver, body);
                    }
                }
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
    };

    private void displayNotification(String sender, String receiver, String body) {
        Notification.Builder newNotify = null;                                                      //알림 관련 초기화

        if (receiver.equals("")) {                                                                  //일반 메시지
            newNotify = new Notification.Builder(NotifyService.this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("에스티커 새 메시지")
                    .setContentText(sender + " : " + body.trim());
        } else if (receiver.equals(myName)) {                                                       //귓속말
            newNotify = new Notification.Builder(NotifyService.this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("에스티커 새 귓속말")
                    .setContentText(sender + " : " + body.trim());
        }

        if (!displayWakeLock.isHeld())
            displayWakeLock.acquire(1000);                                                          //화면 켜기
        if (hasVibrator)
            vibrator.vibrate(vibreatePattern, -1);                                                  //진동
        notificationManager.notify(REQUEST_NOTIFICATION, newNotify.build());                        //알림 표시
    }

    //메시지 바디를 가져와 본인이 태그되었는지 확인합니다
    private boolean isTagged(String body) {
        Pattern p = Pattern.compile("\\@([0-9a-zA-Z가-힣]*)");
        Matcher m = p.matcher(body);

        while (m.find()) {
            String extracted = m.group();
            if (extracted == null) extracted = "";
            extracted = extracted.replace("@", "");

            if (extracted.equals(myName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;                                                                        //상주
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        roomKeys.add(new String("-KcmGue-RxoIeTz1KyV2"));                                           //알림을 표시할 방의 키를 넣어줘야됨

        //WAKE_LOCK 관련 항목 초기화
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        cpuWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPU");
        displayWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "DISPLAY");
        if (!cpuWakeLock.isHeld()) cpuWakeLock.acquire();

        //알림 및 진동 관련 항목 초기화
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        hasVibrator = vibrator.hasVibrator();

        //저장된 내이름을 가져옴
        myName = getSharedPreferences("setting", MODE_PRIVATE).getString("name", "");

        //파이어베이스 관련 항목 초기화
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRoot = firebaseDatabase.getReference().getRoot();                                         //DB 루트 경로

        for (int i = 0; i < roomKeys.size(); i++) {
            final String roomKey = roomKeys.get(i);

            if (!roomKey.equals("")) {
                final DatabaseReference dbNotifyTarget
                        = dbRoot.child("RoomList").child(roomKey).child("messages");                //변경 사항을 수신받을 경로 지정

                dbNotifyTarget.orderByChild("time").addChildEventListener(notifyListener);          //시간 순으로 변경 사항 수신
                dbNotifyTargets.add(dbNotifyTarget);                                                //경로 저장
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (DatabaseReference dbNotifyTarget : dbNotifyTargets) {                                    //모든 리시버 제거
            dbNotifyTarget.removeEventListener(notifyListener);
        }
        notificationManager.cancel(REQUEST_NOTIFICATION);                                           //알림 제거
        cpuWakeLock.release();                                                                      //WAKE_LOCK 해제
        displayWakeLock.release();
    }
}
