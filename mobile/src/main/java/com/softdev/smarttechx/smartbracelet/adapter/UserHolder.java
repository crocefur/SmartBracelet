package com.softdev.smarttechx.smartbracelet.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.softdev.smarttechx.smartbracelet.R;

/**
 * Created by SMARTTECHX on 11/3/2017.
 */

public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    //OUR VIEWS

    TextView noTxt, nameTxt;

    ItemClickListener itemClickListener;


    public UserHolder(View itemView) {
        super(itemView);
        this.noTxt =  (TextView) itemView.findViewById(R.id.no);
        this.nameTxt =  (TextView) itemView.findViewById(R.id.name);
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
