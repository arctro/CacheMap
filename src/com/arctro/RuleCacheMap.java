package com.arctro;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.arctro.exceptions.ValueDoesNotExistException;
import com.arctro.supporting.Rule;

/**
 * A hashmap that removes values based on a rule defined in the constructor.
 * The rule receives the values of the total number of 'hits' (number of times
 * it has been accessed), the number of hits in the last x milliseconds (defined
 * by {@link com.arctro.supporting.Rule#interval() interval()}), the age of the 
 * value, and the value itself. The map also allows the entire map to be expired.
 * @author Ben McLean 
 * @version 1.0
 * @param <K> The key type
 * @param <V> The value type
 */
public class RuleCacheMap<K, V>{
	//Stores the values
	ConcurrentHashMap<K, V> cache;
	//Stores the total count of accesses
	ConcurrentHashMap<K, Integer> totalHits;
	//Stores the number of hits in the last x minutes
	PassiveCacheMap<K, Integer> hits;
	//Stores the creation time of the value
	ConcurrentHashMap<K, Long> created;
	
	//The rule for expiring the values
	Rule rule;
	
	int delay = 50;
	boolean running = true;
	
	/**
	 * Create an empty CacheMap with no expiry
	 * @param rule The rule to expire values by
	 */
	public RuleCacheMap(Rule rule){
		cache = new ConcurrentHashMap<K, V>();
		totalHits = new ConcurrentHashMap<K, Integer>();
		hits = new PassiveCacheMap<K, Integer>();
		created = new ConcurrentHashMap<K, Long>();
		this.rule = rule;
		
		Thread t = new Thread(){
			public void run(){
				while(running){
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					removeExpired();
				}
			}
		};
		t.start();
	}
	
	/**
	 * Create a CacheMap with an expiry time
	 * @param rule The rule to expire values by
	 * @param expire The time in milliseconds to expire the CacheMap
	 */
	public RuleCacheMap(Rule rule, final long expire){
		cache = new ConcurrentHashMap<K, V>();
		totalHits = new ConcurrentHashMap<K, Integer>();
		hits = new PassiveCacheMap<K, Integer>();
		created = new ConcurrentHashMap<K, Long>();
		this.rule = rule;
		
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
					
					removeExpired();
				}
			}
		};
		t.start();
	}
	
	/**
	 * Set a value
	 * @param key Key to access value
	 * @param value Value to store
	 */
	public void put(K key, V value){
		cache.put(key, value);
		totalHits.put(key, 0);
		hits.put(key, 0, System.currentTimeMillis() + rule.interval());
		created.put(key, System.currentTimeMillis());
	}
	
	/**
	 * Modify a value
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
		if(cache.get(key) == null){
			return null;
		}
		
		totalHits.put(key, totalHits.get(key) + 1);
		if(hits.expired(key)){
			hits.put(key, 0, System.currentTimeMillis() + rule.interval());
		}else{
			try {
				hits.set(key, hits.get(key) + 1);
			} catch (ValueDoesNotExistException e) {
				hits.put(key, 0, System.currentTimeMillis() + rule.interval());
				e.printStackTrace();
			}
		}
		return cache.get(key);
	}
	
	/**
	 * Remove key/value
	 * @param key The key to remove
	 */
	public void remove(K key){
		cache.remove(key);
		totalHits.remove(key);
		hits.remove(key);
	}
	
	/**
	 * Get the HashMap storing all values
	 * @return Raw values
	 */
	public ConcurrentHashMap<K, V> raw(){
		return cache;
	}
	
	public Rule getRule(){
		return rule;
	}
	
	/**
	 * Checks if key is expired
	 * @param key Key to check
	 * @return False if key is not expired, true if key is expired
	 */
	public boolean expired(K key){
		if(cache.get(key) == null){
			return true;
		}
		return rule.expired(getHits(key, true), this.totalHits.get(key), (System.currentTimeMillis() - created.get(key)), cache.get(key));
	}
	
	/**
	 * Set the check delay time
	 * @param delay The delay time
	 */
	public void setDelay(int delay){
		this.delay = delay;
	}
	
	/**
	 * Removes all expired values
	 */
	public void removeExpired(){
		Set<K> keys = cache.keySet();
		Iterator<K> i = keys.iterator();
		while(i.hasNext()){
			K key = i.next();
			Integer hits = getHits(key, true);
			Integer totalHits = this.totalHits.get(key);
			Long age = (System.currentTimeMillis() - created.get(key));
			V value = cache.get(key);
			//System.out.println(hits + " " + totalHits + " " + age + " " + value);
			if(rule.expired(hits, totalHits, age, value)){
				cache.remove(key);
				this.totalHits.remove(key);
				this.hits.remove(key);
				created.remove(key);
			}
		}
	}
	
	private int getHits(K key, boolean ignore){
		if(hits.expired(key)){
			hits.put(key, 0, System.currentTimeMillis() + rule.interval());
			return 0;
		}
		
		if(!ignore){
			try {
				hits.set(key, hits.get(key) + 1);
			} catch (ValueDoesNotExistException e) {
				e.printStackTrace();
			}
		}
		
		return hits.get(key);
	}
}
