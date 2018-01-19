package cardio.cardio;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class DisplayMessageActivity extends AppCompatActivity {
    public static final String GET_GRAPHIC = "getDataType";
    private ArrayList<Float> Data;
    private static int port = 8080;
    private static String host=Server.getInstance().getIpAddress();
    protected void onCreate(Bundle savedInstanceState) {//initialise the page
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(Login.EXTRA_MESSAGE);//show the message from the previous page

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);
        if(!(message ==null))
            new CallServer().execute("");//to call the server we need a thread
    }
    public void getData(View view)//when pressing refresh, to have the lastest data into the server
    {
        new CallServer().execute("");
    }


    public void getCardio(View view)
    {
        Intent intent = new Intent(this, Graphic.class);//go into the graphic page
        intent.putExtra(GET_GRAPHIC, "CARDIO");//with a a message for wich graphic we should watch
        startActivity(intent);
    }

    public void getTemp(View view)
    {
        Intent intent = new Intent(this, Graphic.class);
        intent.putExtra(GET_GRAPHIC, "TEMP");
        startActivity(intent);
    }

    public void getAccel(View view)
    {
        Intent intent = new Intent(this, Graphic.class);
        intent.putExtra(GET_GRAPHIC, "ACCEL");
        startActivity(intent);
    }

    private class CallServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {//watch CallServer in Login for more information
            try {
                InetAddress address = InetAddress.getByName(host);
                Socket client = new Socket(address, port);
               // Socket client = new Socket("************", 8080);  //connect to server
                //DataOutputStream os = null;
                //DataInputStream is = null;
                //os = new DataOutputStream(client.getOutputStream());
                //is = new DataInputStream(client.getInputStream());
                if (client != null ) {
                    try {
                        String[] ok= {"Data"};

                        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                        out.writeObject(ok);//Send the message "Data" to the server to have back all the data
                        ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                        try {
                            ///can be changed, here we limit us to 3 table of 24 number in only one for the first test of socket
                            ArrayList<Double> objects = (ArrayList<Double>)in.readObject();//where we will have these data
                            ArrayList<Double> fill=new ArrayList<Double>();//used to split the data from objects
                            for(int i=0;i<objects.size();i++)//we split objects to put the good data into the good DataUser table
                            {
                                fill.add(objects.get(i));
                                if(i==23) {
                                    DataUser.getInstance().setCrd(fill);
                                    fill=new ArrayList<Double>();
                                }
                                else  if(i==47) {
                                    DataUser.getInstance().setTmp(fill);
                                    fill=new ArrayList<Double>();
                                }
                                else  if(i==71) {
                                    DataUser.getInstance().setAcc(fill);
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        // clean up:
                        // close the output stream
                        // close the input stream
                        // close the socket
                        out.close();
                        in.close();
                        client.close();   //closing the connection
                        //textView.setText("finish");
                    } catch (UnknownHostException e) {
                        System.err.println("Trying to connect to unknown host: " + e);
                    } catch (IOException e) {
                        System.err.println("IOException:  " + e);
                    }
                }


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            TextView txt = (TextView) findViewById(R.id.textView);
            txt.setText("Executed"); // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
