package at.hgz.vocabletrainer;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import at.hgz.vocabletrainer.db.Vocable;

public class VocableListActivity extends ListActivity {

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

		List<Vocable> list = new ArrayList<Vocable>(state.getVocables());
		list.add(new Vocable(-1, -1, "", ""));

		final VocableArrayAdapter adapter = new VocableArrayAdapter(this, R.layout.vocable_list_item, list);
		setListAdapter(adapter);
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final Vocable vocable = getItem(position);

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.vocable_list_item, parent, false);
			}

			EditText listItemEditWord = (EditText) convertView.findViewById(R.id.listItemEditWord);
			EditText listItemEditTranslation = (EditText) convertView.findViewById(R.id.listItemEditTranslation);
			final View buttonDeleteVocable = convertView.findViewById(R.id.buttonDeleteVocable);

			listItemEditWord.setText(vocable.getWord());
			listItemEditWord.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					//vocable.setWord(s.toString());
					Log.e(TAG, "s == '" + s.toString() + "' word=" + vocable.getWord() + " translation=" + vocable.getTranslation());
					if (isLast(vocable) && !s.toString().equals("")) {
						buttonDeleteVocable.setVisibility(View.VISIBLE);
						add(new Vocable(-1, -1, "", ""));
					}
				}
			});
			
			listItemEditTranslation.setText(vocable.getTranslation());
			listItemEditTranslation.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					//vocable.setTranslation(s.toString());
					Log.e(TAG, "s == '" + s.toString() + "' word=" + vocable.getWord() + " translation=" + vocable.getTranslation());
					if (isLast(vocable) && !s.toString().equals("")) {
						buttonDeleteVocable.setVisibility(View.VISIBLE);
						add(new Vocable(-1, -1, "", ""));
					}
				}
			});
			
			buttonDeleteVocable.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					VocableArrayAdapter.this.remove(vocable);
				}
			});
			if (isLast(vocable)) {
				// last view
				buttonDeleteVocable.setVisibility(View.INVISIBLE);
			} else {
				buttonDeleteVocable.setVisibility(View.VISIBLE);
			}

			return convertView;
		}
		
		private static final String TAG = "VocableListActivity";
		
		private boolean isLast(Vocable vocable) {
			Log.e(TAG, "pos=" + getPosition(vocable) + " count=" + getCount());
			return getPosition(vocable) == getCount() - 1;
		}

	}
}
