package com.yongshan.downloaddemo.download;

import com.yongshan.downloaddemo.R;
import com.yongshan.downloaddemo.infos.FileItemInfo;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	static final String TAG = "DownloadDemo.MainActivity";// MainActivity.class.getSimpleName();
	static TextView mProgressText;
	static ProgressBar mProgressbar;
	static Notification notif;

	static NotificationManager mNotificationManager = null;
	Button startBtn;
	Button stopBtn;
	static RemoteViews mRemoteViews = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startBtn = (Button) findViewById(R.id.ui_start_button);
		stopBtn = (Button) findViewById(R.id.ui_stop_button);
		mProgressText = (TextView) findViewById(R.id.ui_progress_text);
		mProgressbar = (ProgressBar) findViewById(R.id.ui_progressbar);
		mProgressbar.setProgress(0);
		startBtn.setOnClickListener(this);
		stopBtn.setOnClickListener(this);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public void onClick(View v) {
		// 判断是否有网络连接
		if (!FileDownloadService.isNetWorkConnected(this)) {
			Toast.makeText(this, "无网络连接！请检查网络状况！", Toast.LENGTH_SHORT).show();
			return;
		}
		switch (v.getId()) {
		case R.id.ui_start_button:
			FileDownloadService.isPause = false;
			// 启动服务
			Intent intent = new Intent(MainActivity.this, FileDownloadService.class);
			intent.setAction(FileDownloadService.START_SERVICE);
			intent.putExtra("newDownload", new FileItemInfo(
					"http://7xn38b.com1.z0.glb.clouddn.com/mp4/SiliconValleyS02E03.mp4", "download2.mp4"));
			this.startService(intent);
			Log.i(TAG, "click the download button in MainActivity");
			break;
		case R.id.ui_stop_button:
			// 暂停下载
			FileDownloadService.isPause = true;
			Log.i(TAG, "click the pause button in MainActivity");
			break;
		}
	}

	public void sendNotification(Context context, String packageName) {
		Intent buttonIntent = new Intent(FileDownloadService.NOTIFICATION_CONTROL);
		notif = new Notification();
		notif.icon = R.drawable.download_icon;
		PendingIntent intent_next = PendingIntent.getBroadcast(context, 0, buttonIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews = new RemoteViews(packageName, R.layout.notification_layout);
		if (FileDownloadService.isPause) {
			mRemoteViews.setImageViewResource(R.id.notification_btn, R.drawable.pause_btn);
		} else {
			mRemoteViews.setImageViewResource(R.id.notification_btn, R.drawable.download_btn);
		}
		mRemoteViews.setOnClickPendingIntent(R.id.notification_btn, intent_next);
		// 通知栏显示所用到的布局文件
		notif.contentView = mRemoteViews;
		// notif.contentIntent = pIntent;
		mNotificationManager.notify(FileDownloadService.NOTIFICATION_ID, notif);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		FileDownloadService.isUIForeground = true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		FileDownloadService.isUIForeground = false;
		sendNotification(MainActivity.this, getPackageName());
	}

}
