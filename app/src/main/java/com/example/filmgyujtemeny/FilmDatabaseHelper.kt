package com.example.filmgyujtemeny


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Film(
    val id: Int,
    val title: String,
    val genre: String,
    val rating: Int
)

class FilmDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "film.db", null, 1) {

    //adatbázistábla létrehozása
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE films (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                genre TEXT NOT NULL,
                rating INTEGER NOT NULL
                )
            """.trimIndent()
        )
    }

    //adatbázis verzió váltás történik
    override fun onUpgrade(
        db: SQLiteDatabase, oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS films")
        onCreate(db)
    }


    //CREATE
    fun addNewFilm(title: String, genre: String, rating: Int) {
        val values = ContentValues().apply {
            put("title", title)
            put("genre", genre)
            put("rating", rating)
        }
        writableDatabase.insert(
            "films", null,
            values
        )
    }

    //READ
    fun getAllfilms(): List<Film> {

        val films = mutableListOf<Film>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, title, genre, rating FROM films ORDER by ID DESC", null
        )
        cursor.use {
            while (it.moveToNext()) {
                val student = Film(
                    id = it.getInt(it.getColumnIndexOrThrow("id")),
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    genre = it.getString(it.getColumnIndexOrThrow("genre")),
                    rating = it.getInt(it.getColumnIndexOrThrow("rating"))
                )
                films.add(student)
            }
        }
        return films
    }

    //UPDATE
    fun updateFilm(id: Int, title: String, genre: String, rating: Int){
        val values = ContentValues().apply {
            put("title", title)
            put("genre", genre)
            put("rating", rating)
        }
        writableDatabase.update(
            "films",
            values, "id = ?",arrayOf(id.toString())
        )
    }

    //DELETE
    fun deleteFilm(id: Int){
        writableDatabase.delete(
            "films", "id = ?",
            arrayOf(id.toString())
        )
    }
}