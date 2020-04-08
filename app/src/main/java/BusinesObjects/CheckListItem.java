package BusinesObjects;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A Class describing the features of a checklist's item
 */
public class CheckListItem {
    private String itemName;  //Holds the checklist item description
    private String itemParent;  //Holds the name of the item's parent
    private int priority;  //Hold's the item's priority should the user choose to use it to order items
    private String status;  //Holds the completion status of the checklist item
    private String dueDate;  //Holds the date the checklist item is due

    public CheckListItem(){
        this.itemName = "";
        this.itemParent = "";
        this.priority = 0;
        this.status = "Incomplete";

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        this.dueDate = dateFormat.format(date);
    }
    public CheckListItem(CheckListItem nCLI){
        this.itemName = nCLI.getItemName();
        this.itemParent = nCLI.getItemParent();
        this.priority = nCLI.getPriority();
        this.status = nCLI.getStatus();
        this.dueDate = nCLI.getDueDate();
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName){
        this.itemName = itemName;
    }

    public String getItemParent() {
        return itemParent;
    }

    public void setItemParent(String itemParent) {
        this.itemParent = itemParent;
    }

//    public boolean equals(CheckListItem item){
//        this.itemParent += itemParent;
//
//        return true;
//    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if(!String.valueOf(priority).matches("[0-9]")){
            this.priority = 0;
        }
        else
            this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
