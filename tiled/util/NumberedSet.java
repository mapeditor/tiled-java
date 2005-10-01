/*
 *  Tiled Map Editor, (c) 2005
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Rainer Deyke <rainerd@eldwood.com>
 */

package tiled.util;

import java.util.Vector;
import tiled.util.NumberedSetIterator;

/**
 * A NumberedSet is a generic container of Objects where each element is
 * identified by an integer id.  Unlike with a Vector, the mapping between
 * id and element remains unaffected when elements are deleted.  This means
 * that the set of ids for a NumberedSet may not be contiguous.
 */
public class NumberedSet
{
  private Vector data;

  /**
   * Constructs a new empty NumberedSet.
   */
  public NumberedSet()
  {
    data = new Vector();
  }

  /**
   * Returns the element for a specific element, or null if the id does not
   * identify any element in this NumberedSet.
   */
  public Object get(int id)
  {
    try {
      return this.data.get(id);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  /**
   * Returns true if the NumberedSet contains an element for the specified id.
   */
  public boolean containsId(int id)
  {
    return this.get(id) != null;
  }

  /**
   * Sets the element for the specified id, replacing any previous element that
   * was associated with that id.  id should be a relatively small positive
   * integer.
   */
  public void set(int id, Object o)
  {
    if (id < 0) throw new IllegalArgumentException();
    if (id >= this.data.size()) this.data.setSize(id + 1);
    this.data.set(id, o);
    while (this.data.get(this.data.size() - 1) == null) {
      this.data.setSize(this.data.size() - 1);
    }
  }

  /**
   * Returns the first free id in the NumberedSet.
   */
  public int getFirstFreeId()
  {
    int id = 0;
    while (this.containsId(id)) ++id;
    return id;
  }

  /**
   * Returns the last id in the NumberedSet that is associated with an element,
   * or -1 if the NumberedSet is empty.
   */
  public int getMaxId()
  {
    return this.data.size() - 1;
  }

  /**
   * Returns an iterator to iterate over the elements of the NumberedSet.
   */
  public NumberedSetIterator iterator()
  {
    return new NumberedSetIterator(this);
  }

  /**
   * Adds a new element to the NumberedSet and returns its id.
   */
  public int add(Object o)
  {
    int id = this.getFirstFreeId();
    this.set(id, o);
    return id;
  }

  /**
   * Returns the id of the first element of the NumberedSet that is euqal to
   * the given object, or -1 otherwise.
   */
  public int find(Object o)
  {
    return this.data.indexOf(o);
  }

  /**
   * Returns true if at least one element of the NumberedSet is equal to the
   * given object.
   */
  public boolean contains(Object o)
  {
    return this.find(o) != -1;
  }

  /**
   * If this NumberedSet already contains an element equal to the given object,
   * return its id.  Otherwise insert the given object into the NumberedSet
   * and return its id.
   */
  public int findOrAdd(Object o)
  {
    int id = this.find(o);
    if (id != -1) return id;
    return this.add(o);
  }

  /**
   * Returns the number of actual elements in the NumberedSet.  This operation
   * is unfortunately somewhat slow because it requires iterating over the
   * underlying Vector.
   */
  public int size()
  {
    int total = 0;
    for (int id = 0; id < this.data.size(); ++id) {
      if (this.data.get(id) != null) ++total;
    }
    return total;
  }

}

