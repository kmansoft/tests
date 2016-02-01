package org.kman.test.charsettest;

import java.nio.charset.Charset;

public class Charsets {

	public static final String CHARSET_UTF_8 = "UTF-8";
	public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";
	public static final String CHARSET_ASCII = "ASCII";
	public static final String CHARSET_US_ASCII = "US-ASCII";
	public static final String CHARSET_X_UNKNOWN = "X-UNKNOWN";

	public static final Charset NIO_CHARSET_UTF_8 = Charset.forName("UTF-8");
	public static final Charset NIO_CHARSET_US_ASCII = Charset.forName("US-ASCII");
}
