package com.forstudy.pc.communicator.Activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.forstudy.pc.communicator.DAO.ContactsDAO;
import com.forstudy.pc.communicator.DAO.MessagesDAO;
import com.forstudy.pc.communicator.Models.AuthorizationModel;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionParameters;
import com.forstudy.pc.communicator.Services.MessageService;
import com.forstudy.pc.communicator.Services.MyFirebaseMessagingService;
import com.forstudy.pc.communicator.R;
import com.forstudy.pc.communicator.Services.ServiceCallbacks;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class LoginActivity extends Activity implements ServiceCallbacks {

    private Button loginButton;
    private Button destroyAllDataButton;
    private EditText username;
    private EditText password;
    private Intent intent;
    private MessageService boundService;

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private ServiceConnection ServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            boundService = ((MessageService.LocalBinder) service).getService();
            boundService.setCallbacks(LoginActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            boundService = null;
        }
    };

    private boolean serviceIsBound;

    void doBindService() {
        bindService(new Intent(this, MessageService.class), ServiceConnection, BIND_AUTO_CREATE);
        bindService(new Intent(this, MyFirebaseMessagingService.class), ServiceConnection, BIND_AUTO_CREATE);
        serviceIsBound = true;
    }

    void doUnbindService() {
        if (serviceIsBound) {
            boundService.setCallbacks(null);
            unbindService(ServiceConnection);
            serviceIsBound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!isMyServiceRunning(MessageService.class)) {
            Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
            startService(serviceIntent);
        }
        doBindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceIsBound && isMyServiceRunning(MessageService.class)) {
            doUnbindService();
        }
    }


    @Override
    public void onDestroy() {
        if(isMyServiceRunning(MessageService.class)) {
            doUnbindService();
            stopService(new Intent(getApplicationContext(), MessageService.class));
        }
        MyFirebaseMessagingService.NOTIFICATION_ENABLE = false;
        //logout();

        super.onDestroy();
    }

    private Editor prefsEditor;
    private SharedPreferences prefs;
    private boolean knownLogin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        intent = new Intent(getApplicationContext(), NewMessagesActivity.class);
        Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        Context cont = getApplicationContext();
        prefs = cont.getSharedPreferences("preferences", cont.MODE_PRIVATE);
        prefsEditor = prefs.edit();

        startService(serviceIntent);
        doBindService();

        loginButton = (Button) findViewById(R.id.loginButton);
        username = (EditText) findViewById(R.id.loginUsername);
        password = (EditText) findViewById(R.id.loginPassword);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //test
                username.setText("login1");
                password.setText("haslo");
                //test
                try {
                    if(prefs.getString("login", null).equals(username.getText())) {
                        knownLogin = true;
                    }
                }
                catch(NullPointerException e) {
                    Log.d("LoginActivity: ", "Not important NPE");

                }
                AuthorizationModel authorizationCredentials = new AuthorizationModel();
                authorizationCredentials.setUsername(username.getText().toString());
                authorizationCredentials.setPassword(password.getText().toString());
                HttpEntity<AuthorizationModel> requestEntity = new HttpEntity<>(authorizationCredentials);

                boundService.login(requestEntity, String.class);
            }
        });


        destroyAllDataButton = (Button) findViewById(R.id.destroyAllDataButton);
        destroyAllDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Delete all data")
                        .setMessage("If you do this, all your messages will be irrecoverable! Are you sure?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                prefsEditor.putString("login", null);
                                prefsEditor.putString("publicNTRUKey", null);
                                prefsEditor.putString("authorization_token", null);
                                prefsEditor.putInt("id", 0);
                                prefsEditor.putString("ivForPrivateKeyNTRUEncryption", null);
                                prefsEditor.commit();
                                MessagesDAO mDAO = new MessagesDAO(getApplicationContext());
                                ContactsDAO cDAO = new ContactsDAO(getApplicationContext());
                                mDAO.removeAllData();
                                cDAO.removeAllData();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
    }

    @Override
    public void taskResponseCall(ResponseEntity<?> response, int whatTask) {
        if (response == null) {
            Toast.makeText(LoginActivity.this, "Login failed! Try again or call service administrator.", Toast.LENGTH_LONG).show();
        }
        else {
            if (whatTask == MessageService.AUTHORIZATION && response.getHeaders().getFirst("State").equals("true")) {
                if(!knownLogin) {
                    boundService.setBasicInfo(username.getText().toString());
                }
                else {
                    intent = new Intent(getApplicationContext(), NewMessagesActivity.class);
                    startActivity(intent);
                }
            }
            else if(whatTask == MessageService.AUTHORIZATION){
                Toast.makeText(LoginActivity.this, "Wrong credentials!", Toast.LENGTH_LONG).show();
            }
            if(whatTask == MessageService.SET_BASIC_INFO) {
                prefsEditor.putString("login", username.getText().toString());
                prefsEditor.commit();
                intent = new Intent(getApplicationContext(), NewMessagesActivity.class);
                startActivity(intent);
            }
        }
    }
}
