/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntegralType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongLongValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIShortValue;
import org.eclipse.cdt.debug.core.model.ICDebugElementErrorStatus;
import org.eclipse.cdt.debug.core.model.ICExpressionEvaluator;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

/**
 * 
 * The value of a variable.
 * 
 * @since Aug 9, 2002
 */
public class CValue extends CDebugElement implements ICValue
{
	/**
	 * Parent variable.
	 */
	private CVariable fParent = null;

	/**
	 * Cached value.
	 */
	private String fValueString = null;

	/**
	 * Underlying CDI value.
	 */
	private ICDIValue fCDIValue;

	/**
	 * List of child variables.
	 */
	private List fVariables = Collections.EMPTY_LIST;

	/**
	 * Constructor for CValue.
	 * @param target
	 */
	public CValue( CVariable parent, ICDIValue cdiValue )
	{
		super( (CDebugTarget)parent.getDebugTarget() );
		fParent = parent;
		fCDIValue = cdiValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException
	{
		String typeName = null;
		try
		{
			if ( getUnderlyingValue() != null )
			{
				typeName = getUnderlyingValue().getTypeName();
			}
		}
		catch( CDIException e )
		{
			logError( e );
		}
		return typeName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException
	{
		if ( fValueString == null && getUnderlyingValue() != null )
		{
			try
			{
				fValueString = processUnderlyingValue( getUnderlyingValue() );
			}
			catch( CDIException e )
			{
				fValueString = e.getMessage();
			}
		}
		return fValueString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException
	{
		List list = getVariables0();
		return (IVariable[])list.toArray( new IVariable[list.size()] );
	}

	protected synchronized List getVariables0() throws DebugException 
	{
		if ( !isAllocated() || !hasVariables() )
			return Collections.EMPTY_LIST;
		if ( fVariables.size() == 0 )
		{
			try
			{
				List vars = getCDIVariables();
				
				if ( vars.size() > 1 )
					fVariables = CArrayPartition.splitArray( this, vars, 0, vars.size() - 1 );
				else
				{
					fVariables = new ArrayList( vars.size() );
					Iterator it = vars.iterator();
					while( it.hasNext() )
					{
						fVariables.add( new CModificationVariable( this, (ICDIVariable)it.next() ) );
					}
				}
			}
			catch( DebugException e )
			{
				fVariables = new ArrayList( 1 );
				CModificationVariable var = new CModificationVariable( this, new CVariable.ErrorVariable( null, e ) );
				var.setStatus( ICDebugElementErrorStatus.ERROR, e.getMessage() );
				fVariables.add( var );
			}
		}
		return fVariables;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException
	{
		try
		{
			ICDIValue value = getUnderlyingValue();
			if ( value != null )
				return value.getChildrenNumber() > 0;
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		return false;
	}

	public ICDIValue getUnderlyingValue()
	{
		return fCDIValue;
	}
	
	protected List getCDIVariables() throws DebugException
	{
		ICDIVariable[] vars = null;
		try
		{
			ICDIValue value = getUnderlyingValue();
			if ( value != null )
			{
				vars = value.getVariables();
				// Quick fix. 
				// getVariables should return an empty array instead of null.
				if ( vars == null )
				{
					vars = new ICDIVariable[0];
				}
			}
		}
		catch( CDIException e )
		{
			requestFailed( "not available: ", e );
		}
		return Arrays.asList( vars );
	}

	protected String processCDIValue( String cdiValue )
	{
		String result = null;
		if ( cdiValue != null )
		{
			result = cdiValue.trim();
			if ( result.startsWith( "@" ) ) // Reference
			{
				int end = result.indexOf( ':' );
				if ( end == -1 )
					end = result.length();
				result = result.substring( 1, end );
			}
			else if ( result.startsWith( "0x" ) )
			{
				int end = result.indexOf( ' ' );
				if ( end == -1 )
					end = result.length();
				result = result.substring( 0, end );
			}
			else if ( result.endsWith( "\'" ) )
			{
				int start = result.indexOf( '\'' );
				if ( start != -1 && result.length() - start == 3 )
				{
					result = result.substring( start );
				}
				else
				{
					result = null;
				}
			}
		}
		return result;
	}

	public synchronized void setChanged( boolean changed ) throws DebugException
	{
		if ( changed )
		{
			fValueString = null;
		}
		Iterator it = fVariables.iterator();
		while( it.hasNext() )
		{
			((CVariable)it.next()).setChanged( changed );
		}
	}

	protected void dispose()
	{
		Iterator it = fVariables.iterator();
		while( it.hasNext() )
		{
			((CVariable)it.next()).dispose();
		}
	}

	public CVariable getParentVariable()
	{
		return fParent;
	}

	private String processUnderlyingValue( ICDIValue cdiValue ) throws CDIException
	{
		if ( cdiValue != null )
		{
			if ( cdiValue instanceof ICDICharValue )
				return getCharValueString( (ICDICharValue)cdiValue );
			else if ( cdiValue instanceof ICDIShortValue )
				return getShortValueString( (ICDIShortValue)cdiValue );
			else if ( cdiValue instanceof ICDIIntValue )
				return getIntValueString( (ICDIIntValue)cdiValue );
			else if ( cdiValue instanceof ICDILongValue )
				return getLongValueString( (ICDILongValue)cdiValue );
			else if ( cdiValue instanceof ICDILongLongValue )
				return getLongLongValueString( (ICDILongLongValue)cdiValue );
			else if ( cdiValue instanceof ICDIFloatValue )
				return getFloatValueString( (ICDIFloatValue)cdiValue );
			else if ( cdiValue instanceof ICDIDoubleValue )
				return getDoubleValueString( (ICDIDoubleValue)cdiValue );
			else if ( cdiValue instanceof ICDIPointerValue )
				return getPointerValueString( (ICDIPointerValue)cdiValue );
			else if ( cdiValue instanceof ICDIReferenceValue )
				return getReferenceValueString( (ICDIReferenceValue)cdiValue );
			else
				return cdiValue.getValueString();
		}
		return null;
	}

	private String getCharValueString( ICDICharValue value ) throws CDIException
	{
		switch( getParentVariable().getFormat() )
		{
			case ICDIFormat.NATURAL:
			{
				byte byteValue = (byte)value.byteValue();
				return ( ( Character.isISOControl( (char)byteValue ) &&
						   byteValue != '\b' &&
						   byteValue != '\t' && 
						   byteValue != '\n' && 
						   byteValue != '\f' && 
						   byteValue != '\r' ) || byteValue < 0 ) ? "" : new String( new byte[] { '\'', byteValue, '\'' } );
			}
			case ICDIFormat.DECIMAL:
			{
				return ( isUnsigned() ) ? Integer.toString( value.shortValue() ) : 
										  Integer.toString( (byte)value.byteValue() );
			}
			case ICDIFormat.HEXADECIMAL:
			{
				StringBuffer sb = new StringBuffer( "0x" ); 
				String stringValue = ( isUnsigned() ) ? Integer.toHexString( value.shortValue() ) : Integer.toHexString( (byte)value.byteValue() );
				sb.append( ( stringValue.length() > 2 ) ? stringValue.substring( stringValue.length() - 2 ) : stringValue );
				return sb.toString();
			}
		}
		return null;
	}

	private String getShortValueString( ICDIShortValue value ) throws CDIException
	{
		switch( getParentVariable().getFormat() )
		{
			case ICDIFormat.NATURAL:
			case ICDIFormat.DECIMAL:
				return ( isUnsigned() ) ? Integer.toString( value.intValue() ) : Short.toString( value.shortValue() );
			case ICDIFormat.HEXADECIMAL:
			{
				StringBuffer sb = new StringBuffer( "0x" ); 
				String stringValue = Integer.toHexString( ( isUnsigned() ) ? value.intValue() : value.shortValue() );
				sb.append( ( stringValue.length() > 4 ) ? stringValue.substring( stringValue.length() - 4 ) : stringValue );
				return sb.toString();
			}
		}
		return null;
	}

	private String getIntValueString( ICDIIntValue value ) throws CDIException
	{
		switch( getParentVariable().getFormat() )
		{
			case ICDIFormat.NATURAL:
			case ICDIFormat.DECIMAL:
				return ( isUnsigned() ) ? Long.toString( value.longValue() ) : Integer.toString( value.intValue() );
			case ICDIFormat.HEXADECIMAL:
			{
				StringBuffer sb = new StringBuffer( "0x" ); 
				String stringValue = ( isUnsigned() ) ? Long.toHexString( value.longValue() ) : Integer.toHexString( value.intValue() );
				sb.append( ( stringValue.length() > 8 ) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
				return sb.toString();
			}
		}
		return null;
	}

	private String getLongValueString( ICDILongValue value ) throws CDIException
	{
		switch( getParentVariable().getFormat() )
		{
			case ICDIFormat.NATURAL:
			case ICDIFormat.DECIMAL:
				return ( isUnsigned() ) ? Long.toString( value.longValue() ) : Integer.toString( value.intValue() );
			case ICDIFormat.HEXADECIMAL:
			{
				StringBuffer sb = new StringBuffer( "0x" ); 
				String stringValue = Long.toHexString( ( isUnsigned() ) ? value.longValue() : value.intValue() );
				sb.append( ( stringValue.length() > 8 ) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
				return sb.toString();
			}
		}
		return null;
	}

	private String getLongLongValueString( ICDILongLongValue value ) throws CDIException
	{
		switch( getParentVariable().getFormat() )
		{
			case ICDIFormat.NATURAL:
			case ICDIFormat.DECIMAL:
			{
				if ( isUnsigned() )
				{
					BigInteger bigValue = new BigInteger( value.getValueString() );
					return bigValue.toString();
				}
				else
					return Long.toString( value.longValue() );
			}
			case ICDIFormat.HEXADECIMAL:
			{
				StringBuffer sb = new StringBuffer( "0x" );
				 
				if ( isUnsigned() )
				{
					BigInteger bigValue = new BigInteger( value.getValueString() );
					sb.append( bigValue.toString( 16 ) );
				}
				else
					sb.append( Long.toHexString( value.longValue() ) );
				return sb.toString();
			}
		}
		return null;
	}

	private String getFloatValueString( ICDIFloatValue value ) throws CDIException
	{
		float floatValue = value.floatValue();
		Float flt = new Float( floatValue );
		if ( flt.isNaN() || flt.isInfinite() )
			return "";
		long longValue = flt.longValue();
		switch( getParentVariable().getFormat() )
		{
			case ICDIFormat.NATURAL:
				return Float.toString( floatValue );
			case ICDIFormat.DECIMAL:
				return Long.toString( longValue );
			case ICDIFormat.HEXADECIMAL:
			{
				StringBuffer sb = new StringBuffer( "0x" ); 
				String stringValue = Long.toHexString( longValue );
				sb.append( ( stringValue.length() > 8 ) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
				return sb.toString();
			}
		}
		return null;
	}

	private String getDoubleValueString( ICDIDoubleValue value ) throws CDIException
	{
		double doubleValue = value.doubleValue();
		Double dbl = new Double( doubleValue );
		if ( dbl.isNaN() || dbl.isInfinite() )
			return "";
		long longValue = dbl.longValue();
		switch( getParentVariable().getFormat() )
		{
			case ICDIFormat.NATURAL:
				return dbl.toString();
			case ICDIFormat.DECIMAL:
				return Long.toString( longValue );
			case ICDIFormat.HEXADECIMAL:
			{
				StringBuffer sb = new StringBuffer( "0x" ); 
				String stringValue = Long.toHexString( longValue );
				sb.append( ( stringValue.length() > 16 ) ? stringValue.substring( stringValue.length() - 16 ) : stringValue );
				return sb.toString();
			}
		}
		return null;
	}

	private String getPointerValueString( ICDIPointerValue value ) throws CDIException
	{
		long longValue = value.pointerValue();
		switch( getParentVariable().getFormat() )
		{
			case ICDIFormat.DECIMAL:
				return Long.toString( longValue );
			case ICDIFormat.NATURAL:
			case ICDIFormat.HEXADECIMAL:
			{
				StringBuffer sb = new StringBuffer( "0x" ); 
				String stringValue = Long.toHexString( longValue );
				sb.append( ( stringValue.length() > 8 ) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
				return sb.toString();
			}
		}
		return null;
	}

	private String getReferenceValueString( ICDIReferenceValue value ) throws CDIException
	{
		long longValue = value.referenceValue();
		switch( getParentVariable().getFormat() )
		{
			case ICDIFormat.DECIMAL:
				return Long.toString( longValue );
			case ICDIFormat.NATURAL:
			case ICDIFormat.HEXADECIMAL:
			{
				StringBuffer sb = new StringBuffer( "0x" ); 
				String stringValue = Long.toHexString( longValue );
				sb.append( ( stringValue.length() > 8 ) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
				return sb.toString();
			}
		}
		return null;
	}

	private boolean isUnsigned()
	{
		boolean result = false;
		try
		{
			if ( getParentVariable().getCDIVariable() != null )
				result = ( getParentVariable().getCDIVariable().getType() instanceof ICDIIntegralType ) ? 
							((ICDIIntegralType)getParentVariable().getCDIVariable().getType()).isUnsigned() : false;
		}
		catch( CDIException e )
		{
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICValue#evaluateAsExpression()
	 */
	public String evaluateAsExpression()
	{
		ICExpressionEvaluator ee = (ICExpressionEvaluator)getDebugTarget().getAdapter( ICExpressionEvaluator.class );
		String valueString = null; 
		if ( ee != null && ee.canEvaluate() )
		{
			try
			{
				if ( getParentVariable() != null && !getParentVariable().isAccessSpecifier() )
					valueString = ee.evaluateExpressionToString( getParentVariable().getQualifiedName() );
			}
			catch( DebugException e )
			{
				valueString = e.getMessage();
			}
		}
		return valueString;
	}

	protected void reset() throws DebugException
	{
		fValueString = null;
		Iterator it = fVariables.iterator();
		while( it.hasNext() )
		{
			((CVariable)it.next()).reset();
		}
	}
}
