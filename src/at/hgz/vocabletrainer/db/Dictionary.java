package at.hgz.vocabletrainer.db;

public class Dictionary {

	private int id;
	private String name;
	private String language1;
	private String language2;
	
	public Dictionary(int id, String name, String language1, String language2) {
		this.id = id;
		this.name = name;
		this.language1 = language1;
		this.language2 = language2;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLanguage1() {
		return language1;
	}

	public void setLanguage1(String language1) {
		this.language1 = language1;
	}

	public String getLanguage2() {
		return language2;
	}

	public void setLanguage2(String language2) {
		this.language2 = language2;
	}
	
}
