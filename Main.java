package gitlet;

import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author
 */
public class Main {
    static Serializer converter = new Serializer();
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        // FILL THIS IN
        Repository repo = new Repository();
        repo.add("quinoa.txt");
        repo.commit("zander is a fag");
//        if (args.length == 0) {
//            System.out.println("Please enter a command");
//            System.exit(0);
//        } else if (args[0].equals("init")) {
//            Repository repo = new Repository();
//        } else if (!Repository.contains
//                (new File(System.getProperty("user.dir")).list(), ".gitlet")) {
//            System.out.println("Not in an initialized gitlet directory.");
//            System.exit(0);
//        } else if (args[0].equals("add")) {
//            if (args.length != 2) {
//                System.out.println("Incorrect operands.");
//                System.exit(0);
//            } else {
//                String file = args[1];
//                Repository.add(file);
//            }
//        } else if (args[0].equals("commit")) {
//            if (args.length != 2 || args[1].equals("")) {
//                System.out.println("Please enter a commit message.");
//                System.exit(0);
//            } else {
//                String msg = args[1];
//                Repository.commit(msg);
//            }
//        } else if (args[0].equals("rm")) {
//            if (args.length != 2) {
//                System.out.println("Incorrect operands.");
//                System.exit(0);
//            } else {
//                String file = args[1];
//                Repository.remove(file);
//            }
//        } else if (args[0].equals("log")) {
//            Repository.log();
//        } else if (args[0].equals("global-log")) {
//            Repository.globallog();
//        } else if (args[0].equals("find")) {
//            if (args.length != 2) {
//                System.out.println("Incorrect operands.");
//                System.exit(0);
//            } else {
//                String msg = args[1];
//                Repository.find(msg);
//            }
//        } else if (args[0].equals("status")) {
//            Repository.status();
//        } else if (args[0].equals("checkout")) {
//            if (args[1].equals("--")) {
//                if (args.length != 3) {
//                    System.out.println("Incorrect operands.");
//                    System.exit(0);
//                } else {
//                    Repository.checkoutFile(args[2]);
//                }
//            } else if (args[0].equals("checkout") && !args[1].equals("--") && args.length > 2) {
//                if (!args[2].equals("--")) {
//                    System.out.println("Incorrect operands.");
//                    System.exit(0);
//                }
//                Repository.checkoutCommit(args[1], args[3]);
//            } else if (args[0].equals("checkout") && args.length == 2) {
//                Repository.checkoutBranch(args[1]);
//            }
//        } else if (args[0].equals("branch")) {
//            Repository.branch(args[1]);
//        } else if (args[0].equals("rm-branch")) {
//            Repository.removeBranch(args[1]);
//        } else if (args[0].equals("reset")) {
//            Repository.reset(args[1]);
//        }
    }
}



