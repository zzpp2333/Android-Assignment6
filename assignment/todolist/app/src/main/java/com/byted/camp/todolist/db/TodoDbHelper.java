package com.byted.camp.todolist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public class TodoDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TodoList.db";

    // TODO 定义数据库名、版本；创建数据库

    public TodoDbHelper(Context context) {

        //super(context, "todo", null, 0);
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TodoContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(int i = oldVersion ; i < newVersion ; i++){
            switch (i){
                case 1:
                    try{
                        db.execSQL("ALTER TABLE " + TodoContract.TodoEntry.TABLE_NAME +
                                " ADD " + TodoContract.TodoEntry.COLUMN_EXTRA_PRIORITY + " Integer");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
            }
        }
    }

}
