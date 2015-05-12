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
 * Class for representing a <code>ResourceControlRequest</code> from <code>IR</code>
 *
 * <pre>
 * ResourceControlRequest ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   suspendedFlag [39] IMPLICIT BOOLEAN OPTIONAL
 *   resourceReport [40] EXPLICIT ResourceReport OPTIONAL
 *   partialResultsAvailable [41] IMPLICIT INTEGER OPTIONAL
 *   responseRequired [42] IMPLICIT BOOLEAN
 *   triggeredRequestFlag [43] IMPLICIT BOOLEAN OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class ResourceControlRequest extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080314Z";

//----------------------------------------------------------------
/**
 * Default constructor for a ResourceControlRequest.
 */

public
ResourceControlRequest()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a ResourceControlRequest from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
ResourceControlRequest(BEREncoding ber, boolean check_tag)
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
  // ResourceControlRequest should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun ResourceControlRequest: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;
  BERConstructed tagged;

  // Decoding: referenceId ReferenceId OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun ResourceControlRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  try {
    s_referenceId = new ReferenceId(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_referenceId = null; // no, not present
  }

  // Decoding: suspendedFlag [39] IMPLICIT BOOLEAN OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun ResourceControlRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 39 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_suspendedFlag = new ASN1Boolean(p, false);
    part++;
  }

  // Decoding: resourceReport [40] EXPLICIT ResourceReport OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun ResourceControlRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 40 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      tagged = (BERConstructed) p;
    } catch (ClassCastException e) {
      throw new ASN1EncodingException
        ("Zebulun ResourceControlRequest: bad BER encoding: s_resourceReport tag bad\n");
    }
    if (tagged.number_components() != 1) {
      throw new ASN1EncodingException
        ("Zebulun ResourceControlRequest: bad BER encoding: s_resourceReport tag bad\n");
    }

    s_resourceReport = new ResourceReport(tagged.elementAt(0), true);
    part++;
  }

  // Decoding: partialResultsAvailable [41] IMPLICIT INTEGER OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun ResourceControlRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 41 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_partialResultsAvailable = new ASN1Integer(p, false);
    part++;
  }

  // Decoding: responseRequired [42] IMPLICIT BOOLEAN

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun ResourceControlRequest: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 42 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun ResourceControlRequest: bad tag in s_responseRequired\n");

  s_responseRequired = new ASN1Boolean(p, false);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_triggeredRequestFlag = null;

  // Decoding: triggeredRequestFlag [43] IMPLICIT BOOLEAN OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 43 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_triggeredRequestFlag = new ASN1Boolean(p, false);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun ResourceControlRequest: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the ResourceControlRequest.
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
 * Returns a BER encoding of ResourceControlRequest, implicitly tagged.
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
  if (s_referenceId != null)
    num_fields++;
  if (s_suspendedFlag != null)
    num_fields++;
  if (s_resourceReport != null)
    num_fields++;
  if (s_partialResultsAvailable != null)
    num_fields++;
  if (s_triggeredRequestFlag != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding enc[];

  // Encoding s_referenceId: ReferenceId OPTIONAL

  if (s_referenceId != null) {
    fields[x++] = s_referenceId.ber_encode();
  }

  // Encoding s_suspendedFlag: BOOLEAN OPTIONAL

  if (s_suspendedFlag != null) {
    fields[x++] = s_suspendedFlag.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 39);
  }

  // Encoding s_resourceReport: ResourceReport OPTIONAL

  if (s_resourceReport != null) {
    enc = new BEREncoding[1];
    enc[0] = s_resourceReport.ber_encode();
    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 40, enc);
  }

  // Encoding s_partialResultsAvailable: INTEGER OPTIONAL

  if (s_partialResultsAvailable != null) {
    fields[x++] = s_partialResultsAvailable.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 41);
  }

  // Encoding s_responseRequired: BOOLEAN 

  fields[x++] = s_responseRequired.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 42);

  // Encoding s_triggeredRequestFlag: BOOLEAN OPTIONAL

  if (s_triggeredRequestFlag != null) {
    fields[x++] = s_triggeredRequestFlag.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 43);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the ResourceControlRequest. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  if (s_referenceId != null) {
    str.append("referenceId ");
    str.append(s_referenceId);
    outputted++;
  }

  if (s_suspendedFlag != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("suspendedFlag ");
    str.append(s_suspendedFlag);
    outputted++;
  }

  if (s_resourceReport != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("resourceReport ");
    str.append(s_resourceReport);
    outputted++;
  }

  if (s_partialResultsAvailable != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("partialResultsAvailable ");
    str.append(s_partialResultsAvailable);
    outputted++;
  }

  if (0 < outputted)
    str.append(", ");
  str.append("responseRequired ");
  str.append(s_responseRequired);
  outputted++;

  if (s_triggeredRequestFlag != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("triggeredRequestFlag ");
    str.append(s_triggeredRequestFlag);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ReferenceId s_referenceId; // optional
public ASN1Boolean s_suspendedFlag; // optional
public ResourceReport s_resourceReport; // optional
public ASN1Integer s_partialResultsAvailable; // optional
public ASN1Boolean s_responseRequired;
public ASN1Boolean s_triggeredRequestFlag; // optional

//----------------------------------------------------------------
/*
 * Enumerated constants for class.
 */

// Enumerated constants for partialResultsAvailable
public static final int E_subset = 1;
public static final int E_interim = 2;
public static final int E_none = 3;

} // ResourceControlRequest

//----------------------------------------------------------------
//EOF
