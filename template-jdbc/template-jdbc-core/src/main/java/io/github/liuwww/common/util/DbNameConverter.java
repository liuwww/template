package io.github.liuwww.common.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class DbNameConverter {
	public static String convert(String dbname) {
		String name = "";
		StringTokenizer st = new StringTokenizer(dbname, "_");
		if (!st.hasMoreTokens()) {
			throw new RuntimeException("name is null");
		}
		name += st.nextToken().toLowerCase();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.length() == 1) {
				name += token.toUpperCase(java.util.Locale.ROOT);
			} else {
				String first = token.substring(0, 1);
				String others = token.substring(1, token.length());
				name += (first.toUpperCase(java.util.Locale.ROOT) + others.toLowerCase());
			}

		}
		return name;
	}

	public static String convertToDb(String voname) {
		String name = "";
		ArrayList<Integer> positions = new ArrayList<Integer>();
		positions.add(0);
		char[] letters = voname.toCharArray();
		for (int i = 0; i < letters.length; i++) {
			char letter = letters[i];
			if (Character.isUpperCase(letter)) { // an uppercase letter
				positions.add(i);
			}
		}
		positions.add(letters.length);
		for (int j = 1; j < positions.size(); j++) {
			int from = (positions.get(j - 1)).intValue();
			int to = (positions.get(j)).intValue();
			name += voname.substring(from, to).toUpperCase(java.util.Locale.ROOT) + "_";
		}
		return name.substring(0, name.length() - 1);
	}

	public static String setUpperCaseForFirstLetter(String name) {
		if (name.length() == 1) {
			return name.toUpperCase(java.util.Locale.ROOT);
		}
		String firstLetter = name.substring(0, 1);
		String others = name.substring(1, name.length());
		return firstLetter.toUpperCase(java.util.Locale.ROOT) + others;
	}

	public static String setLowerCaseForFirstLetter(String name) {
		if (name.length() == 1) {
			return name.toLowerCase();
		}
		String firstLetter = name.substring(0, 1);
		String others = name.substring(1, name.length());
		return firstLetter.toLowerCase() + others;
	}
}
