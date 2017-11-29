package gitlet;

public class Merge {

    Pointers currentBranch;
    Commit lastCommit;
    Commit givenCommit;

    public Merge(Commit lCommit, Commit gCommit, Pointers cBranch) {
        this.lastCommit = lCommit;
        this.givenCommit = gCommit;
        this.currentBranch = cBranch;
    }

    public Commit findSplit(Commit lCommit) throws NullPointerException  {
        if (lCommit.getSplitPoint() == true) {
            return lCommit;
        } else {
            String code = lCommit.getID();
            Commit temp = null;
            Serializer cereal = new Serializer();
            Commit cPrev = (Commit) cereal.generate(temp, code);
            //fix ^
            if (cPrev == null) {
                throw new NullPointerException("No splitpoint found.");
            }
            if (cPrev.getSplitPoint() == true) {
                return cPrev;
            } else {
                return findSplit(cPrev);
            }
        }
    }

    public void mergeMethod() {
        Commit splitPoint = findSplit(this.lastCommit);
        Commit temp = null;
        Serializer cereal = new Serializer();
        //Commit under Current Branch pointer.
        Commit currCommit = (Commit) cereal.generate(temp, this.currentBranch.getReference());
        //fix ^
        if (splitPoint.equals(this.givenCommit)) {
            System.out.println("Given branch is an ancestor of the current branch");
            return;
        } else if (currCommit.equals(splitPoint)) {
            this.currentBranch.setReference(this.givenCommit.getID());
        }
    }





}
