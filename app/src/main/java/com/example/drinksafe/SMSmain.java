package com.example.drinksafe;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SMSmain extends Activity {
    LocationManager lm;
    static String PHONE_NUMBER;
    static String MESSAGE = "I'm drunken!! http://maps.google.com/?q=";
    private int SIM_STATE;

    TextView tv;
    ToggleButton tb;
    String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        PHONE_NUMBER = i.getStringExtra("phone");
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                100,
                1,
                mLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                100,
                1,
                mLocationListener);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendSMS();
    }
    protected void onDestroy(){
        super.onDestroy();
        lm.removeUpdates(mLocationListener);
    }
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();
            float accuracy = location.getAccuracy();
            String provider = location.getProvider();
            str=latitude+","+longitude;
            MESSAGE = MESSAGE+str;
        }
        public void onProviderDisabled(String provider) {
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };

    public void sendSMS() {
        if (isSimExists()) {
            try {
                String SENT = "SMS_SENT";
                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                        new Intent(SENT), 0);
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        int resultCode = getResultCode();
                        switch (resultCode) {
                            case Activity.RESULT_OK:
                                Toast.makeText(getBaseContext(), "SMS sent",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(
                                        "com.example.drinksafe.SentSmsLogger");
                                intent.putExtra(Constants.KEY_PHONE_NUMBER,
                                        PHONE_NUMBER);
                                intent.putExtra(Constants.KEY_MESSAGE, MESSAGE);
                                intent.setPackage("com.example.drinksafe");
                                startService(intent);

                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                Toast.makeText(getBaseContext(), "Generic failure",
                                        Toast.LENGTH_LONG).show();
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                Toast.makeText(getBaseContext(), "No service",
                                        Toast.LENGTH_LONG).show();
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                Toast.makeText(getBaseContext(), "Null PDU",
                                        Toast.LENGTH_LONG).show();
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                Toast.makeText(getBaseContext(), "Radio off",
                                        Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                }, new IntentFilter(SENT));

                SmsManager smsMgr = SmsManager.getDefault();
                for(int i=0; i<PHONE_NUMBER.split(",").length;i++){
                    smsMgr.sendTextMessage(PHONE_NUMBER.split(",")[i], null, MESSAGE, sentPI,
                            null);
                }

            } catch (Exception e) {
                Toast.makeText(this,
                        e.getMessage() + "!\n" + "Failed to send SMS",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, getSimState() + " " + "Cannot send SMS",
                    Toast.LENGTH_LONG).show();
        }
    }
    private boolean isSimExists() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        SIM_STATE = telephonyManager.getSimState();

        if (SIM_STATE == TelephonyManager.SIM_STATE_READY)
            return true;
        return false;
    }
    private String getSimState() {
        switch (SIM_STATE) {
            case TelephonyManager.SIM_STATE_ABSENT:
                return "No Sim Found!";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return "Network Locked!";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return "PIN Required to access SIM!";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return "PUK Required to access SIM!";
            case TelephonyManager.SIM_STATE_UNKNOWN:
                return "Unknown SIM State!";
        }
        return null;
    }
}