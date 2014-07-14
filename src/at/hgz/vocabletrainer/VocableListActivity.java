package at.hgz.vocabletrainer;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import at.hgz.vocabletrainer.db.Vocable;

public class VocableListActivity extends ListActivity {
	
	private VocableArrayAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vocable_list);

		State state = TrainingApplication.getState();
		
		EditText editTextDictionaryName = (EditText) findViewById(R.id.editTextDictionaryName);
		editTextDictionaryName.setText(state.getDictionary().getName());
		EditText editTextLanguage1 = (EditText) findViewById(R.id.editTextLanguage1);
		editTextLanguage1.setText(state.getDictionary().getLanguage1());
		EditText editTextLanguage2 = (EditText) findViewById(R.id.editTextLanguage2);
		editTextLanguage2.setText(state.getDictionary().getLanguage2());

		adapter = new VocableArrayAdapter(this, R.layout.vocable_list_item, state.getVocables());
		setListAdapter(adapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.vocable_list_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.addVocable:
	            adapter.add(new Vocable(-1, -1, "", ""));
	            setSelection(adapter.getCount() - 1);
	            return true;
	        case R.id.deleteDictionary:
	            deleteDictionary();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void deleteDictionary() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDestroy() {
		
		//saveState();
		
		super.onDestroy();
	}

	private void saveState() {
		/*State state = TrainingApplication.getState();
		
		EditText editTextDictionaryName = (EditText) findViewById(R.id.editTextDictionaryName);
		state.getDictionary().setName(editTextDictionaryName.getText().toString());
		EditText editTextLanguage1 = (EditText) findViewById(R.id.editTextLanguage1);
		state.getDictionary().setLanguage1(editTextLanguage1.getText().toString());
		EditText editTextLanguage2 = (EditText) findViewById(R.id.editTextLanguage2);
		state.getDictionary().setLanguage2(editTextLanguage2.getText().toString());
		
		ListView l = getListView();
		List<Vocable> backup = new ArrayList<Vocable>(l.getChildCount());
		for (int i = 0; i < l.getChildCount(); i++) {
			View c = l.getChildAt(i);
			EditText listItemEditWord = (EditText) c.findViewById(R.id.listItemEditWord);
			EditText listItemEditTranslation = (EditText) c.findViewById(R.id.listItemEditTranslation);
			String word = listItemEditWord.getText().toString();
			String translation = listItemEditTranslation.getText().toString();
			Vocable v = (Vocable) l.getItemAtPosition(i);
			v.setWord(word);
			v.setTranslation(translation);
			backup.add(v);
		}
		
		List<Vocable> vocables = state.getVocables();
		vocables.clear();
		for (int i = 0; i < backup.size() - 1; i++) {
			vocables.add(backup.get(i));
		}*/
	}

	private class VocableArrayAdapter extends ArrayAdapter<Vocable> {
		
		public VocableArrayAdapter(Context context, int resource,
				List<Vocable> objects) {
			super(context, resource, objects);
		}
		
		private class ViewHolder {
			public EditText listItemEditWord;
			public EditText listItemEditTranslation;
			public View buttonDeleteVocable;
			public Vocable vocable;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final Vocable vocable = getItem(position);

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.vocable_list_item, parent, false);
				final ViewHolder vh = new ViewHolder();
				vh.listItemEditWord = (EditText) convertView.findViewById(R.id.listItemEditWord);
				vh.listItemEditTranslation = (EditText) convertView.findViewById(R.id.listItemEditTranslation);
				vh.buttonDeleteVocable = convertView.findViewById(R.id.buttonDeleteVocable);
				
				vh.listItemEditWord.addTextChangedListener(new TextWatcher() {
					@Override
					public void afterTextChanged(Editable arg0) {
						String word = vh.listItemEditWord.getText().toString();
						vh.vocable.setWord(word);
					}
					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					}
					@Override
					public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					}
				});
				
				vh.listItemEditTranslation.addTextChangedListener(new TextWatcher() {
					@Override
					public void afterTextChanged(Editable arg0) {
						String translation = vh.listItemEditTranslation.getText().toString();
						vh.vocable.setTranslation(translation);
					}
					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					}
					@Override
					public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					}
				});
				
				vh.buttonDeleteVocable.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						VocableArrayAdapter.this.remove(vh.vocable);
					}
				});
				convertView.setTag(vh);
			}
			
			ViewHolder vh = (ViewHolder) convertView.getTag();
			vh.vocable = vocable;
			vh.listItemEditWord.setText(vocable.getWord());
			vh.listItemEditTranslation.setText(vocable.getTranslation());

			return convertView;
		}

	}
}
