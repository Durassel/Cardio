package cardio.cardio;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {//create the page
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Context context = getApplicationContext();
        /*File file = new File("host.txt");
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            StringBuffer stringBuffer = new StringBuffer();
            int numCharsRead;
            char[] charArray = new char[1024];
            while ((numCharsRead = fileReader.read(charArray)) > 0) {
                stringBuffer.append(charArray, 0, numCharsRead);
            }
            fileReader.close();
            System.out.println("Contents of file:");
            System.out.println(stringBuffer.toString());
            if(stringBuffer.toString()==null)
            {
                System.out.println("ok");
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(Server.getInstance().getIpAddress());
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            ();
        }*/
        /*System.out.println(context.getFilesDir());
        File path = context.getFilesDir();
        File file = new File(path, "host.txt");
        //file.delete();
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write("text-to-write".getBytes());
            System.out.println("ok");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
            System.out.println("OK:" +in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String contents = new String(bytes);*/
        /*try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("host.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
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
                System.out.println("read:"+stringBuilder.toString());
                if(stringBuilder.toString().isEmpty())
                {
                    new FindServer().execute("").get();
                    System.out.println("done");
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        String success="";
        TextView textView = findViewById(R.id.textView2);
        textView.setText("Connexion...");

        try {
            if(Server.getInstance().getIpAddress()==null || Server.getInstance().getIpAddress().isEmpty())
             {
                new FindServer().execute("").get();
             }
            new CallServer().execute("").get();//where we will call the server
            //we cannot do it the mai thread then we need to create a thread, CallServer
            if(User.getInstance().getName()!=null)//if the login worked go in the main page
            {
                success="Hello "+User.getInstance().getName();
                Intent intent = new Intent(this, DisplayMessageActivity.class);
                intent.putExtra(EXTRA_MESSAGE, success);//we send Extra_messsage in the new page
                startActivity(intent);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }
    private class CallServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String responseLine = null;

            try {

                EditText editText = (EditText) findViewById(R.id.editText);//we take the name and password
                EditText editText2 = (EditText) findViewById(R.id.editText2);
                Socket client = new Socket(/*Server.getInstance().getIpAddress()*/"192.168.43.195", 8080);  //connect to server
                DataOutputStream os = null;//input output for simple type
                DataInputStream is = null;
                os = new DataOutputStream(client.getOutputStream());//initialise
                is = new DataInputStream(client.getInputStream());
                
                if (client != null && os != null && is != null) {
                    try {
                        // The capital string before each colon has a special meaning to SMTP
                        // you may want to read the SMTP specification, RFC1822/3
                        /*os.writeBytes("Connexion");
                        System.out.println("ok");
                        os.writeBytes(editText.getText().toString());
                        System.out.println("ok");
                        os.writeBytes(editText2.getText().toString());
                        System.out.println("ok");*/

                        String[] data = {"Connexion", editText.getText().toString(),editText2.getText().toString()};
                          //responseLine= is.readLine();//catch the answer
                        if(responseLine.equals("Connected"))//if it's ok we initialise the data of the user
                        {
                            User.getInstance().setName(editText.getText().toString());
                            User.getInstance().setPassword(editText2.getText().toString());
                            System.out.println(User.getInstance().getName());
                        }

                        // clean up:
                        // close the output stream
                        // close the input stream
                        // close the socket
                        //out.close();
                        os.close();
                        is.close();
                        out.close();
                        in.close();
                        client.close();   //closing the connection
                        //textView.setText("finish");
                    } catch (UnknownHostException e) {
                        responseLine = "Trying to connect to unknown host";
                        System.err.println("Trying to connect to unknown host: " + e);
                    } catch (IOException e) {
                        responseLine = "Error, couldn't reach the server";
                        System.err.println("IOException:  " + e);
			Server.getInstance().setIpAddress("");
                    }
                }


            } catch (UnknownHostException e) {
                responseLine = "Trying to connect to unknown host";
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
            //if (!result.equals("Connected")){//if it is not good, we show it to the user
                TextView textView = findViewById(R.id.textView2);
                textView.setText(result);
            //}

            // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {
            TextView textView = findViewById(R.id.textView2);
            textView.setText("Connexion...");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
    private class FindServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String responseLine = null;
            // Find the server using UDP broadcast
            try {
                //Open a random port to send the package
                DatagramSocket c = new DatagramSocket();
                c.setBroadcast(true);
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
                //Wait for a response
                byte[] recvBuf = new byte[15000];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                c.receive(receivePacket);
                //We have a response
                System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
                //Check if the message is correct
                String message = new String(receivePacket.getData()).trim();
                if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
                    //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                    System.out.println("HEY"+message);
                    responseLine=receivePacket.getAddress().getHostAddress();
                    Server.getInstance().setIpAddress(receivePacket.getAddress().getHostAddress());
                    Context context = getApplicationContext();
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
            System.out.println("Executed");
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }
}
