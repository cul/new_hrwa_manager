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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:12 UTC
 */

//----------------------------------------------------------------

package z3950.DiagFormat;
import asn1.*;
import z3950.v3.AttributeList;
import z3950.v3.DatabaseName;
import z3950.v3.DefaultDiagFormat;
import z3950.v3.InternationalString;
import z3950.v3.SortElement;
import z3950.v3.Specification;
import z3950.v3.Term;

//================================================================
/**
 * Class for representing a <code>DiagFormat_badSpec</code> from <code>DiagnosticFormatDiag1</code>
 *
 * <pre>
 * DiagFormat_badSpec ::=
 * SEQUENCE {
 *   spec [1] IMPLICIT Specification
 *   db [2] IMPLICIT DatabaseName OPTIONAL
 *   goodOnes [3] IMPLICIT SEQUENCE OF Specification OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class DiagFormat_badSpec extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a DiagFormat_badSpec.
 */

public
DiagFormat_badSpec()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a DiagFormat_badSpec from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
DiagFormat_badSpec(BEREncoding ber, boolean check_tag)
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
  // DiagFormat_badSpec should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun DiagFormat_badSpec: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Decoding: spec [1] IMPLICIT Specification

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun DiagFormat_badSpec: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun DiagFormat_badSpec: bad tag in s_spec\n");

  s_spec = new Specification(p, false);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_db = null;
  s_goodOnes = null;

  // Decoding: db [2] IMPLICIT DatabaseName OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_db = new DatabaseName(p, false);
    part++;
  }

  // Decoding: goodOnes [3] IMPLICIT SEQUENCE OF Specification OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 3 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_goodOnes = new Specification[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_goodOnes[n] = new Specification(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun DiagFormat_badSpec: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the DiagFormat_badSpec.
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
 * Returns a BER encoding of DiagFormat_badSpec, implicitly tagged.
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

  int num_fields = 1; // number of mandatories
  if (s_db != null)
    num_fields++;
  if (s_goodOnes != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding f2[];
  int p;

  // Encoding s_spec: Specification 

  fields[x++] = s_spec.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

  // Encoding s_db: DatabaseName OPTIONAL

  if (s_db != null) {
    fields[x++] = s_db.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
  }

  // Encoding s_goodOnes: SEQUENCE OF OPTIONAL

  if (s_goodOnes != null) {
    f2 = new BEREncoding[s_goodOnes.length];

    for (p = 0; p < s_goodOnes.length; p++) {
      f2[p] = s_goodOnes[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, f2);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the DiagFormat_badSpec. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("spec ");
  str.append(s_spec);
  outputted++;

  if (s_db != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("db ");
    str.append(s_db);
    outputted++;
  }

  if (s_goodOnes != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("goodOnes ");
    str.append("{");
    for (p = 0; p < s_goodOnes.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_goodOnes[p]);
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

public Specification s_spec;
public DatabaseName s_db; // optional
public Specification s_goodOnes[]; // optional

} // DiagFormat_badSpec

//----------------------------------------------------------------
//EOF
