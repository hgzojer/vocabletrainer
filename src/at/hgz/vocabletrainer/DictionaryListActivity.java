package at.hgz.vocabletrainer;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
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
import at.hgz.vocabletrainer.db.Dictionary;
import at.hgz.vocabletrainer.db.Vocable;
import at.hgz.vocabletrainer.db.VocableOpenHelper;
import at.hgz.vocabletrainer.set.TrainingSet;

public class DictionaryListActivity extends ListActivity {

	private static final int EDIT_ACTION = 1;
	private static final int CONFIG_ACTION = 1;
	private List<Dictionary> list = new ArrayList<Dictionary>();
	
	private DictionaryArrayAdapter adapter;
	
	private String directionSymbol = "<->";

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
        SharedPreferences settings = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
		editor.putInt(ConfigActivity.TRANSLATION_DIRECTION, TrainingApplication.getState().getDirection());
		editor.commit();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.dictionary_list_menu, menu);
	    return true;
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
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void loadDictionaryList() {
		int direction = TrainingApplication.getState().getDirection();
		switch (direction) {
		case TrainingSet.DIRECTION_FORWARD:
			directionSymbol = "->";
			break;
		case TrainingSet.DIRECTION_BIDIRECTIONAL:
			directionSymbol = "<->";
			break;
		case TrainingSet.DIRECTION_BACKWARD:
			directionSymbol = "<-";
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
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EDIT_ACTION || requestCode == CONFIG_ACTION) {
			String result = "";
			if (resultCode == RESULT_OK) {
				result = data.getStringExtra("result");
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
