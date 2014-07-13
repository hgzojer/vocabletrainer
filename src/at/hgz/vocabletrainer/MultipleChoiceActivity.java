package at.hgz.vocabletrainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import at.hgz.vocabletrainer.set.TrainingElem;

public class MultipleChoiceActivity extends AbstractTrainingActivity {
	
	private Drawable defaultDrawableRight;
	
	private Drawable defaultDrawableRightCount;
	
	private Drawable defaultDrawableWrong;
	
	private Drawable defaultDrawableWrongCount;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_choice);

		TextView textViewRight = (TextView) findViewById(R.id.textViewRight);
		defaultDrawableRight = textViewRight.getBackground();
		TextView textViewRightCount = (TextView) findViewById(R.id.textViewRightCount);
		defaultDrawableRightCount = textViewRightCount.getBackground();
		TextView textViewWrong = (TextView) findViewById(R.id.textViewWrong);
		defaultDrawableWrong = textViewWrong.getBackground();
		TextView textViewWrongCount = (TextView) findViewById(R.id.textViewWrongCount);
		defaultDrawableWrongCount = textViewWrongCount.getBackground();
		
    	onCreate2();
    }
    
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.buttonChoice1:
		case R.id.buttonChoice2:
		case R.id.buttonChoice3:
			String translation = ((Button) view).getText().toString();
	    	evaluate(translation);
		}
    }


	@Override
	protected void updateDisplay() {
		TrainingElem vocable = state.getVocable();
		String[] alternatives = vocable.getAlternatives();
		List<String> choices = new ArrayList<String>(3);
		choices.add(vocable.getTranslation());
		choices.add(alternatives[0]);
		choices.add(alternatives[1]);
		Collections.shuffle(choices);
		
		TextView textViewLanguageDirection12 = (TextView) findViewById(R.id.textViewLanguageDirection12);
		textViewLanguageDirection12.setText(String.format("%s -> %s", vocable.getLanguage1(), vocable.getLanguage2()));
		
		TextView outputWord = (TextView) findViewById(R.id.textViewWordLanguage1);
		outputWord.setText(vocable.getWord());

		Button buttonChoice1 = (Button) findViewById(R.id.buttonChoice1);
		buttonChoice1.setText(choices.get(0));
		
		Button buttonChoice2 = (Button) findViewById(R.id.buttonChoice2);
		buttonChoice2.setText(choices.get(1));
		
		Button buttonChoice3 = (Button) findViewById(R.id.buttonChoice3);
		buttonChoice3.setText(choices.get(2));
		
		updateDisplayStatistic();
	}

	@Override
	protected void showRightToast() {
		TextView textViewStatistic = (TextView) findViewById(R.id.textViewStatistic);
		textViewStatistic.setBackgroundColor(0x8000FF00);
		TextView textViewRight = (TextView) findViewById(R.id.textViewRight);
		textViewRight.setBackgroundColor(0x8000FF00);
		TextView textViewRightCount = (TextView) findViewById(R.id.textViewRightCount);
		textViewRightCount.setBackgroundColor(0x8000FF00);
		TextView textViewWrong = (TextView) findViewById(R.id.textViewWrong);
		textViewWrong.setBackgroundDrawable(defaultDrawableWrong);
		TextView textViewWrongCount = (TextView) findViewById(R.id.textViewWrongCount);
		textViewWrongCount.setBackgroundDrawable(defaultDrawableWrongCount);
	}

	@Override
	protected void showWrongToast() {
		TextView textViewStatistic = (TextView) findViewById(R.id.textViewStatistic);
		textViewStatistic.setBackgroundColor(0x80FF0000);
		TextView textViewRight = (TextView) findViewById(R.id.textViewRight);
		textViewRight.setBackgroundDrawable(defaultDrawableRight);
		TextView textViewRightCount = (TextView) findViewById(R.id.textViewRightCount);
		textViewRightCount.setBackgroundDrawable(defaultDrawableRightCount);
		TextView textViewWrong = (TextView) findViewById(R.id.textViewWrong);
		textViewWrong.setBackgroundColor(0x80FF0000);
		TextView textViewWrongCount = (TextView) findViewById(R.id.textViewWrongCount);
		textViewWrongCount.setBackgroundColor(0x80FF0000);
	}

	@Override
	protected void showFinishedToast() {
		TextView textViewStatistic = (TextView) findViewById(R.id.textViewStatistic);
		textViewStatistic.setBackgroundColor(0x800000FF);
		TextView textViewRight = (TextView) findViewById(R.id.textViewRight);
		textViewRight.setBackgroundDrawable(defaultDrawableRight);
		TextView textViewRightCount = (TextView) findViewById(R.id.textViewRightCount);
		textViewRightCount.setBackgroundDrawable(defaultDrawableRightCount);
		TextView textViewWrong = (TextView) findViewById(R.id.textViewWrong);
		textViewWrong.setBackgroundDrawable(defaultDrawableWrong);
		TextView textViewWrongCount = (TextView) findViewById(R.id.textViewWrongCount);
		textViewWrongCount.setBackgroundDrawable(defaultDrawableWrongCount);
		
		super.showFinishedToast();
	}

}
