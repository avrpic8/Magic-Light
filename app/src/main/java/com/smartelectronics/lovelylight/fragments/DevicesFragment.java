package com.smartelectronics.lovelylight.fragments;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.smartelectronics.lovelylight.R;
import com.smartelectronics.lovelylight.interfaces.DeviceClick;
import com.smartelectronics.lovelylight.utils.bluetooth.BluetoothControl;

import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class DevicesFragment extends Fragment{

    private View view;

    private BluetoothControl btControl;
    private DevicesArrayAdapter adapter;
    private DeviceClick btClick;

    public DevicesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_devices, container, false);

        init();

        return view;
    }

    private void init(){

        btControl = new BluetoothControl(getActivity());
        initList(this.view);

    }
    private void initList(View view){

        // init listener
        btClick = (DeviceClick) getContext();

        adapter = new DevicesArrayAdapter(getContext(), R.layout.bt_device_items);
        ListView listView = view.findViewById(R.id.bt_list);
        listView.setAdapter(adapter);

        // init listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                BluetoothDevice device = adapter.getItem(position);
                btClick.onDeviceClick(device);
            }
        });

        Set<BluetoothDevice> paredDevices = this.btControl.getParedDevices();
        if(paredDevices.size() > 0){

            for(BluetoothDevice device: paredDevices)
                adapter.add(device);

            return;
        }

    }

    private class DevicesArrayAdapter extends ArrayAdapter<BluetoothDevice>{

        private int layout_resource;
        private Integer selected_position = -1;

        TextView txtBtName, txtBtMac;

        public DevicesArrayAdapter(@NonNull Context context, int resource) {
            super(context, 0);
            this.layout_resource = resource;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if(convertView== null)
                convertView = getLayoutInflater().inflate(this.layout_resource,parent,false);

            BluetoothDevice temp_dev = getItem(position);
            txtBtName = convertView.findViewById(R.id.bt_name);
            txtBtMac  = convertView.findViewById(R.id.bt_mac);

            txtBtName.setText(temp_dev.getName());
            txtBtMac.setText(temp_dev.getAddress());

            return convertView;
        }
    }

}
