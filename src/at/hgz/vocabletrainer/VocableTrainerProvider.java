package at.hgz.vocabletrainer;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class VocableTrainerProvider extends ContentProvider {
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {       
		Log.e("abc", "openFile");
	     File cacheDir = getContext().getCacheDir();
	     File privateFile = new File(cacheDir, uri.getLastPathSegment());
	     Log.d("abc", "" + privateFile);

	     return ParcelFileDescriptor.open(privateFile, ParcelFileDescriptor.MODE_READ_ONLY);
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		Log.e("abc", "delete");
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		Log.e("abc", "getType");
		return "application/vnd.hgz.vocabletrainer";
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		Log.e("abc", "insert");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		Log.e("abc", "onCreate");
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		Log.e("abc", "query");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		Log.e("abc", "update");
		// TODO Auto-generated method stub
		return 0;
	}

}
