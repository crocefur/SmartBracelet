package com.softdev.smarttechx.smartbracelet.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.softdev.smarttechx.smartbracelet.R;
import com.softdev.smarttechx.smartbracelet.UserActivity;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;
import com.softdev.smarttechx.smartbracelet.model.UserDetails;

import java.util.ArrayList;

/**
 * Created by SMARTTECHX on 11/3/2017.
 */

public class UserAdapter extends RecyclerView.Adapter<UserHolder> implements Filterable {

    Context c;
    ArrayList<UserDetails> users, filterList;
    UserFilter filter;
    String name, no;
    private ItemClickListener Listener;


    public UserAdapter(Context ctx, ArrayList<UserDetails> users) {
        this.c = ctx;
        this.users = users;
        this.filterList = users;
    }


    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //CONVERT XML TO VIEW ONBJ
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_adapter, parent, false);

        //HOLDER
       UserHolder holder = new UserHolder(v);

        return holder;
    }

    //DATA BOUND TO VIEWS
    @Override
    public void onBindViewHolder(UserHolder holder, int position) {

        //BIND DATA
        holder.noTxt.setText(users.get(position).getSNO());
        holder.nameTxt.setText(users.get(position).getName());

        //IMPLEMENT CLICK LISTENET
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                Context context = v.getContext();
                v.setTag(context);
                Intent userDetail = new Intent(context, UserActivity.class);
                userDetail.putExtra("name", users.get(pos).getName());
                userDetail.putExtra("email", users.get(pos).getEmail());
                context.startActivity(userDetail);
            }
        });

    }

    public void clear(){
        if(getItemCount()>0){
            int size =this.users.size();
            this.users.clear();
            notifyItemRangeChanged(0,size);
        }
    }
    //GET TOTAL NUM OF PLAYERS
    @Override
    public int getItemCount() {
        return users.size();
    }
    private String convert_millisec(String millis) {
        long milliseconds= Long.valueOf(millis);
        String bike_data;
        int minutes = (int) (((milliseconds / 1000) / 60) % 60);
        int hours = (int) (((milliseconds / 1000) / 3600) % 24);
        bike_data = String.valueOf(hours < 10 ? "0" + hours : hours) + "hr:" + String.valueOf("" + (minutes < 10 ? "0" + minutes : minutes)) + "min";
        return bike_data;
    }

    //RETURN FILTER OBJ
    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new UserFilter(filterList, this);
        }

        return filter;
    }
}
