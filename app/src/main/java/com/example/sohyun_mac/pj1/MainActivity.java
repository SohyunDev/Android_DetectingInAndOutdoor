package com.example.sohyun_mac.pj1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    Location loc; // 최종적으로 확정된 위치정보가 저장 될 객체


    private boolean isGpsReceived;
    private boolean isNetReceived;

    private String gpsProvider;
    private long current=0;
    private long enteringTime=0;
    private long startTime=0;
    private int gpsCount = 0;
    private int netCount = 0;
    private double speed = 0.0;



    EditText editText;
    TextView indoorText;
    Button enteringButton;

    public static int RENEW_GPS = 1;
    public static int SEND_PRINT = 2;
    public Handler mHandler;

    private ArrayList<String> save = new ArrayList<>(2);
    private boolean isIndoor = true;

    LocationListener gpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            isGpsReceived = true;  // gps 위치정보가 수신되면 플래그를 set
            updateWithNewLocation(location, "gps");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    LocationListener netListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            isGpsReceived = true;  // gps 위치정보가 수신되면 플래그를 set
            updateWithNewLocation(location, "network");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isGpsReceived = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        startTime = System.currentTimeMillis();
        editText = (EditText) findViewById(R.id.editText);
        indoorText = (TextView) findViewById(R.id.isIndoor);
        enteringButton = (Button) findViewById(R.id.enter);

        enteringButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0){
                current = System.currentTimeMillis();
                enteringTime = 0;
            }
        });


        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }



        showLocationMethod();
    }

    public void showLocationMethod(){
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what==RENEW_GPS){
                    makeNewGpsService();
                    if(isIndoor == false){
                        indoorText.setText("실외입니다" + " 시작시간으로부터 걸린시간 : "+(current-startTime)/1000+" 걸린시간 : " + (enteringTime - current) / 1000+ "network 수신횟수 : "+netCount);
                    }
                    else{
                        indoorText.setText("실내입니다" + " 시작시간으로부터 걸린시간 : "+(current-startTime)/1000+" 걸린시간 : "+(enteringTime- current)/1000 + "GPS 수신횟수 : "+gpsCount);
                    }
                }
                if(msg.what==SEND_PRINT){
                    logPrint((String)msg.obj);
                }
            }
        };
        Runnable run = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException e){

                    }
                    mHandler.sendEmptyMessage(1);
                }
            }
        };
        Thread thread = new Thread(run);
        thread.start();
    }
    public void makeNewGpsService(){
        onResume();
    }



    public void onResume() {
        super.onResume();

        isGpsReceived = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsReceived) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if(speed == 0) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, gpsListener);
            }
            else{
                int newUpdateTime = (int)(10/speed)*1000;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, newUpdateTime, 10, gpsListener);
            }
        }

        isNetReceived = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetReceived) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            if(speed == 0) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, netListener);
            }
            else{
                int newUpdateTime = (int)(10/speed)*1000;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, newUpdateTime, 10, netListener);
            }
        }
    }

    public void updateWithNewLocation(Location location, String provider) {
        // 여기에서 처리를 해준다.
        // provider 값으로 location이 어떤 provider에서 들어왔는지 알 수 있다.

        if (isGpsReceived || isNetReceived) { // gps 수신여부 체크
            if (LocationManager.GPS_PROVIDER.equals(provider)){
                gpsCount++;
                loc = location; // gps 위치정보
                speed = loc.getSpeed();
                logPrint("provider " + loc.getProvider()  +" Accuracy : " + loc.getAccuracy() + " Speed : " + loc.getSpeed() + " LastTime : " +loc.getTime() + " " + save.toString());
                if(save.size()<2) {
                    save.add(provider);
                }
                else{
                    save.remove(0);
                    save.add(provider);
                }
                if(save.contains("gps")){
                    if(isIndoor == true){
                        enteringTime = loc.getTime();
                    }
                    isIndoor = false;
                }
            }
            else if(LocationManager.NETWORK_PROVIDER.equals(provider)){
                netCount++;
                loc = location;
                speed = loc.getSpeed();
                logPrint("provider " + loc.getProvider()+" Accuracy : " + loc.getAccuracy() + " Speed : " + loc.getSpeed()+ " LastTime : " +loc.getTime()+ " " + save.toString());

                if(save.size()<2) {
                    save.add(provider);
                }
                else{
                    save.remove(0);
                    save.add(provider);
                    if(!save.contains("gps")){
                        if(isIndoor==false){
                            enteringTime = loc.getTime();
                        }
                        isIndoor = true;

                    }
                }

            }
            else{
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                long gpsGenTime = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getTime(); // 마지막으로 수신된 GPS 위치정보
                long netGenTime = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getTime();
                long time;
                boolean isGPS = false;
                if(gpsGenTime<netGenTime){
                    time = netGenTime;
                    isGPS = false;
                }
                else{
                    time = gpsGenTime;
                    isGPS = true;
                }

                long curTime = System.currentTimeMillis(); // 현재 시간
                if ((curTime - time) > 1000) { // gps 정보가 20초 이상 오래된 정보이면 네트워크 위치정보
                    if(isGPS) {
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    else{
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    logPrint("Previous : provider " + loc.getProvider() +" Accuracy : " + loc.getAccuracy() + " Speed : " + loc.getSpeed()+ " LastTime : " +loc.getTime());
                    isGpsReceived = false;
                    isNetReceived = false; // 플래그를 해제
                }
            }

        }
        else {
            logPrint("GPS 안되는데?");
        }
    }

    public void logPrint(String str){
        editText.append(getTimeStr()+" "+str+"\n");
    }

    public String getTimeStr(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("MM/dd HH:mm:ss");
        return sdfNow.format(date);
    }
}
