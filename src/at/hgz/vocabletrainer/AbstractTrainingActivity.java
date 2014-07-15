package at.hgz.vocabletrainer;

import android.app.Activity;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import at.hgz.vocabletrainer.set.TrainingSet;

public abstract class AbstractTrainingActivity extends Activity {
	
	protected State state;
	
	protected AbstractTrainingActivity() {
	}

	protected void onCreate2() {
		state = TrainingApplication.getState();
        //Intent intent = getIntent();
        //state.setDictionaryId(intent.getIntExtra("dictionaryId", state.getDictionaryId()));
    	if (state.isNeedInit()) {
            loadVokabel();
            state.setNeedInit(false);
    	} else {
        	updateDisplay();
    	}
	}

	protected void evaluate(String translation) {
		if (translation != null && !translation.trim().equals("")) {
    		if (translation.equalsIgnoreCase(state.getVocable().getTranslation())) {
    			state.incRight();
    			state.decTodo();
    			if (state.getTodo() > 0) {
    				showRightToast();
    			} else {
    				showFinishedToast();
    			}
    		} else {
            	state.incWrong();
            	state.getList().add(state.getVocable());
            	showWrongToast();
    		}
    		loadVokabel();
    	}
	}

	protected void showRightToast() {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_right_layout,
		                               (ViewGroup) findViewById(R.id.toast_right_layout_root));

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();		
	}

	protected void showWrongToast() {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_wrong_layout,
		                               (ViewGroup) findViewById(R.id.toast_wrong_layout_root));
		
		TextView text = (TextView) layout.findViewById(R.id.textWrongToastDetails);
		Resources resources = getApplicationContext().getResources();
		text.setText(resources.getString(R.string.wrongToastDetails, state.getVocable().getTranslation()));

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();		
	}
    
	protected void showFinishedToast() {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_finished_layout,
		                               (ViewGroup) findViewById(R.id.toast_finished_layout_root));

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();		
	}

    public void loadVokabel() {
    	if (state.getList() == null || state.getList().isEmpty()) {
    		init();
    	}
		state.setVocable(state.getList().remove(0));
    	updateDisplay();
    }

	private void init() {
        state.setList(new TrainingSet(state.getDictionary(), state.getVocables(), state.getDirection()).getList());
		state.setRight(0);
		state.setWrong(0);
		state.setTodo(state.getList().size());
    	ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
    	progressBar.setMax(state.getTodo());
    	progressBar.setProgress(state.getRight());
	}

	protected abstract void updateDisplay();

	protected void updateDisplayStatistic() {
		// Statistik:
		TextView outputRight = (TextView) findViewById(R.id.textViewRightCount);
		outputRight.setText("" + state.getRight());
		TextView outputWrong = (TextView) findViewById(R.id.textViewWrongCount);
		outputWrong.setText("" + state.getWrong());
		TextView outputTodo = (TextView) findViewById(R.id.textViewTodoCount);
		outputTodo.setText("" + state.getTodo());
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setProgress(state.getRight());
	}
}
