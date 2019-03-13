package com.example.mystockwatch;

import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AutoCompleteTextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    final static String EXTRA_TEXT = "com.example.mystockwatch.EXTRA_TEXT";
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;
    private RequestQueue queue;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        queue = Volley.newRequestQueue(this);
        queue.add(getGainers());
        queue.add(getLosers());
        setClickables();
        final AppCompatAutoCompleteTextView autoCompleteTextView =
                findViewById(R.id.searchBar);
        //final TextView selectedText = findViewById(R.id.selected_item);

        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        final TextView nameTextView = (TextView) findViewById(R.id.name);
        final TextView symbolTextView = findViewById(R.id.symbol);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);
        autoCompleteTextView.setOnItemClickListener(

                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                    //    selectedText.setText(autoSuggestAdapter.getObject(position).getSymbol() + " | " + autoSuggestAdapter.getObject(position).getName());
                        StockObj s = autoSuggestAdapter.getObject(position);
                        if (autoCompleteTextView.length() > 0) {
                            TextKeyListener.clear(autoCompleteTextView.getText());
                        }
                        openStockInfo(s);
                    }
                });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {

                    if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                        makeApiCall(autoCompleteTextView.getText().toString());
                    }
                }
                return false;
            }
        });
    }

    private void makeApiCall(String text) {

        ApiCall.make(this, text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                Log.d("apple4","calasdling");
                List<StockObj> stringList = new ArrayList<>();
                try {
                    JSONObject responseObject = new JSONObject(response);
                    Log.d("apple4","calling");
                    JSONArray array = responseObject.getJSONArray("bestMatches");
                    for (int i = 0; i < array.length(); i++) {
                        Log.d("applei","i= "+ i);
                        JSONObject row = array.getJSONObject(i);
                        String region = row.getString("4. region");
                        String currency = row.getString("8. currency");
                        if(region.equals("United States") && currency.equals("USD")){
                            stringList.add(new StockObj(row.getString("1. symbol"), row.getString("2. name")));
                          //  Log.d("added", row.getString("1. symbol")+ " " + row.getString("2. name"));
                        }

                    }
                    Log.d("apple4","calling done+  " + stringList.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //IMPORTANT: set data here and notify

                autoSuggestAdapter.setData(stringList);
                //Log.d("apple4","calasdling123");
                autoSuggestAdapter.notifyDataSetChanged();
                Log.d("apple4","calasdling1231234");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void openStockInfo(StockObj stock){
        Intent intent = new Intent(this,StocksScreen.class);
        intent.putExtra("EXTRA_NAME",stock.getName());
        intent.putExtra("EXTRA_SYMBOL",stock.getSymbol());
        startActivity(intent);
    }

    private StringRequest getGainers(){
        String url = "https://api.iextrading.com/1.0/stock/market/list/gainers?displayPercent=true";
        StringRequest request =  new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    List<StockObj> gainers = new ArrayList<StockObj>();

                    JSONArray arr = new JSONArray(response);

                    for(int i = 0; i < arr.length(); i++){
                        JSONObject obj = arr.getJSONObject(i);
                        String name  = obj.getString("companyName");
                        String symbol = obj.getString("symbol");
                        double percentChange = obj.getDouble("changePercent");
                        gainers.add(new StockObj(symbol,name,percentChange));
                    }

                    Collections.sort(gainers);
                    for(StockObj a : gainers){
                        Log.d("gainers", a.getName() + " " + a.getPercentChange());
                    }

                    for(int j = 0; j < gainers.size(); j ++){
                        int i = j+ 1;
                        String id = "g" + i +"n";
                        int resID = getResources().getIdentifier(id, "id", getPackageName());
                        Log.d("gainers", resID + " id");
                        TextView name = (TextView) findViewById(resID);
                        id = "g" + i +"s";
                        resID = getResources().getIdentifier(id, "id", getPackageName());
                        TextView symbol = (TextView) findViewById(resID);
                        id = "g" + i +"pc";
                        resID = getResources().getIdentifier(id, "id", getPackageName());
                        TextView pc = (TextView) findViewById(resID);

                        name.setText(gainers.get(j).getName());
                        symbol.setText(gainers.get(j).getSymbol());
                        double roundOff = (double) Math.round(gainers.get(j).getPercentChange() * 100) / 100;
                        pc.setText("+"+roundOff+"%");
                    }


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
    private StringRequest getLosers(){
        String url = "https://api.iextrading.com/1.0/stock/market/list/losers?displayPercent=true";
        StringRequest request =  new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    List<StockObj> losers = new ArrayList<StockObj>();

                    JSONArray arr = new JSONArray(response);

                    for(int i = 0; i < arr.length(); i++){
                        JSONObject obj = arr.getJSONObject(i);
                        String name  = obj.getString("companyName");
                        String symbol = obj.getString("symbol");
                        double percentChange = obj.getDouble("changePercent");
                        losers.add(new StockObj(symbol,name,percentChange));
                    }

                    Collections.sort(losers);
                    Collections.reverse(losers);

                    for(StockObj a : losers){
                        Log.d("gainers", a.getName() + " " + a.getPercentChange());
                    }

                    for(int j = 0; j < losers.size(); j ++){
                        int i = j+ 1;
                        String id = "l" + i +"n";
                        int resID = getResources().getIdentifier(id, "id", getPackageName());
                        Log.d("gainers", resID + " id");
                        TextView name = (TextView) findViewById(resID);
                        id = "l" + i +"s";
                        resID = getResources().getIdentifier(id, "id", getPackageName());
                        TextView symbol = (TextView) findViewById(resID);
                        id = "l" + i +"pc";
                        resID = getResources().getIdentifier(id, "id", getPackageName());
                        TextView pc = (TextView) findViewById(resID);

                        name.setText(losers.get(j).getName());
                        symbol.setText(losers.get(j).getSymbol());
                        double roundOff = (double) Math.round(losers.get(j).getPercentChange() * 100) / 100;
                        pc.setText("-"+roundOff+"%");
                    }


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
    private void setClickables(){
        for(int i = 1 ; i <= 10 ; i++){
            int resID = getResources().getIdentifier("g"+i, "id", getPackageName());
            LinearLayout button = (LinearLayout)findViewById(resID);
            resID = getResources().getIdentifier("g"+i+"n", "id", getPackageName());
            TextView name = (TextView)findViewById(resID);
            resID = getResources().getIdentifier("g"+i+"s", "id", getPackageName());
            TextView symbol = (TextView)findViewById(resID);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openStockInfo(new StockObj(symbol.getText().toString(),name.getText().toString()));
                }
            });

        }
        for(int i = 1 ; i <= 10 ; i++){
            int resID = getResources().getIdentifier("l"+i, "id", getPackageName());
            LinearLayout button = (LinearLayout)findViewById(resID);
            resID = getResources().getIdentifier("l"+i+"n", "id", getPackageName());
            TextView name = (TextView)findViewById(resID);
            resID = getResources().getIdentifier("l"+i+"s", "id", getPackageName());
            TextView symbol = (TextView)findViewById(resID);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openStockInfo(new StockObj(symbol.getText().toString(),name.getText().toString()));
                }
            });

        }

    }

}
