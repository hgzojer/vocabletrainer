package at.hgz.vocabletrainer;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.Toast;
import at.hgz.vocabletrainer.db.Dictionary;
import at.hgz.vocabletrainer.db.Vocable;
import at.hgz.vocabletrainer.db.VocableOpenHelper;

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

		Resources resources = getApplicationContext().getResources();
		String confirmDeleteDictionaryTitle = resources.getString(R.string.confirmDeleteDictionaryTitle);
		String confirmDeleteDictionaryText = resources.getString(R.string.confirmDeleteDictionaryText);
		
		new AlertDialog.Builder(this)
		.setTitle(confirmDeleteDictionaryTitle)
		.setMessage(confirmDeleteDictionaryText)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
				deleteState();
				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", "delete");
				setResult(RESULT_OK,returnIntent);
				finish();
		    }})
		.setNegativeButton(android.R.string.no, null).show();
	}

	@Override
	public void onBackPressed() {
		saveState();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("result", "save");
		setResult(RESULT_OK,returnIntent);
		//finish();
		super.onBackPressed();
	}

	private void saveState() {
		State state = TrainingApplication.getState();
		Dictionary dictionary = state.getDictionary();
		
		EditText editTextDictionaryName = (EditText) findViewById(R.id.editTextDictionaryName);
		dictionary.setName(editTextDictionaryName.getText().toString());
		EditText editTextLanguage1 = (EditText) findViewById(R.id.editTextLanguage1);
		dictionary.setLanguage1(editTextLanguage1.getText().toString());
		EditText editTextLanguage2 = (EditText) findViewById(R.id.editTextLanguage2);
		dictionary.setLanguage2(editTextLanguage2.getText().toString());
		
		List<Vocable> vocables = state.getVocables();
		/*vocables.clear();
		for (int i = 0; i < adapter.getCount(); i++) {
			vocables.add(adapter.getItem(i));
		}*/
		
		Resources resources = getApplicationContext().getResources();
		String text = resources.getString(R.string.savingDictionary);
		Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
		toast.show();
		
		VocableOpenHelper helper = VocableOpenHelper.getInstance(getApplicationContext());
		helper.persist(dictionary, vocables);
	}

	private void deleteState() {
		State state = TrainingApplication.getState();
		Dictionary dictionary = state.getDictionary();
		List<Vocable> vocables = state.getVocables();
		
		Resources resources = getApplicationContext().getResources();
		String text = resources.getString(R.string.deletingDictionary);
		Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
		toast.show();
		
		VocableOpenHelper helper = VocableOpenHelper.getInstance(getApplicationContext());
		helper.remove(dictionary, vocables);
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
