/**
 * 文 件 名:  AndroidUtils.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  sKF61027
 * 修改时间:  2011-12-27
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.huawei.vodafone.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.huawei.vodafone.MyApplication;

/**
 * android 自身工具类
 * 
 * 
 * @author sKF61027
 * @version [版本号, 2011-12-27]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class AndroidUtils {
	/*
	 * 原始数据库联系人缓存
	 */
	public static final String HIGH_LIGHT_HEAD = "<font color=\"#e20111\">";

	public static final String HIGH_LIGHT_TAIL = "</font>";

	static Toast toast;

	/**
	 * 调用系统电话接口
	 * 
	 * @param context
	 * @param phoneNumber
	 * @see dialerPhone
	 */
	public static void dialerPhone(Context context, String phoneNumber) {
		if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
			Intent intent = new Intent();
			Uri uri = Uri.parse("tel:" + phoneNumber);
			intent.setAction(Intent.ACTION_CALL);
			intent.setData(uri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	/**
	 * 调用系统的联系人编辑
	 * 
	 * @param context
	 * @param mRawId
	 * @see editContact
	 */
	public static void editContact(Context context, int mRawId) {
		if (mRawId > -1) {
			Uri mUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, mRawId);
			Intent editIntent = new Intent(Intent.ACTION_EDIT, mUri);
			editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(editIntent);
		}
	}

	/**
	 * 发送短信
	 * 
	 * @param context
	 * @param phoneNumber
	 * @param content
	 */
	public static void sendMsg(Context context, String phoneNumber, String str) {
		SmsManager sms = SmsManager.getDefault();

		Intent sendIntent = new Intent("SENT_SMS_ACTION");
		PendingIntent sendPI = PendingIntent.getBroadcast(context, 0,
				sendIntent, 0);
		Intent deliverIntent = new Intent("DELIVERED_SMS_ACTION");
		PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0,
				deliverIntent, 0);
		if (str.getBytes().length > 140) {
			ArrayList<String> msgList = sms.divideMessage(str);
			for (String msg : msgList) {
				sms.sendTextMessage(phoneNumber, null, msg, sendPI, deliverPI);
			}
		} else {
			sms.sendTextMessage(phoneNumber, null, str, sendPI, deliverPI);
		}
	}

	/**
	 * 实现文本复制功能 add by wangqianzhou
	 * 
	 * @param content
	 */
	public static void copy(String content, Context context) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText(content.trim());
	}

	/**
	 * 实现粘贴功能 add by wangqianzhou
	 * 
	 * @param context
	 * @return
	 */
	public static String paste(Context context) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		return cmb.getText().toString().trim();
	}

	/**
	 * 获取SmsMessage消息
	 * 
	 * @param intent
	 * @return
	 */
	public final static SmsMessage[] getMessagesFromIntent(Intent intent) {
		Object[] messages = (Object[]) intent.getSerializableExtra("pdus");

		byte[][] pduObjs = new byte[messages.length][];

		for (int i = 0; i < messages.length; i++) {
			pduObjs[i] = (byte[]) messages[i];
		}

		byte[][] pdus = new byte[pduObjs.length][];

		int pduCount = pdus.length;

		SmsMessage[] msgs = new SmsMessage[pduCount];

		for (int i = 0; i < pduCount; i++) {
			pdus[i] = pduObjs[i];

			msgs[i] = SmsMessage.createFromPdu(pdus[i]);
		}
		return msgs;
	}

	/**
	 * 关闭游标
	 * 
	 * @param cursor
	 * @see closeCursor
	 */
	public static void closeCursor(Cursor cursor) {
		// 判定游标是否为空
		if (null != cursor) {
			// 判定游标是否已经关闭
			if (!cursor.isClosed()) {
				// 关闭游标
				cursor.close();
			}
			// 游标置空
			cursor = null;
		}
	}

	/**
	 * 去电话号码头部
	 * 
	 * @param number
	 *            : 号码
	 * @return number (Fail: null)
	 * @see deleteHead
	 */
	public static String deleteHead(String number) {
		String ret = number;

		if ((null == number) || ("".equals(number.trim()))) {
			return ret;
		}

		// TODO
		String countryCode = "";// Global.getInstance().getLoginInfo().getCountryCode();
		if (StringUtils.isEmpty(countryCode)) {
			return ret;
		}

		int startIndex = number.indexOf(countryCode);

		if (startIndex != -1) {
			startIndex += countryCode.length();
			ret = number.substring(startIndex);
		}

		return ret;
	}

	/**
	 * 获得手机的IP地址
	 * 
	 * @return String string
	 * @see [类、类#方法、类#成员]
	 */
	public static String getLocalIPAddress() {
		String address = "";

		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						address = inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("ip", ex.toString());
		}
		return address;
	}

	/**
	 * 
	 * 判断是否为wifi网络
	 * 
	 * @param mContext
	 * @return
	 * @see isWifi
	 */
	public static boolean isWifi(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * 是否联网
	 * 
	 * @param context
	 * @return
	 * @see isNetWork
	 */
	public static boolean isNetWork(Context context) {
		ConnectivityManager cManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {
			// 联网
			return true;
		} else {
			// 未联网
			return false;
		}
	}

	// Android验证email地址的函数
	static boolean isValidAddress(String address) {
		// Note: Some email provider may violate the standard, so here we only
		// check that
		// address consists of two part that are separated by '@', and domain
		// part contains
		// at least one '.'.
		int len = address.length();
		int firstAt = address.indexOf('@');
		int lastAt = address.lastIndexOf('@');
		int firstDot = address.indexOf('.', lastAt + 1);
		int lastDot = address.lastIndexOf('.');
		return firstAt > 0 && firstAt == lastAt && lastAt + 1 < firstDot
				&& firstDot <= lastDot && lastDot < len - 1;
	}

	/**
	 * 获取SIM卡联系人信息
	 */
	public void getSIMContacts(Context context) {
		ContentResolver resolver = context.getContentResolver();

		/*
		 * 获取Sims卡联系人
		 */
		Uri uri = Uri.parse("content://icc/adn");

		/*
		 * 获取库Phon表字段
		 */
		String[] PHONES_PROJECTION = new String[] { Phone.DISPLAY_NAME,
				Phone.NUMBER, Photo.PHOTO_ID, Phone._ID };

		Cursor cursor = resolver
				.query(uri, PHONES_PROJECTION, null, null, null);

		if (cursor == null) {
			return;
		}

		if (cursor.moveToFirst()) {
			while (cursor.moveToNext()) {
				/*
				 * 得到手机号码
				 */
				String phone = cursor.getString(cursor
						.getColumnIndex(Phone.NUMBER));
				/*
				 * 当手机号码为空的或者为空字段 跳过当前循环
				 */
				if (!StringUtils.isEmpty(phone)) {
					/*
					 * 得到联系人名称
					 */
					cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
				}
			}
		}

		closeCursor(cursor);
	}

	/**
	 * sourceStr是否等于 constraint + ***
	 * 
	 * @param sourceStr
	 * @param constraint
	 * @return
	 * @see isExist
	 */
	public static boolean isExist(String sourceStr, String constraint) {
		String regex = "";
		String reg = "(.*\\b)?" + constraint;
		regex = "^" + reg + ".*$";

		return Pattern.compile(regex).matcher(sourceStr).matches();
	}

	/**
	 * Html格式化输入字符串 <功能详细描述>
	 * 
	 * @param head
	 * @param body
	 * @param end
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static String highLight(String head, String body, String end) {
		head = (null != head) ? head : "";
		body = (null != body && !"".equals(body)) ? HIGH_LIGHT_HEAD + body
				+ HIGH_LIGHT_TAIL : "";
		end = (null != end) ? end : "";
		return (head + body + end);
	}

	@SuppressWarnings("unused")
	public static int[] quanpinMatch(String key, String code, String head,
			String match) {
		/*
		 * 返回高亮显示的开始和结束位置
		 */
		int[] rets = new int[] { -1, -1 };

		/*
		 * 将 head字段以","为标识,拆分成String数组
		 */
		String[] heads = head.split(",");
		if (heads == null) {
			return rets;
		}
		/*
		 * 将 match字段以","为标识,拆分成String数组
		 */
		String[] matchs = match.split(",");
		if (matchs == null) {
			return rets;
		}
		/*
		 * 定义个数为姓名长度的Boolean数组,记录名字中各个字是否需要高亮 // 未使用字段
		 */

		boolean[] boolOfMatch = new boolean[heads[0].length()];

		/*
		 * 定义key中首字母在head中出现的位置 //未使用字段
		 */
		// boolean[] boolOfIndex = new boolean[heads[0].length()];

		/*
		 * 定义一个List拼音数组
		 */
		List<String[]> matchList = new ArrayList<String[]>();

		/*
		 * 将match字段完全拆分成拼音数组
		 */
		for (int i = 0; i < matchs.length; i++) {
			matchList.add(matchs[i].split("_"));
		}

		/*
		 * 如果当前code字段中包含key字符串
		 */
		if (code.contains(key)) {
			/*
			 * 将当前code字段以","为标识,拆分成多音字数组
			 */
			String[] codes = code.split(",");

			/*
			 * 循环,次数为code拆分后的数组长度(姓名的一种拼音组合)
			 */
			for (int i = 0; i < codes.length; i++) {
				/*
				 * 如果当前拼音组合中包含key字段
				 */
				if (codes[i].contains(key)) {
					/*
					 * 当前拼音组合的首字母字符串
					 */
					String headtmp = heads[i];
					/*
					 * 当前拼音组合中汉字对应的拼音字符串数组
					 */
					String[] matchtmp = matchList.get(i);
					/*
					 * 姓名中包括多少个汉字字符
					 */
					int lengthOfHead = headtmp.length();
					/*
					 * 循环,次数为姓名的长度
					 */
					a: for (int j = 0; j < lengthOfHead; j++) {
						/*
						 * 如果key的首字母等于当前循环中汉字的首字母
						 */
						if (headtmp.charAt(j) == key.charAt(0)) {
							// boolOfIndex[j] = true;

							/*
							 * 当前汉字中拼音的个数
							 */
							int lengthOfOneCode = matchtmp[j].length();
							/*
							 * key中拼音个数
							 */
							int lengthOfKey = key.length();

							/*
							 * 如果key的长度小于等于当前汉字对应拼音的长度
							 */
							if (lengthOfKey <= lengthOfOneCode) {
								/*
								 * key的长度小于等于匹配拼音的长度时 如果匹配拼音中包含key 则此匹配拼音高亮
								 * Exp:单于(32698) key = (32) 时,"单"字高亮
								 * 
								 * 
								 * 循环,次数为当前汉字对应拼音的长度
								 */
								b: for (int k = 0; k < lengthOfOneCode; k++) {
									/*
									 * 当前汉字对应拼音中前K个字符串
									 */
									String tmpk = matchtmp[j].substring(0,
											k + 1);

									/*
									 * 如果当前汉字对应拼音中前K个字符串和key相等
									 */
									if (tmpk.equals(key)) {

										/*
										 * 当前汉字高亮
										 */
										boolOfMatch[j] = true;

										rets = new int[] { j, j };
										/*
										 * 匹配结束,终止匹配循环
										 */
										break a;
									}
								}
							} else {
								/*
								 * key的长度大于匹配拼音的长度
								 * 从匹配拼音开始加key的长度结束包含的全部拼音必须完全包含key 拼音对应汉字高亮
								 * 否则全部不高亮 Exp:单于(32698) key =
								 * 单于(326998)时,"单于"高亮 Exp:单于(32698) key =
								 * 单于(3269989)时,"单于" 不高亮
								 */

								/*
								 * 当前汉字对应拼音
								 */
								StringBuffer tmpk = new StringBuffer(
										matchtmp[j]);

								/*
								 * 当前汉字之后一个汉字的拼音在数组中的位置
								 */
								int ki = j;

								while (tmpk.length() < lengthOfKey
										&& ++ki < matchtmp.length) {
									tmpk.append(matchtmp[ki]);
								}

								/*
								 * 可以与key对应的,以key首字母开始的姓名中汉字对应拼音的长度
								 * Exp:code=单于于(3269898); key=(989) 则: tmpk =
								 * 9898, lengthOfTmpk = 4
								 */
								int lengthOfTmpk = tmpk.length();

								/*
								 * 循环,次数为当前匹配到的拼音组合的长度
								 */
								for (int k = 0; k < lengthOfTmpk; k++) {

									/*
									 * 将当前组合分解为单个拼音字符累加的组合方式 Exp: 9898 ->
									 * 9,98,989,9898
									 */
									String tmpki = tmpk.toString().substring(0,
											k + 1);

									/*
									 * 如果当前的组合方式中完全包含key字符串,则匹配成功
									 */
									if (tmpki.equals(key)) {
										rets = new int[] { j, ki };

										/*
										 * 判断key对应的汉字个数
										 */
										for (int l = j; l <= ki; l++) {
											boolOfMatch[l] = true;
										}
										/*
										 * 匹配成功,推出当前循环
										 */
										break;
									}
								}
							}
						}
					}
				}
			}
		}

		return rets;
	}

	public static boolean extractDatabase(Context context) {
		boolean flag = false;
		File path = new File("/data/data/" + context.getPackageName()
				+ "/databases/");
		String file = "/data/data/" + context.getPackageName()
				+ "/databases/hanzi2code.db";

		if (!path.exists()) {
			flag = path.mkdir();
			if (flag == false) {
				return false;
			}
		}

		if (new File(file).exists()) {
			return true;
		}

		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = context.getAssets().open("hanzi2code.db");
			fos = new FileOutputStream(file);

			byte[] buffer = new byte[1024];
			int count = 0;

			while ((count = is.read(buffer)) > 0) {
				fos.write(buffer, 0, count);
				fos.flush();
			}
		} catch (Exception e) {
			Log.e("AndroidUtils", e.getMessage());
		} finally {
			try {
				if (null != is) {
					is.close();
				}
			} catch (IOException e) {
				Log.e("AndroidUtils", e.getMessage());
			}

			try {
				if (null != fos) {
					fos.close();
				}
			} catch (IOException e) {
				Log.e("AndroidUtils", e.getMessage());
			}

		}

		return true;
	}

	/**
	 * 判定输入字符串是否以汉字，数字，字母组成
	 * 
	 * @param str
	 * @return
	 * @see checkCode
	 */
	public static String checkCode(String str) {
		StringBuffer sb = new StringBuffer();
		int length = str.length();
		for (int i = 0; i < length; i++) {
			char c = str.charAt(i);
			if (c < 40680 && c > 19967) {
				sb.append(str.charAt(i));
			} else if (c < 123 && c > 96) {
				sb.append(str.charAt(i));
			} else if (c < 91 && c > 64) {
				sb.append(str.charAt(i));
			} else if (c < 58 && c > 47) {
				sb.append(str.charAt(i));
			}
		}

		return sb.toString();
	}

	/**
	 * 
	 * 弹出提示窗口
	 * 
	 * @param context
	 * @param message
	 * @param length
	 */
	public static void showToast(Context context, String message, int length) {
		Toast.makeText(context, message, length).show();
	}

	/**
	 * 同时多次点击只展示一个toast（最新的那个）
	 * 
	 * @param id
	 *            要显示内容的id
	 * @see [类、类#方法、类#成员]
	 */
	public static synchronized void showMsgByToast(int id) {
		// if(toast==null)
		// {
		// toast=Toast.makeText(UserProfile.getInstance().getContext(),
		// UserProfile.getInstance().getContext().getResources().getString(id),
		// Toast.LENGTH_SHORT);
		// }
		// else
		// {
		// toast.setText(UserProfile.getInstance().getContext().getResources().getString(id));
		// }
		// toast.show();
	}

	/**
	 * 显示在中间的Toast（无背景） <功能详细描述>
	 * 
	 * @param resourceId
	 * @see [类、类#方法、类#成员]
	 */
	public static void showToast(int resourceId) {
		// Toast toast = new Toast(UserProfile.getInstance().getContext());
		// toast.setGravity(Gravity.CENTER, 0, 0);
		// TextView text = new TextView(UserProfile.getInstance().getContext());
		// text.setText(resourceId);
		// toast.setView(text);
		// toast.setDuration(Toast.LENGTH_LONG);
		// toast.show();
	}

	/**
	 * 去除软件盘
	 * 
	 * @param context
	 * @see hideSoftInput
	 */
	@SuppressWarnings("static-access")
	public static void hideSoftInput(Context context) {
		InputMethodManager m = (InputMethodManager) context
				.getSystemService(context.INPUT_METHOD_SERVICE);
		if (null != m && null != ((Activity) context).getCurrentFocus()) {
			m.hideSoftInputFromWindow(((Activity) context).getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 判断有无网络
	 * 
	 * @return
	 */
	public static boolean hasNetwork(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (null != manager) {
			NetworkInfo info = manager.getActiveNetworkInfo();
			if (null != info) {
				if (info.isRoaming() || info.isConnected()) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * 获取设备UUID
	 */
	private static UUID uuid = null;

	public static synchronized UUID getDeviceUUID(Context context) {
		if (context == null) {
			return null;
		}

		if (uuid == null) {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			String id = tm.getDeviceId();
			if (id != null) {
				uuid = UUID.nameUUIDFromBytes(id.getBytes());
			}
		}

		return uuid;
	}

	/** 执行Linux命令，并返回执行结果。 */
	public static String exec(String[] args) {
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();

			int read = -1;

			process = processBuilder.start();

			errIs = process.getErrorStream();

			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}

			baos.write('\n');

			inIs = process.getInputStream();

			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}

			byte[] data = baos.toByteArray();

			result = new String(data);
		} catch (IOException e) {
			Log.e("AndroidUtils", e.getMessage());
		} catch (Exception e) {
			Log.e("AndroidUtils", e.getMessage());
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}

				if (inIs != null) {
					inIs.close();
				}
				if (baos != null) {
					baos.close();
				}
			} catch (IOException e) {
				Log.i("VCLIENTTOOL", "exec linux command error");
			}

			if (process != null) {
				process.destroy();
			}
		}
		return result;
	}

	public static String unicode2UTF(String code) {
		int length = code.length();

		char[] cc = code.toCharArray();

		String ret = "";

		for (int i = 0; i < length; i++) {

			char c = cc[i];

			String str = Integer.toBinaryString(c);

			int len = str.length();

			if (c > 0x00000000 && c < 0x0000007F) {
				ret += c;
			} else if (c > 0x00000080 && c < 0x000007FF) {
				String tmp1 = str.substring(0, len - 6);

				String add1 = "110";

				while ((add1 + tmp1).length() < 8) {
					add1 += "0";
				}

				tmp1 = add1 + tmp1;

				ret += (char) Integer.parseInt(tmp1, 2);

				ret += (char) Integer
						.parseInt("10" + str.substring(len - 6), 2);
			} else if (c > 0x00000800 && c < 0x0000FFFF) {
				String tmp1 = str.substring(0, len - 12);

				String add1 = "1110";

				while ((add1 + tmp1).length() < 8) {
					add1 += "0";
				}

				tmp1 = add1 + tmp1;

				ret += (char) Integer.parseInt(tmp1, 2);

				ret += (char) Integer.parseInt(
						"10" + str.substring(len - 12, len - 6), 2);

				ret += (char) Integer
						.parseInt("10" + str.substring(len - 6), 2);

			} else if (c > 0x00010000 && c < 0x0010FFFF) {
				String tmp1 = str.substring(0, len - 18);

				String add1 = "1110";

				while ((add1 + tmp1).length() < 8) {
					add1 += "0";
				}

				tmp1 = add1 + tmp1;

				ret += (char) Integer.parseInt(tmp1, 2);

				ret += (char) Integer.parseInt(
						"10" + str.substring(len - 18, len - 12), 2);

				ret += (char) Integer.parseInt(
						"10" + str.substring(len - 12, len - 6), 2);

				ret += (char) Integer
						.parseInt("10" + str.substring(len - 6), 2);

			}
		}
		return ret;
	}

	/**
	 * 判断一个手机号码是否在指定的号码段内 <功能详细描述>
	 * 
	 * @param numberList
	 *            指定的号码段
	 * @param number
	 *            需要检查的号码
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean isUsefulNumber(List<String> numberList, String number) {
		boolean ret = false;

		if (StringUtils.isEmpty(number) || null == numberList) {
			return ret;
		}

		if (numberList.size() == 0 || number.length() <= 1) {
			return ret;
		}

		if (number.startsWith("0")) {
			number = number.substring(1);
		}

		for (int i = 0, n = numberList.size(); i < n; i++) {
			if (number.startsWith(numberList.get(i))) {
				ret = true;
				break;
			}
		}

		return ret;
	}

	/**
	 * 获取非零的区号
	 * 
	 * @param areaCode
	 *            指定的区号
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static String getAreaCode(String areaCode) {
		if (areaCode.startsWith("0")) {
			areaCode = areaCode.substring(1);
		}
		return areaCode;
	}

	/**
	 * 获取非零的手机号(如013977885210-->13977885210)
	 * 
	 * @param number
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static String getPhoneNumber(String number) {
		if (number.startsWith("0")) {
			number = number.substring(1);
		}
		return number;
	}

	public static int generateLocalUdpPort(int portBase) {
		int TIMES_LIMIT = 10;
		int count = 0;
		int resp = -1;
		int port = portBase;
		while ((resp == -1) && (port < Integer.MAX_VALUE)) {
			if (count > TIMES_LIMIT) {
				return -1;
			}

			if (isLocalUdpPortFree(port)) {
				resp = port;
				break;
			} else {
				Log.i("generateLocalUdpPort", "try bind port:" + port
						+ " error, try again");
				port += 2;
				count++;
			}
		}

		return resp;
	}

	private static boolean isLocalUdpPortFree(int port) {
		boolean res = false;
		try {
			ServerSocket sock1 = new ServerSocket(port);
			sock1.close();
			res = true;
		} catch (IOException e) {
			Log.i("generateLocalUdpPort", "bind port error");
			res = false;
		}
		return res;
	}

	/**
	 * 判断指定服务是否已启动 <功能详细描述>
	 * 
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean isServiceStarted(Context context, String serviceAction) {
		boolean ret = false;
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		for (int i = 0; i < serviceList.size(); i++) {
			ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
			ComponentName serviceName = serviceInfo.service;
			if (serviceName.getClassName().equals(serviceAction)) {
				if (serviceInfo.pid != 0) {
					ret = true;
				} else {
					ret = false;
				}
			}
		}

		return ret;
	}

	/**
	 * 获取设备的mac地址
	 * 
	 * @return
	 */
	public static String getLocalMacAddress() {
		String mac = "";
		try {
			String path = "sys/class/net/eth0/address";
			FileInputStream fis_name = new FileInputStream(path);
			byte[] buffer_name = new byte[1024 * 8];
			int byteCount_name = fis_name.read(buffer_name);
			if (byteCount_name > 0) {
				mac = new String(buffer_name, 0, byteCount_name, "utf-8");
			}

			if (mac.length() == 0 || mac == null) {
				path = "sys/class/net/wlan0/address";
				FileInputStream fis = new FileInputStream(path);
				byte[] buffer = new byte[1024 * 8];
				int byteCount = fis.read(buffer);
				if (byteCount > 0) {
					mac = new String(buffer, 0, byteCount, "utf-8");
				}
			}

			if (mac.length() == 0 || mac == null) {
				return "";
			}
		} catch (Exception io) {

		}
		return mac.trim();
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static boolean isRunningForeground(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (!TextUtils.isEmpty(currentPackageName)
				&& currentPackageName.equals(context.getPackageName())) {
			return true;
		}

		return false;
	}

	public static boolean isAppRunning(Context context) {
		// 获取ActivityManager
		ActivityManager mAm = (ActivityManager) context
				.getSystemService(context.ACTIVITY_SERVICE);
		// 获得当前运行的task
		List<ActivityManager.RunningTaskInfo> taskList = mAm
				.getRunningTasks(100);
		for (ActivityManager.RunningTaskInfo rti : taskList) {
			// 找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台
			if (rti.topActivity.getPackageName().equals(
					context.getPackageName())
					&& rti.baseActivity.getPackageName().equals(
							context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 返回 ClassName app在前台运行。 2 app在运行,但不在前台 -1 app不在运行
	 */
	public static String isAppRun(Context context) {
		ActivityManager mAm = (ActivityManager) context
				.getSystemService(context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskList = mAm
				.getRunningTasks(100);
		for (int i = 0; i < taskList.size(); i++) {
			ComponentName topActivity = taskList.get(i).topActivity;
			ComponentName baseActivity = taskList.get(i).baseActivity;
			if (i == 0) {
				if (!TextUtils.isEmpty(topActivity.getPackageName())
						&& topActivity.getPackageName().equals(
								context.getPackageName())) {
					return topActivity.getClassName();
				}
			}
			// 找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台
			if (topActivity.getPackageName().equals(context.getPackageName())
					&& baseActivity.getPackageName().equals(
							context.getPackageName())) {
				return "2";
			}
		}
		return "-1";
	}

	public static boolean isAppOnForeground() {

		ActivityManager activityManager = (ActivityManager) MyApplication.applicationContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = MyApplication.applicationContext.getPackageName();

		List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null)
			return false;

		for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}
		return false;
	}

	public static String getModel() {
		return android.os.Build.MODEL;
	}

}
