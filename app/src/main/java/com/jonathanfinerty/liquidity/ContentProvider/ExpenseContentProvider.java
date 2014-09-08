package com.jonathanfinerty.liquidity.ContentProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ExpenseContentProvider extends ContentProvider {

    private ExpensesDatabaseHelper expensesDatabaseHelper;

    @Override
    public boolean onCreate() {
        expensesDatabaseHelper = new ExpensesDatabaseHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = expensesDatabaseHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        if (uri.equals(LiquidityContract.Expense.CONTENT_URI)){

            builder.setTables(LiquidityContract.Expense.TABLE_NAME);

            if (TextUtils.isEmpty(sortOrder)) {
                sortOrder = LiquidityContract.Expense.SORT_ORDER_DEFAULT;
            }

        } else if (uri.equals(LiquidityContract.Expense.CONTENT_ITEM_URI)) {

            builder.setTables(LiquidityContract.Expense.TABLE_NAME);

            builder.appendWhere(LiquidityContract.Expense._ID + " = " + uri.getLastPathSegment());

        } else {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        if (LiquidityContract.Expense.CONTENT_URI.equals(uri)) {

            return LiquidityContract.Expense.CONTENT_TYPE;

        } else if (LiquidityContract.Expense.CONTENT_ITEM_URI.equals(uri)) {

            return LiquidityContract.Expense.CONTENT_ITEM_TYPE;

        }

        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uri.equals(LiquidityContract.Expense.CONTENT_URI) == false) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }

        SQLiteDatabase db = expensesDatabaseHelper.getWritableDatabase();
        long expenseId = db.insert(LiquidityContract.Expense.TABLE_NAME, null, values);

        return getUriForId(uri, expenseId);
    }

    private Uri getUriForId(Uri uri, long expenseId) {
        Uri uriWithId = ContentUris.withAppendedId(uri, expenseId);
        getContext().getContentResolver().notifyChange(uriWithId, null);
        return uriWithId;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = expensesDatabaseHelper.getWritableDatabase();
        return db.delete(LiquidityContract.Expense.TABLE_NAME,
                LiquidityContract.Expense._ID + "=?",
                new String[]{ uri.getLastPathSegment() });
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
