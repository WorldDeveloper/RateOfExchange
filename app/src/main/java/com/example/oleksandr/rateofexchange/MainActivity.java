package com.example.oleksandr.rateofexchange;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private final String dataSource = "http://www.bank.gov.ua/control/uk/curmetal/currency/search?formType=searchPeriodForm&time_step=daily&currency=169&periodStartTime=01.03.2016&periodEndTime=30.03.2016&outer=xml";
    private TextView tvOutput;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        tvOutput = (TextView) findViewById(R.id.tvOutput);
        Calendar c = Calendar.getInstance();
        tvOutput.setText(c.getTime().toString());

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            DownloadData data=new DownloadData();
                                            data.execute(dataSource);


                                        }
                                    }
        );


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
        } catch (Exception e) {
            Log.d("RateOfExchange", "Error while saving file: "+e.getMessage());
        }
    }


    private class DownloadData extends AsyncTask<String, Void, String>
    {
        private String mFileContents;
        @Override
        protected String doInBackground(String...params)
        {
            mFileContents=downloadFile(params[0]);

            return mFileContents;
        }

        protected  void onPostExecute(String result)
        {
            Log.d("RateOfExchange", "File was downloaded");
            if(mFileContents!=null)
                tvOutput.setText(mFileContents);
        }

        private String downloadFile(String urlPath){
            StringBuilder tempBuffer=new StringBuilder();
            try{
                URL url=new URL(urlPath);
                HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                int response=connection.getResponseCode();
                Log.d("RateOfExchange", "Response code: " + response);
                InputStream is=connection.getInputStream();

//                File dataFile=new File(getFilesDir(),"data.xml");
//                Log.d("RateOfExchange", "Saving xml file");
//                copyInputStreamToFile(is, dataFile);
//                Log.d("RateOfExchange", "Xml file saved in: "+dataFile.getPath());

                InputStreamReader isr=new InputStreamReader(is);
                int charRead;
                char[] inputBuffer=new char[500];
                while((charRead=isr.read(inputBuffer))>0){
                    tempBuffer.append(String.copyValueOf(inputBuffer,0,charRead));
                }
                 isr.close();
                is.close();

            }catch(Exception e){
                Log.d("RateOfExchange", "Error: "+e.getMessage());
            }

            return tempBuffer.toString();
        }

    }

}
