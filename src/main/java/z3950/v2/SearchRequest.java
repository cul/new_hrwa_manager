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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:14:28 UTC
 */

//----------------------------------------------------------------

package z3950.v2;
import asn1.*;


//================================================================
/**
 * Class for representing a <code>SearchRequest</code> from <code>IR</code>
 *
 * <pre>
 * SearchRequest ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   smallSetUpperBound [13] IMPLICIT INTEGER
 *   largeSetLowerBound [14] IMPLICIT INTEGER
 *   mediumSetPresentNumber [15] IMPLICIT INTEGER
 *   replaceIndicator [16] IMPLICIT BOOLEAN
 *   resultSetName [17] IMPLICIT VisibleString
 *   databaseNames [18] IMPLICIT SEQUENCE OF DatabaseName
 *   smallSetElementSetNames [100] IMPLICIT ElementSetNames OPTIONAL
 *   mediumSetElementSetNames [101] IMPLICIT ElementSetNames OPTIONAL
 *   preferredRecordSyntax PreferredRecordSyntax OPTIONAL
 *   query [21] EXPLICIT Query
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class SearchRequest extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080314Z";

//----------------------------------------------------------------
/**
 * Default constructor for a SearchRequest.
 */

public
SearchRequest()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a SearchRequest from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
SearchRequest(BEREncoding ber, boolean check_tag)
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
  // SearchRequest should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun SearchRequest: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;
  BERConstructed tagged;

  // Decoding: referenceId ReferenceId OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  try {
    s_referenceId = new ReferenceId(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_referenceId = null; // no, not present
  }

  // Decoding: smallSetUpperBound [13] IMPLICIT INTEGER

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 13 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun SearchRequest: bad tag in s_smallSetUpperBound\n");

  s_smallSetUpperBound = new ASN1Integer(p, false);
  part++;

  // Decoding: largeSetLowerBound [14] IMPLICIT INTEGER

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 14 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun SearchRequest: bad tag in s_largeSetLowerBound\n");

  s_largeSetLowerBound = new ASN1Integer(p, false);
  part++;

  // Decoding: mediumSetPresentNumber [15] IMPLICIT INTEGER

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 15 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun SearchRequest: bad tag in s_mediumSetPresentNumber\n");

  s_mediumSetPresentNumber = new ASN1Integer(p, false);
  part++;

  // Decoding: replaceIndicator [16] IMPLICIT BOOLEAN

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 16 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun SearchRequest: bad tag in s_replaceIndicator\n");

  s_replaceIndicator = new ASN1Boolean(p, false);
  part++;

  // Decoding: resultSetName [17] IMPLICIT VisibleString

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 17 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun SearchRequest: bad tag in s_resultSetName\n");

  s_resultSetName = new ASN1VisibleString(p, false);
  part++;

  // Decoding: databaseNames [18] IMPLICIT SEQUENCE OF DatabaseName

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 18 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun SearchRequest: bad tag in s_databaseNames\n");

  try {
    BERConstructed cons = (BERConstructed) p;
    int parts = cons.number_components();
    s_databaseNames = new DatabaseName[parts];
    int n;
    for (n = 0; n < parts; n++) {
      s_databaseNames[n] = new DatabaseName(cons.elementAt(n), true);
    }
  } catch (ClassCastException e) {
    throw new ASN1EncodingException("Bad BER");
  }
  part++;

  // Decoding: smallSetElementSetNames [100] IMPLICIT ElementSetNames OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 100 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_smallSetElementSetNames = new ElementSetNames(p, false);
    part++;
  }

  // Decoding: mediumSetElementSetNames [101] IMPLICIT ElementSetNames OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 101 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_mediumSetElementSetNames = new ElementSetNames(p, false);
    part++;
  }

  // Decoding: preferredRecordSyntax PreferredRecordSyntax OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  try {
    s_preferredRecordSyntax = new PreferredRecordSyntax(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_preferredRecordSyntax = null; // no, not present
  }

  // Decoding: query [21] EXPLICIT Query

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun SearchRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 21 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun SearchRequest: bad tag in s_query\n");

  try {
    tagged = (BERConstructed) p;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun SearchRequest: bad BER encoding: s_query tag bad\n");
  }
  if (tagged.number_components() != 1) {
    throw new ASN1EncodingException
      ("Zebulun SearchRequest: bad BER encoding: s_query tag bad\n");
  }

  s_query = new Query(tagged.elementAt(0), true);
  part++;

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun SearchRequest: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the SearchRequest.
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
 * Returns a BER encoding of SearchRequest, implicitly tagged.
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

  int num_fields = 7; // number of mandatories
  if (s_referenceId != null)
    num_fields++;
  if (s_smallSetElementSetNames != null)
    num_fields++;
  if (s_mediumSetElementSetNames != null)
    num_fields++;
  if (s_preferredRecordSyntax != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding f2[];
  int p;
  BEREncoding enc[];

  // Encoding s_referenceId: ReferenceId OPTIONAL

  if (s_referenceId != null) {
    fields[x++] = s_referenceId.ber_encode();
  }

  // Encoding s_smallSetUpperBound: INTEGER 

  fields[x++] = s_smallSetUpperBound.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 13);

  // Encoding s_largeSetLowerBound: INTEGER 

  fields[x++] = s_largeSetLowerBound.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 14);

  // Encoding s_mediumSetPresentNumber: INTEGER 

  fields[x++] = s_mediumSetPresentNumber.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 15);

  // Encoding s_replaceIndicator: BOOLEAN 

  fields[x++] = s_replaceIndicator.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 16);

  // Encoding s_resultSetName: VisibleString 

  fields[x++] = s_resultSetName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 17);

  // Encoding s_databaseNames: SEQUENCE OF 

    f2 = new BEREncoding[s_databaseNames.length];

    for (p = 0; p < s_databaseNames.length; p++) {
      f2[p] = s_databaseNames[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 18, f2);

  // Encoding s_smallSetElementSetNames: ElementSetNames OPTIONAL

  if (s_smallSetElementSetNames != null) {
    fields[x++] = s_smallSetElementSetNames.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 100);
  }

  // Encoding s_mediumSetElementSetNames: ElementSetNames OPTIONAL

  if (s_mediumSetElementSetNames != null) {
    fields[x++] = s_mediumSetElementSetNames.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 101);
  }

  // Encoding s_preferredRecordSyntax: PreferredRecordSyntax OPTIONAL

  if (s_preferredRecordSyntax != null) {
    fields[x++] = s_preferredRecordSyntax.ber_encode();
  }

  // Encoding s_query: Query 

  enc = new BEREncoding[1];
  enc[0] = s_query.ber_encode();
  fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 21, enc);

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the SearchRequest. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  if (s_referenceId != null) {
    str.append("referenceId ");
    str.append(s_referenceId);
    outputted++;
  }

  if (0 < outputted)
    str.append(", ");
  str.append("smallSetUpperBound ");
  str.append(s_smallSetUpperBound);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("largeSetLowerBound ");
  str.append(s_largeSetLowerBound);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("mediumSetPresentNumber ");
  str.append(s_mediumSetPresentNumber);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("replaceIndicator ");
  str.append(s_replaceIndicator);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("resultSetName ");
  str.append(s_resultSetName);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("databaseNames ");
  str.append("{");
  for (p = 0; p < s_databaseNames.length; p++) {
    if (p != 0)
      str.append(", ");
    str.append(s_databaseNames[p]);
  }
  str.append("}");
  outputted++;

  if (s_smallSetElementSetNames != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("smallSetElementSetNames ");
    str.append(s_smallSetElementSetNames);
    outputted++;
  }

  if (s_mediumSetElementSetNames != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("mediumSetElementSetNames ");
    str.append(s_mediumSetElementSetNames);
    outputted++;
  }

  if (s_preferredRecordSyntax != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("preferredRecordSyntax ");
    str.append(s_preferredRecordSyntax);
    outputted++;
  }

  if (0 < outputted)
    str.append(", ");
  str.append("query ");
  str.append(s_query);
  outputted++;

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ReferenceId s_referenceId; // optional
public ASN1Integer s_smallSetUpperBound;
public ASN1Integer s_largeSetLowerBound;
public ASN1Integer s_mediumSetPresentNumber;
public ASN1Boolean s_replaceIndicator;
public ASN1VisibleString s_resultSetName;
public DatabaseName s_databaseNames[];
public ElementSetNames s_smallSetElementSetNames; // optional
public ElementSetNames s_mediumSetElementSetNames; // optional
public PreferredRecordSyntax s_preferredRecordSyntax; // optional
public Query s_query;

} // SearchRequest

//----------------------------------------------------------------
//EOF
