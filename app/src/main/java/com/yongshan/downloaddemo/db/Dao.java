package com.yongshan.downloaddemo.db;

import java.util.ArrayList;
import java.util.List;

import com.yongshan.downloaddemo.infos.ThreadInfo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 
 * 一个数据库业务类
 */
public class Dao {
	private static DBOpenHelper mHelper = null;
	private final String TAG = "DownloadDemo.db";

	public Dao(Context context) {
		mHelper = DBOpenHelper.getInstance(context);
	}

	public SQLiteDatabase getConnection() {
		SQLiteDatabase sqliteDatabase = null;
		try {
			sqliteDatabase = mHelper.getReadableDatabase();
		} catch (Exception e) {
		}
		return sqliteDatabase;
	}

	/**
	 * 查看数据库中是否有符合url的未完成任务
	 */
	public synchronized boolean isHasInfors(String url) {
		SQLiteDatabase database = getConnection();
		int count = -1;
		Cursor cursor = null;
		try {
			String sql = "select count(*)  from thread_info where url=?";
			cursor = database.rawQuery(sql, new String[] { url });
			if (cursor.moveToFirst()) { // 判断游标是否为空
				count = cursor.getInt(0); // 获取第一个列的id值
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != database) {
				database.close();
			}
			if (null != cursor) {
				cursor.close();
			}
		}
		return count == 0;
	}

	/**
	 * 保存 下载的具体信息
	 */
	public synchronized void saveInfos(List<ThreadInfo> infos) {
		SQLiteDatabase database = getConnection();
		try {
			for (ThreadInfo info : infos) {
				String sql = "insert into thread_info(thread_id,start_pos, end_pos,downloaded_size,url) values (?,?,?,?,?)";
				Object[] bindArgs = { info.getThreadId(), info.getStartPos(), info.getEndPos(),
						info.getDownloadedSize(), info.getUrl() };
				database.execSQL(sql, bindArgs);
				Log.i(TAG, "saveInfos: " + "id:" + info.getThreadId() + " from " + info.getStartPos() + " to "
						+ info.getEndPos() + " hasFinished " + info.getDownloadedSize() + " " + info.getUrl());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != database) {
				database.close();
			}
		}
	}

	/**
	 * 得到已保存的下载具体信息
	 */
	public synchronized List<ThreadInfo> getInfos(String url) {
		List<ThreadInfo> list = new ArrayList<ThreadInfo>(); // 下载线程信息列表
		SQLiteDatabase database = getConnection();
		Cursor cursor = null;
		try {
			String sql = "select thread_id, start_pos, end_pos,downloaded_size,url from thread_info where url=?";
			cursor = database.rawQuery(sql, new String[] { url });
			while (cursor.moveToNext()) {
				ThreadInfo info = new ThreadInfo(cursor.getInt(0), cursor.getLong(1), cursor.getLong(2),
						cursor.getLong(3), cursor.getString(4));
				list.add(info);
				Log.i(TAG, "getInfos" + " id " + cursor.getInt(0) + " from " + cursor.getLong(1) + " to "
						+ cursor.getLong(2) + " hasFinished " + cursor.getLong(3) + " " + cursor.getString(4));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != database) {
				database.close();
			}
			if (null != cursor) {
				cursor.close();
			}
		}
		return list;
	}

	/**
	 * 更新数据库中的下载信息
	 */
	public synchronized void updateInfos(int threadId, long downloadedSize, String url) {
		SQLiteDatabase database = getConnection();
		try {
			String sql = "update thread_info set downloaded_size=? where thread_id=? and url=?";
			Object[] bindArgs = { downloadedSize, threadId, url };
			database.execSQL(sql, bindArgs);
			// 打印日志信息
			Log.i(TAG, "updateInfos:" + " hasFinished " + downloadedSize + " id " + threadId + url);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != database) {
				database.close();
			}
		}
	}

	/**
	 * 单条线程下载完成后删除数据库中的数据
	 */
	public synchronized void delete(String url, int threadId) {
		SQLiteDatabase database = getConnection();
		try {
			database.delete("thread_info", "url=? and thread_id=?", new String[] { url, String.valueOf(threadId) });
			Log.i(TAG, "delete" + url + " id " + threadId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != database) {
				database.close();
			}
		}
	}
}
