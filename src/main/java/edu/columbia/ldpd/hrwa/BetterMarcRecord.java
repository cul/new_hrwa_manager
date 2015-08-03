package edu.columbia.ldpd.hrwa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

public class BetterMarcRecord {
	
	HashMap<String, ControlField> recordControlFields = new HashMap<String, ControlField>();
	HashMap<String, ArrayList<DataField>> recordDataFields = new HashMap<String, ArrayList<DataField>>();
	Leader leader = null;
	
	public BetterMarcRecord(Record marcRecord) {
		
		leader = marcRecord.getLeader();
		List<ControlField> controlFields = (List<ControlField>)(marcRecord.getControlFields()); //Control fields: 001-009
		List<DataField> dataFields = (List<DataField>)(marcRecord.getDataFields()); //Data fields: 010-999
		
		for(ControlField controlField : controlFields) {
			recordControlFields.put(controlField.getTag(), controlField);
		}
		
		for(DataField dataField : dataFields) {
			String tag = dataField.getTag();
			if( ! recordDataFields.containsKey(tag) ) {
				recordDataFields.put(tag, new ArrayList<DataField>());
			}
			recordDataFields.get(tag).add(dataField);
		}
		
	}
	
	public Leader getLeader() {
		return this.leader;
	}
	
	/**
	 * Returns control field value, or null if not present.
	 * @param field
	 * @return
	 */
	public ControlField getControlField(String field) {
		if(! recordControlFields.containsKey(field) ) { return null; }
		return recordControlFields.get(field);
	}
	
	
	
	/**
	 * Returns values if present, empty ArrayList otherwise.
	 * @param field
	 * @return
	 */
	public ArrayList<DataField> getDataFields(String field) { 
		if(recordDataFields.containsKey(field)) {
			return recordDataFields.get(field);
		} else {
			return new ArrayList<DataField>();
		}
	}
	
	/**
	 * @param dataField
	 * @param indicator1 - Pass null to skip indicator1 check
	 * @param indicator2 - Pass null to skip indicator2 check
	 * @param subfield
	 */
	public static ArrayList<String> getDataFieldValue(DataField dataField, Character indicator1, Character indicator2, Character subfieldChar) {
		
		ArrayList<String> arrayListToReturn = new ArrayList<String>();
		
		if(dataField.getSubfield(subfieldChar) == null) {
			return arrayListToReturn;
		}
		
		if(indicator1 != null && dataField.getIndicator1() != indicator1) {
			return arrayListToReturn;
		}
		
		if(indicator2 != null && dataField.getIndicator2() != indicator2) {
			return arrayListToReturn;
		}
		
		for(Subfield subfield : dataField.getSubfields(subfieldChar)) {
			arrayListToReturn.add(subfield.getData());
		}
		
		return arrayListToReturn;
	}
	
	/**
	 * @param dataField
	 * @param indicator1 - Pass null to skip indicator1 check
	 * @param indicator2 - Pass null to skip indicator2 check
	 * @return The combined string, or null if no values are found.
	 */
	public static String getDataFieldValueForAllSubfieldsInOrder(DataField dataField, Character indicator1, Character indicator2, String allowedSubfieldPattern, String subfieldSeparator) {
		
		if(indicator1 != null && dataField.getIndicator1() != indicator1) {
			return null;
		}
		
		if(indicator2 != null && dataField.getIndicator2() != indicator2) {
			return null;
		}
		
		Pattern p = null;
		Matcher m = null;
		if(allowedSubfieldPattern != null) {
			p = Pattern.compile(allowedSubfieldPattern);
		}
		
		ArrayList<String> values = new ArrayList<String>();
		for(Subfield subfield : dataField.getSubfields()) {
			m = p.matcher("" + subfield.getCode());
			if(m.matches()) {
				values.add(subfield.getData());
			}
		}
		
		if(values.size() > 0 ) {
			return StringUtils.join(values, subfieldSeparator).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
		}
		
		return null;
	}
	
	public static String removeCommonTrailingCharacters(String str) {
		return StringUtils.strip(str, " /.;:,");
	}
}
