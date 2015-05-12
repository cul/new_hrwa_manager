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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:14:27 UTC
 */

//----------------------------------------------------------------

package z3950.v2;
import asn1.*;


//================================================================
/**
 * Class for representing a <code>InitializeResponse</code> from <code>IR</code>
 *
 * <pre>
 * InitializeResponse ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   protocolVersion ProtocolVersion
 *   options Options
 *   preferredMessageSize PreferredMessageSize
 *   maximumRecordSize MaximumRecordSize
 *   result [12] IMPLICIT BOOLEAN
 *   implementationId ImplementationId OPTIONAL
 *   implementationName ImplementationName OPTIONAL
 *   implementationVersion ImplementationVersion OPTIONAL
 *   userInformationField UserInformationField OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class InitializeResponse extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080314Z";

//----------------------------------------------------------------
/**
 * Default constructor for a InitializeResponse.
 */

public
InitializeResponse()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a InitializeResponse from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
InitializeResponse(BEREncoding ber, boolean check_tag)
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
  // InitializeResponse should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun InitializeResponse: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Decoding: referenceId ReferenceId OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
  }
  p = ber_cons.elementAt(part);

  try {
    s_referenceId = new ReferenceId(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_referenceId = null; // no, not present
  }

  // Decoding: protocolVersion ProtocolVersion

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
  }
  p = ber_cons.elementAt(part);

  s_protocolVersion = new ProtocolVersion(p, true);
  part++;

  // Decoding: options Options

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
  }
  p = ber_cons.elementAt(part);

  s_options = new Options(p, true);
  part++;

  // Decoding: preferredMessageSize PreferredMessageSize

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
  }
  p = ber_cons.elementAt(part);

  s_preferredMessageSize = new PreferredMessageSize(p, true);
  part++;

  // Decoding: maximumRecordSize MaximumRecordSize

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
  }
  p = ber_cons.elementAt(part);

  s_maximumRecordSize = new MaximumRecordSize(p, true);
  part++;

  // Decoding: result [12] IMPLICIT BOOLEAN

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 12 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun InitializeResponse: bad tag in s_result\n");

  s_result = new ASN1Boolean(p, false);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_implementationId = null;
  s_implementationName = null;
  s_implementationVersion = null;
  s_userInformationField = null;

  // Decoding: implementationId ImplementationId OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  try {
    s_implementationId = new ImplementationId(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_implementationId = null; // no, not present
  }

  // Decoding: implementationName ImplementationName OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  try {
    s_implementationName = new ImplementationName(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_implementationName = null; // no, not present
  }

  // Decoding: implementationVersion ImplementationVersion OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  try {
    s_implementationVersion = new ImplementationVersion(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_implementationVersion = null; // no, not present
  }

  // Decoding: userInformationField UserInformationField OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  try {
    s_userInformationField = new UserInformationField(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_userInformationField = null; // no, not present
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun InitializeResponse: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the InitializeResponse.
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
 * Returns a BER encoding of InitializeResponse, implicitly tagged.
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

  int num_fields = 5; // number of mandatories
  if (s_referenceId != null)
    num_fields++;
  if (s_implementationId != null)
    num_fields++;
  if (s_implementationName != null)
    num_fields++;
  if (s_implementationVersion != null)
    num_fields++;
  if (s_userInformationField != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;

  // Encoding s_referenceId: ReferenceId OPTIONAL

  if (s_referenceId != null) {
    fields[x++] = s_referenceId.ber_encode();
  }

  // Encoding s_protocolVersion: ProtocolVersion 

  fields[x++] = s_protocolVersion.ber_encode();

  // Encoding s_options: Options 

  fields[x++] = s_options.ber_encode();

  // Encoding s_preferredMessageSize: PreferredMessageSize 

  fields[x++] = s_preferredMessageSize.ber_encode();

  // Encoding s_maximumRecordSize: MaximumRecordSize 

  fields[x++] = s_maximumRecordSize.ber_encode();

  // Encoding s_result: BOOLEAN 

  fields[x++] = s_result.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 12);

  // Encoding s_implementationId: ImplementationId OPTIONAL

  if (s_implementationId != null) {
    fields[x++] = s_implementationId.ber_encode();
  }

  // Encoding s_implementationName: ImplementationName OPTIONAL

  if (s_implementationName != null) {
    fields[x++] = s_implementationName.ber_encode();
  }

  // Encoding s_implementationVersion: ImplementationVersion OPTIONAL

  if (s_implementationVersion != null) {
    fields[x++] = s_implementationVersion.ber_encode();
  }

  // Encoding s_userInformationField: UserInformationField OPTIONAL

  if (s_userInformationField != null) {
    fields[x++] = s_userInformationField.ber_encode();
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the InitializeResponse. 
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

  if (0 < outputted)
    str.append(", ");
  str.append("protocolVersion ");
  str.append(s_protocolVersion);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("options ");
  str.append(s_options);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("preferredMessageSize ");
  str.append(s_preferredMessageSize);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("maximumRecordSize ");
  str.append(s_maximumRecordSize);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("result ");
  str.append(s_result);
  outputted++;

  if (s_implementationId != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("implementationId ");
    str.append(s_implementationId);
    outputted++;
  }

  if (s_implementationName != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("implementationName ");
    str.append(s_implementationName);
    outputted++;
  }

  if (s_implementationVersion != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("implementationVersion ");
    str.append(s_implementationVersion);
    outputted++;
  }

  if (s_userInformationField != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("userInformationField ");
    str.append(s_userInformationField);
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
public ProtocolVersion s_protocolVersion;
public Options s_options;
public PreferredMessageSize s_preferredMessageSize;
public MaximumRecordSize s_maximumRecordSize;
public ASN1Boolean s_result;
public ImplementationId s_implementationId; // optional
public ImplementationName s_implementationName; // optional
public ImplementationVersion s_implementationVersion; // optional
public UserInformationField s_userInformationField; // optional

} // InitializeResponse

//----------------------------------------------------------------
//EOF
