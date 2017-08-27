package app.test.firebasechat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import app.test.firebasechat.Service.NotifyService;

/**
 * Created by kimok_000 on 2017-02-13.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {                                   //부팅시 알림 서비스 자동 실행
            final Intent notifyIntent = new Intent(context, NotifyService.class);
            context.startService(notifyIntent);
        }
    }
}
