package com.arctro.cachemap.supporting;

/**
 * Defines a rule for calculating if a {@link com.arctro.cachemap.RuleCacheMap RateCacheMap} entry
 * should be expired or removed.
 * @author Ben McLean
 * @version 1.0
 */
public abstract class Rule {
	/**
	 * @return The interval time for the hits counter (i.e when the counter should be reset)
	 */
	public int interval(){
		return 1000;
	}
	
	/**
	 * The rule for entry deletion
	 * @param hits The number of hits/uses in the last x milliseconds (as defined by {@link #interval() interval()}.
	 * @param totalHits The total number of hits/uses of the entry
	 * @param age The age of the entry
	 * @param value The value of the entry
	 * @return If the entry has expired/should be deleted
	 */
	public abstract boolean expired(int hits, int totalHits, long age, Object value);
}
