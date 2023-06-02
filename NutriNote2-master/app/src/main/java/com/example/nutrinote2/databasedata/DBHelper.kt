package com.example.nutrinote2.databasedata

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class DBHandler(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USERNAME_COL + " TEXT,"
                + PASSWORD_COL + " TEXT,"
                + EMAIL_COL + " TEXT)")
        db.execSQL(createTableQuery)

        val createWaterIntakeTableQuery = ("CREATE TABLE " + WATER_INTAKE_TABLE_NAME + " ("
                + WATER_INTAKE_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATE_COL + " TEXT,"
                + AMOUNT_COL + " INTEGER)")
        db.execSQL(createWaterIntakeTableQuery)

        insertWaterIntakeEntry("2023-05-23", 500) // Day 1: 500ml
        insertWaterIntakeEntry("2023-05-24", 1000) // Day 2: 1000ml
        insertWaterIntakeEntry("2023-05-25", 1250) // Day 3: 1250ml
        insertWaterIntakeEntry("2023-05-26", 1750) // Day 4: 1750ml
        insertWaterIntakeEntry("2023-05-27", 1000) // Day 5: 1000ml
        insertWaterIntakeEntry("2023-05-28", 2000) // Day 6: 2000ml
        insertWaterIntakeEntry("2023-05-29", 1750) // Day 7: 1750ml
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $WATER_INTAKE_TABLE_NAME")
        onCreate(db)
    }
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Month starts from 0, so add 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    fun insertWaterIntakeEntry(date: String, amount: Int) {
        val values = ContentValues()
        values.put(AMOUNT_COL, amount)
        values.put(DATE_COL, date)

        val db = this.writableDatabase
        db.insert(WATER_INTAKE_TABLE_NAME, null, values)
        db.close()
    }

    fun getAllWaterIntakeEntries(): List<WaterIntakeEntry> {
        val waterIntakeEntries = mutableListOf<WaterIntakeEntry>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $WATER_INTAKE_TABLE_NAME"
        val cursor = db.rawQuery(selectQuery, null)

        val idIndex = cursor.getColumnIndex(ID_COL)
        val dateIndex = cursor.getColumnIndex(DATE_COL)
        val amountIndex = cursor.getColumnIndex(AMOUNT_COL)

        if (idIndex != -1 && dateIndex != -1 && amountIndex != -1) {
            if (cursor.moveToFirst()) {
                do {
                    val entry = WaterIntakeEntry(
                        id = cursor.getInt(idIndex),
                        date = cursor.getString(dateIndex),
                        amount = cursor.getInt(amountIndex)
                    )
                    waterIntakeEntries.add(entry)
                } while (cursor.moveToNext())
            }
        }

        cursor.close()
        return waterIntakeEntries
    }


    fun addUser(username: String?, password: String?, email: String?) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(USERNAME_COL, username)
        values.put(PASSWORD_COL, password)
        values.put(EMAIL_COL, email)

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun findByUsername(username: String?): User? {
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $USERNAME_COL = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(username))
        val user = if (cursor.moveToFirst()) {
            User(
                username = cursor.getString(cursor.getColumnIndex(USERNAME_COL)),
                email = cursor.getString(cursor.getColumnIndex(EMAIL_COL)),
                password = cursor.getString(cursor.getColumnIndex(PASSWORD_COL))
            )
        } else {
            null
        }
        cursor.close()
        db.close()
        return user
    }

    @SuppressLint("Range")
    fun findByEmail(email: String?): User? {
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $EMAIL_COL = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(email))
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                username = cursor.getString(cursor.getColumnIndex(USERNAME_COL)),
                email = cursor.getString(cursor.getColumnIndex(EMAIL_COL)),
                password = cursor.getString(cursor.getColumnIndex(PASSWORD_COL))
            )
        }
        cursor.close()
        // db.close() -- removed as explained in the previous message
        return user
    }

    companion object {
        private const val DB_NAME = "userdb"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "users"
        private const val ID_COL = "id"
        private const val USERNAME_COL = "username"
        private const val PASSWORD_COL = "password"
        private const val EMAIL_COL = "email"

        private const val WATER_INTAKE_TABLE_NAME = "water_intake"
        private const val WATER_INTAKE_ID_COL = "id"
        private const val DATE_COL = "date"
        private const val AMOUNT_COL = "amount"
    }
}
