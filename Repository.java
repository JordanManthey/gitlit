package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rnatarajan1 on 7/20/2017.
 */
public class Repository implements Serializable {

    static File workingdir = new File(System.getProperty("user.dir"));
    static Serializer converter = new Serializer();
    static File gitletdir = new File(workingdir, ".gitlet");
    static File indexdir = new File(gitletdir, "index_folder");
    static File blobdir = new File(gitletdir, "blobs");
    static File commitdir = new File(gitletdir, "commits");
    static File pointerdir = new File(gitletdir, "Pointers");
    static File indexblobs = new File(indexdir, "index_blobs");

    public Repository() {
        if (contains(workingdir.list(), ".gitlet")) {
            System.out.println("A gitlet version control system "
                    + "already exists in the current directory.");
            System.exit(0);
        } else {
            //make gitlet directory
            gitletdir.mkdir();

            //make index directory and index
            indexdir.mkdir();
            indexblobs.mkdir();
            StagingArea index = new StagingArea();
            converter.store(index, indexdir.toPath().resolve("index.ser").toString());

            //make blob directory
            blobdir.mkdir();
//            HashMap<String, Blob> blobHolder = new HashMap<>();
//            converter.store(blobHolder, blobdir.toPath().resolve("blob map.ser").toString());

            //make commit directory, initial commit, and hashmap of string ids and commits
            commitdir.mkdir();
            HashMap<String, String> abbrev = new HashMap<>();
            Commit initial = new Commit();
            String sub = (String) initial.getID().subSequence(0, 6);
            abbrev.put(sub, initial.getID());
            File commitloc =
                    new File(commitdir.toPath().resolve(initial.getID() + ".ser").toString());
            converter.store(initial, commitloc.toString());
            File mapLoc = new File(commitdir.toPath().resolve("Commit_map.ser").toString());
            converter.store(abbrev, mapLoc.toString());

            //make pointer directory and branch and head pointers
            pointerdir.mkdir();
            Pointers master = new Pointers("master", initial.getID());
            Pointers head = new Pointers(master.getName());
            converter.store(master, pointerdir.toPath().resolve("master.ser").toString());
            converter.store(head, pointerdir.toPath().resolve("HEAD.ser").toString());
        }
    }

    public static void add(String file) {

        Path directory = Repository.workingdir.toPath();
        Path fileLoc = directory.resolve(file);

        //Checks if file exists in proj2
        if (!Files.exists(fileLoc)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        //converts the path at fileLoc to a File
        File filepath = new File(fileLoc.toString());
        byte[] contents;
        String sha1;
        try {
            //byte array as File content, sha1 generated from byte array
            contents = Utils.readContents(filepath);
            sha1 = Utils.sha1(contents);
        } catch (IllegalArgumentException e) {
            return;
        }
        Object obj = null;
        StagingArea index =
                (StagingArea) converter.generate(obj, indexdir.toPath()
                        .resolve("index.ser").toString());
        boolean dum = false;
        if (index == null) {
            index = new StagingArea();
            dum = true;
        }
        try {
            //adds filename and sha1 to Index hashmap
            if (index.removed.contains(file)) {
                index.removed.remove(file);
            } else {
                index.add(file, sha1);
            }
        } catch (NullPointerException e) {
            return;
        }

        Object obj1 = null;
        Object obj2 = null;
        Pointers head =
                (Pointers) converter.generate(obj1, pointerdir.toPath()
                        .resolve("HEAD.ser").toString());
        Pointers currBranch =
                (Pointers) converter.generate
                        (obj2, pointerdir.toPath()
                                .resolve(head.getCurrBranch() + ".ser").toString());
        String target = currBranch.getReference();

        Object obj3 = null;
        Commit curr =
                (Commit) converter.generate(obj3, commitdir.toPath()
                        .resolve(target + ".ser").toString());
        if (curr.getFiles() != null && curr.getFiles().keySet().contains(file)
                && curr.getFiles().containsValue(sha1)) {
            index.stage.remove(file, sha1);
        }
        Blob data = new Blob(filepath);
        String id = sha1;

        File blobloc = new File(indexblobs.toPath().resolve(id + ".txt").toString());
        converter.store(data, blobloc.toString());
        converter.store(index, indexdir.toPath().resolve("index.ser").toString());
        converter.store(curr, commitdir.toPath()
                .resolve(curr.getID() + ".ser").toString());
        converter.store(currBranch, pointerdir.toPath()
                .resolve(currBranch.getName() + ".ser").toString());
        converter.store(head, pointerdir.toPath().resolve("HEAD.ser").toString());
    }

    public static void commit(String msg) {
        Object obj = null;
        StagingArea index =
                (StagingArea) converter.generate(obj, indexdir.toPath()
                        .resolve("index.ser").toString());
        if (index.stage.size() == 0 && index.removed.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else {
            Object obj1 = null;
            Object obj2 = null;
            Pointers head =
                    (Pointers) converter.generate(obj1, pointerdir.toPath()
                            .resolve("HEAD.ser").toString());
            Pointers currBranch =
                    (Pointers) converter.generate
                            (obj2, pointerdir.toPath()
                                    .resolve(head.getCurrBranch() + ".ser").toString());
            String target = currBranch.getReference();

            Object obj3 = null;
            Commit parent =
                    (Commit) converter.generate(obj3, commitdir.toPath()
                            .resolve(target + ".ser").toString());
            Object map = null;
            HashMap<String, String> mapped =
                    (HashMap) converter.generate(map, commitdir.toPath()
                            .resolve("Commit_map.ser").toString());
            HashMap<String, String> files = new HashMap<>();
            HashMap<String, String> copy = index.committing();
            if (parent.getFiles() != null) {
                files.putAll(parent.getFiles());
            }
            files.putAll(copy);
            try {
                for (Iterator<String> iterator = files.keySet().iterator(); iterator.hasNext();) {
                    String file = iterator.next();
                    if (index.removed.contains(file)) {
                        iterator.remove();
                    }
                }
            } catch (NullPointerException e) {
                return;
            }

            String parentId = parent.getID();
            Commit curr = new Commit(files, msg, parentId);
            index.removed.clear();
            String subId = (String) curr.getID().subSequence(0, 6);
            mapped.put(subId, curr.getID());
            converter.store(mapped, commitdir.toPath().resolve("Commit_map.ser").toString());
            converter.store(curr, commitdir.toPath().resolve(curr.getID() + ".ser").toString());
            converter.store(parent, commitdir.toPath().resolve(parentId + ".ser").toString());
            converter.store(index, indexdir.toPath().resolve("index.ser").toString());

            ArrayList<String> help = new ArrayList<>();
            for (Object id: curr.getFiles().values()) {
                if (!contains(blobdir.list(), id + ".txt")) {
//                    if (msg.equals("Add h.txt and remove g.txt")) {
//                        System.out.println(id);
//                    }
                    Object temp = null;
                    Blob stored =
                            (Blob) converter.generate(temp, indexblobs.toPath()
                                    .resolve(id + ".txt").toString());
                    converter.store(stored, blobdir.toPath().resolve(id + ".txt").toString());
                    new File(indexblobs.toPath().resolve(id + ".txt").toString()).delete();
                }
            }
            currBranch.moveBranch(curr.getID());
            converter.store(currBranch, pointerdir.toPath()
                    .resolve(currBranch.getName() + ".ser").toString());
            converter.store(head, pointerdir.toPath().resolve("HEAD.ser").toString());
        }
    }

    public static void remove(String file) {
        Object obj = null;
        StagingArea index =
                (StagingArea) converter.generate(obj, indexdir.toPath()
                        .resolve("index.ser").toString());
        Object obj1 = null;
        Object obj2 = null;
        Pointers head =
                (Pointers) converter.generate(obj1, pointerdir.toPath()
                        .resolve("HEAD.ser").toString());
        Pointers currBranch =
                (Pointers) converter.generate
                        (obj2, pointerdir.toPath()
                                .resolve(head.getCurrBranch() + ".ser").toString());
        String target = currBranch.getReference();

        Object obj3 = null;
        Commit curr =
                (Commit) converter.generate(obj3, commitdir.toPath()
                        .resolve(target + ".ser").toString());

        if (curr.getFiles() != null) {
            if (!contains(workingdir.list(), file)) {
                index.remove(file);
                index.removed.add(file);
                converter.store(currBranch, pointerdir.toPath()
                        .resolve(currBranch.getName() + ".ser").toString());
                converter.store(head, pointerdir.toPath().resolve("HEAD.ser").toString());
                converter.store(curr, commitdir.toPath().resolve(curr.getID() + ".ser").toString());
                converter.store(index, indexdir.toPath().resolve("index.ser").toString());
                return;
            } else if (curr.getFiles().containsKey(file)) {
                try {
                    Files.delete(workingdir.toPath().resolve(file));
                } catch (IOException e) {
                    return;
                }
                index.removed.add(file);
            }
            if (index.stage.containsKey(file)) {
                index.remove(file);
                try {
                    Files.delete(indexblobs.toPath().resolve(file));
                } catch (IOException e) {
                    return;
                }
            }
        } else if (index.stage.containsKey(file)) {
            String id = index.stage.get(file);
            index.remove(file);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        converter.store(currBranch, pointerdir.toPath()
                .resolve(currBranch + ".ser").toString());
        converter.store(head, pointerdir.toPath()
                .resolve("HEAD.ser").toString());
        converter.store(curr, commitdir.toPath()
                .resolve(curr.getID() + ".ser").toString());
        converter.store(index, indexdir.toPath()
                .resolve("index.ser").toString());
    }

    public static void log() {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Object obj1 = null;
        Object obj2 = null;
        Pointers head =
                (Pointers) converter.generate(obj1, pointerdir.toPath()
                        .resolve("HEAD.ser").toString());
        Pointers currBranch =
                (Pointers) converter.generate
                        (obj2, pointerdir.toPath()
                                .resolve(head.getCurrBranch() + ".ser").toString());
        String ref = currBranch.getReference();
        Object obj3 = null;
        Commit curr =
                (Commit) converter.generate(obj3, commitdir.toPath()
                        .resolve(ref + ".ser").toString());

        while (!curr.getLog().equals("initial commit")) {
            System.out.println("===");
            System.out.println("Commit " + curr.getID());
            System.out.println(curr.getTimestamp());
            System.out.println(curr.getLog());
            System.out.println();
            converter.store(curr, commitdir.toPath()
                    .resolve(curr.getID() + ".ser").toString());
            Object temp = null;
            curr = (Commit) converter.generate
                    (temp, commitdir.toPath()
                            .resolve(curr.getPrev() + ".ser").toString());
        }
        System.out.println("===");
        System.out.println("Commit " + curr.getID());
        System.out.println(curr.getTimestamp());
        System.out.println(curr.getLog());
        System.out.println();
        converter.store(curr, commitdir.toPath()
                .resolve(curr.getID() + ".ser").toString());
        converter.store(currBranch, pointerdir.toPath()
                .resolve(currBranch.getName() + ".ser").toString());
        converter.store(head, pointerdir.toPath()
                .resolve("HEAD.ser").toString());
    }

    public static void globallog() {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        String[] commitlist = commitdir.list();
        for (String commit : commitlist) {
            Object obj = null;
            if (!commit.equals("Commit_map.ser")) {
                Commit curr =
                        (Commit) converter.generate(obj, commitdir.toPath()
                                .resolve(commit).toString());
                System.out.println("===");
                System.out.println("Commit " + curr.getID());
                System.out.println(curr.getTimestamp());
                System.out.println(curr.getLog());
                System.out.println();
                converter.store(curr, commitdir.toPath().resolve(commit).toString());
            }
        }
    }

    public static void find(String msg) {
        boolean bool = false;
        String[] commitlist = commitdir.list();
        for (String commit: commitlist) {
            Object obj = null;
            if (!commit.equals("Commit_map.ser")) {
                Commit curr =
                        (Commit) converter.generate(obj, commitdir.toPath()
                                .resolve(commit).toString());
                if (curr.getLog().equals(msg)) {
                    System.out.println(curr.getID());
                    bool = true;
                }
                converter.store(curr, commitdir.toPath().resolve(commit).toString());
            }
        }
        if (!bool) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        Object obj = null;
        Object obj1 = null;
        StagingArea index =
                (StagingArea) converter.generate(obj, indexdir.toPath()
                        .resolve("index.ser").toString());
        String[] pointers = pointerdir.list();
        Pointers head =
                (Pointers) converter.generate(obj1, pointerdir.toPath()
                        .resolve("HEAD.ser").toString());
        String curr = head.getCurrBranch();
        List<String> sorted = new ArrayList<>();
        System.out.println("=== Branches ===");
        for (String branches: pointers) {
            Object temp = null;
            Pointers current =
                    (Pointers) converter.generate(temp, pointerdir.toPath()
                            .resolve(branches).toString());
            if (!current.getName().equals("HEAD")) {
                sorted.add(current.getName());
            }
            converter.store(current, pointerdir.toPath().resolve(branches).toString());
        }
        Collections.sort(sorted);
        sorted = sorted.stream()
                .distinct()
                .collect(Collectors.toList());

        for (String name: sorted) {
            if (name.equals(curr)) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
        System.out.println();
        converter.store(head, pointerdir.toPath().resolve("HEAD.ser").toString());

        System.out.println("=== Staged Files ===");
        ArrayList<String> sortfiles = new ArrayList<>();
        for (String files: index.stage.keySet()) {
            sortfiles.add(files);
        }
        Collections.sort(sortfiles);

        for (String staged: sortfiles) {
            System.out.println(staged);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        Collections.sort(index.removed);
        for (String name: index.removed) {
            System.out.println(name);
        }
        converter.store(index, indexdir.toPath()
                .resolve("index.ser").toString());
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void checkoutFile(String file) {
        Object obj1 = null;
        Object obj2 = null;
        Pointers head =
                (Pointers) converter.generate(obj1, pointerdir.toPath()
                        .resolve("HEAD.ser").toString());
        Pointers currBranch =
                (Pointers) converter.generate
                        (obj2, pointerdir.toPath()
                                .resolve(head.getCurrBranch() + ".ser").toString());
        String ref = currBranch.getReference();
        Object obj3 = null;
        Commit curr =
                (Commit) converter.generate(obj3, commitdir.toPath()
                        .resolve(ref + ".ser").toString());
        String target = (String) curr.getFiles().get(file);
        Object b = null;
        Blob wanted =
                (Blob) converter.generate(b, blobdir.toPath()
                        .resolve(target + ".txt").toString());
        File currBlob = new File(workingdir.toPath().resolve(file).toString());
        Utils.writeContents(currBlob, wanted.getContents());
        converter.store(curr, commitdir.toPath()
                .resolve(curr.getID() + ".ser").toString());
        converter.store(wanted, blobdir.toPath()
                .resolve(target + ".txt").toString());
    }

    public static void checkoutCommit(String id, String file) {
        if (id.length() < 40) {
            Object map = null;
            HashMap<String, String> mapped =
                    (HashMap<String, String>)
                            converter.generate(map, commitdir.toPath()
                                    .resolve("Commit_map.ser").toString());
            id = (String) id.subSequence(0, 6);
            if (mapped.containsKey(id)) {
                id = mapped.get(id);
            } else {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            converter.store(mapped, commitdir.toPath()
                    .resolve("Commit_map.ser").toString());
        }
        Object c = null;
        Commit curr =
                (Commit) converter.generate(c, commitdir.toPath()
                        .resolve(id + ".ser").toString());
        if (curr == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        String target = null;
        if (curr.getFiles() != null && curr.getFiles().containsKey(file)) {
            target = (String) curr.getFiles().get(file);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Object b = null;
        Blob wanted =
                (Blob) converter.generate(b, blobdir.toPath()
                        .resolve(target + ".txt").toString());
        File currBlob = new File(workingdir.toPath().resolve(file).toString());
        Utils.writeContents(currBlob, wanted.getContents());
        converter.store(curr, commitdir.toPath()
                .resolve(curr.getID() + ".ser").toString());
        converter.store(wanted, blobdir.toPath()
                .resolve(target + ".txt").toString());
    }

    public static void checkoutBranch(String branch) {
        String[] branches = pointerdir.list();
        boolean bool = false;
        String branchser = branch + ".ser";
        for (String name: branches) {
            if (!name.equals("HEAD.ser")) {
                if (name.equals(branchser)) {
                    bool = true;
                }
            }
        }
        if (!bool) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Object obj1 = null;
        Object obj2 = null;
        Object obj4 = null;
        Pointers head =
                (Pointers) converter.generate(obj1, pointerdir.toPath()
                        .resolve("HEAD.ser").toString());
        Pointers currBranch =
                (Pointers) converter.generate
                        (obj4, pointerdir.toPath()
                                .resolve(head.getCurrBranch() + ".ser").toString());
        String ref = currBranch.getReference();
        Object obj3 = null;
        Commit curr =
                (Commit) converter.generate(obj3, commitdir.toPath()
                        .resolve(ref + ".ser").toString());
        if (head.getCurrBranch().equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Pointers checkedout =
                (Pointers) converter.generate(obj2, pointerdir.toPath()
                        .resolve(branch + ".ser").toString());

        Object c = null;
        Commit checkcomm =
                (Commit) converter.generate(c, commitdir.toPath()
                        .resolve(checkedout.getReference() + ".ser").toString());
        String[] workfiles = workingdir.list();
        boolean conflict = false;
        String offender = "";
        for (String file: workfiles) {
            if (!file.equals(".gitlet")) {
                if (curr.getFiles() == null || !curr.getFiles().containsKey(file)) {
                    conflict = true;
                    offender = file;
                }
            }
        }
        if (conflict) {
            if (checkcomm.getFiles() != null
                    && checkcomm.getFiles().containsKey(offender)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                System.exit(0);
            }
        }
        if (checkcomm.getFiles() != null) {
            for (Object overwrite: checkcomm.getFiles().keySet()) {
                String overwrite1 = (String) overwrite;
                String sha1 = (String) checkcomm.getFiles().get(overwrite1);
                Object b = null;
                Blob wanted =
                        (Blob) converter.generate(b, blobdir.toPath()
                                .resolve(sha1 + ".txt").toString());
                File currBlob = new File(workingdir.toPath()
                        .resolve(overwrite1).toString());
                Utils.writeContents(currBlob, wanted.getContents());
                converter.store(wanted, blobdir.toPath()
                        .resolve(sha1 + ".txt").toString());
            }
        }
        head.setCurrBranch(branch);
        converter.store(head, pointerdir.toPath().resolve("HEAD.ser").toString());

        ArrayList<String> delete = new ArrayList<>();
        for (String file: workfiles) {
            if (checkcomm.getFiles() == null || !checkcomm.getFiles().containsKey(file)) {
                if (!file.equals(".gitlet")) {
                    delete.add(file);
                }
            }
        }
        for (String file: delete) {
            try {
                Files.delete(workingdir.toPath().resolve(file));
            } catch (IOException e) {
                return;
            }
        }
    }

    public static void branch(String name) {
        String[] branches = pointerdir.list();
        boolean bool = false;
        String branchser = name + ".ser";
        for (String branchh: branches) {
            if (!branchh.equals("HEAD.ser")) {
                if (branchh.equals(branchser)) {
                    bool = true;
                }
            }
        }
        if (bool) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Object obj1 = null;
        Object obj2 = null;
        Pointers head =
                (Pointers) converter.generate(obj1, pointerdir.toPath()
                        .resolve("HEAD.ser").toString());
        Pointers currBranch =
                (Pointers) converter.generate
                        (obj2, pointerdir.toPath()
                                .resolve(head.getCurrBranch() + ".ser").toString());
        String target = currBranch.getReference();
        Object split = null;
        Commit splitted =
                (Commit) converter.generate(split, commitdir.toPath()
                        .resolve(target + ".ser").toString());
        splitted.setSplitPoint(true);
        converter.store(splitted, commitdir.toPath()
                .resolve(target + ".ser").toString());
        Pointers newGuy = new Pointers(name, target);
        converter.store(newGuy, pointerdir.toPath().resolve(name + ".ser").toString());
    }

    public static void removeBranch(String branch) {
        Object obj1 = null;
        Object obj2 = null;
        Pointers head =
                (Pointers) converter.generate(obj1, pointerdir.toPath()
                        .resolve("HEAD.ser").toString());
        Pointers currBranch =
                (Pointers) converter.generate
                        (obj2, pointerdir.toPath()
                                .resolve(head.getCurrBranch() + ".ser").toString());
        String currName = currBranch.getName();
        if (currBranch.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
        }
        String[] branches = pointerdir.list();
        boolean bool = false;
        for (String branchNames: branches) {
            if (!branchNames.equals("HEAD")) {
                if (branchNames.equals(branch)) {
                    try {
                        Files.delete(pointerdir.toPath().resolve(branch + ".ser"));
                    } catch (IOException e) {
                        return;
                    }
                    bool = true;
                }
            }
        }
        if (!bool) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String target = currBranch.getReference();
        Object split = null;
        Commit splitted =
                (Commit) converter.generate(split, commitdir.toPath()
                        .resolve(target + ".ser").toString());
        splitted.setSplitPoint(false);
        converter.store(splitted, commitdir.toPath()
                .resolve(target + ".ser").toString());
    }

    public static void reset(String id) {
        if (id.length() < 40) {
            Object map = null;
            HashMap<String, String> mapped =
                    (HashMap<String, String>)
                            converter.generate(map, commitdir.toPath()
                                    .resolve("Commit_map.ser").toString());
            if (mapped.containsKey((String) id.subSequence(0, 5))) {
                id = mapped.get(id);
            } else {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            converter.store(mapped, commitdir.toPath()
                    .resolve("Commit_map.ser").toString());
        }
        Object c = null;
        Commit reset =
                (Commit) converter.generate(c, commitdir.toPath()
                        .resolve(id + ".ser").toString());
        if (reset == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Object obj1 = null;
        Object obj4 = null;
        Pointers head =
                (Pointers) converter.generate(obj1, pointerdir.toPath()
                        .resolve("HEAD.ser").toString());
        Pointers currBranch =
                (Pointers) converter.generate
                        (obj4, pointerdir.toPath()
                                .resolve(head.getCurrBranch() + ".ser").toString());
        String ref = currBranch.getReference();
        Object obj3 = null;
        Commit curr =
                (Commit) converter.generate(obj3, commitdir.toPath()
                        .resolve(ref + ".ser").toString());
        String[] workfiles = workingdir.list();
        boolean conflict = false;
        String offender = "";
        for (String file: workfiles) {
            if (!file.equals(".gitlet")) {
                if (curr.getFiles() == null || !curr.getFiles().containsKey(file)) {
                    conflict = true;
                    offender = file;
                }
            }
        }
        if (conflict) {
            if (reset.getFiles() != null
                    && reset.getFiles().containsKey(offender)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                System.exit(0);
            }
        }
        for (Object file: reset.getFiles().keySet()) {
            String filename = (String) file;
            Repository.checkoutCommit(id, filename);
        }
        currBranch.setReference(reset.getID());
        converter.store(currBranch, pointerdir.toPath()
                .resolve(currBranch.getName() + ".ser").toString());
        Object ind = null;
        StagingArea index =
                (StagingArea) converter.generate(ind, indexdir.toPath()
                        .resolve("index.ser").toString());
        index.clear();
        converter.store(index, indexdir.toPath()
                .resolve("index.ser").toString());
    }

    public static boolean contains(String[] files, String id) {
        for (String strings: files) {
            if (strings.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
