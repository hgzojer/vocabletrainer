package at.hgz.vocabletrainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import at.hgz.vocabletrainer.db.Dictionary;
import at.hgz.vocabletrainer.db.Vocable;
import at.hgz.vocabletrainer.db.VocableOpenHelper;
import at.hgz.vocabletrainer.set.TrainingSet;
import at.hgz.vocabletrainer.xml.XmlUtil;
import at.hgz.vocabletrainer.xml.XmlUtil.Entity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

public class DictionaryListActivity extends /*AppCompatActivity*/ ListActivity implements ConnectionCallbacks, OnConnectionFailedListener {

	private static final String TAG = "DictionaryListActivity";
	private static final int EDIT_ACTION = 1;
	private static final int CONFIG_ACTION = 2;
	private static final int IMPORT_ACTION = 3;
	private static final int RESOLVE_CONNECTION_REQUEST_CODE = 4;
	private static final int REQUEST_CODE_CREATOR = 5;
	private static final int REQUEST_CODE_OPENER = 6;
	
	private State state;
	
	private List<Dictionary> list = new ArrayList<>();
	
	private DictionaryArrayAdapter adapter;
	
	private String directionSymbol = "↔";
	
	private GoogleApiClient googleApiClient;
	
	private boolean uploadFlag;
	private boolean driveTransaction;
	
	private boolean alreadyRefreshed;
	
	private ShareActionProvider mShareActionProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dictionary_list);
		
		if (savedInstanceState == null) {
			int id = TrainingApplication.getNextId();
			state = TrainingApplication.getState(id);
		} else {
			int id = savedInstanceState.getInt(State.STATE_ID);
			state = TrainingApplication.getState(id);
		}

        loadConfig();
		loadDirectionSymbol();
		loadDictionaryList();

		adapter = new DictionaryArrayAdapter(this, R.layout.dictionary_list_item, list);
		setListAdapter(adapter);

		Intent intent = getIntent();
		if (intent.getBooleanExtra("import", false) && savedInstanceState == null) {
			int position = list.size() - 1;
			selectDictionary(position);
		}
		alreadyRefreshed = true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(State.STATE_ID, state.getId());
	}

	@Override
	protected void onDestroy() {
        saveConfig();
        if (isFinishing()) {
        	TrainingApplication.removeState(state.getId());
        }
		super.onDestroy();
	}

	@Override
	protected void onPause() {
        saveConfig();
        if (googleApiClient != null && !driveTransaction) {
            googleApiClient.disconnect();
        }
		super.onPause();
	}

	@Override
	protected void onStop() {
        saveConfig();
		super.onStop();
	}

	private void loadConfig() {
		SharedPreferences settings = DictionaryListActivity.this.getPreferences(MODE_PRIVATE);
        int direction = settings.getInt(ConfigActivity.TRANSLATION_DIRECTION, TrainingSet.DIRECTION_BIDIRECTIONAL);
        state.setDirection(direction);
	}

	private void saveConfig() {
		if (state.hasConfigChanged()) {
			SharedPreferences settings = this.getPreferences(MODE_PRIVATE);
	        SharedPreferences.Editor editor = settings.edit();
			editor.putInt(ConfigActivity.TRANSLATION_DIRECTION, state.getDirection());
			editor.commit();
			state.setConfigChanged(false);
		}
	}

	private boolean isDictionarySelected() {
		return state.getDictionary() != null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.dictionary_list_menu, menu);
	    MenuItem item = menu.findItem(R.id.menu_item_share);
	    mShareActionProvider = (ShareActionProvider) item.getActionProvider();
	    return true;
	}
	
	private void setShareIntent(Intent shareIntent) {
	    if (mShareActionProvider != null) {
	        mShareActionProvider.setShareIntent(shareIntent);
	    }
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem exportToExternalStorage = menu.findItem(R.id.exportToExternalStorage);
		exportToExternalStorage.setVisible(isDictionarySelected());
		MenuItem uploadToGoogleDrive = menu.findItem(R.id.uploadToGoogleDrive);
		uploadToGoogleDrive.setVisible(isDictionarySelected());
		MenuItem share = menu.findItem(R.id.menu_item_share);
		share.setVisible(isDictionarySelected());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.addDictionary:
	        {
	        	state.setDictionary(new Dictionary(-1, "", "", ""));
	        	List<Vocable> vocables = new ArrayList<>(1);
	        	vocables.add(new Vocable(-1, -1, "", ""));
	        	vocables.add(new Vocable(-1, -1, "", ""));
	        	vocables.add(new Vocable(-1, -1, "", ""));
	        	vocables.add(new Vocable(-1, -1, "", ""));
	        	vocables.add(new Vocable(-1, -1, "", ""));
	        	state.setVocables(vocables);
				Intent intent = new Intent(DictionaryListActivity.this, VocableListActivity.class);
				intent.putExtra(State.STATE_ID, state.getId());
				DictionaryListActivity.this.startActivityForResult(intent, EDIT_ACTION);
	            return true;
	        }
	        case R.id.openConfig:
	        {
				Intent intent = new Intent(DictionaryListActivity.this, ConfigActivity.class);
				intent.putExtra(State.STATE_ID, state.getId());
				DictionaryListActivity.this.startActivityForResult(intent, CONFIG_ACTION);
	            return true;
	        }
	        case R.id.exportToExternalStorage:
	        {
				exportDictionaryToExternalStorage();
	        	return true;
	        }
	        case R.id.importFromExternalStorage:
	        {
				Intent intent = new Intent(DictionaryListActivity.this, ImportActivity.class);
				intent.putExtra(State.STATE_ID, state.getId());
				DictionaryListActivity.this.startActivityForResult(intent, IMPORT_ACTION);
	        	return true;
	        }
	        case R.id.uploadToGoogleDrive:
	        {
				uploadToGoogleDrive();
	        	return true;
	        }
	        case R.id.downloadFromGoogleDrive:
	        {
				downloadFromGoogleDrive();
	        	return true;
	        }
	        case R.id.menu_item_share:
	        {
	        	provideShareIntent();
	            return super.onOptionsItemSelected(item);
	        }
	        case R.id.about:
	        {
				Intent intent = new Intent(DictionaryListActivity.this, AboutActivity.class);
	        	DictionaryListActivity.this.startActivity(intent);
	        	return true;
	        }
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private File exportDictionaryToCacheDir() {
		XmlUtil util = XmlUtil.getInstance();
		Dictionary dictionary = state.getDictionary();
		List<Vocable> vocables = state.getVocables();
		byte[] dictionaryBytes = util.marshall(dictionary, vocables);
		File storageDir = getCacheDir();
		if (!storageDir.exists()) {
			if (!storageDir.mkdirs()) {
				Log.d(TAG, "failed to create directory");
			}
		}
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
	    File file;
	    int i = 1;
	    do {
	    	file = new File(storageDir, "DICT_"+ timeStamp + (i > 1 ? "_" + i : "") + ".vt");
	    	i++;
	    } while (file.exists());
	    try (OutputStream out = new FileOutputStream(file)) {
			out.write(dictionaryBytes);
			out.flush();
	    } catch (IOException ex) {
	    	throw new RuntimeException(ex.getMessage(), ex);
	    }
	    return file;
	}

	private void exportDictionaryToExternalStorage() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			XmlUtil util = XmlUtil.getInstance();
			Dictionary dictionary = state.getDictionary();
			List<Vocable> vocables = state.getVocables();
			byte[] dictionaryBytes = util.marshall(dictionary, vocables);
			File storageDir = ImportActivity.getSDCardDir(this);
			if (!storageDir.exists()) {
				if (!storageDir.mkdirs()) {
					Log.d(TAG, "failed to create directory");
				}
			}
		    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		    File file;
		    int i = 1;
		    do {
		    	file = new File(storageDir, "DICT_"+ timeStamp + (i > 1 ? "_" + i : "") + ".vt");
		    	i++;
		    } while (file.exists());
		    try (OutputStream out = new FileOutputStream(file)) {
				out.write(dictionaryBytes);
				out.flush();
		    } catch (IOException ex) {
		    	throw new RuntimeException(ex.getMessage(), ex);
		    }
			String text = getResources().getString(R.string.exportedDictionary, file.getName());
		    Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		} else {
			String text = getResources().getString(R.string.errorExportingDictionary);
		    Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}
	}
	
	private void uploadToGoogleDrive() {
		uploadFlag = true;
		driveTransaction = true;
        connectToGoogleDrive();
 	}
	
	private void downloadFromGoogleDrive() {
		uploadFlag = false;
		driveTransaction = true;
        connectToGoogleDrive();
 	}

	private void connectToGoogleDrive() {
		if (googleApiClient == null) {
        	googleApiClient = new GoogleApiClient.Builder(this)
	            .addApi(Drive.API)
	            .addScope(Drive.SCOPE_FILE)
	            .addConnectionCallbacks(this)
	            .addOnConnectionFailedListener(this)
	            .build();
        }
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        } else {
    		if (uploadFlag) {
    			doUploadToGoogleDrive();
    		} else {
    			doDownloadFromGoogleDrive();
    		}
        }
	}
	
	@Override
	public void onConnected(Bundle bundle) {
		Log.d(TAG, "API client connected.");

		if (uploadFlag) {
			doUploadToGoogleDrive();
		} else {
			doDownloadFromGoogleDrive();
		}
	}
	
	private void doUploadToGoogleDrive() {
		ResultCallback<ContentsResult> newContentsCallback = new
		        ResultCallback<ContentsResult>() {
		    @Override
		    public void onResult(ContentsResult result) {
		    	try {
				    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
			    	String title = "DICT_"+ timeStamp + ".vt";
			    	
					XmlUtil util = XmlUtil.getInstance();
					Dictionary dictionary = state.getDictionary();
					List<Vocable> vocables = state.getVocables();
					byte[] dictionaryBytes = util.marshall(dictionary, vocables);
	
				    try (OutputStream out = result.getContents().getOutputStream()) {
						out.write(dictionaryBytes);
						out.flush();
				    } catch (IOException ex) {
				    	throw new RuntimeException(ex.getMessage(), ex);
				    }
			    	
			        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
			                .setMimeType("application/vnd.hgz.vocabletrainer")
			                .setTitle(title).build();
			        IntentSender intentSender = Drive.DriveApi
			                 .newCreateFileActivityBuilder()
			                 .setInitialMetadata(metadataChangeSet)
			                 .setInitialContents(result.getContents())
			                 .build(DictionaryListActivity.this.googleApiClient);
			        try {
			            startIntentSenderForResult(
			                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
			        } catch (SendIntentException e) {
			            Log.w(TAG, "Unable to send intent", e);
						String text = getResources().getString(R.string.errorUploadingDictionary);
					    Toast.makeText(DictionaryListActivity.this, text, Toast.LENGTH_LONG).show();
			        }
		    	} finally {
		    		driveTransaction = false;
		    	}
		    }
		};
		Drive.DriveApi.newContents(googleApiClient).setResultCallback(newContentsCallback);
	}
	
	private void doDownloadFromGoogleDrive() {
	    IntentSender intentSender = Drive.DriveApi
	            .newOpenFileActivityBuilder()
	            .setMimeType(new String[] { "application/vnd.hgz.vocabletrainer" })
	            .build(googleApiClient);
	    try {
	        startIntentSenderForResult(
	                intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
	    } catch (SendIntentException e) {
	        Log.w(TAG, "Unable to send intent", e);
			String text = getResources().getString(R.string.errorDownloadingDictionary);
		    Toast.makeText(DictionaryListActivity.this, text, Toast.LENGTH_LONG).show();
	    }
	}

	private boolean doDownloadFromGoogleDriveNow(Contents contents) {
		try {
			InputStream in = contents.getInputStream();
			byte[] dictionaryBytes = IOUtils.toByteArray(in);
			XmlUtil util = XmlUtil.getInstance();
			Entity entity = util.unmarshall(dictionaryBytes);
			Resources resources = getApplicationContext().getResources();
			String text = resources.getString(R.string.downloadingDictionary);
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
			VocableOpenHelper helper = VocableOpenHelper.getInstance(getApplicationContext());
			helper.persist(entity.getDictionary(), entity.getVocables());
			return true;
		} catch (Exception ex) {
			Resources resources = getApplicationContext().getResources();
			String text = resources.getString(R.string.errorDownloadingDictionary);
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
			return false;
		} finally {
			driveTransaction = false;
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.d(TAG, "API client connection suspended.");
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	    if (connectionResult.hasResolution()) {
	        try {
	            connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
	        } catch (IntentSender.SendIntentException e) {
	            // TODO Unable to resolve, message user appropriately
	        }
	    } else {
	        GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
	    }
	}

	private void loadDictionaryList() {
		VocableOpenHelper helper = VocableOpenHelper.getInstance(getApplicationContext());
		list.clear();
		list.addAll(helper.getDictionaries());
	}

	private void loadDirectionSymbol() {
		int direction = state.getDirection();
		switch (direction) {
		case TrainingSet.DIRECTION_FORWARD:
			directionSymbol = "→";
			break;
		case TrainingSet.DIRECTION_BIDIRECTIONAL:
			directionSymbol = "↔";
			break;
		case TrainingSet.DIRECTION_BACKWARD:
			directionSymbol = "←";
			break;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		selectDictionary(position);
	}

	private void selectDictionary(int position) {
		loadDictionaryVocables(position);
		adapter.notifyDataSetChanged();
		setSelection(position);
		provideShareIntent();
	}

	private void provideShareIntent() {
		// TODO
		if (isDictionarySelected()) {
			File file = exportDictionaryToCacheDir();
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("application/vnd.hgz.vocabletrainer");
			Uri uri = Uri.parse("content://at.hgz.vocabletrainer.provider/" + file.getName());
			shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
			setShareIntent(shareIntent);
		}
	}

	private void loadDictionaryVocables(final int position) {
		VocableOpenHelper helper = VocableOpenHelper.getInstance(DictionaryListActivity.this);
		Dictionary dictionary = list.get(position);
		List<Vocable> vocables = helper.getVocables(dictionary.getId());
		state.setDictionary(dictionary);
		state.setVocables(vocables);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!alreadyRefreshed) {
			int dictionaryId = -1;
			if (state.getDictionary() != null) {
				dictionaryId = state.getDictionary().getId();
			}
			loadDictionaryList();
			boolean found = false;
			if (dictionaryId != -1) {
				for (Dictionary dictionary : list) {
					if (dictionary.getId() == dictionaryId) {
						int position = list.indexOf(dictionary);
						selectDictionary(position);
						found = true;
					}
				}
			}
			if (!found) {
				state.setDictionary(null);
				state.setVocables(null);
				adapter.notifyDataSetChanged();
			}
		}
		alreadyRefreshed = false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == EDIT_ACTION) {
			String result = "";
			if (resultCode == RESULT_OK) {
				result = data.getStringExtra("result");
				if ("save".equals(result)) {
					int position = list.indexOf(state.getDictionary());
					if (position != -1) {
						selectDictionary(position);
					}
				} else if ("add".equals(result)) {
					loadDictionaryList();
					int position = list.size() - 1;
					selectDictionary(position);
					alreadyRefreshed = true;
				} else if ("delete".equals(result)) {
					loadDictionaryList();
					state.setDictionary(null);
					state.setVocables(null);
					adapter.notifyDataSetChanged();
					alreadyRefreshed = true;
				}
			}
			if (resultCode == RESULT_CANCELED) {
			}
		}
		
		if (requestCode == CONFIG_ACTION) {
			loadDirectionSymbol();
			adapter.notifyDataSetChanged();
		}
		
		if (requestCode == IMPORT_ACTION) {
			if (resultCode == RESULT_OK) {
				if (importDictionaryFromExternalStorage(this, data.getData())) {
					loadDictionaryList();
					int position = list.size() - 1;
					selectDictionary(position);
					alreadyRefreshed = true;
				}
			}
			if (resultCode == RESULT_CANCELED) {
			}
		}
		
		if (requestCode == RESOLVE_CONNECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                googleApiClient.connect();
            }
		}
		
		if (requestCode == REQUEST_CODE_CREATOR) {
            if (resultCode == RESULT_OK) {
    			String text = getResources().getString(R.string.uploadedDictionary);
    		    Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            }
            if (resultCode == RESULT_CANCELED) {
	    		driveTransaction = false;
            }
		}
		
		if (requestCode == REQUEST_CODE_OPENER) {
            if (resultCode == RESULT_OK) {
            	DriveId driveId = (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
            	
        		DriveFile driveFile = Drive.DriveApi.getFile(googleApiClient, driveId);
        		ResultCallback<ContentsResult> contentsOpenedCallback =
        		        new ResultCallback<ContentsResult>() {
        		    @Override
        		    public void onResult(ContentsResult result) {
        		        if (!result.getStatus().isSuccess()) {
        	    			String text = getResources().getString(R.string.errorDownloadingDictionary);
        	    		    Toast.makeText(DictionaryListActivity.this, text, Toast.LENGTH_LONG).show();
        		    		driveTransaction = false;
        		            return;
        		        }
        		        Contents contents = result.getContents();
        				if (doDownloadFromGoogleDriveNow(contents)) {
        					loadDictionaryList();
        					int position = list.size() - 1;
        					selectDictionary(position);
        				}
        		    }
        		};
        		driveFile.openContents(googleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(contentsOpenedCallback);
            }
            if (resultCode == RESULT_CANCELED) {
	    		driveTransaction = false;
            }
		}
	}

	public static boolean importDictionaryFromExternalStorage(Activity activity, Uri importFile) {
		try {
			InputStream in = activity.getContentResolver().openInputStream(importFile);
			byte[] dictionaryBytes = IOUtils.toByteArray(in);
			XmlUtil util = XmlUtil.getInstance();
			Entity entity = util.unmarshall(dictionaryBytes);
			Resources resources = activity.getApplicationContext().getResources();
			String text = resources.getString(R.string.importingDictionary);
			Toast toast = Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
			VocableOpenHelper helper = VocableOpenHelper.getInstance(activity.getApplicationContext());
			helper.persist(entity.getDictionary(), entity.getVocables());
			return true;
		} catch (Exception ex) {
			Resources resources = activity.getApplicationContext().getResources();
			String text = resources.getString(R.string.errorImportingDictionary);
			Toast toast = Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
			return false;
		}
	}

	private class DictionaryArrayAdapter extends ArrayAdapter<Dictionary> {

		public DictionaryArrayAdapter(Context context, int resource,
				List<Dictionary> objects) {
			super(context, resource, objects);
		}
		
		private class ViewHolder {
			public TextView listItemName;
			public TextView listItemLanguage12;
			public Dictionary dictionary;
			public TextView listItemCount;
			public Button buttonEdit;
			public Button buttonTraining;
			public Button buttonMultipleChoice;
		}

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {

			Dictionary dictionary = getItem(position);

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.dictionary_list_item, parent, false);
				final ViewHolder vh = new ViewHolder();
				vh.listItemName = (TextView) convertView.findViewById(R.id.listItemName);
				vh.listItemLanguage12 = (TextView) convertView.findViewById(R.id.listItemLanguage12);
				vh.listItemCount = (TextView) convertView.findViewById(R.id.listItemCount);
				vh.buttonEdit = (Button) convertView.findViewById(R.id.buttonEdit);
				vh.buttonTraining = (Button) convertView.findViewById(R.id.buttonTraining);
				vh.buttonMultipleChoice = (Button) convertView.findViewById(R.id.buttonMultipleChoice);
				vh.buttonEdit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(DictionaryListActivity.this, VocableListActivity.class);
						intent.putExtra(State.STATE_ID, state.getId());
						DictionaryListActivity.this.startActivityForResult(intent, EDIT_ACTION);
					}
				});
				vh.buttonTraining.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(DictionaryListActivity.this, TrainingActivity.class);
						intent.putExtra(State.STATE_ID, state.getId());
						DictionaryListActivity.this.startActivity(intent);
					}
				});
				vh.buttonMultipleChoice.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(DictionaryListActivity.this, MultipleChoiceActivity.class);
						intent.putExtra(State.STATE_ID, state.getId());
						DictionaryListActivity.this.startActivity(intent);
					}
				});

				convertView.setTag(vh);
			}

			ViewHolder vh = (ViewHolder) convertView.getTag();
			vh.dictionary = dictionary;
			vh.listItemName.setText(dictionary.getName());
		    vh.listItemLanguage12.setText(String.format("%s %s %s",  dictionary.getLanguage1(), directionSymbol, dictionary.getLanguage2()));
			int visibility = View.GONE;
			int visibilityTraining = View.GONE;
			if (vh.dictionary == state.getDictionary()) {
				visibility = View.VISIBLE;
				int count = state.getVocables().size();
				Resources resources = getApplicationContext().getResources();
				vh.listItemCount.setText(resources.getString(R.string.count, count));
				if (count > 0) {
					visibilityTraining = View.VISIBLE;
				}
			}
			vh.listItemCount.setVisibility(visibility);
			vh.buttonEdit.setVisibility(visibility);
			vh.buttonTraining.setVisibility(visibilityTraining);
			vh.buttonMultipleChoice.setVisibility(visibilityTraining);

			return convertView;
		}

	}
}
