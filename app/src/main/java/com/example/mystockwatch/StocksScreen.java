package com.example.mystockwatch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StocksScreen  extends AppCompatActivity {
    private CandleStickChart candleChart;

    private ArrayList<CandleEntry> data;
    private JSONArray jsonArray;
    private long referenceTimeStamp;
    private String stockSymbol;
    private String timeframe;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        timeframe = "1 day"; //defualt

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocks_screen);
        linkButtons();
        Intent intent = getIntent();
        stockSymbol = intent.getStringExtra(MainActivity.EXTRA_TEXT);

        data = new ArrayList<>();

        referenceTimeStamp = -1;
        TextView stockTextView = (TextView) findViewById(R.id.stockName);
        stockTextView.setText(stockSymbol);


        setChartSettings();

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(JsonParse());




    }

    private void setChartSettings(){
        candleChart = (CandleStickChart)findViewById(R.id.candleStickChart);
        candleChart.setNoDataText("No data from this stock");
        candleChart.setHighlightPerDragEnabled(true);

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
            url= "https://api.iextrading.com/1.0/stock/"+stockSymbol+"/chart/1m";
        }else if(timeframe.equals("1 day")){
            url= "https://api.iextrading.com/1.0/stock/"+stockSymbol+"/chart/1d";
        }else{
            url= "https://api.iextrading.com/1.0/stock/"+stockSymbol+"/chart/1y";
        }

        StringRequest request =  new StringRequest( Request.Method.GET,url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr =  new JSONArray(response);
                    SimpleDateFormat sdf;
                    if(timeframe.equals("1 month") || timeframe.equals("1 year")) {
                        sdf = new SimpleDateFormat("yyyy-MM-dd");
                    }else if(timeframe.equals("1 day")){
                        sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
                    }else{
                        sdf = null;
                        //placehoder
                    }
                    for(int i = 0; i < arr.length();i++){
                        JSONObject obj = arr.getJSONObject(i);
                        String dateString = obj.getString("date");

                        if(timeframe.equals("1 day")){
                            String minute = obj.getString("minute");
                            dateString = dateString + " " + minute;
                            Log.d("lol",dateString);
                        }
                        float high = Float.parseFloat(obj.getString("high"));

                        float low = Float.parseFloat(obj.getString("low"));
                        if(!obj.getString("open").equals("null")) {
                            Log.d("open", obj.getString("open"));

                            float open = Float.parseFloat(obj.getString("open"));


                            float close = Float.parseFloat(obj.getString("close"));

                            Date date = sdf.parse(dateString);
                            Timestamp ts = new Timestamp(date.getTime());
                            long milli = date.getTime();

                            Log.d("lol", Long.toString(milli));
                            long unit = 0;
                            if (timeframe.equals("1 month") || timeframe.equals("1 year")) {
                                unit = 86400000;    // divide by one day
                            } else if (timeframe.equals("1 day")) {
                                unit = 60000; //divide by one minute;
                            }

                            if (referenceTimeStamp == -1) {
                                referenceTimeStamp = milli;
                                milli = 0;
                            } else {
                                milli = (milli - referenceTimeStamp) / unit;
                            }
                            data.add(new CandleEntry((float) milli, high, low, open, close));


                            Log.d("lol2", Long.toString(milli));
                        }
                    }

                    Log.d("error","here1 " + data.size());
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
                    Log.d("error","here2");

                    CandleData data = new CandleData(set1);
                    Log.d("error","here3");
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
                RequestQueue queue = Volley.newRequestQueue(StocksScreen.this);
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
                RequestQueue queue = Volley.newRequestQueue(StocksScreen.this);

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
                RequestQueue queue = Volley.newRequestQueue(StocksScreen.this);
                queue.add(JsonParse());
            }
        });



    }
}
