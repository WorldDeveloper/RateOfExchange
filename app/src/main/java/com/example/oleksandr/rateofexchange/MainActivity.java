package com.example.oleksandr.rateofexchange;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;

import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class MainActivity extends AppCompatActivity {

    private String dataSource;
    private TextView tvOutput;
    private TextView tvForecast;
    private String mTimePeriod;
    private String mCurrency;
    private String mNumberOfUnits;
    NodeList nodelist;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvOutput = (TextView) findViewById(R.id.tvOutput);
        tvForecast=(TextView)findViewById(R.id.tvForecast);

        mTimePeriod=getIntent().getStringExtra("timePeriod");
        if(mTimePeriod==null)
            mTimePeriod="";

        mCurrency=getIntent().getStringExtra("currency");
        if(mCurrency==null)
            mCurrency="";

        makeCalculations();
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
            try {
                Intent intent = new Intent(getApplicationContext(), ConfigurationActivity.class);

                intent.putExtra("timePeriod",mTimePeriod);
                intent.putExtra("currency", mCurrency);
                startActivity(intent);
            }catch(Exception e)
            {}

            return true;
        }
        else if(id==R.id.action_update){
            makeCalculations();
            return true;
        }
        else if(id==R.id.action_exit){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeCalculations(){
        try {
            tvOutput.setText("Status: updating...");
            tvForecast.setText("");

            SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyy", Locale.UK);
            Calendar c= Calendar.getInstance();
            String endDate=sdf.format(c.getTime());
            dataSource=String.format("http://www.bank.gov.ua/control/uk/curmetal/currency/search?formType=searchPeriodForm&time_step=daily&currency=%1s&periodStartTime=%2s&periodEndTime=%3s&outer=xml", getCurrencyCode(mCurrency), getStartDate(mTimePeriod), endDate);
            new DownloadXML().execute(dataSource);
        } catch (Exception e) {
            Log.d("RateOfExchange", e.getMessage());
            tvOutput.setText("Error!");
        }
    }

    private String getCurrencyCode(String code){
        switch(code){
            case "EUR":
               return "196";
            case "GBP":
                return "163";
            case "JPY":
                return "78";
            case "CNY":
                return "34";
            case "RUB":
                return "209";
            default://USD
                mCurrency="USD";
                return "169";
        }
    }

    private String formatDate(Calendar date){
        SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyy", Locale.UK);
        return sdf.format(date.getTime());
    }

    private String getStartDate(String timePeriod){
        Calendar date= Calendar.getInstance();

        switch(timePeriod){
            case "Half year":
                date.add(Calendar.DAY_OF_MONTH, -365/2);
                return formatDate(date);
            case "Year":
                date.add(Calendar.DAY_OF_MONTH, -365);
                return formatDate(date);
            case "From 1996":
                return "02.09.1996";
            default://Month
                date.add(Calendar.DAY_OF_MONTH, -31);
                return formatDate(date);
        }
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

        ArrayList<Float> yVals=new ArrayList<>();
        for(DataPoint point:realData){
            yVals.add(point.getValue());
        }
        LinearRegression linearRegression=new LinearRegression(yVals);

        for (DataPoint element : realData) {
            realValues.add(new Entry(element.getValue(), element.getId()));
            xVals.add(element.getLabel());
        }

        predictedValues.add(new Entry((float)(linearRegression.getBeta0()), 0));//start point

        int x=realValues.size()-1;
        predictedValues.add(new Entry((float)((x+1)*linearRegression.getBeta1()+linearRegression.getBeta0()), (x+1)));
        float forecastOnNextMonth=(float)((x+30)*linearRegression.getBeta1()+linearRegression.getBeta0());
        predictedValues.add(new Entry(forecastOnNextMonth, (x+30)));
        tvForecast.setText(String.format("Forecast on the next month: %.2f UAH/%s%s\nModel quality: %.2f%%",
                forecastOnNextMonth,mNumberOfUnits, mCurrency, linearRegression.getR2()*100));
        for(int i=1; i<31; i++) {
            xVals.add(i + " days");
        }

        LineDataSet realSet = new LineDataSet(realValues, "Real");
        realSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        realSet.setDrawValues(false);
        realSet.setCircleRadius(0.5f);

        LineDataSet predictedSet = new LineDataSet(predictedValues, "Predicted");
        predictedSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        predictedSet.setColors(new int[]{Color.RED});
        //predictedSet.setDrawValues(false);
        predictedSet.setCircleColor(Color.RED);
        predictedSet.setCircleRadius(1.0f);


        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(realSet);
        dataSets.add(predictedSet);

        LineData data = new LineData(xVals, dataSets);
        lineChart.setData(data);
        lineChart.setDescription(String.format("UAH per %1s %2s", mNumberOfUnits, mCurrency));
        lineChart.invalidate(); // refresh

    }

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
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... Url)  {
            try {
                nodelist=null;
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

            if(nodelist==null) {
                tvOutput.setText("Error: can't download data");
                pDialog.dismiss();
                return;
            }else if( nodelist.getLength() < 2) {
                tvOutput.setText("Error: not enough data");
                pDialog.dismiss();
                return;
            }

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

            Node nNode = nodelist.item(0);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                mNumberOfUnits=getNode("number_of_units", eElement);
            }

            Log.d("RateOfExchange", "chart points count: "+testData.size());

            Calendar c = Calendar.getInstance();
            tvOutput.setText("Last updated: " + DateFormat.getDateTimeInstance().format(c.getTime()));

            if(testData.size()>3)
                setGraphData(testData);
            else{
                tvOutput.setText("Error: can't build graph");
            }
            // Close progressbar
            pDialog.dismiss();
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
