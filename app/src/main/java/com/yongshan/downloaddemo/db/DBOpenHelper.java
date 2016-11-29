package com.yongshan.downloaddemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 建立一个数据库帮助类
 */
public class DBOpenHelper extends SQLiteOpenHelper {
	// download.db-->数据库名
	private final static String DB_NAME = "downloadDemo.db";
	private final static String CREATE_SQL = "create table thread_info(_id integer PRIMARY KEY AUTOINCREMENT, thread_id integer, "
			+ "start_pos integer, end_pos integer, downloaded_size integer,url txt)";
	private final static String DROP_SQL = "drop table if exists thread_info";
	private final static int DB_VERSION = 1;
	private static DBOpenHelper dbOpenHelper;

	public DBOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	// 单例模式
	public static DBOpenHelper getInstance(Context context) {
		if (null == dbOpenHelper) {
			dbOpenHelper = new DBOpenHelper(context);
		}

		return dbOpenHelper;
	}

	/**
	 * 在downloadDemo.db数据库下创建一个thread_info表存储下载信息
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DROP_SQL);
		db.execSQL(CREATE_SQL);
	}

}