package org.basex.data;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.util.Token;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class allows to output XML results via SAX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class SAXSerializer extends Serializer implements XMLReader {
  /** Content handler reference. */
  private ContentHandler content;
  /** Lexical handler reference. */
  private LexicalHandler lexical;
  /** Content handler reference. */
  private final Result result;
  /** Caches a tag name. */
  private String tag;
  /** Caches attributes. */
  private AttributesImpl atts;

  /**
   * Constructor.
   * @param res result
   */
  public SAXSerializer(final Result res) {
    result = res;
  }

  /* Implements XMLReader method. */
  public ContentHandler getContentHandler() {
    return content;
  }

  /* Implements XMLReader method. */
  public DTDHandler getDTDHandler() {
    return null;
  }

  /* Implements XMLReader method. */
  public EntityResolver getEntityResolver() {
    return null;
  }

  /* Implements XMLReader method. */
  public ErrorHandler getErrorHandler() {
    return null;
  }

  /* Implements XMLReader method. */
  public boolean getFeature(final String name) {
    return false;
  }

  /* Implements XMLReader method. */
  public Object getProperty(final String name) {
    return null;
  }

  /* Implements XMLReader method. */
  public void parse(final InputSource input) throws SAXException {
    parse("");
  }

  /* Implements XMLReader method. */
  public void parse(final String id) throws SAXException {
    try {
      // execute query
      content.startDocument();
      result.serialize(this);
      content.endDocument();
    } catch(final Exception ex) {
      throw new SAXException(ex);
    }
  }

  /* Implements XMLReader method. */
  public void setContentHandler(final ContentHandler c) {
    content = c;
  }

  /**
   * Sets the lexical handler for reacting on comments.
   * @param l handler
   */
  public void setLexicalHandler(final LexicalHandler l) {
    lexical = l;
  }

  /* Implements XMLReader method. */
  public void setDTDHandler(final DTDHandler h) {
    Main.notimplemented();
  }

  /* Implements XMLReader method. */
  public void setEntityResolver(final EntityResolver resolver) {
    Main.notimplemented();
  }

  /* Implements XMLReader method. */
  public void setErrorHandler(final ErrorHandler h) {
    Main.notimplemented();
  }

  /* Implements XMLReader method. */
  public void setFeature(final String name, final boolean value) {
    Main.notimplemented();
  }

  /* Implements XMLReader method. */
  public void setProperty(final String name, final Object value) {
    Main.notimplemented();
  }

  @Override
  public void openResult() throws IOException {
    openElement(RESULT);
  }

  @Override
  public void closeResult() throws IOException {
    closeElement();
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) {
    final String an = Token.string(n);
    atts.addAttribute("", an, an, "", Token.string(v));
  }

  @Override
  protected void start(final byte[] t) {
    tag = Token.string(t);
    atts = new AttributesImpl();
  }

  @Override
  protected void empty() throws IOException {
    finish();
    try {
      content.endElement("", tag, tag);
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  protected void finish() throws IOException {
    try {
      content.startElement("", tag, tag, atts);
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  protected void close(final byte[] t) throws IOException {
    try {
      tag = Token.string(t);
      content.endElement("", tag, tag);
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  protected void cls() {
  }

  @Override
  public void text(final byte[] b) throws IOException {
    finishElement();
    final char[] c = Token.string(b).toCharArray();
    try {
      content.characters(c, 0, c.length);
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  public void text(final byte[] b, final FTPos ftp) throws IOException {
    text(b);
  }

  @Override
  public void comment(final byte[] t) throws IOException {
    finishElement();
    try {
      final char[] c = Token.string(t).toCharArray();
      if(lexical != null) lexical.comment(c, 0, t.length);
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException {
    finishElement();
    try {
      content.processingInstruction(Token.string(n), Token.string(v));
    } catch(final SAXException ex) {
      throw new IOException(ex.getMessage());
    }
  }

  @Override
  public void item(final byte[] b) throws IOException {
    finishElement();
    throw new IOException("Can't serialize atomic items.");
  }
}