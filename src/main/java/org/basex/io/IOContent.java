package org.basex.io;

import org.basex.io.in.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * {@link IO} reference, representing a byte array.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class IOContent extends IO {
  /** Content. */
  private final byte[] cont;

  /**
   * Constructor.
   * @param c contents
   */
  public IOContent(final byte[] c) {
    this(c, "");
  }

  /**
   * Constructor.
   * @param c contents
   */
  public IOContent(final String c) {
    this(Token.token(c), "");
  }

  /**
   * Constructor.
   * @param c contents
   * @param p content path
   */
  public IOContent(final byte[] c, final String p) {
    super(p);
    cont = c;
    len = cont.length;
  }

  @Override
  public byte[] read() {
    return cont;
  }

  @Override
  public InputSource inputSource() {
    final InputSource is = new InputSource(inputStream());
    is.setSystemId(name());
    return is;
  }

  @Override
  public ArrayInput inputStream() {
    return new ArrayInput(cont);
  }

  @Override
  public String toString() {
    return Token.string(cont);
  }
}
