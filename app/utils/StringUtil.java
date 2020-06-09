package utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * String Utility functions.
 * 
 * @author G. Annunziata
 * 
 */
public class StringUtil {

	/**
	 * check the emptiness of a object or its reference.
	 * 
	 * @param o
	 *            the object to check.
	 * 
	 * @return true if string representation of passed object is Null or Empty,
	 *         false otherwise.
	 */
	public static boolean isNil(Object o) {
		if (o == null || "".equals(o))
			return true;

		return false;
	}

	public static String nil(Object o, String nilValue, String newNilValue) {
		if (o == null || "".equals(o))
			return newNilValue;

		String r = o.toString();

		if (nilValue.equals(r))
			return newNilValue;

		return r;
	}

	public static String nil(Object o, String nilValue) {
		if (o == null || "".equals(o))
			return nilValue;

		return o.toString();
	}

	public static String nil(Object[] o) {

		if (o == null)
			return "";

		if (o.length <= 0)
			return "";
		else
			return nil(o[0]);

	}

	public static String nil(Object o) {
		if (o == null)
			return "";
		return o.toString();
	}

	public static String date(Object o, String format) {
		if (o == null)
			return "";

		Date d = null;
		java.sql.Date dd = null;
		GregorianCalendar c = null;
		SimpleDateFormat dt = new SimpleDateFormat(format);
		SimpleDateFormat ddt = new SimpleDateFormat();

		if (o instanceof java.sql.Date) {
			dd = (java.sql.Date) o;
			d = new Date(dd.getTime());
		} else if (o instanceof Date) {
			d = (Date) o;
		} else if (o instanceof GregorianCalendar) {
			c = (GregorianCalendar) o;
			d = c.getTime();
		} else {
			try {
				d = ddt.parse(o.toString());
				// dd = java.sql.Date.valueOf(o.toString());
				// d = new Date(dd.getTime());
			} catch (Exception ee) {
				return "";
			}
		}

		try {
			return dt.format(d);
		} catch (Exception e) {
			return "";
		}

	}

	public static String js(String s) {
		return nil(s).replaceAll("(')", "\\'").replaceAll("\"", "\\\"");

	}

	public static String db(Object s) {
		return nil(s).replaceAll("'", "''");
	}

	public static String html(String s, boolean convertLF) {
		if (convertLF) {
			return nil(s).replaceAll("\"", "\\\"").replaceAll("\n", "<br />");
		} else {
			return nil(s).replaceAll("\"", "\\\"");
		}
	}

	public static String tag(String start, String val, String end) {
		if (val == null || "".equals(val))
			return "";
		return nil(start) + val + nil(end);
	}

	public static boolean isTrue(Object param) {
		if (param == null)
			return false;

		if (param instanceof Boolean)
			return (Boolean) param;

		return isTrue(param.toString());
	}

	public static boolean isTrue(String param) {
		if (param == null)
			return false;
		if ("true".equalsIgnoreCase(param) || "on".equalsIgnoreCase(param) || "ok".equalsIgnoreCase(param)
				|| "1".equals(param) || "s".equalsIgnoreCase(param) || "si".equalsIgnoreCase(param)
				|| "y".equalsIgnoreCase(param) || "yes".equalsIgnoreCase(param))
			return true;

		return false;
	}

	public static boolean isFalse(Object param) {
		if (param == null)
			return false;

		if (param instanceof Boolean)
			return !(Boolean) param;

		return isFalse(param.toString());
	}

	public static boolean isFalse(String param) {
		if (param == null)
			return false;
		if ("false".equalsIgnoreCase(param) || "off".equalsIgnoreCase(param) || "ko".equalsIgnoreCase(param)
				|| "0".equals(param) || "n".equalsIgnoreCase(param) || "no".equalsIgnoreCase(param))
			return true;

		return false;
	}

	public static String emailSubject(String string) {
		return StringUtil.nil(string).replaceAll("\\:", " :");
	}

	public static boolean isNilDb(String val) {
		return isNil(db(val));
	}

	public static String db(String s, String nullValue) {

		return nil(s, nullValue).replaceAll("'", "''");
	}

	public static String fileName(String nomeFile) {
		int pos;
		pos = nomeFile.lastIndexOf(File.separator);
		if (pos >= 0)
			nomeFile = nomeFile.substring(pos + 1);
		pos = nomeFile.lastIndexOf("/");
		if (pos >= 0)
			nomeFile = nomeFile.substring(pos + 1);

		return nomeFile;
	}

	public static Date parseDate(String dt, String formatDateDb) throws ParseException {

		SimpleDateFormat df = new SimpleDateFormat(formatDateDb);
		return df.parse(dt);

	}

	public static Date tryParseDate(String dt, String formatDateDb, Date defaultDate) {
		if (dt == null)
			return defaultDate;

		try {
			SimpleDateFormat df = new SimpleDateFormat(formatDateDb);
			return df.parse(dt);
		} catch (Exception e) {
			return defaultDate;
		}

	}

	public static Date tryParseDate(String dt, Date defaultDate, String... formats) {
		if (dt == null)
			return defaultDate;

		for (String format : formats) {
			try {
				SimpleDateFormat df = new SimpleDateFormat(format);
				df.setLenient(false);
				return df.parse(dt);
			} catch (Exception e) {
				// ignore
			}
		}

		return defaultDate;

	}

	public static boolean isNil(Object obj, String nilValue) {
		String r = nil(obj, nilValue);
		return r.equals(nilValue);
	}

	public static BigDecimal numBD(String str, double errNil) {
		if (isNil(str))
			return new BigDecimal(errNil);

		try {
			double r = Double.parseDouble(str);
			return new BigDecimal(r);

		} catch (Exception e) {
			return new BigDecimal(errNil);
		}

	}

	public static String tryInt(String strNum) {
		return tryInt(strNum, "0");
	}

	public static String tryInt(String strNum, String defValue) {
		try {
			Integer i = Integer.parseInt(strNum);
			return String.valueOf(i);
		} catch (Exception e) {
			return defValue;
		}
	}

	public static String dbNum(String num) {

		return tryInt(num).replaceAll("\\'", "");
	}

	public static String firstNotNull(String a, String b, String c, String d) {
		if (a != null && !"".equals(a))
			return a;

		if (b != null && !"".equals(b))
			return b;

		if (c != null && !"".equals(c))
			return c;

		if (d != null && !"".equals(d))
			return d;

		return "";
	}

	public static String strBool(String val, String strTrue, String strFalse) {
		if (isTrue(val)) {
			return strTrue;
		} else {
			return strFalse;
		}
	}

	public static boolean isEmpty(String val) {
		if (val == null || val.length() == 0)
			return true;

		val = val.replaceAll("\\s++", "").trim();
		if ("".equals(val))
			return true;

		return false;
	}

	public static String normalizeWhitespaces(String s) {
		StringBuffer res = new StringBuffer();

		int prevIndex = 0;

		int currIndex = -1;

		int stringLength = s.length();

		String searchString = "  ";

		while ((currIndex = s.indexOf(searchString, currIndex + 1)) >= 0) {
			res.append(s.substring(prevIndex, currIndex + 1));

			while (currIndex < stringLength && s.charAt(currIndex) == ' ') {
				currIndex++;
			}

			prevIndex = currIndex;
		}
		res.append(s.substring(prevIndex));

		return res.toString();
	}

	public static String normalizeText(String txt) {
		if (txt == null)
			return null;

		return txt.replaceAll("\\&nbsp\\;", " ").replaceAll("[\\s\u00a0]++", " ").trim();

	}

	/**
	 * converts time (in milliseconds) to human-readable format
	 * "<w> days, <x> hours, <y> minutes and (z) seconds"
	 */
	public static String millisToLongDHMS(long duration) {
		StringBuffer res = new StringBuffer();
		long temp = 0;
		if (duration >= ONE_SECOND) {
			temp = duration / ONE_DAY;
			if (temp > 0) {
				duration -= temp * ONE_DAY;
				res.append(temp).append(" giorn").append(temp > 1 ? "i" : "o")
						.append(duration >= ONE_MINUTE ? ", " : "");
			}

			temp = duration / ONE_HOUR;
			if (temp > 0) {
				duration -= temp * ONE_HOUR;
				res.append(temp).append(" or").append(temp > 1 ? "e" : "a").append(duration >= ONE_MINUTE ? ", " : "");
			}

			temp = duration / ONE_MINUTE;
			if (temp > 0) {
				duration -= temp * ONE_MINUTE;
				res.append(temp).append(" minut").append(temp > 1 ? "i" : "o");
			}

			if (!res.toString().equals("") && duration >= ONE_SECOND) {
				res.append(" e ");
			}

			temp = duration / ONE_SECOND;
			if (temp > 0) {
				res.append(temp).append(" second").append(temp > 1 ? "i" : "o");
			}
			return res.toString();
		} else {
			return "0 secondi";
		}
	}

	/**
	 * converts time (in milliseconds) to human-readable format "<dd:>hh:mm:ss"
	 */
	public static String millisToShortDHMS(long duration) {
		String res = "";
		duration /= ONE_SECOND;
		int seconds = (int) (duration % SECONDS);
		duration /= SECONDS;
		int minutes = (int) (duration % MINUTES);
		duration /= MINUTES;
		int hours = (int) (duration % HOURS);
		int days = (int) (duration / HOURS);
		if (days == 0) {
			res = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		} else {
			res = String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
		}
		return res;
	}

	public final static long ONE_SECOND = 1000;
	public final static long SECONDS = 60;

	public final static long ONE_MINUTE = ONE_SECOND * 60;
	public final static long MINUTES = 60;

	public final static long ONE_HOUR = ONE_MINUTE * 60;
	public final static long HOURS = 24;

	public final static long ONE_DAY = ONE_HOUR * 24;

	/**
	 * @param normalizedText
	 * @param wordsText
	 * @param pre
	 * @param post
	 * @return
	 */
	public static String mark(String normalizedText, String wordsText, String pre, String post) {

		String[] words = wordsText.split(" ");
		String pr = "__@@@__";
		String po = "__@!!@__";
		for (String word : words) {
			if (word.length() < 3)
				continue;

			normalizedText = Pattern.compile("(" + Matcher.quoteReplacement(word) + ")", Pattern.CASE_INSENSITIVE)
					.matcher(normalizedText).replaceAll(pr + "$1" + po);

			// normalizedText = normalizedText.replaceAll(, pre + word + post);
		}
		normalizedText = normalizedText.replaceAll(pr, pre);
		normalizedText = normalizedText.replaceAll(po, post);
		return normalizedText;

		/*
		 * // String[] textWords = normalizedText.split("[^a-zA-Z0-9]+");
		 * StringBuffer buf = new StringBuffer(); boolean match = false; for
		 * (String t : textWords) { match = false; for (String word : words) {
		 * if (t.equalsIgnoreCase(word)) { match = true; break; } } if (match) {
		 * buf.append(pre); buf.append(t); buf.append(post); } else {
		 * buf.append(t); } buf.append(" "); } return buf.toString();
		 */
	}

	public static String toLabel(String name) {
		if (name == null) {
			return "";
		}

		StringBuilder buffer = new StringBuilder();

		for (int i = 0, size = name.length(); i < size; i++) {
			char aChar = name.charAt(i);

			if (i == 0) {
				buffer.append(Character.toUpperCase(aChar));

			} else {
				buffer.append(aChar);

				if (i < name.length() - 1) {
					char nextChar = name.charAt(i + 1);
					if (Character.isLowerCase(aChar)
							&& (Character.isUpperCase(nextChar) || Character.isDigit(nextChar))) {

						// Add space before digits or uppercase letters
						buffer.append(' ');

					} else if (Character.isDigit(aChar) && (!Character.isDigit(nextChar))) {

						// Add space after digits
						buffer.append(' ');
					}
				}
			}
		}

		return buffer.toString();
	}

	/** Hexadecimal characters for MD5 encoding. */
	private static final char[] HEXADECIMAL = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
			'e', 'f' };

	/**
	 * Return an 32 char MD5 encoded string from the given plain text. The
	 * returned value is MD5 hash compatible with Tomcat catalina Realm.
	 * <p/>
	 * Adapted from <tt>org.apache.catalina.util.MD5Encoder</tt>
	 *
	 * @param plaintext
	 *            the plain text value to encode
	 * @return encoded MD5 string
	 */
	public static String toMD5Hash(String plaintext) {
		if (plaintext == null) {
			throw new IllegalArgumentException("Null plaintext parameter");
		}
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			md.update(plaintext.getBytes("UTF-8"));

			byte[] binaryData = md.digest();

			char[] buffer = new char[32];

			for (int i = 0; i < 16; i++) {
				int low = (binaryData[i] & 0x0f);
				int high = ((binaryData[i] & 0xf0) >> 4);
				buffer[i * 2] = HEXADECIMAL[high];
				buffer[i * 2 + 1] = HEXADECIMAL[low];
			}

			return new String(buffer);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String createQuery(String templateQuery, Properties variables) {
		return applyTemplate(templateQuery, variables, true);
	}

	public static String createQuery(String templateQuery, HashMap<String, Object> variables) {
		return applyTemplate(templateQuery, variables, true);
	}

	public static String createText(String templateText, Properties variables) {
		return applyTemplate(templateText, variables, false);
	}

	public static String createText(String templateText, HashMap<String, Object> variables) {
		return applyTemplate(templateText, variables, false);
	}

	protected static String applyTemplate(String template, Map<?, ?> variables, boolean isDb) {

		StringBuffer buf = new StringBuffer(template);

		for (Object e : variables.keySet()) {

			String v = String.valueOf(e);
			String v2 = "${" + v + "}";

			int pos = 0;
			String val;
			if (isDb) {
				val = StringUtil.db(variables.get(v));
			} else {
				val = StringUtil.nil(variables.get(v));
			}

			while (pos >= 0) {
				pos = buf.indexOf(v2, pos);
				if (pos >= 0) {
					buf = buf.replace(pos, pos + v2.length(), val);
				}
			}
		}

		return buf.toString();
	}

	public static String cleanHtml(String html) {
		if (StringUtil.isNil(html))
			return "";

		return html.replaceAll("\\</?[A-Za-z]+\\s*[^\\>]*\\/?>", "");
	}
	

	public static String cleanFilename(String file) {
		return cleanFilename(file, '_');
	}

	public static String cleanFilename(String file, char sep) {
		if (StringUtil.isNil(file))
			return "";

		return file.replaceAll("[\\s\\\\/\\.\\-\\_]++", " ").trim().replace(' ', sep);
	}

	public static String convertPath(String pathId) {
		if (StringUtil.isNil(pathId))
			return StringUtil.nil(pathId);

		return pathId.replaceAll("\\\\", "/");
	}

	public static String firstNotNull(String... elems) {

		for (String e : elems) {
			if (!StringUtil.isNil(e))
				return e;
		}

		return "";
	}

	/**
	 * @param state
	 * @param i
	 * @param string
	 * @return
	 */
	public static String cut(String str, int maxLength, String tail) {
		if (str == null)
			return "";

		if (str.length() > maxLength)
			return str.substring(0, maxLength - tail.length()) + nil(tail);

		return str;
	}

	/**
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean equals(String str1, String str2) {
		if (str1 == null)
			str1 = "";

		if (str2 == null)
			str2 = "";

		return str1.equals(str2);
	}

	public static String repeatChar(char toRepeat, String val, int numChars) {
		if (val != null && !val.isEmpty())
			numChars -= val.length();
		else
			val = "";

		if (numChars < 0)
			numChars = 0;

		StringBuilder sb = new StringBuilder(val.length() + numChars);
		for (int i = 0; i < numChars; i++)
			sb.append(toRepeat);

		sb.append(val);

		return sb.toString();
	}

	/**
	 * @param text
	 * @param chars
	 * @return -1 not found, or index of last char found in sequence
	 */
	public static int searchFor(String text, char... chars) {
		int pos = -1;
		for (char c : chars) {
			pos = text.indexOf(c, pos + 1);
			if (pos == -1)
				break;
		}
		return pos;
	}

	/**
	 * @param name
	 * @param params
	 * @return
	 */
	public static boolean oneOf(String name, String... names) {
		if (name == null)
			return false;
		name = name.trim();
		for (String n : names) {
			if (n == null)
				continue;
			n = n.trim();
			if (n.equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	/**
	 * @param name
	 * @param params
	 * @return
	 */
	public static boolean oneNotNil(String... names) {
		if (names == null)
			return false;
		for (String n : names) {
			if (n == null)
				continue;
			n = n.trim();
			if (!n.isEmpty())
				return true;
		}

		return false;
	}

	/**
	 * @param name
	 * @return
	 */
	public static String clean(String name) {
		if (name == null)
			name = "";
		name = name.replaceAll("\\s++", " ");
		name = name.trim();
		return name;
	}

	public static String cleanSpaces(String st) {
		return st.replaceAll("\\s+", "");
	}

	/**
	 * @param list
	 * @return
	 */
	public static String toString(List<?> list) {
		return toString(list, ",");
	}

	/**
	 * @param list
	 * @return
	 */
	public static String toString(List<?> list, String sep) {

		if (list == null)
			return "";
		StringBuffer res = new StringBuffer(list.size() * 10);
		String s = "";
		for (Object el : list) {
			res.append(s);
			res.append(el);
			s = sep;
		}
		return res.toString();
	}

	/**
	 * @param val
	 * @param numDigit
	 * @return
	 */
	public static String viewFloat(Float val, int numDigit) {
		if (val == null)
			val = 0.0f;

		double nd = Math.pow(10, numDigit);

		return String.valueOf(Math.round(val * nd) / nd);
	}

	/**
	 * @param val
	 * @param numDigit
	 * @return
	 */
	public static String viewDouble(Double val, int numDigit) {
		if (val == null)
			val = 0.0;

		double nd = Math.pow(10, numDigit);

		return String.valueOf(Math.round(val * nd) / nd);
	}

	/**
	 * @param zz
	 * @param gp
	 * @return
	 */
	public static String firstNotNullAsString(Object... elems) {
		if (elems == null || elems.length == 0)
			return null;

		for (Object e : elems) {
			if (e != null) {
				return e.toString();
			}
		}

		return null;
	}

	/**
	 * @param tags
	 * @param string
	 * @return
	 */
	public static String join(Collection<String> elems, String sep) {
		StringBuffer s = new StringBuffer();
		join(elems, sep, s);
		return s.toString();
	}

	/**
	 * @param elems
	 * @param sep
	 * @param s
	 */
	public static void join(Collection<String> elems, String sep, StringBuffer s) {
		if (elems == null || elems.isEmpty())
			return;

		String sp = "";
		for (String elem : elems) {
			s.append(sp);
			s.append(elem);
			sp = sep;
		}

	}

	/**
	 * @param txt
	 * @return
	 */
	public static boolean isNilTrim(String txt) {
		if (txt == null)
			return true;
		return isNil(txt.trim());
	}

	// from netty.util.internal

	public static final String EMPTY_STRING = "";
	public static final String NEWLINE;

	public static final char DOUBLE_QUOTE = '\"';
	public static final char COMMA = ',';
	public static final char LINE_FEED = '\n';
	public static final char CARRIAGE_RETURN = '\r';
	public static final char TAB = '\t';

	private static final String[] BYTE2HEX_PAD = new String[256];
	private static final String[] BYTE2HEX_NOPAD = new String[256];

	/**
	 * 2 - Quote character at beginning and end. 5 - Extra allowance for
	 * anticipated escape characters that may be added.
	 */
	// private static final int CSV_NUMBER_ESCAPE_CHARACTERS = 2 + 5;
	// private static final char PACKAGE_SEPARATOR_CHAR = '.';

	static {
		// Determine the newline character of the current platform.
		String newLine;

		Formatter formatter = new Formatter();
		try {
			newLine = formatter.format("%n").toString();
		} catch (Exception e) {
			// Should not reach here, but just in case.
			newLine = "\n";
		} finally {
			formatter.close();
		}

		NEWLINE = newLine;

		// Generate the lookup table that converts a byte into a 2-digit
		// hexadecimal integer.
		int i;
		for (i = 0; i < 10; i++) {
			StringBuilder buf = new StringBuilder(2);
			buf.append('0');
			buf.append(i);
			BYTE2HEX_PAD[i] = buf.toString();
			BYTE2HEX_NOPAD[i] = String.valueOf(i);
		}
		for (; i < 16; i++) {
			StringBuilder buf = new StringBuilder(2);
			char c = (char) ('a' + i - 10);
			buf.append('0');
			buf.append(c);
			BYTE2HEX_PAD[i] = buf.toString();
			BYTE2HEX_NOPAD[i] = String.valueOf(c);
		}
		for (; i < BYTE2HEX_PAD.length; i++) {
			StringBuilder buf = new StringBuilder(2);
			buf.append(Integer.toHexString(i));
			String str = buf.toString();
			BYTE2HEX_PAD[i] = str;
			BYTE2HEX_NOPAD[i] = str;
		}
	}

	/**
	 * Converts the specified byte array into a hexadecimal value.
	 */
	public static String toHexString(byte[] src) {
		return toHexString(src, 0, src.length);
	}

	/**
	 * Converts the specified byte array into a hexadecimal value.
	 */
	public static String toHexString(byte[] src, int offset, int length) {
		return toHexString(new StringBuilder(length << 1), src, offset, length).toString();
	}

	/**
	 * Converts the specified byte array into a hexadecimal value and appends it
	 * to the specified buffer.
	 */
	public static <T extends Appendable> T toHexString(T dst, byte[] src) {
		return toHexString(dst, src, 0, src.length);
	}

	/**
	 * Converts the specified byte array into a hexadecimal value and appends it
	 * to the specified buffer.
	 */
	public static <T extends Appendable> T toHexString(T dst, byte[] src, int offset, int length) {
		assert length >= 0;
		if (length == 0) {
			return dst;
		}

		final int end = offset + length;
		final int endMinusOne = end - 1;
		int i;

		// Skip preceding zeroes.
		for (i = offset; i < endMinusOne; i++) {
			if (src[i] != 0) {
				break;
			}
		}

		byteToHexString(dst, src[i++]);
		int remaining = end - i;
		toHexStringPadded(dst, src, i, remaining);

		return dst;
	}

	/**
	 * Converts the specified byte array into a hexadecimal value.
	 */
	public static String toHexStringPadded(byte[] src) {
		return toHexStringPadded(src, 0, src.length);
	}

	/**
	 * Converts the specified byte array into a hexadecimal value.
	 */
	public static String toHexStringPadded(byte[] src, int offset, int length) {
		return toHexStringPadded(new StringBuilder(length << 1), src, offset, length).toString();
	}

	/**
	 * Converts the specified byte array into a hexadecimal value and appends it
	 * to the specified buffer.
	 */
	public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src) {
		return toHexStringPadded(dst, src, 0, src.length);
	}

	/**
	 * Converts the specified byte value into a 2-digit hexadecimal integer.
	 */
	public static String byteToHexStringPadded(int value) {
		return BYTE2HEX_PAD[value & 0xff];
	}

	/**
	 * Converts the specified byte value into a 2-digit hexadecimal integer and
	 * appends it to the specified buffer.
	 */
	public static <T extends Appendable> T byteToHexStringPadded(T buf, int value) {
		try {
			buf.append(byteToHexStringPadded(value));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return buf;
	}

	/**
	 * Converts the specified byte array into a hexadecimal value and appends it
	 * to the specified buffer.
	 */
	public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src, int offset, int length) {
		final int end = offset + length;
		for (int i = offset; i < end; i++) {
			byteToHexStringPadded(dst, src[i]);
		}
		return dst;
	}

	/**
	 * Converts the specified byte value into a hexadecimal integer.
	 */
	public static String byteToHexString(int value) {
		return BYTE2HEX_NOPAD[value & 0xff];
	}

	/**
	 * Converts the specified byte value into a hexadecimal integer and appends
	 * it to the specified buffer.
	 */
	public static <T extends Appendable> T byteToHexString(T buf, int value) {
		try {
			buf.append(byteToHexString(value));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return buf;
	}

	public static boolean oneIsEmpty(Object... objs) {
		if (objs == null || objs.length == 0)
			return true;

		for (Object o : objs) {
			if (o == null || o.toString().isEmpty())
				return true;
		}

		return false;
	}

	/**
	 * @param defVal
	 * @param string
	 * @param string2
	 * @return
	 */
	public static boolean surrounded(String val, String pre, String post) {
		if (val == null || val.isEmpty())
			return false;
		return val.startsWith(pre) && val.endsWith(post);
	}

	/**
	 * @param groupLabel
	 * @return
	 */
	public static String minimize(String label) {
		if (label.length() < 7)
			return label;
		String[] ww = label.split(" ");
		if (ww == null || ww.length < 2) {
			return label.substring(0, 6) + ".";
		}
		StringBuffer min = new StringBuffer();
		for (String w : ww) {
			if (w == null || w.isEmpty())
				continue;
			min.append(Character.toUpperCase(w.charAt(0))).append(".");
		}
		return min.toString();
	}

	public static String cleanup(String text) {
		if (text == null)
			return "";
		// ..l\u0027hotel b\u0026b ... if (text == null) return null; text = //
		// text.trim();
		// text
		// =
		// StringEscapeUtils.unescapeJava(text);
		text = StringEscapeUtils.unescapeHtml(text);
		text = text.replaceAll("\\s++", " ");
		return text;
	}

	public static String htmlToText(String html) {
		String text = StringUtil.cleanHtml(html);
		text = cleanup(text);
		return text;
	}

	public static void main(String[] args) {
		String origText = " ..l\\u0027hotel b\\u0026b anche un Bed &amp; Breakfast...l\u0027hotel b\u0026b ...";
		String text = cleanup(origText);
		System.out.println("orig:" + origText);
		System.out.println("text:" + text);

		origText = " B&B mio HOTEL excelsioR and Bed &amp; breakfast or B &B";
		text = cleanName(origText);
		System.out.println("orig:" + origText);
		System.out.println("text:" + text);

	}

	/**
	 * @param name
	 * @return
	 */
	public static String cleanStrName(String name) {
		name = cleanup(name).trim();
		return name;
	}

	/**
	 * @param name
	 * @return
	 */
	public static String cleanName(String name) {
		name = cleanup(name).toLowerCase();
		String[] words = name.split("[\\s]++");
		StringBuffer res = new StringBuffer();
		String sep = "";
		for (String w : words) {
			res.append(sep);
			if (w.contains("&") && w.length() > 1) {
				int pos = w.indexOf("&");
				String w1 = StringUtils.capitalize(w.substring(0, pos));
				String w2 = StringUtils.capitalize(w.substring(pos + 1));
				res.append(w1);
				res.append("&");
				res.append(w2);
			} else {
				w = StringUtils.capitalize(w);
				res.append(sep);
				res.append(w);
			}

			sep = " ";
		}
		name = res.toString();
		name = name.replaceAll("&", " & ");
		name = name.replaceAll("[\\s]++", " ").trim();
		name = name.replaceAll("B & B", "B&B");
		return name;
	}

	/**
	 * @param text
	 * @param accepted
	 * @return
	 */
	public static String only(String text, String accepted) {
		if (text == null)
			return null;

		StringBuffer res = new StringBuffer(text.length());
		char[] chars = text.toCharArray();
		for (char c : chars) {
			if (accepted.indexOf(c) < 0)
				continue;
			res.append(c);
		}

		return res.toString();
	}

	/**
	 * @param pk
	 * @param packages
	 * @return
	 */
	public static boolean findStartsWith(String full, String... prefixes) {
		for (String pre : prefixes) {
			if (full.startsWith(pre))
				return true;
		}
		return false;
	}

	/**
	 * @param pk
	 * @param packages
	 * @return
	 */
	public static boolean findStartsWith(String full, String sep, String... prefixes) {
		for (String pre : prefixes) {
			if (full.startsWith(pre + sep))
				return true;
		}
		return false;
	}

	/**
	 * @param nextLine
	 * @param separator
	 * @return
	 */
	public static String join(String[] strings, Character separator) {
		if (strings == null)
			return "";

		StringBuilder sb = new StringBuilder();
		Character sep = null;
		for (String s : strings) {
			if (sep != null)
				sb.append(sep);
			sb.append(s);
			sep = separator;
		}

		return sb.toString();
	}

	/**
	 * @param param
	 * @param defValue
	 * @return
	 */
	public static boolean isTrue(Object param, boolean defValue) {
		if (param == null)
			return defValue;

		if (param instanceof Boolean)
			return (Boolean) param;

		return isTrue(param.toString());
	}

	/**
	 * @param string
	 * @param firstName
	 * @param lastName
	 * @return
	 */
	public static String join(Character sep, String... elements) {
		return join(elements, sep);
	}

	/**
	 * @param port
	 * @param string
	 * @return
	 */
	public static String empty(Object val, String defValue) {
		if (val == null)
			return defValue;

		String v = String.valueOf(val);
		if (v.length() == 0)
			return defValue;

		String v2 = v.replaceAll("\\s++", "").trim();
		if ("".equals(v2))
			return defValue;

		return v;
	}
}
