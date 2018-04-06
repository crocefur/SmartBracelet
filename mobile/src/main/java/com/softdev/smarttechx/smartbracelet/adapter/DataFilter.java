package com.softdev.smarttechx.smartbracelet.adapter;

import android.widget.Filter;

import com.softdev.smarttechx.smartbracelet.model.Userdata;

import java.util.ArrayList;

/**
 * Created by SMARTTECHX on 11/3/2017.
 */

public class DataFilter extends Filter {
    DataAdapter adapter;
    ArrayList<Userdata> filterList;


    public DataFilter(ArrayList<Userdata> filterList,DataAdapter adapter)
    {
        this.adapter=adapter;
        this.filterList=filterList;

    }

    //FILTERING OCURS
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();

        //CHECK CONSTRAINT VALIDITY
        if(constraint != null && constraint.length() > 0)
        {
            //CHANGE TO UPPER
            constraint=constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<Userdata> filteredPlayers=new ArrayList<>();

            for (int i=0;i<filterList.size();i++)
            {
                //CHECK
                if(filterList.get(i).getName().toUpperCase().contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredPlayers.add(filterList.get(i));
                }
            }

            results.count=filteredPlayers.size();
            results.values=filteredPlayers;
        }else
        {
            results.count=filterList.size();
            results.values=filterList;

        }


        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.Bracedata= (ArrayList<Userdata>) results.values;

        //REFRESH
        adapter.notifyDataSetChanged();
    }
}
