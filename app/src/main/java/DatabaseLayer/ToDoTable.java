package DatabaseLayer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import BusinesObjects.CheckListItem;

/**
 * Created by admin on 2/24/2018.
 */

public class ToDoTable {

    public static final String TABLE_NAME = "ToDoList";  //Table's name
    public static final String COLUMN_ITEM_ID = "ItemID";  //For item's unique identifier in table
    public static final String COLUMN_ITEM_NAME = "ItemName";  //Holds the checklist item description
    public static final String COLUMN_ITEM_PARENT = "ItemParent";  //Holds the name of the item's parent
    public static final String COLUMN_PRIORITY = "Priority";  //Hold's the item's priority should the user choose to use it to order items
    public static final String COLUMN_STATUS = "Status";  //Holds the completion status of the checklist item
    public static final String COLUMN_DUE_DATE = "DueDate";  //Holds the date the checklist item is due

    public static void onCreate(SQLiteDatabase db){
        String CREATE_TODOTABLE = "CREATE TABLE " + TABLE_NAME +
                "("+ COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
                COLUMN_ITEM_PARENT + " STRING NOT NULL, "+
                COLUMN_ITEM_NAME + " STRING NOT NULL, "+
                COLUMN_PRIORITY + " REAL, " +
                COLUMN_STATUS + " STRING, " +
                COLUMN_DUE_DATE + " DATETIME" +
                ")";
        db.execSQL(CREATE_TODOTABLE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldversion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Method that allows a new checklist item (New table row) to be added to the table
     * @Parameters: Database (db), CheckListItem (cLI)
     * @return: True: For successful insertion
     *          False: For failed insertion
     */
    public static boolean insertItem(SQLiteDatabase db, CheckListItem cLI){
        try {

            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_ITEM_NAME, cLI.getItemName());
            contentValues.put(COLUMN_ITEM_PARENT, cLI.getItemParent());
            contentValues.put(COLUMN_PRIORITY, cLI.getPriority());
            contentValues.put(COLUMN_STATUS, cLI.getStatus());
            contentValues.put(COLUMN_DUE_DATE, cLI.getDueDate());

            db.insertOrThrow(TABLE_NAME, null, contentValues);

            return true;
        }
        catch (SQLiteConstraintException e)
        {
            Log.e("Insert Item",e.toString());
            return false;
        }
    }

    /**
     * Method that allows for an existing checklist item to be deleted form the table
     * The method doesn't take into account dependencies between items
     *@Parameters: Database (db), ItemName (iName), ParentName (pName)
     *@return: True: For successful delete
     *         False: For failed delete
     */
    public static boolean deleteItem(SQLiteDatabase db, String iName, String pName){
        try {
            boolean deleted = db.delete(TABLE_NAME, COLUMN_ITEM_NAME + " = '" + iName + "' AND " +
                    COLUMN_ITEM_PARENT + " = ?", new String[]{pName}) > 0;
            return deleted; //returns true if deleted, return false if no row deleted because no such user id exists
        }
        catch(SQLiteConstraintException e)
        {
            Log.e("Delete Item",e.toString());
            return false;
        }
    }

    private static Cursor getAll(SQLiteDatabase db){
        Cursor allData = db.rawQuery("SELECT * FROM " + TABLE_NAME, null );
        return allData;
    }

    /**
     *Updates the content of a record (row) in the table
     * @Parameters: Database (db), ParentName (pName), ItemName (iName), ColumnName (colName), NewValue (nVal)
     * @return: True: For successful Update
     *          False: For failed Update
     */
    public static boolean updateItem(SQLiteDatabase db, String pName, String iName, String nVal, String colName){
        List<CheckListItem> trgList = new ArrayList<>();
        boolean updated = false;
        int rowsUpdated = 0;

        trgList.addAll(getItems(db, pName, null));

        try{
            int i = 0;
            //check if the item exists
            while(i < trgList.size()){
                if( iName.equals(trgList.get(i).getItemName())) {
                    break;
                }
                i++;
            }

            //update if the item exists
            if(i != trgList.size()){///iName.equals(iID.getString(iID.getColumnIndex(COLUMN_ITEM_NAME)))) {
                ContentValues cV = new ContentValues();
                if (colName.equals(COLUMN_PRIORITY))
                    cV.put(COLUMN_PRIORITY, Integer.valueOf(nVal));
                else
                    cV.put(colName, nVal);

                rowsUpdated = db.update(TABLE_NAME, cV, COLUMN_ITEM_NAME + " = ? AND "
                        + COLUMN_ITEM_PARENT + " = ?"
                        , new String[]{iName, pName});//trgList.get(i).getItemName()});

                if (rowsUpdated > 0) updated = true;
            }

            return updated;
        }
        catch(SQLiteConstraintException e){
            Log.e("Update Item",e.toString());
            return false;
        }
    }

    ///DO NOT USE THIS
//    public static CheckListItem getItem(SQLiteDatabase db, String iN){
//        CheckListItem item = new CheckListItem();
//        Cursor alldata = getAll(db);
//
//        try{
//            alldata.moveToFirst();
//            while (!alldata.isAfterLast()){
//                if(alldata.getColumnName(alldata.getColumnIndex(COLUMN_ITEM_NAME)).equals(iN)){
//                    item.setItemName(iN);
//                    item.setItemParent(alldata.getColumnName(alldata.getColumnIndex(COLUMN_ITEM_PARENT)));
//                    item.setPriority(Integer.valueOf(alldata.getColumnName(alldata.getColumnIndex(COLUMN_PRIORITY))));
//                    item.setDueDate(alldata.getColumnName(alldata.getColumnIndex(COLUMN_DUE_DATE)));
//                    break;
//                }
//                alldata.moveToNext();
//            }
//        }
//        finally {
//            alldata.close();
//        }
//        return item;
//    }

//    public boolean resetItem(SQLiteDatabase db, String iN){
//        Cursor data = getAll(db);
//        try {
//            data.moveToFirst();
//            while (!data.isAfterLast()) {
//
//                data.moveToNext();
//            }
//        }
//    }

    /**
     * Retrieves Checklist items directly associated to given parent item. Orders the list's items according to priority or due date.
     * By default the method orders the list by itemID, which is the same order it was
     * entered by the user.
     * @Parameters: Database(db), ParentItemName(pIN), OrderBy (oB)
     * @return: The list of items associated to the given ParentItemName
     */
    public static List<CheckListItem> getItems(SQLiteDatabase db, String pIN, String oB){
        if(oB == null || oB == ""){
            oB = COLUMN_ITEM_ID;
        }
        Cursor items = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ITEM_PARENT +
                "= ? Order by ?",new String[]{pIN, oB});
        List<CheckListItem> cLI = new ArrayList<>();

        try{
            items.moveToFirst();
            while(!items.isAfterLast()){
                CheckListItem item = new CheckListItem();
                item.setItemParent(pIN);
                item.setItemName(items.getString(items.getColumnIndex(COLUMN_ITEM_NAME)));
                item.setPriority(Integer.valueOf(items.getString(items.getColumnIndex(COLUMN_PRIORITY))));
                item.setStatus(items.getString(items.getColumnIndex(COLUMN_STATUS)));
                item.setDueDate(items.getString(items.getColumnIndex(COLUMN_DUE_DATE)));

                cLI.add(item);
                items.moveToNext();
            }

            return cLI;
        }
        finally {
            items.close();
        }
    }

    /**
     * Checks the database to see if the word provided by the user appears in the Parent column
     * @Parameters: Database (db), TargetName(trgName)
     * @return: True: Target Name is a parent (Successful search)
     *          False: TargetName is not a parent (Unsuccessful search)
     */
    public static boolean isParent(SQLiteDatabase db, CheckListItem trgItem){
        boolean found = false;
        String trgParent = trgItem.getItemParent() + ";" + trgItem.getItemName();
        Cursor parent = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_ITEM_PARENT +" = ?", new String[] {trgParent});
        try {
            parent.moveToFirst();
            while (!parent.isAfterLast()) {
                if (parent.getString(parent.getColumnIndex(COLUMN_ITEM_PARENT)).equals(trgParent)) {
                    found = true;
                    break;
                }
                parent.moveToNext();
            }
            return found;
        }
        finally {
            parent.close();
        }
    }
}
