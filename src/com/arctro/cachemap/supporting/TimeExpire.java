package com.arctro.cachemap.supporting;

/**
 * Holds the expire time for a key
 * @author Ben McLean
 * @version 1.0
 * @param <K> The key type
 * @deprecated No longer required
 */
public class TimeExpire<K> {
	/**
	 * The key of the value to associate expire time with
	 */
	public K key;
	/**
	 * The expire time of the value
	 */
	public long expire;
	
	/**
	 * Create a new instance
	 * @param key Key to associated expire time with
	 * @param expire Expire time for key
	 */
	public TimeExpire(K key, long expire){
		this.key = key;
		this.expire = expire;
	}
}
