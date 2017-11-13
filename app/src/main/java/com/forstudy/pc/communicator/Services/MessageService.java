package com.forstudy.pc.communicator.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.forstudy.pc.communicator.Activities.LoginActivity;
import com.forstudy.pc.communicator.DAO.MessagesDAO;
import com.forstudy.pc.communicator.Models.Employees;
import com.forstudy.pc.communicator.Models.MessagesForLocalDB;
import com.forstudy.pc.communicator.Models.MessagesFromWebService;
import com.forstudy.pc.communicator.Models.MessagesToWebService;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionKeyPair;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionPrivateKey;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionPublicKey;
import com.forstudy.pc.communicator.Utilities.DepartmentsResponseList;
import com.forstudy.pc.communicator.Utilities.EmployeesResponseList;
import com.forstudy.pc.communicator.Utilities.MessagesResponseList;
import com.forstudy.pc.communicator.Utilities.RequestInterceptor;
import com.forstudy.pc.communicator.Utilities.RestTemplateHeaders;
import com.forstudy.pc.communicator.crypt.Encryption;
import com.forstudy.pc.communicator.crypt.KeysGenerator;
import com.forstudy.pc.communicator.crypt.Signing;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by pc on 27.02.17.
 */


public class MessageService extends Service {

    private final static String SERVER_ADDRESS = "http://10.0.2.2:8080/communicator_web_service/";

    private SharedPreferences prefs;
    private Editor prefsEditor;
    private Context cont;
    private Employees employee;
    private ServiceCallbacks serviceCallbacks;

    private final IBinder Binder = new LocalBinder();
    public static final String INTENT_FILTER_UPDATE_MESSAGE = "UPDATE_MESSAGE";

    private static String FCM_TOKEN;

    public static final int STANDARD_TASK = 0;
    public static final int NON_STANDARD_TASK = 1;
    public static final int AUTHORIZATION = 2;
    public static final int SET_BASIC_INFO = 3;
    public static final int GET_SIGN_KEY = 4;
    public static final int GET_MESSAGES = 5;
    public static final int GET_PRIVATE_KEY = 6;
    public static final int GET_EMPLOYEE_FOR_SEND = 7;

    @Override
    public void onCreate() {
        registerReceiver(FCMServiceReceiver, new IntentFilter(MyFirebaseMessagingService.INTENT_FILTER));
        registerReceiver(FCMIDReceiver, new IntentFilter(MyFirebaseInstanceIDService.INTENT_FILTER));
        cont = getApplicationContext();
        prefs = cont.getSharedPreferences("preferences", cont.MODE_PRIVATE);
        prefsEditor = prefs.edit();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(FCMServiceReceiver);
        unregisterReceiver(FCMIDReceiver);
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    public class LocalBinder extends Binder {
        public MessageService getService() {
            return MessageService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return Binder;
    }

    private BroadcastReceiver FCMServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if( prefs.getString("authorization_token", null) != null && prefs.getInt("id", 0) != 0) {
                        getSentMessages();
                }
            }
            catch(RuntimeException e) {
                Log.d("FCMBrodcastMessage: ", "Fail");
            }
        }
    };

    private BroadcastReceiver FCMIDReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if( prefs.getString("authorization_token", null) != null && prefs.getInt("id", 0) != 0) {
                    Object[] temp = new Object[2];
                    temp[0] = prefs.getInt("id", 0);
                    temp[1] = intent.getStringExtra("FCM_Token");
                    HttpEntity<Object[]> token = new HttpEntity<>(temp);
                    new HttpRequestTask<>(SERVER_ADDRESS + "fcm_update", HttpMethod.PUT, token, String.class, NON_STANDARD_TASK).execute();
                }
                else {
                    FCM_TOKEN = intent.getStringExtra("FCM_Token");
                }
            }
            catch(RuntimeException e) {
                Log.d("FCMBrodcastID: ", "IDRefreshFail\n" + e.getStackTrace());
            }
        }
    };

    private RestTemplate getRestTemplate(List<RequestInterceptor> headers) {
        return new RestTemplateHeaders().getRestWithHeaders(new RestTemplate(), headers);
    }

    private List<RequestInterceptor> getDefaultHeadersToRestTemplate() {
        List<RequestInterceptor> headers = new ArrayList<>();
        headers.add(new RequestInterceptor("Authorization", prefs.getString("authorization_token", null)));
        headers.add(new RequestInterceptor("Content-Type", "application/json"));
        return headers;
    }

    public <T, S> void login(HttpEntity<T> requestEntity, Class<S> responseClass) {
        new HttpRequestTask<>(SERVER_ADDRESS + "login", HttpMethod.POST, requestEntity, responseClass, null, AUTHORIZATION).execute();
    }

    public void setBasicInfo(String login) {
        new HttpRequestTask<>(SERVER_ADDRESS + "get_employee_by_login" + "?login=" + login, HttpMethod.GET, null, Employees.class, SET_BASIC_INFO).execute();
    }

    public void getSigningKey(int id) {
        new HttpRequestTask<>(SERVER_ADDRESS + "get_sing_key" + "?id=" + id, HttpMethod.GET, null, String.class, GET_SIGN_KEY).execute();
    }

    public <T, S> void updateRecivedMessage(HttpEntity<T> requestEntity, Class<S> responseClass) {
        new HttpRequestTask<>(SERVER_ADDRESS + "message_update", HttpMethod.PUT, requestEntity, responseClass, NON_STANDARD_TASK).execute();
    }

    public void getSentMessages() {
        new HttpRequestTask<>(SERVER_ADDRESS + "get_messages_recieved" + "?id=" + prefs.getInt("id", 0), HttpMethod.GET, null, MessagesResponseList.class, GET_MESSAGES).execute();
    }

    public void getDepartments() {
        new HttpRequestTask<>(SERVER_ADDRESS + "get_departments" + "?id=" + prefs.getInt("id", 0), HttpMethod.GET, null, DepartmentsResponseList.class, STANDARD_TASK).execute();
    }

    public void getDepartmentContacts(int id_dept) {
        new HttpRequestTask<>(SERVER_ADDRESS + "get_departments_contacts" + "?id=" + id_dept, HttpMethod.GET, null, EmployeesResponseList.class, STANDARD_TASK).execute();
    }

    public void getPrivateKey() {
        new HttpRequestTask<>(SERVER_ADDRESS + "get_private_key" + "?id=" + prefs.getInt("id", 0), HttpMethod.GET, null, String.class, GET_PRIVATE_KEY).execute();
    }

    public void sendMessage(String message, int id_receiver, Employees employee) {
        if(employee == null) {
            getReceiverAndPassMessage(id_receiver, message);
        }
        else {
            MessagesToWebService messageToSent = new MessagesToWebService();
            MessagesForLocalDB messageToSave = new MessagesForLocalDB();
            long timeSent = System.currentTimeMillis();

            SecureRandom rnd = new SecureRandom();
            byte[] ivGen = new byte[16];
            rnd.nextBytes(ivGen);
            IvParameterSpec iv = new IvParameterSpec(ivGen);
            SecretKey secretKey = KeysGenerator.generateAES();
            byte[] encryptedMessageByteArray = Encryption.encryptMessage(message, secretKey, iv);
            String signature = Signing.sign(encryptedMessageByteArray);
            String encryptedMessage = Base64.encodeToString(encryptedMessageByteArray, Base64.DEFAULT);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(secretKey.getEncoded());
                outputStream.write(iv.getIV());
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte keyAndIV[] = outputStream.toByteArray();
            String encryptKeyAndIV = Encryption.encryptAes(keyAndIV, employee.getPublicKey());
            String encryptKeyAndIVLocal = Encryption.encryptAes(keyAndIV, prefs.getString("publicNTRUKey", null));
            
            messageToSave.setMessage(encryptedMessage);
            messageToSave.setSecondName(prefs.getString("secondNameLocal", null));
            messageToSave.setReceived(System.currentTimeMillis());
            messageToSave.setSent(timeSent);
            messageToSave.setEncryptionKey(encryptKeyAndIVLocal);
            messageToSave.setFirstName(prefs.getString("firstNameLocal", null));
            messageToSave.setSender(prefs.getString("login", null));
            messageToSave.setSenderId(prefs.getInt("id", 0));
            messageToSave.setSignature(signature);
            messageToSave.setReceiver(employee.getId());
            
            MessagesDAO mDAO = new MessagesDAO(getApplicationContext());
            long err = mDAO.addMessage(messageToSave);
            if (err != -1) {
                Intent intent = new Intent(INTENT_FILTER_UPDATE_MESSAGE);
                sendBroadcast(intent);
            }

            messageToSent.setSent(timeSent);
            messageToSent.setReceived(0);
            messageToSent.setEncryptionKey(encryptKeyAndIV);
            messageToSent.setReceiver(employee.getId());
            messageToSent.setSender(prefs.getInt("id", 0));
            messageToSent.setMessage(encryptedMessage);
            messageToSent.setSignature(signature);
            
            HttpEntity<MessagesToWebService> requestEntity = new HttpEntity<>(messageToSent);
            
            if (err != -1) {
                new HttpRequestTask<>(SERVER_ADDRESS + "message_sent", HttpMethod.POST, requestEntity, String.class, NON_STANDARD_TASK).execute();
            }
        }
    }

    private void getReceiverAndPassMessage(int id_receiver, String message) {
        new HttpRequestTask<>(SERVER_ADDRESS + "get_employee" + "?id=" + id_receiver, HttpMethod.GET, null, Employees.class, GET_EMPLOYEE_FOR_SEND, message).execute();
    }


    private class HttpRequestTask<T, S> extends AsyncTask<Void, Void, ResponseEntity<S>> {
        private String url;
        private HttpEntity<T> requestEntity;
        private Class<S> responseClass;
        private HttpMethod httpMethod;
        private RestTemplate restTemplate;
        private ResponseEntity<S> response;
        private int whatTask;
        private String additionalInfo;

        private Context cont = getApplicationContext();


        public HttpRequestTask(String url, HttpMethod httpMethod, HttpEntity<T> requestEntity, Class<S> responseClass, List<RequestInterceptor> headers, int whatTask) {
            this.url = url;
            this.httpMethod = httpMethod;
            this.requestEntity = requestEntity;
            this.responseClass = responseClass;
            this.restTemplate = getRestTemplate(headers);
            this.whatTask = whatTask;
        }

        public HttpRequestTask(String url, HttpMethod httpMethod, HttpEntity<T> requestEntity, Class<S> responseClass, List<RequestInterceptor> headers, int whatTask, String additionalInfo) {
            this(url, httpMethod, requestEntity, responseClass, headers, whatTask);
            this.additionalInfo = additionalInfo;
        }

        public HttpRequestTask(String url, HttpMethod httpMethod, HttpEntity<T> requestEntity, Class<S> responseClass, int whatTask) {
            this.url = url;
            this.httpMethod = httpMethod;
            this.requestEntity = requestEntity;
            this.responseClass = responseClass;
            this.restTemplate = getRestTemplate(getDefaultHeadersToRestTemplate());
            this.whatTask = whatTask;
        }

        public HttpRequestTask(String url, HttpMethod httpMethod, HttpEntity<T> requestEntity, Class<S> responseClass, int whatTask, String additionalInfo) {
            this(url, httpMethod, requestEntity, responseClass, whatTask);
            this.additionalInfo = additionalInfo;
        }

        @Override
        protected ResponseEntity<S> doInBackground(Void... params) {
            try {
                response = restTemplate.exchange(url, httpMethod, requestEntity, responseClass);
                return response;
            } catch (Exception e) {
                Log.d("MainActivity", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseEntity<S> response) {
            try {
                if (whatTask == STANDARD_TASK) {
                    pingAllActivitiesAboutResponse(response);
                }

                if(whatTask == GET_PRIVATE_KEY) {
                    pingAllActivitiesAboutResponse(response);
                }

                if(whatTask == GET_SIGN_KEY) {
                    pingAllActivitiesAboutResponse(response);
                }

                if (whatTask == AUTHORIZATION) {
                    authorizeUser(response);
                }

                if (GET_MESSAGES == whatTask) {
                    receiveMessages(response);
                }

                if (whatTask == GET_EMPLOYEE_FOR_SEND) {
                    sendMessage(additionalInfo, 0, (Employees) response.getBody());
                }

                if (whatTask == SET_BASIC_INFO) {
                    setBasicInfoAboutUser(response);
                }
            }
            catch(RuntimeException e) {
                e.printStackTrace();
                if(response != null) {
                    if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
                        prefsEditor.putString("authorization_token", null);
                        prefsEditor.commit();
                        Toast.makeText(getApplicationContext(), "Try login again.", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                    if(response.getStatusCode() == HttpStatus.NOT_FOUND || response.getStatusCode() == HttpStatus.REQUEST_TIMEOUT) {
                        Toast.makeText(getApplicationContext(), "Problem with provider, please try later or conntact administrator.", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    boolean isInternetConnected = true;
                    try {
                        InetAddress.getByName("google.com");
                    } catch (Exception err) {
                        isInternetConnected = false;
                    }
                    if (!isInternetConnected) {
                        Toast.makeText(getApplicationContext(), "Internet connection failed!", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Unknown error!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        private void pingAllActivitiesAboutResponse(ResponseEntity<S> response) {
            if (serviceCallbacks != null) {
                serviceCallbacks.taskResponseCall(response, whatTask);
            }
        }

        private void authorizeUser(ResponseEntity<S> response) {
            if(response.getHeaders() != null) {
                prefsEditor.putString("authorization_token", response.getHeaders().getFirst("Authorization"));
                prefsEditor.commit();
                    pingAllActivitiesAboutResponse(response);
            }
            else {
                pingAllActivitiesAboutResponse(null);

            }
        }

        private void setBasicInfoAboutUser(ResponseEntity<S> response) {
            employee = (Employees) response.getBody();
            if (FCM_TOKEN != null) {
                updateFCMTokenInRemoteDB();
            }

            pingAllActivitiesAboutResponse(response);
            PublicKey publicSigningKey = KeysGenerator.generateRSAForSigning();
            EncryptionKeyPair NTRUKeys = KeysGenerator.generateNTRUKeys();
            saveUserInfoInPreferences(NTRUKeys.getPublic(), publicSigningKey);
            KeysGenerator.generateAESForPrivateKeyEncryption();
            sendEncryptionKeysToRemoteDB(NTRUKeys.getPrivate());

        }

        private void updateFCMTokenInRemoteDB() {
            Object[] temp = new Object[2];
            temp[0] = employee.getId();
            temp[1] = FCM_TOKEN;
            HttpEntity<Object[]> token = new HttpEntity<>(temp);
            new HttpRequestTask<>(SERVER_ADDRESS + "fcm_update", HttpMethod.PUT, token, String.class, NON_STANDARD_TASK).execute();
        }

        private void saveUserInfoInPreferences(EncryptionPublicKey publicNTRUKey, PublicKey publicSigningKey) {
            prefsEditor.putInt("id", employee.getId());
            prefsEditor.putString("firstNameLocal", employee.getFirstName());
            prefsEditor.putString("secondNameLocal", employee.getSecondName());
            prefsEditor.putString("localPublicSigningKey", Base64.encodeToString( publicSigningKey.getEncoded(), Base64.DEFAULT));
            prefsEditor.putString("publicNTRUKey", Base64.encodeToString(publicNTRUKey.getEncoded(), Base64.DEFAULT));
            prefsEditor.commit();
        }

        private void sendEncryptionKeysToRemoteDB(EncryptionPrivateKey NTRUPrivateKey) {
            Object[] forPut = new Object[4];
            forPut[0] = prefs.getInt("id", 0);
            forPut[1] = prefs.getString("publicNTRUKey", null);
            forPut[2] = prefs.getString("localPublicSigningKey", null);
            forPut[3] = Encryption.encryptNTRUPrivateKey(NTRUPrivateKey.getEncoded(), cont);
            HttpEntity<Object[]> publicKeyForPut = new HttpEntity<>(forPut);
            new HttpRequestTask<>(SERVER_ADDRESS + "update_keys", HttpMethod.PUT, publicKeyForPut, String.class, NON_STANDARD_TASK).execute();
        }
        private void receiveMessages(ResponseEntity<S> response) {
            MessagesResponseList messages = (MessagesResponseList) response.getBody();
            for(MessagesFromWebService message : messages) {
                if (saveMessageToLocalDatabase(message) != -1) {
                    updateReceivedMessageInRemoteDB(message);
                    sendBroadcastToActivitiesAboutReceivedMessage();
                }
            }
        }

        private long saveMessageToLocalDatabase(MessagesFromWebService message) {
            MessagesDAO messagesDAO = new MessagesDAO(getApplicationContext());
            return messagesDAO.addMessage(message);
        }

        private void updateReceivedMessageInRemoteDB(MessagesFromWebService message) {
            MessagesToWebService temp = new MessagesToWebService();
            temp.setId(message.getId());
            temp.setReceived(System.currentTimeMillis());
            HttpEntity<MessagesToWebService> requestEntity = new HttpEntity<>(temp);
            updateRecivedMessage(requestEntity, null);
        }

        private void sendBroadcastToActivitiesAboutReceivedMessage() {
            sendBroadcast(new Intent(INTENT_FILTER_UPDATE_MESSAGE));
        }
    }
}
