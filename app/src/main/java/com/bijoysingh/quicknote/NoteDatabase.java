package com.bijoysingh.quicknote;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.bijoysingh.starter.database.DBColumn;
import com.github.bijoysingh.starter.database.DatabaseColumn;
import com.github.bijoysingh.starter.database.DatabaseManager;
import com.github.bijoysingh.starter.database.DatabaseModel;

import java.util.List;

/**
 * The note database
 * Created by bijoy on 5/4/16.
 */
public class NoteDatabase extends DatabaseManager {

    public NoteDatabase(Context context) {
        super(context, new DatabaseModel[]{new NoteItem()});
    }

    public NoteItem get(Integer id, NoteItem model) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + model.getTableName()
            + " WHERE id=?", new String[]{id.toString()});
        if (cursor != null) {
            cursor.moveToNext();
            model = parseCursor(cursor);
        }
        db.close();
        return model;
    }

    public NoteItem add(NoteItem model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = model.getValues();
        values.remove("id");
        long returnCode = db.insert(model.getTableName(), null, values);
        Log.d(DatabaseManager.class.getSimpleName(), "Row added at " + returnCode
            + " of " + model.getTableName());

        Cursor cursor = db.rawQuery("SELECT * FROM " + model.getTableName()
            + " WHERE id=last_insert_rowid()", new String[]{});
        if (cursor != null) {
            cursor.moveToNext();
            model = parseCursor(cursor);
            Log.d(DatabaseManager.class.getSimpleName(), " Got the item -> " + model.id);
        }

        db.close();
        return model;
    }

    public NoteItem parseCursor(Cursor cursor) {
        List<DatabaseColumn> keys = (new NoteItem()).getKeys();
        NoteItem model = new NoteItem();
        try {
            Integer position = 0;
            for (DatabaseColumn column : keys) {
                Log.d(DatabaseColumn.class.getSimpleName(), column.fieldName + " : " + column.field);
                if (column.fieldType.equals(DBColumn.Type.INTEGER)) {
                    column.field.set(model, cursor.getInt(position));
                } else if (column.fieldType.equals(DBColumn.Type.TEXT)) {
                    column.field.set(model, cursor.getString(position));
                } else if (column.fieldType.equals(DBColumn.Type.REAL)) {
                    column.field.set(model, cursor.getDouble(position));
                }
                position += 1;
            }
        } catch (Exception exception) {
            Log.e(DatabaseManager.class.getSimpleName(), exception.getMessage(), exception);
        }
        return model;
    }

    public NoteItem addOrUpdate(NoteItem model) {
        if (model.id == null) {
            return add(model);
        } else {
            update(model);
            return model;
        }
    }

    public long update(NoteItem model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = model.getValues();
        long returnCode = db.update(model.getTableName(),
            values, "id=?", new String[]{model.id.toString()});
        Log.d(DatabaseManager.class.getSimpleName(), "Row added at " + returnCode
            + " of " + model.getTableName());
        db.close();
        return returnCode;
    }

    public void remove(NoteItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(item.getTableName(), "id=?", new String[]{item.id.toString()});
        db.close();
    }
}
