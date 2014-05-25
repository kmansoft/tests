package org.kman.KitKatAlarmTest.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.kman.tests.utils.MyLog;

public class AddressResolution {

	private static final String TAG = "AddressResolution";
	private static final String DNS_FAILURE = "DNS resolution failed";

	private static/* final */boolean VERBOSE_LOG = false;

	/*
	 * There are cases when Android is trying to use an IPv6 DNS record, even though the device does
	 * not have IPv6 connectivity. We have to resolve and try IPv6 + IPv4 ourselves.
	 */
	public static InetAddress[] resolveServerName(String name) throws UnknownHostException {
		if (VERBOSE_LOG) MyLog.i(TAG, "Resolving address for %s", name);
		final InetAddress[] addrList = InetAddress.getAllByName(name);

		InetAddress addr6 = null;
		InetAddress addr4 = null;

		if (addrList != null) {
			for (InetAddress addr : addrList) {
				if (addr instanceof Inet6Address) {
					if (VERBOSE_LOG) MyLog.i(TAG, "IPv6: %s", addr);
					if (addr6 == null) {
						addr6 = addr;
					}
				} else if (addr instanceof Inet4Address) {
					if (VERBOSE_LOG) MyLog.i(TAG, "IPv4: %s", addr);
					if (addr4 == null) {
						addr4 = addr;
					}
				}
				if (addr6 != null && addr4 != null) {
					break;
				}
			}
		}

		if (addr6 != null && addr4 != null) {
			return new InetAddress[] { addr4, addr6 };
		} else if (addr6 != null) {
			return new InetAddress[] { addr6 };
		} else if (addr4 != null) {
			return new InetAddress[] { addr4 };
		} else {
			MyLog.i(TAG, DNS_FAILURE);
			throw new UnknownHostException(DNS_FAILURE);
		}
	}

	/*
	 * DEBUG
	 * 
	 * addr6 = Inet6Address.getByAddress("imap.gmail.com", new byte[] { 0x2a, 0x00, 0x14, 0x50,
	 * 0x40, 0x08, 0x0c, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6d });
	 */

	// public static boolean isIgnorableError(IOException x) {
	// if (x instanceof SocketException) {
	// /*
	// * java.net.ConnectException: failed to connect to imap.gmail.com/2a00:1450:4008:c01::6d
	// * (port 993) after 30000ms: isConnected failed: ENETUNREACH (Network is unreachable)
	// *
	// * java.net.ConnectException: failed to connect to
	// * imap.strato.de/2a01:238:20a:202:54f0::1103 (port 993) after 30000ms: isConnected
	// * failed: EHOSTUNREACH (No route to host)
	// *
	// * On some devices, it's SocketException and not ConnectException
	// *
	// * The actual elapsed time is much shorter than 30 seconds, so we can afford to retry.
	// */
	// MyLog.msg(MyLog.FEAT_NETWRK, "SocketException error: %s", x);
	// final String msg = x.getMessage();
	// if (msg != null) {
	// if (msg.contains("EHOSTUNREACH") || msg.contains("ENETUNREACH")) {
	// MyLog.msg(MyLog.FEAT_NETWRK, "Ignoring misconfigured IPv6");
	// return true;
	// }
	// }
	// }
	// return false;
	// }
}
