/**
 * 
 * Original file name: UuidGenerator.java
 * Created by gannunziata
 */
package utils;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.elasticsearch.common.Base64;
import org.elasticsearch.common.Strings;

/**
 * UUID generator utility.
 *
 * @author gannunziata
 */
public class UuidGenerator {

	/**
	 * @return
	 */
	public static String createId() {
		return "a" + UUID.randomUUID().toString().replace('-', '_');
	}

	public static String generate(String prefixCode) {
		String ret = prefixCode + UUID.randomUUID();
		return ret;
	}

	public static String generate20(String prefixCode) {
		String ret = prefixCode + Strings.randomBase64UUID();
		return ret;
	}

	public static String generateTs(String prefixCode) {
		String ret = prefixCode + StringUtil.date(new Date(), "yyyyMMdd-HHmmss");
		return ret;
	}

	public static String generate(String prefixCode, int digit) {
		try {
			final byte[] randomBytes = new byte[digit];
			random.nextBytes(randomBytes);
			// Set the version to version 4 (see
			// http://www.ietf.org/rfc/rfc4122.txt)
			// The randomly or pseudo-randomly generated version.
			// The version number is in the most significant 4 bits of the time
			// stamp (bits 4 through 7 of the time_hi_and_version field).
			if (digit > 6) {
				// clear the 4 most significant bits for the version
				randomBytes[6] &= 0x0f;
				// set the version to 0100 / 0x40
				randomBytes[6] |= 0x40;
			}

			// Set the variant:
			// The high field of th clock sequence multiplexed with the variant.
			// We set only the MSB of the variant
			if (digit > 8) {
				// clear the 2 most significant bits
				randomBytes[8] &= 0x3f;
				// set the variant (MSB is set)
				randomBytes[8] |= 0x80;
			}
			byte[] encoded = Base64.encodeBytesToBytes(randomBytes, 0, randomBytes.length, Base64.URL_SAFE);
			// we know the bytes are 16, and not a multi of 3, so remove the 2
			// padding chars that are added
			assert encoded[encoded.length - 1] == '=';
			assert encoded[encoded.length - 2] == '=';

			return prefixCode + new String(encoded, 0, encoded.length - 2, Base64.PREFERRED_ENCODING);
		} catch (IOException e) {
			throw new IllegalStateException("should not be thrown");
		}
	}

	static Random random = new Random();

	public static String generate10(String prefixCode) {
		int id = random.nextInt(Integer.MAX_VALUE);
		Integer ii = permuteId(id);
		return prefixCode + toBase62(ii);
	}

	private static char base62Digit(int d) {
		if (d < 26) {
			return (char) ('a' + d);
		} else if (d < 52) {
			return (char) ('A' + d - 26);
		} else if (d < 62) {
			return (char) ('0' + d - 52);
		} else {
			throw new IllegalArgumentException("base62 digit cannot be with d=" + d);
		}
	}

	public static String toBase62(int n) {
		StringBuilder res = new StringBuilder();
		while (n != 0) {
			res.insert(0, base62Digit(n % 62));
			n /= 62;
		}
		return res.toString();
	}

	private static double roundFunction(int input) {
		// Must be a function in the mathematical sense (x=y implies f(x)=f(y))
		// but it doesn't have to be reversible.
		// Must return a value between 0 and 1
		return ((1369 * input + 150889) % 714025) / 714025.0;
	}

	private static int permuteId(int id) {
		int l1 = (id >> 16) & 65535;
		int r1 = id & 65535;
		int l2, r2;
		for (int i = 0; i < 3; i++) {
			l2 = r1;
			r2 = l1 ^ (int) (roundFunction(r1) * 65535);
			l1 = l2;
			r1 = r2;
		}
		return ((r1 << 16) + l1);
	}

}
