package at.hgz.vocabletrainer;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import at.hgz.vocabletrainer.db.Dictionary;
import at.hgz.vocabletrainer.db.Vocable;
import at.hgz.vocabletrainer.db.VocableOpenHelper;

public class DictionaryListActivity extends ListActivity {

	private List<Dictionary> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dictionary_list);

		VocableOpenHelper helper = VocableOpenHelper.getInstance(getApplicationContext());

		list = new ArrayList<Dictionary>();
		for (Dictionary lib : helper.getDictionaries()) {
			list.add(lib);
		}

		final DictionaryArrayAdapter adapter = new DictionaryArrayAdapter(this, R.layout.dictionary_list_item, list);
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
		
		VocableOpenHelper helper = VocableOpenHelper.getInstance(DictionaryListActivity.this);
		Dictionary dictionary = list.get(position1);
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
				DictionaryListActivity.this.startActivity(intent);
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
	       listItemLanguage12.setText(String.format("%s <-> %s",  dictionary.getLanguage1(), dictionary.getLanguage2()));

	       return convertView;
	   }		

	}
}
