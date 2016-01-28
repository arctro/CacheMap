package com.arctro;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.arctro.exceptions.ValueDoesNotExistException;

/**
 * A hashmap that stores values and expire times for the values. Unlike
 * a {@link com.arctro.CacheMap CacheMap} it does not check if
 * a value has expired until a function has been called on it. It then
 * checks if the value has expired and removes it if needed.
 * @author Ben McLean
 * @version 1.0
 * @param <K> The key type
 * @param <V> The value type
 */
public class PassiveCacheMap<K, V>{
	//Stores the values
	HashMap<K, V> cache;
	//Stores the expire time of the values
	HashMap<K, Long> cacheExpire;
	
	/**
	 * Create an empty PassiveCacheMap
	 */
	public PassiveCacheMap(){
		cache = new LinkedHashMap<K, V>();
		cacheExpire = new LinkedHashMap<K, Long>();
	}
	
	/**
	 * Set a value
	 * @param key Key to access value
	 * @param value Value to store
	 * @param expire Time to expire
	 */
	public void put(K key, V value, long expire){
		cache.put(key, value);
		cacheExpire.put(key, expire);
	}
	
	/**
	 * Change a value
	 * @param key Key to access value
	 * @param value Value to store
	 * @throws ValueDoesNotExistException  Value does not exist or has expired
	 */
	public void set(K key, V value) throws ValueDoesNotExistException{
		if(expired(key)){
			throw new ValueDoesNotExistException();
		}
		
		cache.put(key, value);
	}
	
	/**
	 * Get a value
	 * @param key Key to get value by
	 * @return The value associated with key
	 */
	public V get(K key){
		//If value is expired, delete and return null
		if(expired(key)){
			return null;
		}
		
		return cache.get(key);
	}
	
	/**
	 * Get the expire time of a value
	 * @param key The key of the value to check
	 * @return The expire time of the value
	 */
	public long getExpire(K key){
		return cacheExpire.get(key);
	}
	
	/**
	 * Remove key/value
	 * @param key The key to remove
	 */
	public void remove(K key){
		cache.remove(key);
		cacheExpire.remove(key);
	}
	
	/**
	 * Get the HashMap storing all values
	 * @return Raw values
	 */
	public HashMap<K, V> raw(){
		return cache;
	}
	
	/**
	 * Get the HashMap storing all expire times
	 * @return The expire HashMap
	 */
	public HashMap<K, Long> rawExpire(){
		return cacheExpire;
	}
	
	/**
	 * Checks if key is expired
	 * @param key Key to check
	 * @return False if key is not expired, true if key is expired
	 */
	public boolean expired(K key){
		if(cacheExpire.get(key) != null){
			if(System.currentTimeMillis() > cacheExpire.get(key)){
				cacheExpire.remove(key);
				cache.remove(key);
				return true;
			}else{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Removes all expired values
	 */
	public void removeExpired(){
		Iterator<K> i = cacheExpire.keySet().iterator();
		while(i.hasNext()){
			K key = i.next();
			if(System.currentTimeMillis() > cacheExpire.get(key)){
				cache.remove(key);
				cacheExpire.remove(key);
			}
		}
	}
}
