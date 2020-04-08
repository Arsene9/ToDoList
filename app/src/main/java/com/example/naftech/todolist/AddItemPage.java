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
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.Calendar;

import BusinesObjects.CheckListItem;
import DatabaseLayer.DatabaseManager;

public class AddItemPage extends AppCompatActivity {

    private EditText itemName;
    private EditText priority;
    private EditText dueDate;
    private EditText dueTime;
    private Button confirmAdd;
    private Button cancelAdd;

    private DatabaseManager dbMan;
    private String fullDueDate;
    private Calendar calendar;
    private Time time;
    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;
    private String parentName = "", ppN = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_to_list_page);

        dbMan = DatabaseManager.getInstance(this);
        Intent mIntent = getIntent();

        parentName = mIntent.getStringExtra("Parent");
        ppN = mIntent.getStringExtra("ppN");

        if(parentName.equals(""))
            parentName = "None";

        itemName = (EditText) findViewById(R.id.itemNameEditText);
        priority = (EditText) findViewById(R.id.priorityEditText);
        dueDate = (EditText) findViewById(R.id.dueDateEditText);
        dueTime = (EditText) findViewById(R.id.dueTimeEditText);
        confirmAdd = (Button) findViewById(R.id.confirmAddButton);
        cancelAdd = (Button) findViewById(R.id.cancelAddButton);

        confirmAdd.setOnClickListener(onAddButtonClick);
        cancelAdd.setOnClickListener(onCancelButtonClick);
        dueDate.setOnClickListener(onDueDateEditTextClick);
        dueTime.setOnClickListener(onDueTimeEditTextClick);
    }

    private EditText.OnClickListener onDueTimeEditTextClick = new EditText.OnClickListener() {
        @Override
        public void onClick(View view) {
            calendar = Calendar.getInstance();

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            timePicker = new TimePickerDialog(AddItemPage.this, onTimeSetListener, hour, minute, true);
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
            datePicker = new DatePickerDialog(AddItemPage.this, onDateSetListener, year, month, day);
            datePicker.show();
        }
    };

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int dYear, int dMonth, int dDay) {
            dueDate.setText((dMonth+1) + "/" + dDay + "/" + dYear);
        }
    };

    private Button.OnClickListener onAddButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            fullDueDate = dueDate.getText().toString() + " " + dueTime.getText().toString();
            CheckListItem cLI = new CheckListItem();
            cLI.setDueDate(fullDueDate);
            cLI.setItemName(itemName.getText().toString());
            cLI.setItemParent(ppN);//("None");

            if(priority.getText().toString().matches("[0-9]"))
                cLI.setPriority(Integer.valueOf(priority.getText().toString()));
            else
                cLI.setPriority(1);
            //messageToastDisplay(AddItemPage.this, "Entered: " + priority.getText() +
            //        " Stored: " + cLI.getPriority());
            cLI.setStatus("Incomplete");
            dbMan.insertChecklistItem(cLI);

            Intent welcomePg = new Intent(getApplicationContext(), MainPage.class);
            welcomePg.putExtra("ParentName", parentName);
            welcomePg.putExtra("ppN", ppN);
            welcomePg.putExtra("NewParent", true);
            startActivity(welcomePg);

            Toast.makeText(AddItemPage.this, "New Item was added to " + parentName, Toast.LENGTH_LONG).show();
            //Ends this Activity
            finish();
        }
    };

    private Button.OnClickListener onCancelButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent welcomePg = new Intent(getApplicationContext(), MainPage.class);
            startActivity(welcomePg);

            Toast.makeText(AddItemPage.this, "Item add was canceled", Toast.LENGTH_LONG).show();
            //Ends this Activity
            finish();
        }
    };

    //**************************   Helper Methods   ****************************
    public void messageToastDisplay(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
