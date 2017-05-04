package uk.ac.standrews.cs.storage.implementations.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import uk.ac.standrews.cs.storage.exceptions.BindingAbsentException;
import uk.ac.standrews.cs.storage.exceptions.PersistenceException;
import uk.ac.standrews.cs.storage.implementations.NameObjectBindingImpl;
import uk.ac.standrews.cs.storage.interfaces.IDirectory;
import uk.ac.standrews.cs.storage.interfaces.NameObjectBinding;
import uk.ac.standrews.cs.storage.interfaces.StatefulObject;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static uk.ac.standrews.cs.storage.CastoreConstants.FOLDER_DELIMITER;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class DropboxDirectory extends DropboxStatefulObject implements IDirectory {

    private static final Logger log = Logger.getLogger(DropboxDirectory.class.getName());

    public DropboxDirectory(DbxClientV2 client, IDirectory parent, String name) {
        super(client, parent, name);
    }

    public DropboxDirectory(DbxClientV2 client, String name) {
        super(client, name);
    }

    @Override
    public String getPathname() {
        if (logicalParent == null) {
            return name + FOLDER_DELIMITER;
        } else if (name == null || name.isEmpty()) {
            return logicalParent.getPathname() + FOLDER_DELIMITER;
        } else {
            return logicalParent.getPathname() + name + FOLDER_DELIMITER;
        }
    }

    @Override
    public StatefulObject get(String name) throws BindingAbsentException {

        try {
            Metadata metadata = getMetadata(getPathname() + name);

            if (metadata instanceof FolderMetadata) {
                return new DropboxDirectory(client, this, name);
            } else if (metadata instanceof FileMetadata) {
                return new DropboxFile(client, this, name);
            } else {
                throw new BindingAbsentException("Object type is unknown");
            }

        } catch (DbxException e) {
            throw new BindingAbsentException("Unable to get object with name " + name);
        }

    }

    @Override
    public boolean contains(String name) {

        try {
            getMetadata(getPathname() + name);
        } catch (DbxException e) {
            return false;
        }

        return true;
    }

    @Override
    public void remove(String name) throws BindingAbsentException {

        name = normalise(name);

        try {
            client.files().delete(getPathname() + name);
        } catch (DbxException e) {
            throw new BindingAbsentException("Unable to delete object with name " + name);
        }
    }

    @Override
    public void persist() throws PersistenceException {
        try {
            if (exists()) return;

            String path = normalise(getPathname());

            client.files().createFolder(path);
        } catch (DbxException e) {
            throw new PersistenceException("Unable to persist directory", e);
        }
    }

    @Override
    public Iterator<NameObjectBinding> getIterator() {

        try {
            return new DirectoryIterator(client.files().listFolder(getPathname()));
        } catch (DbxException e) {
            log.log(Level.WARNING, "Unable to create the Directory Iterator properly");
        }

        return new DirectoryIterator();
    }

    private class DirectoryIterator implements Iterator<NameObjectBinding>  {

        private ListFolderResult list;
        private Iterator<Metadata> meta;

        DirectoryIterator(ListFolderResult list) {
            this.list = list;
            this.meta = list.getEntries().iterator();
        }

        DirectoryIterator() {
            meta = Collections.EMPTY_LIST.iterator();
        }

        public boolean hasNext() {
            boolean hasNext = meta.hasNext();

            // Check if there are more results from the original list of results.
            if (!hasNext && list != null) {

                hasNext = list.getHasMore();

                if (hasNext) {
                    String continueCursor = list.getCursor();
                    try {
                        list = client.files().listFolderContinue(continueCursor);
                        meta = list.getEntries().iterator();
                    } catch (DbxException e) {
                        return false;
                    }
                }
            }

            return hasNext;
        }

        public NameObjectBinding next() {

            if (!hasNext()) {
                return null;
            }

            try {
                Metadata metadata = meta.next();
                String name = metadata.getName();
                System.out.println("name " + name);
                StatefulObject obj = get(name);

                return new NameObjectBindingImpl(name, obj);
            } catch (BindingAbsentException e) {
                log.log(Level.SEVERE, "Unable to create a Binding Object for the next element from the iterator");
            }

            return null;
        }
    }

}
