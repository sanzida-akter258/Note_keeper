package com.example.notes.activities.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.notes.activities.dao.notedao;
import com.example.notes.activities.entities.note;

@Database(entities = note.class, version = 1, exportSchema = false)
public abstract class notedatabase extends RoomDatabase {

    private static notedatabase notedatabase;

    public static synchronized notedatabase getNotedatabase(Context context) {
        if (notedatabase == null) {
            notedatabase= Room.databaseBuilder(
                    context,
                    com.example.notes.activities.database.notedatabase.class,
                    "notes_db"
            ).build();

        }
        return notedatabase;
    }
    public abstract notedao notedao();
}
