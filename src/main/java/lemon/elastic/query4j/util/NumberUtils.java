package lemon.elastic.query4j.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import org.elasticsearch.common.lang3.StringUtils;

import com.google.common.base.Preconditions;

/**
 * Miscellaneous utility methods for number conversion and parsing. Mainly for
 * internal use within the framework; consider Jakarta's Commons Lang for a more
 * comprehensive suite of string utilities.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1.2
 */
public abstract class NumberUtils {

	/**
	 * Convert the given number into an instance of the given target class.
	 * 
	 * @param number
	 *            the number to convert
	 * @param targetClass
	 *            the target class to convert to
	 * @return the converted number
	 * @throws IllegalArgumentException
	 *             if the target class is not supported (i.e. not a standard
	 *             Number subclass as included in the JDK)
	 * @see java.lang.Byte
	 * @see java.lang.Short
	 * @see java.lang.Integer
	 * @see java.lang.Long
	 * @see java.math.BigInteger
	 * @see java.lang.Float
	 * @see java.lang.Double
	 * @see java.math.BigDecimal
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T convertNumberToTargetClass(Number number, Class<T> targetClass)
			throws IllegalArgumentException {

		Preconditions.checkNotNull(number, "Number must not be null");
		Preconditions.checkNotNull(targetClass, "Target class must not be null");

		if (targetClass.isInstance(number)) {
			return (T) number;
		} else if (targetClass.equals(Byte.class)) {
			long value = number.longValue();
			if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
				raiseOverflowException(number, targetClass);
			}
			return (T) new Byte(number.byteValue());
		} else if (targetClass.equals(Short.class)) {
			long value = number.longValue();
			if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
				raiseOverflowException(number, targetClass);
			}
			return (T) new Short(number.shortValue());
		} else if (targetClass.equals(Integer.class)) {
			long value = number.longValue();
			if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
				raiseOverflowException(number, targetClass);
			}
			return (T) new Integer(number.intValue());
		} else if (targetClass.equals(Long.class)) {
			return (T) new Long(number.longValue());
		} else if (targetClass.equals(BigInteger.class)) {
			if (number instanceof BigDecimal) {
				// do not lose precision - use BigDecimal's own conversion
				return (T) ((BigDecimal) number).toBigInteger();
			} else {
				// original value is not a Big* number - use standard long
				// conversion
				return (T) BigInteger.valueOf(number.longValue());
			}
		} else if (targetClass.equals(Float.class)) {
			return (T) new Float(number.floatValue());
		} else if (targetClass.equals(Double.class)) {
			return (T) new Double(number.doubleValue());
		} else if (targetClass.equals(BigDecimal.class)) {
			// always use BigDecimal(String) here to avoid unpredictability of
			// BigDecimal(double)
			// (see BigDecimal javadoc for details)
			return (T) new BigDecimal(number.toString());
		} else {
			throw new IllegalArgumentException("Could not convert number [" + number + "] of type ["
					+ number.getClass().getName() + "] to unknown target class [" + targetClass.getName() + "]");
		}
	}

	/**
	 * Raise an overflow exception for the given number and target class.
	 * 
	 * @param number
	 *            the number we tried to convert
	 * @param targetClass
	 *            the target class we tried to convert to
	 */
	private static void raiseOverflowException(Number number, Class targetClass) {
		throw new IllegalArgumentException("Could not convert number [" + number + "] of type ["
				+ number.getClass().getName() + "] to target class [" + targetClass.getName() + "]: overflow");
	}

	/**
	 * Parse the given text into a number instance of the given target class,
	 * using the corresponding <code>decode</code> / <code>valueOf</code>
	 * methods.
	 * <p>
	 * Trims the input <code>String</code> before attempting to parse the
	 * number. Supports numbers in hex format (with leading "0x", "0X" or "#")
	 * as well.
	 * 
	 * @param text
	 *            the text to convert
	 * @param targetClass
	 *            the target class to parse into
	 * @return the parsed number
	 * @throws IllegalArgumentException
	 *             if the target class is not supported (i.e. not a standard
	 *             Number subclass as included in the JDK)
	 * @see java.lang.Byte#decode
	 * @see java.lang.Short#decode
	 * @see java.lang.Integer#decode
	 * @see java.lang.Long#decode
	 * @see #decodeBigInteger(String)
	 * @see java.lang.Float#valueOf
	 * @see java.lang.Double#valueOf
	 * @see java.math.BigDecimal#BigDecimal(String)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T parseNumber(String text, Class<T> targetClass) {
		Preconditions.checkNotNull(text, "Text must not be null");
		Preconditions.checkNotNull(targetClass, "Target class must not be null");
		String trimmed = SpringStringUtils.trimAllWhitespace(text);

		if (targetClass.equals(Byte.class)) {
			return (T) (isHexNumber(trimmed) ? Byte.decode(trimmed) : Byte.valueOf(trimmed));
		} else if (targetClass.equals(Short.class)) {
			return (T) (isHexNumber(trimmed) ? Short.decode(trimmed) : Short.valueOf(trimmed));
		} else if (targetClass.equals(Integer.class)) {
			return (T) (isHexNumber(trimmed) ? Integer.decode(trimmed) : Integer.valueOf(trimmed));
		} else if (targetClass.equals(Long.class)) {
			return (T) (isHexNumber(trimmed) ? Long.decode(trimmed) : Long.valueOf(trimmed));
		} else if (targetClass.equals(BigInteger.class)) {
			return (T) (isHexNumber(trimmed) ? decodeBigInteger(trimmed) : new BigInteger(trimmed));
		} else if (targetClass.equals(Float.class)) {
			return (T) Float.valueOf(trimmed);
		} else if (targetClass.equals(Double.class)) {
			return (T) Double.valueOf(trimmed);
		} else if (targetClass.equals(BigDecimal.class) || targetClass.equals(Number.class)) {
			return (T) new BigDecimal(trimmed);
		} else {
			throw new IllegalArgumentException(
					"Cannot convert String [" + text + "] to target class [" + targetClass.getName() + "]");
		}
	}

	/**
	 * Parse the given text into a number instance of the given target class,
	 * using the given NumberFormat. Trims the input <code>String</code> before
	 * attempting to parse the number.
	 * 
	 * @param text
	 *            the text to convert
	 * @param targetClass
	 *            the target class to parse into
	 * @param numberFormat
	 *            the NumberFormat to use for parsing (if <code>null</code>,
	 *            this method falls back to
	 *            <code>parseNumber(String, Class)</code>)
	 * @return the parsed number
	 * @throws IllegalArgumentException
	 *             if the target class is not supported (i.e. not a standard
	 *             Number subclass as included in the JDK)
	 * @see java.text.NumberFormat#parse
	 * @see #convertNumberToTargetClass
	 * @see #parseNumber(String, Class)
	 */
	public static <T extends Number> T parseNumber(String text, Class<T> targetClass, NumberFormat numberFormat) {
		if (numberFormat != null) {
			Preconditions.checkNotNull(text, "Text must not be null");
			Preconditions.checkNotNull(targetClass, "Target class must not be null");
			DecimalFormat decimalFormat = null;
			boolean resetBigDecimal = false;
			if (numberFormat instanceof DecimalFormat) {
				decimalFormat = (DecimalFormat) numberFormat;
				if (BigDecimal.class.equals(targetClass) && !decimalFormat.isParseBigDecimal()) {
					decimalFormat.setParseBigDecimal(true);
					resetBigDecimal = true;
				}
			}
			try {
				Number number = numberFormat.parse(SpringStringUtils.trimAllWhitespace(text));
				return convertNumberToTargetClass(number, targetClass);
			} catch (ParseException ex) {
				throw new IllegalArgumentException("Could not parse number: " + ex.getMessage());
			} finally {
				if (resetBigDecimal) {
					decimalFormat.setParseBigDecimal(false);
				}
			}
		} else {
			return parseNumber(text, targetClass);
		}
	}

	/**
	 * Determine whether the given value String indicates a hex number, i.e.
	 * needs to be passed into <code>Integer.decode</code> instead of
	 * <code>Integer.valueOf</code> (etc).
	 */
	private static boolean isHexNumber(String value) {
		int index = (value.startsWith("-") ? 1 : 0);
		return (value.startsWith("0x", index) || value.startsWith("0X", index) || value.startsWith("#", index));
	}

	/**
	 * Decode a {@link java.math.BigInteger} from a {@link String} value.
	 * Supports decimal, hex and octal notation.
	 * 
	 * @see BigInteger#BigInteger(String, int)
	 */
	private static BigInteger decodeBigInteger(String value) {
		int radix = 10;
		int index = 0;
		boolean negative = false;

		// Handle minus sign, if present.
		if (value.startsWith("-")) {
			negative = true;
			index++;
		}

		// Handle radix specifier, if present.
		if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
			index += 2;
			radix = 16;
		} else if (value.startsWith("#", index)) {
			index++;
			radix = 16;
		} else if (value.startsWith("0", index) && value.length() > 1 + index) {
			index++;
			radix = 8;
		}

		BigInteger result = new BigInteger(value.substring(index), radix);
		return (negative ? result.negate() : result);
	}

	public static boolean isNumber(String str) {
		if (StringUtils.isEmpty(str)) {
			return false;
		}
		char[] chars = str.toCharArray();
		int sz = chars.length;
		boolean hasExp = false;
		boolean hasDecPoint = false;
		boolean allowSigns = false;
		boolean foundDigit = false;

		int start = chars[0] == '-' ? 1 : 0;
		if ((sz > start + 1) && (chars[start] == '0')) {
			if ((chars[(start + 1)] == 'x') || (chars[(start + 1)] == 'X')) {
				int i = start + 2;
				if (i == sz) {
					return false;
				}

				for (; i < chars.length; i++) {
					if (((chars[i] < '0') || (chars[i] > '9')) && ((chars[i] < 'a') || (chars[i] > 'f'))
							&& ((chars[i] < 'A') || (chars[i] > 'F'))) {
						return false;
					}
				}
				return true;
			}
			if (Character.isDigit(chars[(start + 1)])) {
				int i = start + 1;
				for (; i < chars.length; i++) {
					if ((chars[i] < '0') || (chars[i] > '7')) {
						return false;
					}
				}
				return true;
			}
		}
		sz--;

		int i = start;

		while ((i < sz) || ((i < sz + 1) && (allowSigns) && (!foundDigit))) {
			if ((chars[i] >= '0') && (chars[i] <= '9')) {
				foundDigit = true;
				allowSigns = false;
			} else if (chars[i] == '.') {
				if ((hasDecPoint) || (hasExp)) {
					return false;
				}
				hasDecPoint = true;
			} else if ((chars[i] == 'e') || (chars[i] == 'E')) {
				if (hasExp) {
					return false;
				}
				if (!foundDigit) {
					return false;
				}
				hasExp = true;
				allowSigns = true;
			} else if ((chars[i] == '+') || (chars[i] == '-')) {
				if (!allowSigns) {
					return false;
				}
				allowSigns = false;
				foundDigit = false;
			} else {
				return false;
			}
			i++;
		}
		if (i < chars.length) {
			if ((chars[i] >= '0') && (chars[i] <= '9')) {
				return true;
			}
			if ((chars[i] == 'e') || (chars[i] == 'E')) {
				return false;
			}
			if (chars[i] == '.') {
				if ((hasDecPoint) || (hasExp)) {
					return false;
				}

				return foundDigit;
			}
			if ((!allowSigns) && ((chars[i] == 'd') || (chars[i] == 'D') || (chars[i] == 'f') || (chars[i] == 'F'))) {
				return foundDigit;
			}
			if ((chars[i] == 'l') || (chars[i] == 'L')) {
				return (foundDigit) && (!hasExp) && (!hasDecPoint);
			}

			return false;
		}

		return (!allowSigns) && (foundDigit);
	}

}
