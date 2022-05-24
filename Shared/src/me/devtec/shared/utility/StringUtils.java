package me.devtec.shared.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.devtec.shared.API;
import me.devtec.shared.Ref;

public class StringUtils {

	// INIT THIS

	public static ColormaticFactory color;
	public static Pattern rainbowSplit;
	// TIME UTILS
	public static Pattern sec;
	public static Pattern min;
	public static Pattern hour;
	public static Pattern day;
	public static Pattern week;
	public static Pattern mon;
	public static Pattern year;
	public static final Map<String, List<String>> actions = new ConcurrentHashMap<>();
	// COLOR UTILS
	public static Pattern gradientFinder;

	// VARRIABLE INIT
	public static Map<String, String> colorMap = new ConcurrentHashMap<>();
	public static String tagPrefix = "!";
	public static String timeSplit = ":";
	public static String timeFormat = "%time% %format%";

	// DO NOT TOUCH

	public static final Random random = new Random();
	// SPECIAL CHARS
	private static final Pattern special = Pattern.compile("[^A-Z-a-z0-9_]+");
	// CALCULATOR
	private static final Pattern extra = Pattern.compile("((^[-])?[ ]*[0-9.]+)[ ]*([*/])[ ]*(-?[ ]*[0-9.]+)");
	private static final Pattern normal = Pattern.compile("((^[-])?[ ]*[0-9.]+)[ ]*([+-])[ ]*(-?[ ]*[0-9.]+)");

	public interface ColormaticFactory {

		/**
		 * @apiNote Generates random color depends on software & version
		 */
		public String generateColor();

		/**
		 * @apiNote @see {@link API#basics()}
		 */
		public String[] getLastColors(String text);

		/**
		 * @apiNote Replace #RRGGBB hex color depends on software
		 */
		public String replaceHex(String msg);

		/**
		 * @apiNote @see {@link API#basics()}
		 */
		public String gradient(String msg, String fromHex, String toHex);

		/**
		 * @apiNote @see {@link API#basics()}
		 */
		public String rainbow(String msg, String generateColor, String generateColor2);
	}

	public enum FormatType {
		BASIC, // Basic format - xxx.xx
		NORMAL, // Improved BASIS format - xxx,xxx.xx
		COMPLEX // NORMAL format + balance type
	}

	public static String formatDouble(FormatType type, double value) {
		switch (type) {
		case BASIC: {
			String formatted = String.format(Locale.ENGLISH, "%.2f", value);
			if (formatted.endsWith("00"))
				formatted = formatted.substring(0, formatted.length() - 3); // .00
			if (formatted.endsWith("0"))
				formatted = formatted.substring(0, formatted.length() - 1); // .X0
			return formatted;
		}
		case NORMAL: {
			String formatted = String.format(Locale.ENGLISH, "%,.2f", value);
			if (formatted.endsWith("00"))
				formatted = formatted.substring(0, formatted.length() - 3); // .00
			if (formatted.endsWith("0"))
				formatted = formatted.substring(0, formatted.length() - 1); // .X0
			return formatted;
		}
		case COMPLEX: {
			String formatted = String.format(Locale.ENGLISH, "%,.2f", value);
			String[] s = formatted.split(",");
			if (s.length >= 22) { // Why?...
				if (formatted.startsWith("-"))
					return "-∞";
				return "∞";
			}
			if (s.length >= 21)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E60) + "NOV";
			if (s.length >= 20)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E57) + "OCT";
			if (s.length >= 19)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E54) + "SEP";
			if (s.length >= 18)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E51) + "SED";
			if (s.length >= 17)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E48) + "QUI";
			if (s.length >= 16)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E45) + "QUA";
			if (s.length >= 15)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E42) + "tre";
			if (s.length >= 14)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E39) + "duo";
			if (s.length >= 13)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E36) + "und";
			if (s.length >= 12)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E33) + "dec";
			if (s.length >= 11)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E30) + "non";
			if (s.length >= 10)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E27) + "oct";
			if (s.length >= 9)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E24) + "sep";
			if (s.length >= 8) // No, it's not "sex"...
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E21) + "sex";
			if (s.length >= 7)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E18) + "qui";
			if (s.length >= 6)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E15) + "qua";
			if (s.length >= 5)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E12) + "t";
			if (s.length >= 4)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1.0E9) + "b";
			if (s.length >= 3)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1000000) + "m";
			if (s.length >= 2)
				return StringUtils.formatDouble(FormatType.NORMAL, value / 1000) + "k";
			return formatted;
		}
		default:
			break;
		}
		return value + "";
	}

	/**
	 * @apiNote Generate random int within limits
	 * @param max Maximum int (defaulty {@link Integer#MAX_VALUE}
	 */
	public static int generateRandomInt(int max) {
		return StringUtils.generateRandomInt(0, max);
	}

	/**
	 * @apiNote Generate random double within limits
	 * @param max Maximum double (defaulty {@link Double#MAX_VALUE}
	 */
	public static double generateRandomDouble(double max) {
		return StringUtils.generateRandomDouble(0, max);
	}

	/**
	 * @apiNote Generate random double within limits
	 * @param min Minimum double (defaulty 0)
	 * @param max Maximum double (defaulty {@link Double#MAX_VALUE}
	 * @return double
	 */
	public static double generateRandomDouble(double min, double max) {
		if (min == max)
			return min;
		double result = StringUtils.generateRandomInt((int) min, (int) max) + StringUtils.random.nextDouble();
		if (result > max)
			return max;
		return result;
	}

	/**
	 * @apiNote Generate random int within limits
	 * @param min Minimum int (defaulty 0)
	 * @param max Maximum int (defaulty {@link Integer#MAX_VALUE}
	 * @return int
	 */
	public static int generateRandomInt(int min, int max) {
		if (min == max)
			return min;
		return StringUtils.random.nextInt(max - min) + min;
	}

	/**
	 * @apiNote Split text correctly with colors
	 */
	public static List<String> fixedSplit(String text, int lengthOfSplit) {
		if (text == null)
			return null;
		List<String> splitted = new ArrayList<>();
		String split = text;
		String prefix = "";
		while (split.length() > lengthOfSplit) {
			int length = lengthOfSplit - 1 - prefix.length();
			String a = prefix + split.substring(0, length);
			if (a.endsWith("§")) {
				--length;
				a = prefix + split.substring(0, length);
			}
			String[] last = StringUtils.getLastColorsSplitFormats(a);
			prefix = (!last[0].isEmpty() ? "§" + last[0] : "") + (!last[1].isEmpty() ? "§" + last[1] : "");
			splitted.add(a);
			split = split.substring(length);
		}
		if (!(prefix + split).isEmpty())
			splitted.add(prefix + split);
		return splitted;
	}

	/**
	 * @apiNote Copy matches of String from Iterable<String>
	 * @return List<String>
	 */
	public static List<String> copyPartialMatches(String prefix, Iterable<String> originals) {
		List<String> collection = new ArrayList<>();
		for (String string : originals)
			if (string == null
					|| string.length() >= prefix.length() && (string.regionMatches(true, 0, prefix, 0, prefix.length())
							|| string.regionMatches(true, 1, prefix, 0, prefix.length())))
				collection.add(string);
		return collection;
	}

	/**
	 * @apiNote Copy matches of String from Iterable<String>
	 * @return List<String>
	 */
	public static List<String> copySortedPartialMatches(String prefix, Iterable<String> originals) {
		List<String> collection = StringUtils.copyPartialMatches(prefix, originals);
		Collections.sort(collection);
		return collection;
	}

	/**
	 * @apiNote Join Iterable into one String with split {@value split} @see
	 *          {@link StringUtils#join(Iterable<?>, String, int, int)}
	 * @param split Split string (defaulty ' ')
	 * @param args  Arguments
	 * @return String
	 */
	public static String join(Iterable<?> args, String split) {
		return StringUtils.join(args, split, 0, -1);
	}

	/**
	 * @apiNote Join Iterable into one String with split {@value split} @see
	 *          {@link StringUtils#join(Iterable<?>, String, int, int)}
	 * @param split Split string (defaulty ' ')
	 * @param start Start argument (defaulty 0)
	 * @param args  Arguments
	 * @return String
	 */
	public static String join(Iterable<?> args, String split, int start) {
		return StringUtils.join(args, split, start, -1);
	}

	/**
	 * @apiNote Join Iterable into one String with split {@value split} @see
	 *          {@link StringUtils#join(Iterable<?>, String, int, int)}
	 * @param split Split string (defaulty ' ')
	 * @param start Start argument (defaulty 0)
	 * @param end   Last argument (defaultly -1)
	 * @param args  Arguments
	 * @return String
	 */
	public static String join(Iterable<?> args, String split, int start, int end) {
		if (args == null || split == null)
			return null;
		StringBuilder msg = new StringBuilder();
		Iterator<?> iterator = args.iterator();
		for (int i = start; iterator.hasNext() && (end == -1 || i < end); ++i) {
			if (msg.length() != 0)
				msg.append(split);
			msg.append(iterator.next());
		}
		return msg.toString();
	}

	/**
	 * @apiNote Join objects into one String with split {@value split} @see
	 *          {@link StringUtils#join(Object[], String, int, int)}
	 * @param split Split string (defaulty ' ')
	 * @param args  Arguments
	 * @return String
	 */
	public static String join(Object[] args, String split) {
		return StringUtils.join(args, split, 0, args.length);
	}

	/**
	 * @apiNote Join objects into one String with split {@value split} @see
	 *          {@link StringUtils#join(Object[], String, int, int)}
	 * @param split Split string (defaulty ' ')
	 * @param start Start argument (defaulty 0)
	 * @param args  Arguments
	 * @return String
	 */
	public static String join(Object[] args, String split, int start) {
		return StringUtils.join(args, split, start, args.length);
	}

	/**
	 * @apiNote Join objects into one String with split {@value split} @see
	 *          {@link StringUtils#join(Object[], String, int, int)}
	 * @param split Split string (defaulty ' ')
	 * @param start Start argument (defaulty 0)
	 * @param end   Last argument (defaultly args.length)
	 * @param args  Arguments
	 * @return String
	 */
	public static String join(Object[] args, String split, int start, int end) {
		if (args == null || split == null)
			return null;
		StringBuilder msg = new StringBuilder();
		for (int i = start; i < args.length && i < end; ++i) {
			if (msg.length() != 0)
				msg.append(split);
			msg.append(args[i]);
		}
		return msg.toString();
	}

	/**
	 * @apiNote Return joined strings ([0] + [1]) from
	 *          {@link StringUtils#getLastColorsSplitFormats(String)}
	 * @param text Input string
	 * @return String
	 */
	public static String getLastColors(String text) {
		String[] split = StringUtils.color.getLastColors(text);
		return split[0] + split[1];
	}

	/**
	 * @apiNote Get last colors from String (HEX SUPPORT!)
	 * @param text Input string
	 * @return String[]
	 */
	public static String[] getLastColorsSplitFormats(String text) {
		return StringUtils.color.getLastColors(text);
	}

	/**
	 * @apiNote Replace gradients in the List of strings
	 * @param list Input list of strings to colorize
	 * @return List<String>
	 */
	public static List<String> gradient(List<String> list) {
		list.replaceAll(StringUtils::gradient);
		return list;
	}

	/**
	 * @apiNote Replace gradients in the String
	 * @param originalMsg Input string to colorize
	 * @return String
	 */
	public static String gradient(String originalMsg) {
		if (originalMsg == null || StringUtils.gradientFinder == null)
			return originalMsg;

		String legacyMsg = originalMsg;

		String low = legacyMsg.toLowerCase();
		for (Entry<String, String> code : StringUtils.colorMap.entrySet()) {
			String rawCode = (StringUtils.tagPrefix + code.getKey()).toLowerCase();
			if (!low.contains(rawCode))
				continue;
			legacyMsg = legacyMsg.replace(rawCode, code.getValue());
		}
		Matcher matcher = StringUtils.gradientFinder.matcher(legacyMsg);
		while (matcher.find()) {
			if (matcher.groupCount() == 0 || matcher.group().isEmpty())
				continue;
			String replace = StringUtils.color.gradient(matcher.group(2), matcher.group(1), matcher.group(3));
			if (replace == null)
				continue;
			legacyMsg = legacyMsg.replace(matcher.group(), replace);
		}
		return legacyMsg;
	}

	/**
	 * @apiNote Colorize List of strings with colors
	 * @param list Texts to colorize
	 * @return List<String>
	 */
	public static List<String> colorize(List<String> list) {
		list.replaceAll(StringUtils::colorize);
		return list;
	}

	/**
	 * @apiNote Colorize string with colors
	 * @param original Text to colorize
	 * @return String
	 */
	public static String colorize(String original) {
		if (original == null || original.trim().isEmpty())
			return original;

		String msg = original;

		if (StringUtils.color != null && /** Fast check for working #RRGGBB symbol **/
				(!Ref.serverType().isBukkit() || Ref.isNewerThan(15))) {
			msg = StringUtils.gradient(msg);
			if (msg.contains("#"))
				msg = StringUtils.color.replaceHex(msg);
		}
		char[] b = msg.toCharArray();
		for (int i = 0; i < b.length - 1; i++)
			if (b[i] == '&' && StringUtils.has(b[i + 1])) {
				b[i] = '§';
				b[i + 1] = StringUtils.lower(b[i + 1]);
			}
		msg = new String(b);
		if (msg.contains("&u"))
			msg = StringUtils.color.rainbow(msg, StringUtils.color.generateColor(), StringUtils.color.generateColor());
		return msg;
	}

	private static boolean has(int c) {
		return c <= 102 && c >= 97 || c <= 57 && c >= 48 || c <= 70 && c >= 65 || c <= 79 && c >= 75
				|| c <= 111 && c >= 107 || c == 114 || c == 82 || c == 88 || c == 120;
	}

	private static char lower(int c) {
		switch (c) {
		case 65:
		case 66:
		case 67:
		case 68:
		case 69:
		case 70:
		case 75:
		case 76:
		case 77:
		case 78:
		case 79:
		case 82:
			return (char) (c + 32);
		case 120:
			return (char) 88;
		default:
			return (char) c;
		}
	}

	/**
	 * @apiNote Join strings to one String with split ' ' @see
	 *          {@link StringUtils#join(Object[], String, int, int)}
	 * @param args Arguments
	 * @return String
	 *
	 */
	public static String buildString(String[] args) {
		return StringUtils.join(args, " ", 0, args.length);
	}

	/**
	 * @apiNote Join strings to one String with split ' ' @see
	 *          {@link StringUtils#join(Object[], String, int, int)}
	 * @param start Start argument (defaulty 0)
	 * @param args  Arguments
	 * @return String
	 *
	 */
	public static String buildString(int start, String[] args) {
		return StringUtils.join(args, " ", start, args.length);
	}

	/**
	 * @apiNote Join strings to one String with split ' ' @see
	 *          {@link StringUtils#join(Object[], String, int, int)}
	 * @param start Start argument (defaulty 0)
	 * @param end   Last argument (defaultly args.length)
	 * @param args  Arguments
	 * @return String
	 *
	 */
	public static String buildString(int start, int end, String[] args) {
		return StringUtils.join(args, " ", start, end);
	}

	/**
	 * @apiNote Return random object from list
	 */
	public static <T> T getRandomFromList(List<T> list) {
		if (list == null || list.isEmpty())
			return null;
		return list.get(StringUtils.random.nextInt(list.size()));
	}

	/**
	 * @apiNote Return random object from collection
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getRandomFromCollection(Collection<T> list) {
		if (list == null || list.isEmpty())
			return null;
		if (list instanceof List)
			return StringUtils.getRandomFromList((List<T>) list);
		return (T) list.toArray()[StringUtils.random.nextInt(list.size())];
	}

	/**
	 * @apiNote Get long from string
	 * @param period String
	 * @return long
	 */
	public static long getTimeFromString(String period) {
		return StringUtils.timeFromString(period);
	}

	/**
	 * @apiNote Get long from string
	 * @param period String
	 * @return long
	 */
	public static long timeFromString(String original) {
		if (original == null || original.isEmpty())
			return 0;

		String period = original;

		if (StringUtils.isFloat(period) && !period.endsWith("d") && !period.endsWith("e"))
			return (long) StringUtils.getFloat(period);
		float time = 0;

		Matcher matcher = StringUtils.sec.matcher(period);
		while (matcher.find()) {
			time += StringUtils.getFloat(matcher.group());
			period = period.replace(matcher.group(), "");
		}
		matcher = StringUtils.min.matcher(period);
		while (matcher.find()) {
			time += StringUtils.getFloat(matcher.group()) * 60;
			period = period.replace(matcher.group(), "");
		}
		matcher = StringUtils.hour.matcher(period);
		while (matcher.find()) {
			time += StringUtils.getFloat(matcher.group()) * 3600;
			period = period.replace(matcher.group(), "");
		}
		matcher = StringUtils.day.matcher(period);
		while (matcher.find()) {
			time += StringUtils.getFloat(matcher.group()) * 86400;
			period = period.replace(matcher.group(), "");
		}
		matcher = StringUtils.week.matcher(period);
		while (matcher.find()) {
			time += StringUtils.getFloat(matcher.group()) * 86400 * 7;
			period = period.replace(matcher.group(), "");
		}
		matcher = StringUtils.mon.matcher(period);
		while (matcher.find()) {
			time += StringUtils.getFloat(matcher.group()) * 86400 * 31;
			period = period.replace(matcher.group(), "");
		}
		matcher = StringUtils.year.matcher(period);
		while (matcher.find()) {
			time += StringUtils.getFloat(matcher.group()) * 86400 * 31 * 12;
			period = period.replace(matcher.group(), "");
		}
		return (long) time;
	}

	/**
	 * @apiNote Set long to string
	 * @param period long
	 * @return String
	 */
	public static String setTimeToString(long period) {
		return StringUtils.timeToString(period);
	}

	private static String findCorrectFormat(int i, String string) {
		String result = i + string;
		for (String s : StringUtils.actions.get(string))
			if (s.startsWith("=,") ? StringUtils.getInt(s.substring(1).split(",")[1]) == i
					: s.startsWith("<,") ? StringUtils.getInt(s.substring(1).split(",")[1]) >= i
							: s.startsWith(">,") ? StringUtils.getInt(s.substring(1).split(",")[1]) <= i : false)
				return s.substring(3 + s.substring(1).split(",")[1].length());
		return result;
	}

	private static String format(int time, String section) {
		return StringUtils.timeFormat.replace("%time%", "" + time).replace("%format%",
				StringUtils.findCorrectFormat(time, section));
	}

	/**
	 * @apiNote Set long to string
	 * @param time long
	 * @return String
	 */
	public static String timeToString(long time) {
		if (time == 0)
			return StringUtils.format(0, "Seconds");
		int minutes = (int) (time / 60) % 60;
		int hours = (int) (time / 3600) % 24;
		int days = (int) (time / 86400) % 31;
		int month = 0;
		int year = 0;
		try {
			month = (int) (time / 86400 / 31) % 12;
			year = (int) time / 86400 / 31 / 12;
		} catch (Exception er) {
		}
		StringBuilder date = new StringBuilder(64);
		if (year > 0)
			date.append(StringUtils.format(year, "Years"));
		if (month > 0) {
			if (date.length() != 0)
				date.append(StringUtils.timeSplit);
			date.append(StringUtils.format(month, "Months"));
		}
		if (days > 0) {
			if (date.length() != 0)
				date.append(StringUtils.timeSplit);
			date.append(StringUtils.format(days, "Days"));
		}
		if (hours > 0) {
			if (date.length() != 0)
				date.append(StringUtils.timeSplit);
			date.append(StringUtils.format(hours, "Hours"));
		}
		if (minutes > 0) {
			if (date.length() != 0)
				date.append(StringUtils.timeSplit);
			date.append(StringUtils.format(minutes, "Minutes"));
		}
		if (time % 60 > 0) {
			if (date.length() != 0)
				date.append(StringUtils.timeSplit);
			date.append(StringUtils.format((int) (time % 60), "Seconds"));
		}
		return date.toString();
	}

	/**
	 * @apiNote Get boolean from string
	 * @return boolean
	 */
	public static boolean getBoolean(String fromString) {
		try {
			return fromString.equalsIgnoreCase("true");
		} catch (Exception er) {
			return false;
		}
	}

	/**
	 * @apiNote Convert String to Math and Calculate exempt
	 * @return double
	 */
	public static double calculate(String original) {

		String val = original;

		if (val.contains("(") && val.contains(")"))
			val = StringUtils.splitter(val);
		if (val.contains("*") || val.contains("/")) {
			Matcher s = StringUtils.extra.matcher(val);
			while (s.find()) {
				double a = StringUtils.getDouble(s.group(1));
				String b = s.group(3);
				double d = StringUtils.getDouble(s.group(4));
				val = val.replace(s.group(), (a == 0 || d == 0 ? 0 : b.equals("*") ? a * d : a / d) + "");
				s.reset(val);
			}
		}
		if (val.contains("+") || val.contains("-")) {
			Matcher s = StringUtils.normal.matcher(val);
			while (s.find()) {
				double a = StringUtils.getDouble(s.group(1));
				String b = s.group(3);
				double d = StringUtils.getDouble(s.group(4));
				val = val.replace(s.group(), (b.equals("+") ? a + d : a - d) + "");
				s.reset(val);
			}
		}
		return StringUtils.getDouble(val.replaceAll("[^0-9+.-]", ""));
	}

	private static String splitter(String s) {
		StringBuilder i = new StringBuilder();
		StringBuilder fix = new StringBuilder();

		int count = 0;
		int waiting = 0;
		for (char c : s.toCharArray()) {
			i.append(c);
			if (c == '(') {
				fix.append(c);
				waiting = 1;
				++count;
			} else if (c == ')') {
				fix.append(c);
				if (--count == 0) {
					waiting = 0;
					i = new StringBuilder(i.toString().replace(fix.toString(),
							"" + StringUtils.calculate(fix.substring(1, fix.length() - 1))));
					fix.delete(0, fix.length());
				}
			} else if (waiting == 1)
				fix.append(c);
		}
		return i.toString();
	}

	/**
	 * @apiNote Get double from string
	 * @return double
	 */
	public static double getDouble(String fromString) {
		if (fromString == null)
			return 0.0D;
		String a = fromString.replaceAll("[^+0-9E.,-]+", "").replace(",", ".");
		try {
			return Double.parseDouble(a);
		} catch (NumberFormatException e) {
		}
		return 0.0D;
	}

	/**
	 * @apiNote Is string, double ?
	 * @return boolean
	 */
	public static boolean isDouble(String fromString) {
		try {
			Double.parseDouble(fromString);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * @apiNote Get long from string
	 * @return long
	 */
	public static long getLong(String fromString) {
		if (fromString == null)
			return 0L;
		String a = fromString.replaceAll("[^+0-9E.,-]+", "").replace(",", ".");
		try {
			return Long.parseLong(a);
		} catch (NumberFormatException e) {
		}
		return 0L;
	}

	/**
	 * @apiNote Is string, long ?
	 * @return boolean
	 */
	public static boolean isLong(String fromString) {
		try {
			Long.parseLong(fromString);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * @apiNote Get int from string
	 * @return int
	 */
	public static int getInt(String fromString) {
		if (fromString == null)
			return 0;
		String a = fromString.replaceAll("[^+0-9E.,-]+", "").replace(",", ".");
		if (!a.contains(".")) {
			try {
				return Integer.parseInt(a);
			} catch (NumberFormatException e) {
			}
			try {
				return (int) Long.parseLong(a);
			} catch (NumberFormatException e) {
			}
		}
		try {
			return (int) Double.parseDouble(a);
		} catch (NumberFormatException e) {
		}
		return 0;
	}

	/**
	 * @apiNote Is string, int ?
	 * @return boolean
	 */
	public static boolean isInt(String fromString) {
		try {
			Integer.parseInt(fromString);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * @apiNote Is string, float ?
	 * @return boolean
	 */
	public static boolean isFloat(String fromString) {
		try {
			Float.parseFloat(fromString);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * @apiNote Get float from string
	 * @return float
	 */
	public static float getFloat(String fromString) {
		if (fromString == null)
			return 0F;
		String a = fromString.replaceAll("[^+0-9E.,-]+", "").replace(",", ".");
		try {
			return Float.parseFloat(a);
		} catch (NumberFormatException e) {
		}
		return 0;
	}

	/**
	 * @apiNote Is string, float ?
	 * @return boolean
	 */
	public static boolean isByte(String fromString) {
		try {
			Byte.parseByte(fromString);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * @apiNote Get float from string
	 * @return float
	 */
	public static byte getByte(String fromString) {
		if (fromString == null)
			return (byte) 0;
		String a = fromString.replaceAll("[^+0-9E-]+", "");
		try {
			return Byte.parseByte(a);
		} catch (NumberFormatException e) {
		}
		return 0;
	}

	/**
	 * @apiNote Is string, float ?
	 * @return boolean
	 */
	public static boolean isShort(String fromString) {
		try {
			Short.parseShort(fromString);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * @apiNote Get float from string
	 * @return float
	 */
	public static short getShort(String fromString) {
		if (fromString == null)
			return (short) 0;
		String a = fromString.replaceAll("[^+0-9E-]+", "");
		try {
			return Short.parseShort(a);
		} catch (NumberFormatException e) {
		}
		return 0;
	}

	/**
	 * @apiNote Is string, number ?
	 * @return boolean
	 */
	public static boolean isNumber(String fromString) {
		return StringUtils.isInt(fromString) || StringUtils.isDouble(fromString) || StringUtils.isLong(fromString)
				|| StringUtils.isByte(fromString) || StringUtils.isShort(fromString) || StringUtils.isFloat(fromString);
	}

	/**
	 * @apiNote Is string, boolean ?
	 * @return boolean
	 */
	public static boolean isBoolean(String fromString) {
		if (fromString == null)
			return false;
		return fromString.equalsIgnoreCase("true") || fromString.equalsIgnoreCase("false");
	}

	public static boolean containsSpecial(String value) {
		return StringUtils.special.matcher(value).find();
	}

	public static Number getNumber(String o) {
		if (o == null)
			return null;
		if (!o.contains(".")) {
			if (StringUtils.isInt(o))
				return StringUtils.getInt(o);
			if (StringUtils.isLong(o))
				return StringUtils.getLong(o);
			if (StringUtils.isByte(o))
				return StringUtils.getByte(o);
			if (StringUtils.isShort(o))
				return StringUtils.getShort(o);
		}
		if (StringUtils.isDouble(o))
			return StringUtils.getDouble(o);
		if (StringUtils.isFloat(o))
			return StringUtils.getFloat(o);
		return null;
	}
}
