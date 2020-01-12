package joyli.example.com.shopifydemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Joyli on 2017-09-04.
 */

public class SQLDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "mylist.db";
    public static final String TABLE_NAME = "mylist_data";
    public static final String NAME = "product_name";
    public static final String PRICE = "product_price";
    public static final String IMAGE = "product_image";

    public SQLDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);}

    @Override
    public void onCreate(SQLiteDatabase db) { //called when the database is created for the first time. This iswehre the creation of tables and the initial population of the tables should happen

        String createTable = "CREATE TABLE " + TABLE_NAME + "(" +NAME+ " TEXT,"+ PRICE + " TEXT," + IMAGE+ " TEXT);";
        db.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);

        onCreate(db);
    }

    public boolean addData (String productName, String productPrice, String productImageSource) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME, productName);
        contentValues.put(PRICE, productPrice);
        contentValues.put(IMAGE, productImageSource);
        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1) {
            return false;
        }
        else {
            return true;
        }
    }

    public Cursor getListContents () {
        SQLiteDatabase db = this.getWritableDatabase(); //create and/ore open a database that will be used for reading and writing
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return data;
    }

    public Double totalPrice () {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + PRICE + ") AS Total FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()){
            Double total = cursor.getDouble(cursor.getColumnIndex("product_price"));
            return total;
        }
        return 0.0;
    }

    public Cursor total (){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT Sum(" + PRICE + ") AS Total FROM " + TABLE_NAME, null);
        return  cursor;
    }

    public void deleteCartItem(String productName, String productPrice, String imageSRC)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + NAME+" = '"+productName+ "'" + " AND " + PRICE + " = '"+productPrice +"'" +" AND " + IMAGE + " = '"+ imageSRC+ "'";
        db.execSQL(query);

        //return db.delete(TABLE_NAME, NAME + "=" + productName, null) > 0;
    }
}

