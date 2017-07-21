package com.spirent.pttb.servertest2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ServerSocket serverSocket;

    Handler updateConversationHandler;

    Thread serverThread = null;

    private TextView text;

    public static final int SERVERPORT = 6000;


    private LocationManager locationManager=null;
    private Criteria criteria=null;
    private LocationListener locationListener = null;
    private GpsStatus.NmeaListener nmeaListener = null;
    private GpsStatus.Listener gpsStatusListener = null;
    private TextView txtGPS_Quality = null;
    private TextView txtGPS_Location = null;
    private TextView txtGPS_Satellites = null;
    private static  String nmeaData = "null";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text2);





        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        //retrieveData();
        //locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        //locationManager.addNmeaListener(nmeaListener);
        //LocationManager LM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationManager LM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LM.addNmeaListener(nmeaListener);

        ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                .requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {

                    @Override
                    public void onLocationChanged(Location loc) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }



                });

        LM.addNmeaListener(new GpsStatus.NmeaListener()  {
            public void onNmeaReceived(long timestamp, String nmea) {
                //check nmea's checksum
                nmeaData = nmea;
                //text.setText(nmea);
                //Log.d("GPS-NMEA", nmea);

        }
    });




        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
    }






    @Override
    protected void onStop() {
        super.onStop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }




    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;


        //private LocationManager locationManager;



        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {


            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();


                    //write

                    //text.setText("Log: " + String.valueOf(location.getLongitude()));



                    BufferedWriter out = new BufferedWriter
                            (new OutputStreamWriter
                                    (clientSocket.getOutputStream()));
                    //getLocation();

                   out.write(nmeaData);
                    out.newLine();

                    //out.newLine();
                   out.flush();


                    updateConversationHandler.post(new updateUIThread(read));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }












    }

    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");




        }
    }










}
