package at.hgz.vocabletrainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class LicenseActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_license);

		Intent intent = getIntent();
		String moduleName = intent.getStringExtra("moduleName");
		String licenseText = intent.getStringExtra("licenseText");
		
		TextView textViewName = (TextView) findViewById(R.id.textViewName);
		TextView textViewDescription = (TextView) findViewById(R.id.textViewDescription);
		
		textViewName.setText(moduleName);
		textViewDescription.setText(licenseText);

		ActionBar actionBar = this.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

}
