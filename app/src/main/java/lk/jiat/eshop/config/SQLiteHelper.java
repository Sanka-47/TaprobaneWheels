package lk.jiat.eshop.config;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.eshop.model.Product;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EShopLocal.db";
    private static final int DATABASE_VERSION = 1;

    // Table for Recently Viewed Products
    public static final String TABLE_RECENTLY_VIEWED = "recently_viewed";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_IMAGE = "image_url";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_RECENTLY_VIEWED + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCT_ID + " TEXT UNIQUE, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_PRICE + " REAL, " +
                    COLUMN_IMAGE + " TEXT, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";

    public SQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENTLY_VIEWED);
        onCreate(db);
    }

    public void addRecentlyViewed(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_ID, product.getProductId());
        values.put(COLUMN_TITLE, product.getTitle());
        values.put(COLUMN_PRICE, product.getPrice());
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            values.put(COLUMN_IMAGE, product.getImages().get(0));
        }

        // Insert or Replace to update timestamp if product already exists
        db.insertWithOnConflict(TABLE_RECENTLY_VIEWED, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<Product> getRecentlyViewed() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECENTLY_VIEWED, null, null, null, null, null, COLUMN_TIMESTAMP + " DESC", "10");

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)));
                product.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
                
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
                List<String> images = new ArrayList<>();
                images.add(imageUrl);
                product.setImages(images);
                
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }
}
