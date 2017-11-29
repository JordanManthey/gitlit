package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Jordan on 7/14/2017.
 */

public class Commit implements Serializable {

    private String timestamp;
    String pattern = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat format = new SimpleDateFormat(pattern);
    private String logMessage;
    private String shaID;
    private HashMap<String, String> addedFiles; //filename, sha1
    private String prev;
    private boolean isSplitPoint;

    public String getPrev() {
        return prev;
    }

    public void setSplitPoint(boolean tf) {
        if (tf) {
            this.isSplitPoint = true;
        } else {
            this.isSplitPoint = false;
        }
    }

    public Commit(HashMap files, String msg, String prevId) {
        this.isSplitPoint = false;
        this.addedFiles = files;
        this.logMessage = msg;
        this.shaID = Utils.sha1(this.logMessage + this.addedFiles.values());
        this.timestamp = format.format(new Date());
        this.prev = prevId;

    }

    public Commit() {
        this.isSplitPoint = false;
        this.addedFiles = null;
        this.logMessage = "initial commit";
        this.shaID = Utils.sha1(this.logMessage);
        this.timestamp = format.format(new Date());
        this.prev = null;
    }

    @Override
    public int hashCode() {
        int result = shaID.hashCode();
        result = 31 * result + addedFiles.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        return (this.addedFiles.equals(((Commit) other).addedFiles)
                && this.logMessage.equals(((Commit) other).logMessage)
                && this.shaID.equals(((Commit) other).shaID)
                && this.prev.equals(((Commit) other).prev));
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getLog() {
        return this.logMessage;
    }

    public String getID() {
        return this.shaID;
    }

    public HashMap getFiles() {
        return this.addedFiles;
    }



}
