package edu.buffalo.cse.cse486586.simplemessenger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.OutputStream; //
import java.io.OutputStreamWriter; //
import java.io.BufferedWriter; //
import java.io.InputStream; //
import java.io.InputStreamReader; //
import java.io.BufferedReader; //

import edu.buffalo.cse.cse486586.simplemessenger.R;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

public class SimpleMessengerActivity extends Activity 
{
    static final String TAG = SimpleMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final int SERVER_PORT = 10000;

    /** Called when the Activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
                      
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try 
        {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } 
        catch (IOException e) 
        {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        final EditText editText = (EditText) findViewById(R.id.edit_text);
        
        editText.setOnKeyListener(new OnKeyListener() 
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) 
            {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) 
                {
                    String msg = editText.getText().toString() + "\n";
                    editText.setText(""); // This is one way to reset the input box.
                    TextView localTextView = (TextView) findViewById(R.id.local_text_display);
                    localTextView.append("\t" + msg); // This is one way to display a string.
                    TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
                    remoteTextView.append("\n");

                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                    return true;
                }
                return false;
            }
        });
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> 
    {
        @Override
        protected Void doInBackground(ServerSocket... sockets) 
        {
                        
            // Indranil's code starts here
        	// Receive message from Other Device
        	try
        	{
        		while (true)
        		{        	
        			ServerSocket serverSocket = sockets[0];
        			Socket accept_socket = serverSocket.accept();
        			
        			InputStream sock_in = accept_socket.getInputStream();
        			InputStreamReader sock_in_read = new InputStreamReader(sock_in);
        			BufferedReader r_buffer = new BufferedReader(sock_in_read);
                
        			String msgToRecv = r_buffer.readLine();
        	        publishProgress(msgToRecv);
        		}
        	}
        	catch (IOException e)
        	{
        		Log.e(TAG, "Accept Failed");
        	}
        	
        	// Indranil's Code end here
        	
            return null;
        }

        protected void onProgressUpdate(String...strings) 
        {
            
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.local_text_display);
            localTextView.append("\n");
            
            String filename = "SimpleMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try 
            {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } 
            catch (Exception e) 
            {
                Log.e(TAG, "File write failed");
            }

            return;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> 
    {
        @Override
        protected Void doInBackground(String... msgs) 
        {
            try 
            {
                String remotePort = REMOTE_PORT0;
                if (msgs[1].equals(REMOTE_PORT0))
                    remotePort = REMOTE_PORT1;

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(remotePort));
                
                String msgToSend = msgs[0];
               
                // Indranil's Code Starts Here    
                
                // Sending Message out to Other Device
                OutputStream sock_out = socket.getOutputStream();
                OutputStreamWriter sock_out_write = new OutputStreamWriter(sock_out);
                BufferedWriter w_buffer = new BufferedWriter(sock_out_write);
                                
                w_buffer.write(msgToSend);
                w_buffer.flush();
                
                socket.close();
                
                // Indranil's Code ends Here
            } 
            catch (UnknownHostException e) 
            {
                Log.e(TAG, "ClientTask UnknownHostException");
            } 
            catch (IOException e) 
            {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }
}