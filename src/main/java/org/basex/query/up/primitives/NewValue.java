package org.basex.query.up.primitives;

import org.basex.query.item.Nod;
import org.basex.query.item.QNm;

/**
 * Abstract update primitive which holds an additional 'name' attribute to for
 * updating values, names, etc..
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public abstract class NewValue extends UpdatePrimitive {
  /** New name. */
  final QNm name;

  /**
   * Constructor.
   * @param n target node
   * @param newName new name
   */
  public NewValue(final Nod n, final QNm newName) {
    super(n);
    name = newName;
  }
}