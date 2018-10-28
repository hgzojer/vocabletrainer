package at.hgz.vocabletrainer.csv;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import at.hgz.vocabletrainer.db.Dictionary;
import at.hgz.vocabletrainer.db.Vocable;
import at.hgz.vocabletrainer.xml.XmlDictionary;
import at.hgz.vocabletrainer.xml.XmlUtil.Entity;
import at.hgz.vocabletrainer.xml.XmlVocable;

public class CsvUtil {

    private static CsvUtil instance;

    public static CsvUtil getInstance() {
        if (instance == null) {
            instance = new CsvUtil();
        }
        return instance;
    }

    public byte[] marshall(Dictionary dictionary, List<Vocable> vocables) {
        StringBuilder csvBuilder = new StringBuilder();
        for (Vocable vocable : vocables) {
            csvBuilder.append(vocable.getWord());
            csvBuilder.append("\t");
            csvBuilder.append(vocable.getTranslation());
            csvBuilder.append("\r\n");
        }
        return csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public Entity unmarshall(byte[] dictionaryBytes) {
        String csvString = new String(dictionaryBytes, StandardCharsets.UTF_8);
        try (BufferedReader in = new BufferedReader(new StringReader(csvString))) {
            Dictionary dictionary = null;
            List<Vocable> vocables = new ArrayList<>();
            String line = in.readLine();
            while (line != null) {
                String[] arr = line.split("\t", 2);
                String word;
                if (arr.length >= 1) {
                    word = arr[0];
                } else {
                    word = null;
                }
                String translation;
                if (arr.length >= 2) {
                    translation = arr[1];
                } else {
                    translation = null;
                }
                if (dictionary == null) {
                    String name = word + "/" + translation;
                    String language1 = word;
                    String language2 = translation;
                    dictionary = new Dictionary(-1, name, language1, language2);
                }
                vocables.add(new Vocable(-1, -1, word, translation));
                line = in.readLine();
            }
            return new Entity(dictionary, vocables);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
