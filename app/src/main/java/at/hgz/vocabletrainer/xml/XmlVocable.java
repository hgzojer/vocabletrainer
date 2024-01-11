package at.hgz.vocabletrainer.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="vocable")
public class XmlVocable {

	@Element
	private String word;
	
	@Element
	private String translation;

	public XmlVocable() {
	}

	public XmlVocable(String word, String translation) {
		this.word = word;
		this.translation = translation;
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
