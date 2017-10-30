package lm.pkp.com.landmap.position;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.sync.LMRRestAsyncTask;
import lm.pkp.com.landmap.util.AndroidSystemUtil;

public class PositionsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    public static final String POSITION_TABLE_NAME = "position_master";

    public static final String POSITION_COLUMN_NAME = "name";
    public static final String POSITION_COLUMN_DESCRIPTION = "desc";
    public static final String POSITION_COLUMN_LAT = "lat";
    public static final String POSITION_COLUMN_LON = "lon";
    public static final String POSITION_COLUMN_TAGS = "tags";
    private static final String POSITION_COLUMN_UNIQUE_AREA_ID = "uniqueAreaId";
    private static final String POSITION_COLUMN_UNIQUE_ID = "uniqueId";

    public PositionsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + POSITION_TABLE_NAME + "(" +
                        POSITION_COLUMN_NAME + " text," +
                        POSITION_COLUMN_DESCRIPTION + " text," +
                        POSITION_COLUMN_LAT + " text, " +
                        POSITION_COLUMN_LON + " text," +
                        POSITION_COLUMN_UNIQUE_AREA_ID + " text," +
                        POSITION_COLUMN_UNIQUE_ID + " text," +
                        POSITION_COLUMN_TAGS + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + POSITION_TABLE_NAME);
        onCreate(db);
    }

    public PositionElement insertPositionLocally(PositionElement pe, AreaElement ae) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        String uniqueId = UUID.randomUUID().toString();
        contentValues.put(POSITION_COLUMN_UNIQUE_ID, uniqueId);
        pe.setUniqueId(uniqueId);

        contentValues.put(POSITION_COLUMN_UNIQUE_AREA_ID, ae.getUniqueId());
        pe.setUniqueAreaId(ae.getUniqueId());

        contentValues.put(POSITION_COLUMN_NAME, pe.getName());
        contentValues.put(POSITION_COLUMN_DESCRIPTION, pe.getDescription());
        contentValues.put(POSITION_COLUMN_LAT, pe.getLat());
        contentValues.put(POSITION_COLUMN_LON, pe.getLon());
        contentValues.put(POSITION_COLUMN_TAGS, pe.getTags());

        db.insert(POSITION_TABLE_NAME, null, contentValues);
        db.close();
        return pe;
    }

    public PositionElement insertPositionFromServer(PositionElement pe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(POSITION_COLUMN_UNIQUE_ID, pe.getUniqueId());
        contentValues.put(POSITION_COLUMN_UNIQUE_AREA_ID, pe.getUniqueAreaId());
        contentValues.put(POSITION_COLUMN_NAME, pe.getName());
        contentValues.put(POSITION_COLUMN_DESCRIPTION, pe.getDescription());
        contentValues.put(POSITION_COLUMN_LAT, pe.getLat());
        contentValues.put(POSITION_COLUMN_LON, pe.getLon());
        contentValues.put(POSITION_COLUMN_TAGS, pe.getTags());

        db.insert(POSITION_TABLE_NAME, null, contentValues);
        db.close();
        return pe;
    }

    public void insertPositionToServer(PositionElement pe) {
        new LMRRestAsyncTask().execute(preparePostParams("insert", pe));
    }

    public Integer deletePosition(PositionElement pe) {
        SQLiteDatabase db = this.getWritableDatabase();
        int delete = db.delete(POSITION_TABLE_NAME, POSITION_COLUMN_UNIQUE_ID + " = ? ", new String[]{pe.getUniqueId()});
        new LMRRestAsyncTask().execute(preparePostParams("delete", pe));
        db.close();
        return delete;
    }

    public void deletePositionById(String uniqueId, String uniqueAreaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+POSITION_TABLE_NAME+" WHERE "
                + POSITION_COLUMN_UNIQUE_AREA_ID +" = '"+uniqueAreaId
                + "' AND "+ POSITION_COLUMN_UNIQUE_ID +" = '" + uniqueId + "'");

        PositionElement pe = new PositionElement();
        pe.setUniqueAreaId(uniqueAreaId);
        pe.setUniqueId(uniqueId);

        new LMRRestAsyncTask().execute(preparePostParams("deleteById", pe));
        db.close();
    }

    public void deletePositionByUniqueAreaId(String uniqueAreaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + POSITION_TABLE_NAME + " WHERE "
                + POSITION_COLUMN_UNIQUE_AREA_ID + " = '" + uniqueAreaId + "'");

        PositionElement pe = new PositionElement();
        pe.setUniqueAreaId(uniqueAreaId);

        new LMRRestAsyncTask().execute(preparePostParams("deleteByUniqueAreaId", pe));
        db.close();
    }

    public ArrayList<PositionElement> getAllPositionForArea(AreaElement ae) {
        ArrayList<PositionElement> pes = new ArrayList<PositionElement>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + POSITION_TABLE_NAME + " WHERE " + POSITION_COLUMN_UNIQUE_AREA_ID + "=?",
                new String[]{ae.getUniqueId() + ""});
        if(cursor != null){
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                PositionElement pe = new PositionElement();

                pe.setUniqueId(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_UNIQUE_ID)));
                pe.setUniqueAreaId(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_UNIQUE_AREA_ID)));

                pe.setName(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_NAME)));
                pe.setDescription(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_DESCRIPTION)));

                String latStr = cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_LAT));
                pe.setLat(Double.parseDouble(latStr));

                String lonStr = cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_LON));
                pe.setLon(Double.parseDouble(lonStr));

                pe.setTags(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_TAGS)));
                pes.add(pe);

                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return pes;
    }

    private JSONObject preparePostParams(String queryType, PositionElement pe) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("requestType", "PositionMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID",AndroidSystemUtil.getDeviceId());
            postParams.put("lon",pe.getLon() + "");
            postParams.put("lat",pe.getLat() + "");
            postParams.put("desc",pe.getDescription());
            postParams.put("tags",pe.getTags());
            postParams.put("name",pe.getName());
            postParams.put("uniqueAreaId",pe.getUniqueAreaId());
            postParams.put("uniqueId",pe.getUniqueId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  postParams;
    }

    public void deletePositionsLocally() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(POSITION_TABLE_NAME, "1", null);
        db.close();
    }

}