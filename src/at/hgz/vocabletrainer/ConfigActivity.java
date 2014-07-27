package at.hgz.vocabletrainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import at.hgz.vocabletrainer.db.VocableOpenHelper;
import at.hgz.vocabletrainer.set.TrainingSet;

public class ConfigActivity extends Activity {
	
    public static final String TRANSLATION_DIRECTION = "translationDirection";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        
        int direction = TrainingApplication.getState().getDirection();
        
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
    }
    
	public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        
        // Check which radio button was clicked
		if (checked) {
			switch (view.getId()) {
			case R.id.radioDirection1:
				TrainingApplication.getState().setDirection(TrainingSet.DIRECTION_FORWARD);
				TrainingApplication.getState().setConfigChanged(true);
				break;
			case R.id.radioDirection2:
				TrainingApplication.getState().setDirection(TrainingSet.DIRECTION_BIDIRECTIONAL);
				TrainingApplication.getState().setConfigChanged(true);
				break;
			case R.id.radioDirection3:
				TrainingApplication.getState().setDirection(TrainingSet.DIRECTION_BACKWARD);
				TrainingApplication.getState().setConfigChanged(true);
				break;
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
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
				Resources resources = getApplicationContext().getResources();
				String text = resources.getString(R.string.resettingDatabase);
				Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
				toast.show();
		    	VocableOpenHelper helper = VocableOpenHelper.getInstance(ConfigActivity.this);
		    	helper.resetDatabase();
		    }})
		.setNegativeButton(android.R.string.no, null).show();
    }
    
}
