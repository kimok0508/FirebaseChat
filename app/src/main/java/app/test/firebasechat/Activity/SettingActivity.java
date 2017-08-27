package app.test.firebasechat.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import app.test.firebasechat.R;

/**
 * Created by kimok_000 on 2017-02-12.
 */

                                                                                                    //테스트용 액티비티
public class SettingActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private EditText editName, editProfile;
    private Button btnEnter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editName = (EditText) findViewById(R.id.set_edit_name);
        editProfile = (EditText) findViewById(R.id.set_edit_profile);
        btnEnter = (Button) findViewById(R.id.set_btn_enter);
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = editName.getText().toString().trim();
                final String profile = editProfile.getText().toString().trim();
                final Intent intent = new Intent(SettingActivity.this, MainActivity.class);

                editor.putString("name", name);
                editor.putString("profile", profile);
                editor.commit();

                startActivity(intent);
                finish();
            }
        });
    }
}
