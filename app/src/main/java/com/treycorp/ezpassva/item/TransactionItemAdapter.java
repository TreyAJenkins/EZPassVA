package com.treycorp.ezpassva.item;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.treycorp.ezpassva.R;

import org.fabiomsr.moneytextview.MoneyTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TransactionItemAdapter extends RecyclerView.Adapter<TransactionItemAdapter.ViewHolder> {

    private ArrayList<HashMap> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public TransactionItemAdapter(Context context, ArrayList<HashMap> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //String title = mData.get(position);
        HashMap arr = mData.get(position);

        holder.titleTransaction.setText(arr.get("title").toString());
        holder.bodyTransaction.setText(arr.get("body").toString());
        holder.transactionTollPaid.setAmount(Float.valueOf(arr.get("toll").toString()));
        holder.datePosted.setText(arr.get("posted").toString());
        holder.transactionBalance.setAmount(Float.valueOf(arr.get("balance").toString()));

        //#388e3c
        if (holder.transactionTollPaid.getAmount() == 0) {
            holder.transactionTollPaid.setBaseColor(Color.parseColor("#555555"));
            holder.transactionTollPaid.setDecimalsColor(Color.parseColor("#555555"));
            holder.transactionTollPaid.setSymbolColor(Color.parseColor("#555555"));
        } else if (holder.transactionTollPaid.getAmount() > 0) {
            holder.transactionTollPaid.setBaseColor(Color.parseColor("#388e3c"));
            holder.transactionTollPaid.setDecimalsColor(Color.parseColor("#388e3c"));
            holder.transactionTollPaid.setSymbolColor(Color.parseColor("#388e3c"));
        } else {
            holder.transactionTollPaid.setBaseColor(Color.parseColor("#B00020"));
            holder.transactionTollPaid.setDecimalsColor(Color.parseColor("#B00020"));
            holder.transactionTollPaid.setSymbolColor(Color.parseColor("#B00020"));
        }

        if (holder.transactionBalance.getAmount() < 0) {
            holder.transactionBalance.setBaseColor(Color.parseColor("#B00020"));
            holder.transactionBalance.setDecimalsColor(Color.parseColor("#B00020"));
            holder.transactionBalance.setSymbolColor(Color.parseColor("#B00020"));
        }

        //6/15/2019 3:49:19 PM
        String originalStringFormat = "M/d/yyyy h:mm:ss a";
        String desiredStringFormat = "MM/dd/yyyy h:mm a";

        SimpleDateFormat readingFormat = new SimpleDateFormat(originalStringFormat);
        SimpleDateFormat outputFormat = new SimpleDateFormat(desiredStringFormat);

        Date entryDT = null;
        Date exitDT = null;

        //arr.put("entryDT", "9/9/2018 7:59:21 PM");
        if (arr.get("entryDT").toString().length() > 0) {
            try {
                entryDT = readingFormat.parse(arr.get("entryDT").toString());
                //outputFormat.format(entryDT);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (arr.get("exitDT").toString().length() > 0) {
            try {
                exitDT = readingFormat.parse(arr.get("exitDT").toString());
                //Log.d("ExitDT", outputFormat.format(exitDT));
                //outputFormat.format(entryDT);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if ((entryDT != null) && (exitDT != null)) {
            holder.entryDateTime.setText(outputFormat.format(entryDT));
            holder.exitDateTime.setText(outputFormat.format(exitDT));

            long diff = exitDT.getTime() - entryDT.getTime();
            int diffMinutes = (int) (diff / (60 * 1000) % 60);

            holder.elapsedTime.setText(Integer.toString(diffMinutes) + " MIN");

        } else {
            holder.elapsedLayout.setVisibility(View.GONE);
        }

        holder.transactionTollPaid.setAmount(Math.abs(holder.transactionTollPaid.getAmount()));
        holder.transactionBalance.setAmount(Math.abs(holder.transactionBalance.getAmount()));
        if (holder.bodyTransaction.length() == 0) {
            holder.bodyTransaction.setVisibility(View.GONE);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTransaction;
        TextView bodyTransaction;
        MoneyTextView transactionTollPaid;
        TextView datePosted;
        MoneyTextView transactionBalance;

        TextView entryDateTime;
        TextView exitDateTime;
        TextView elapsedTime;

        ConstraintLayout elapsedLayout;

        ViewHolder(View itemView) {
            super(itemView);
            titleTransaction = itemView.findViewById(R.id.titleTransaction);
            bodyTransaction = itemView.findViewById(R.id.bodyTransaction);
            transactionTollPaid = itemView.findViewById(R.id.transactionTollPaid);
            datePosted = itemView.findViewById(R.id.transactionDatePosted);
            transactionBalance = itemView.findViewById(R.id.transactionBalance);
            entryDateTime = itemView.findViewById(R.id.entryDateTime);
            exitDateTime = itemView.findViewById(R.id.exitDateTime);
            elapsedTime = itemView.findViewById(R.id.elapsedTimeTransaction);
            elapsedLayout = itemView.findViewById(R.id.elapsedLayout);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    HashMap getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}