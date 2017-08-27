package app.test.firebasechat;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by kimok_000 on 2017-02-12.
 */

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);                                 //오프라인DB를 사용
    }
}
