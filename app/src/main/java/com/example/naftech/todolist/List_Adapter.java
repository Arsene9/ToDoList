package com.example.naftech.todolist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import BusinesObjects.CheckListItem;
import DatabaseLayer.DatabaseManager;

public class List_Adapter extends ArrayAdapter<CheckListItem> {

    private CheckListItem item;
    private boolean isReturnTo;


    public List_Adapter(@NonNull Context context, int resource, @NonNull List<CheckListItem> objects, boolean isReturnTo) {
        super(context, resource, objects);
        this.isReturnTo = isReturnTo;
    }

    @NonNull
    @Override
    public View getView( int position, View convertview,  ViewGroup parent){
        View v = convertview;
        boolean checked = false;
        final int pos = position;
        final ViewGroup par = parent;

        if(null == v){
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.check_list_item, null);
        }

        item = getItem(position);

        if(item.getStatus().equals("Complete"))
            checked = true;

        CheckBox listItem = (CheckBox) v.findViewById(R.id.itemCheckBox);

        if(isReturnTo) {
            if(item.getItemName().equals("None"))
                listItem.setText("Home");
            else
                listItem.setText(item.getItemName());
        }
        else
            listItem.setText(item.getItemName());
        listItem.setChecked(checked);
        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) par).performItemClick(v, pos, 0); // Let the event be handled in onItemClick());
            }
        });
        return v;
    }
}
