/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators.Multi2Sim;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class FileEnumeration implements Enumeration {

    private String[] listOfFiles;
    private int current = 0;

    public FileEnumeration(String[] listOfFiles) {
        this.listOfFiles = listOfFiles;
    }

    public boolean hasMoreElements() {
        if (current < listOfFiles.length)
            return true;
        else
            return false;
    }

    public Object nextElement() {
        InputStream in = null;

        if (!hasMoreElements())
            throw new NoSuchElementException("No more files.");
        else {
            String nextElement = listOfFiles[current];
            current++;
            try {
                in = new FileInputStream(nextElement);
            } catch (FileNotFoundException e) {
                System.err.println(nextElement + " does not exist");
            }
        }
        return in;
    }
}
