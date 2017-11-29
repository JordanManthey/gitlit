package gitlet;
import java.io.File;
import java.util.HashMap;

/**
 * Created by rnatarajan1 on 7/15/2017.
 */
public class BlobHolder {
    protected HashMap<String, File> holder;
    public BlobHolder() {
        holder = new HashMap<>();
    }

    //return blobfile given sha-1
    public File returnBlob(String sha1) {
        return this.holder.get(sha1);
    }

}
