package gitlet;

import java.io.Serializable;

/**
 * Created by rnatarajan1 on 7/16/2017.
 */
public class Pointers implements Serializable {
    private String reference;
    private String currBranch;
    private String name;
    private String type;

    public Pointers() {
    }

    public Pointers(String name, String target) {
        this.name = name;
        reference = target;
        type = "Branch";
    }

    public Pointers(String branch) {
        name = "HEAD";
        currBranch = branch;
        type = "Active";

    }

    public void moveBranch(String newid) {
        reference = newid;
    }

    public void moveHead(String newBranch) {
        currBranch = newBranch;
    }

    public static String getType(Pointers point) {
        return point.type;
    }

    public String getReference() {
        return reference;
    }

    public String getCurrBranch() {
        return currBranch;
    }

    public String getName() {
        return name;
    }

    public void setReference(String newGuy) {
        this.reference = newGuy;
    }

    public void setCurrBranch(String currBranch) {
        this.currBranch = currBranch;
    }
}

