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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:21 UTC
 */

//----------------------------------------------------------------

package z3950.RS_Explain;
import asn1.*;
import z3950.v3.AttributeElement;
import z3950.v3.AttributeList;
import z3950.v3.AttributeSetId;
import z3950.v3.DatabaseName;
import z3950.v3.ElementSetName;
import z3950.v3.IntUnit;
import z3950.v3.InternationalString;
import z3950.v3.OtherInformation;
import z3950.v3.Specification;
import z3950.v3.StringOrNumeric;
import z3950.v3.Term;
import z3950.v3.Unit;

//================================================================
/**
 * Class for representing a <code>TermListInfo_termLists</code> from <code>RecordSyntax-explain</code>
 *
 * <pre>
 * TermListInfo_termLists ::=
 * SEQUENCE {
 *   name [1] IMPLICIT InternationalString
 *   title [2] IMPLICIT HumanString OPTIONAL
 *   searchCost [3] IMPLICIT INTEGER OPTIONAL
 *   scanable [4] IMPLICIT BOOLEAN
 *   broader [5] IMPLICIT SEQUENCE OF InternationalString OPTIONAL
 *   narrower [6] IMPLICIT SEQUENCE OF InternationalString OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class TermListInfo_termLists extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a TermListInfo_termLists.
 */

public
TermListInfo_termLists()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a TermListInfo_termLists from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
TermListInfo_termLists(BEREncoding ber, boolean check_tag)
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
  // TermListInfo_termLists should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun TermListInfo_termLists: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Decoding: name [1] IMPLICIT InternationalString

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun TermListInfo_termLists: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun TermListInfo_termLists: bad tag in s_name\n");

  s_name = new InternationalString(p, false);
  part++;

  // Decoding: title [2] IMPLICIT HumanString OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun TermListInfo_termLists: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_title = new HumanString(p, false);
    part++;
  }

  // Decoding: searchCost [3] IMPLICIT INTEGER OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun TermListInfo_termLists: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 3 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_searchCost = new ASN1Integer(p, false);
    part++;
  }

  // Decoding: scanable [4] IMPLICIT BOOLEAN

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun TermListInfo_termLists: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 4 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun TermListInfo_termLists: bad tag in s_scanable\n");

  s_scanable = new ASN1Boolean(p, false);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_broader = null;
  s_narrower = null;

  // Decoding: broader [5] IMPLICIT SEQUENCE OF InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 5 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_broader = new InternationalString[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_broader[n] = new InternationalString(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: narrower [6] IMPLICIT SEQUENCE OF InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 6 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_narrower = new InternationalString[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_narrower[n] = new InternationalString(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun TermListInfo_termLists: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the TermListInfo_termLists.
 *
 * @exception	ASN1Exception Invalid or cannot be encoded.
 * @return	The BER encoding.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of TermListInfo_termLists, implicitly tagged.
 *
 * @param tag_type	The type of the implicit tag.
 * @param tag	The implicit tag.
 * @return	The BER encoding of the object.
 * @exception	ASN1Exception When invalid or cannot be encoded.
 * @see asn1.BEREncoding#UNIVERSAL_TAG
 * @see asn1.BEREncoding#APPLICATION_TAG
 * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
 * @see asn1.BEREncoding#PRIVATE_TAG
 */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
{
  // Calculate the number of fields in the encoding

  int num_fields = 2; // number of mandatories
  if (s_title != null)
    num_fields++;
  if (s_searchCost != null)
    num_fields++;
  if (s_broader != null)
    num_fields++;
  if (s_narrower != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding f2[];
  int p;

  // Encoding s_name: InternationalString 

  fields[x++] = s_name.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

  // Encoding s_title: HumanString OPTIONAL

  if (s_title != null) {
    fields[x++] = s_title.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
  }

  // Encoding s_searchCost: INTEGER OPTIONAL

  if (s_searchCost != null) {
    fields[x++] = s_searchCost.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
  }

  // Encoding s_scanable: BOOLEAN 

  fields[x++] = s_scanable.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);

  // Encoding s_broader: SEQUENCE OF OPTIONAL

  if (s_broader != null) {
    f2 = new BEREncoding[s_broader.length];

    for (p = 0; p < s_broader.length; p++) {
      f2[p] = s_broader[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, f2);
  }

  // Encoding s_narrower: SEQUENCE OF OPTIONAL

  if (s_narrower != null) {
    f2 = new BEREncoding[s_narrower.length];

    for (p = 0; p < s_narrower.length; p++) {
      f2[p] = s_narrower[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 6, f2);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the TermListInfo_termLists. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("name ");
  str.append(s_name);
  outputted++;

  if (s_title != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("title ");
    str.append(s_title);
    outputted++;
  }

  if (s_searchCost != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("searchCost ");
    str.append(s_searchCost);
    outputted++;
  }

  if (0 < outputted)
    str.append(", ");
  str.append("scanable ");
  str.append(s_scanable);
  outputted++;

  if (s_broader != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("broader ");
    str.append("{");
    for (p = 0; p < s_broader.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_broader[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_narrower != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("narrower ");
    str.append("{");
    for (p = 0; p < s_narrower.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_narrower[p]);
    }
    str.append("}");
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public InternationalString s_name;
public HumanString s_title; // optional
public ASN1Integer s_searchCost; // optional
public ASN1Boolean s_scanable;
public InternationalString s_broader[]; // optional
public InternationalString s_narrower[]; // optional

//----------------------------------------------------------------
/*
 * Enumerated constants for class.
 */

// Enumerated constants for searchCost
public static final int E_optimized = 0;
public static final int E_normal = 1;
public static final int E_expensive = 2;
public static final int E_filter = 3;

} // TermListInfo_termLists

//----------------------------------------------------------------
//EOF
