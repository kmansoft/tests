package org.kman.KitKatAlarmTest.net;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.kman.tests.utils.MyLog;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.SSLCertificateSocketFactory;
import android.net.SSLSessionCache;
import android.os.Build;

public abstract class SSLSocketFactoryMaker {

	protected static final String TAG = "SSLSocketFactoryMaker";

	public static final int CONNECT_TIMEOUT = 30 * 1000;
	public static final int DATA_TIMEOUT = 60 * 1000;

	public static SSLSocketFactory getStrictFactory(Context context) {
		MyLog.i(TAG, "Using strict SSL factory");
		synchronized (SSLSocketFactoryMaker.class) {
			if (gStrictFactory == null) {
				final SSLSocketFactoryMaker maker = SSLSocketFactoryMaker.factory();
				gStrictFactory = maker.createStrictFactory(context);
			}
			return gStrictFactory;
		}
	}

	/*
	 * We like using SSLCertificateSocketFactory because of caching, but its host verification gets
	 * in the way when someone connect to e.g. smtp.foo.com, whose canonical DNS reverse name is
	 * smtp.gmail.com or some such.
	 */
	public static SSLSocketFactory getStrictTlsFactory(Context context) {
		MyLog.i(TAG, "Using strict STARTTLS factory");
		synchronized (SSLSocketFactoryMaker.class) {
			if (gStrictTlsFactory == null) {
				gStrictTlsFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			}
			return gStrictTlsFactory;
		}
	}

	public static SSLSocketFactory getAcceptAllFactory(Context context) {
		MyLog.i(TAG, "Using relaxed SSL/STARTTLS factory");
		synchronized (SSLSocketFactoryMaker.class) {
			if (gAcceptAllFactory == null) {
				final SSLSocketFactoryMaker maker = SSLSocketFactoryMaker.factory();
				gAcceptAllFactory = maker.createRelaxedFactory(context);
			}
			return gAcceptAllFactory;
		}
	}

	private static SSLSocketFactory gStrictFactory;
	private static SSLSocketFactory gStrictTlsFactory;
	private static SSLSocketFactory gAcceptAllFactory;

	private static SSLSocketFactoryMaker factory() {
		if (Build.VERSION.SDK_INT >= 8) {
			return new SSLSocketFactoryMaker_api8();
		}
		return new SSLSocketFactoryMaker_api5();
	}

	protected abstract SSLSocketFactory createStrictFactory(Context context);

	protected abstract SSLSocketFactory createRelaxedFactory(Context context);
}

class SSLSocketFactoryMaker_api5 extends SSLSocketFactoryMaker {

	@Override
	protected SSLSocketFactory createStrictFactory(Context context) {
		return (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	@Override
	protected SSLSocketFactory createRelaxedFactory(Context context) {
		try {
			TrustManager[] tm = new TrustManager[] { new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					MyLog.i(TAG, "getAcceptedIssuers");
					return null;
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					MyLog.i(TAG, "checkServerTrusted: " + authType);
				}

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					MyLog.i(TAG, "checkClientTrusted:" + authType);
				}
			} };

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tm, null);
			return sslContext.getSocketFactory();
		} catch (GeneralSecurityException x) {
			MyLog.i(TAG, "Error initializing security, using default SSL", x);
			return (SSLSocketFactory) SSLSocketFactory.getDefault();
		}
	}
}

@TargetApi(8)
class SSLSocketFactoryMaker_api8 extends SSLSocketFactoryMaker {

	@Override
	protected SSLSocketFactory createStrictFactory(Context context) {
		final SSLSessionCache cache = SSLSessionCacheCompat.getSSLSessionCache(context);
		return SSLCertificateSocketFactory.getDefault(CONNECT_TIMEOUT, cache);
	}

	@Override
	protected SSLSocketFactory createRelaxedFactory(Context context) {
		final SSLSessionCache cache = SSLSessionCacheCompat.getSSLSessionCache(context);
		return SSLCertificateSocketFactory.getInsecure(CONNECT_TIMEOUT, cache);
	}
}
