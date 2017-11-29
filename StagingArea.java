package gitlet;


import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by rnatarajan1 on 7/14/2017.
 */
public class StagingArea implements Serializable {
    //Key:filename, Value:shaID
    HashMap<String, String> stage;
    ArrayList<String> removed;

    public StagingArea() {
        stage = new HashMap<>();
        removed = new ArrayList<>();
    }

    public void add(String filename, String sha1) throws NullPointerException {
        if (stage.containsValue(sha1)) {
            throw new NullPointerException();
        } else {
            stage.put(filename, sha1);
        }
    }

    public void clear() {
        stage.clear();
    }

    public void remove(String filename) throws NullPointerException {
        stage.remove(filename);
    }

    public int size() {
        return stage.size();
    }

    public HashMap<String, String> committing() {
        HashMap<String, String> copy;
        copy = stage;
        stage = new HashMap<String, String>();
        return copy;
    }

}
