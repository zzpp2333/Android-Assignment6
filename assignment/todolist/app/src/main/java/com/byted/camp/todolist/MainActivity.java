package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.debug.DebugActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;

    private TodoDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
                notesAdapter.refresh(loadNotesFromDatabase());
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
                notesAdapter.refresh(loadNotesFromDatabase());
            }
        });
        recyclerView.setAdapter(notesAdapter);

        mDbHelper = new TodoDbHelper(getApplicationContext());

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans

        SQLiteDatabase mDb = mDbHelper.getReadableDatabase();

        List<Note> itemLists = new ArrayList<>();
        Cursor cursor = null;

        try{
            String[] projection = {
                    TodoContract.TodoEntry.COLUMN_NAME_ID,
                    TodoContract.TodoEntry.COLUMN_NAME_STATE,
                    TodoContract.TodoEntry.COLUMN_NAME_CONTENT,
                    TodoContract.TodoEntry.COLUMN_NAME_TIME,
                    //TodoContract.TodoEntry.COLUMN_EXTRA_PRIORITY
            };

            String sortOrder = TodoContract.TodoEntry.COLUMN_NAME_TIME + " Desc";

            cursor = mDb.query(
                    TodoContract.TodoEntry.TABLE_NAME,
                    projection,
                    null, null,
                    null, null,
                    sortOrder
            );

            while(cursor.moveToNext()){
                Long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_NAME_ID));
                Integer itemState = cursor.getInt(
                        cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_NAME_STATE));
                String itemContent = cursor.getString(
                        cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_NAME_CONTENT));
                Long itemDateString = cursor.getLong(
                        cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_NAME_TIME));

                /*SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
                Date itemDate = null;
                java.util.Date resDate = null;
                try{
                    resDate = format.parse(itemDateString);
                    itemDate = new Date(resDate.getTime());
                }catch (Exception e){
                    e.printStackTrace();
                }*/
                Note note = new Note(itemId);
                note.setContent(itemContent);
                note.setDate(new Date(itemDateString));
                State noteState = State.from(itemState);
                note.setState(noteState);
                itemLists.add(note);
            }
        }finally{
            if(cursor != null){
                cursor.close();
            }
        }

        return itemLists;
        //return null;
    }

    private void deleteNote(Note note) {
        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        String selection = TodoContract.TodoEntry.COLUMN_NAME_ID + "=?";
        String[] selectionArgs = {String.valueOf(note.id)};
        int deletedRows = mDb.delete(TodoContract.TodoEntry.TABLE_NAME,selection,selectionArgs);
        Log.i("deletedRows",String.valueOf(deletedRows));
        // TODO 删除数据
    }

    private void updateNode(Note note) {
        // 更新数据
        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        String content = note.getContent();

        Integer state = note.getState().intValue;

        values.put(TodoContract.TodoEntry.COLUMN_NAME_ID,note.id);
        values.put(TodoContract.TodoEntry.COLUMN_NAME_STATE,state);
        values.put(TodoContract.TodoEntry.COLUMN_NAME_CONTENT,content);
        values.put(TodoContract.TodoEntry.COLUMN_NAME_TIME,note.getDate().toString());

        String selection = TodoContract.TodoEntry.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(note.id)};
        int count = mDb.update(TodoContract.TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        Log.i("updated",String.valueOf(count));
    }

}
