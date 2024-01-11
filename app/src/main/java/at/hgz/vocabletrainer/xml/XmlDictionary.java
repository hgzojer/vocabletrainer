package at.hgz.vocabletrainer.xml;

import java.util.ArrayList;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="dictionary")
public class XmlDictionary {

	@Element
	private String name;
	
	@Element
	private String language1;
	
	@Element
	private String language2;
	
	@ElementList
	private ArrayList<XmlVocable> vocables;

	public XmlDictionary() {
	}

	public XmlDictionary(String name, String language1, String language2,
			ArrayList<XmlVocable> vocables) {
		this.name = name;
		this.language1 = language1;
		this.language2 = language2;
		this.vocables = vocables;
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

	public ArrayList<XmlVocable> getVocables() {
		return vocables;
	}

	public void setVocables(ArrayList<XmlVocable> vocables) {
		this.vocables = vocables;
	}
	
}
