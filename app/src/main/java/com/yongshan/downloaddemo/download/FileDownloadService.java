package com.yongshan.downloaddemo.download;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.yongshan.downloaddemo.R;
import com.yongshan.downloaddemo.db.Dao;
import com.yongshan.downloaddemo.infos.FileItemInfo;
import com.yongshan.downloaddemo.infos.ThreadInfo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * @author SY
 *
 */
public class FileDownloadService extends Service {
	static final int NOTIFICATION_ID = 100;
	static final String START_SERVICE = "ACTION_START_SERVICE";
	static final String NOTIFICATION_CONTROL = "ACTION_NOTIFICATION_CONTROL";
	static boolean isPause = false; // 是否暂停下载
	static final int UPDATE_UI_PROGRESS = 0; // 更新UI控件
	static boolean isUIForeground = true; // UI是否获取焦点
	static boolean isNotificationDownloading = true; // 通知栏是否下载
	private static final int UPDATE_NOTIFICATION_PROGRESS = 1; // 更新通知栏控件
	private ExecutorService threadsPool = Executors.newCachedThreadPool(); // 带缓存的线程池
	private long fileSize = -1; // 文件大小
	private String downloadUrl = null;
	private String fileName = null;
	private final int threadNum = 5;
	private Dao dao = new Dao(this);
	private final String TAG = "DownloadDemo.FileDownloadService";
	/**
	 * Handler更新UI线程
	 */
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_UI_PROGRESS:
				MainActivity.mProgressbar.setProgress((int) (msg.getData().getLong("size") * 100 / fileSize));
				int progress = MainActivity.mProgressbar.getProgress();

				MainActivity.mProgressText.setText("下载进度:" + progress + " %");
				if (progress == 100) {
					MainActivity.mProgressText.setText("下载进度:" + progress + " %" + " 已完成！");
				}
				break;
			case UPDATE_NOTIFICATION_PROGRESS:
				MainActivity.notif.contentView.setTextViewText(R.id.notification_progress_text,
						"下载1   " + msg.getData().getLong("size") * 100 / fileSize + "%");
				MainActivity.notif.contentView.setProgressBar(R.id.notification_progressbar, 100,
						(int) (msg.getData().getLong("size") * 100 / fileSize), false);
				if (msg.getData().getLong("size") * 100 / fileSize == 100) {
					MainActivity.notif.contentView.setTextViewText(R.id.notification_progress_text,
							"下载1   已完成" + msg.getData().getLong("size") * 100 / fileSize + "%");
				}
				MainActivity.mNotificationManager.notify(FileDownloadService.NOTIFICATION_ID, MainActivity.notif);
				break;
			default:
				Log.i(TAG, "FileDownloadService.mHandler: default msg.what");
				break;
			}
		}
	};
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(FileDownloadService.NOTIFICATION_CONTROL)) {
				isNotificationDownloading = !isNotificationDownloading;
				if (isNotificationDownloading) {
					// 继续下载
					FileDownloadService.isPause = false;
					String path;
					// 判断存储于内存卡还是手机内存
					if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
						path = Environment.getExternalStorageDirectory() + File.separator + "AppDownload"
								+ File.separator;
					} else {
						path = Environment.getDataDirectory() + File.separator + "AppDownload" + File.separator;
					}
					File file = new File(path);
					if (!file.exists()) {
						file.mkdirs();
					}
					String filepath = path + fileName;
					DownloadTask downloadtask = new DownloadTask(downloadUrl, threadNum, filepath);
					threadsPool.execute(downloadtask); // 线程池中运行
					Log.i(TAG, "FileDownloadService.onStartCommand.downloadtask.start();");
					Log.i(TAG, "start service in notification's broadcastreceiver");
					MainActivity.notif.contentView.setImageViewResource(R.id.notification_btn, R.drawable.download_btn);
					MainActivity.mNotificationManager.notify(FileDownloadService.NOTIFICATION_ID, MainActivity.notif);
				} else {
					FileDownloadService.isPause = true;
					MainActivity.notif.contentView.setImageViewResource(R.id.notification_btn, R.drawable.pause_btn);
					MainActivity.mNotificationManager.notify(FileDownloadService.NOTIFICATION_ID, MainActivity.notif);
				}
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(FileDownloadService.START_SERVICE)) {
			// 获取sd卡路径
			String path;
			if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				path = Environment.getExternalStorageDirectory() + File.separator + "AppDownload" + File.separator;
			} else {
				path = Environment.getDataDirectory() + File.separator + "AppDownload" + File.separator;
			}
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
			FileItemInfo fileItemInfo = (FileItemInfo) intent.getSerializableExtra("newDownload");
			this.fileName = fileItemInfo.getFileName();
			this.downloadUrl = fileItemInfo.getUrl();
			String filepath = path + fileName;
			DownloadTask downloadtask = new DownloadTask(downloadUrl, threadNum, filepath);
			threadsPool.execute(downloadtask);
			Log.i(TAG, "FileDownloadService.onStartCommand.downloadtask.start();");
		}
		// 动态注册广播接收器
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(FileDownloadService.NOTIFICATION_CONTROL);
		registerReceiver(mBroadcastReceiver, intentFilter);
		return super.onStartCommand(intent, flags, startId);
	}

	public static boolean isNetWorkConnected(Context context) {
		if (context != null) {
			// 获取系统服务
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(CONNECTIVITY_SERVICE);
			// This method requires the caller to hold the permission
			// android.Manifest.permission.ACCESS_NETWORK_STATE.
			// 检查网络状况
			NetworkInfo mNetWorkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetWorkInfo != null) {
				return mNetWorkInfo.isAvailable();
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// 解注册
		if (mBroadcastReceiver != null) {
			unregisterReceiver(mBroadcastReceiver);
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 判断是否是第一次 下载
	 */
	private boolean isNewDownloadTask(String url) {
		return dao.isHasInfors(url);
	}

	class DownloadTask extends Thread {
		private String downloadUrl;
		private int threadNum;
		private String filePath;
		private long blockSize;

		public DownloadTask(String downloadUrl, int threadNum, String fileptah) {
			this.downloadUrl = downloadUrl;
			this.threadNum = threadNum;
			this.filePath = fileptah;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			try {
				URL url = new URL(downloadUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				fileSize = conn.getContentLength();
				if (fileSize <= 0) {
					Log.i(TAG, "Service.downloadTask:网络文件大小异常！");
					return;
				}
				MainActivity.mProgressbar.setMax(100);

				blockSize = (fileSize % threadNum) == 0 ? fileSize / threadNum : fileSize / threadNum + 1;

				Log.i(TAG, "Service.downloadTask: fileSize: " + fileSize + "  blockSize:" + blockSize);

				File file = new File(filePath);
				List<ThreadInfo> infos;
				FileDownloadThread[] threads;
				// 是新任务则新建数据库记录并存储
				if (isNewDownloadTask(downloadUrl)) {
					infos = new ArrayList<ThreadInfo>();
					threads = new FileDownloadThread[threadNum];
					for (int i = 0; i < threads.length; i++) {
						if (i == threads.length - 1) {
							// 最后一个线程需要判断下载截止点
							threads[i] = new FileDownloadThread(file, downloadUrl, i + 1, blockSize * i,
									((blockSize * (i + 1) - 1) >= (fileSize - 1)) ? (fileSize - 1)
											: (blockSize * (i + 1) - 1),
									0, dao);
							infos.add(new ThreadInfo(i + 1, blockSize * i, ((blockSize * (i + 1) - 1) >= (fileSize - 1))
									? (fileSize - 1) : (blockSize * (i + 1) - 1), 0, downloadUrl));
						} else {
							threads[i] = new FileDownloadThread(file, downloadUrl, i + 1, blockSize * i,
									blockSize * (i + 1) - 1, 0, dao);
							infos.add(new ThreadInfo(i + 1, blockSize * i, blockSize * (i + 1) - 1, 0, downloadUrl));
						}

						threads[i].setName("Thread:" + i);
						threadsPool.execute(threads[i]);
					}
					dao.saveInfos(infos);
				} else { // 是未完成任务则继续下载
					infos = dao.getInfos(downloadUrl);
					threads = new FileDownloadThread[infos.size()];
					Log.i(TAG, "FileDownloadService.!isNewDownloadTask(downloadUrl) infos.size: " + infos.size());
					for (int i = 0; i < infos.size(); i++) {
						threads[i] = new FileDownloadThread(file, infos.get(i).getUrl(), infos.get(i).getThreadId(),
								infos.get(i).getStartPos(), infos.get(i).getEndPos(), infos.get(i).getDownloadedSize(),
								dao);
						threadsPool.execute(threads[i]);
					}

				}
				long fileDownloadedSize = (threadNum - threads.length) * blockSize;
				boolean isFileDownloadFinished = false;
				while (!isFileDownloadFinished) {
					if (FileDownloadService.isPause) {
						Log.i(TAG, " Service.downloadTask.ispause: all of downloadSize: " + fileDownloadedSize);
						break;
					}
					isFileDownloadFinished = true;
					fileDownloadedSize = (threadNum - threads.length) * blockSize;
					// 检查是否全部完成
					for (int i = 0; i < threads.length; i++) {
						fileDownloadedSize += threads[i].getThreadDownloadedSize();
						if (!threads[i].isCompleted()) {
							isFileDownloadFinished = false;
						}
					}
					// handler更新控件
					Message msg = new Message();
					msg.getData().putLong("size", fileDownloadedSize);
					if (isUIForeground) {
						msg.what = UPDATE_UI_PROGRESS;
					} else {
						msg.what = UPDATE_NOTIFICATION_PROGRESS;
					}
					mHandler.sendMessage(msg);
					Thread.sleep(1000);
				}

				if (isFileDownloadFinished) {
					Log.i(TAG, " Service.downloadTask.isisFileDownloadFinished: all of downloadSize: "
							+ fileDownloadedSize);
					stopSelf(); // 停止服务
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}
