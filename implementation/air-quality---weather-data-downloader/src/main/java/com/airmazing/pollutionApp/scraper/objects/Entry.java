package com.airmazing.pollutionApp.scraper.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by andrei on 27/10/15.
 */

public class Entry {
    private HashMap<String, String> attributes = new HashMap<String, String>();

    public Entry() {
    }

    public String getAttribute(String key) {
        if (this.attributes.containsKey(key)) {
            return this.attributes.get(key);
        }
        else {
            return null;
        }
    }

    public void setAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public String toString() {
        String text = "";
        for (String key : this.attributes.keySet()) {
            text += "\t\t" + key + " - " + this.attributes.get(key) + "\n";
        }
        return text;
    }

    public List<String> getAttributes() {
        List<String> attributes = new ArrayList<String>();
        for (String key : this.attributes.keySet()) {
            attributes.add(key);
        }
        return attributes;
    }

    public List<String> getValues() {
        List<String> values = new ArrayList<String>();
        for (String value : this.attributes.values()) {
            values.add(value);
        }
        return values;
    }

    public List<String> getValues(String[] keys) {
        List<String> values = new ArrayList<String>();
        for (String key : keys) {
            if (this.attributes.containsKey(key)) {
                values.add(this.attributes.get(key));
            } else {
                values.add(null);
            }
        }
        return values;
    }


}