package com.example.naftech.todolist;

import android.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.CalendarContract;
import androidx.annotation.RequiresApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import BusinesObjects.CheckListItem;
import DatabaseLayer.DatabaseManager;

public class MainPage extends AppCompatActivity {
    private static final String TAG = "<===> ToDo List <===>";
    private ViewStub checkListViewStub;
    private ListView checkListView;
    private List_Adapter checkListAdapter;
    private FloatingActionButton addItemFB;
    private Toolbar toolbar;
    private TextView contextName, orderbyTextView;
    private AutoCompleteTextView orderbyOptions;
    private MenuItem addItem, cancelAction, confirmDel, backHome, backupData, restoreData, stepBack;

    private List<CheckListItem> itemList, deleteList, returnToList;
    private final String[] orderbyList = {"Entry Order", "Item Name", "Priority Number", "Completion Status", "Due Date"};
    private DatabaseManager dbMan;
    private final String mainPageName = "Home";
    private String listName = "";
    private CheckListItem currentParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_list_page);

        dbMan = DatabaseManager.getInstance(this);
        itemList = new ArrayList<>();
        deleteList = new ArrayList<>();
        returnToList = new ArrayList<>();
        currentParent = new CheckListItem();
        Intent anIntent = getIntent();
        //Intent subItemStart = getIntent();

        //boolean subItemFlag = subItemStart.getBooleanExtra("Start SubItem", false);
        boolean addSubItem = anIntent.getBooleanExtra("Add a sub item", false);
        boolean newParent = anIntent.getBooleanExtra("NewParent", false);

        checkListViewStub = findViewById(R.id.checkListViewStub);
        checkListViewStub.inflate();
        checkListViewStub.canScrollVertically(1);
        checkListView = findViewById(R.id.checkListViewList);
        addItemFB = findViewById(R.id.addItemFloatingActionButton);
        contextName = findViewById(R.id.parentNameTextView);
        orderbyTextView = findViewById(R.id.orderbyTextView);
        orderbyOptions = findViewById(R.id.orderbyAutoCompleteTextView);

        if(addSubItem) {
            listName = "Sub Item Add";
            contextName.setText(listName);
            setCurrentParent(anIntent.getStringExtra("ParentName"),anIntent.getStringExtra("ppN"));
            updateReturnList(currentParent.getItemParent());
            messageToastDisplay("Please select the item you which to add a sub item to");
        }
        else if(newParent) {
            setCurrentParent(anIntent.getStringExtra("ParentName"),anIntent.getStringExtra("ppN"));
            updateReturnList(currentParent.getItemParent());
            contextName.setText(currentParent.getItemName());
        }
        else {
            setCurrentParent("None","None");
            updateReturnList(currentParent.getItemParent());
            contextName.setText(mainPageName);
        }
        toolbar = findViewById(R.id.contextToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);


        GenerateListView(currentParent.getItemParent(), null);
        addItemFB.setOnClickListener(onAddItemClick);

        if(!addItemFB.isShown()){
            addItemFB.setVisibility(View.VISIBLE);
        }
        //messageToastDisplay(currentParent.getItemParent());

        orderbyOptions.setOnClickListener(onOrderbyClick);
        orderbyOptions.setOnItemClickListener(onOderbyItemClick);

        ArrayAdapter<String> orderlist = new ArrayAdapter<String>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, orderbyList);

        orderbyOptions.setAdapter(orderlist); //Attaches the dropdown list to AutocompleteTextView
        orderbyOptions.setThreshold(0);
        orderbyOptions.setDropDownWidth(400);
    }

    //*************************************   Menu setup  ******************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.action_menu_main, menu);
        confirmDel = menu.findItem(R.id.confirmDeleteItemAction);
        cancelAction = menu.findItem(R.id.cancelAction);
        backHome = menu.findItem(R.id.backHomeAction);
        addItem = menu.findItem(R.id.addItemAction);
        restoreData = menu.findItem(R.id.restoreDataAction);
        backupData = menu.findItem(R.id.backupDataAction);
        stepBack = menu.findItem(R.id.oneStepBackReturnAction);
        cancelAction.setVisible(false);
        confirmDel.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem Item){
        if(Item.getItemId() == R.id.addItemAction){
            showAddItemDialog();
        }
        else if(Item.getItemId() == R.id.deleteItemAction){
            listName = "Delete";
            contextName.setText(listName);
            addItemFB.setVisibility(View.INVISIBLE);
            addItem.setVisible(false);
            cancelAction.setVisible(true);
            confirmDel.setVisible(true);

            Toast.makeText(getApplicationContext(), "Select the items you wish to delete",
                    Toast.LENGTH_LONG).show();
        }
        else if(Item.getItemId() == R.id.editItemAction){
            listName = "Edit Item";
            contextName.setText(listName);
            addItemFB.setVisibility(View.INVISIBLE);
            cancelAction.setVisible(true);
            addItem.setVisible(false);
            messageToastDisplay("Please select the Item you wish to modify");
        }
        else if(Item.getItemId() == R.id.cancelAction){
            if(!currentParent.getItemName().equals("None"))
                contextName.setText(currentParent.getItemName());
            else
                contextName.setText(mainPageName);
            addItemFB.setVisibility(View.VISIBLE);
            cancelAction.setVisible(false);
            confirmDel.setVisible(false);
            addItem.setVisible(true);
            deleteList.clear();
            GenerateListView(currentParent.getItemParent(), null);
        }
        else if(Item.getItemId() == R.id.confirmDeleteItemAction){
            showAlertDialog();
        }
        else if(Item.getItemId() == R.id.returnToAction){
            listName = "Return Options";
            contextName.setText(listName);
            addItemFB.setVisibility(View.GONE);
            checkListAdapter = new List_Adapter(this, R.id.itemCheckBox, returnToList, true);
            checkListView.setAdapter(checkListAdapter);
            checkListView.setOnItemClickListener(onItemClick);
        }
        else if(Item.getItemId() == R.id.resetAction){
            for(CheckListItem i : itemList) {
                if(!dbMan.resetCheckListItem(i)) {
                    messageToastDisplay("It didn't seem to have worked");
                    break;
                }
            }
            GenerateListView(currentParent.getItemParent(), null);
        }
        else if(Item.getItemId() == R.id.restoreDataAction){
            restoreDatabase("ToDoList");
        }
        else if(Item.getItemId() == backupData.getItemId()){//R.id.backupDataAction){
            copyDatabase("ToDoList.db");
        }
        else if(Item.getItemId() == stepBack.getItemId()){
            for(int i = returnToList.size() - 1; i >= 0; i-- ){
                if(currentParent.getItemName().equals(returnToList.get(i).getItemName())){
                    if(i > 1){
                        currentParent = returnToList.get(--i);
                        contextName.setText(currentParent.getItemName());
                        GenerateListView(currentParent.getItemParent(), null);
                    }else{
                        currentParent = returnToList.get(0);
                        contextName.setText(mainPageName);
                        GenerateListView(currentParent.getItemParent(), null);
                    }
                    addItemFB.setVisibility(View.VISIBLE);
                }
            }
        }
        else{
            setCurrentParent("None", "None");
            //updateReturnList(currentParent.getItemParent());
            GenerateListView(currentParent.getItemParent(), null);
            contextName.setText(mainPageName);
        }
        return true;
    }
    //******************************   On Click Listeners setup  ***********************************
    // On Item click listener
    private final AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(confirmDel.isVisible()){
                String selectColor = "#FEC6BC";
                String unselectedCol = "#2196F3"; //R.color.checlListItemColor;

                if(deleteList.contains(itemList.get(position))) {
                    deleteList.remove(itemList.get(position));
                    listName = "Delete " + deleteList.size();
                    contextName.setText(listName);
                    view.setBackgroundColor(Color.parseColor(unselectedCol));
                }
                else {
                    deleteList.add(itemList.get(position));
                    listName = "Delete " + deleteList.size();
                    contextName.setText(listName);
                    view.setBackgroundColor(Color.parseColor(selectColor));
                }
            }
            else if(contextName.getText().equals("Edit Item")){
                Intent goToEditPg = new Intent(MainPage.this, EditItemPage.class);
                goToEditPg.putExtra("ItemName", itemList.get(position).getItemName());
                goToEditPg.putExtra("ParentName",itemList.get(position).getItemParent());
                //messageToastDisplay(itemList.get(position).getItemParent());
                startActivity(goToEditPg);
            }
            else if(contextName.getText().equals("Sub Item Add")){
                Intent addItem = new Intent(MainPage.this, AddItemPage.class);
                addItem.putExtra("Parent", itemList.get(position).getItemName());
                addItem.putExtra("ppN", itemList.get(position).getItemParent()
                        + ";" + itemList.get(position).getItemName());
                startActivity(addItem);
                finish();
            }
            else if (contextName.getText().equals("Return Options")){
                currentParent = returnToList.get(position);
                if(currentParent.getItemName().equals("None")){
                    contextName.setText(mainPageName);
                    GenerateListView(currentParent.getItemParent(), null);
                }
                else{
                    contextName.setText(currentParent.getItemName());
                    GenerateListView(currentParent.getItemParent(), null);
                }
                addItemFB.setVisibility(View.VISIBLE);
            }
            else {
                if (dbMan.isParent(itemList.get(position))) {// If it has children:
                    //      Then start the SubListPage for this item
                    String newP = itemList.get(position).getItemParent() + ";" + itemList.get(position).getItemName();
                    setCurrentParent(itemList.get(position).getItemName(),newP);
                    updateReturnList(currentParent.getItemParent());
                    //messageToastDisplay(currentParent + parentOfParent);
                    contextName.setText(currentParent.getItemName());
                    GenerateListView(currentParent.getItemParent(),null);
                    //messageToastDisplay(itemList.get(position).getItemName() + " has children");
                }
                else {// If it has no children:
                    if (!itemList.get(position).getStatus().equals("Complete")) {//      If the checkbox is checked, set the item's status to "Complete"
                        if (dbMan.modifyCheckListItemStatus(itemList.get(position), "Complete")) {
                            GenerateListView(itemList.get(position).getItemParent(), null);
                            messageToastDisplay(itemList.get(position).getItemName() + " has been modified to " +
                                    itemList.get(position).getStatus());
                            //CheckListItem nItem = new CheckListItem();
                            while(!currentParent.getItemName().equals("None") && allItemsCompleted(itemList)){
                                String completedP = currentParent.getItemName();
                                for(CheckListItem i : returnToList){
                                    if(i.getItemParent().equals(currentParent.getItemParent())) {
                                        currentParent = returnToList.get(returnToList.indexOf(i) - 1);
                                        break;
                                    }
                                }
                                if (!currentParent.getItemName().equals("None"))
                                    contextName.setText(currentParent.getItemName());
                                else {
                                    contextName.setText(mainPageName);
                                }
                                CheckListItem citem = new CheckListItem();
                                citem.setItemParent(currentParent.getItemParent());
                                citem.setItemName(completedP);
                                if(dbMan.modifyCheckListItemStatus(citem, "Complete"))
                                    messageToastDisplay(citem.getItemName() + " checklist is complete");
                                GenerateListView(currentParent.getItemParent(), null);
                            }
                        } else
                            messageToastDisplay(itemList.get(position).getItemName() + " was not modified");
                    }
                    else {
                        //      Otherwise set it to "Incomplete"
                        if (dbMan.modifyCheckListItemStatus(itemList.get(position), "Incomplete")) {
                            GenerateListView(itemList.get(position).getItemParent(), null);
                            messageToastDisplay(itemList.get(position).getItemName() + " has been modified to " +
                                    itemList.get(position).getStatus());
                            if(!currentParent.getItemName().equals("None")) {
                                CheckListItem p = new CheckListItem();
                                p.setItemName(currentParent.getItemName());
                                p.setItemParent(getPrevParent(currentParent));
                                dbMan.modifyCheckListItemStatus(p, "Incomplete");
                                String[] parentLine = getPrevParent(currentParent).split(";");
                                for(int i = parentLine.length-1; i>0; i--) {
                                    p.setItemName(parentLine[i]);
                                    p.setItemParent(getPrevParent(p));
                                    //if (allItemsCompleted(dbMan.getCheckListItems(p.getItemParent(),null)))
                                        dbMan.modifyCheckListItemStatus(p, "Incomplete");
                                    //messageToastDisplay(p.getItemName() + " has been modified to " +
                                    //        p.getStatus() + " with parent name " + p.getItemParent());
                                }
                            }
                        } else
                            messageToastDisplay(itemList.get(position).getItemName() + " was not modified");
                    }
                    //Toast.makeText(getContext(),  item.getItemName()+" has been modified", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    //Add Item floating button click listener
    private final FloatingActionButton.OnClickListener onAddItemClick = new FloatingActionButton.OnClickListener(){
        @Override
        public void onClick(View view) {
            showAddItemDialog();
        }
    };

    private final AutoCompleteTextView.OnClickListener onOrderbyClick = new AutoCompleteTextView.OnClickListener(){

        @Override
        public void onClick(View v) {
            orderbyOptions.showDropDown();
        }
    };

    private final AdapterView.OnItemClickListener onOderbyItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            messageToastDisplay(position + currentParent.getItemParent());
            switch(position){
                case 1  : GenerateListView(currentParent.getItemParent(), "ItemName"); break;
                case 2  : GenerateListView(currentParent.getItemParent(), "Priority"); break;
                case 3  : GenerateListView(currentParent.getItemParent(), "Status"); break;
                case 4  : GenerateListView(currentParent.getItemParent(), "DueDate"); break;
                default : GenerateListView(currentParent.getItemParent(), null); break;
            }
        }
    };

    //*************************************  Helper Methods  ***************************************
    private void setCurrentParent(String itemName, String parentName){
        currentParent.setItemName(itemName);
        currentParent.setItemParent(parentName);
    }

    private String getPrevParent(CheckListItem tItem){
        String[] pLine = tItem.getItemParent().split(";");
        StringBuilder pL= new StringBuilder(pLine[0]);
        for(int i=1; i < pLine.length ; i++){
            if (!tItem.getItemName().equals(pLine[i]))
                pL.append(";").append(pLine[i]);
        }
        return pL.toString();
    }

    private void showAddItemDialog(){
        FragmentManager fManager = getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putCharSequence("ParentName", currentParent.getItemName());
        args.putCharSequence("ppN", currentParent.getItemParent());
        AddItemDialog addItemDialog = new AddItemDialog();
        addItemDialog.setArguments(args);
        addItemDialog.show(fManager, "aIDial");
    }

    public void deleteSelectedItems(){
        for(CheckListItem item : deleteList) {
            dbMan.deleteCheckListItem(item);
            itemList.remove(item);
        }
        deleteList.clear();
    }

    private  void afterDeletePageReset(){
        if(currentParent.getItemName().equals("None")){
            contextName.setText(mainPageName);
        }
        else
            contextName.setText(currentParent.getItemName());

        addItemFB.setVisibility(View.VISIBLE);
        addItem.setVisible(true);
        cancelAction.setVisible(false);
        confirmDel.setVisible(false);
        GenerateListView(currentParent.getItemParent(), null);
    }

    private void showAlertDialog(){
        new AlertDialog.Builder(this)
                //set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
                //set title
                .setTitle("Permanent Item Delete")
                //set message
                .setMessage("Do you want to delete the " + deleteList.size() + " item(s)?")
                //set positive button
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainPage.this, deleteList.size() +
                                " items were deleted", Toast.LENGTH_LONG).show();
                        deleteSelectedItems();
                        afterDeletePageReset();
                    }
                })
                //set negative button
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainPage.this, "Delete action was canceled", Toast.LENGTH_LONG).show();
                        afterDeletePageReset();
                    }
                })
                .show();
    }

    private void GenerateListView(String parent, String orderBy){
        itemList.clear();
        itemList.addAll(GenerateItemList(parent,orderBy));
        checkListAdapter = new List_Adapter(this, R.id.itemCheckBox,itemList, false);
        checkListView.setAdapter(checkListAdapter);
        checkListView.setOnItemClickListener(onItemClick);
    }

    public List<CheckListItem> GenerateItemList(String parentItem, String orderBy){
        List<CheckListItem> checkListItems = new ArrayList<>();
        checkListItems.addAll(dbMan.getCheckListItems(parentItem, orderBy));

        return checkListItems;
    }

    public void messageToastDisplay(String message){
        Toast.makeText(MainPage.this, message, Toast.LENGTH_LONG).show();
    }

    private boolean allItemsCompleted(List<CheckListItem> cLI){
        boolean allComplete = true;
        for(CheckListItem i : cLI){
            if(!i.getStatus().equals("Complete")) {
                allComplete = false;
                break;
            }
        }
        return allComplete;
    }

    private void updateReturnList(String parentName){
        returnToList.clear();
        //CheckListItem item = new CheckListItem();
        if(parentName.contains(";")){
            String parent = "";
            String[] genealogy = parentName.split(";");
            messageToastDisplay(genealogy[0] + " " + genealogy[1]);
            for (String s : genealogy) {
                CheckListItem item = new CheckListItem();
                if (s.equals("None")) {
                    item.setItemName(s);
                    item.setItemParent(s);
                    parent = s;
                } else {
                    parent += ";" + s;
                    item.setItemName(s);
                    item.setItemParent(parent);
                }
                returnToList.add(item);
            }
        }
        else{
            CheckListItem item = new CheckListItem();
            item.setItemName("None");
            item.setItemParent("None");
            returnToList.add(item);
        }
    }

    private void copyDatabase (String databaseName){
        try {
            final String inFileName = "/data/data/com.example.naftech.flightdataapplication/databases/database.db";
            final String outFileName = Environment.getExternalStorageDirectory() + databaseName + ".db";
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            Log.d(TAG, "copyDatabase: outFile = " + outFileName);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();
        }catch (Exception e){
            Log.d(TAG, "copyDatabase: backup error");
        }
    }

    private void restoreDatabase (String databaseName){
        try {
            final String inFileName = Environment.getExternalStorageDirectory() + databaseName + ".db";
            final String outFileName = "/data/data/com.example.naftech.flightdataapplication/databases/database.db";
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            Log.d(TAG, "copyDatabase: outFile = " + outFileName);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();
        }catch (Exception e){
            Log.d(TAG, "copyDatabase: backup error");
        }
    }
}
