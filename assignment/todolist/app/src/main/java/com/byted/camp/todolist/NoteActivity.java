package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;

public class NoteActivity extends AppCompatActivity {

    private EditText editText;
    private Button addBtn;

    private TodoDbHelper mDbHelper;
    private static long idCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        addBtn = findViewById(R.id.btn_add);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim());
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        mDbHelper = new TodoDbHelper(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    private boolean saveNote2Database(String content) {
        // TODO 插入一条新数据，返回是否插入成功

        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        try{
            ContentValues values = new ContentValues();
            values.put(TodoContract.TodoEntry.COLUMN_NAME_ID,idCount++);
            values.put(TodoContract.TodoEntry.COLUMN_NAME_CONTENT,content);
            values.put(TodoContract.TodoEntry.COLUMN_NAME_STATE, 0);

            //SimpleDateFormat formatRaw = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);

            /*Date date = new Date();
            date.setTime(System.currentTimeMillis());*/
            //SimpleDateFormat formatRaw = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);

            //String resTime = formatRaw.parse(date.toString()).toString();
            Long resTime = System.currentTimeMillis();
            values.put(TodoContract.TodoEntry.COLUMN_NAME_TIME,resTime);
            //Log.i("inserted",resTime);
            Log.i("inserted_content",content);
            Long newRowId = mDb.insert(TodoContract.TodoEntry.TABLE_NAME,null,values);
            Log.i("inserted at ",String.valueOf(newRowId));
            return true;
        }catch (Exception e){e.printStackTrace();}

        return false;
    }
}
