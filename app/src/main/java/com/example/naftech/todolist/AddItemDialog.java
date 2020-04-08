package com.example.naftech.todolist;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

public class AddItemDialog extends DialogFragment implements View.OnClickListener {

    private RadioButton newItem, subItem;
    private Button confirmAdd, cancelAdd;

    private String pName, ppN;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View addItemDialog = inflater.inflate(R.layout.item_add_option,null);

        pName = getArguments().getString("ParentName", "None");
        ppN = getArguments().getString("ppN", "None");

        confirmAdd = (Button) addItemDialog.findViewById(R.id.answerConfirmAddButton);
        cancelAdd = (Button) addItemDialog.findViewById(R.id.answerCancelAddButton);
        newItem = (RadioButton) addItemDialog.findViewById(R.id.newAddRadioButton);
        subItem = (RadioButton) addItemDialog.findViewById(R.id.subAddRadioButton);

        //Toast.makeText(getActivity(), pName, Toast.LENGTH_LONG).show();

        confirmAdd.setOnClickListener(this);
        cancelAdd.setOnClickListener(this);

        setCancelable(false);
        return addItemDialog;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.answerConfirmAddButton){
            if(newItem.isChecked()){
                Intent addItem = new Intent(getContext(), AddItemPage.class);
                addItem.putExtra("Parent", pName);
                addItem.putExtra("ppN", ppN);
                startActivity(addItem);

                Toast.makeText(getActivity(), "Adding a new item to this List", Toast.LENGTH_LONG).show();
                dismiss();
            }
            else if (subItem.isChecked()){
                Intent homePg = new Intent(getContext(), MainPage.class);
                homePg.putExtra("Add a sub item", true);
                homePg.putExtra("ParentName", pName);
                homePg.putExtra("ppN", ppN);
                startActivity(homePg);
                //Toast.makeText(getActivity(), "Adding a new item to an existing item", Toast.LENGTH_LONG).show();
                dismiss();
            }
            else{
                newItem.setBackgroundColor(Color.parseColor("#F5C5C5"));
                subItem.setBackgroundColor(Color.parseColor("#F5C5C5"));

                Toast.makeText(getActivity(), "Please select one of the two add options", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Intent homePg = new Intent(getContext(), MainPage.class);
            Toast.makeText(getActivity(), "Item add was canceled", Toast.LENGTH_LONG).show();
            homePg.putExtra("ParentName", pName);
            homePg.putExtra("ppN", ppN);
            startActivity(homePg);
            dismiss();
        }
    }
}
