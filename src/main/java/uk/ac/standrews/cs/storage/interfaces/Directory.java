package uk.ac.standrews.cs.storage.interfaces;

import uk.ac.standrews.cs.storage.exceptions.BindingAbsentException;

import java.util.Iterator;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public interface Directory extends StatefulObject {

    StatefulObject get(String name) throws BindingAbsentException;

    boolean contains(String name);

    void addSOSFile(File file, String name);

    void addSOSDirectory(Directory directory, String name);

    void remove(String name) throws BindingAbsentException;

    /**
     * An iterator of the Stateful objects contained in this directory.
     * @return
     */
    Iterator<NameObjectBinding> getIterator();

}
