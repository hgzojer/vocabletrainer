package at.hgz.vocabletrainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import at.hgz.vocabletrainer.db.VocableOpenHelper;
import at.hgz.vocabletrainer.set.TrainingSet;

public class ConfigActivity extends Activity {
	
    private State state;
	public static final String TRANSLATION_DIRECTION = "translationDirection";
	public static final String FILE_FORMAT = "fileFormat";
	public static final int FILE_FORMAT_XML = 0;
	public static final int FILE_FORMAT_JSON = 1;
	public static final int FILE_FORMAT_CSV = 2;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        
		Intent intent = getIntent();
		state = TrainingApplication.getState(intent.getIntExtra(State.STATE_ID, -1));
				 
        int direction = state.getDirection();
        
        RadioGroup radioGroupDirection = (RadioGroup) this.findViewById(R.id.radioGroupDirection);
		switch (direction) {
		case TrainingSet.DIRECTION_FORWARD:
			radioGroupDirection.check(R.id.radioDirection1);
			break;
		case TrainingSet.DIRECTION_BIDIRECTIONAL:
			radioGroupDirection.check(R.id.radioDirection2);
			break;
		case TrainingSet.DIRECTION_BACKWARD:
			radioGroupDirection.check(R.id.radioDirection3);
			break;
		}

		int fileFormat = state.getFileFormat();

		RadioGroup radioGroupFileFormat = (RadioGroup) this.findViewById(R.id.radioGroupFileFormat);
		switch (fileFormat) {
			case FILE_FORMAT_XML:
				radioGroupFileFormat.check(R.id.radioFileFormat1);
				break;
			case FILE_FORMAT_JSON:
				radioGroupFileFormat.check(R.id.radioFileFormat2);
				break;
			case FILE_FORMAT_CSV:
				radioGroupFileFormat.check(R.id.radioFileFormat3);
				break;
		}
    }
    
	public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        
        // Check which radio button was clicked
		if (checked) {
			int id = view.getId();
			if (id == R.id.radioDirection1) {
				state.setDirection(TrainingSet.DIRECTION_FORWARD);
				state.setConfigChanged(true);
			} else if (id == R.id.radioDirection2) {
				state.setDirection(TrainingSet.DIRECTION_BIDIRECTIONAL);
				state.setConfigChanged(true);
			} else if (id == R.id.radioDirection3) {
				state.setDirection(TrainingSet.DIRECTION_BACKWARD);
				state.setConfigChanged(true);
			} else if (id == R.id.radioFileFormat1) {
				state.setFileFormat(FILE_FORMAT_XML);
				state.setConfigChanged(true);
			} else if (id == R.id.radioFileFormat2) {
				state.setFileFormat(FILE_FORMAT_JSON);
				state.setConfigChanged(true);
			} else if (id == R.id.radioFileFormat3) {
				state.setFileFormat(FILE_FORMAT_CSV);
				state.setConfigChanged(true);
			}
		}
	}
    
	public void onClickResetDatabase(View view) {

		Resources resources = getApplicationContext().getResources();
		String confirmDeleteDictionaryTitle = resources.getString(R.string.confirmResetDatabaseTitle);
		String confirmDeleteDictionaryText = resources.getString(R.string.confirmResetDatabaseText);
		
		new AlertDialog.Builder(this)
		.setTitle(confirmDeleteDictionaryTitle)
		.setMessage(confirmDeleteDictionaryText)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
			Resources resources1 = getApplicationContext().getResources();
			String text = resources1.getString(R.string.resettingDatabase);
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
			VocableOpenHelper helper = VocableOpenHelper.getInstance(ConfigActivity.this);
			helper.resetDatabase();
		})
		.setNegativeButton(android.R.string.cancel, null).show();
    }
    
}
