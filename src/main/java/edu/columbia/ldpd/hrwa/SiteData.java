package edu.columbia.ldpd.hrwa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import com.opencsv.CSVReader;

import edu.columbia.ldpd.hrwa.util.MetadataUtils;

public class SiteData {
	
	private static ConcurrentHashMap<String, String> geographicAreasToFullNames = getGeographicAreasToFullNamesMap();
	private static ConcurrentHashMap<String, String> countryCodesToFullNamesMap = getCountryCodesToFullNamesMap();
	private static ConcurrentHashMap<String, String> languageCodesToFullNamesMap = getLanguageCodesToFullNamesMap();
	private static ConcurrentHashMap<String, ArrayList<String>> hostStringsToRelatedHostStrings = getHostStringsToRelatedHostStrings();
	
	public ArrayList<String> hostStrings = new ArrayList<String>();
	public HashSet<String> relatedHostStrings = new HashSet<String>();
	
	public ArrayList<String> originalUrl = new ArrayList<String>();
	public ArrayList<String> archivedUrl = new ArrayList<String>();
	public String organizationType = null;
	public ArrayList<String> subject = new ArrayList<String>();
	public ArrayList<String> geographicFocus = new ArrayList<String>();
	public String organizationBasedIn = null;
	public ArrayList<String> language = new ArrayList<String>();
	public String title = null;
	public ArrayList<String> alternativeTitle = new ArrayList<String>();
	public ArrayList<String> creatorName = new ArrayList<String>();
	public String summary = null;
	public String bibId = null;
	
	public ArrayList<String> validationErrors = new ArrayList<String>(); //Populated with validation error strings when the isValid() method is called 
	
	public SiteData() {
		
	}
	
	public SiteData(InputStream marcXmlInputStream) {
		
		//Get MARC record bibliographic data
		ArrayList<DataField> fields = null;
		MarcXmlReader reader = new MarcXmlReader(marcXmlInputStream);
		if (reader.hasNext()) {
			Record bibRecord = reader.next();
			BetterMarcRecord betterMarcRecord = new BetterMarcRecord(bibRecord);
			
			//bibId --- 001
			this.bibId = betterMarcRecord.getControlField("001").getData().trim();
			
			//originalUrl --- 920 40 $u
			fields = betterMarcRecord.getDataFields("920");
			for(DataField field : fields) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters(
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, '4', '0', 'u'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				if( ! result.isEmpty() ) {
					this.originalUrl.add(result);
					String resultAsHostString = MetadataUtils.extractHostString(result);
					this.hostStrings.add(resultAsHostString);
					
					//Set relatedHosts based on hostString
					if(SiteData.hostStringsToRelatedHostStrings.containsKey(resultAsHostString)) {
						for(String relatedHostString : hostStringsToRelatedHostStrings.get(resultAsHostString)) {
							this.relatedHostStrings.add(relatedHostString); 
						}
					}
				}
			}
			
			//archivedUrl --- 920 41 $u
			fields = betterMarcRecord.getDataFields("920");
			for(DataField field : fields) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters(
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, '4', '1', 'u'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				if( ! result.isEmpty() ) {
					this.archivedUrl.add(result);
				}
			}
			
			//organizationType --- 653 #0 
			fields = betterMarcRecord.getDataFields("653");
			for(DataField field : fields) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters(
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, '0', 'a'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				if( ! result.isEmpty() ) {
					this.organizationType = result;
					break;
				}
			}
			
			//subject --- 650 $a and $x 
			fields = betterMarcRecord.getDataFields("650");
			for(DataField field : fields) {
				String subfieldA = StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim();
				String subfieldX = StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'x'), ", ").trim();
				
				String result = "";
				
				if(subfieldX.isEmpty() && subfieldA.equals("Human rights")) {
					//If $x is blank and $a equals "Human rights", use value "Human rights (General)"
					result = "Human rights (General)";
				} else if(subfieldA.equals(subfieldX)) {
					//If $a and $x are the same, just use $a
					result = BetterMarcRecord.removeCommonTrailingCharacters(subfieldA).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space;
				} else {
					//Otherwise combine $a and $x
					result = BetterMarcRecord.removeCommonTrailingCharacters(subfieldA + " " + subfieldX).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				}
				
				if( ! result.isEmpty() ) {
					this.subject.add(result);
				}
			}
			
			//geographicFocus --- 043, but if 043 blank then check for value of "Global focus" in 965 field instead 
			fields = betterMarcRecord.getDataFields("043");
			for(DataField field : fields) {
				String result = StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim();
				result = StringUtils.strip(result, "-"); //Also remove trailing dashes
				if( ! result.isEmpty() ) {
					this.geographicFocus.add(geographicAreasToFullNames.get(result));
				}
			}
			if(this.geographicFocus.size() == 0) {
				fields = betterMarcRecord.getDataFields("965");
				for(DataField field : fields) {
					String result = StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim();
					if( result.equals("Global focus") ) {
						this.geographicFocus.add("Global focus");
					}
				}
			}
			
			//organizationBasedIn --- 008 field, bytes 15-17
			String organizationBasedInCode = betterMarcRecord.getControlField("008").getData().substring(15, 18).trim();
			if(organizationBasedInCode.startsWith("xx")) {
				this.organizationBasedIn = "undetermined";
			} else {
				this.organizationBasedIn = countryCodesToFullNamesMap.get(organizationBasedInCode);
			}
			
			//language --- 041, but if 041 blank then check for value in 008, bytes 35-37 
			fields = betterMarcRecord.getDataFields("041");
			for(DataField field : fields) {
				for(String val : BetterMarcRecord.getDataFieldValue(field, null, null, 'a')) {
					val = val.trim();
					if( ! val.isEmpty() ) {
						this.language.add(languageCodesToFullNamesMap.get(val));
					}
				}
			}
			if(this.language.size() == 0) {
				String languageCode = betterMarcRecord.getControlField("008").getData().substring(35, 38).trim();
				if( ! languageCode.isEmpty() ) {
					this.language.add(languageCodesToFullNamesMap.get(languageCode));
				}
			}
			
			//title --- 245 $a, and $b if present
			fields = betterMarcRecord.getDataFields("245");
			for(DataField field : fields) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters(
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim() + " " +
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				if( ! result.isEmpty() ) {
					this.title = result;
					break;
				}
			}
			
			//alternativeTitle --- 246 $a
			fields = betterMarcRecord.getDataFields("246");
			for(DataField field : fields) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters(
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				if( ! result.isEmpty() ) {
					this.alternativeTitle.add(result);
				}
			}
			
			//creatorName --- 110 $a, and $b if present --- 710 $a, and $b if present --- 100 $a, and $c if present 
			fields = betterMarcRecord.getDataFields("110");
			for(DataField field : fields) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters(
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim() + " " +
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				if( ! result.isEmpty() ) {
					this.creatorName.add(result);
				}
			}
			fields = betterMarcRecord.getDataFields("710");
			for(DataField field : fields) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters(
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim() + " " +
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'b'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				if( ! result.isEmpty() ) {
					this.creatorName.add(result);
				}
			}
			fields = betterMarcRecord.getDataFields("100");
			for(DataField field : fields) {
				String result = BetterMarcRecord.removeCommonTrailingCharacters(
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim() + " " +
					StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'c'), ", ").trim()
				).replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				if( ! result.isEmpty() ) {
					this.creatorName.add(result);
				}
			}
			
			//summary --- 520 $a
			fields = betterMarcRecord.getDataFields("520");
			for(DataField field : fields) {
				String result = StringUtils.join(BetterMarcRecord.getDataFieldValue(field, null, null, 'a'), ", ").trim().replaceAll(" +", " "); //replaceAll with regex to convert multiple spaces into a single space
				if( ! result.isEmpty() ) {
					this.summary = result;
					break;
				}
			}
			
		}
	}
	
	/**
	 * Perform validation and return true if valid.
	 */
	public boolean isValid() {
		
		if(bibId == null) { validationErrors.add("Missing bibId."); }
		if(originalUrl.size() == 0) { validationErrors.add("Missing originalUrl."); }
		if(archivedUrl.size() == 0) { validationErrors.add("Missing archivedUrl."); }
		if(hostStrings.size() == 0) { validationErrors.add("Missing hostString (derived from originalUrl)."); }
		if(organizationType == null) { validationErrors.add("Missing organizationType."); }
		if(subject.size() == 0) { validationErrors.add("Missing subject."); }
		if(language.size() == 0) { validationErrors.add("Missing language."); }
		if(title == null) { validationErrors.add("Missing title."); }
		if(creatorName.size() == 0) { validationErrors.add("Missing creatorName."); }
		
		return validationErrors.size() == 0;
	}
	
	public ArrayList<String> getValidationErrors() {
		return this.validationErrors;
	}
	
	public void sendToSolr() {
//		String urlString = "http://localhost:8983/solr/techproducts";
//		SolrClient solr = new HttpSolrClient(urlString);
//		
//		SolrInputDocument document = new SolrInputDocument();
//		document.addField("id", "552199");
//		document.addField("name", "Gouda cheese wheel");
//		document.addField("price", "49.99");
//		UpdateResponse response = solr.add(document);
//		 
//		// Commit changes
//		solr.commit();
	}
	
	public static ConcurrentHashMap<String,String> getLanguageCodesToFullNamesMap() {
		ConcurrentHashMap<String,String> languageCodesToFullNames = new ConcurrentHashMap<String,String>();
		
		languageCodesToFullNames.put("aar", "Afar");
		languageCodesToFullNames.put("abk", "Abkhaz");
		languageCodesToFullNames.put("ace", "Achinese");
		languageCodesToFullNames.put("ach", "Acoli");
		languageCodesToFullNames.put("ada", "Adangme");
		languageCodesToFullNames.put("ady", "Adygei");
		languageCodesToFullNames.put("afa", "Afroasiatic (Other)");
		languageCodesToFullNames.put("afh", "Afrihili (Artificial language)");
		languageCodesToFullNames.put("afr", "Afrikaans");
		languageCodesToFullNames.put("ain", "Ainu");
		languageCodesToFullNames.put("-ajm", "Aljamía");
		languageCodesToFullNames.put("aka", "Akan");
		languageCodesToFullNames.put("akk", "Akkadian");
		languageCodesToFullNames.put("alb", "Albanian");
		languageCodesToFullNames.put("ale", "Aleut");
		languageCodesToFullNames.put("alg", "Algonquian (Other)");
		languageCodesToFullNames.put("alt", "Altai");
		languageCodesToFullNames.put("amh", "Amharic");
		languageCodesToFullNames.put("ang", "English, Old (ca. 450-1100)");
		languageCodesToFullNames.put("anp", "Angika");
		languageCodesToFullNames.put("apa", "Apache languages");
		languageCodesToFullNames.put("ara", "Arabic");
		languageCodesToFullNames.put("arc", "Aramaic");
		languageCodesToFullNames.put("arg", "Aragonese");
		languageCodesToFullNames.put("arm", "Armenian");
		languageCodesToFullNames.put("arn", "Mapuche");
		languageCodesToFullNames.put("arp", "Arapaho");
		languageCodesToFullNames.put("art", "Artificial (Other)");
		languageCodesToFullNames.put("arw", "Arawak");
		languageCodesToFullNames.put("asm", "Assamese");
		languageCodesToFullNames.put("ast", "Bable");
		languageCodesToFullNames.put("ath", "Athapascan (Other)");
		languageCodesToFullNames.put("aus", "Australian languages");
		languageCodesToFullNames.put("ava", "Avaric");
		languageCodesToFullNames.put("ave", "Avestan");
		languageCodesToFullNames.put("awa", "Awadhi");
		languageCodesToFullNames.put("aym", "Aymara");
		languageCodesToFullNames.put("aze", "Azerbaijani");
		languageCodesToFullNames.put("bad", "Banda languages");
		languageCodesToFullNames.put("bai", "Bamileke languages");
		languageCodesToFullNames.put("bak", "Bashkir");
		languageCodesToFullNames.put("bal", "Baluchi");
		languageCodesToFullNames.put("bam", "Bambara");
		languageCodesToFullNames.put("ban", "Balinese");
		languageCodesToFullNames.put("baq", "Basque");
		languageCodesToFullNames.put("bas", "Basa");
		languageCodesToFullNames.put("bat", "Baltic (Other)");
		languageCodesToFullNames.put("bej", "Beja");
		languageCodesToFullNames.put("bel", "Belarusian");
		languageCodesToFullNames.put("bem", "Bemba");
		languageCodesToFullNames.put("ben", "Bengali");
		languageCodesToFullNames.put("ber", "Berber (Other)");
		languageCodesToFullNames.put("bho", "Bhojpuri");
		languageCodesToFullNames.put("bih", "Bihari (Other)");
		languageCodesToFullNames.put("bik", "Bikol");
		languageCodesToFullNames.put("bin", "Edo");
		languageCodesToFullNames.put("bis", "Bislama");
		languageCodesToFullNames.put("bla", "Siksika");
		languageCodesToFullNames.put("bnt", "Bantu (Other)");
		languageCodesToFullNames.put("bos", "Bosnian");
		languageCodesToFullNames.put("bra", "Braj");
		languageCodesToFullNames.put("bre", "Breton");
		languageCodesToFullNames.put("btk", "Batak");
		languageCodesToFullNames.put("bua", "Buriat");
		languageCodesToFullNames.put("bug", "Bugis");
		languageCodesToFullNames.put("bul", "Bulgarian");
		languageCodesToFullNames.put("bur", "Burmese");
		languageCodesToFullNames.put("byn", "Bilin");
		languageCodesToFullNames.put("cad", "Caddo");
		languageCodesToFullNames.put("cai", "Central American Indian (Other)");
		languageCodesToFullNames.put("-cam", "Khmer");
		languageCodesToFullNames.put("car", "Carib");
		languageCodesToFullNames.put("cat", "Catalan");
		languageCodesToFullNames.put("cau", "Caucasian (Other)");
		languageCodesToFullNames.put("ceb", "Cebuano");
		languageCodesToFullNames.put("cel", "Celtic (Other)");
		languageCodesToFullNames.put("cha", "Chamorro");
		languageCodesToFullNames.put("chb", "Chibcha");
		languageCodesToFullNames.put("che", "Chechen");
		languageCodesToFullNames.put("chg", "Chagatai");
		languageCodesToFullNames.put("chi", "Chinese");
		languageCodesToFullNames.put("chk", "Chuukese");
		languageCodesToFullNames.put("chm", "Mari");
		languageCodesToFullNames.put("chn", "Chinook jargon");
		languageCodesToFullNames.put("cho", "Choctaw");
		languageCodesToFullNames.put("chp", "Chipewyan");
		languageCodesToFullNames.put("chr", "Cherokee");
		languageCodesToFullNames.put("chu", "Church Slavic");
		languageCodesToFullNames.put("chv", "Chuvash");
		languageCodesToFullNames.put("chy", "Cheyenne");
		languageCodesToFullNames.put("cmc", "Chamic languages");
		languageCodesToFullNames.put("cop", "Coptic");
		languageCodesToFullNames.put("cor", "Cornish");
		languageCodesToFullNames.put("cos", "Corsican");
		languageCodesToFullNames.put("cpe", "Creoles and Pidgins, English-based (Other)");
		languageCodesToFullNames.put("cpf", "Creoles and Pidgins, French-based (Other)");
		languageCodesToFullNames.put("cpp", "Creoles and Pidgins, Portuguese-based (Other)");
		languageCodesToFullNames.put("cre", "Cree");
		languageCodesToFullNames.put("crh", "Crimean Tatar");
		languageCodesToFullNames.put("crp", "Creoles and Pidgins (Other)");
		languageCodesToFullNames.put("csb", "Kashubian");
		languageCodesToFullNames.put("cus", "Cushitic (Other)");
		languageCodesToFullNames.put("cze", "Czech");
		languageCodesToFullNames.put("dak", "Dakota");
		languageCodesToFullNames.put("dan", "Danish");
		languageCodesToFullNames.put("dar", "Dargwa");
		languageCodesToFullNames.put("day", "Dayak");
		languageCodesToFullNames.put("del", "Delaware");
		languageCodesToFullNames.put("den", "Slavey");
		languageCodesToFullNames.put("dgr", "Dogrib");
		languageCodesToFullNames.put("din", "Dinka");
		languageCodesToFullNames.put("div", "Divehi");
		languageCodesToFullNames.put("doi", "Dogri");
		languageCodesToFullNames.put("dra", "Dravidian (Other)");
		languageCodesToFullNames.put("dsb", "Lower Sorbian");
		languageCodesToFullNames.put("dua", "Duala");
		languageCodesToFullNames.put("dum", "Dutch, Middle (ca. 1050-1350)");
		languageCodesToFullNames.put("dut", "Dutch");
		languageCodesToFullNames.put("dyu", "Dyula");
		languageCodesToFullNames.put("dzo", "Dzongkha");
		languageCodesToFullNames.put("efi", "Efik");
		languageCodesToFullNames.put("egy", "Egyptian");
		languageCodesToFullNames.put("eka", "Ekajuk");
		languageCodesToFullNames.put("elx", "Elamite");
		languageCodesToFullNames.put("eng", "English");
		languageCodesToFullNames.put("enm", "English, Middle (1100-1500)");
		languageCodesToFullNames.put("epo", "Esperanto");
		languageCodesToFullNames.put("-esk", "Eskimo languages");
		languageCodesToFullNames.put("-esp", "Esperanto");
		languageCodesToFullNames.put("est", "Estonian");
		languageCodesToFullNames.put("-eth", "Ethiopic");
		languageCodesToFullNames.put("ewe", "Ewe");
		languageCodesToFullNames.put("ewo", "Ewondo");
		languageCodesToFullNames.put("fan", "Fang");
		languageCodesToFullNames.put("fao", "Faroese");
		languageCodesToFullNames.put("-far", "Faroese");
		languageCodesToFullNames.put("fat", "Fanti");
		languageCodesToFullNames.put("fij", "Fijian");
		languageCodesToFullNames.put("fil", "Filipino");
		languageCodesToFullNames.put("fin", "Finnish");
		languageCodesToFullNames.put("fiu", "Finno-Ugrian (Other)");
		languageCodesToFullNames.put("fon", "Fon");
		languageCodesToFullNames.put("fre", "French");
		languageCodesToFullNames.put("-fri", "Frisian");
		languageCodesToFullNames.put("frm", "French, Middle (ca. 1300-1600)");
		languageCodesToFullNames.put("fro", "French, Old (ca. 842-1300)");
		languageCodesToFullNames.put("frr", "North Frisian");
		languageCodesToFullNames.put("frs", "East Frisian");
		languageCodesToFullNames.put("fry", "Frisian");
		languageCodesToFullNames.put("ful", "Fula");
		languageCodesToFullNames.put("fur", "Friulian");
		languageCodesToFullNames.put("gaa", "Gã");
		languageCodesToFullNames.put("-gae", "Scottish Gaelix");
		languageCodesToFullNames.put("-gag", "Galician");
		languageCodesToFullNames.put("-gal", "Oromo");
		languageCodesToFullNames.put("gay", "Gayo");
		languageCodesToFullNames.put("gba", "Gbaya");
		languageCodesToFullNames.put("gem", "Germanic (Other)");
		languageCodesToFullNames.put("geo", "Georgian");
		languageCodesToFullNames.put("ger", "German");
		languageCodesToFullNames.put("gez", "Ethiopic");
		languageCodesToFullNames.put("gil", "Gilbertese");
		languageCodesToFullNames.put("gla", "Scottish Gaelic");
		languageCodesToFullNames.put("gle", "Irish");
		languageCodesToFullNames.put("glg", "Galician");
		languageCodesToFullNames.put("glv", "Manx");
		languageCodesToFullNames.put("gmh", "German, Middle High (ca. 1050-1500)");
		languageCodesToFullNames.put("goh", "German, Old High (ca. 750-1050)");
		languageCodesToFullNames.put("gon", "Gondi");
		languageCodesToFullNames.put("gor", "Gorontalo");
		languageCodesToFullNames.put("got", "Gothic");
		languageCodesToFullNames.put("grb", "Grebo");
		languageCodesToFullNames.put("grc", "Greek, Ancient (to 1453)");
		languageCodesToFullNames.put("gre", "Greek, Modern (1453-)");
		languageCodesToFullNames.put("grn", "Guarani");
		languageCodesToFullNames.put("gsw", "Swiss German");
		languageCodesToFullNames.put("-gua", "Guarani");
		languageCodesToFullNames.put("guj", "Gujarati");
		languageCodesToFullNames.put("gwi", "Gwich'in");
		languageCodesToFullNames.put("hai", "Haida");
		languageCodesToFullNames.put("hat", "Haitian French Creole");
		languageCodesToFullNames.put("hau", "Hausa");
		languageCodesToFullNames.put("haw", "Hawaiian");
		languageCodesToFullNames.put("heb", "Hebrew");
		languageCodesToFullNames.put("her", "Herero");
		languageCodesToFullNames.put("hil", "Hiligaynon");
		languageCodesToFullNames.put("him", "Western Pahari languages");
		languageCodesToFullNames.put("hin", "Hindi");
		languageCodesToFullNames.put("hit", "Hittite");
		languageCodesToFullNames.put("hmn", "Hmong");
		languageCodesToFullNames.put("hmo", "Hiri Motu");
		languageCodesToFullNames.put("hrv", "Croatian");
		languageCodesToFullNames.put("hsb", "Upper Sorbian");
		languageCodesToFullNames.put("hun", "Hungarian");
		languageCodesToFullNames.put("hup", "Hupa");
		languageCodesToFullNames.put("iba", "Iban");
		languageCodesToFullNames.put("ibo", "Igbo");
		languageCodesToFullNames.put("ice", "Icelandic");
		languageCodesToFullNames.put("ido", "Ido");
		languageCodesToFullNames.put("iii", "Sichuan Yi");
		languageCodesToFullNames.put("ijo", "Ijo");
		languageCodesToFullNames.put("iku", "Inuktitut");
		languageCodesToFullNames.put("ile", "Interlingue");
		languageCodesToFullNames.put("ilo", "Iloko");
		languageCodesToFullNames.put("ina", "Interlingua (International Auxiliary Language Association)");
		languageCodesToFullNames.put("inc", "Indic (Other)");
		languageCodesToFullNames.put("ind", "Indonesian");
		languageCodesToFullNames.put("ine", "Indo-European (Other)");
		languageCodesToFullNames.put("inh", "Ingush");
		languageCodesToFullNames.put("-int", "Interlingua (International Auxiliary Language Association)");
		languageCodesToFullNames.put("ipk", "Inupiaq");
		languageCodesToFullNames.put("ira", "Iranian (Other)");
		languageCodesToFullNames.put("-iri", "Irish");
		languageCodesToFullNames.put("iro", "Iroquoian (Other)");
		languageCodesToFullNames.put("ita", "Italian");
		languageCodesToFullNames.put("jav", "Javanese");
		languageCodesToFullNames.put("jbo", "Lojban (Artificial language)");
		languageCodesToFullNames.put("jpn", "Japanese");
		languageCodesToFullNames.put("jpr", "Judeo-Persian");
		languageCodesToFullNames.put("jrb", "Judeo-Arabic");
		languageCodesToFullNames.put("kaa", "Kara-Kalpak");
		languageCodesToFullNames.put("kab", "Kabyle");
		languageCodesToFullNames.put("kac", "Kachin");
		languageCodesToFullNames.put("kal", "Kalâtdlisut");
		languageCodesToFullNames.put("kam", "Kamba");
		languageCodesToFullNames.put("kan", "Kannada");
		languageCodesToFullNames.put("kar", "Karen languages");
		languageCodesToFullNames.put("kas", "Kashmiri");
		languageCodesToFullNames.put("kau", "Kanuri");
		languageCodesToFullNames.put("kaw", "Kawi");
		languageCodesToFullNames.put("kaz", "Kazakh");
		languageCodesToFullNames.put("kbd", "Kabardian");
		languageCodesToFullNames.put("kha", "Khasi");
		languageCodesToFullNames.put("khi", "Khoisan (Other)");
		languageCodesToFullNames.put("khm", "Khmer");
		languageCodesToFullNames.put("kho", "Khotanese");
		languageCodesToFullNames.put("kik", "Kikuyu");
		languageCodesToFullNames.put("kin", "Kinyarwanda");
		languageCodesToFullNames.put("kir", "Kyrgyz");
		languageCodesToFullNames.put("kmb", "Kimbundu");
		languageCodesToFullNames.put("kok", "Konkani");
		languageCodesToFullNames.put("kom", "Komi");
		languageCodesToFullNames.put("kon", "Kongo");
		languageCodesToFullNames.put("kor", "Korean");
		languageCodesToFullNames.put("kos", "Kosraean");
		languageCodesToFullNames.put("kpe", "Kpelle");
		languageCodesToFullNames.put("krc", "Karachay-Balkar");
		languageCodesToFullNames.put("krl", "Karelian");
		languageCodesToFullNames.put("kro", "Kru (Other)");
		languageCodesToFullNames.put("kru", "Kurukh");
		languageCodesToFullNames.put("kua", "Kuanyama");
		languageCodesToFullNames.put("kum", "Kumyk");
		languageCodesToFullNames.put("kur", "Kurdish");
		languageCodesToFullNames.put("-kus", "Kusaie");
		languageCodesToFullNames.put("kut", "Kootenai");
		languageCodesToFullNames.put("lad", "Ladino");
		languageCodesToFullNames.put("lah", "Lahndā");
		languageCodesToFullNames.put("lam", "Lamba (Zambia and Congo)");
		languageCodesToFullNames.put("-lan", "Occitan (post 1500)");
		languageCodesToFullNames.put("lao", "Lao");
		languageCodesToFullNames.put("-lap", "Sami");
		languageCodesToFullNames.put("lat", "Latin");
		languageCodesToFullNames.put("lav", "Latvian");
		languageCodesToFullNames.put("lez", "Lezgian");
		languageCodesToFullNames.put("lim", "Limburgish");
		languageCodesToFullNames.put("lin", "Lingala");
		languageCodesToFullNames.put("lit", "Lithuanian");
		languageCodesToFullNames.put("lol", "Mongo-Nkundu");
		languageCodesToFullNames.put("loz", "Lozi");
		languageCodesToFullNames.put("ltz", "Luxembourgish");
		languageCodesToFullNames.put("lua", "Luba-Lulua");
		languageCodesToFullNames.put("lub", "Luba-Katanga");
		languageCodesToFullNames.put("lug", "Ganda");
		languageCodesToFullNames.put("lui", "Luiseño");
		languageCodesToFullNames.put("lun", "Lunda");
		languageCodesToFullNames.put("luo", "Luo (Kenya and Tanzania)");
		languageCodesToFullNames.put("lus", "Lushai");
		languageCodesToFullNames.put("mac", "Macedonian");
		languageCodesToFullNames.put("mad", "Madurese");
		languageCodesToFullNames.put("mag", "Magahi");
		languageCodesToFullNames.put("mah", "Marshallese");
		languageCodesToFullNames.put("mai", "Maithili");
		languageCodesToFullNames.put("mak", "Makasar");
		languageCodesToFullNames.put("mal", "Malayalam");
		languageCodesToFullNames.put("man", "Mandingo");
		languageCodesToFullNames.put("mao", "Maori");
		languageCodesToFullNames.put("map", "Austronesian (Other)");
		languageCodesToFullNames.put("mar", "Marathi");
		languageCodesToFullNames.put("mas", "Maasai");
		languageCodesToFullNames.put("-max", "Manx");
		languageCodesToFullNames.put("may", "Malay");
		languageCodesToFullNames.put("mdf", "Moksha");
		languageCodesToFullNames.put("mdr", "Mandar");
		languageCodesToFullNames.put("men", "Mende");
		languageCodesToFullNames.put("mga", "Irish, Middle (ca. 1100-1550)");
		languageCodesToFullNames.put("mic", "Micmac");
		languageCodesToFullNames.put("min", "Minangkabau");
		languageCodesToFullNames.put("mis", "Miscellaneous languages");
		languageCodesToFullNames.put("mkh", "Mon-Khmer (Other)");
		languageCodesToFullNames.put("-mla", "Malagasy");
		languageCodesToFullNames.put("mlg", "Malagasy");
		languageCodesToFullNames.put("mlt", "Maltese");
		languageCodesToFullNames.put("mnc", "Manchu");
		languageCodesToFullNames.put("mni", "Manipuri");
		languageCodesToFullNames.put("mno", "Manobo languages");
		languageCodesToFullNames.put("moh", "Mohawk");
		languageCodesToFullNames.put("-mol", "Moldavian");
		languageCodesToFullNames.put("mon", "Mongolian");
		languageCodesToFullNames.put("mos", "Mooré");
		languageCodesToFullNames.put("mul", "Multiple languages");
		languageCodesToFullNames.put("mun", "Munda (Other)");
		languageCodesToFullNames.put("mus", "Creek");
		languageCodesToFullNames.put("mwl", "Mirandese");
		languageCodesToFullNames.put("mwr", "Marwari");
		languageCodesToFullNames.put("myn", "Mayan languages");
		languageCodesToFullNames.put("myv", "Erzya");
		languageCodesToFullNames.put("nah", "Nahuatl");
		languageCodesToFullNames.put("nai", "North American Indian (Other)");
		languageCodesToFullNames.put("nap", "Neapolitan Italian");
		languageCodesToFullNames.put("nau", "Nauru");
		languageCodesToFullNames.put("nav", "Navajo");
		languageCodesToFullNames.put("nbl", "Ndebele (South Africa)");
		languageCodesToFullNames.put("nde", "Ndebele (Zimbabwe)");
		languageCodesToFullNames.put("ndo", "Ndonga");
		languageCodesToFullNames.put("nds", "Low German");
		languageCodesToFullNames.put("nep", "Nepali");
		languageCodesToFullNames.put("new", "Newari");
		languageCodesToFullNames.put("nia", "Nias");
		languageCodesToFullNames.put("nic", "Niger-Kordofanian (Other)");
		languageCodesToFullNames.put("niu", "Niuean");
		languageCodesToFullNames.put("nno", "Norwegian (Nynorsk)");
		languageCodesToFullNames.put("nob", "Norwegian (Bokmål)");
		languageCodesToFullNames.put("nog", "Nogai");
		languageCodesToFullNames.put("non", "Old Norse");
		languageCodesToFullNames.put("nor", "Norwegian");
		languageCodesToFullNames.put("nqo", "N'Ko");
		languageCodesToFullNames.put("nso", "Northern Sotho");
		languageCodesToFullNames.put("nub", "Nubian languages");
		languageCodesToFullNames.put("nwc", "Newari, Old");
		languageCodesToFullNames.put("nya", "Nyanja");
		languageCodesToFullNames.put("nym", "Nyamwezi");
		languageCodesToFullNames.put("nyn", "Nyankole");
		languageCodesToFullNames.put("nyo", "Nyoro");
		languageCodesToFullNames.put("nzi", "Nzima");
		languageCodesToFullNames.put("oci", "Occitan (post-1500)");
		languageCodesToFullNames.put("oji", "Ojibwa");
		languageCodesToFullNames.put("ori", "Oriya");
		languageCodesToFullNames.put("orm", "Oromo");
		languageCodesToFullNames.put("osa", "Osage");
		languageCodesToFullNames.put("oss", "Ossetic");
		languageCodesToFullNames.put("ota", "Turkish, Ottoman");
		languageCodesToFullNames.put("oto", "Otomian languages");
		languageCodesToFullNames.put("paa", "Papuan (Other)");
		languageCodesToFullNames.put("pag", "Pangasinan");
		languageCodesToFullNames.put("pal", "Pahlavi");
		languageCodesToFullNames.put("pam", "Pampanga");
		languageCodesToFullNames.put("pan", "Panjabi");
		languageCodesToFullNames.put("pap", "Papiamento");
		languageCodesToFullNames.put("pau", "Palauan");
		languageCodesToFullNames.put("peo", "Old Persian (ca. 600-400 B.C.)");
		languageCodesToFullNames.put("per", "Persian");
		languageCodesToFullNames.put("phi", "Philippine (Other)");
		languageCodesToFullNames.put("phn", "Phoenician");
		languageCodesToFullNames.put("pli", "Pali");
		languageCodesToFullNames.put("pol", "Polish");
		languageCodesToFullNames.put("pon", "Pohnpeian");
		languageCodesToFullNames.put("por", "Portuguese");
		languageCodesToFullNames.put("pra", "Prakrit languages");
		languageCodesToFullNames.put("pro", "Provençal (to 1500)");
		languageCodesToFullNames.put("pus", "Pushto");
		languageCodesToFullNames.put("que", "Quechua");
		languageCodesToFullNames.put("raj", "Rajasthani");
		languageCodesToFullNames.put("rap", "Rapanui");
		languageCodesToFullNames.put("rar", "Rarotongan");
		languageCodesToFullNames.put("roa", "Romance (Other)");
		languageCodesToFullNames.put("roh", "Raeto-Romance");
		languageCodesToFullNames.put("rom", "Romani");
		languageCodesToFullNames.put("rum", "Romanian");
		languageCodesToFullNames.put("run", "Rundi");
		languageCodesToFullNames.put("rup", "Aromanian");
		languageCodesToFullNames.put("rus", "Russian");
		languageCodesToFullNames.put("sad", "Sandawe");
		languageCodesToFullNames.put("sag", "Sango (Ubangi Creole)");
		languageCodesToFullNames.put("sah", "Yakut");
		languageCodesToFullNames.put("sai", "South American Indian (Other)");
		languageCodesToFullNames.put("sal", "Salishan languages");
		languageCodesToFullNames.put("sam", "Samaritan Aramaic");
		languageCodesToFullNames.put("san", "Sanskrit");
		languageCodesToFullNames.put("-sao", "Samoan");
		languageCodesToFullNames.put("sas", "Sasak");
		languageCodesToFullNames.put("sat", "Santali");
		languageCodesToFullNames.put("-scc", "Serbian");
		languageCodesToFullNames.put("scn", "Sicilian Italian");
		languageCodesToFullNames.put("sco", "Scots");
		languageCodesToFullNames.put("-scr", "Croatian");
		languageCodesToFullNames.put("sel", "Selkup");
		languageCodesToFullNames.put("sem", "Semitic (Other)");
		languageCodesToFullNames.put("sga", "Irish, Old (to 1100)");
		languageCodesToFullNames.put("sgn", "Sign languages");
		languageCodesToFullNames.put("shn", "Shan");
		languageCodesToFullNames.put("-sho", "Shona");
		languageCodesToFullNames.put("sid", "Sidamo");
		languageCodesToFullNames.put("sin", "Sinhalese");
		languageCodesToFullNames.put("sio", "Siouan (Other)");
		languageCodesToFullNames.put("sit", "Sino-Tibetan (Other)");
		languageCodesToFullNames.put("sla", "Slavic (Other)");
		languageCodesToFullNames.put("slo", "Slovak");
		languageCodesToFullNames.put("slv", "Slovenian");
		languageCodesToFullNames.put("sma", "Southern Sami");
		languageCodesToFullNames.put("sme", "Northern Sami");
		languageCodesToFullNames.put("smi", "Sami");
		languageCodesToFullNames.put("smj", "Lule Sami");
		languageCodesToFullNames.put("smn", "Inari Sami");
		languageCodesToFullNames.put("smo", "Samoan");
		languageCodesToFullNames.put("sms", "Skolt Sami");
		languageCodesToFullNames.put("sna", "Shona");
		languageCodesToFullNames.put("snd", "Sindhi");
		languageCodesToFullNames.put("-snh", "Sinhalese");
		languageCodesToFullNames.put("snk", "Soninke");
		languageCodesToFullNames.put("sog", "Sogdian");
		languageCodesToFullNames.put("som", "Somali");
		languageCodesToFullNames.put("son", "Songhai");
		languageCodesToFullNames.put("sot", "Sotho");
		languageCodesToFullNames.put("spa", "Spanish");
		languageCodesToFullNames.put("srd", "Sardinian");
		languageCodesToFullNames.put("srn", "Sranan");
		languageCodesToFullNames.put("srp", "Serbian");
		languageCodesToFullNames.put("srr", "Serer");
		languageCodesToFullNames.put("ssa", "Nilo-Saharan (Other)");
		languageCodesToFullNames.put("-sso", "Sotho");
		languageCodesToFullNames.put("ssw", "Swazi");
		languageCodesToFullNames.put("suk", "Sukuma");
		languageCodesToFullNames.put("sun", "Sundanese");
		languageCodesToFullNames.put("sus", "Susu");
		languageCodesToFullNames.put("sux", "Sumerian");
		languageCodesToFullNames.put("swa", "Swahili");
		languageCodesToFullNames.put("swe", "Swedish");
		languageCodesToFullNames.put("-swz", "Swazi");
		languageCodesToFullNames.put("syc", "Syriac");
		languageCodesToFullNames.put("syr", "Syriac, Modern");
		languageCodesToFullNames.put("-tag", "Tagalog");
		languageCodesToFullNames.put("tah", "Tahitian");
		languageCodesToFullNames.put("tai", "Tai (Other)");
		languageCodesToFullNames.put("-taj", "Tajik");
		languageCodesToFullNames.put("tam", "Tamil");
		languageCodesToFullNames.put("-tar", "Tatar");
		languageCodesToFullNames.put("tat", "Tatar");
		languageCodesToFullNames.put("tel", "Telugu");
		languageCodesToFullNames.put("tem", "Temne");
		languageCodesToFullNames.put("ter", "Terena");
		languageCodesToFullNames.put("tet", "Tetum");
		languageCodesToFullNames.put("tgk", "Tajik");
		languageCodesToFullNames.put("tgl", "Tagalog");
		languageCodesToFullNames.put("tha", "Thai");
		languageCodesToFullNames.put("tib", "Tibetan");
		languageCodesToFullNames.put("tig", "Tigré");
		languageCodesToFullNames.put("tir", "Tigrinya");
		languageCodesToFullNames.put("tiv", "Tiv");
		languageCodesToFullNames.put("tkl", "Tokelauan");
		languageCodesToFullNames.put("tlh", "Klingon (Artificial language)");
		languageCodesToFullNames.put("tli", "Tlingit");
		languageCodesToFullNames.put("tmh", "Tamashek");
		languageCodesToFullNames.put("tog", "Tonga (Nyasa)");
		languageCodesToFullNames.put("ton", "Tongan");
		languageCodesToFullNames.put("tpi", "Tok Pisin");
		languageCodesToFullNames.put("-tru", "Truk");
		languageCodesToFullNames.put("tsi", "Tsimshian");
		languageCodesToFullNames.put("tsn", "Tswana");
		languageCodesToFullNames.put("tso", "Tsonga");
		languageCodesToFullNames.put("-tsw", "Tswana");
		languageCodesToFullNames.put("tuk", "Turkmen");
		languageCodesToFullNames.put("tum", "Tumbuka");
		languageCodesToFullNames.put("tup", "Tupi languages");
		languageCodesToFullNames.put("tur", "Turkish");
		languageCodesToFullNames.put("tut", "Altaic (Other)");
		languageCodesToFullNames.put("tvl", "Tuvaluan");
		languageCodesToFullNames.put("twi", "Twi");
		languageCodesToFullNames.put("tyv", "Tuvinian");
		languageCodesToFullNames.put("udm", "Udmurt");
		languageCodesToFullNames.put("uga", "Ugaritic");
		languageCodesToFullNames.put("uig", "Uighur");
		languageCodesToFullNames.put("ukr", "Ukrainian");
		languageCodesToFullNames.put("umb", "Umbundu");
		languageCodesToFullNames.put("und", "Undetermined");
		languageCodesToFullNames.put("urd", "Urdu");
		languageCodesToFullNames.put("uzb", "Uzbek");
		languageCodesToFullNames.put("vai", "Vai");
		languageCodesToFullNames.put("ven", "Venda");
		languageCodesToFullNames.put("vie", "Vietnamese");
		languageCodesToFullNames.put("vol", "Volapük");
		languageCodesToFullNames.put("vot", "Votic");
		languageCodesToFullNames.put("wak", "Wakashan languages");
		languageCodesToFullNames.put("wal", "Wolayta");
		languageCodesToFullNames.put("war", "Waray");
		languageCodesToFullNames.put("was", "Washoe");
		languageCodesToFullNames.put("wel", "Welsh");
		languageCodesToFullNames.put("wen", "Sorbian (Other)");
		languageCodesToFullNames.put("wln", "Walloon");
		languageCodesToFullNames.put("wol", "Wolof");
		languageCodesToFullNames.put("xal", "Oirat");
		languageCodesToFullNames.put("xho", "Xhosa");
		languageCodesToFullNames.put("yao", "Yao (Africa)");
		languageCodesToFullNames.put("yap", "Yapese");
		languageCodesToFullNames.put("yid", "Yiddish");
		languageCodesToFullNames.put("yor", "Yoruba");
		languageCodesToFullNames.put("ypk", "Yupik languages");
		languageCodesToFullNames.put("zap", "Zapotec");
		languageCodesToFullNames.put("zbl", "Blissymbolics");
		languageCodesToFullNames.put("zen", "Zenaga");
		languageCodesToFullNames.put("zha", "Zhuang");
		languageCodesToFullNames.put("znd", "Zande languages");
		languageCodesToFullNames.put("zul", "Zulu");
		languageCodesToFullNames.put("zun", "Zuni");
		languageCodesToFullNames.put("zxx", "No linguistic content");
		languageCodesToFullNames.put("zza", "Zaza");
		
		return languageCodesToFullNames;
	}
	
	public static ConcurrentHashMap<String,String> getCountryCodesToFullNamesMap() {
		ConcurrentHashMap<String,String> countryCodesToFullNames = new ConcurrentHashMap<String,String>();
		
		countryCodesToFullNames.put("aa", "Albania");
		countryCodesToFullNames.put("abc", "Alberta");
		countryCodesToFullNames.put("-ac", "Ashmore and Cartier Islands");
		countryCodesToFullNames.put("aca", "Australian Capital Territory");
		countryCodesToFullNames.put("ae", "Algeria");
		countryCodesToFullNames.put("af", "Afghanistan");
		countryCodesToFullNames.put("ag", "Argentina");
		countryCodesToFullNames.put("-ai", "Anguilla");
		countryCodesToFullNames.put("ai", "Armenia (Republic)");
		countryCodesToFullNames.put("-air", "Armenian S.S.R.");
		countryCodesToFullNames.put("aj", "Azerbaijan");
		countryCodesToFullNames.put("-ajr", "Azerbaijan S.S.R.");
		countryCodesToFullNames.put("aku", "Alaska");
		countryCodesToFullNames.put("alu", "Alabama");
		countryCodesToFullNames.put("am", "Anguilla");
		countryCodesToFullNames.put("an", "Andorra");
		countryCodesToFullNames.put("ao", "Angola");
		countryCodesToFullNames.put("aq", "Antigua and Barbuda");
		countryCodesToFullNames.put("aru", "Arkansas");
		countryCodesToFullNames.put("as", "American Samoa");
		countryCodesToFullNames.put("at", "Australia");
		countryCodesToFullNames.put("au", "Austria");
		countryCodesToFullNames.put("aw", "Aruba");
		countryCodesToFullNames.put("ay", "Antarctica");
		countryCodesToFullNames.put("azu", "Arizona");
		countryCodesToFullNames.put("ba", "Bahrain");
		countryCodesToFullNames.put("bb", "Barbados");
		countryCodesToFullNames.put("bcc", "British Columbia");
		countryCodesToFullNames.put("bd", "Burundi");
		countryCodesToFullNames.put("be", "Belgium");
		countryCodesToFullNames.put("bf", "Bahamas");
		countryCodesToFullNames.put("bg", "Bangladesh");
		countryCodesToFullNames.put("bh", "Belize");
		countryCodesToFullNames.put("bi", "British Indian Ocean Territory");
		countryCodesToFullNames.put("bl", "Brazil");
		countryCodesToFullNames.put("bm", "Bermuda Islands");
		countryCodesToFullNames.put("bn", "Bosnia and Herzegovina");
		countryCodesToFullNames.put("bo", "Bolivia");
		countryCodesToFullNames.put("bp", "Solomon Islands");
		countryCodesToFullNames.put("br", "Burma");
		countryCodesToFullNames.put("bs", "Botswana");
		countryCodesToFullNames.put("bt", "Bhutan");
		countryCodesToFullNames.put("bu", "Bulgaria");
		countryCodesToFullNames.put("bv", "Bouvet Island");
		countryCodesToFullNames.put("bw", "Belarus");
		countryCodesToFullNames.put("-bwr", "Byelorussian S.S.R.");
		countryCodesToFullNames.put("bx", "Brunei");
		countryCodesToFullNames.put("ca", "Caribbean Netherlands");
		countryCodesToFullNames.put("cau", "California");
		countryCodesToFullNames.put("cb", "Cambodia");
		countryCodesToFullNames.put("cc", "China");
		countryCodesToFullNames.put("cd", "Chad");
		countryCodesToFullNames.put("ce", "Sri Lanka");
		countryCodesToFullNames.put("cf", "Congo (Brazzaville)");
		countryCodesToFullNames.put("cg", "Congo (Democratic Republic)");
		countryCodesToFullNames.put("ch", "China (Republic : 1949- )");
		countryCodesToFullNames.put("ci", "Croatia");
		countryCodesToFullNames.put("cj", "Cayman Islands");
		countryCodesToFullNames.put("ck", "Colombia");
		countryCodesToFullNames.put("cl", "Chile");
		countryCodesToFullNames.put("cm", "Cameroon");
		countryCodesToFullNames.put("-cn", "Canada");
		countryCodesToFullNames.put("co", "Curaçao");
		countryCodesToFullNames.put("cou", "Colorado");
		countryCodesToFullNames.put("-cp", "Canton and Enderbury Islands");
		countryCodesToFullNames.put("cq", "Comoros");
		countryCodesToFullNames.put("cr", "Costa Rica");
		countryCodesToFullNames.put("-cs", "Czechoslovakia");
		countryCodesToFullNames.put("ctu", "Connecticut");
		countryCodesToFullNames.put("cu", "Cuba");
		countryCodesToFullNames.put("cv", "Cabo Verde");
		countryCodesToFullNames.put("cw", "Cook Islands");
		countryCodesToFullNames.put("cx", "Central African Republic");
		countryCodesToFullNames.put("cy", "Cyprus");
		countryCodesToFullNames.put("-cz", "Canal Zone");
		countryCodesToFullNames.put("dcu", "District of Columbia");
		countryCodesToFullNames.put("deu", "Delaware");
		countryCodesToFullNames.put("dk", "Denmark");
		countryCodesToFullNames.put("dm", "Benin");
		countryCodesToFullNames.put("dq", "Dominica");
		countryCodesToFullNames.put("dr", "Dominican Republic");
		countryCodesToFullNames.put("ea", "Eritrea");
		countryCodesToFullNames.put("ec", "Ecuador");
		countryCodesToFullNames.put("eg", "Equatorial Guinea");
		countryCodesToFullNames.put("em", "Timor-Leste");
		countryCodesToFullNames.put("enk", "England");
		countryCodesToFullNames.put("er", "Estonia");
		countryCodesToFullNames.put("-err", "Estonia");
		countryCodesToFullNames.put("es", "El Salvador");
		countryCodesToFullNames.put("et", "Ethiopia");
		countryCodesToFullNames.put("fa", "Faroe Islands");
		countryCodesToFullNames.put("fg", "French Guiana");
		countryCodesToFullNames.put("fi", "Finland");
		countryCodesToFullNames.put("fj", "Fiji");
		countryCodesToFullNames.put("fk", "Falkland Islands");
		countryCodesToFullNames.put("flu", "Florida");
		countryCodesToFullNames.put("fm", "Micronesia (Federated States)");
		countryCodesToFullNames.put("fp", "French Polynesia");
		countryCodesToFullNames.put("fr", "France");
		countryCodesToFullNames.put("fs", "Terres australes et antarctiques françaises");
		countryCodesToFullNames.put("ft", "Djibouti");
		countryCodesToFullNames.put("gau", "Georgia");
		countryCodesToFullNames.put("gb", "Kiribati");
		countryCodesToFullNames.put("gd", "Grenada");
		countryCodesToFullNames.put("-ge", "Germany (East)");
		countryCodesToFullNames.put("gh", "Ghana");
		countryCodesToFullNames.put("gi", "Gibraltar");
		countryCodesToFullNames.put("gl", "Greenland");
		countryCodesToFullNames.put("gm", "Gambia");
		countryCodesToFullNames.put("-gn", "Gilbert and Ellice Islands");
		countryCodesToFullNames.put("go", "Gabon");
		countryCodesToFullNames.put("gp", "Guadeloupe");
		countryCodesToFullNames.put("gr", "Greece");
		countryCodesToFullNames.put("gs", "Georgia (Republic)");
		countryCodesToFullNames.put("-gsr", "Georgian S.S.R.");
		countryCodesToFullNames.put("gt", "Guatemala");
		countryCodesToFullNames.put("gu", "Guam");
		countryCodesToFullNames.put("gv", "Guinea");
		countryCodesToFullNames.put("gw", "Germany");
		countryCodesToFullNames.put("gy", "Guyana");
		countryCodesToFullNames.put("gz", "Gaza Strip");
		countryCodesToFullNames.put("hiu", "Hawaii");
		countryCodesToFullNames.put("-hk", "Hong Kong");
		countryCodesToFullNames.put("hm", "Heard and McDonald Islands");
		countryCodesToFullNames.put("ho", "Honduras");
		countryCodesToFullNames.put("ht", "Haiti");
		countryCodesToFullNames.put("hu", "Hungary");
		countryCodesToFullNames.put("iau", "Iowa");
		countryCodesToFullNames.put("ic", "Iceland");
		countryCodesToFullNames.put("idu", "Idaho");
		countryCodesToFullNames.put("ie", "Ireland");
		countryCodesToFullNames.put("ii", "India");
		countryCodesToFullNames.put("ilu", "Illinois");
		countryCodesToFullNames.put("inu", "Indiana");
		countryCodesToFullNames.put("io", "Indonesia");
		countryCodesToFullNames.put("iq", "Iraq");
		countryCodesToFullNames.put("ir", "Iran");
		countryCodesToFullNames.put("is", "Israel");
		countryCodesToFullNames.put("it", "Italy");
		countryCodesToFullNames.put("-iu", "Israel-Syria Demilitarized Zones");
		countryCodesToFullNames.put("iv", "Côte d'Ivoire");
		countryCodesToFullNames.put("-iw", "Israel-Jordan Demilitarized Zones");
		countryCodesToFullNames.put("iy", "Iraq-Saudi Arabia Neutral Zone");
		countryCodesToFullNames.put("ja", "Japan");
		countryCodesToFullNames.put("ji", "Johnston Atoll");
		countryCodesToFullNames.put("jm", "Jamaica");
		countryCodesToFullNames.put("-jn", "Jan Mayen");
		countryCodesToFullNames.put("jo", "Jordan");
		countryCodesToFullNames.put("ke", "Kenya");
		countryCodesToFullNames.put("kg", "Kyrgyzstan");
		countryCodesToFullNames.put("-kgr", "Kirghiz S.S.R.");
		countryCodesToFullNames.put("kn", "Korea (North)");
		countryCodesToFullNames.put("ko", "Korea (South)");
		countryCodesToFullNames.put("ksu", "Kansas");
		countryCodesToFullNames.put("ku", "Kuwait");
		countryCodesToFullNames.put("kv", "Kosovo");
		countryCodesToFullNames.put("kyu", "Kentucky");
		countryCodesToFullNames.put("kz", "Kazakhstan");
		countryCodesToFullNames.put("-kzr", "Kazakh S.S.R.");
		countryCodesToFullNames.put("lau", "Louisiana");
		countryCodesToFullNames.put("lb", "Liberia");
		countryCodesToFullNames.put("le", "Lebanon");
		countryCodesToFullNames.put("lh", "Liechtenstein");
		countryCodesToFullNames.put("li", "Lithuania");
		countryCodesToFullNames.put("-lir", "Lithuania");
		countryCodesToFullNames.put("-ln", "Central and Southern Line Islands");
		countryCodesToFullNames.put("lo", "Lesotho");
		countryCodesToFullNames.put("ls", "Laos");
		countryCodesToFullNames.put("lu", "Luxembourg");
		countryCodesToFullNames.put("lv", "Latvia");
		countryCodesToFullNames.put("-lvr", "Latvia");
		countryCodesToFullNames.put("ly", "Libya");
		countryCodesToFullNames.put("mau", "Massachusetts");
		countryCodesToFullNames.put("mbc", "Manitoba");
		countryCodesToFullNames.put("mc", "Monaco");
		countryCodesToFullNames.put("mdu", "Maryland");
		countryCodesToFullNames.put("meu", "Maine");
		countryCodesToFullNames.put("mf", "Mauritius");
		countryCodesToFullNames.put("mg", "Madagascar");
		countryCodesToFullNames.put("-mh", "Macao");
		countryCodesToFullNames.put("miu", "Michigan");
		countryCodesToFullNames.put("mj", "Montserrat");
		countryCodesToFullNames.put("mk", "Oman");
		countryCodesToFullNames.put("ml", "Mali");
		countryCodesToFullNames.put("mm", "Malta");
		countryCodesToFullNames.put("mnu", "Minnesota");
		countryCodesToFullNames.put("mo", "Montenegro");
		countryCodesToFullNames.put("mou", "Missouri");
		countryCodesToFullNames.put("mp", "Mongolia");
		countryCodesToFullNames.put("mq", "Martinique");
		countryCodesToFullNames.put("mr", "Morocco");
		countryCodesToFullNames.put("msu", "Mississippi");
		countryCodesToFullNames.put("mtu", "Montana");
		countryCodesToFullNames.put("mu", "Mauritania");
		countryCodesToFullNames.put("mv", "Moldova");
		countryCodesToFullNames.put("-mvr", "Moldavian S.S.R.");
		countryCodesToFullNames.put("mw", "Malawi");
		countryCodesToFullNames.put("mx", "Mexico");
		countryCodesToFullNames.put("my", "Malaysia");
		countryCodesToFullNames.put("mz", "Mozambique");
		countryCodesToFullNames.put("-na", "Netherlands Antilles");
		countryCodesToFullNames.put("nbu", "Nebraska");
		countryCodesToFullNames.put("ncu", "North Carolina");
		countryCodesToFullNames.put("ndu", "North Dakota");
		countryCodesToFullNames.put("ne", "Netherlands");
		countryCodesToFullNames.put("nfc", "Newfoundland and Labrador");
		countryCodesToFullNames.put("ng", "Niger");
		countryCodesToFullNames.put("nhu", "New Hampshire");
		countryCodesToFullNames.put("nik", "Northern Ireland");
		countryCodesToFullNames.put("nju", "New Jersey");
		countryCodesToFullNames.put("nkc", "New Brunswick");
		countryCodesToFullNames.put("nl", "New Caledonia");
		countryCodesToFullNames.put("-nm", "Northern Mariana Islands");
		countryCodesToFullNames.put("nmu", "New Mexico");
		countryCodesToFullNames.put("nn", "Vanuatu");
		countryCodesToFullNames.put("no", "Norway");
		countryCodesToFullNames.put("np", "Nepal");
		countryCodesToFullNames.put("nq", "Nicaragua");
		countryCodesToFullNames.put("nr", "Nigeria");
		countryCodesToFullNames.put("nsc", "Nova Scotia");
		countryCodesToFullNames.put("ntc", "Northwest Territories");
		countryCodesToFullNames.put("nu", "Nauru");
		countryCodesToFullNames.put("nuc", "Nunavut");
		countryCodesToFullNames.put("nvu", "Nevada");
		countryCodesToFullNames.put("nw", "Northern Mariana Islands");
		countryCodesToFullNames.put("nx", "Norfolk Island");
		countryCodesToFullNames.put("nyu", "New York (State)");
		countryCodesToFullNames.put("nz", "New Zealand");
		countryCodesToFullNames.put("ohu", "Ohio");
		countryCodesToFullNames.put("oku", "Oklahoma");
		countryCodesToFullNames.put("onc", "Ontario");
		countryCodesToFullNames.put("oru", "Oregon");
		countryCodesToFullNames.put("ot", "Mayotte");
		countryCodesToFullNames.put("pau", "Pennsylvania");
		countryCodesToFullNames.put("pc", "Pitcairn Island");
		countryCodesToFullNames.put("pe", "Peru");
		countryCodesToFullNames.put("pf", "Paracel Islands");
		countryCodesToFullNames.put("pg", "Guinea-Bissau");
		countryCodesToFullNames.put("ph", "Philippines");
		countryCodesToFullNames.put("pic", "Prince Edward Island");
		countryCodesToFullNames.put("pk", "Pakistan");
		countryCodesToFullNames.put("pl", "Poland");
		countryCodesToFullNames.put("pn", "Panama");
		countryCodesToFullNames.put("po", "Portugal");
		countryCodesToFullNames.put("pp", "Papua New Guinea");
		countryCodesToFullNames.put("pr", "Puerto Rico");
		countryCodesToFullNames.put("-pt", "Portuguese Timor");
		countryCodesToFullNames.put("pw", "Palau");
		countryCodesToFullNames.put("py", "Paraguay");
		countryCodesToFullNames.put("qa", "Qatar");
		countryCodesToFullNames.put("qea", "Queensland");
		countryCodesToFullNames.put("quc", "Québec (Province)");
		countryCodesToFullNames.put("rb", "Serbia");
		countryCodesToFullNames.put("re", "Réunion");
		countryCodesToFullNames.put("rh", "Zimbabwe");
		countryCodesToFullNames.put("riu", "Rhode Island");
		countryCodesToFullNames.put("rm", "Romania");
		countryCodesToFullNames.put("ru", "Russia (Federation)");
		countryCodesToFullNames.put("-rur", "Russian S.F.S.R.");
		countryCodesToFullNames.put("rw", "Rwanda");
		countryCodesToFullNames.put("-ry", "Ryukyu Islands, Southern");
		countryCodesToFullNames.put("sa", "South Africa");
		countryCodesToFullNames.put("-sb", "Svalbard");
		countryCodesToFullNames.put("sc", "Saint-Barthélemy");
		countryCodesToFullNames.put("scu", "South Carolina");
		countryCodesToFullNames.put("sd", "South Sudan");
		countryCodesToFullNames.put("sdu", "South Dakota");
		countryCodesToFullNames.put("se", "Seychelles");
		countryCodesToFullNames.put("sf", "Sao Tome and Principe");
		countryCodesToFullNames.put("sg", "Senegal");
		countryCodesToFullNames.put("sh", "Spanish North Africa");
		countryCodesToFullNames.put("si", "Singapore");
		countryCodesToFullNames.put("sj", "Sudan");
		countryCodesToFullNames.put("-sk", "Sikkim");
		countryCodesToFullNames.put("sl", "Sierra Leone");
		countryCodesToFullNames.put("sm", "San Marino");
		countryCodesToFullNames.put("sn", "Sint Maarten");
		countryCodesToFullNames.put("snc", "Saskatchewan");
		countryCodesToFullNames.put("so", "Somalia");
		countryCodesToFullNames.put("sp", "Spain");
		countryCodesToFullNames.put("sq", "Swaziland");
		countryCodesToFullNames.put("sr", "Surinam");
		countryCodesToFullNames.put("ss", "Western Sahara");
		countryCodesToFullNames.put("st", "Saint-Martin");
		countryCodesToFullNames.put("stk", "Scotland");
		countryCodesToFullNames.put("su", "Saudi Arabia");
		countryCodesToFullNames.put("-sv", "Swan Islands");
		countryCodesToFullNames.put("sw", "Sweden");
		countryCodesToFullNames.put("sx", "Namibia");
		countryCodesToFullNames.put("sy", "Syria");
		countryCodesToFullNames.put("sz", "Switzerland");
		countryCodesToFullNames.put("ta", "Tajikistan");
		countryCodesToFullNames.put("-tar", "Tajik S.S.R.");
		countryCodesToFullNames.put("tc", "Turks and Caicos Islands");
		countryCodesToFullNames.put("tg", "Togo");
		countryCodesToFullNames.put("th", "Thailand");
		countryCodesToFullNames.put("ti", "Tunisia");
		countryCodesToFullNames.put("tk", "Turkmenistan");
		countryCodesToFullNames.put("-tkr", "Turkmen S.S.R.");
		countryCodesToFullNames.put("tl", "Tokelau");
		countryCodesToFullNames.put("tma", "Tasmania");
		countryCodesToFullNames.put("tnu", "Tennessee");
		countryCodesToFullNames.put("to", "Tonga");
		countryCodesToFullNames.put("tr", "Trinidad and Tobago");
		countryCodesToFullNames.put("ts", "United Arab Emirates");
		countryCodesToFullNames.put("-tt", "Trust Territory of the Pacific Islands");
		countryCodesToFullNames.put("tu", "Turkey");
		countryCodesToFullNames.put("tv", "Tuvalu");
		countryCodesToFullNames.put("txu", "Texas");
		countryCodesToFullNames.put("tz", "Tanzania");
		countryCodesToFullNames.put("ua", "Egypt");
		countryCodesToFullNames.put("uc", "United States Misc. Caribbean Islands");
		countryCodesToFullNames.put("ug", "Uganda");
		countryCodesToFullNames.put("-ui", "United Kingdom Misc. Islands");
		countryCodesToFullNames.put("uik", "United Kingdom Misc. Islands");
		countryCodesToFullNames.put("-uk", "United Kingdom");
		countryCodesToFullNames.put("un", "Ukraine");
		countryCodesToFullNames.put("-unr", "Ukraine");
		countryCodesToFullNames.put("up", "United States Misc. Pacific Islands");
		countryCodesToFullNames.put("-ur", "Soviet Union");
		countryCodesToFullNames.put("-us", "United States");
		countryCodesToFullNames.put("utu", "Utah");
		countryCodesToFullNames.put("uv", "Burkina Faso");
		countryCodesToFullNames.put("uy", "Uruguay");
		countryCodesToFullNames.put("uz", "Uzbekistan");
		countryCodesToFullNames.put("-uzr", "Uzbek S.S.R.");
		countryCodesToFullNames.put("vau", "Virginia");
		countryCodesToFullNames.put("vb", "British Virgin Islands");
		countryCodesToFullNames.put("vc", "Vatican City");
		countryCodesToFullNames.put("ve", "Venezuela");
		countryCodesToFullNames.put("vi", "Virgin Islands of the United States");
		countryCodesToFullNames.put("vm", "Vietnam");
		countryCodesToFullNames.put("-vn", "Vietnam, North");
		countryCodesToFullNames.put("vp", "Various places");
		countryCodesToFullNames.put("vra", "Victoria");
		countryCodesToFullNames.put("-vs", "Vietnam, South");
		countryCodesToFullNames.put("vtu", "Vermont");
		countryCodesToFullNames.put("wau", "Washington (State)");
		countryCodesToFullNames.put("-wb", "West Berlin");
		countryCodesToFullNames.put("wea", "Western Australia");
		countryCodesToFullNames.put("wf", "Wallis and Futuna");
		countryCodesToFullNames.put("wiu", "Wisconsin");
		countryCodesToFullNames.put("wj", "West Bank of the Jordan River");
		countryCodesToFullNames.put("wk", "Wake Island");
		countryCodesToFullNames.put("wlk", "Wales");
		countryCodesToFullNames.put("ws", "Samoa");
		countryCodesToFullNames.put("wvu", "West Virginia");
		countryCodesToFullNames.put("wyu", "Wyoming");
		countryCodesToFullNames.put("xa", "Christmas Island (Indian Ocean)");
		countryCodesToFullNames.put("xb", "Cocos (Keeling) Islands");
		countryCodesToFullNames.put("xc", "Maldives");
		countryCodesToFullNames.put("xd", "Saint Kitts-Nevis");
		countryCodesToFullNames.put("xe", "Marshall Islands");
		countryCodesToFullNames.put("xf", "Midway Islands");
		countryCodesToFullNames.put("xga", "Coral Sea Islands Territory");
		countryCodesToFullNames.put("xh", "Niue");
		countryCodesToFullNames.put("-xi", "Saint Kitts-Nevis-Anguilla");
		countryCodesToFullNames.put("xj", "Saint Helena");
		countryCodesToFullNames.put("xk", "Saint Lucia");
		countryCodesToFullNames.put("xl", "Saint Pierre and Miquelon");
		countryCodesToFullNames.put("xm", "Saint Vincent and the Grenadines");
		countryCodesToFullNames.put("xn", "Macedonia");
		countryCodesToFullNames.put("xna", "New South Wales");
		countryCodesToFullNames.put("xo", "Slovakia");
		countryCodesToFullNames.put("xoa", "Northern Territory");
		countryCodesToFullNames.put("xp", "Spratly Island");
		countryCodesToFullNames.put("xr", "Czech Republic");
		countryCodesToFullNames.put("xra", "South Australia");
		countryCodesToFullNames.put("xs", "South Georgia and the South Sandwich Islands");
		countryCodesToFullNames.put("xv", "Slovenia");
		countryCodesToFullNames.put("xx", "No place, unknown, or undetermined");
		countryCodesToFullNames.put("xxc", "Canada");
		countryCodesToFullNames.put("xxk", "United Kingdom");
		countryCodesToFullNames.put("-xxr", "Soviet Union");
		countryCodesToFullNames.put("xxu", "United States");
		countryCodesToFullNames.put("ye", "Yemen");
		countryCodesToFullNames.put("ykc", "Yukon Territory");
		countryCodesToFullNames.put("-ys", "Yemen (People's Democratic Republic)");
		countryCodesToFullNames.put("-yu", "Serbia and Montenegro");
		countryCodesToFullNames.put("za", "Zambia");
		
		return countryCodesToFullNames;
	}
	
	public static ConcurrentHashMap<String,String> getGeographicAreasToFullNamesMap() {
		ConcurrentHashMap<String,String> geographicAreasToFullNames = new ConcurrentHashMap<String,String>();
		
		geographicAreasToFullNames.put("a", "Asia");
		geographicAreasToFullNames.put("a-af", "Afghanistan");
		geographicAreasToFullNames.put("a-ai", "Armenia (Republic)");
		geographicAreasToFullNames.put("a-aj", "Azerbaijan");
		geographicAreasToFullNames.put("a-ba", "Bahrain");
		geographicAreasToFullNames.put("a-bg", "Bangladesh");
		geographicAreasToFullNames.put("a-bn", "Borneo");
		geographicAreasToFullNames.put("a-br", "Burma");
		geographicAreasToFullNames.put("a-bt", "Bhutan");
		geographicAreasToFullNames.put("a-bx", "Brunei");
		geographicAreasToFullNames.put("a-cb", "Cambodia");
		geographicAreasToFullNames.put("a-cc", "China");
		geographicAreasToFullNames.put("a-cc-an", "Anhui Sheng (China)");
		geographicAreasToFullNames.put("a-cc-ch", "Zhejiang Sheng (China)");
		geographicAreasToFullNames.put("a-cc-cq", "Chongqing (China)");
		geographicAreasToFullNames.put("a-cc-fu", "Fujian Sheng (China)");
		geographicAreasToFullNames.put("a-cc-ha", "Hainan Sheng (China)");
		geographicAreasToFullNames.put("a-cc-he", "Heilongjiang Sheng (China)");
		geographicAreasToFullNames.put("a-cc-hh", "Hubei Sheng (China)");
		geographicAreasToFullNames.put("a-cc-hk", "Hong Kong (China)");
		geographicAreasToFullNames.put("a-cc-ho", "Henan Sheng (China)");
		geographicAreasToFullNames.put("a-cc-hp", "Hebei Sheng (China)");
		geographicAreasToFullNames.put("a-cc-hu", "Hunan Sheng (China)");
		geographicAreasToFullNames.put("a-cc-im", "Inner Mongolia (China)");
		geographicAreasToFullNames.put("a-cc-ka", "Gansu Sheng (China)");
		geographicAreasToFullNames.put("a-cc-kc", "Guangxi Zhuangzu Zizhiqu (China)");
		geographicAreasToFullNames.put("a-cc-ki", "Jiangxi Sheng (China)");
		geographicAreasToFullNames.put("a-cc-kn", "Guangdong Sheng (China)");
		geographicAreasToFullNames.put("a-cc-kr", "Jilin Sheng (China)");
		geographicAreasToFullNames.put("a-cc-ku", "Jiangsu Sheng (China)");
		geographicAreasToFullNames.put("a-cc-kw", "Guizhou Sheng (China)");
		geographicAreasToFullNames.put("a-cc-lp", "Liaoning Sheng (China)");
		geographicAreasToFullNames.put("a-cc-mh", "Macau (China : Special Administrative Region)");
		geographicAreasToFullNames.put("a-cc-nn", "Ningxia Huizu Zizhiqu (China)");
		geographicAreasToFullNames.put("a-cc-pe", "Beijing (China)");
		geographicAreasToFullNames.put("a-cc-sh", "Shanxi Sheng (China)");
		geographicAreasToFullNames.put("a-cc-sm", "Shanghai (China)");
		geographicAreasToFullNames.put("a-cc-sp", "Shandong Sheng (China)");
		geographicAreasToFullNames.put("a-cc-ss", "Shaanxi Sheng (China)");
		geographicAreasToFullNames.put("a-cc-su", "Xinjiang Uygur Zizhiqu (China)");
		geographicAreasToFullNames.put("a-cc-sz", "Sichuan Sheng (China)");
		geographicAreasToFullNames.put("a-cc-ti", "Tibet (China)");
		geographicAreasToFullNames.put("a-cc-tn", "Tianjin (China)");
		geographicAreasToFullNames.put("a-cc-ts", "Qinghai Sheng (China)");
		geographicAreasToFullNames.put("a-cc-yu", "Yunnan Sheng (China)");
		geographicAreasToFullNames.put("a-ccg", "Yangtze River (China)");
		geographicAreasToFullNames.put("a-cck", "Kunlun Mountains (China and India)");
		geographicAreasToFullNames.put("a-ccp", "Bo Hai (China)");
		geographicAreasToFullNames.put("a-ccs", "Xi River (China)");
		geographicAreasToFullNames.put("a-ccy", "Yellow River (China)");
		geographicAreasToFullNames.put("a-ce", "Sri Lanka");
		geographicAreasToFullNames.put("a-ch", "Taiwan");
		geographicAreasToFullNames.put("a-cy", "Cyprus");
		geographicAreasToFullNames.put("a-em", "Timor-Leste");
		geographicAreasToFullNames.put("a-gs", "Georgia (Republic)");
		geographicAreasToFullNames.put("-a-hk", "Hong Kong");
		geographicAreasToFullNames.put("a-ii", "India");
		geographicAreasToFullNames.put("a-io", "Indonesia");
		geographicAreasToFullNames.put("a-iq", "Iraq");
		geographicAreasToFullNames.put("a-ir", "Iran");
		geographicAreasToFullNames.put("a-is", "Israel");
		geographicAreasToFullNames.put("a-ja", "Japan");
		geographicAreasToFullNames.put("a-jo", "Jordan");
		geographicAreasToFullNames.put("a-kg", "Kyrgyzstan");
		geographicAreasToFullNames.put("a-kn", "Korea (North)");
		geographicAreasToFullNames.put("a-ko", "Korea (South)");
		geographicAreasToFullNames.put("a-kr", "Korea");
		geographicAreasToFullNames.put("a-ku", "Kuwait");
		geographicAreasToFullNames.put("a-kz", "Kazakhstan");
		geographicAreasToFullNames.put("a-le", "Lebanon");
		geographicAreasToFullNames.put("a-ls", "Laos");
		geographicAreasToFullNames.put("-a-mh", "Macao");
		geographicAreasToFullNames.put("a-mk", "Oman");
		geographicAreasToFullNames.put("a-mp", "Mongolia");
		geographicAreasToFullNames.put("a-my", "Malaysia");
		geographicAreasToFullNames.put("a-np", "Nepal");
		geographicAreasToFullNames.put("a-nw", "New Guinea");
		geographicAreasToFullNames.put("-a-ok", "Okinawa");
		geographicAreasToFullNames.put("a-ph", "Philippines");
		geographicAreasToFullNames.put("a-pk", "Pakistan");
		geographicAreasToFullNames.put("a-pp", "Papua New Guinea");
		geographicAreasToFullNames.put("-a-pt", "Portuguese Timor");
		geographicAreasToFullNames.put("a-qa", "Qatar");
		geographicAreasToFullNames.put("a-si", "Singapore");
		geographicAreasToFullNames.put("-a-sk", "Sikkim");
		geographicAreasToFullNames.put("a-su", "Saudi Arabia");
		geographicAreasToFullNames.put("a-sy", "Syria");
		geographicAreasToFullNames.put("a-ta", "Tajikistan");
		geographicAreasToFullNames.put("a-th", "Thailand");
		geographicAreasToFullNames.put("a-tk", "Turkmenistan");
		geographicAreasToFullNames.put("a-ts", "United Arab Emirates");
		geographicAreasToFullNames.put("a-tu", "Turkey");
		geographicAreasToFullNames.put("a-uz", "Uzbekistan");
		geographicAreasToFullNames.put("-a-vn", "Viet Nam, North");
		geographicAreasToFullNames.put("-a-vs", "Viet Nam, South");
		geographicAreasToFullNames.put("a-vt", "Vietnam");
		geographicAreasToFullNames.put("a-ye", "Yemen (Republic)");
		geographicAreasToFullNames.put("-a-ys", "Yemen (People's Democratic Republic)");
		geographicAreasToFullNames.put("aa", "Amur River (China and Russia)");
		geographicAreasToFullNames.put("ab", "Bengal, Bay of");
		geographicAreasToFullNames.put("ac", "Asia, Central");
		geographicAreasToFullNames.put("ae", "East Asia");
		geographicAreasToFullNames.put("af", "Thailand, Gulf of");
		geographicAreasToFullNames.put("ag", "Mekong River");
		geographicAreasToFullNames.put("ah", "Himalaya Mountains");
		geographicAreasToFullNames.put("ai", "Indochina");
		geographicAreasToFullNames.put("ak", "Caspian Sea");
		geographicAreasToFullNames.put("am", "Malaya");
		geographicAreasToFullNames.put("an", "East China Sea");
		geographicAreasToFullNames.put("ao", "South China Sea");
		geographicAreasToFullNames.put("aopf", "Paracel Islands");
		geographicAreasToFullNames.put("aoxp", "Spratly Islands");
		geographicAreasToFullNames.put("ap", "Persian Gulf");
		geographicAreasToFullNames.put("ar", "Arabian Peninsula");
		geographicAreasToFullNames.put("as", "Southeast Asia");
		geographicAreasToFullNames.put("at", "Tien Shan");
		geographicAreasToFullNames.put("au", "Arabian Sea");
		geographicAreasToFullNames.put("aw", "Middle East");
		geographicAreasToFullNames.put("awba", "West Bank");
		geographicAreasToFullNames.put("awgz", "Gaza Strip");
		geographicAreasToFullNames.put("-awiu", "Israel-Syria Demilitarized Zones");
		geographicAreasToFullNames.put("-awiw", "Israel-Jordan Demilitarized Zones");
		geographicAreasToFullNames.put("-awiy", "Iraq-Saudi Arabia Neutral Zone");
		geographicAreasToFullNames.put("ay", "Yellow Sea");
		geographicAreasToFullNames.put("az", "South Asia");
		geographicAreasToFullNames.put("b", "Commonwealth countries");
		geographicAreasToFullNames.put("c", "Intercontinental areas (Western Hemisphere)");
		geographicAreasToFullNames.put("cc", "Caribbean Area; Caribbean Sea");
		geographicAreasToFullNames.put("cl", "Latin America");
		geographicAreasToFullNames.put("-cm", "Middle America");
		geographicAreasToFullNames.put("-cr", "Circumcaribbean");
		geographicAreasToFullNames.put("d", "Developing countries");
		geographicAreasToFullNames.put("dd", "Developed countries");
		geographicAreasToFullNames.put("e", "Europe");
		geographicAreasToFullNames.put("e-aa", "Albania");
		geographicAreasToFullNames.put("e-an", "Andorra");
		geographicAreasToFullNames.put("e-au", "Austria");
		geographicAreasToFullNames.put("e-be", "Belgium");
		geographicAreasToFullNames.put("e-bn", "Bosnia and Herzegovina");
		geographicAreasToFullNames.put("e-bu", "Bulgaria");
		geographicAreasToFullNames.put("e-bw", "Belarus");
		geographicAreasToFullNames.put("e-ci", "Croatia");
		geographicAreasToFullNames.put("e-cs", "Czechoslovakia");
		geographicAreasToFullNames.put("e-dk", "Denmark");
		geographicAreasToFullNames.put("e-er", "Estonia");
		geographicAreasToFullNames.put("e-fi", "Finland");
		geographicAreasToFullNames.put("e-fr", "France");
		geographicAreasToFullNames.put("e-ge", "Germany (East)");
		geographicAreasToFullNames.put("e-gi", "Gibraltar");
		geographicAreasToFullNames.put("e-gr", "Greece");
		geographicAreasToFullNames.put("e-gw", "Germany (West)");
		geographicAreasToFullNames.put("e-gx", "Germany");
		geographicAreasToFullNames.put("e-hu", "Hungary");
		geographicAreasToFullNames.put("e-ic", "Iceland");
		geographicAreasToFullNames.put("e-ie", "Ireland");
		geographicAreasToFullNames.put("e-it", "Italy");
		geographicAreasToFullNames.put("e-kv", "Kosovo");
		geographicAreasToFullNames.put("e-lh", "Liechtenstein");
		geographicAreasToFullNames.put("e-li", "Lithuania");
		geographicAreasToFullNames.put("e-lu", "Luxembourg");
		geographicAreasToFullNames.put("e-lv", "Latvia");
		geographicAreasToFullNames.put("e-mc", "Monaco");
		geographicAreasToFullNames.put("e-mm", "Malta");
		geographicAreasToFullNames.put("e-mo", "Montenegro");
		geographicAreasToFullNames.put("e-mv", "Moldova");
		geographicAreasToFullNames.put("e-ne", "Netherlands");
		geographicAreasToFullNames.put("e-no", "Norway");
		geographicAreasToFullNames.put("e-pl", "Poland");
		geographicAreasToFullNames.put("e-po", "Portugal");
		geographicAreasToFullNames.put("e-rb", "Serbia");
		geographicAreasToFullNames.put("e-rm", "Romania");
		geographicAreasToFullNames.put("e-ru", "Russia (Federation)");
		geographicAreasToFullNames.put("e-sm", "San Marino");
		geographicAreasToFullNames.put("e-sp", "Spain");
		geographicAreasToFullNames.put("e-sw", "Sweden");
		geographicAreasToFullNames.put("e-sz", "Switzerland");
		geographicAreasToFullNames.put("e-uk", "Great Britain");
		geographicAreasToFullNames.put("e-uk-en", "England");
		geographicAreasToFullNames.put("e-uk-ni", "Northern Ireland");
		geographicAreasToFullNames.put("e-uk-st", "Scotland");
		geographicAreasToFullNames.put("e-uk-ui", "Great Britain Miscellaneous Island Dependencies");
		geographicAreasToFullNames.put("e-uk-wl", "Wales");
		geographicAreasToFullNames.put("e-un", "Ukraine");
		geographicAreasToFullNames.put("e-ur", "Russia. Russian Empire. Soviet Union. Former Soviet Republics");
		geographicAreasToFullNames.put("-e-ur-ai", "Armenia (Republic)");
		geographicAreasToFullNames.put("-e-ur-aj", "Azerbaijan");
		geographicAreasToFullNames.put("-e-ur-bw", "Belarus");
		geographicAreasToFullNames.put("-e-ur-er", "Estonia");
		geographicAreasToFullNames.put("-e-ur-gs", "Georgia (Republic)");
		geographicAreasToFullNames.put("-e-ur-kg", "Kyrgyzstan");
		geographicAreasToFullNames.put("-e-ur-kz", "Kazakhstan");
		geographicAreasToFullNames.put("-e-ur-li", "Lithuania");
		geographicAreasToFullNames.put("-e-ur-lv", "Latvia");
		geographicAreasToFullNames.put("-e-ur-mv", "Moldova");
		geographicAreasToFullNames.put("-e-ur-ru", "Russia (Federation)");
		geographicAreasToFullNames.put("-e-ur-ta", "Tajikistan");
		geographicAreasToFullNames.put("-e-ur-tk", "Turkmenistan");
		geographicAreasToFullNames.put("-e-ur-un", "Ukraine");
		geographicAreasToFullNames.put("-e-ur-uz", "Uzbekistan");
		geographicAreasToFullNames.put("e-urc", "Central Chernozem Region (Russia)");
		geographicAreasToFullNames.put("e-ure", "Siberia, Eastern (Russia)");
		geographicAreasToFullNames.put("e-urf", "Russian Far East (Russia)");
		geographicAreasToFullNames.put("e-urk", "Caucasus");
		geographicAreasToFullNames.put("-e-url", "Central Region, RSFSR");
		geographicAreasToFullNames.put("e-urn", "Soviet Union, Northwestern");
		geographicAreasToFullNames.put("-e-uro", "Soviet Central Asia");
		geographicAreasToFullNames.put("e-urp", "Volga River (Russia)");
		geographicAreasToFullNames.put("e-urr", "Caucasus, Northern (Russia)");
		geographicAreasToFullNames.put("e-urs", "Siberia (Russia)");
		geographicAreasToFullNames.put("e-uru", "Ural Mountains (Russia)");
		geographicAreasToFullNames.put("-e-urv", "Volgo-Viatskii Region, RSFSR");
		geographicAreasToFullNames.put("e-urw", "Siberia, Western (Russia)");
		geographicAreasToFullNames.put("e-vc", "Vatican City");
		geographicAreasToFullNames.put("e-xn", "Macedonia (Republic)");
		geographicAreasToFullNames.put("e-xo", "Slovakia");
		geographicAreasToFullNames.put("e-xr", "Czech Republic");
		geographicAreasToFullNames.put("e-xv", "Slovenia");
		geographicAreasToFullNames.put("e-yu", "Serbia and Montenegro; Yugoslavia");
		geographicAreasToFullNames.put("ea", "Alps");
		geographicAreasToFullNames.put("eb", "Baltic States");
		geographicAreasToFullNames.put("ec", "Europe, Central");
		geographicAreasToFullNames.put("ed", "Balkan Peninsula");
		geographicAreasToFullNames.put("ee", "Europe, Eastern");
		geographicAreasToFullNames.put("-ei", "Iberian Peninsula");
		geographicAreasToFullNames.put("el", "Benelux countries");
		geographicAreasToFullNames.put("en", "Europe, Northern");
		geographicAreasToFullNames.put("eo", "Danube River");
		geographicAreasToFullNames.put("ep", "Pyrenees");
		geographicAreasToFullNames.put("er", "Rhine River");
		geographicAreasToFullNames.put("es", "Europe, Southern");
		geographicAreasToFullNames.put("-et", "Europe, East Central");
		geographicAreasToFullNames.put("ev", "Scandinavia");
		geographicAreasToFullNames.put("ew", "Europe, Western");
		geographicAreasToFullNames.put("f", "Africa");
		geographicAreasToFullNames.put("f-ae", "Algeria");
		geographicAreasToFullNames.put("f-ao", "Angola");
		geographicAreasToFullNames.put("f-bd", "Burundi");
		geographicAreasToFullNames.put("f-bs", "Botswana");
		geographicAreasToFullNames.put("-f-by", "Biafra");
		geographicAreasToFullNames.put("f-cd", "Chad");
		geographicAreasToFullNames.put("f-cf", "Congo (Brazzaville)");
		geographicAreasToFullNames.put("f-cg", "Congo (Democratic Republic)");
		geographicAreasToFullNames.put("f-cm", "Cameroon");
		geographicAreasToFullNames.put("f-cx", "Central African Republic");
		geographicAreasToFullNames.put("f-dm", "Benin");
		geographicAreasToFullNames.put("f-ea", "Eritrea");
		geographicAreasToFullNames.put("f-eg", "Equatorial Guinea");
		geographicAreasToFullNames.put("f-et", "Ethiopia");
		geographicAreasToFullNames.put("f-ft", "Djibouti");
		geographicAreasToFullNames.put("f-gh", "Ghana");
		geographicAreasToFullNames.put("f-gm", "Gambia");
		geographicAreasToFullNames.put("f-go", "Gabon");
		geographicAreasToFullNames.put("f-gv", "Guinea");
		geographicAreasToFullNames.put("-f-if", "Ifni");
		geographicAreasToFullNames.put("f-iv", "Côte d'Ivoire");
		geographicAreasToFullNames.put("f-ke", "Kenya");
		geographicAreasToFullNames.put("f-lb", "Liberia");
		geographicAreasToFullNames.put("f-lo", "Lesotho");
		geographicAreasToFullNames.put("f-ly", "Libya");
		geographicAreasToFullNames.put("f-mg", "Madagascar");
		geographicAreasToFullNames.put("f-ml", "Mali");
		geographicAreasToFullNames.put("f-mr", "Morocco");
		geographicAreasToFullNames.put("f-mu", "Mauritania");
		geographicAreasToFullNames.put("f-mw", "Malawi");
		geographicAreasToFullNames.put("f-mz", "Mozambique");
		geographicAreasToFullNames.put("f-ng", "Niger");
		geographicAreasToFullNames.put("f-nr", "Nigeria");
		geographicAreasToFullNames.put("f-pg", "Guinea-Bissau");
		geographicAreasToFullNames.put("f-rh", "Zimbabwe");
		geographicAreasToFullNames.put("f-rw", "Rwanda");
		geographicAreasToFullNames.put("f-sa", "South Africa");
		geographicAreasToFullNames.put("f-sd", "South Sudan");
		geographicAreasToFullNames.put("f-sf", "Sao Tome and Principe");
		geographicAreasToFullNames.put("f-sg", "Senegal");
		geographicAreasToFullNames.put("f-sh", "Spanish North Africa");
		geographicAreasToFullNames.put("f-sj", "Sudan");
		geographicAreasToFullNames.put("f-sl", "Sierra Leone");
		geographicAreasToFullNames.put("f-so", "Somalia");
		geographicAreasToFullNames.put("f-sq", "Swaziland");
		geographicAreasToFullNames.put("f-ss", "Western Sahara");
		geographicAreasToFullNames.put("f-sx", "Namibia");
		geographicAreasToFullNames.put("f-tg", "Togo");
		geographicAreasToFullNames.put("f-ti", "Tunisia");
		geographicAreasToFullNames.put("f-tz", "Tanzania");
		geographicAreasToFullNames.put("f-ua", "Egypt");
		geographicAreasToFullNames.put("f-ug", "Uganda");
		geographicAreasToFullNames.put("f-uv", "Burkina Faso");
		geographicAreasToFullNames.put("f-za", "Zambia");
		geographicAreasToFullNames.put("fa", "Atlas Mountains");
		geographicAreasToFullNames.put("fb", "Africa, Sub-Saharan");
		geographicAreasToFullNames.put("fc", "Africa, Central");
		geographicAreasToFullNames.put("fd", "Sahara");
		geographicAreasToFullNames.put("fe", "Africa, Eastern");
		geographicAreasToFullNames.put("ff", "Africa, North");
		geographicAreasToFullNames.put("fg", "Congo River");
		geographicAreasToFullNames.put("fh", "Africa, Northeast");
		geographicAreasToFullNames.put("fi", "Niger River");
		geographicAreasToFullNames.put("fl", "Nile River");
		geographicAreasToFullNames.put("fn", "Sudan (Region)");
		geographicAreasToFullNames.put("fq", "Africa, French-speaking Equatorial");
		geographicAreasToFullNames.put("fr", "Great Rift Valley");
		geographicAreasToFullNames.put("fs", "Africa, Southern");
		geographicAreasToFullNames.put("fu", "Suez Canal (Egypt)");
		geographicAreasToFullNames.put("fv", "Volta River (Ghana)");
		geographicAreasToFullNames.put("fw", "Africa, West");
		geographicAreasToFullNames.put("fz", "Zambezi River");
		geographicAreasToFullNames.put("h", "French Community");
		geographicAreasToFullNames.put("i", "Indian Ocean");
		geographicAreasToFullNames.put("i-bi", "British Indian Ocean Territory");
		geographicAreasToFullNames.put("i-cq", "Comoros");
		geographicAreasToFullNames.put("i-fs", "Terres australes et antarctiques françaises");
		geographicAreasToFullNames.put("i-hm", "Heard and McDonald Islands");
		geographicAreasToFullNames.put("i-mf", "Mauritius");
		geographicAreasToFullNames.put("i-my", "Mayotte");
		geographicAreasToFullNames.put("i-re", "Réunion");
		geographicAreasToFullNames.put("i-se", "Seychelles");
		geographicAreasToFullNames.put("i-xa", "Christmas Island (Indian Ocean)");
		geographicAreasToFullNames.put("i-xb", "Cocos (Keeling) Islands");
		geographicAreasToFullNames.put("i-xc", "Maldives");
		geographicAreasToFullNames.put("-i-xo", "Socotra Island");
		geographicAreasToFullNames.put("l", "Atlantic Ocean");
		geographicAreasToFullNames.put("ln", "North Atlantic Ocean");
		geographicAreasToFullNames.put("lnaz", "Azores");
		geographicAreasToFullNames.put("lnbm", "Bermuda Islands");
		geographicAreasToFullNames.put("lnca", "Canary Islands");
		geographicAreasToFullNames.put("lncv", "Cabo Verde");
		geographicAreasToFullNames.put("lnfa", "Faroe Islands");
		geographicAreasToFullNames.put("lnjn", "Jan Mayen Island");
		geographicAreasToFullNames.put("lnma", "Madeira Islands");
		geographicAreasToFullNames.put("lnsb", "Svalbard (Norway)");
		geographicAreasToFullNames.put("ls", "South Atlantic Ocean");
		geographicAreasToFullNames.put("lsai", "Ascension Island (Atlantic Ocean)");
		geographicAreasToFullNames.put("lsbv", "Bouvet Island");
		geographicAreasToFullNames.put("lsfk", "Falkland Islands");
		geographicAreasToFullNames.put("lstd", "Tristan da Cunha");
		geographicAreasToFullNames.put("lsxj", "Saint Helena");
		geographicAreasToFullNames.put("lsxs", "South Georgia and South Sandwich Islands");
		geographicAreasToFullNames.put("m", "Intercontinental areas (Eastern Hemisphere)");
		geographicAreasToFullNames.put("ma", "Arab countries");
		geographicAreasToFullNames.put("mb", "Black Sea");
		geographicAreasToFullNames.put("me", "Eurasia");
		geographicAreasToFullNames.put("mm", "Mediterranean Region; Mediterranean Sea");
		geographicAreasToFullNames.put("mr", "Red Sea");
		geographicAreasToFullNames.put("n", "North America");
		geographicAreasToFullNames.put("n-cn", "Canada");
		geographicAreasToFullNames.put("n-cn-ab", "Alberta");
		geographicAreasToFullNames.put("n-cn-bc", "British Columbia");
		geographicAreasToFullNames.put("n-cn-mb", "Manitoba");
		geographicAreasToFullNames.put("n-cn-nf", "Newfoundland and Labrador");
		geographicAreasToFullNames.put("n-cn-nk", "New Brunswick");
		geographicAreasToFullNames.put("n-cn-ns", "Nova Scotia");
		geographicAreasToFullNames.put("n-cn-nt", "Northwest Territories");
		geographicAreasToFullNames.put("n-cn-nu", "Nunavut");
		geographicAreasToFullNames.put("n-cn-on", "Ontario");
		geographicAreasToFullNames.put("n-cn-pi", "Prince Edward Island");
		geographicAreasToFullNames.put("n-cn-qu", "Québec (Province)");
		geographicAreasToFullNames.put("n-cn-sn", "Saskatchewan");
		geographicAreasToFullNames.put("n-cn-yk", "Yukon Territory");
		geographicAreasToFullNames.put("n-cnh", "Hudson Bay");
		geographicAreasToFullNames.put("n-cnm", "Maritime Provinces");
		geographicAreasToFullNames.put("n-cnp", "Prairie Provinces");
		geographicAreasToFullNames.put("n-gl", "Greenland");
		geographicAreasToFullNames.put("n-mx", "Mexico");
		geographicAreasToFullNames.put("n-us", "United States");
		geographicAreasToFullNames.put("n-us-ak", "Alaska");
		geographicAreasToFullNames.put("n-us-al", "Alabama");
		geographicAreasToFullNames.put("n-us-ar", "Arkansas");
		geographicAreasToFullNames.put("n-us-az", "Arizona");
		geographicAreasToFullNames.put("n-us-ca", "California");
		geographicAreasToFullNames.put("n-us-co", "Colorado");
		geographicAreasToFullNames.put("n-us-ct", "Connecticut");
		geographicAreasToFullNames.put("n-us-dc", "Washington (D.C.)");
		geographicAreasToFullNames.put("n-us-de", "Delaware");
		geographicAreasToFullNames.put("n-us-fl", "Florida");
		geographicAreasToFullNames.put("n-us-ga", "Georgia");
		geographicAreasToFullNames.put("n-us-hi", "Hawaii");
		geographicAreasToFullNames.put("n-us-ia", "Iowa");
		geographicAreasToFullNames.put("n-us-id", "Idaho");
		geographicAreasToFullNames.put("n-us-il", "Illinois");
		geographicAreasToFullNames.put("n-us-in", "Indiana");
		geographicAreasToFullNames.put("n-us-ks", "Kansas");
		geographicAreasToFullNames.put("n-us-ky", "Kentucky");
		geographicAreasToFullNames.put("n-us-la", "Louisiana");
		geographicAreasToFullNames.put("n-us-ma", "Massachusetts");
		geographicAreasToFullNames.put("n-us-md", "Maryland");
		geographicAreasToFullNames.put("n-us-me", "Maine");
		geographicAreasToFullNames.put("n-us-mi", "Michigan");
		geographicAreasToFullNames.put("n-us-mn", "Minnesota");
		geographicAreasToFullNames.put("n-us-mo", "Missouri");
		geographicAreasToFullNames.put("n-us-ms", "Mississippi");
		geographicAreasToFullNames.put("n-us-mt", "Montana");
		geographicAreasToFullNames.put("n-us-nb", "Nebraska");
		geographicAreasToFullNames.put("n-us-nc", "North Carolina");
		geographicAreasToFullNames.put("n-us-nd", "North Dakota");
		geographicAreasToFullNames.put("n-us-nh", "New Hampshire");
		geographicAreasToFullNames.put("n-us-nj", "New Jersey");
		geographicAreasToFullNames.put("n-us-nm", "New Mexico");
		geographicAreasToFullNames.put("n-us-nv", "Nevada");
		geographicAreasToFullNames.put("n-us-ny", "New York");
		geographicAreasToFullNames.put("n-us-oh", "Ohio");
		geographicAreasToFullNames.put("n-us-ok", "Oklahoma");
		geographicAreasToFullNames.put("n-us-or", "Oregon");
		geographicAreasToFullNames.put("n-us-pa", "Pennsylvania");
		geographicAreasToFullNames.put("n-us-ri", "Rhode Island");
		geographicAreasToFullNames.put("n-us-sc", "South Carolina");
		geographicAreasToFullNames.put("n-us-sd", "South Dakota");
		geographicAreasToFullNames.put("n-us-tn", "Tennessee");
		geographicAreasToFullNames.put("n-us-tx", "Texas");
		geographicAreasToFullNames.put("n-us-ut", "Utah");
		geographicAreasToFullNames.put("n-us-va", "Virginia");
		geographicAreasToFullNames.put("n-us-vt", "Vermont");
		geographicAreasToFullNames.put("n-us-wa", "Washington (State)");
		geographicAreasToFullNames.put("n-us-wi", "Wisconsin");
		geographicAreasToFullNames.put("n-us-wv", "West Virginia");
		geographicAreasToFullNames.put("n-us-wy", "Wyoming");
		geographicAreasToFullNames.put("n-usa", "Appalachian Mountains");
		geographicAreasToFullNames.put("n-usc", "Middle West");
		geographicAreasToFullNames.put("n-use", "Northeastern States");
		geographicAreasToFullNames.put("n-usl", "Middle Atlantic States");
		geographicAreasToFullNames.put("n-usm", "Mississippi River");
		geographicAreasToFullNames.put("n-usn", "New England");
		geographicAreasToFullNames.put("n-uso", "Ohio River");
		geographicAreasToFullNames.put("n-usp", "West (U.S.)");
		geographicAreasToFullNames.put("n-usr", "East (U.S.)");
		geographicAreasToFullNames.put("n-uss", "Missouri River");
		geographicAreasToFullNames.put("n-ust", "Southwest, New");
		geographicAreasToFullNames.put("n-usu", "Southern States");
		geographicAreasToFullNames.put("-n-usw", "Northwest (U.S.)");
		geographicAreasToFullNames.put("n-xl", "Saint Pierre and Miquelon");
		geographicAreasToFullNames.put("nc", "Central America");
		geographicAreasToFullNames.put("ncbh", "Belize");
		geographicAreasToFullNames.put("nccr", "Costa Rica");
		geographicAreasToFullNames.put("nccz", "Canal Zone");
		geographicAreasToFullNames.put("nces", "El Salvador");
		geographicAreasToFullNames.put("ncgt", "Guatemala");
		geographicAreasToFullNames.put("ncho", "Honduras");
		geographicAreasToFullNames.put("ncnq", "Nicaragua");
		geographicAreasToFullNames.put("ncpn", "Panama");
		geographicAreasToFullNames.put("nl", "Great Lakes (North America); Lake States");
		geographicAreasToFullNames.put("nm", "Mexico, Gulf of");
		geographicAreasToFullNames.put("np", "Great Plains");
		geographicAreasToFullNames.put("nr", "Rocky Mountains");
		geographicAreasToFullNames.put("nw", "West Indies");
		geographicAreasToFullNames.put("nwaq", "Antigua and Barbuda");
		geographicAreasToFullNames.put("nwaw", "Aruba");
		geographicAreasToFullNames.put("nwbb", "Barbados");
		geographicAreasToFullNames.put("-nwbc", "Barbuda");
		geographicAreasToFullNames.put("nwbf", "Bahamas");
		geographicAreasToFullNames.put("nwbn", "Bonaire");
		geographicAreasToFullNames.put("nwcj", "Cayman Islands");
		geographicAreasToFullNames.put("nwco", "Curaçao");
		geographicAreasToFullNames.put("nwcu", "Cuba");
		geographicAreasToFullNames.put("nwdq", "Dominica");
		geographicAreasToFullNames.put("nwdr", "Dominican Republic");
		geographicAreasToFullNames.put("nweu", "Sint Eustatius");
		geographicAreasToFullNames.put("-nwga", "Greater Antilles");
		geographicAreasToFullNames.put("nwgd", "Grenada");
		geographicAreasToFullNames.put("nwgp", "Guadeloupe");
		geographicAreasToFullNames.put("-nwgs", "Grenadines");
		geographicAreasToFullNames.put("nwhi", "Hispaniola");
		geographicAreasToFullNames.put("nwht", "Haiti");
		geographicAreasToFullNames.put("nwjm", "Jamaica");
		geographicAreasToFullNames.put("nwla", "Antilles, Lesser");
		geographicAreasToFullNames.put("nwli", "Leeward Islands (West Indies)");
		geographicAreasToFullNames.put("nwmj", "Montserrat");
		geographicAreasToFullNames.put("nwmq", "Martinique");
		geographicAreasToFullNames.put("-nwna", "Netherlands Antilles");
		geographicAreasToFullNames.put("nwpr", "Puerto Rico");
		geographicAreasToFullNames.put("-nwsb", "Saint-Barthélemy");
		geographicAreasToFullNames.put("nwsc", "Saint-Barthélemy");
		geographicAreasToFullNames.put("nwsd", "Saba");
		geographicAreasToFullNames.put("nwsn", "Sint Maarten");
		geographicAreasToFullNames.put("nwst", "Saint-Martin");
		geographicAreasToFullNames.put("nwsv", "Swan Islands (Honduras)");
		geographicAreasToFullNames.put("nwtc", "Turks and Caicos Islands");
		geographicAreasToFullNames.put("nwtr", "Trinidad and Tobago");
		geographicAreasToFullNames.put("nwuc", "United States Miscellaneous Caribbean Islands");
		geographicAreasToFullNames.put("nwvb", "British Virgin Islands");
		geographicAreasToFullNames.put("nwvi", "Virgin Islands of the United States");
		geographicAreasToFullNames.put("-nwvr", "Virgin Islands");
		geographicAreasToFullNames.put("nwwi", "Windward Islands (West Indies)");
		geographicAreasToFullNames.put("nwxa", "Anguilla");
		geographicAreasToFullNames.put("nwxi", "Saint Kitts and Nevis");
		geographicAreasToFullNames.put("nwxk", "Saint Lucia");
		geographicAreasToFullNames.put("nwxm", "Saint Vincent and the Grenadines");
		geographicAreasToFullNames.put("p", "Pacific Ocean");
		geographicAreasToFullNames.put("pn", "North Pacific Ocean");
		geographicAreasToFullNames.put("po", "Oceania");
		geographicAreasToFullNames.put("poas", "American Samoa");
		geographicAreasToFullNames.put("pobp", "Solomon Islands");
		geographicAreasToFullNames.put("poci", "Caroline Islands");
		geographicAreasToFullNames.put("-pocp", "Canton and Enderbury Islands");
		geographicAreasToFullNames.put("pocw", "Cook Islands");
		geographicAreasToFullNames.put("poea", "Easter Island");
		geographicAreasToFullNames.put("pofj", "Fiji");
		geographicAreasToFullNames.put("pofp", "French Polynesia");
		geographicAreasToFullNames.put("pogg", "Galapagos Islands");
		geographicAreasToFullNames.put("-pogn", "Gilbert and Ellice Islands");
		geographicAreasToFullNames.put("pogu", "Guam");
		geographicAreasToFullNames.put("poji", "Johnston Island");
		geographicAreasToFullNames.put("pokb", "Kiribati");
		geographicAreasToFullNames.put("poki", "Kermadec Islands");
		geographicAreasToFullNames.put("poln", "Line Islands");
		geographicAreasToFullNames.put("pome", "Melanesia");
		geographicAreasToFullNames.put("pomi", "Micronesia (Federated States)");
		geographicAreasToFullNames.put("ponl", "New Caledonia");
		geographicAreasToFullNames.put("ponn", "Vanuatu");
		geographicAreasToFullNames.put("ponu", "Nauru");
		geographicAreasToFullNames.put("popc", "Pitcairn Island");
		geographicAreasToFullNames.put("popl", "Palau");
		geographicAreasToFullNames.put("pops", "Polynesia");
		geographicAreasToFullNames.put("-pory", "Ryukyu Islands, Southern");
		geographicAreasToFullNames.put("-posc", "Santa Cruz Islands");
		geographicAreasToFullNames.put("posh", "Samoan Islands");
		geographicAreasToFullNames.put("-posn", "Solomon Islands");
		geographicAreasToFullNames.put("potl", "Tokelau");
		geographicAreasToFullNames.put("poto", "Tonga");
		geographicAreasToFullNames.put("pott", "Micronesia");
		geographicAreasToFullNames.put("potv", "Tuvalu");
		geographicAreasToFullNames.put("poup", "United States Miscellaneous Pacific Islands");
		geographicAreasToFullNames.put("powf", "Wallis and Futuna Islands");
		geographicAreasToFullNames.put("powk", "Wake Island");
		geographicAreasToFullNames.put("pows", "Samoa");
		geographicAreasToFullNames.put("poxd", "Mariana Islands");
		geographicAreasToFullNames.put("poxe", "Marshall Islands");
		geographicAreasToFullNames.put("poxf", "Midway Islands");
		geographicAreasToFullNames.put("poxh", "Niue");
		geographicAreasToFullNames.put("ps", "South Pacific Ocean");
		geographicAreasToFullNames.put("q", "Cold regions");
		geographicAreasToFullNames.put("r", "Arctic Ocean; Arctic regions");
		geographicAreasToFullNames.put("s", "South America");
		geographicAreasToFullNames.put("s-ag", "Argentina");
		geographicAreasToFullNames.put("s-bl", "Brazil");
		geographicAreasToFullNames.put("s-bo", "Bolivia");
		geographicAreasToFullNames.put("s-ck", "Colombia");
		geographicAreasToFullNames.put("s-cl", "Chile");
		geographicAreasToFullNames.put("s-ec", "Ecuador");
		geographicAreasToFullNames.put("s-fg", "French Guiana");
		geographicAreasToFullNames.put("s-gy", "Guyana");
		geographicAreasToFullNames.put("s-pe", "Peru");
		geographicAreasToFullNames.put("s-py", "Paraguay");
		geographicAreasToFullNames.put("s-sr", "Suriname");
		geographicAreasToFullNames.put("s-uy", "Uruguay");
		geographicAreasToFullNames.put("s-ve", "Venezuela");
		geographicAreasToFullNames.put("sa", "Amazon River");
		geographicAreasToFullNames.put("sn", "Andes");
		geographicAreasToFullNames.put("sp", "Rio de la Plata (Argentina and Uruguay)");
		geographicAreasToFullNames.put("t", "Antarctic Ocean; Antarctica");
		geographicAreasToFullNames.put("-t-ay", "Antarctica");
		geographicAreasToFullNames.put("u", "Australasia");
		geographicAreasToFullNames.put("u-ac", "Ashmore and Cartier Islands");
		geographicAreasToFullNames.put("u-at", "Australia");
		geographicAreasToFullNames.put("u-at-ac", "Australian Capital Territory");
		geographicAreasToFullNames.put("u-atc", "Central Australia");
		geographicAreasToFullNames.put("u-ate", "Eastern Australia");
		geographicAreasToFullNames.put("u-atn", "Northern Australia");
		geographicAreasToFullNames.put("u-at-ne", "New South Wales");
		geographicAreasToFullNames.put("u-at-no", "Northern Territory");
		geographicAreasToFullNames.put("u-at-qn", "Queensland");
		geographicAreasToFullNames.put("u-at-sa", "South Australia");
		geographicAreasToFullNames.put("u-at-tm", "Tasmania");
		geographicAreasToFullNames.put("u-at-vi", "Victoria");
		geographicAreasToFullNames.put("u-at-we", "Western Australia");
		geographicAreasToFullNames.put("u-cs", "Coral Sea Islands");
		geographicAreasToFullNames.put("u-nz", "New Zealand");
		geographicAreasToFullNames.put("-v", "Communist countries");
		geographicAreasToFullNames.put("w", "Tropics");
		geographicAreasToFullNames.put("x", "Earth");
		geographicAreasToFullNames.put("xa", "Eastern Hemisphere");
		geographicAreasToFullNames.put("xb", "Northern Hemisphere");
		geographicAreasToFullNames.put("xc", "Southern Hemisphere");
		geographicAreasToFullNames.put("xd", "Western Hemisphere");
		geographicAreasToFullNames.put("zd", "Deep space");
		geographicAreasToFullNames.put("zju", "Jupiter");
		geographicAreasToFullNames.put("zma", "Mars");
		geographicAreasToFullNames.put("zme", "Mercury");
		geographicAreasToFullNames.put("zmo", "Moon");
		geographicAreasToFullNames.put("zne", "Neptune");
		geographicAreasToFullNames.put("zo", "Outer space");
		geographicAreasToFullNames.put("zpl", "Pluto");
		geographicAreasToFullNames.put("zs", "Solar system");
		geographicAreasToFullNames.put("zsa", "Saturn");
		geographicAreasToFullNames.put("zsu", "Sun");
		geographicAreasToFullNames.put("zur", "Uranus");
		geographicAreasToFullNames.put("zve", "Venus");
		
		return geographicAreasToFullNames;
	}
	
	public static ConcurrentHashMap<String, ArrayList<String>> getHostStringsToRelatedHostStrings() {
		if(new File(HrwaManager.relatedHostsFile).exists()) {
			try {
				return relatedHostsFromReader(new FileReader(HrwaManager.relatedHostsFile));
			} catch (FileNotFoundException e) {
				HrwaManager.logger.error("Unable to find related hosts CSV file at: " + HrwaManager.relatedHostsFile + "\n" + e.getMessage());
				e.printStackTrace();
				System.exit(HrwaManager.EXIT_CODE_ERROR);
			}
		}
		return new ConcurrentHashMap<String, ArrayList<String>>();
	}
	
	/**
	 * This method is only here for unit testing purposes.
	 * @throws UnsupportedEncodingException 
	 */
	public static void overrideRelatedHostsDataFromStreamSource(InputStream relatedHostsCsvInputStream) throws UnsupportedEncodingException {
		SiteData.hostStringsToRelatedHostStrings = relatedHostsFromReader(new InputStreamReader(relatedHostsCsvInputStream, "UTF-8"));
	}
	
	public static ConcurrentHashMap<String, ArrayList<String>> relatedHostsFromReader(Reader reader) {
		ConcurrentHashMap<String, ArrayList<String>> hostStringsToRelatedHostStrings = new ConcurrentHashMap<String, ArrayList<String>>();
		
		CSVReader csvReader;
		try {
			csvReader = new CSVReader(reader);
			String [] csvRow;
		     while ((csvRow = csvReader.readNext()) != null) {
		        if(csvRow[0].equals("seed")) {
		        	//Skip column heading row
		        	continue;
		        }
		        
		        String keyHostString = MetadataUtils.extractHostString(csvRow[0]);
		        String valueHostString = MetadataUtils.extractHostString(csvRow[1]);
		        
		    	if( ! hostStringsToRelatedHostStrings.containsKey(keyHostString) ) {
		    		hostStringsToRelatedHostStrings.put(keyHostString, new ArrayList<String>());
		    	}
		    	hostStringsToRelatedHostStrings.get(keyHostString).add(valueHostString);
		     }
		     csvReader.close();
		} catch (IOException e) {
			HrwaManager.logger.error("IOException encountered while trying to read related hosts CSV file at: " + HrwaManager.relatedHostsFile + "\n" + e.getMessage());
			e.printStackTrace();
			System.exit(HrwaManager.EXIT_CODE_ERROR);
		}
		return hostStringsToRelatedHostStrings;
	}
	

}
