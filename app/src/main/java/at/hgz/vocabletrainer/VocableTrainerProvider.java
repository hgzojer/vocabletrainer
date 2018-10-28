package at.hgz.vocabletrainer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.util.Log;

public class VocableTrainerProvider extends ContentProvider {

	public static final String MIMETYPE_VOCABLETRAINER_JSON = "application/vnd.hgz.vocabletrainer.json";
	public static final String MIMETYPE_VOCABLETRAINER_CSV = "application/vnd.hgz.vocabletrainer.csv";
	public static final String MIMETYPE_VOCABLETRAINER = "application/vnd.hgz.vocabletrainer";

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		try {
			String cacheDir = getContext().getCacheDir().toString();
			File privateFile = new File(cacheDir, uri.getLastPathSegment());

			if (!privateFile.getCanonicalPath().startsWith(cacheDir)) {
				throw new IllegalArgumentException();
			}

			return ParcelFileDescriptor.open(privateFile, ParcelFileDescriptor.MODE_READ_ONLY);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		String path = uri.getPath();
		return getMimeType(path);
	}

	@NonNull
	public static String getMimeType(String path) {
		String mimeType;
		if (path.endsWith(".vtj")) {
			mimeType = MIMETYPE_VOCABLETRAINER_JSON;
		} else if (path.endsWith(".vtc")) {
			mimeType = MIMETYPE_VOCABLETRAINER_CSV;
		} else {
			mimeType = MIMETYPE_VOCABLETRAINER;
		}
		return mimeType;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

}
