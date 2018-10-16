package at.hgz.vocabletrainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		Intent intent = getIntent();
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			if (DictionaryListActivity.importDictionaryFromExternalStorage(this, intent.getData())) {
			}
		}
		Intent intent1 = new Intent(ViewActivity.this, DictionaryListActivity.class);
		intent1.putExtra("import", true);
		ViewActivity.this.startActivity(intent1);
	}

}
