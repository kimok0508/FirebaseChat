package app.test.firebasechat.Model;

/**
 * Created by kimok_000 on 2017-02-12.
 */

public class ChatMessage {
    private String sender;                                                                          //발신자 이름.
    private String receiver;                                                                        //수신자 이름. ""->일반 메시지, "이름"->귓속말
    private String body;                                                                            //메시지 내용
    private String photoUrl;                                                                        //사진 URL. "NOPHOTO"->사진 없음.
    private String profileUrl;                                                                      //프로필 사진 URL.
    private long time;                                                                              //전송 시간

    public ChatMessage() {
    }

    public ChatMessage(String sender, String receiver, String body, String photoUrl, String profileUrl, long time) {
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
        this.photoUrl = photoUrl;
        this.profileUrl = profileUrl;
        this.time = time;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public String getBody() {
        return body;
    }

    public long getTime() {
        return time;
    }
}
