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
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

public class DictionaryListActivity extends ListActivity implements ConnectionCallbacks, OnConnectionFailedListener {

	private static final String TAG = "DictionaryListActivity";
	private static final int EDIT_ACTION = 1;
	private static final int CONFIG_ACTION = 2;
	private static final int IMPORT_ACTION = 3;
	private static final int RESOLVE_CONNECTION_REQUEST_CODE = 4;
	private static final int REQUEST_CODE_CREATOR = 5;
	private List<Dictionary> list = new ArrayList<Dictionary>();
	
	private DictionaryArrayAdapter adapter;
	
	private String directionSymbol = "↔";
	
	private GoogleApiClient googleApiClient;
	
	private boolean uploadFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dictionary_list);

        loadConfig();
		loadDirectionSymbol();
		loadDictionaryList();

		adapter = new DictionaryArrayAdapter(this, R.layout.dictionary_list_item, list);
		setListAdapter(adapter);
	}
	
	@Override
	protected void onDestroy() {
        saveConfig();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
        saveConfig();
        if (googleApiClient != null) {
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
        TrainingApplication.getState().setDirection(direction);
	}

	private void saveConfig() {
		if (TrainingApplication.getState().hasConfigChanged()) {
			SharedPreferences settings = this.getPreferences(MODE_PRIVATE);
	        SharedPreferences.Editor editor = settings.edit();
			editor.putInt(ConfigActivity.TRANSLATION_DIRECTION, TrainingApplication.getState().getDirection());
			editor.commit();
			TrainingApplication.getState().setConfigChanged(false);
		}
	}

	private boolean isDictionarySelected() {
		return TrainingApplication.getState().getDictionary() != null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.dictionary_list_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem exportToExternalStorage = menu.findItem(R.id.exportToExternalStorage);
		exportToExternalStorage.setVisible(isDictionarySelected());
		MenuItem uploadToGoogleDrive = menu.findItem(R.id.uploadToGoogleDrive);
		uploadToGoogleDrive.setVisible(isDictionarySelected());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.addDictionary:
	        {
	        	TrainingApplication.getState().setDictionary(new Dictionary(-1, "", "", ""));
	        	List<Vocable> vocables = new ArrayList<Vocable>(1);
	        	vocables.add(new Vocable(-1, -1, "", ""));
	        	vocables.add(new Vocable(-1, -1, "", ""));
	        	vocables.add(new Vocable(-1, -1, "", ""));
	        	vocables.add(new Vocable(-1, -1, "", ""));
	        	vocables.add(new Vocable(-1, -1, "", ""));
	        	TrainingApplication.getState().setVocables(vocables);
				Intent intent = new Intent(DictionaryListActivity.this, VocableListActivity.class);
				//intent.putExtra("dictionaryId", dictionaryId);
				DictionaryListActivity.this.startActivityForResult(intent, EDIT_ACTION);
	            return true;
	        }
	        case R.id.openConfig:
	        {
				Intent intent = new Intent(DictionaryListActivity.this, ConfigActivity.class);
				//intent.putExtra("dictionaryId", dictionaryId);
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
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void exportDictionaryToExternalStorage() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			XmlUtil util = XmlUtil.getInstance();
			Dictionary dictionary = TrainingApplication.getState().getDictionary();
			List<Vocable> vocables = TrainingApplication.getState().getVocables();
			byte[] dictionaryBytes = util.marshall(dictionary, vocables);
			File storageDir = getExternalFilesDir(null);
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
		    try {
			    OutputStream out = new FileOutputStream(file);
			    try {
			    	out.write(dictionaryBytes);
			    	out.flush();
			    } catch (IOException ex) {
			    	
			    } finally {
			    	if (out != null) {
			    		out.close();
			    	}
			    }
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
        connectToGoogleDrive();
 	}
	
	private void downloadFromGoogleDrive() {
		uploadFlag = false;
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
	    }
	}

	private void doUploadToGoogleDrive() {
		ResultCallback<ContentsResult> newContentsCallback = new
		        ResultCallback<ContentsResult>() {
		    @Override
		    public void onResult(ContentsResult result) {
			    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		    	String title = "DICT_"+ timeStamp + ".vt";
		    	
				XmlUtil util = XmlUtil.getInstance();
				Dictionary dictionary = TrainingApplication.getState().getDictionary();
				List<Vocable> vocables = TrainingApplication.getState().getVocables();
				byte[] dictionaryBytes = util.marshall(dictionary, vocables);

			    try {
				    OutputStream out = result.getContents().getOutputStream();
				    try {
				    	out.write(dictionaryBytes);
				    	out.flush();
				    } catch (IOException ex) {
				    	
				    } finally {
				    	if (out != null) {
				    		out.close();
				    	}
				    }
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
		    }
		};
		Drive.DriveApi.newContents(googleApiClient).setResultCallback(newContentsCallback);
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
		for (Dictionary lib : helper.getDictionaries()) {
			list.add(lib);
		}
	}

	private void loadDirectionSymbol() {
		int direction = TrainingApplication.getState().getDirection();
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
		
		loadDictionaryVocables(position);
		adapter.notifyDataSetChanged();
		setSelection(position);
	}

	private void loadDictionaryVocables(final int position) {
		VocableOpenHelper helper = VocableOpenHelper.getInstance(DictionaryListActivity.this);
		Dictionary dictionary = list.get(position);
		List<Vocable> vocables = helper.getVocables(dictionary.getId());
		TrainingApplication.getState().setDictionary(dictionary);
		TrainingApplication.getState().setVocables(vocables);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == EDIT_ACTION) {
			String result = "";
			if (resultCode == RESULT_OK) {
				result = data.getStringExtra("result");
				if ("save".equals(result)) {
					int position = list.indexOf(TrainingApplication.getState().getDictionary());
					loadDictionaryVocables(position);
					adapter.notifyDataSetChanged();
					setSelection(position);
				} else if ("add".equals(result)) {
					loadDictionaryList();
					int position = list.size() - 1;
					loadDictionaryVocables(position);
					adapter.notifyDataSetChanged();
					setSelection(position);
				} else if ("delete".equals(result)) {
					loadDictionaryList();
					TrainingApplication.getState().setDictionary(null);
					TrainingApplication.getState().setVocables(null);
					adapter.notifyDataSetChanged();
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
				if (importDictionaryFromExternalStorage(data.getData())) {
					loadDictionaryList();
					int position = list.size() - 1;
					loadDictionaryVocables(position);
					adapter.notifyDataSetChanged();
					setSelection(position);
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
            }
		}
	}

	private boolean importDictionaryFromExternalStorage(Uri importFile) {
		try {
			InputStream in = getContentResolver().openInputStream(importFile);
			byte[] dictionaryBytes = IOUtils.toByteArray(in);
			XmlUtil util = XmlUtil.getInstance();
			Entity entity = util.unmarshall(dictionaryBytes);
			Resources resources = getApplicationContext().getResources();
			String text = resources.getString(R.string.importingDictionary);
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
			VocableOpenHelper helper = VocableOpenHelper.getInstance(getApplicationContext());
			helper.persist(entity.getDictionary(), entity.getVocables());
			return true;
		} catch (Exception ex) {
			Resources resources = getApplicationContext().getResources();
			String text = resources.getString(R.string.errorImportingDictionary);
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
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
						//intent.putExtra("dictionaryId", dictionaryId);
						DictionaryListActivity.this.startActivityForResult(intent, EDIT_ACTION);
					}
				});
				vh.buttonTraining.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(DictionaryListActivity.this, TrainingActivity.class);
						//intent.putExtra("dictionaryId", dictionaryId);
						DictionaryListActivity.this.startActivity(intent);
					}
				});
				vh.buttonMultipleChoice.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(DictionaryListActivity.this, MultipleChoiceActivity.class);
						//intent.putExtra("dictionaryId", dictionaryId);
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
			if (vh.dictionary == TrainingApplication.getState().getDictionary()) {
				visibility = View.VISIBLE;
				int count = TrainingApplication.getState().getVocables().size();
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
