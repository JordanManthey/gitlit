package gitlet;

import java.io.*;

/**
 * Created by rnatarajan1 on 7/17/2017.
 */
public class Serializer implements Serializable {

    public void store(Object obj, String storage) {
        File outFile = new File(storage);
        try {
            FileOutputStream outstream = new FileOutputStream(outFile);
            ObjectOutputStream out =
                    new ObjectOutputStream(outstream);
            out.writeObject(obj);
            out.close();
        } catch (IOException excp) {
            System.out.println("here");
        }
    }

    public Object generate(Object obj, String storage) {
        File inFile = new File(storage);
        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(inFile));
            obj = inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            obj = null;
        }
        return obj;
    }
}
