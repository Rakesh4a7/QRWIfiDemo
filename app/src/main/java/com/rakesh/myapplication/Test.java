package com.rakesh.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Test extends Activity {

    EditText textOut;
    TextView textIn;
    Socket socket = null;
    DataOutputStream dataOutputStream = null;
    DataInputStream dataInputStream = null;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        textOut = (EditText)findViewById(R.id.textout);
        Button buttonSend = (Button)findViewById(R.id.send);
        textIn = (TextView)findViewById(R.id.textin);
        buttonSend.setOnClickListener(buttonSendOnClickListener);
    }

    Button.OnClickListener buttonSendOnClickListener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
runTcpClient();
/*            Thread thread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        socket = new Socket("192.168.137.1", 4096);
                        dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataInputStream = new DataInputStream(socket.getInputStream());
                        dataOutputStream.writeUTF(textOut.getText().toString());
                        textIn.setText(dataInputStream.readUTF());
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    finally{
                        if (socket != null){
                            try {
                                socket.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if (dataOutputStream != null){
                            try {
                                dataOutputStream.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if (dataInputStream != null){
                            try {
                                dataInputStream.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };
            thread.start();*/

        }};


    private static final int TCP_SERVER_PORT = 4096;
    private void runTcpClient() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Socket s = new Socket("192.168.0.106", TCP_SERVER_PORT);
                    s.setSoTimeout(10000);
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    Log.i("TcpClient", "Connected!");
                    //send output msg
                    String outMsg = textOut.getText().toString()+"\n";
                    out.write(outMsg);
                    out.flush();
                    Log.i("TcpClient", "sent: " + outMsg);
                    //accept server response
                    String inMsg = in.readLine() + System.getProperty("line.separator");
                    Log.i("TcpClient", "received: " + inMsg);
                    //close connection
                    s.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}