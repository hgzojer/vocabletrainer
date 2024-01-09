package at.hgz.vocabletrainer.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import at.hgz.vocabletrainer.R;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class VocableOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 15;
    private static final String DATABASE_NAME = "vocabledb";
    
    private static final String VOCABLE_TABLE_NAME = "vocable";
    private static final String DICTIONARY_ID_COL_NAME = "dictionary_id";
    private static final String WORD_COL_NAME = "word";
    private static final String TRANSLATION_COL_NAME = "translation";
    
    private static final String DICTIONARY_TABLE_NAME = "dictionary";
    private static final String ID_COL_NAME = "id";
    private static final String NAME_COL_NAME = "name";
    private static final String LANGUAGE1_COL_NAME = "language1";
    private static final String LANGUAGE2_COL_NAME = "language2";
    
    private static final String VOCABLE_TABLE_CREATE =
                "CREATE TABLE " + VOCABLE_TABLE_NAME + " (" +
                		ID_COL_NAME + " INTEGER, " +
                		DICTIONARY_ID_COL_NAME + " INTEGER, " +
                		WORD_COL_NAME + " TEXT, " +
                		TRANSLATION_COL_NAME + " TEXT);";

    private static final String DICTIONARY_TABLE_CREATE =
            "CREATE TABLE " + DICTIONARY_TABLE_NAME + " (" +
            		ID_COL_NAME + " INTEGER, " +
            		NAME_COL_NAME + " TEXT," +
            		LANGUAGE1_COL_NAME + " TEXT," +
            		LANGUAGE2_COL_NAME + " TEXT);";
    
    public static final String PREFS_NAME = "VocableOpenHelperFile";
    
    private Context context;
    
    private static VocableOpenHelper instance;
    
    public static synchronized VocableOpenHelper getInstance(Context context) {
    	if (instance == null) {
        	instance = new VocableOpenHelper(context);
    	}
    	return instance;
    }
    
    private VocableOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
        db.execSQL(VOCABLE_TABLE_CREATE);
        
        loadJsonDefaultDictionary(db);
    }
    
    public void resetDatabase() {
		SQLiteDatabase db = getWritableDatabase();
		resetDatabase(db);
    }

	private void loadJsonDefaultDictionary(final SQLiteDatabase db) {
		int dictionaryIdNext = 1;
        int vocableIdNext = 1;
        
    	db.beginTransaction();
    	try {
			Resources res = context.getResources();

			InputStream in = res.openRawResource(R.raw.default_dictionaries);
			String json = IOUtils.toString(in, StandardCharsets.UTF_8);
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();

			JsonArray dictionaries = root.getAsJsonArray("dictionaries");
			for (JsonElement dictionariesElem : dictionaries) {
				JsonObject dictionary = dictionariesElem.getAsJsonObject();
				
				String name = dictionary.get("name").getAsString();
				String language1 = dictionary.get("language1").getAsString();
				String language2 = dictionary.get("language2").getAsString();
				
		    	int dictionaryId = dictionaryIdNext++;
		    	addDictionary(db, dictionaryId, name, language1, language2);
		    	
		    	JsonArray vocables = dictionary.getAsJsonArray("vocables");
				for (JsonElement vocablesElem : vocables) {
					JsonObject vocable = vocablesElem.getAsJsonObject();
					
					String word = vocable.get("word").getAsString();
					String translation = vocable.get("translation").getAsString();
					
					int vocableId = vocableIdNext++;
					addVocable(db, vocableId, dictionaryId, word, translation);
				}
			}
	    	
    		db.setTransactionSuccessful();
    	} catch (Exception e) {
    		throw new RuntimeException(e.getMessage(), e);
		} finally {
    		db.endTransaction();
    	}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		resetDatabase(db);
	}

	private void resetDatabase(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + VOCABLE_TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + DICTIONARY_TABLE_NAME + ";");
		onCreate(db);
	}
	
	public List<Dictionary> getDictionaries() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(DICTIONARY_TABLE_NAME, new String[] { ID_COL_NAME, NAME_COL_NAME, LANGUAGE1_COL_NAME, LANGUAGE2_COL_NAME }, null, null, null, null, ID_COL_NAME);
		List<Dictionary> list = new LinkedList<>();
		while (cursor.moveToNext()) {
			int id = cursor.getInt(0);
			String name = cursor.getString(1);
			String language1 = cursor.getString(2);
			String language2 = cursor.getString(3);
			list.add(new Dictionary(id, name, language1, language2));
		}
		cursor.close();
		return list;
	}
	
	public List<Vocable> getVocables(int dictionaryId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(VOCABLE_TABLE_NAME, new String[] { ID_COL_NAME, DICTIONARY_ID_COL_NAME, WORD_COL_NAME, TRANSLATION_COL_NAME }, DICTIONARY_ID_COL_NAME + " = ?", new String[] {""+dictionaryId}, null, null, ID_COL_NAME);
		List<Vocable> list = new LinkedList<>();
		while (cursor.moveToNext()) {
			int id = cursor.getInt(0);
			int dictionaryId1 = cursor.getInt(1);
			String word = cursor.getString(2);
			String translation = cursor.getString(3);
			list.add(new Vocable(id, dictionaryId1, word, translation));
		}
		cursor.close();
		return list;
	}
	
	private int getDictionaryIdNext(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery("SELECT MAX(" + ID_COL_NAME + ") FROM " + DICTIONARY_TABLE_NAME, null);
		int ret = 0;
		if (cursor.moveToFirst()) {
			ret = cursor.getInt(0) + 1;
		}
		cursor.close();
		return ret;
	}
	
	private int getVocableIdNext(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery("SELECT MAX(" + ID_COL_NAME + ") FROM " + VOCABLE_TABLE_NAME, null);
		int ret = 0;
		if (cursor.moveToFirst()) {
			ret = cursor.getInt(0) + 1;
		}
		cursor.close();
		return ret;
	}
	
	private void addDictionary(SQLiteDatabase db, int dictionaryId, String name, String language1, String language2) {
		ContentValues values = new ContentValues();
		values.put(ID_COL_NAME, dictionaryId);
		values.put(NAME_COL_NAME, name);
		values.put(LANGUAGE1_COL_NAME, language1);
		values.put(LANGUAGE2_COL_NAME, language2);
		db.insert(DICTIONARY_TABLE_NAME, null, values);
	}
	
	private void addVocable(SQLiteDatabase db, int vocableId, int dictionaryId, String word, String translation) {
		ContentValues values = new ContentValues();
		values.put(ID_COL_NAME, vocableId);
		values.put(DICTIONARY_ID_COL_NAME, dictionaryId);
		values.put(WORD_COL_NAME, word);
		values.put(TRANSLATION_COL_NAME, translation);
		db.insert(VOCABLE_TABLE_NAME, null, values);
	}

	public void persist(Dictionary dictionary, List<Vocable> vocables) {
		SQLiteDatabase db = getWritableDatabase();
		
    	db.beginTransaction();
    	try {
			int dictionaryId = dictionary.getId();
			if (dictionaryId != -1) {
				ContentValues values = new ContentValues();
				values.put(NAME_COL_NAME, dictionary.getName());
				values.put(LANGUAGE1_COL_NAME, dictionary.getLanguage1());
				values.put(LANGUAGE2_COL_NAME, dictionary.getLanguage2());
				int ret = db.update(DICTIONARY_TABLE_NAME, values, ID_COL_NAME + " = ?", new String[] {""+dictionaryId});
				if (ret < 1) {
					// if dictionary was deleted meanwhile
					dictionaryId = -1;
				}
			}
			if (dictionaryId == -1) {
				dictionaryId = getDictionaryIdNext(db);
				addDictionary(db, dictionaryId, dictionary.getName(), dictionary.getLanguage1(), dictionary.getLanguage2());
			}
			
			db.delete(VOCABLE_TABLE_NAME, DICTIONARY_ID_COL_NAME + " = ?", new String[] {""+dictionaryId});
			
			int vocableId = getVocableIdNext(db);
			for (Vocable vocable : vocables) {
				addVocable(db, vocableId, dictionaryId, vocable.getWord(), vocable.getTranslation());
				vocableId++;
			}
	    	
    		db.setTransactionSuccessful();
    	} catch (Exception e) {
    		throw new RuntimeException(e.getMessage(), e);
		} finally {
    		db.endTransaction();
    	}
	}
	
	public void remove(Dictionary dictionary, List<Vocable> vocables) {
		SQLiteDatabase db = getWritableDatabase();
		
    	db.beginTransaction();
    	try {
			if (dictionary.getId() != -1) {
				db.delete(VOCABLE_TABLE_NAME, DICTIONARY_ID_COL_NAME + " = ?", new String[] {""+dictionary.getId()});
				db.delete(DICTIONARY_TABLE_NAME, ID_COL_NAME + " = ?", new String[] {""+dictionary.getId()});
			}
	    	
    		db.setTransactionSuccessful();
    	} catch (Exception e) {
    		throw new RuntimeException(e.getMessage(), e);
		} finally {
    		db.endTransaction();
    	}
	}
}
