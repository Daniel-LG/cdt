/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.pdom.db;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

/**
 * A container for storing a set of strings in the Database.  The container allows only one
 * instance of each string to be stored.
 * <p>
 * This implementation should only be used when the set is expected to be small.  It uses a
 * singly linked list for storing strings in Database.  Which means that a linear lookup is needed
 * to find strings in the list.  An in-memory, lazily-loaded, cache is provided so the list will
 * only be fully retrieved once in the lifetime of this instance.  A BTree will be more efficient
 * for larger sets.
 */
public class PDOMStringSet
{
	private final Database db;

	private long ptr;
	private long head;
	private long loaded;

	private Map<String, Long> lazyCache;

	public PDOMStringSet( Database db, long ptr ) throws CoreException
	{
		this.db = db;
		this.ptr = ptr;

		head = 0;
		loaded = 0;
	}

	public void clearCaches()
	{
		head = 0;
		loaded = 0;

		if( lazyCache != null )
			lazyCache = null;
	}

	private long getHead() throws CoreException
	{
		if( head == 0 )
			head = db.getRecPtr( ptr );
		return head;
	}

	// A simple enum describing the type of the information that is stored in the Database.  Each
	// enumerator represents a single field in the persistent structure and is able to answer its
	// offset in that structure.
	private static enum NodeType
	{
		Next,
		Item,
		_last;

		// NOTE: All fields are pointers, if that changes then these initializations will need
		//       to be updated.
		public final long offset = ordinal() * Database.PTR_SIZE;
		public static final int sizeof = (int)_last.offset;

		/** Return the value of the pointer stored in this field in the given instance. */
		public long get( Database db, long instance ) throws CoreException
		{
			return db.getRecPtr( instance + offset );
		}

		/** Store the given pointer into this field in the given instance. */
		public void put( Database db, long instance, long value ) throws CoreException
		{
			db.putRecPtr( instance + offset, value );
		}
	}

	/**
	 * Adds the given string to the receiving set.  May cause the entire list to be loaded from the
	 * Database while testing for uniqueness.  Returns the record of the string that was inserted into
	 * the list.
	 */
	public long add( String str ) throws CoreException
	{
		long record = find( str );
		if( record != 0 )
			return record;

		IString string = db.newString( str );
		record = string.getRecord();

		long new_node = db.malloc( NodeType.sizeof );
		NodeType.Next.put( db, new_node, getHead() );
		NodeType.Item.put( db, new_node, record );

		if( lazyCache == null )
			lazyCache = new HashMap<String, Long>();
		lazyCache.put( str, record );

		// If the Database has already been partially searched, then the loaded pointer will be after the
		// head.  Since we've already put this new record into the lazy cache, there is no reason to try to
		// load it again.  We put the new node at the start of the list so that it will be before the loaded
		// pointer.
		head = new_node;
		if( loaded == 0 )
			loaded = new_node;
		db.putRecPtr( ptr, new_node );
		return record;
	}

	/**
	 * Search for the given string in the receiver.  This could cause the entire list to be loaded from
	 * the Database.  The results are cached, so the list will only be loaded one time during the lifetime
	 * of this instance.  Returns the record of the String.
	 */
	public long find( String str ) throws CoreException
	{
		if( lazyCache != null )
		{
			Long l = lazyCache.get( str );
			if( l != null )
				return l.longValue();
		}

		// if there is nothing in the Database, then there is nothing to load
		if( getHead() == 0 )
			return 0;

		// otherwise prepare the cache for the data that is about to be loaded
		if( lazyCache == null )
			lazyCache = new HashMap<String, Long>();

		// if nothing has been loaded, then start loading with the head node, otherwise continue
		// loading from whatever is after the last loaded node
		long curr = loaded == 0 ? getHead() : NodeType.Next.get( db, loaded );
		while( curr != 0 )
		{
			long next = NodeType.Next.get( db, curr );
			long item = NodeType.Item.get( db, curr );

			IString string = db.getString( item );

			// put the value into the cache
			lazyCache.put( string.getString(), Long.valueOf( item ) );

			// return immediately if this is the target
			if( string.compare( str, true ) == 0 )
				return item;

			// otherwise keep looking
			loaded = curr;
			curr = next;
		}

		return 0;
	}

	/**
	 * Return a pointer to the record of the String that was removed.
	 */
	public long remove( String str ) throws CoreException
	{
		if( lazyCache != null )
			lazyCache.remove( str );

		long prev = 0;
		long curr = getHead();
		while( curr != 0 )
		{
			long next = NodeType.Next.get( db, curr );
			long item = NodeType.Item.get( db, curr );

			IString string = db.getString( item );

			if( string.compare( str, true ) == 0 )
			{
				if( head != curr )
					NodeType.Next.put( db, prev, next );
				else
				{
					db.putRecPtr( ptr, next );
					head = next;
				}

				db.free( curr );
				return item;
			}

			prev = curr;
			curr = next;
		}

		return 0;
	}
}