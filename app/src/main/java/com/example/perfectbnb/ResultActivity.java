package com.example.perfectbnb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {
    double longitute,lat;
    TextView txtName;
    TextView txtTemp;
    Button historyButton;
    Handler handler;
    String cityName,temp;
    DBHelper dbHelper;
    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
//    JSONParser jsonParser = new JSONParser();

    // single product url

    private static final String TAG_NAME = "name";
    private static final String APIKEY = "2128bfb8359e27e54b852ace26c1a2b7";
   private ArrayList<Location> historyArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent i = getIntent();
        txtName = (TextView) findViewById(R.id.city_name_text_view);
        txtTemp = (TextView) findViewById(R.id.temp_text_view);
        historyButton = (Button)findViewById(R.id.history_button);
        longitute = i.getDoubleExtra("long",0.00);
        lat = i.getDoubleExtra("lat",0.00);
        String url_product_detials2 = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+longitute+"&appid="+APIKEY+"&units=metric";
        new GetProductDetails().execute(url_product_detials2);
        dbHelper = new DBHelper(this);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                txtName.setText(cityName);
                txtTemp.setText(temp);

            }

        };
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyArrayList = dbHelper.getAllNotes();
            }
        });
    }

    class GetProductDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ResultActivity.this);
            pDialog.setMessage("Loading product details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(final String... params) {
            final HttpURLConnection[] connection = {null};
            final BufferedReader[] reader = {null};
            // updating UI from Background Thread



                    try {
                    URL url = new URL(params[0]);
                    connection[0] = (HttpURLConnection) url.openConnection();
                        connection[0].connect();


                        // Building Parameters
                        InputStream stream = connection[0].getInputStream();
                        reader[0] = new BufferedReader(new InputStreamReader(stream));
                        StringBuffer buffer = new StringBuffer();
                        String line = "";
                        while ((line = reader[0].readLine()) != null ){
                            buffer.append(line);
                        }
                        String  result = buffer.toString();
                        JSONObject parentObject = new JSONObject(result);
                         cityName = parentObject.getString("name");
                        JSONObject parentObject2 = parentObject.getJSONObject("main");
                        temp= parentObject2.getString("temp");
                        JSONArray parentArray = parentObject.getJSONArray("main");
                          JSONObject  finalTempJSON = parentArray.getJSONObject(0);
                       //  temp = finalTempJSON.getString("temp");

                        return temp;

                    }
                    catch (MalformedURLException e){
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if(connection[0] != null){
                            connection[0].disconnect();
                        }
                        if (reader[0] != null){
                            try {
                                reader[0].close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

            return null;
        }


        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details
            pDialog.dismiss();
            txtName.setText("City Name:"+cityName);
            txtTemp.setText("Temp:"+temp + "\u2103");
            Location location = new Location(cityName,longitute+"",lat+"");
            dbHelper.insertNote(location);
            System.out.println("Result Activity, Temp::"+temp);
        }
    }
}
