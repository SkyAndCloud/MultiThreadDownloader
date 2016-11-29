package com.yongshan.downloaddemo.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.yongshan.downloaddemo.db.Dao;

import android.util.Log;

public class FileDownloadThread extends Thread {
	private boolean isThreadCompleted = false;
	private long threadDownloadedSize = 0;
	private File file;
	private String downloadUrl;
	private int threadId;
	private long startPos;
	private long endPos;
	private Dao dao;
	private final int BUFFER_SIZE = 1024;
	private final String TAG = "DownloadDemo.FileDownloadThread";

	public FileDownloadThread(File file, String downloadUrl, int threadId, long startPos, long endPos,
			long threadDownloadedSize, Dao dao) {
		super();
		this.file = file;
		this.downloadUrl = downloadUrl;
		this.threadId = threadId;
		this.startPos = startPos;
		this.endPos = endPos;
		this.threadDownloadedSize = threadDownloadedSize;
		this.dao = dao;
	}

	@Override
	public void run() {

		BufferedInputStream bis = null;
		RandomAccessFile raf = null;

		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
			// 设置下载开始与终止点
			conn.setRequestProperty("Range", "bytes=" + (startPos + threadDownloadedSize) + "-" + endPos);
			Log.i(TAG + ".FileDownloadThread",
					this.getThreadId() + " bytes: from " + (startPos + threadDownloadedSize) + " to " + endPos);
			byte[] buffer = new byte[BUFFER_SIZE];
			bis = new BufferedInputStream(conn.getInputStream());

			raf = new RandomAccessFile(file, "rwd");
			raf.seek(startPos + threadDownloadedSize);
			int len;
			while ((len = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
				if (FileDownloadService.isPause) {
					Log.i(TAG, "FileDownloadThread.ispause " + this.getThreadId() + "has finished, all size: "
							+ threadDownloadedSize);
					// 下载停止时更新数据库记录
					dao.updateInfos(this.getThreadId(), threadDownloadedSize, this.getDownloadUrl());
					return;
				}
				raf.write(buffer, 0, len);
				threadDownloadedSize += len;

			}
			isThreadCompleted = true;
			dao.delete(downloadUrl, threadId);
			Log.i(TAG, "FileDownloadThread " + this.getThreadId() + "has finished,all size: " + threadDownloadedSize);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isCompleted() {
		return isThreadCompleted;
	}

	public long getThreadDownloadedSize() {
		return threadDownloadedSize;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public int getThreadId() {
		return threadId;
	}

	public long getStartPos() {
		return startPos;
	}

	public long getEndPos() {
		return endPos;
	}

}
