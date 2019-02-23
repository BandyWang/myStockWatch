package com.example.mystockwatch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleEntry;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StocksScreen  extends AppCompatActivity {
    private CandleStickChart chart;
    private String url;
    private List<CandleEntry> list;
    private JSONArray jsonArray;

    @Override
    protected void onCreate (Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocks_screen);

        Intent intent = getIntent();
        String stockSymbol = intent.getStringExtra(MainActivity.EXTRA_TEXT);
        Log.d("key","stock: "+stockSymbol);

        list = new ArrayList<>();
        url= "https://api.iextrading.com/1.0/stock/"+stockSymbol+"/chart/1m";


        /*
        int timeout = 3000;
        AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
        TimeSeries stockTimeSeries = new TimeSeries(apiConnector);
        Log.d("test","hello");
        try {
            IntraDay response = stockTimeSeries.intraDay("MSFT", Interval.ONE_MIN, OutputSize.COMPACT);
            Map<String, String> metaData = response.getMetaData();
            System.out.println("Information: " + metaData.get("1. Information"));
            System.out.println("Stock: " + metaData.get("2. Symbol"));

            List<StockData> stockData = response.getStockData();
            for(int i = 0; i<stockData.size();i++){
                Log.d("test",stockData.get(i).getDateTime().toString());
            }
        } catch (AlphaVantageException e) {
            Log.d("test","apple");
        }
        */
        setChartSettings();
        //setJsonArray();
        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(JsonParse());


    /*
        if(jsonArray != null){
            for(int i = 0; i < jsonArray.length();i++){
                JSONObject obj;
                try {
                    obj = jsonArray.getJSONObject(i);
                    float open = Float.parseFloat(obj.getString("1. open"));
                    Log.d("test","Low: " + open);
                    float high = Float.parseFloat(obj.getString("2. high"));
                    float low = Float.parseFloat(obj.getString("3. low"));
                    float close = Float.parseFloat(obj.getString("4. close"));

                } catch (JSONException e){

                    Log.d("test","obj is null inside for loop");
                }



            }

        }
   */

    }
    private void setJsonArray(){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = JsonReader.readJsonFromUrl(url);
        } catch (IOException e) {
            Log.e("StocksScreen", "jsonObject IOException");
        } catch (JSONException e) {
            Log.e("StocksScreen", "jsonObject jsonException");
        }
        try {
            jsonArray = jsonObject.getJSONArray("Time Series (5min)");
        } catch (JSONException e) {
            Log.d("test", "jsonArray jsonException");
            e.printStackTrace();

        }
    }
    private void setChartSettings(){
        chart = (CandleStickChart)findViewById(R.id.candleStickChart);
        chart.setNoDataText("No data from this stock");
        chart.setHighlightPerDragEnabled(true);

        chart.setDrawBorders(true);

        chart.setBorderColor(getResources().getColor(R.color.colorPrimaryDark));

        YAxis yAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        yAxis.setDrawGridLines(false);
        rightAxis.setDrawGridLines(false);
        chart.requestDisallowInterceptTouchEvent(true);

        XAxis xAxis = chart.getXAxis();

        xAxis.setDrawGridLines(false);// disable x axis grid lines
        xAxis.setDrawLabels(false);
        rightAxis.setTextColor(Color.WHITE);
        yAxis.setDrawLabels(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setAvoidFirstLastClipping(true);

        Legend l = chart.getLegend();
        l.setEnabled(false);

    }
    private StringRequest JsonParse(){
        Log.d("key","hello");
        StringRequest request =  new StringRequest( Request.Method.GET,url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr =  new JSONArray(response);

                    for(int i = 0; i < arr.length();i++){
                        JSONObject obj = arr.getJSONObject(i);
                        String date = obj.getString("date");
                        Log.d("key", date);
                        double high = obj.getDouble("high");
                        Log.d("key", Double.toString(high));
                        //String key = obj.names().getString(i);
                       // JSONObject data = obj.get(key);
                       // Double open = data.getDouble("1. open");
                        //Log.d("key",Double.toString(open));

                    }
                } catch (JSONException e) {
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
}
