package at.hgz.vocabletrainer.set;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import at.hgz.vocabletrainer.db.Vocable;

public class TrainingElem {

	private String word;
	private String translation;
	private String language1;
	private String language2;
	
	private List<Vocable> vocables;
	private boolean flipVocables;
	
	/**
	 * Constructor.
	 * @param word word
	 * @param translation translation
	 * @param language1 language 1
	 * @param language2 language 2
	 * @param vocables vocables
	 * @param flipVocables false if DIRECTION_FORWARD, true if DIRECTION_BACKWARD
	 */
	public TrainingElem(String word, String translation, String language1,
			String language2, List<Vocable> vocables, boolean flipVocables) {
		this.word = word;
		this.translation = translation;
		this.language1 = language1;
		this.language2 = language2;
		this.vocables = vocables;
		this.flipVocables = flipVocables;
	}

	public String getWord() {
		return word;
	}
	
	public String getTranslation() {
		return translation;
	}
	
	public String[] getAlternatives() {
		List<Vocable> pool = new LinkedList<>(vocables);
		if (flipVocables) {
			pool.removeIf(vocable -> vocable.getWord() == translation && vocable.getTranslation() == word);
			if (pool.size() < 2) {
				pool.add(new Vocable(-1, -1, translation + "a", word + "a"));
				pool.add(new Vocable(-1, -1, translation + "o", word + "o"));
			}
			Vocable alt1 = pool.remove((int) (Math.random() * pool.size()));
			Vocable alt2 = pool.remove((int) (Math.random() * pool.size()));
			return new String[] { alt1.getWord(), alt2.getWord() };
		} else {
			pool.removeIf(vocable -> vocable.getWord() == word && vocable.getTranslation() == translation);
			if (pool.size() < 2) {
				pool.add(new Vocable(-1, -1, word + "a", translation + "a"));
				pool.add(new Vocable(-1, -1, word + "o", translation + "o"));
			}
			Vocable alt1 = pool.remove((int) (Math.random() * pool.size()));
			Vocable alt2 = pool.remove((int) (Math.random() * pool.size()));
			return new String[] { alt1.getTranslation(), alt2.getTranslation() };
		}
	}

	public String getLanguage1() {
		return language1;
	}

	public String getLanguage2() {
		return language2;
	}
	
}
