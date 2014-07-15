package at.hgz.vocabletrainer.set;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import at.hgz.vocabletrainer.db.Dictionary;
import at.hgz.vocabletrainer.db.Vocable;

public class TrainingSet {
	
	public static final int DIRECTION_FORWARD = 1;
	public static final int DIRECTION_BACKWARD = 2;
	public static final int DIRECTION_BIDIRECTIONAL = 3;

	private List<TrainingElem> list;
	private Dictionary dictionary;

	public TrainingSet(Dictionary dictionary, List<Vocable> vocables, int direction) {
		this.dictionary = dictionary;
		list = new LinkedList<TrainingElem>();
		if (direction == DIRECTION_FORWARD || direction == DIRECTION_BIDIRECTIONAL) {
			createList1to2(vocables);
		}
		if (direction == DIRECTION_BACKWARD || direction == DIRECTION_BIDIRECTIONAL) {
			createList2to1(vocables);
		}
		Collections.shuffle(list);
	}

	/**
	 * List for language 1 to language 2
	 * 
	 * @param vocables
	 */
	private void createList1to2(List<Vocable> vocables) {
		for (Vocable vocable : vocables) {
			list.add(new TrainingElem(vocable.getWord(), vocable
					.getTranslation(), dictionary.getLanguage1(), dictionary
					.getLanguage2(), vocables, false));
		}
	}

	/**
	 * List for language 2 to language 1
	 * 
	 * @param vocables
	 */
	private void createList2to1(List<Vocable> vocables) {
		for (Vocable vocable : vocables) {
			list.add(new TrainingElem(vocable.getTranslation(), vocable
					.getWord(), dictionary.getLanguage2(), dictionary
					.getLanguage1(), vocables, true));
		}
	}

	public List<TrainingElem> getList() {
		return list;
	}

}
