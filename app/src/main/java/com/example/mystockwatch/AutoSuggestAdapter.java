package com.example.mystockwatch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class AutoSuggestAdapter extends ArrayAdapter<String> implements Filterable {
    private List<StockObj> mlistData;

    public AutoSuggestAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mlistData = new ArrayList<>();
    }

    public void setData(List<StockObj> list) {
        mlistData.clear();
        mlistData.addAll(list);
    }

    @Override
    public int getCount() {
        return mlistData.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return mlistData.get(position).getSymbol() + " | " + mlistData.get(position).getName();
    }

    /**
     * Used to Return the full object directly from adapter.
     *
     * @param position
     * @return
     */
    public StockObj getObject(int position) {
        return mlistData.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter dataFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    Log.d("apple","happayhappy)");
                    filterResults.values = mlistData;
                    filterResults.count = mlistData.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && (results.count > 0)) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
                Log.d("apple","happayhappysadsad)");
            }
        };
        return dataFilter;
    }
}