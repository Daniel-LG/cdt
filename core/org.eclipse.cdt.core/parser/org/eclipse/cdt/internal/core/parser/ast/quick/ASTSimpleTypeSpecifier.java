/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;

/**
 * @author jcamelon
 *
 */
public class ASTSimpleTypeSpecifier implements IASTSimpleTypeSpecifier
{
	private final boolean isTypename;
    private final Type kind;
	private final String typeName;  
	private final boolean isLong, isShort, isSigned, isUnsigned; 
	
	private static final Map nameMap;
	static 
	{
		nameMap  = new Hashtable();
		nameMap.put( Type.BOOL, "bool");
		nameMap.put( Type.CHAR, "char");
		nameMap.put( Type.DOUBLE, "double");
		nameMap.put( Type.FLOAT, "float");
		nameMap.put( Type.INT, "int");
		nameMap.put( Type.VOID, "void" );
		nameMap.put( Type.WCHAR_T, "wchar_t" );
	}
    /**
     * @param kind
     * @param typeName
     */
    public ASTSimpleTypeSpecifier(Type kind, ITokenDuple typeName, boolean isShort, boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename)
    {
        this.kind = kind;
        this.isLong = isLong;
        this.isShort = isShort;
        this.isSigned = isSigned;
        this.isUnsigned = isUnsigned;
        this.isTypename = isTypename;       
		
		
		StringBuffer type = new StringBuffer();
		if( this.kind == IASTSimpleTypeSpecifier.Type.CHAR || this.kind == IASTSimpleTypeSpecifier.Type.WCHAR_T )
		{
			if (isUnsigned())
				type.append("unsigned ");
			type.append( (String)nameMap.get( this.kind ));
		}
		else if( this.kind == Type.BOOL || this.kind == Type.FLOAT || this.kind == Type.VOID )
		{
			type.append( (String) nameMap.get( this.kind ));
		}
		else if( this.kind == Type.INT )
		{
			if (isUnsigned())
				type.append("unsigned ");
			if (isShort())
				type.append("short ");
			if (isLong())
				type.append("long ");
			type.append( (String)nameMap.get( this.kind ));
		}
		else if( this.kind == Type.DOUBLE )
		{
			if (isLong())
				type.append("long ");
			type.append( (String)nameMap.get( this.kind ));
		}
		else if( this.kind == Type.CLASS_OR_TYPENAME || this.kind == Type.TEMPLATE )
		{
			if (isTypename() )
				type.append("typename ");
			type.append(typeName.toString());
		}
		else if( this.kind == Type.UNSPECIFIED )
		{
			if (isUnsigned())
				type.append("unsigned ");
			if (isShort())
				type.append("short ");
			if (isLong())
				type.append("long ");
			if (isSigned())
				type.append("signed ");
		}
		this.typeName = type.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#getType()
     */
    public Type getType()
    {
        return kind;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#getTypename()
     */
    public String getTypename()
    {
        return typeName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isLong()
     */
    public boolean isLong()
    {
        return isLong;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isShort()
     */
    public boolean isShort()
    {
        return isShort;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isSigned()
     */
    public boolean isSigned()
    {
        return isSigned;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isUnsigned()
     */
    public boolean isUnsigned()
    {
        return isUnsigned;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier#isTypename()
     */
    public boolean isTypename()
    {
        return isTypename;
    }
}
