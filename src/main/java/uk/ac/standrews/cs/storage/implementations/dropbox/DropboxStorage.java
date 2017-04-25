package uk.ac.standrews.cs.storage.implementations.dropbox;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import uk.ac.standrews.cs.storage.data.Data;
import uk.ac.standrews.cs.storage.exceptions.DestroyException;
import uk.ac.standrews.cs.storage.exceptions.StorageException;
import uk.ac.standrews.cs.storage.implementations.CommonStorage;
import uk.ac.standrews.cs.storage.interfaces.IDirectory;
import uk.ac.standrews.cs.storage.interfaces.IFile;
import uk.ac.standrews.cs.storage.interfaces.IStorage;

import java.util.Locale;

/**
 * Dropbox CORE API available here - https://www.dropbox.com/developers/documentation/java
 * Github project - https://github.com/dropbox/dropbox-sdk-java
 * Useful example - https://github.com/dropbox/dropbox-sdk-java/blob/master/examples/upload-file/src/main/java/com/dropbox/core/examples/upload_file/Main.java
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class DropboxStorage extends CommonStorage implements IStorage {

    private DbxClientV2 client;

    public DropboxStorage(final String accessToken, String root) {

        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial")
                .withUserLocaleFrom(Locale.UK)
                .build();
        client = new DbxClientV2(config, accessToken);

        createRoot();
    }

    @Override
    public IDirectory createDirectory(IDirectory parent, String name) throws StorageException {
        return null;
    }

    @Override
    public IDirectory createDirectory(String name) throws StorageException {
        return null;
    }

    @Override
    public IFile createFile(IDirectory parent, String filename) throws StorageException {
        return null;
    }

    @Override
    public IFile createFile(IDirectory parent, String filename, Data data) throws StorageException {
        return null;
    }

    @Override
    public void destroy() throws DestroyException {

    }

    private void createRoot() {
    }
}
