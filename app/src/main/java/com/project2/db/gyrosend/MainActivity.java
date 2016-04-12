package com.project2.db.gyrosend;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends Activity implements LocationListener, SensorEventListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SERVERPORT = 5000;
    private static final String SERVER_IP = "192.168.0.102";
    boolean isClicked =false;
    boolean running = false;
    //the Sensor Manager
    DataOutputStream dataOutputStream = null;
    DataInputStream dataInputStream = null;
    private Button p1_button;
    private TextView tv;
    private TextView tv_gps;
    private SensorManager sManager;
    private LocationManager locationManager;
    private int senx,seny,senz;
    private int senx0,senx1,senx2;
    private int seny0,seny1,seny2;
    private int senz0,senz1,senz2;
    private float lat,lon;
    //client socket
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new ClientThread()).start();




        //get a hook to the sensor service
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        /********** get Gps location service LocationManager object ***********/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                /* CAL METHOD requestLocationUpdates */

        // Parameters :
        //   First(provider)    :  the name of the provider with which to register
        //   Second(minTime)    :  the minimum time interval for notifications,
        //                         in milliseconds. This field is only used as a hint
        //                         to conserve power, and actual time between location
        //                         updates may be greater or lesser than this value.
        //   Third(minDistance) :  the minimum distance interval for notifications, in meters
        //   Fourth(listener)   :  a {#link LocationListener} whose onLocationChanged(Location)
        //                         method will be called for each location update


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
        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0.2f, this);


    }

    protected void onButtonCalibClick(View v) {

    }
    @Override
    protected void onResume()
    {
        super.onResume();
        /*register the sensor listener to listen to the gyroscope sensor, use the
        callbacks defined in this class, and gather the sensor information as quick
        as possible*/
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_FASTEST);
    }

    //When this Activity isn't visible anymore
    @Override

    protected void onStop()
    {
        //unregister the sensor listener
        sManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
        //Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            return;
        }
        senx = (int)event.values[2];
        seny  = (int)event.values[1];
        senz = (int)event.values[0];
        tv=(TextView) findViewById(R.id.tv);
        //else it will output the Roll, Pitch and Yawn values
        tv.setText("Orientation X (Roll) :"+ Float.toString(event.values[2]) +"\n"+
                "Orientation Y (Pitch) :"+ Float.toString(event.values[1]) +"\n"+
                "Orientation Z (Yaw) :"+ Float.toString(event.values[0]));
    }
    public void onLocationChanged(Location location) {
        //tv_gps = (TextView) findViewById(R.id.tv_gps);
        //String str = "Latitude: \n"+location.getLatitude()+"Longitude: "+location.getLongitude();
        //tv_gps.setText(str);
        //Toast.makeText(getBaseContext(), str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {



        Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {



        Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }



    //client part

    public void onClick(View view) {

         p1_button = (Button)findViewById(R.id.button_start);

        if(isClicked == true){
            new Thread(new messageThread()).start();
            p1_button.setText("STOP");
            running = true;
            isClicked = false;
        }
        else{
            p1_button.setText("START");
            running = false;
            isClicked = true;
        }



    }

    public void splitInt(int num,int coord){
        int hun,ten, one;
        hun = num/100;
        if(hun<0)hun = hun*-1;
        ten = (num/10)%10;
        if(ten<0)ten = ten*-1;
        one = num%10;

        if(coord==0){
            senx0 = hun;
            senx1 = ten;
            senx2 = one;
        }
        if(coord==1){
            seny0 = hun;
            seny1 = ten;
            seny2 = one;
        }
        if(coord==2){
            senz0 = hun;
            senz1 = ten;
            senz2 = one;
        }
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }

    class messageThread implements Runnable {


        public void run() {
            while(running) {
                try {
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    splitInt(senx,0);
                    splitInt(seny,1);
                    splitInt(senz,2);

                    String str = senx0+" "+senx1+" "+senx2+" "+seny0+" "+seny1+" "+seny2+" "+senz0+" "+senz1+" "+senz2+"\0";
                    dataOutputStream.write(str.getBytes());
                    Log.d(TAG,"message sent "+str);
                    dataOutputStream.flush();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    running = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    running = false;
                }
            }
        }
    }
}
