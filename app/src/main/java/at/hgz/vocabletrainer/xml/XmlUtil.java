package at.hgz.vocabletrainer.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import at.hgz.vocabletrainer.db.Dictionary;
import at.hgz.vocabletrainer.db.Vocable;

public final class XmlUtil {

    private static XmlUtil instance;

    public static XmlUtil getInstance() {
        if (instance == null) {
            instance = new XmlUtil();
        }
        return instance;
    }

    public byte[] marshall(Dictionary dictionary, List<Vocable> vocables) {
        try {
            Serializer serializer = new Persister(new Format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
            XmlDictionary xmlDictionary = new XmlDictionary();
            xmlDictionary.setName(dictionary.getName());
            xmlDictionary.setLanguage1(dictionary.getLanguage1());
            xmlDictionary.setLanguage2(dictionary.getLanguage2());
            ArrayList<XmlVocable> xmlVocables = new ArrayList<>();
            xmlDictionary.setVocables(xmlVocables);
            for (Vocable vocable : vocables) {
                xmlVocables.add(new XmlVocable(vocable.getWord(), vocable.getTranslation()));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            serializer.write(xmlDictionary, out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static class Entity {
        private Dictionary dictionary;
        private List<Vocable> vocables;

        public Entity(Dictionary dictionary, List<Vocable> vocables) {
            this.dictionary = dictionary;
            this.vocables = vocables;
        }

        public Dictionary getDictionary() {
            return dictionary;
        }

        public List<Vocable> getVocables() {
            return vocables;
        }
    }

    public Entity unmarshall(byte[] dictionaryBytes) {
        try {
            Serializer serializer = new Persister();
            ByteArrayInputStream in = new ByteArrayInputStream(dictionaryBytes);
            XmlDictionary xmlDictionary = serializer.read(XmlDictionary.class, in);
            String name, language1, language2;
            if (xmlDictionary != null) {
                name = xmlDictionary.getName();
                language1 = xmlDictionary.getLanguage1();
                language2 = xmlDictionary.getLanguage2();
            } else {
                name = null;
                language1 = null;
                language2 = null;
            }
            Dictionary dictionary = new Dictionary(-1, name, language1, language2);
            List<Vocable> vocables = new ArrayList<>();
            if (xmlDictionary != null && xmlDictionary.getVocables() != null) {
                for (XmlVocable xmlVocable : xmlDictionary.getVocables()) {
                    if (xmlVocable != null) {
                        vocables.add(new Vocable(-1, -1, xmlVocable.getWord(), xmlVocable.getTranslation()));
                    }
                }
            }
            return new Entity(dictionary, vocables);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
