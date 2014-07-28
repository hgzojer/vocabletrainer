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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.hgz.vocabletrainer.db.Dictionary;
import at.hgz.vocabletrainer.db.Vocable;
import at.hgz.vocabletrainer.db.VocableOpenHelper;
import at.hgz.vocabletrainer.set.TrainingSet;
import at.hgz.vocabletrainer.xml.XmlUtil;
import at.hgz.vocabletrainer.xml.XmlUtil.Entity;

public class DictionaryListActivity extends ListActivity {

	private static final int EDIT_ACTION = 1;
	private static final int CONFIG_ACTION = 2;
	private static final int IMPORT_ACTION = 3;
	private List<Dictionary> list = new ArrayList<Dictionary>();
	
	private DictionaryArrayAdapter adapter;
	
	private String directionSymbol = "↔";
	
	private boolean dictionarySelected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dictionary_list);

        SharedPreferences settings = DictionaryListActivity.this.getPreferences(MODE_PRIVATE);
        int direction = settings.getInt(ConfigActivity.TRANSLATION_DIRECTION, TrainingSet.DIRECTION_BIDIRECTIONAL);
        TrainingApplication.getState().setDirection(direction);

		loadDictionaryList();
	}
	
	@Override
	protected void onDestroy() {
        saveConfig();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
        saveConfig();
		super.onPause();
	}

	@Override
	protected void onStop() {
        saveConfig();
		super.onStop();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.dictionary_list_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem exportToExternalStorage = menu.findItem(R.id.exportToExternalStorage);
		exportToExternalStorage.setVisible(dictionarySelected);
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
					Log.d("DictionaryListActivity", "failed to create directory");
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

	private void loadDictionaryList() {
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

		VocableOpenHelper helper = VocableOpenHelper.getInstance(getApplicationContext());
		list.clear();
		for (Dictionary lib : helper.getDictionaries()) {
			list.add(lib);
		}

		adapter = new DictionaryArrayAdapter(this, R.layout.dictionary_list_item, list);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		final int position1 = position;
		for (int i = 0; i < l.getChildCount(); i++) {
			if (i != position) {
				View c = l.getChildAt(i);
				c.findViewById(R.id.listItemCount).setVisibility(View.GONE);
				c.findViewById(R.id.buttonEdit).setVisibility(View.GONE);
				c.findViewById(R.id.buttonTraining).setVisibility(View.GONE);
				c.findViewById(R.id.buttonMultipleChoice).setVisibility(View.GONE);
			}
		}
		dictionarySelected = true;
		
		expandItem(v, position1);
	}

	private void expandItem(View v, final int position) {
		VocableOpenHelper helper = VocableOpenHelper.getInstance(DictionaryListActivity.this);
		Dictionary dictionary = list.get(position);
		List<Vocable> vocables = helper.getVocables(dictionary.getId());
		TrainingApplication.getState().setDictionary(dictionary);
		TrainingApplication.getState().setVocables(vocables);
		int count = vocables.size();
		
		TextView listItemCount = (TextView) v.findViewById(R.id.listItemCount);
		Resources resources = getApplicationContext().getResources();
		listItemCount.setText(resources.getString(R.string.count, count));
		listItemCount.setVisibility(View.VISIBLE);
		
		View buttonEdit = v.findViewById(R.id.buttonEdit);
		buttonEdit.setVisibility(View.VISIBLE);
		buttonEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(DictionaryListActivity.this, VocableListActivity.class);
				//intent.putExtra("dictionaryId", dictionaryId);
				DictionaryListActivity.this.startActivityForResult(intent, EDIT_ACTION);
			}
		});
		
		if (count > 0) {
			View buttonTraining = v.findViewById(R.id.buttonTraining);
			buttonTraining.setVisibility(View.VISIBLE);
			buttonTraining.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(DictionaryListActivity.this, TrainingActivity.class);
					//intent.putExtra("dictionaryId", dictionaryId);
					DictionaryListActivity.this.startActivity(intent);
				}
			});
			
			View buttonMultipleChoice = v.findViewById(R.id.buttonMultipleChoice);
			buttonMultipleChoice.setVisibility(View.VISIBLE);
			buttonMultipleChoice.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(DictionaryListActivity.this, MultipleChoiceActivity.class);
					//intent.putExtra("dictionaryId", dictionaryId);
					DictionaryListActivity.this.startActivity(intent);
				}
			});
		}
		setSelection(position);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EDIT_ACTION || requestCode == CONFIG_ACTION || requestCode == IMPORT_ACTION) {
			String result = "";
			if (resultCode == RESULT_OK) {
				result = data.getStringExtra("result");
				if (requestCode == IMPORT_ACTION) {
					importDictionaryFromExternalStorage(data.getData());
				}
			}
			if (resultCode == RESULT_CANCELED) {
			}
			
			ListView l = getListView();
			for (int i = 0; i < l.getChildCount(); i++) {
				View c = l.getChildAt(i);
				c.findViewById(R.id.listItemCount).setVisibility(View.GONE);
				c.findViewById(R.id.buttonEdit).setVisibility(View.GONE);
				c.findViewById(R.id.buttonTraining).setVisibility(View.GONE);
				c.findViewById(R.id.buttonMultipleChoice).setVisibility(View.GONE);
			}
			dictionarySelected = false;
			loadDictionaryList();
			adapter.notifyDataSetChanged();
			if ("save".equals(result)) {
				for (int i = 0; i < l.getChildCount(); i++) {
					if (l.getItemAtPosition(i) == TrainingApplication.getState().getDictionary()) {
						View c = l.getChildAt(i);
						expandItem(c, i);
					}
				}
			}
		}
	}

	private void importDictionaryFromExternalStorage(Uri importFile) {
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
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private class DictionaryArrayAdapter extends ArrayAdapter<Dictionary> {

		public DictionaryArrayAdapter(Context context, int resource,
				List<Dictionary> objects) {
			super(context, resource, objects);
		}

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {

	       Dictionary dictionary = getItem(position);    

	       if (convertView == null) {
	          convertView = LayoutInflater.from(getContext()).inflate(R.layout.dictionary_list_item, parent, false);
	       }

	       TextView listItemName = (TextView) convertView.findViewById(R.id.listItemName);
	       TextView listItemLanguage12 = (TextView) convertView.findViewById(R.id.listItemLanguage12);
	        
	       listItemName.setText(dictionary.getName());
	       listItemLanguage12.setText(String.format("%s %s %s",  dictionary.getLanguage1(), directionSymbol, dictionary.getLanguage2()));

	       return convertView;
	   }		

	}
}
