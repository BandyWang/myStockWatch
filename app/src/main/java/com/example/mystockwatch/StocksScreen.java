package com.example.mystockwatch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.IntraDay;
import org.patriques.output.timeseries.data.StockData;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class StocksScreen  extends AppCompatActivity {
    private CandleStickChart candleChart;

    private ArrayList<StockObj> dataTemp;
    private ArrayList<CandleEntry> data;
    private JSONArray jsonArray;
    private long referenceTimeStamp;
    private String stockSymbol;
    private String stockName;
    private String timeframe;
    private RequestQueue queue;

    //SQLite variables
    private DatabaseHelper db;


    @Override
    protected void onCreate (Bundle savedInstanceState) {

        timeframe = "1 day"; //defualt

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocks_screen);
        linkButtons();
        Intent intent = getIntent();
        stockSymbol = intent.getStringExtra("EXTRA_SYMBOL");
        stockName = intent.getStringExtra("EXTRA_NAME");

        data = new ArrayList<>();
        dataTemp = new ArrayList<StockObj>();
        referenceTimeStamp = -1;
        TextView symbolTextView = (TextView) findViewById(R.id.stockSymbol);
        TextView nameTextView = (TextView) findViewById(R.id.stockName);
        symbolTextView.setText(stockSymbol);
        nameTextView.setText(stockName);


        setChartSettings();

        queue = Volley.newRequestQueue(this);
        queue.add(JsonParse());
        queue.add(getKeyFacts());
        queue.add(getKeyFacts2());

        db = new DatabaseHelper(this);
        boolean isInDB = db.exists(stockSymbol);
        if(!isInDB){
            Log.d("SQL","Not in DB!");
        }else{
            Log.d("SQL","Is in DB!");
        }
        linkAddDbutton(isInDB);



    }

    private void setChartSettings(){
        candleChart = (CandleStickChart)findViewById(R.id.candleStickChart);
        candleChart.setNoDataText("No data from this stock");
        candleChart.setHighlightPerDragEnabled(true);
        candleChart.setTouchEnabled(true);
        candleChart.setDrawBorders(true);

        candleChart.setBorderColor(getResources().getColor(R.color.colorPrimaryDark));

        candleChart.setHighlightPerDragEnabled(true);

        candleChart.setDrawBorders(true);
        candleChart.setBackgroundColor(Color.BLACK); //set whatever color you prefer
        candleChart.setDrawGridBackground(false);// this is a must
        candleChart.setScaleEnabled(false);
        //candleChart.setBorderColor(getResources().getColor(R.color.colorLightGray));

        YAxis yAxis = candleChart.getAxisLeft();
        YAxis rightAxis = candleChart.getAxisRight();
        yAxis.setDrawGridLines(false);
        rightAxis.setDrawGridLines(false);
        candleChart.requestDisallowInterceptTouchEvent(true);

        XAxis xAxis = candleChart.getXAxis();

        xAxis.setDrawGridLines(false);// disable x axis grid lines
        xAxis.setDrawLabels(true);
        rightAxis.setTextColor(Color.WHITE);
        yAxis.setDrawLabels(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setAvoidFirstLastClipping(true);

        Legend l = candleChart.getLegend();
        l.setEnabled(false);

    }
    private StringRequest JsonParse(){
        String url = "";
        if(timeframe.equals("1 month")){
            url= "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="+stockSymbol
                    +"&apikey=P1CWMMTE0GLRFBYL&outputsize=compact";
        }else if(timeframe.equals("1 day")){
            url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol="+stockSymbol+
                    "&interval=15min&outputsize=full&apikey=P1CWMMTE0GLRFBYL";
           // url= "https://api.iextrading.com/1.0/stock/"+stockSymbol+"/chart/1d";
        }else{
            url= "https://www.alphavantage.co/query?function=TIME_SERIES_WEEKLY&symbol="+stockSymbol+"&apikey=P1CWMMTE0GLRFBYL";
        }

        StringRequest request =  new StringRequest( Request.Method.GET,url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj =  new JSONObject(response);
                    if(timeframe.equals("1 day")) {
                        obj = obj.getJSONObject("Time Series (15min)");
                    }else if(timeframe.equals("1 month")){
                        obj = obj.getJSONObject("Time Series (Daily)");
                    }else if(timeframe.equals("1 year")){
                        obj = obj.getJSONObject("Weekly Time Series");
                    }
                    SimpleDateFormat sdf;
                    if(timeframe.equals("1 month") || timeframe.equals("1 year")) {
                        sdf = new SimpleDateFormat("yyyy-MM-dd");
                    }else if(timeframe.equals("1 day")){
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    }else{
                        sdf = null;
                        //placehoder
                    }
                    int count = 0;
                    JSONArray keys = obj.names();
                    String firstDate = keys.getString(0);
                    long nearEndMili = 0;
                    int unit = -1;
                    if(timeframe.equals("1 day")) {
                        SimpleDateFormat sdfTemp = new SimpleDateFormat("yyyy-MM-dd");

                        String[] split = firstDate.split("\\s+");
                        String nearDayString = split[0];
                        Date nearDay = sdfTemp.parse(nearDayString);

                        unit = 900000;
                        nearEndMili = nearDay.getTime();
                    }else{
                        SimpleDateFormat sdfTemp = new SimpleDateFormat("yyyy-MM-dd");
                        Date dateTemp = sdfTemp.parse(firstDate);
                        Calendar c = Calendar.getInstance();
                        c.setTime(dateTemp);
                        if(timeframe.equals("1 month")) {
                            c.add(Calendar.MONTH, -1);
                            unit  = 86400000; // 1 day
                        }else if (timeframe.equals("1 year")){
                            c.add(Calendar.YEAR, -1);
                            unit = 604800000; //1 week
                        }
                        dateTemp = c.getTime();
                        nearEndMili = dateTemp.getTime();

                    }

                    for(int i = 0; i < keys.length();i++){


                        String key = keys.getString(i);
                        //Log.d("apple",key);
                        JSONObject obj2 = obj.getJSONObject(key);
                        Date date = sdf.parse(key);
                        //Timestamp ts = new Timestamp(date.getTime());
                        long milli = date.getTime();

                        if(milli <  nearEndMili){
                            break;
                        }
                        //milli = milli/ 900000;

                        float high = Float.parseFloat(obj2.getString("2. high"));
                        float low = Float.parseFloat(obj2.getString("3. low"));
                        float open = Float.parseFloat(obj2.getString("1. open"));
                        float close = Float.parseFloat(obj2.getString("4. close"));

                            dataTemp.add(new StockObj( milli, high, low, open, close, key));

                        }
                    Collections.reverse(dataTemp);
                    referenceTimeStamp = -1;
                    for(int i = 0; i < dataTemp.size();i++){
                        StockObj SOtemp = dataTemp.get(i);
                        if(referenceTimeStamp == -1){
                            referenceTimeStamp = SOtemp.getX();
                        }
                        float xValue = (float)((SOtemp.getX() - referenceTimeStamp)/unit);
                        data.add(new CandleEntry( xValue,SOtemp.getHigh(),SOtemp.getLow(),SOtemp.getOpen(),SOtemp.getClose(),SOtemp.getLabel()));
                    }
                   // referenceTimeStamp = data.get(0).getX();


                    CandleDataSet set1 = new CandleDataSet(data, "DataSet 1");
                    set1.setColor(Color.rgb(80, 80, 80));
                    set1.setShadowColor(Color.parseColor("#d7dbe2"));
                    set1.setShadowWidth(0.8f);
                    set1.setDecreasingColor(Color.parseColor("#e02a02"));
                    set1.setDecreasingPaintStyle(Paint.Style.FILL);
                    set1.setIncreasingColor(Color.parseColor("#1dc17a"));
                    set1.setIncreasingPaintStyle(Paint.Style.FILL);
                    set1.setNeutralColor(Color.LTGRAY);
                    set1.setDrawValues(false);
                    //Log.d("error","here2");

                    CandleData data = new CandleData(set1);
                    //Log.d("error","here3");
                    candleChart.setData(data);
                    candleChart.invalidate();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });


        return request;
    }
    private void linkButtons(){
        Button oneYear = (Button) findViewById(R.id.oneYear);
        oneYear.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d("button","1");
                timeframe = "1 year";
                referenceTimeStamp = -1;
                data.clear();
                dataTemp.clear();
                //RequestQueue queue = Volley.newRequestQueue(StocksScreen.this);
                queue.add(JsonParse());
            }
        });

        Button oneMonth = (Button) findViewById(R.id.oneMonth);
        oneMonth.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d("button","2");
                timeframe = "1 month";
                referenceTimeStamp = -1;
                data.clear();
                dataTemp.clear();
                //RequestQueue queue = Volley.newRequestQueue(StocksScreen.this);
                queue.add(JsonParse());
            }
        });

        Button oneDay = (Button) findViewById(R.id.oneDay);
        oneDay.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d("button","3");
                timeframe = "1 day";
                referenceTimeStamp= -1;
                data.clear();
                dataTemp.clear();
               // RequestQueue queue = Volley.newRequestQueue(StocksScreen.this);
                queue.add(JsonParse());
            }
        });
    }
    private StringRequest getKeyFacts(){
        String url = "https://api.iextrading.com/1.0/stock/"+stockSymbol+"/quote";
        StringRequest request =  new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    TextView open = findViewById(R.id.Zero2);
                    open.setText(obj.getString("open"));

                    TextView high = findViewById(R.id.One2);
                    high.setText(obj.getString("high"));

                    TextView low = findViewById(R.id.Two2);
                    low.setText(obj.getString("low"));

                    TextView wk52Hi = findViewById(R.id.Three2);
                    wk52Hi.setText(obj.getString("week52High"));

                    TextView wk52Low = findViewById(R.id.Four2);
                    wk52Low.setText(obj.getString("week52Low"));

                    TextView close = findViewById(R.id.Zero6);
                    close.setText(obj.getString("close"));

                    TextView avgVol = findViewById(R.id.One6);
                    avgVol.setText(format(obj.getLong("avgTotalVolume")));

                    TextView mktCap = findViewById(R.id.Two6);
                    mktCap.setText(format(obj.getLong("marketCap")));

                    TextView peRatio = findViewById(R.id.Three6);
                    peRatio.setText(obj.getString("peRatio"));


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        return request;

    }
    private StringRequest getKeyFacts2(){
        String url = "https://api.iextrading.com/1.0/stock/"+stockSymbol+"/stats";
        StringRequest request =  new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    TextView pbRatio = findViewById(R.id.Four6);
                    pbRatio.setText(obj.getString("priceToBook"));


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        return request;

    }
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }
    private static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
    private void linkAddDbutton(boolean isInDB){
        Button button = (Button)findViewById(R.id.add);
        if(!isInDB){
            button.setText("Add");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean boo = db.addData(stockSymbol);
                    if(boo){
                        Log.d("SQL","added "+ stockSymbol +" to database!");
                    }else{
                        Log.d("SQL","Error in add!");
                    }
                    linkAddDbutton(true);
                }
            });
        }else{
            button.setText("Delete");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.delete(stockSymbol);
                    Log.d("SQL","Deleted from SQL: "+ stockSymbol);
                    linkAddDbutton(false);
                }
            });
        }
    }
}
