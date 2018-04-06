package com.softdev.smarttechx.smartbracelet.adapter;

import android.widget.Filter;

import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.model.Userdata;

import java.util.ArrayList;

/**
 * Created by SMARTTECHX on 11/3/2017.
 */

public class UserFilter extends Filter {
    UserAdapter adapter;
    ArrayList<UserDetails> filterList;


    public UserFilter(ArrayList<UserDetails> filterList, UserAdapter adapter)
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
            ArrayList<UserDetails> filteredPlayers=new ArrayList<>();

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

        adapter.users= (ArrayList<UserDetails>) results.values;

        //REFRESH
        adapter.notifyDataSetChanged();
    }
}
