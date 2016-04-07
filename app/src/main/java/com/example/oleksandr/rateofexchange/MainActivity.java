package com.example.oleksandr.rateofexchange;

import android.app.ProgressDialog;
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

import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


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
        tvOutput.setText("Last updated: " + DateFormat.getDateTimeInstance().format(c.getTime()));

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new DownloadXML().execute(dataSource);

                                            // DownloadData data=new DownloadData();
                                            // data.execute(dataSource);

//                                            List<DataPoint> testData=new ArrayList<>();
//                                            testData.add(new DataPoint(0, 100.0f, "2000"));
//                                            testData.add(new DataPoint(1, 110.0f, "2001"));
//                                            testData.add(new DataPoint(2, 120.0f, "2002"));
//                                            testData.add(new DataPoint(3,110.0f, "2003"));
//
//                                            setGraphData(testData);
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

    private class DataPoint {
        private int mId;
        private float mValue;
        private String mLabel;

        public DataPoint(int id, float value, String label) {
            mId = id;
            mValue = value;
            mLabel = label;
        }

        public int getId() {
            return mId;
        }

        public float getValue() {
            return mValue;
        }

        public String getLabel() {
            return mLabel;
        }
    }

    private void setGraphData(List<DataPoint> realData) {
        LineChart lineChart = (LineChart) findViewById(R.id.chart);

        ArrayList<Entry> realValues = new ArrayList<Entry>();
        ArrayList<Entry> predictedValues = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        for (DataPoint element : realData) {
            realValues.add(new Entry(element.getValue(), element.getId()));
            xVals.add(element.getLabel());
        }


        Entry c2e1 = new Entry(2220.000f, 0); // 0 == quarter 1
        predictedValues.add(c2e1);
        Entry c2e2 = new Entry(2000.000f, 1); // 1 == quarter 2 ...
        predictedValues.add(c2e2);
        //...

        LineDataSet realSet = new LineDataSet(realValues, "Real");
        realSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet predictedSet = new LineDataSet(predictedValues, "Predicted");
        predictedSet.setAxisDependency(YAxis.AxisDependency.LEFT);


        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(realSet);
        dataSets.add(predictedSet);

        LineData data = new LineData(xVals, dataSets);
        lineChart.setData(data);
        lineChart.invalidate(); // refresh

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
            Log.d("RateOfExchange", "Error while saving file: " + e.getMessage());
        }
    }

    private class DownloadData extends AsyncTask<String, Void, String> {
        private String mFileContents;

        @Override
        protected String doInBackground(String... params) {
            mFileContents = downloadFile(params[0]);

            return mFileContents;
        }

        protected void onPostExecute(String result) {
            Log.d("RateOfExchange", "File was downloaded");
            if (mFileContents != null)
                tvOutput.setText(mFileContents);
        }

        private String downloadFile(String urlPath) {
            StringBuilder tempBuffer = new StringBuilder();
            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d("RateOfExchange", "Response code: " + response);
                InputStream is = connection.getInputStream();

//                File dataFile=new File(getFilesDir(),"data.xml");
//                Log.d("RateOfExchange", "Saving xml file");
//                copyInputStreamToFile(is, dataFile);
//                Log.d("RateOfExchange", "Xml file saved in: "+dataFile.getPath());

                InputStreamReader isr = new InputStreamReader(is);
                int charRead;
                char[] inputBuffer = new char[500];
                while ((charRead = isr.read(inputBuffer)) > 0) {
                    tempBuffer.append(String.copyValueOf(inputBuffer, 0, charRead));
                }
                isr.close();
                is.close();

            } catch (Exception e) {
                Log.d("RateOfExchange", "Error: " + e.getMessage());
            }

            return tempBuffer.toString();
        }

    }


    NodeList nodelist;
    ProgressDialog pDialog;

    // DownloadXML AsyncTask
    private class DownloadXML extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressbar
            pDialog = new ProgressDialog(MainActivity.this);
            // Set progressbar title
            pDialog.setTitle("Please, wait");
            // Set progressbar message
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            // Show progressbar
            //pDialog.show();
        }

        @Override
        protected Void doInBackground(String... Url) {
            try {
                URL url = new URL(Url[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                // Download the XML file
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
                // Locate the Tag Name
                nodelist = doc.getElementsByTagName("currency");

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            List<DataPoint> testData = new ArrayList<>();
            Log.d("RateOfExchange", "Nodes count: "+nodelist.getLength());
            int id=0;
            for (int temp = 0; temp <nodelist.getLength(); temp++) {

                Node nNode = nodelist.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    testData.add(new DataPoint(id++, Float.parseFloat(getNode("exchange_rate", eElement)), getNode("date", eElement)));
                }
            }

            Log.d("RateOfExchange", "chart points count: "+testData.size());
            if(testData.size()>3)
                setGraphData(testData);
            else{
                Toast toast=Toast.makeText(MainActivity.this, "It is not enought data", Toast.LENGTH_LONG );
                toast.show();
            }
            // Close progressbar
           // pDialog.dismiss();
        }
    }

    // getNode function
    private static String getNode(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
                .getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }

}
