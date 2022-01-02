package com.example.myapplication1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class weatherRVAdapter extends RecyclerView.Adapter<weatherRVAdapter.ViewHolder> {
    private Context context;
    private ArrayList<wetherRVModel> wetherRVModelArrayList;

    public weatherRVAdapter(Context context, ArrayList<wetherRVModel> wetherRVModelArrayList) {
        this.context = context;
        this.wetherRVModelArrayList = wetherRVModelArrayList;
    }

    @NonNull
    @Override
    public weatherRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.weather_rv_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull weatherRVAdapter.ViewHolder holder, int position) {
        wetherRVModel model=wetherRVModelArrayList.get(position);
        holder.temprtureTV.setText(model.getTemperature()+"°c");
        Picasso.get().load("http:".concat(model.getIcon())).into(holder.conditioniv);
        holder.windTV.setText(model.getWindspeed()+"Km/h");
        SimpleDateFormat input=new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output=new SimpleDateFormat("hh:mm aa");
        try{
            Date t=input.parse(model.getTime());
            holder.timeTV.setText(output.format(t));
        }catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return wetherRVModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView windTV,temprtureTV,timeTV;
        private ImageView conditioniv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            windTV=itemView.findViewById(R.id.tvwindspeed);
            temprtureTV=itemView.findViewById(R.id.temprature);
            conditioniv=itemView.findViewById(R.id.icon_conditon);
            timeTV=itemView.findViewById(R.id.time);
        }
    }
}
