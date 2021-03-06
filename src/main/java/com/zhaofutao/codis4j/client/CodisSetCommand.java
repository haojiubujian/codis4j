package com.zhaofutao.codis4j.client;

import java.nio.ByteBuffer;

import com.zhaofutao.codis4j.CodisConnectionHandler;

import redis.clients.jedis.Jedis;

public abstract class CodisSetCommand<T> {
	private int maxRetryTime = 3;
	private CodisConnectionHandler connectionHandler;

	public CodisSetCommand(CodisConnectionHandler connectionHandler, int maxRetryTime) {
		this.connectionHandler = connectionHandler;
		this.maxRetryTime = maxRetryTime;
	}

	public abstract T execute(Jedis jedis);

	public T run(ByteBuffer key) {
		int groupId = connectionHandler.getGroupId(key);
		if (groupId == -1) {
			return null;
		}
		return runWithRetries(groupId);
	}

	public T run(String key) {
		int groupId = connectionHandler.getGroupId(key);
		if (groupId == -1) {
			return null;
		}
		return runWithRetries(groupId);
	}

	private T runWithRetries(int groupId) {
		Jedis jedis = null;
		int retry = 0;
		while (retry++ < this.maxRetryTime) {
			try {
				jedis = connectionHandler.getConnection(groupId, true);
				return execute(jedis);
			} catch (Exception e) {
				System.out.println(String.format("CodisSetCommand retry[%d], groupId[%d]", retry, groupId));
				// e.printStackTrace();
			} finally {
				releaseConnection(jedis);
			}
		}
		return null;
	}

	private void releaseConnection(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
		}
	}

}
