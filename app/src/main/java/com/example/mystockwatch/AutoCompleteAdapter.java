package com.example.mystockwatch;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable
{
    private ArrayList<String> data;
    private final String server2 = "http://myserver/script.php?query=";
    private final String server = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&symbol=";
    private final String key = "&apikey=P1CWMMTE0GLRFBYL";
    AutoCompleteAdapter (@NonNull Context context, @LayoutRes int resource)
    {
        super (context, resource);
        this.data = new ArrayList<>();
    }

    @Override
    public int getCount()
    {
        return data.size();
    }

    @Nullable
    @Override
    public String getItem (int position)
    {
        return data.get (position);
    }

    @NonNull
    @Override
    public Filter getFilter()
    {
        return new Filter()
        {
            @Override
            protected FilterResults performFiltering (CharSequence constraint)
            {
                FilterResults results = new FilterResults();

                if (constraint != null)
                {
                    Log.d("input2",constraint.toString());
                    HttpURLConnection conn = null;
                    InputStream input = null;
                    try
                    {
                        Log.d("input",constraint.toString());
                        URL url = new URL (server + constraint.toString()+ key);
                        conn = (HttpURLConnection) url.openConnection();
                        input = conn.getInputStream();
                        InputStreamReader reader = new InputStreamReader (input, "UTF-8");
                        BufferedReader buffer = new BufferedReader (reader, 8192);
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = buffer.readLine()) != null)
                        {
                            builder.append (line);
                        }
                        JSONArray terms = new JSONArray (builder.toString());
                        ArrayList<String> suggestions = new ArrayList<>();
                        for (int ind = 0; ind < terms.length(); ind++)
                        {
                            JSONObject temp = terms.getJSONObject(ind);
                            String term = temp.getString ("1. symbol");
                            suggestions.add (term);
                        }
                        results.values = suggestions;
                        results.count = suggestions.size();
                        data = suggestions;
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    finally
                    {
                        if (input != null)
                        {
                            try
                            {
                                input.close();
                            }
                            catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                        if (conn != null) conn.disconnect();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults (CharSequence constraint, FilterResults results)
            {
                if (results != null && results.count > 0)
                {
                    notifyDataSetChanged();
                }
                else notifyDataSetInvalidated();
            }
        };
    }}