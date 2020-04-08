package com.example.naftech.todolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import BusinesObjects.CheckListItem;
import DatabaseLayer.DatabaseManager;

public class EditItemPage extends AppCompatActivity {

    private EditText itemName, prentName;
    private EditText priority;
    private EditText dueDate;
    private EditText dueTime;
    private Button confirmEdit;
    private Button cancelEdit;
    private TextView contextName;

    private DatabaseManager dbMan;
    private String fullDueDate, iName;
    private Calendar calendar;
    private Time time;
    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;
    private CheckListItem trgItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_page);

        dbMan = DatabaseManager.getInstance(this);
        Intent getItemName = getIntent();
        List<CheckListItem> data = new ArrayList<>();
        String parentName;
        //trgItem = new CheckListItem();

        iName = getItemName.getStringExtra("ItemName");
        parentName = getItemName.getStringExtra("ParentName");

        data.addAll(dbMan.getCheckListItems(parentName, null));
        //messageToastDisplay(EditItemPage.this, dbMan.getCheckListItems(parentName, null).size() + " ");
        for(CheckListItem item : data){
            if(item.getItemName().equals(iName)){
                trgItem = new CheckListItem(item);
                break;
            }
        }

        //trgItem = new CheckListItem(dbMan.getACheckListItem(iName));
        //messageToastDisplay(EditItemPage.this, dbMan.getACheckListItem(iName).getItemName());

        itemName = (EditText) findViewById(R.id.itemNameEditText);
        prentName = (EditText) findViewById(R.id.parentNameEditText);
        priority = (EditText) findViewById(R.id.priorityEditText);
        dueDate = (EditText) findViewById(R.id.dueDateEditText);
        dueTime = (EditText) findViewById(R.id.dueTimeEditText);
        confirmEdit = (Button) findViewById(R.id.editConfirmButton);
        cancelEdit = (Button) findViewById(R.id.cancelEditButton);
        contextName = (TextView) findViewById(R.id.parentNameTextView2);

        if(trgItem != null) {
            contextName.setText("Edit " + iName);
            itemName.setText(trgItem.getItemName());
            prentName.setText(trgItem.getItemParent());
            priority.setText(String.valueOf(trgItem.getPriority()));
            if(!trgItem.getDueDate().equals("")) {
                String[] dD = trgItem.getDueDate().split(" ");
                if(dD.length > 0) {
                    dueDate.setText(dD[0]);
                    if (dD.length == 2)
                        dueTime.setText(dD[1]);
                }
            }
        }
        else{
            messageToastDisplay("The item " + iName + " could not be found");
            returnToHomePage();
        }

        confirmEdit.setOnClickListener(onEditButtonClick);
        cancelEdit.setOnClickListener(onCancelButtonClick);
        dueDate.setOnClickListener(onDueDateEditTextClick);
        dueTime.setOnClickListener(onDueTimeEditTextClick);
    }

    private EditText.OnClickListener onDueTimeEditTextClick = new EditText.OnClickListener() {
        @Override
        public void onClick(View view) {
            calendar = Calendar.getInstance();

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            timePicker = new TimePickerDialog(EditItemPage.this, onTimeSetListener, hour, minute, true);
            timePicker.show();
        }
    };

    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int dHour, int dMin) {
            dueTime.setText(dHour + ":" + dMin + ":00");
        }
    };

    private EditText.OnClickListener onDueDateEditTextClick = new EditText.OnClickListener() {
        @Override
        public void onClick(View view) {
            calendar = Calendar.getInstance();

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            datePicker = new DatePickerDialog(EditItemPage.this, onDateSetListener, year, month, day);
            datePicker.show();
        }
    };

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int dYear, int dMonth, int dDay) {
            dueDate.setText((dMonth+1) + "/" + dDay + "/" + dYear);
        }
    };

    private Button.OnClickListener onEditButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            fullDueDate = dueDate.getText().toString() + " " + dueTime.getText().toString();
            //CheckListItem cLI = new CheckListItem();
            //cLI.setDueDate(fullDueDate);
            //cLI.setItemName(itemName.getText().toString());
            //cLI.setItemParent(trgItem.getItemParent());
            //if(priority.getText().toString().matches("[0-9]"))
            //    cLI.setPriority(Integer.valueOf(priority.getText().toString()));
            //else
                //cLI.setPriority(1);

            if(!trgItem.getItemName().equals(itemName.getText().toString()))
                dbMan.modifyCheckListItemName(trgItem, itemName.getText().toString());
//            if(!trgItem.getItemParent().equals(prentName.getText().toString()))
//                dbMan.modifyCheckListItemParent(trgItem, prentName.getText().toString());
            else if(!String.valueOf(trgItem.getPriority()).equals(String.valueOf(priority.getText().toString())))
                dbMan.modifyCheckListItemPriority(trgItem, String.valueOf(priority.getText().toString()));
            else if(!trgItem.getDueDate().equals(fullDueDate))
                dbMan.modifyCheckListItemDueDate(trgItem, fullDueDate);

            Intent homePg = new Intent(getApplicationContext(), MainPage.class);
            homePg.putExtra("ParentName", getPrevParent(trgItem));
            homePg.putExtra("ppN", trgItem.getItemParent());
            homePg.putExtra("NewParent", true);
            startActivity(homePg);

            messageToastDisplay( trgItem.getItemName() + " was Modified");
            //Ends this Activity
            finish();
        }
    };

    private Button.OnClickListener onCancelButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            Intent welcomePg = new Intent(getApplicationContext(), MainPage.class);
//            startActivity(welcomePg);
            Intent homePg = new Intent(getApplicationContext(), MainPage.class);
            homePg.putExtra("ParentName", getPrevParent(trgItem));
            homePg.putExtra("ppN", trgItem.getItemParent());
            homePg.putExtra("NewParent", true);
            startActivity(homePg);

            Toast.makeText(EditItemPage.this, "Item edit was canceled", Toast.LENGTH_LONG).show();
            //Ends this Activity
            finish();
        }
    };

    //*******************   Helper Methods   ***************************
    private String getPrevParent(CheckListItem tItem){
        String[] pLine = tItem.getItemParent().split(";");
        return pLine[pLine.length-1];
    }

    public void messageToastDisplay(String message){
        Toast.makeText(EditItemPage.this, message, Toast.LENGTH_LONG).show();
    }

    private void returnToHomePage(){
        Intent returnHP = new Intent(EditItemPage.this, MainPage.class);
        startActivity(returnHP);
    }
}
