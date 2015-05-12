/*
 * $Source$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 1998, Hoylen Sue.  All Rights Reserved.
 * <h.sue@ieee.org>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  Refer to
 * the supplied license for more details.
 *
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:11 UTC
 */

//----------------------------------------------------------------

package z3950.v3;
import asn1.*;

//================================================================
/**
 * Class for representing a <code>SortElement</code> from <code>Z39-50-APDU-1995</code>
 *
 * <pre>
 * SortElement ::=
 * CHOICE {
 *   generic [1] EXPLICIT SortKey
 *   datbaseSpecific [2] IMPLICIT SEQUENCE OF SortElement_datbaseSpecific
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class SortElement extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a SortElement.
 */

public
SortElement()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a SortElement from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
SortElement(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
{
  super(ber, check_tag);
}

//----------------------------------------------------------------
/**
 * Initializing object from a BER encoding.
 * This method is for internal use only. You should use
 * the constructor that takes a BEREncoding.
 *
 * @param ber the BER to decode.
 * @param check_tag if the tag should be checked.
 * @exception ASN1Exception if the BER encoding is bad.
 */

public void
ber_decode(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
{
  BERConstructed tagwrapper;

  // Null out all choices

  c_generic = null;
  c_datbaseSpecific = null;

  // Try choice generic
  if (ber.tag_get() == 1 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagwrapper = (BERConstructed) ber;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun SortElement: bad BER form\n");
    }
    if (tagwrapper.number_components() != 1)
      throw new ASN1EncodingException
        ("Zebulun SortElement: bad BER form\n");
    c_generic = new SortKey(tagwrapper.elementAt(0), true);
    return;
  }

  // Try choice datbaseSpecific
  if (ber.tag_get() == 2 &&
      ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    BEREncoding ber_data;
    ber_data = ber;
    BERConstructed ber_cons;
    try {
      ber_cons = (BERConstructed) ber_data;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun SortElement: bad BER form\n");
    }

    int num_parts = ber_cons.number_components();
    int p;

    c_datbaseSpecific = new SortElement_datbaseSpecific[num_parts];

    for (p = 0; p < num_parts; p++) {
      c_datbaseSpecific[p] = new SortElement_datbaseSpecific(ber_cons.elementAt(p), true);
    }
    return;
  }

  throw new ASN1Exception("Zebulun SortElement: bad BER encoding: choice not matched");
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of SortElement.
 *
 * @return	The BER encoding.
 * @exception	ASN1Exception Invalid or cannot be encoded.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  BEREncoding chosen = null;

  BEREncoding f2[];
  int p;
  BEREncoding enc[];

  // Encoding choice: c_generic
  if (c_generic != null) {
    enc = new BEREncoding[1];
    enc[0] = c_generic.ber_encode();
    chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);
  }

  // Encoding choice: c_datbaseSpecific
  if (c_datbaseSpecific != null) {
    if (chosen != null)
      throw new ASN1Exception("CHOICE multiply set");
    f2 = new BEREncoding[c_datbaseSpecific.length];

    for (p = 0; p < c_datbaseSpecific.length; p++) {
      f2[p] = c_datbaseSpecific[p].ber_encode();
    }

    chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);
  }

  // Check for error of having none of the choices set
  if (chosen == null)
    throw new ASN1Exception("CHOICE not set");

  return chosen;
}

//----------------------------------------------------------------

/**
 * Generating a BER encoding of the object
 * and implicitly tagging it.
 * <p>
 * This method is for internal use only. You should use
 * the ber_encode method that does not take a parameter.
 * <p>
 * This function should never be used, because this
 * production is a CHOICE.
 * It must never have an implicit tag.
 * <p>
 * An exception will be thrown if it is called.
 *
 * @param tag_type the type of the tag.
 * @param tag the tag.
 * @exception ASN1Exception if it cannot be BER encoded.
 */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
{
  // This method must not be called!

  // Method is not available because this is a basic CHOICE
  // which does not have an explicit tag on it. So it is not
  // permitted to allow something else to apply an implicit
  // tag on it, otherwise the tag identifying which CHOICE
  // it is will be overwritten and lost.

  throw new ASN1EncodingException("Zebulun SortElement: cannot implicitly tag");
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the SortElement. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");

  boolean found = false;

  if (c_generic != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: generic> ");
    found = true;
    str.append("generic ");
  str.append(c_generic);
  }

  if (c_datbaseSpecific != null) {
    if (found)
      str.append("<ERROR: multiple CHOICE: datbaseSpecific> ");
    found = true;
    str.append("datbaseSpecific ");
  str.append("{");
  for (p = 0; p < c_datbaseSpecific.length; p++) {
    str.append(c_datbaseSpecific[p]);
  }
  str.append("}");
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public SortKey c_generic;
public SortElement_datbaseSpecific c_datbaseSpecific[];

} // SortElement

//----------------------------------------------------------------
//EOF
