package com.huawei.vodafone.ui.myview.datepicker;

import java.util.Date;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.huawei.vodafone.R;
import com.huawei.vodafone.listener.Listener.Click;
import com.huawei.vodafone.listener.Listener.setOkListener;
import com.huawei.vodafone.util.DateUtil;
import com.huawei.vodafone.util.StringUtils;

public class DatePickerTwoDialog extends Dialog {
	private Context mContext;

	private Dialog mDialog;

	/** 确认事件 */
	private setOkListener okListener;

	/** 取消事件 */
	private OnClickListener cancelListener;

	/** 确认按钮 */
	private Button ok;

	/** 取消按钮 */
	private Button cancel;

	/** 日历控件 */
	private DatePickerTwo datePicker;

	/** 时间选择 */
	private TextView time;

	private View view;

	public DatePickerTwoDialog(Context context) {
		super(context, R.style.myDialog1);
		this.mContext = context;
		mDialog = new Dialog(mContext, R.style.myDialog1);// 自定义Dialog的样式
		initView();
	}

	private void initView() {
		mDialog.setContentView(R.layout.date_picker_two_dialog);
		ok = (Button) mDialog.findViewById(R.id.ok);
		 view=mDialog.findViewById(R.id.date_ll);
		cancel = (Button) mDialog.findViewById(R.id.cancel);
		datePicker = (DatePickerTwo) mDialog.findViewById(R.id.datePicker);
		time = (TextView) mDialog.findViewById(R.id.time);
		time.setText(datePicker.getFormaTime());
		datePicker.setOnSelectingListener(new OnSelectingListener() {

			@Override
			public void selected(boolean selected) {
				if (!selected)
					return;
				time.setText(datePicker.getFormaTime());

			}
		});
		// 确认事件
		ok.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (okListener != null) {
					okListener.isOk(true);
				}
				mDialog.dismiss();
			}
		});
		// 取消事件
		cancel.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (okListener != null) {
					okListener.isOk(false);
				}
				mDialog.dismiss();
			}
		});
	}

	public static void showStartTime(Context mContext,
			final TextView approve_shenqinriqi, final Click clickListener,int w) {
		final DatePickerTwoDialog datepickerdialog = new DatePickerTwoDialog(
				mContext);
		if (StringUtils.isEmpty(approve_shenqinriqi.getText().toString())
				|| approve_shenqinriqi.getText().toString().equals(" ")) {
			datepickerdialog.setDate(new Date());
		} else {
			datepickerdialog.setDate(DateUtil.ConverToDate(approve_shenqinriqi
					.getText().toString(), "MM/yy"));
		}
		datepickerdialog.show();
		datepickerdialog.view.getLayoutParams().width=w;
		datepickerdialog.setOkListener(new setOkListener() {

			@Override
			public void isOk(boolean isOk) {
				if (isOk) {
					approve_shenqinriqi.setText(DateUtil.ConverToString(
							datepickerdialog.getDate(), "MM/yy"));
					if (clickListener != null) {
						clickListener.onClicked(0, 0, approve_shenqinriqi
								.getText().toString(), true);
					}
				}
			}

		});
	}

	public void show() {
		if (mDialog != null && !mDialog.isShowing()) {
			mDialog.show();
		}
	}

	public void dismiss(int position) {
		if (mDialog.isShowing()) {
			mDialog.cancel();
		}
	}

	public void setTime(long time) {
		if (datePicker == null)
			return;
		datePicker.setTime(time);
	}

	public void setDate(Date date) {
		if (datePicker == null)
			return;
		datePicker.setDate(date);
		time.setText(datePicker.getFormaTime());
	}

	public void setTime(int year, int month, int day) {
		if (datePicker == null)
			return;
		datePicker.setTime(year, month, day);
	}

	public Date getDate() {
		if (datePicker == null) {
			return new Date(System.currentTimeMillis());
		}
		return datePicker.getDate();
	}

	public long getTime() {
		if (datePicker == null)
			return -1L;
		return datePicker.getTime();
	}

	public int getYear() {
		if (datePicker == null)
			return -1;
		return datePicker.getYear();
	}

	public int getMonth() {
		if (datePicker == null)
			return -1;
		return datePicker.getMonth();
	}

	public String getFormaTime() {
		if (datePicker == null)
			return "";
		return datePicker.getFormaTime();
	}

	public String getFormatTime(String format) {
		if (datePicker == null)
			return "";
		return datePicker.getFormatTime(format);
	}

	public void setOkListener(setOkListener onClickListener) {
		this.okListener = onClickListener;
	}
}
