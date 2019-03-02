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
import android.widget.TextView;
import android.widget.AutoCompleteTextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    final static String EXTRA_TEXT = "com.example.mystockwatch.EXTRA_TEXT";
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;

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

       /* AutoCompleteTextView searchBar = findViewById(R.id.searchBar);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1,symbols);
        searchBar.setAdapter(adapter); */
        final AppCompatAutoCompleteTextView autoCompleteTextView =
                findViewById(R.id.searchBar);
        final TextView selectedText = findViewById(R.id.selected_item);

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


                        Log.d("apple","never");
                        selectedText.setText(autoSuggestAdapter.getObject(position).getSymbol() + " | " + autoSuggestAdapter.getObject(position).getName());
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

    public void openStockInfo(StockObj stock){
        Intent intent = new Intent(this,StocksScreen.class);
        intent.putExtra("EXTRA_NAME",stock.getName());
        intent.putExtra("EXTRA_SYMBOL",stock.getSymbol());
        startActivity(intent);
    }

}
