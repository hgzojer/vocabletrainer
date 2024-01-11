package at.hgz.vocabletrainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class ViewActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = this.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		Intent intent = getIntent();
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			if (DictionaryListActivity.importDictionaryFromExternalStorage(this, intent.getData(), intent.getType())) {
			}
		}
		Intent intent1 = new Intent(ViewActivity.this, DictionaryListActivity.class);
		intent1.putExtra("import", true);
		ViewActivity.this.startActivity(intent1);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
            /*
            Intent myIntent = new Intent(getApplicationContext(), DictionaryListActivity.class);
            startActivityForResult(myIntent, 0);
            return true;
             */
			Intent returnIntent = new Intent();
			returnIntent.putExtra("result", "back");
			setResult(RESULT_CANCELED, returnIntent);
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

}
