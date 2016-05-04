package com.bijoysingh.quicknote;

import com.github.bijoysingh.starter.database.DBColumn;
import com.github.bijoysingh.starter.database.DatabaseModel;

import java.io.Serializable;

/**
 * The note item
 * Created by bijoy on 5/4/16.
 */
public class NoteItem extends DatabaseModel implements Serializable {
    @DBColumn(primaryKey = true, autoIncrement = true)
    public Integer id;

    @DBColumn
    public String title;

    @DBColumn
    public String description;

    @DBColumn
    public String timestamp;

    public NoteItem() {
    }

    public NoteItem(String now) {
        id = null;
        title = "";
        description = "";
        timestamp = now;
    }

    public NoteItem(Integer id, String title, String description, String timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
    }
}
