package com.softdev.smarttechx.smartbracelet.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.softdev.smarttechx.smartbracelet.R;
import com.softdev.smarttechx.smartbracelet.UserActivity;

/**
 * Created by SMARTTECHX on 11/3/2017.
 */

public class DataHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    //OUR VIEWS

    TextView noTxt, nameTxt,dataRawTxt,dataConTxt, labelTxt,labelTxtstep;

    ItemClickListener itemClickListener;


    public DataHolder(View itemView) {
        super(itemView);
        this.noTxt =  (TextView) itemView.findViewById(R.id.no);
        this.nameTxt =  (TextView) itemView.findViewById(R.id.name);
        this.dataRawTxt= (TextView) itemView.findViewById(R.id.rawdata);
        this.dataConTxt= (TextView) itemView.findViewById(R.id.condata);
        this.labelTxt=(TextView) itemView.findViewById(R.id.label);
        this.labelTxtstep=(TextView) itemView.findViewById(R.id.labelstep);
        labelTxt.setText(Html.fromHtml("grCO2"));
        labelTxtstep.setText("steps");
        itemView.setTag(itemView);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        this.itemClickListener.onItemClick(v ,getLayoutPosition());
    }

    public void setItemClickListener(ItemClickListener ic)
    {
        this.itemClickListener=ic;
    }
}
