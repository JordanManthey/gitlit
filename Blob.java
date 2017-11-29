package gitlet;

import java.io.File;
import java.io.Serializable;

/**
 * Created by rnatarajan1 on 7/14/2017.
 */
public class Blob implements Serializable {
    private String ID;
    private byte[] contents;

    //Contents are converted contents of file, ID is the sha1 of contents.
    public Blob() {
        ID = null;
        contents = null;
    }
    public Blob(File filepath) {
        this.contents = Utils.readContents(filepath);
        this.ID = Utils.sha1(this.contents);
    }

    public String getID() {
        return ID;
    }

    public byte[] getContents() {
        return contents;
    }
}
