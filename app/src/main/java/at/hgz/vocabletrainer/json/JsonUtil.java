package at.hgz.vocabletrainer.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import at.hgz.vocabletrainer.db.Dictionary;
import at.hgz.vocabletrainer.db.Vocable;
import at.hgz.vocabletrainer.xml.XmlDictionary;
import at.hgz.vocabletrainer.xml.XmlUtil.Entity;
import at.hgz.vocabletrainer.xml.XmlVocable;

public class JsonUtil {

    private static JsonUtil instance;

    public static JsonUtil getInstance() {
        if (instance == null) {
            instance = new JsonUtil();
        }
        return instance;
    }

    public byte[] marshall(Dictionary dictionary, List<Vocable> vocables) {
        Gson gson = new Gson();
        XmlDictionary xmlDictionary = new XmlDictionary();
        xmlDictionary.setName(dictionary.getName());
        xmlDictionary.setLanguage1(dictionary.getLanguage1());
        xmlDictionary.setLanguage2(dictionary.getLanguage2());
        ArrayList<XmlVocable> xmlVocables = new ArrayList<>();
        xmlDictionary.setVocables(xmlVocables);
        for (Vocable vocable : vocables) {
            xmlVocables.add(new XmlVocable(vocable.getWord(), vocable.getTranslation()));
        }
        String jsonString = gson.toJson(xmlDictionary);
        return jsonString.getBytes(StandardCharsets.UTF_8);
    }

    public Entity unmarshall(byte[] dictionaryBytes) {
        try {
            String json = new String(dictionaryBytes, StandardCharsets.UTF_8);
            JsonObject jsonDictionary = JsonParser.parseString(json).getAsJsonObject();

            String name, language1, language2;
            if (jsonDictionary != null) {
                name = jsonDictionary.get("name").getAsString();
                language1 = jsonDictionary.get("language1").getAsString();
                language2 = jsonDictionary.get("language2").getAsString();
            } else {
                name = null;
                language1 = null;
                language2 = null;
            }
            Dictionary dictionary = new Dictionary(-1, name, language1, language2);
            List<Vocable> vocables = new ArrayList<>();
            if (jsonDictionary != null) {
                JsonArray jsonVocables = jsonDictionary.getAsJsonArray("vocables");
                for (JsonElement vocablesElem : jsonVocables) {
                    JsonObject jsonVocable = vocablesElem.getAsJsonObject();
                    if (jsonVocable != null) {
                        String word = jsonVocable.get("word").getAsString();
                        String translation = jsonVocable.get("translation").getAsString();
                        vocables.add(new Vocable(-1, -1, word, translation));
                    }
                }
            }
            return new Entity(dictionary, vocables);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
