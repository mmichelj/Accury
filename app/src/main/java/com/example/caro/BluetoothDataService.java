package com.example.caro;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.Vector;

public class BluetoothDataService extends Service {

    //Debug TAG
    private static final String TAG = "BluetoothDataService";

    //Random UUID
    private static final String A_UUID="00000000-0000-1000-8000-00805F9B34FB";
    private static final String SP_UUID="00001101-0000-1000-8000-00805F9B34F";
    private static final String NAME_SECURE = "BluetoothSecure";
    private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    //Service states
    public static final int STATE_NONE = 0; // idle
    public static final int STATE_LISTEN = 1; // listening for incoming data
    public static final int STATE_CONNECTING = 2; // connecting
    public static final int STATE_CONNECTED = 3; // connected

    private ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;

    private static Handler mHandler = null;
    public static int mState = STATE_NONE;
    public static String deviceName;
    public Vector<Byte> packdata = new Vector<Byte>(2048);
    public static BluetoothDevice device = null;
    private Context mContext;
    private final boolean mAllowInsecureConnections = true;
    public boolean secureConnection;


    BluetoothAdapter bluetoothAdapter;
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Accessed");
        return mBinder;
    }



    public class LocalBinder extends Binder {
       public BluetoothDataService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothDataService.this;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    public void toast(String toastMsg){
        Toast.makeText(this,toastMsg,Toast.LENGTH_SHORT).show();
    }

    private synchronized void connectToDevice(String macAddress){
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        if (mState == STATE_CONNECTING){
            if (mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device);
        //toast("Connecting...");
        mConnectThread.start();
        setState(STATE_CONNECTING);
        Log.d(TAG, "connectToDevice: STATE_CONNECTING");
    }

    private void setState(int state){
        mState = state;
        /*if (mHandler != null){
            // mHandler.obtainMessage();
        }*/
    }


    public synchronized void stop(){
        setState(STATE_NONE);
        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (bluetoothAdapter != null){
            bluetoothAdapter.cancelDiscovery();
        }
        stopSelf();
    }

    public void sendData(String message){
        Log.d(TAG, "sendData: Accessed");
        if (mConnectedThread!= null){
            Log.d(TAG, "sendData: ConnectedThread is not null");
            mConnectedThread.write(message.getBytes());
           // toast("Data sent.");
            Log.d(TAG, "sendData: Data sent.");
            Log.d(TAG, "sendData: "+message);
        }else {
            Toast.makeText(BluetoothDataService.this,"Failed to send data",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sendData: Failed to send data.");
        }
    }

    @Override
    public boolean stopService(Intent name) {
        setState(STATE_NONE);

        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        bluetoothAdapter.cancelDiscovery();
        return super.stopService(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String deviceMAC = intent.getStringExtra("bluetooth_device");
        secureConnection = intent.getBooleanExtra("secure_connection",true);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectToDevice(deviceMAC);

        return START_STICKY;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // mmSocket is final
            //tmp is used to temporarily save the socket object
            java.lang.reflect.Method m;
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.d(TAG, "ConnectThread: mmDevice "+mmDevice.getName());

            try {
                // Get the BluetoothSocket connected
                if(secureConnection)
                {
                    ParcelUuid[] uuids = mmDevice.getUuids();
                    try{
                        m = mmDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] { int.class });
                        tmp = (BluetoothSocket)m.invoke(mmDevice, 1);
                    }catch (NoSuchMethodException ne){
                        Log.e(TAG, "ConnectThread: No such method exception: ", ne);
                    }catch (java.lang.IllegalAccessException ile){
                        Log.e(TAG, "ConnectThread: Illegal acces exception: ", ile);
                    }catch (java.lang.reflect.InvocationTargetException ite){
                        Log.e(TAG, "ConnectThread: Invocation Target Exception: ",ite);
                    }

                }else {
                    Log.d(TAG, "ConnectThread: UUID - "+SerialPortServiceClass_UUID);
                    tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
                }

            } catch (IOException e) {
                Log.e(TAG, "Método create() en BluetoothSocket falló.", e);
            }
            
            //If the BluetoothSocket gets connected, then mmSocket is final
            mmSocket = tmp;
            Log.d(TAG, "ConnectThread: mmSocket connected. "+mmSocket);
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.d(TAG, "run: Connect thread run method connected");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.e(TAG, "run: Error de conexion. ",connectException);
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Conexión del socket cerrada.");
                } catch (IOException closeException) {
                    Log.e(TAG, "No se pudo cerrar la conexión del socket.", closeException);

                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            Log.d(TAG, "run: mmSocket connected! "+mmSocket);
            mConnectedThread = new ConnectedThread(mmSocket,mmDevice);
            mConnectedThread.start();

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "No se pudo cerrar la conexión del socket.", e);
            }
        }
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket cSocket;
        private final InputStream inS;
        private final OutputStream outS;
        private final BluetoothDevice BTDevice;


        private byte[] buffer;

        public ConnectedThread(BluetoothSocket socket,BluetoothDevice device){
            cSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            BTDevice =device;
            try {
                Log.d(TAG, "ConnectedThread: Input stream ini");
                tmpIn = socket.getInputStream();
                Log.d(TAG, "ConnectedThread: Input stream done: " +tmpIn);

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Log.d(TAG, "ConnectedThread: Output stream ini");
                tmpOut = socket.getOutputStream();
                Log.d(TAG, "ConnectedThread: Output stream done: "+tmpOut);
            } catch (IOException e) {
                e.printStackTrace();
            }

            inS = tmpIn;
            outS = tmpOut;
            setState(STATE_CONNECTED);


        }

        @Override
        public void run() {
            Log.d(TAG, "run: Conected thread running");

           /* Intent doneConnecting = new Intent();
            doneConnecting.setAction("Connected");
            doneConnecting.putExtra("deviceMAC",BTDevice.getAddress());
            doneConnecting.putExtra("Result","Connected");
            sendBroadcast(doneConnecting);*/
            String msg="hola";
            byte [] buff = new byte[1024];
            int mByte=0;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    Intent doneConnecting = new Intent();
                    doneConnecting.setAction("Connected");
                    doneConnecting.putExtra("deviceMAC",BTDevice.getAddress());
                    doneConnecting.putExtra("Result","Connected");
                    doneConnecting.putExtra("msg",msg);
                    doneConnecting.putExtra("buff",buff);
                    doneConnecting.putExtra("mByte",mByte);
                    sendBroadcast(doneConnecting);

                    Log.d(TAG, "run: reading input stream: "+inS);
                    mByte = inS.read(buff);
                    Log.d(TAG, "run: "+mByte);
                    msg = new String(buff,0,mByte);
                    Log.d(TAG, "run: String received: "+msg);



                    Log.d(TAG, "run: Data sent to UI "+msg);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    break;
                }
            }


        }


        public void write(byte[] buff){
            try {
                outS.write(buff);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void cancel(){
            try {
                cSocket.close();
                Log.d("service","connected thread cancel method");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        this.stop();
        super.onDestroy();
    }

    private boolean checkS9() {
        return Build.MANUFACTURER.toLowerCase().contains("samsung") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }


}
