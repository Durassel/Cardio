package cardio.cardio;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class Login extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "getMessage";
    private boolean first=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {//create the page
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getString("name","").equals(""))
        {
            first=true;
            AlertDialog alertDialog = new AlertDialog.Builder(Login.this).create();
            alertDialog.setTitle("First use");
            alertDialog.setMessage("Set a username and password to set them");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            TextView message=findViewById(R.id.textView2);
            message.setText("Set your username and password");
        }
        System.out.println("name:"+prefs.getString("name",""));
        System.out.println("host:"+prefs.getString("host","0"));
        try {
            InputStream inputStream = context.openFileInput("host.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                if(stringBuilder.toString().isEmpty())
                {
                    new FindServer().execute("");
                }
                else
                {
                    Server.getInstance().setIpAddress(stringBuilder.toString());
                }
                inputStream.close();

            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/

    }
    protected void onStart() {

        super.onStart();

        User.setName("");
    }
    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        String success="";
        TextView textView = findViewById(R.id.textView2);
        textView.setText(getString(R.string.connection));

        //try {
        if(first==true)
        {
            //need authorization too


            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            Intent intent2 = new Intent(getApplicationContext(), Alert.class);
            startService(intent2);
            Toast.makeText(getApplicationContext(), success, Toast.LENGTH_SHORT).show();
            //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            startActivity(intent);
        }
        else
            new CallServer().execute("");//where we will call the server
            //we cannot do it the mai thread then we need to create a thread, CallServer


        /*}/*catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/


    }
    private class CallServer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String responseLine = null;
            publishProgress(getString(R.string.connection));
            try {
                EditText editText = (EditText) findViewById(R.id.editText);//we take the name and password
                EditText editText2 = (EditText) findViewById(R.id.editText2);
                Socket client = new Socket(Server.getInstance().getIpAddress(), 8088);  //connect to server


                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                if (client != null /*&& os != null && is != null*/) {
                    try {
                        // The capital string before each colon has a special meaning to SMTP
                        // you may want to read the SMTP specification, RFC1822/3

                        String on= "{\"type\":\"Connexion\",\"name\":\""+editText.getText().toString()+"\",\"pwd\":\""+editText2.getText().toString()+"\"}";
                        out.println(on);
                        String message = in.readLine();
                        try {
                            JSONObject jsonObj = new JSONObject( message);
                            if(jsonObj.getString("Connexion").equals("Connected"))//if it's ok we initialise the data of the user
                            {
                                User.getInstance().setName(editText.getText().toString());
                                User.getInstance().setPassword(editText2.getText().toString());
                            }
                            responseLine=jsonObj.getString("Connexion");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            responseLine="Error";
                        }

                        out.close();
                        in.close();
                        client.close();   //closing the connection
                        //textView.setText("finish");
                    } catch (UnknownHostException e) {
                        responseLine = "Error, trying to connect to unknown host";
                        System.err.println("Error, trying to connect to unknown host: " + e);
                    } catch (IOException e) {
                        responseLine = "Error, couldn't reach the server";
                        System.err.println("IOException:  " + e);

                    }
                }


            } catch (UnknownHostException e) {
                responseLine = "Error, trying to connect to unknown host";
                e.printStackTrace();
            } catch (IOException e) {
                responseLine = "Error, couldn't reach the server";
                e.printStackTrace();
                Server.getInstance().setIpAddress("");
            }
            return responseLine;
        }

        @Override
        protected void onPostExecute(String result) {
            ProgressBar pb = (ProgressBar) findViewById(R.id.logWait);
            pb.setVisibility(View.GONE);
            TextView textView = findViewById(R.id.textView2);
            if(result.contains("Error"))
            {
                textView.setText(getString(R.string.searchServ));
                new FindServer().execute("");
            }
            else if (!result.equals("Connected")){//if it is not good, we show it to the user
                Button click = (Button) findViewById(R.id.button);
                click.setEnabled(true);
                textView.setText(result);
            }

           else//if the login worked go in the main page
            {
                Button click = (Button) findViewById(R.id.button);
                click.setEnabled(true);
                textView.setText("");
                String success=getString(R.string.hello)+", "+User.getInstance().getName();
                try
                {
                    Bundle saveName= new Bundle();
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        // request permission (see result in onRequestPermissionsResult() method)
                        ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.SEND_SMS}, 123);
                        textView.setText(getString(R.string.acceptSMS));
                        return;
                    }
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // request permission (see result in onRequestPermissionsResult() method)
                    ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                    textView.setText(getString(R.string.acceptSMS));
                    return;
                }
                    saveName.putString("Username", User.getName());
                    Intent intent = new Intent(getApplicationContext(), DisplayMessageActivity.class);
                    Intent intent2 = new Intent(getApplicationContext(), Alert.class);
                    startService(intent2);
                    Toast.makeText(getApplicationContext(), success, Toast.LENGTH_SHORT).show();
                    //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    intent.putExtra(EXTRA_MESSAGE, success);//we send Extra_messsage in the new page
                    startActivity(intent);


                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

        }

        @Override
        protected void onPreExecute() {
            ProgressBar pb = (ProgressBar) findViewById(R.id.logWait);
            pb.setVisibility(View.VISIBLE);
            Button click = (Button) findViewById(R.id.button);
            click.setEnabled(false);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            TextView textView = findViewById(R.id.textView2);
            //textView.setText(values[0]);

        }
    }
    private class FindServer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String responseLine = null;
            // Find the server using UDP broadcast
            try {
                //Open a random port to send the package
                DatagramSocket c = new DatagramSocket();
                c.setBroadcast(true);
                c.setSoTimeout(5000);
                //byte[] sendData= "{\"type\":\"Data\"}".getBytes();
                byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();
                //Try the 255.255.255.255 first
                try {
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                    c.send(sendPacket);
                    System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
                } catch (Exception e) {
                }
                // Broadcast the message over all the network interfaces
                Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue; // Don't want to broadcast to the loopback interface
                    }
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast == null) {
                            continue;
                        }
                        // Send the broadcast package!
                        try {
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                            c.send(sendPacket);
                        } catch (Exception e) {
                        }
                        System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                    }
                }
                System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");
                publishProgress(getString(R.string.searchServ));
                //Wait for a response
                byte[] recvBuf = new byte[1500];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                c.receive(receivePacket);
                //We have a response
                System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
                //Check if the message is correct
                String message = new String(receivePacket.getData()).trim();

                if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
                    //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                    responseLine=receivePacket.getAddress().getHostAddress();
                    Server.getInstance().setIpAddress(receivePacket.getAddress().getHostAddress());
                    Context context = getApplicationContext();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor =prefs.edit();
                    editor.putString("host",receivePacket.getAddress().getHostAddress());
                    editor.apply();

                    Bundle bundle= new Bundle();
                    bundle.putString("host", receivePacket.getAddress().getHostAddress());
                    System.out.println("host:"+bundle.getString("host"));
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("host.txt", Context.MODE_PRIVATE));
                        outputStreamWriter.write(receivePacket.getAddress().getHostAddress());
                        outputStreamWriter.close();
                    }
                    catch (IOException e) {
                        System.out.println("Exception" +  "File write failed: " + e.toString());
                    }
                }
                //Close the port!
                c.close();
            } catch (IOException ex) {
                Logger.getLogger(String.valueOf(ex));
            }

            return responseLine;
        }

        @Override
        protected void onPostExecute(String result) {
            ProgressBar pb = (ProgressBar) findViewById(R.id.logWait);
            pb.setVisibility(View.GONE);
            Button click = (Button) findViewById(R.id.button);
            click.setEnabled(true);
            TextView textView = findViewById(R.id.textView2);
            if(result!=null) {
                textView.setText("Server found, sign in again");
                new CallServer().execute("");
            }
            else
            {
                textView.setText(getString(R.string.ErrServ));
            }


        }

        @Override
        protected void onPreExecute() {
            ProgressBar pb = (ProgressBar) findViewById(R.id.logWait);
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            TextView textView = findViewById(R.id.textView2);
            textView.setText(values[0]);}

    }
}
