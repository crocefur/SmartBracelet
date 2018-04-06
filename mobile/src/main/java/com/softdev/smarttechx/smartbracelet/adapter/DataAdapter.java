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
import com.softdev.smarttechx.smartbracelet.model.Userdata;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by SMARTTECHX on 11/3/2017.
 */

public class DataAdapter extends RecyclerView.Adapter<DataHolder> implements Filterable {

    Context c;
    ArrayList<Userdata> Bracedata, filterList;
    DataFilter filter;
    double mcoData,grbike;
    String data, condata;

    private ItemClickListener Listener;
    private static DecimalFormat formatDis = new DecimalFormat("#.##");


    public DataAdapter(Context ctx, ArrayList<Userdata> Bracedata) {
        this.c = ctx;
        this.Bracedata = Bracedata;
        this.filterList = Bracedata;
    }


    @Override
    public DataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //CONVERT XML TO VIEW ONBJ
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.data_adapter, parent, false);

        //HOLDER
        DataHolder holder = new DataHolder(v);

        return holder;
    }

    //DATA BOUND TO VIEWS
    @Override
    public void onBindViewHolder(DataHolder holder, int position) {

        //BIND DATA
        holder.noTxt.setText(Bracedata.get(position).getNo());
        holder.nameTxt.setText(Bracedata.get(position).getName());
        if ((Bracedata.get(position).getData_type().equals("step"))) {
            data = Bracedata.get(position).getStepdata();
            mcoData = Double.valueOf(Bracedata.get(position).getStepdata());
            condata = String.valueOf(formatDis.format(mcoData /13));
            holder.dataRawTxt.setText(data);
            holder.labelTxtstep.setText("steps");
            holder.dataConTxt.setText(condata);

        } else {
            holder.dataRawTxt.setText(convert_millisec(Bracedata.get(position).getBikedata()));
           // mcoData = Double.valueOf(Bracedata.get(position).getStepdata());
            holder.labelTxtstep.setText("");
            grbike=Long.valueOf(Bracedata.get(position).getBikedata())/2400;
            condata = String.valueOf(formatDis.format(grbike));
            holder.dataConTxt.setText(condata);
        }

        //IMPLEMENT CLICK LISTENET
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                Context context = v.getContext();
                Intent userDetail = new Intent(context, UserActivity.class);
                userDetail.putExtra("name", Bracedata.get(pos).getName());
                userDetail.putExtra("email", Bracedata.get(pos).getEmail());
                userDetail.putExtra("firstname", Bracedata.get(pos).getFirstname());
                userDetail.putExtra("lastname", Bracedata.get(pos).getLastname());
                context.startActivity(userDetail);
            }
        });

    }

    public void clear(){
        if(getItemCount()>0){
            int size =this.Bracedata.size();
            this.Bracedata.clear();
            notifyItemRangeChanged(0,size);
        }
    }
    //GET TOTAL NUM OF PLAYERS
    @Override
    public int getItemCount() {
        return Bracedata.size();
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
            filter = new DataFilter(filterList, this);
        }

        return filter;
    }
}
