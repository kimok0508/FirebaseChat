package app.test.firebasechat.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import app.test.firebasechat.Model.ChatMessage;
import app.test.firebasechat.R;

/**
 * Created by kimok_000 on 2017-02-12.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private SharedPreferences sharedPreferences;
    private Context context;

    private ArrayList<ChatMessage> arrayList;
    private String myName = "";                                                                     //내이름

    public MessageAdapter(Context context, ArrayList<ChatMessage> arrayList) {
        this.context = context;
        this.arrayList = arrayList;

        //저장된 내이름 가져옴.
        sharedPreferences = context.getSharedPreferences("setting", context.MODE_PRIVATE);
        myName = sharedPreferences.getString("name", "");
    }

    private String localizeTime(long time) {                                                        //long형식으로된 시간을 '오후 08:00'식으로 변환
        if (time == 0)
            return "?";                                                                  //시간이 0이면 ?표시 -> 실제 전송된 메시지가 아님

        SimpleDateFormat dateFormat = new SimpleDateFormat("a hh:mm");
        return dateFormat.format(time);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = parent.inflate(context, R.layout.layout_message, null);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));         //가로가 WRAP_CONTENT로 설정되지 않도록 함

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //해당 위치의 데이터 가져옴.
        final ChatMessage message = arrayList.get(position);
        String sender = message.getSender();
        String receiver = message.getReceiver();
        String body = message.getBody();
        String time = localizeTime(message.getTime());                                              //long으로된 시간을 String으로 바꿔줌.
        String photoUrl = message.getPhotoUrl();
        String profileUrl = message.getProfileUrl();

        //nullpointer 처리
        if (body == null) body = "";
        if (sender == null) sender = "";
        if (receiver == null) receiver = "";
        if (time == null) time = "";
        if (photoUrl == null) photoUrl = "";
        if (profileUrl == null) profileUrl = "";

        if (sender.equals(myName)) {                                                                //본인이 보낸 메시지
            holder.linearMe.setVisibility(View.VISIBLE);                                            //우측 레이아웃 보이게
            holder.linearOther.setVisibility(View.GONE);

            if (!photoUrl.equals("NOPHOTO")) {                                                      //사진이 있는 경우
                holder.imgPictureMe.setVisibility(View.VISIBLE);
                Glide.with(context).load(photoUrl).centerCrop().
                        thumbnail(0.5f).crossFade().
                        override(512, 512).into(holder.imgPictureMe);

                if (body.equals("")) {                                                              //바디가 비어있으면 바디 표시 안함
                    holder.textBodyMe.setVisibility(View.GONE);
                } else holder.textBodyMe.setVisibility(View.VISIBLE);
            } else holder.imgPictureMe.setVisibility(View.GONE);

            holder.textTimeMe.setText(time);
            holder.textBodyMe.setText(body);

            //receiver가 지정되어 있다면 귓속말 컬러로 설정
            if (receiver.equals(""))
                holder.textBodyMe.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            else
                holder.textBodyMe.setBackgroundColor(context.getResources().getColor(R.color.colorMessageReceiver));
        } else {
            holder.linearMe.setVisibility(View.GONE);                                               //좌측 레이아웃 보이게
            holder.linearOther.setVisibility(View.VISIBLE);

            if (!photoUrl.equals("NOPHOTO")) {
                holder.imgPictureOther.setVisibility(View.VISIBLE);
                Glide.with(context).load(photoUrl).centerCrop().
                        thumbnail(0.5f).crossFade().
                        override(512, 512).into(holder.imgPictureOther);

                if (body.equals("")) {
                    holder.textBodyOther.setVisibility(View.GONE);
                } else holder.textBodyOther.setVisibility(View.VISIBLE);
            } else holder.imgPictureOther.setVisibility(View.GONE);

            if (profileUrl.equals(""))                                                          //프로필 없으면 기본 이미지
                holder.imgProfile.setImageResource(R.drawable.ic_profile);
            else Glide.with(context).load(profileUrl).centerCrop()
                    .thumbnail(0.25f).crossFade().into(holder.imgProfile);


            holder.textSenderOther.setText(sender);
            holder.textBodyOther.setText(body);
            holder.textTimeOther.setText(time);

            //receiver가 지정되어 있다면 귓속말 컬러로 설정
            if (receiver.equals(""))
                holder.textBodyOther.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            else
                holder.textBodyOther.setBackgroundColor(context.getResources().getColor(R.color.colorMessageReceiver));
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile;
        private LinearLayout linearOther;
        private LinearLayout linearMe;
        private TextView textSenderOther;
        private TextView textBodyOther;
        private TextView textTimeOther;
        private ImageView imgPictureOther;
        private TextView textBodyMe;
        private TextView textTimeMe;
        private ImageView imgPictureMe;

        public ViewHolder(View itemView) {
            super(itemView);

            imgProfile = (ImageView) itemView.findViewById(R.id.message_img_profile);
            linearOther = (LinearLayout) itemView.findViewById(R.id.message_linear_other);
            linearMe = (LinearLayout) itemView.findViewById(R.id.message_linear_me);
            textSenderOther = (TextView) itemView.findViewById(R.id.message_text_sender_other);
            textBodyOther = (TextView) itemView.findViewById(R.id.message_text_body_other);
            textTimeOther = (TextView) itemView.findViewById(R.id.message_text_time_other);
            imgPictureOther = (ImageView) itemView.findViewById(R.id.message_img_picture_other);
            textBodyMe = (TextView) itemView.findViewById(R.id.message_text_body_me);
            textTimeMe = (TextView) itemView.findViewById(R.id.message_text_time_me);
            imgPictureMe = (ImageView) itemView.findViewById(R.id.message_img_picture_me);
        }
    }
}
