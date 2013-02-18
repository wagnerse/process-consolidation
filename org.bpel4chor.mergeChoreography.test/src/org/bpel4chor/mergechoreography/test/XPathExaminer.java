package org.bpel4chor.mergechoreography.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bpel4chor.mergechoreography.util.XPathUtil;

public class XPathExaminer {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String pattern = Pattern.quote("$myVar") + "(?!\\w{1})" + "\\W??"; // [[\\W?]|[^\\w{1}]]
		String s = "//doc[contains(., $myVar) $myVar]/*/text($myVar/hallo) $myVar.test $myVar1 $myVar $myVar=$myVar";
		String sVarNames = "//doc[contains(., $myVar1) $Test__Var]/*/text($halloVar3/hallo) $shitVarblablublib.test $hihihiVar233423 $joBitch $myVar=$myVar";
		
		String newvar = "newVar";
		
		StringBuilder newStr = new StringBuilder();
		int rLast = 0;
		for (MatchResult r : XPathExaminer.findMatches(pattern, s)) {
			System.out.println(r.group() + " von " + r.start() + " bis " + r.end());
			newStr.append(s.substring(rLast, r.start()));
			newStr.append("$" + newvar);
			// newStr.append(s.charAt(r.end()/* - 1 */));
			rLast = r.end();
		}
		if (rLast < s.length()) {
			newStr.append(s.substring(rLast, s.length()));
		}
		
		System.out.println("" + s);
		System.out.println("" + newStr);
		System.out.println("" + s.replaceAll(Pattern.quote("$myVar") + "(?!\\w{1})\\W??", "\\$newVar"));
		
		String prop = "bpel:getVariableProperty(\"varA\",\"b:propB\") bpel:getVariableProperty('varA','b:propB')";
		String varNameOrig = "varA";
		String varNameNew = "varNew";
		String replProp = prop.replaceAll("bpel:getVariableProperty\\(\"" + varNameOrig + "\"", "bpel:getVariableProperty(\"" + varNameNew + "\"");
		replProp = replProp.replaceAll("bpel:getVariableProperty\\('" + varNameOrig + "'", "bpel:getVariableProperty('" + varNameNew + "'");
		System.out.println(prop);
		System.out.println(replProp);
		
		// New for testing
		System.out.println(XPathUtil.replaceVariableName(s, "myVar", "newVar"));
		System.out.println(XPathUtil.replaceVariableName(prop, "varA", "varB"));
		
		List<String> varNames = XPathUtil.getUsedVariabeNames(sVarNames);
		String searchName = "halloVar3";
		System.out.println("varNames contains halloVar3 : " + varNames.contains("halloVar3"));
		
		// String pattern2 = Pattern.quote("$") + "(\\w+|_+)" + "(?!\\w{1})" +
		// "\\W??";
		String pattern2 = "bpel:getVariableProperty\\(\"(\\w+|_+)\"";
		String pattern3 = "bpel:getVariableProperty\\('(\\w+|_+)'";
		// "bpel:getVariableProperty\\(\"" + varNameOrig + "\""
		
		for (MatchResult r : XPathExaminer.findMatches(pattern2, prop)) {
			System.out.println(r.group() + " von " + r.start() + " bis " + r.end());
			System.out.println("Variable ist : " + r.group().indexOf("\"") + ", " + r.group().lastIndexOf("\""));
			System.out.println("Variable ist : " + r.group().substring(r.group().indexOf("\"") + 1, r.group().lastIndexOf("\"")));
		}
		
		for (MatchResult r : XPathExaminer.findMatches(pattern3, prop)) {
			System.out.println(r.group() + " von " + r.start() + " bis " + r.end());
			System.out.println("Variable ist : " + r.group().indexOf("'") + ", " + r.group().lastIndexOf("'"));
			System.out.println("Variable ist : " + r.group().substring(r.group().indexOf("'") + 1, r.group().lastIndexOf("'")));
		}
	}
	
	static Iterable<MatchResult> findMatches(String pattern, CharSequence s) {
		List<MatchResult> results = new ArrayList<MatchResult>();
		
		for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
			results.add(m.toMatchResult());
		}
		
		return results;
	}
}
