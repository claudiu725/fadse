/** 
 * BinaryTournamentComparator.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.base.operator.comparator;

import java.util.*;
import jmetal.base.Solution;

/**
 * This class implements a <code>Comparator</code> for <code>Solution</code>
 */
public class BinaryTournamentComparator implements Comparator<Solution> {
  
  /**
   * stores a dominance comparator
   */
  private static final Comparator<Solution> dominance_ = new DominanceComparator();
  private static final Comparator<Solution> crowding_ = new CrowdingDistanceComparator();
  
  /**
   * Compares two solutions.
   * A <code>Solution</code> a is less than b for this <code>Comparator</code>.
   * if the crowding distance of a if greater than the crowding distance of b.
   * @param o1 Object representing a <code>Solution</code>.
   * @param o2 Object representing a <code>Solution</code>.
   * @return -1, or 0, or 1 if o1 is less than, equals, or greater than o2,
   * respectively.
   */
  public int compare(Solution o1, Solution o2) {
    int flag = dominance_.compare(o1,o2);
    if (flag!=0) {
      return flag;
    }
    
    return crowding_.compare(o1, o2);
  } // compare
} // BinaryTournamentComparator.
