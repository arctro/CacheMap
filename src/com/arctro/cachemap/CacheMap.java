package com.arctro.cachemap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.arctro.cachemap.exceptions.ValueDoesNotExistException;

/**
 * A hashmap that expires values after a defined time. Checks the next 10 keys every millisecond 
 * for expired values, although this can be changed with the {@link #setDelay(int) setDelay(int)}
 * and {@link #setStageSize(int) setStageSize(int)} function. An expiry time for the entire hashmap
 * can also be set, in which all values will be removed and no new data can be entered.
 * @author Ben McLean 
 * @version 1.6
 * @param <K> The key type
 * @param <V> The value type
 */
public class CacheMap<K, V>{
	//Stores the values
	HashMap<K, V> cache;
	//Stores the expire time of the values
	HashMap<K, Long> cacheExpire;
	
	Iterator<K> stagedIterator;
	
	int delay = 1;
	boolean running = true;
	
	int stageSize = 10;
	
	/**
	 * Create an empty CacheMap with no expiry
	 */
	public CacheMap(){
		cache = new LinkedHashMap<K, V>();
		cacheExpire = new LinkedHashMap<K, Long>();
		stagedIterator = cacheExpire.keySet().iterator();
		
		Thread t = new Thread(){
			public void run(){
				while(running){
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					removeExpiredStage();
				}
			}
		};
		t.start();
	}
	
	/**
	 * Create a CacheMap with an expiry time
	 * @param expire The time in milliseconds to expire the CacheMap
	 */
	public CacheMap(final long expire){
		cache = new LinkedHashMap<K, V>();
		cacheExpire = new LinkedHashMap<K, Long>();
		stagedIterator = cacheExpire.keySet().iterator();
		
		Thread t = new Thread(){
			public void run(){
				while(running){
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if(System.currentTimeMillis() > expire){
						running = false;
						//TODO remove all data
					}
					
					removeExpiredStage();
				}
			}
		};
		t.start();
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
	 * @throws ValueDoesNotExistException Value does not exist or has expired
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
		if(cacheExpire.get(key) != null && System.currentTimeMillis() < cacheExpire.get(key)){
			return false;
		}
		return true;
	}
	
	/**
	 * Set the check delay time
	 * @param delay The delay time
	 */
	public void setDelay(int delay){
		this.delay = delay;
	}
	
	/**
	 * Sets the size of the stage when iterating through the keys. For example if stage size is set 
	 * to 10 the map will check the first 10 keys, then on the next check it will check the next 10
	 * keys, and so on.
	 * @param size Stage size
	 */
	public void setStageSize(int size){
		stageSize = size;
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
	
	//Checks all rows for expired stageSize at a time
	private void removeExpiredStage(){
		int count = 0;
		
		//Iterate through iterator until reaches end or stageSize
		while(stagedIterator.hasNext() && count <= stageSize){
			K key = stagedIterator.next();
			if(System.currentTimeMillis() > cacheExpire.get(key)){
				cache.remove(key);
				cacheExpire.remove(key);
			}
			
			count++;
		}
		
		//If end of iteration reset iterator
		if(count < 10 || !stagedIterator.hasNext()){
			stagedIterator = cacheExpire.keySet().iterator();
		}
	}
}
