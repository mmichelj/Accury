package com.example.caro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{


    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mNamesBT = new ArrayList<>();
    private Context mContext;
    private BluetoothAdapter mbtadapter;
    public ArrayList<BluetoothDevice> mBTDevices;

    public RecyclerViewAdapter(Context context, ArrayList<String> names, ArrayList<String> namesBT,BluetoothAdapter btadapter, ArrayList<BluetoothDevice> BTDevices) {
        mNames = names;
        mContext = context;
        mbtadapter = btadapter;
        mBTDevices =BTDevices;
        mNamesBT=namesBT;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        holder.name.setText(mNamesBT.get(position));
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(mContext,"Conectar a: " + mNames.get(position),Toast.LENGTH_LONG).show();
                mbtadapter.cancelDiscovery();
                Intent intent = new Intent(mContext,ConexionActivity.class);
                intent.putExtra("devices",mNames.get(position));
                intent.putExtra("position",position);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.image);
            name=itemView.findViewById(R.id.name);
        }
    }



}
