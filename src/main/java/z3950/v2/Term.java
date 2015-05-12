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
 * Class for representing a <code>Term</code> from <code>IR</code>
 *
 * <pre>
 * Term ::=
 * [45] IMPLICIT OCTET STRING
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class Term extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080314Z";

//----------------------------------------------------------------
/**
 * Default constructor for a Term.
 */

public
Term()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a Term from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
Term(BEREncoding ber, boolean check_tag)
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
  // Check tag matches

  if (check_tag) {
    if (ber.tag_get() != 45 ||
        ber.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
      throw new ASN1EncodingException
        ("Zebulun: Term: bad BER: tag=" + ber.tag_get() + " expected 45\n");
  }

  value = new ASN1OctetString(ber, false);
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the Term.
 *
 * @exception	ASN1Exception Invalid or cannot be encoded.
 * @return	The BER encoding.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  return ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 45);
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of Term, implicitly tagged.
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
    return value.ber_encode(tag_type, tag);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the Term. 
 */

public String
toString()
{
  return value.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ASN1OctetString value;

} // Term

//----------------------------------------------------------------
//EOF
