package at.hgz.vocabletrainer.db;

public class Vocable {

	private int id;
	private int dictionaryId;
	private String word;
	private String translation;
	
	public Vocable(int id, int dictionaryId, String word, String translation) {
		this.id = id;
		this.dictionaryId = dictionaryId;
		this.word = word;
		this.translation = translation;
	}

	public int getId() {
		return id;
	}

	public int getDictionaryId() {
		return dictionaryId;
	}

	public void setDictionaryId(int trainingSetId) {
		this.dictionaryId = trainingSetId;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getTranslation() {
		return translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}
}
