package app.test.firebasechat.Model;

/**
 * Created by kimok_000 on 2017-02-12.
 */

public class ChatRoom {
    private String name;                                                                            //채팅방 이름
    private String desc;                                                                            //채팅방 설명

    public ChatRoom() {

    }

    public ChatRoom(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
