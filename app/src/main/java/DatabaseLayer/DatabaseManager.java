package DatabaseLayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import BusinesObjects.CheckListItem;

/**
 * Created by admin on 2/24/2018.
 */

public class DatabaseManager extends SQLiteOpenHelper {

    private static DatabaseManager dbHandlerInstance = null;
    private static final String DATABASE_NAME = "ToDoList";
    private static final int DATABASE_VERSION = 1;

    public static synchronized DatabaseManager getInstance(Context context) {

        if (dbHandlerInstance == null) {
            dbHandlerInstance = new DatabaseManager(context.getApplicationContext());
        }
        return dbHandlerInstance;
    }

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ToDoTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ToDoTable.onUpgrade(db,oldVersion,newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    ////////////////////////////////  ToDOList Actions  ///////////////////////////////////////////
    public boolean insertChecklistItem(CheckListItem checkListItem){
        SQLiteDatabase db = this.getWritableDatabase();
        return ToDoTable.insertItem(db, checkListItem);
    }

    public boolean deleteCheckListItem(CheckListItem item){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean deleted = true;
        List<CheckListItem> treeList = new ArrayList<>();
        treeList.addAll(getCheckListItemTree(item));

        for(CheckListItem i : treeList){
            if(!ToDoTable.deleteItem(db, item.getItemName(), item.getItemParent())) {
                deleted = false;
                break;
            }
        }

        return deleted;
    }

    public boolean resetCheckListItem(CheckListItem item){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean reset = true;
        List<CheckListItem> treeList = new ArrayList<>();
        treeList.addAll(getCheckListItemTree(item));

        for(CheckListItem i : treeList){
            if(!modifyCheckListItemStatus(i, "Incomplete")) {
                reset = false;
                break;
            }
        }

        return reset;
    }

    public boolean modifyCheckListItemStatus(CheckListItem item, String newValue){
        SQLiteDatabase db = this.getWritableDatabase();
        return ToDoTable.updateItem(db, item.getItemParent(), item.getItemName(), newValue, ToDoTable.COLUMN_STATUS);
    }

    public boolean modifyCheckListItemName(CheckListItem item, String newValue){
        SQLiteDatabase db = this.getWritableDatabase();
        return ToDoTable.updateItem(db, item.getItemParent(), item.getItemName(), newValue, ToDoTable.COLUMN_ITEM_NAME);
    }

    private boolean modifyCheckListItemParent(CheckListItem item, String newValue){
        SQLiteDatabase db = this.getWritableDatabase();
        return ToDoTable.updateItem(db, item.getItemParent(), item.getItemName(), newValue, ToDoTable.COLUMN_ITEM_PARENT);
    }

    public boolean modifyCheckListItemPriority(CheckListItem item, String newValue){
        SQLiteDatabase db = this.getWritableDatabase();
        return ToDoTable.updateItem(db, item.getItemParent(), item.getItemName(), newValue, ToDoTable.COLUMN_PRIORITY);
    }

    public boolean modifyCheckListItemDueDate(CheckListItem item, String newValue){
        SQLiteDatabase db = this.getWritableDatabase();
        return ToDoTable.updateItem(db, item.getItemParent(), item.getItemName(), newValue, ToDoTable.COLUMN_DUE_DATE);
    }

//    public CheckListItem getACheckListItem(String itemName){
//        SQLiteDatabase db = this.getReadableDatabase();
//        return ToDoTable.getItem(db, itemName);
//    }

    public List<CheckListItem> getCheckListItems(String parentItemName, String orderByOption){
        SQLiteDatabase db = this.getReadableDatabase();
        return ToDoTable.getItems(db, parentItemName, orderByOption);
    }

//    private String getParent(String itemName){
//        String parentL = "";
//        CheckListItem item = new CheckListItem();
//        item.setItemName(itemName);
//        List<CheckListItem> tree = new ArrayList<>();
//        tree.addAll(getCheckListItemTree(item));
//
//        return parentL;
//    }

    private List<CheckListItem> getCheckListItemTree(CheckListItem rootItem){
        List<CheckListItem> treeList = new ArrayList<>();
        //CheckListItem rootItem = new CheckListItem();
        //rootItem.setItemName(rootItemName);
        SQLiteDatabase db = this.getReadableDatabase();
        treeList.add(rootItem);
        CheckListItem item;
        int i = 0;
        while(i < treeList.size()){
            item = treeList.get(i++);
            if(isParent(item)){
                treeList.addAll(getCheckListItems(item.getItemParent() + ";" + item.getItemName(), null));
            }
        }
        return treeList;
    }

    public boolean isParent(CheckListItem item){
        SQLiteDatabase db = this.getReadableDatabase();
        return ToDoTable.isParent(db, item);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

}//END DatabaseManager
