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
package org.eclipse.cdt.core.parser.tests;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTPointerToFunction;
import org.eclipse.cdt.core.parser.ast.IASTPointerToMethod;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.ast.IASTArrayModifier;
/**
 * @author jcamelon
 *
 */
public class QuickParseASTTests extends BaseASTTest
{
    public QuickParseASTTests(String a)
    {
        super(a);
    }
    /**
     * Test code: int x = 5;
     * Purpose: to test the simple decaration in it's simplest form.
     */
    public void testIntGlobal() throws Exception
    {
        // Parse and get the translation Unit
        IASTCompilationUnit translationUnit = parse("int x = 5;");
        Iterator i = translationUnit.getDeclarations();
        assertTrue(i.hasNext());
        IASTVariable var = (IASTVariable)i.next();
        assertTrue(
            var.getAbstractDeclaration().getTypeSpecifier()
                instanceof IASTSimpleTypeSpecifier);
        assertTrue(
            ((IASTSimpleTypeSpecifier)var
                .getAbstractDeclaration()
                .getTypeSpecifier())
                .getType()
                == IASTSimpleTypeSpecifier.Type.INT);
        assertEquals(var.getName(), "x");
        assertNotNull(var.getInitializerClause());
        assertNotNull(var.getInitializerClause().getAssigmentExpression());
        assertFalse(i.hasNext());
    }
    /**
     * Test code: class A { } a;
     * Purpose: tests the use of a classSpecifier in 
     */
    public void testEmptyClass() throws Exception
    {
        // Parse and get the translation unit
        Writer code = new StringWriter();
        code.write("class A { } a;");
        IASTCompilationUnit translationUnit = parse(code.toString());
        Iterator i = translationUnit.getDeclarations();
        assertTrue(i.hasNext());
        IASTVariable var = (IASTVariable)i.next();
        assertEquals(var.getName(), "a");
        assertTrue(
            var.getAbstractDeclaration().getTypeSpecifier()
                instanceof IASTClassSpecifier);
        IASTClassSpecifier classSpec =
            (IASTClassSpecifier)var.getAbstractDeclaration().getTypeSpecifier();
        assertEquals(classSpec.getName(), "A");
        assertFalse(classSpec.getDeclarations().hasNext());
        assertFalse(i.hasNext());
    }
    /**
     * Test code: class A { public: int x; };
     * Purpose: tests a declaration in a class scope.
     */
    public void testSimpleClassMember() throws Exception
    {
        // Parse and get the translaton unit
        Writer code = new StringWriter();
        code.write("class A { public: int x; };");
        IASTCompilationUnit cu = parse(code.toString());
        Iterator i = cu.getDeclarations();
        assertTrue(i.hasNext());
        IASTAbstractTypeSpecifierDeclaration declaration =
            (IASTAbstractTypeSpecifierDeclaration)i.next();
        assertFalse(i.hasNext());
        assertTrue(
            declaration.getTypeSpecifier() instanceof IASTClassSpecifier);
        assertTrue(
            ((IASTClassSpecifier)declaration.getTypeSpecifier()).getClassKind()
                == ASTClassKind.CLASS);
        Iterator j =
            ((IASTClassSpecifier)declaration.getTypeSpecifier())
                .getDeclarations();
        assertTrue(j.hasNext());
        IASTField field = (IASTField)j.next();
        assertFalse(j.hasNext());
        assertTrue(field.getName().equals("x"));
        assertTrue(
            field.getAbstractDeclaration().getTypeSpecifier()
                instanceof IASTSimpleTypeSpecifier);
        assertTrue(
            ((IASTSimpleTypeSpecifier)field
                .getAbstractDeclaration()
                .getTypeSpecifier())
                .getType()
                == IASTSimpleTypeSpecifier.Type.INT);
    }
    public void testNamespaceDefinition() throws Exception
    {
        for (int i = 0; i < 2; ++i)
        {
            IASTCompilationUnit translationUnit;
            if (i == 0)
                translationUnit = parse("namespace KingJohn { int x; }");
            else
                translationUnit = parse("namespace { int x; }");
            Iterator iter = translationUnit.getDeclarations();
            assertTrue(iter.hasNext());
            IASTNamespaceDefinition namespaceDefinition =
                (IASTNamespaceDefinition)iter.next();
            assertFalse(iter.hasNext());
            if (i == 0)
                assertTrue(namespaceDefinition.getName().equals("KingJohn"));
            else
                assertEquals(namespaceDefinition.getName(), "");
            Iterator j = namespaceDefinition.getDeclarations();
            assertTrue(j.hasNext());
            IASTVariable var = (IASTVariable)j.next();
            assertFalse(j.hasNext());
            assertTrue(
                var.getAbstractDeclaration().getTypeSpecifier()
                    instanceof IASTSimpleTypeSpecifier);
            assertTrue(
                ((IASTSimpleTypeSpecifier)var
                    .getAbstractDeclaration()
                    .getTypeSpecifier())
                    .getType()
                    == IASTSimpleTypeSpecifier.Type.INT);
            assertEquals(var.getName(), "x");
        }
    }
    
	public void testLinkageSpecification() throws Exception
	{
		for( int i = 0; i < 2; ++i )
		{
			IASTCompilationUnit compilationUnit; 
			if( i == 0  )
				compilationUnit = parse("extern \"C\" { int x(void); }");
			else
				compilationUnit = parse("extern \"ADA\" int x(void);");
				
			Iterator declarations = compilationUnit.getDeclarations();
			assertTrue( declarations.hasNext() );
			IASTLinkageSpecification linkage = (IASTLinkageSpecification)declarations.next(); 
			assertFalse( declarations.hasNext() );
			
			if( i == 0 )
				assertEquals( "C", linkage.getLinkageString() );
			else
				assertEquals( "ADA", linkage.getLinkageString() );
			
			Iterator subDeclarations = linkage.getDeclarations();
			assertTrue( subDeclarations.hasNext() );
			IASTFunction function = (IASTFunction)subDeclarations.next();
			assertFalse( subDeclarations.hasNext());
			
			assertEquals( ((IASTSimpleTypeSpecifier)function.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
			assertEquals( function.getName(), "x");
			
			Iterator parameters = function.getParameters();
			assertTrue( parameters.hasNext() );
			IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
			assertFalse( parameters.hasNext() );
			assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.VOID );
			assertEquals( parm.getName(), "" );
		}
	}
    
	public void testEnumSpecifier() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "enum { yo, go = 3, away };\n");
		code.write( "enum hasAString { last = 666 };");
		IASTCompilationUnit translationUnit = parse( code.toString() );
		Iterator declarations = translationUnit.getDeclarations();
		
		for( int i = 0; i < 2; ++i )
		{ 
			assertTrue( declarations.hasNext() );
			IASTAbstractTypeSpecifierDeclaration abstractDeclaration = (IASTAbstractTypeSpecifierDeclaration)declarations.next(); 
			IASTEnumerationSpecifier enumerationSpec = (IASTEnumerationSpecifier)abstractDeclaration.getTypeSpecifier();
			if( i == 0 )
				assertEquals( enumerationSpec.getName(), "" );
			else
				assertEquals( enumerationSpec.getName(), "hasAString" );
			Iterator j = enumerationSpec.getEnumerators();
			
			if( i == 0 )
			{
				IASTEnumerator enumerator1 = (IASTEnumerator)j.next();
				assertEquals( enumerator1.getName(), "yo");
				assertNull( enumerator1.getInitialValue() );
				IASTEnumerator enumerator2 = (IASTEnumerator)j.next();
				assertNotNull( enumerator2.getInitialValue() );
				assertEquals( enumerator2.getInitialValue().getLiteralString(), "3");
				assertEquals( enumerator2.getInitialValue().getExpressionKind(), IASTExpression.Kind.PRIMARY_INTEGER_LITERAL );
				assertEquals( enumerator2.getName(), "go"); 
				IASTEnumerator enumerator3 = (IASTEnumerator)j.next();
				assertEquals( enumerator3.getName(), "away");
				assertNull( enumerator3.getInitialValue() );
				assertFalse( j.hasNext() );  
			}
			else
			{
				IASTEnumerator enumerator2 = (IASTEnumerator)j.next();
				assertNotNull( enumerator2.getInitialValue() );
				assertEquals( enumerator2.getInitialValue().getLiteralString(), "666");
				assertEquals( enumerator2.getInitialValue().getExpressionKind(), IASTExpression.Kind.PRIMARY_INTEGER_LITERAL );
				assertEquals( enumerator2.getName(), "last"); 
				assertFalse( j.hasNext() );
			}
		}		
	}
	
	public void testTypedef() throws Exception
	{
		Iterator i = parse( "typedef const struct A * const cpStructA;").getDeclarations();
		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)i.next(); 
		assertFalse( i.hasNext() );
		assertTrue( typedef.getAbstractDeclarator().isConst() );
		assertTrue( typedef.getAbstractDeclarator().getTypeSpecifier() instanceof IASTElaboratedTypeSpecifier );
		IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier)typedef.getAbstractDeclarator().getTypeSpecifier();
		assertEquals( elab.getName(), "A");
		assertEquals( elab.getClassKind(), ASTClassKind.STRUCT );
		assertTrue( typedef.getAbstractDeclarator().getPointerOperators().hasNext() );
		Iterator pIter = (Iterator)typedef.getAbstractDeclarator().getPointerOperators();
		ASTPointerOperator po =(ASTPointerOperator)pIter.next(); 
		assertEquals( po, ASTPointerOperator.CONST_POINTER ); 
		assertFalse( pIter.hasNext() );
		assertEquals( typedef.getName(), "cpStructA");
		
	}
	
	public void testUsingClauses() throws Exception
	{
		Writer code = new StringWriter();
		
		code.write("using namespace A::B::C;\n");
		code.write("using namespace C;\n");
		code.write("using B::f;\n");
		code.write("using ::f;\n");
		code.write("using typename crap::de::crap;");
		Iterator declarations = parse(code.toString()).getDeclarations();
		
		IASTUsingDirective usingDirective = (IASTUsingDirective)declarations.next();
		assertEquals( usingDirective.getNamespaceName(), "A::B::C" );
		
		usingDirective = (IASTUsingDirective)declarations.next();
		assertEquals( usingDirective.getNamespaceName(), "C" );
		
		IASTUsingDeclaration usingDeclaration = (IASTUsingDeclaration)declarations.next(); 
		assertEquals( usingDeclaration.usingTypeName(), "B::f" ); 
		
		usingDeclaration = (IASTUsingDeclaration)declarations.next(); 
		assertEquals( usingDeclaration.usingTypeName(), "::f" ); 
		usingDeclaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( usingDeclaration.usingTypeName(), "crap::de::crap" );
		assertTrue( usingDeclaration.isTypename() ); 
		  
		assertFalse( declarations.hasNext());
	}
	
	/**
	 * Test code: class A : public B, private C, virtual protected D { public: int x, y; float a,b,c; }
	 * Purpose: tests a declaration in a class scope.
	 */
	public void testSimpleClassMembers() throws Exception {
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("class A : public B, private C, virtual protected D { public: int x, y; float a,b,c; };");
		Iterator declarations = parse( code.toString() ).getDeclarations();
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertFalse( declarations.hasNext() );
		Iterator baseClauses = classSpec.getBaseClauses();
		IASTBaseSpecifier baseSpec = (IASTBaseSpecifier)baseClauses.next(); 
		assertEquals( baseSpec.getParentClassName(), "B" );
		assertEquals( baseSpec.getAccess(), ASTAccessVisibility.PUBLIC );
		baseSpec = (IASTBaseSpecifier)baseClauses.next();
		assertEquals( baseSpec.getParentClassName(), "C" );
		assertEquals( baseSpec.getAccess(), ASTAccessVisibility.PRIVATE);
		baseSpec = (IASTBaseSpecifier)baseClauses.next();
		assertEquals( baseSpec.getAccess(), ASTAccessVisibility.PROTECTED );
		assertTrue( baseSpec.isVirtual() );
		assertEquals( baseSpec.getParentClassName(), "D" );
		assertFalse( baseClauses.hasNext() );

		Iterator members = classSpec.getDeclarations();
		IASTField field = (IASTField)members.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)field.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
		assertEquals( field.getName(), "x" ); 
		
		field = (IASTField)members.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)field.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
		assertEquals( field.getName(), "y" ); 
		
		field = (IASTField)members.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)field.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.FLOAT ); 
		assertEquals( field.getName(), "a" ); 
		field = (IASTField)members.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)field.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.FLOAT ); 
		assertEquals( field.getName(), "b" ); 
		field = (IASTField)members.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)field.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.FLOAT ); 
		assertEquals( field.getName(), "c" );
		assertFalse( members.hasNext() ); 
		
	}
	
		/**
		 * Test code: int myFunction( void ); 
		 */
		public void testSimpleFunctionDeclaration() throws Exception
		{
			// Parse and get the translaton unit
			Writer code = new StringWriter();
			code.write("void myFunction( void );");
			Iterator declarations = parse( code.toString()).getDeclarations();
			IASTFunction f1 = (IASTFunction)declarations.next(); 
			assertFalse( declarations.hasNext() ); 
			assertEquals( f1.getName(), "myFunction");
			assertEquals( ((IASTSimpleTypeSpecifier)f1.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.VOID ); 
			Iterator parameters = f1.getParameters();
			IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
			assertFalse( parameters.hasNext() );
			assertEquals( parm.getName(), "" );
			assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.VOID );
			
		}
	
	/**
	 * Test code: bool myFunction( int parm1 = 3 * 4, double parm2 );
	 * @throws Exception
	 */
	public void testFunctionDeclarationWithParameters() throws Exception
	{
		// Parse and get the translaton unit
		Writer code = new StringWriter();
		code.write("bool myFunction( int parm1 = 3 * 4, double parm2 );");
		Iterator declarations = parse(code.toString()).getDeclarations();
		IASTFunction f1 = (IASTFunction)declarations.next(); 
		assertFalse( declarations.hasNext() ); 
		assertEquals( f1.getName(), "myFunction");
		assertEquals( ((IASTSimpleTypeSpecifier)f1.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.BOOL ); 
		Iterator parameters = f1.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertEquals( parm.getName(), "parm1" );
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( parm.getDefaultValue().getAssigmentExpression().getExpressionKind(), IASTExpression.Kind.MULTIPLICATIVE_MULTIPLY );
		
		parm = (IASTParameterDeclaration)parameters.next(); 
		assertEquals( parm.getName(), "parm2" );
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.DOUBLE );
		assertNull( parm.getDefaultValue()  );
		assertFalse( parameters.hasNext());
		
	}
	
	public void testAssignmentExpressions() throws Exception 
	{
		parse( "int x = y = z = 5;");
	}
	
	public void testBug39348() throws Exception
	{
		parse("unsigned char a[sizeof (struct sss)];");
	}
	
	public void testBug39501() throws Exception
	{
		parse("struct A { A() throw (int); };");
	}
	
	public void testBug39349() throws Exception
	{
		parse( "enum foo {  foo1   = 0,  foo2   = 0xffffffffffffffffULL,  foo3   = 0xf0fffffffffffffeLLU };" ); 
	}
	
	public void testBug39544() throws Exception	{
		parse("wchar_t wc = L'X';"); 
	}
	
	public void testBug36290() throws Exception {
		parse("typedef void ( A:: * pFunction ) ( void ); ");
		parse("typedef void (boo) ( void ); ");
		parse("typedef void boo (void); ");
	}
	public void testBug36769B() throws Exception {
		parse("class X { operator int(); } \n");
		parse("class X { operator int*(); } \n");
		parse("class X { operator int&(); } \n");
		parse("class X { operator A(); } \n");
		parse("class X { operator A*(); } \n");
		parse("class X { operator A&(); } \n");
		
		parse("X::operator int() { } \n");
		parse("X::operator int*() { } \n");
		parse("X::operator int&() { } \n");
		parse("X::operator A() { } \n");
		parse("X::operator A*() { } \n");
		parse("X::operator A&() { } \n");
		
		parse("template <class A,B> class X<A,C> { operator int(); } \n");
		parse("template <class A,B> class X<A,C> { operator int*(); } \n");
		parse("template <class A,B> class X<A,C> { operator int&(); } \n");
		parse("template <class A,B> class X<A,C> { operator A(); } \n");
		parse("template <class A,B> class X<A,C> { operator A*(); } \n");
		parse("template <class A,B> class X<A,C> { operator A&(); } \n");

		parse("template <class A,B> X<A,C>::operator int() { } \n");
		parse("template <class A,B> X<A,C>::operator int*() { } \n");
		parse("template <class A,B> X<A,C>::operator int&() { } \n");
		parse("template <class A,B> X<A,C>::operator A() { } \n");
		parse("template <class A,B> X<A,C>::operator A*() { } \n");
		parse("template <class A,B> X<A,C>::operator A&() { } \n");
	}
	public void testBug36932C() throws Exception {
		parse("X::X( ) : var( new int ) {}");
		parse("X::X( ) : var( new int(5) ) {}");
		parse("X::X( ) : var( new int(B) ) {}");
		parse("X::X( ) : var( new int(B,C) ) {}");
		parse("X::X( ) : var( new int[5] ) {}");
		parse("X::X( ) : var( new int[5][10] ) {}");
		parse("X::X( ) : var( new int[B] ) {}");
		parse("X::X( ) : var( new int[B][C][D] ) {}");

		parse("X::X( ) : var( new A ) {}");
		parse("X::X( ) : var( new A(5) ) {}");
		parse("X::X( ) : var( new A(B) ) {}");
		parse("X::X( ) : var( new A(B,C) ) {}");
		parse("X::X( ) : var( new A[5] ) {}");
		parse("X::X( ) : var( new A[5][10] ) {}");
		parse("X::X( ) : var( new A[B] ) {}");
		parse("X::X( ) : var( new A[B][C][D] ) {}");

		parse("X::X( ) : var( new (int) ) {}");
		parse("X::X( ) : var( new (int)(5) ) {}");
		parse("X::X( ) : var( new (int)(B) ) {}");
		parse("X::X( ) : var( new (int)(B,C) ) {}");
		parse("X::X( ) : var( new (int)[5] ) {}");
		parse("X::X( ) : var( new (int)[5][10] ) {}");
		parse("X::X( ) : var( new (int)[B] ) {}");
		parse("X::X( ) : var( new (int)[B][C][D] ) {}");

		parse("X::X( ) : var( new (A) ) {}");
		parse("X::X( ) : var( new (A)(5) ) {}");
		parse("X::X( ) : var( new (A)(B) ) {}");
		parse("X::X( ) : var( new (A)(B,C) ) {}");
		parse("X::X( ) : var( new (A)[5] ) {}");
		parse("X::X( ) : var( new (A)[5][10] ) {}");
		parse("X::X( ) : var( new (A)[B] ) {}");
		parse("X::X( ) : var( new (A)[B][C][D] ) {}");

		parse("X::X( ) : var( new (0) int ) {}");
		parse("X::X( ) : var( new (0) int(5) ) {}");
		parse("X::X( ) : var( new (0) int(B) ) {}");
		parse("X::X( ) : var( new (0) int(B,C) ) {}");
		parse("X::X( ) : var( new (0) int[5] ) {}");
		parse("X::X( ) : var( new (0) int[5][10] ) {}");
		parse("X::X( ) : var( new (0) int[B] ) {}");
		parse("X::X( ) : var( new (0) int[B][C][D] ) {}");

		parse("X::X( ) : var( new (0) A ) {}");
		parse("X::X( ) : var( new (0) A(5) ) {}");
		parse("X::X( ) : var( new (0) A(B) ) {}");
		parse("X::X( ) : var( new (0) A(B,C) ) {}");
		parse("X::X( ) : var( new (0) A[5] ) {}");
		parse("X::X( ) : var( new (0) A[5][10] ) {}");
		parse("X::X( ) : var( new (0) A[B] ) {}");
		parse("X::X( ) : var( new (0) A[B][C][D] ) {}");

		parse("X::X( ) : var( new (0) (int) ) {}");
		parse("X::X( ) : var( new (0) (int)(5) ) {}");
		parse("X::X( ) : var( new (0) (int)(B) ) {}");
		parse("X::X( ) : var( new (0) (int)(B,C) ) {}");
		parse("X::X( ) : var( new (0) (int)[5] ) {}");
		parse("X::X( ) : var( new (0) (int)[5][10] ) {}");
		parse("X::X( ) : var( new (0) (int)[B] ) {}");
		parse("X::X( ) : var( new (0) (int)[B][C][D] ) {}");

		parse("X::X( ) : var( new (0) (A) ) {}");
		parse("X::X( ) : var( new (0) (A)(5) ) {}");
		parse("X::X( ) : var( new (0) (A)(B) ) {}");
		parse("X::X( ) : var( new (0) (A)(B,C) ) {}");
		parse("X::X( ) : var( new (0) (A)[5] ) {}");
		parse("X::X( ) : var( new (0) (A)[5][10] ) {}");
		parse("X::X( ) : var( new (0) (A)[B] ) {}");
		parse("X::X( ) : var( new (0) (A)[B][C][D] ) {}");

		parse("X::X( ) : var( new (P) int ) {}");
		parse("X::X( ) : var( new (P) int(5) ) {}");
		parse("X::X( ) : var( new (P) int(B) ) {}");
		parse("X::X( ) : var( new (P) int(B,C) ) {}");
		parse("X::X( ) : var( new (P) int[5] ) {}");
		parse("X::X( ) : var( new (P) int[5][10] ) {}");
		parse("X::X( ) : var( new (P) int[B] ) {}");
		parse("X::X( ) : var( new (P) int[B][C][D] ) {}");

		parse("X::X( ) : var( new (P) A ) {}");
		parse("X::X( ) : var( new (P) A(5) ) {}");
		parse("X::X( ) : var( new (P) A(B) ) {}");
		parse("X::X( ) : var( new (P) A(B,C) ) {}");
		parse("X::X( ) : var( new (P) A[5] ) {}");
		parse("X::X( ) : var( new (P) A[5][10] ) {}");
		parse("X::X( ) : var( new (P) A[B] ) {}");
		parse("X::X( ) : var( new (P) A[B][C][D] ) {}");

		parse("X::X( ) : var( new (P) (int) ) {}");
		parse("X::X( ) : var( new (P) (int)(5) ) {}");
		parse("X::X( ) : var( new (P) (int)(B) ) {}");
		parse("X::X( ) : var( new (P) (int)(B,C) ) {}");
		parse("X::X( ) : var( new (P) (int)[5] ) {}");
		parse("X::X( ) : var( new (P) (int)[5][10] ) {}");
		parse("X::X( ) : var( new (P) (int)[B] ) {}");
		parse("X::X( ) : var( new (P) (int)[B][C][D] ) {}");

		parse("X::X( ) : var( new (P) (A) ) {}");
		parse("X::X( ) : var( new (P) (A)(5) ) {}");
		parse("X::X( ) : var( new (P) (A)(B) ) {}");
		parse("X::X( ) : var( new (P) (A)(B,C) ) {}");
		parse("X::X( ) : var( new (P) (A)[5] ) {}");
		parse("X::X( ) : var( new (P) (A)[5][10] ) {}");
		parse("X::X( ) : var( new (P) (A)[B] ) {}");
		parse("X::X( ) : var( new (P) (A)[B][C][D] ) {}");
	}
    

	public void testBugSingleton192() throws Exception {
		parse("int Test::* pMember_;" );
	}
	public void testBug36931() throws Exception {
		parse("A::nested::nested(){}; ");
		parse("int A::nested::foo() {} ");
		parse("int A::nested::operator+() {} ");
		parse("A::nested::operator int() {} ");
		parse("static const int A::nested::i = 1; ");
        
		parse("template <class B,C> A<B>::nested::nested(){}; ");
		parse("template <class B,C> int A::nested<B,D>::foo() {} ");
		parse("template <class B,C> int A<B,C>::nested<C,B>::operator+() {} ");
		parse("template <class B,C> A::nested::operator int() {} ");
	}
	
	public void testBug37019() throws Exception {
		parse("static const A a( 1, 0 );");
	}	

	public void testBug36766and36769A() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class _CharT, class _Alloc>\n");
		code.write("rope<_CharT, _Alloc>::rope(size_t __n, _CharT __c,\n");
		code.write("const allocator_type& __a): _Base(__a)\n");
		code.write("{}\n");
		parse(code.toString());
	}

	public void testBug36766and36769B() throws Exception {
		Writer code = new StringWriter();
		code.write("template<class _CharT>\n");
		code.write("bool _Rope_insert_char_consumer<_CharT>::operator()\n");
		code.write("(const _CharT* __leaf, size_t __n)\n");
		code.write("{}\n");
		parse(code.toString());
	}

	public void testBug36766and36769C() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class _CharT, class _Alloc>\n");
		code.write("_Rope_char_ref_proxy<_CharT, _Alloc>&\n");
		code.write(
			"_Rope_char_ref_proxy<_CharT, _Alloc>::operator= (_CharT __c)\n");
		code.write("{}\n");
		parse(code.toString());
	}
	
	public void testBug36766and36769D() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class _CharT, class _Alloc>\n");
		code.write("rope<_CharT, _Alloc>::~rope()\n");
		code.write("{}\n");
		parse(code.toString());
	}
    
	public void testBug36932A() throws Exception {
		parse("A::A( ) : var( new char [ (unsigned)bufSize ] ) {}");
	}
        
	public void testBug36932B() throws Exception {
		parse(" p = new int; ");
		parse(" p = new int(5); ");
		parse(" p = new int(B); ");
		parse(" p = new int(B,C); ");
		parse(" p = new int[5]; ");
		parse(" p = new int[5][10]; ");
		parse(" p = new int[B]; ");
		parse(" p = new int[B][C][D]; ");
 
		parse(" p = new A; ");
		parse(" p = new A(5); ");
		parse(" p = new A(B); ");
		parse(" p = new A(B,C); ");
		parse(" p = new A[5]; ");
		parse(" p = new A[5][10]; ");
		parse(" p = new A[B]; ");
		parse(" p = new A[B][C][D]; ");

		parse(" p = new (int); ");
		parse(" p = new (int)(5); ");
		parse(" p = new (int)(B); ");
		parse(" p = new (int)(B,C); ");
		parse(" p = new (int)[5]; ");
		parse(" p = new (int)[5][10]; ");
		parse(" p = new (int)[B]; ");
		parse(" p = new (int)[B][C][D]; ");

		parse(" p = new (A); ");
		parse(" p = new (A)(5); ");
		parse(" p = new (A)(B); ");
		parse(" p = new (A)(B,C); ");
		parse(" p = new (A)[5]; ");
		parse(" p = new (A)[5][10]; ");
		parse(" p = new (A)[B]; ");
		parse(" p = new (A)[B][C][D]; ");

		parse(" p = new (0) int; ");
		parse(" p = new (0) int(5); ");
		parse(" p = new (0) int(B); ");
		parse(" p = new (0) int(B,C); ");
		parse(" p = new (0) int[5]; ");
		parse(" p = new (0) int[5][10]; ");
		parse(" p = new (0) int[B]; ");
		parse(" p = new (0) int[B][C][D]; ");

		parse(" p = new (0) A; ");
		parse(" p = new (0) A(5); ");
		parse(" p = new (0) A(B); ");
		parse(" p = new (0) A(B,C); ");
		parse(" p = new (0) A[5]; ");
		parse(" p = new (0) A[5][10]; ");
		parse(" p = new (0) A[B]; ");
		parse(" p = new (0) A[B][C][D]; ");

		parse(" p = new (0) (int); ");
		parse(" p = new (0) (int)(5); ");
		parse(" p = new (0) (int)(B); ");
		parse(" p = new (0) (int)(B,C); ");
		parse(" p = new (0) (int)[5]; ");
		parse(" p = new (0) (int)[5][10]; ");
		parse(" p = new (0) (int)[B]; ");
		parse(" p = new (0) (int)[B][C][D]; ");

		parse(" p = new (0) (A); ");
		parse(" p = new (0) (A)(5); ");
		parse(" p = new (0) (A)(B); ");
		parse(" p = new (0) (A)(B,C); ");
		parse(" p = new (0) (A)[5]; ");
		parse(" p = new (0) (A)[5][10]; ");
		parse(" p = new (0) (A)[B]; ");
		parse(" p = new (0) (A)[B][C][D]; ");

		parse(" p = new (P) int; ");
		parse(" p = new (P) int(5); ");
		parse(" p = new (P) int(B); ");
		parse(" p = new (P) int(B,C); ");
		parse(" p = new (P) int[5]; ");
		parse(" p = new (P) int[5][10]; ");
		parse(" p = new (P) int[B]; ");
		parse(" p = new (P) int[B][C][D]; ");

		parse(" p = new (P) A; ");
		parse(" p = new (P) A(5); ");
		parse(" p = new (P) A(B); ");
		parse(" p = new (P) A(B,C); ");
		parse(" p = new (P) A[5]; ");
		parse(" p = new (P) A[5][10]; ");
		parse(" p = new (P) A[B]; ");
		parse(" p = new (P) A[B][C][D]; ");

		parse(" p = new (P) (int); ");
		parse(" p = new (P) (int)(5); ");
		parse(" p = new (P) (int)(B); ");
		parse(" p = new (P) (int)(B,C); ");
		parse(" p = new (P) (int)[5]; ");
		parse(" p = new (P) (int)[5][10]; ");
		parse(" p = new (P) (int)[B]; ");
		parse(" p = new (P) (int)[B][C][D]; ");

		parse(" p = new (P) (A); ");
		parse(" p = new (P) (A)(5); ");
		parse(" p = new (P) (A)(B); ");
		parse(" p = new (P) (A)(B,C); ");
		parse(" p = new (P) (A)[5]; ");
		parse(" p = new (P) (A)[5][10]; ");
		parse(" p = new (P) (A)[B]; ");
		parse(" p = new (P) (A)[B][C][D]; ");
	}

	public void testBug36769A() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class A, B> cls<A, C>::operator op &() const {}\n");
		code.write("template <class A, B> cls<A, C>::cls() {}\n");
		code.write("template <class A, B> cls<A, C>::~cls() {}\n");
			
		parse( code.toString());
	}
	
	public void testBug36714() throws Exception {
		Writer code = new StringWriter();
		code.write("unsigned long a = 0UL;\n");
		code.write("unsigned long a2 = 0L; \n");
	
		parse( code.toString() );
	}
	
	public void testBugFunctor758() throws Exception {
		parse( "template <typename Fun> Functor(Fun fun) : spImpl_(new FunctorHandler<Functor, Fun>(fun)){}" ); 
	}
	
	public void testBug36932() throws Exception
	{
		parse( "A::A(): b( new int( 5 ) ), b( new B ), c( new int ) {}" );
	}

	public void testBug36704() throws Exception {
		Writer code = new StringWriter(); 
		code.write( "template <class T, class U>\n" ); 
		code.write( "struct Length< Typelist<T, U> >\n" );
		code.write( "{\n" );
		code.write( "enum { value = 1 + Length<U>::value };\n" );
		code.write( "};\n" );
		parse(code.toString());
	}

	public void testBug36699() throws Exception {
		Writer code = new StringWriter();
		code.write(
			"template <	template <class> class ThreadingModel = DEFAULT_THREADING,\n");
		code.write("std::size_t chunkSize = DEFAULT_CHUNK_SIZE,\n");
		code.write(
			"std::size_t maxSmallObjectSize = MAX_SMALL_OBJECT_SIZE	>\n");
		code.write("class SmallObject : public ThreadingModel<\n");
		code.write(
			"SmallObject<ThreadingModel, chunkSize, maxSmallObjectSize> >\n");
		code.write("{};\n");
		parse(code.toString());
	}

	public void testBug36691() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class T, class H>\n");
		code.write(
			"typename H::template Rebind<T>::Result& Field(H& obj)\n");
		code.write("{	return obj;	}\n");
		parse(code.toString());
	}
	
	public void testBug36702() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "void mad_decoder_init(struct mad_decoder *, void *,\n" ); 
		code.write( "			  enum mad_flow (*)(void *, struct mad_stream *),\n" ); 
		code.write( "			  enum mad_flow (*)(void *, struct mad_header const *),\n" ); 
		code.write( "			  enum mad_flow (*)(void *,\n" ); 
		code.write( "					struct mad_stream const *,\n" ); 
		code.write( "					struct mad_frame *),\n" ); 
		code.write( "			  enum mad_flow (*)(void *,\n" ); 
		code.write( "					struct mad_header const *,\n" ); 
		code.write( "					struct mad_pcm *),\n" ); 
		code.write( "			  enum mad_flow (*)(void *,\n" ); 
		code.write( "					struct mad_stream *,\n" ); 
		code.write( "					struct mad_frame *),\n" ); 
		code.write( "			  enum mad_flow (*)(void *, void *, unsigned int *)\n" ); 
		code.write( ");\n" );  
		
		parse( code.toString() );
		
	}
	
	public void testBug36852() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "int CBT::senseToAllRect( double id_standardQuot = DOSE, double id_minToleranz =15.0,\n" );  
		code.write( "double id_maxToleranz = 15.0, unsigned int iui_minY = 0, \n" );
		code.write( "unsigned int iui_maxY = HEIGHT );\n" );
		parse( code.toString() );
	}
	
	public void testBug36689() throws Exception {
		Writer code = new StringWriter();
		code.write("template\n");
		code.write("<\n");
		code.write("class AbstractFact,\n");
		code.write(
			"template <class, class> class Creator = OpNewFactoryUnit,\n");
		code.write("class TList = typename AbstractFact::ProductList\n");
		code.write(">\n");
		code.write("class ConcreteFactory\n");
		code.write(": public GenLinearHierarchy<\n");
		code.write(
			"typename TL::Reverse<TList>::Result, Creator, AbstractFact>\n");
		code.write("{\n");
		code.write("public:\n");
		code.write(
			"typedef typename AbstractFact::ProductList ProductList;\n");
		code.write("typedef TList ConcreteProductList;\n");
		code.write("};\n");
		parse(code.toString());
	}
	
	public void testBug36707() throws Exception {
		parse("enum { exists = sizeof(typename H::Small) == sizeof((H::Test(H::MakeT()))) };");
	}
	
	public void testBug36717() throws Exception  {
		parse("enum { eA = A::b };");
	}
	
	public void testBug36693() throws Exception {
		parse("FixedAllocator::Chunk* FixedAllocator::VicinityFind(void* p){}");
	}

	public void testWeirdExpression() throws Exception
	{
		parse( "int x = rhs.spImpl_.get();");
	}

	public void testBug36696() throws Exception {
		Writer code = new StringWriter();
		code.write(
			"template <typename P1> RefCounted(const RefCounted<P1>& rhs)\n");
		code.write(
			": pCount_(reinterpret_cast<const RefCounted&>(rhs).pCount_) {}\n");
		parse(code.toString());
	}

	public void testArrayOfPointerToFunctions() throws Exception
	{
		parse( "unsigned char (*main_data)[MAD_BUFFER_MDLEN];");
	}

	public void testBug36073() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "class A{\n" ); 
		writer.write( "int x;\n" ); 
		writer.write( "public:\n" ); 
		writer.write( "A(const A&);\n" ); 
		writer.write( "};\n" ); 
		writer.write( "A::A(const A&v) : x(v.x) { }\n" );
		parse( writer.toString() );
	}
	
	
	public void testTemplateSpecialization() throws Exception
	{
		Iterator declarations = parse( "template<> class stream<char> { /* ... */ };").getDeclarations();
		IASTClassSpecifier specifier = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)((IASTTemplateSpecialization)declarations.next()).getOwnedDeclaration()).getTypeSpecifier();
		assertFalse( declarations.hasNext());
		assertEquals( specifier.getName(), "stream<char>"); 
		assertFalse( specifier.getDeclarations().hasNext() );		
	}
	
	public void testTemplateInstantiation() throws Exception
	{
		Iterator declarations = parse( "template class Array<char>;").getDeclarations();
		IASTElaboratedTypeSpecifier specifier = (IASTElaboratedTypeSpecifier)((IASTAbstractTypeSpecifierDeclaration)((IASTTemplateInstantiation)declarations.next()).getOwnedDeclaration()).getTypeSpecifier();
		assertFalse( declarations.hasNext() );
		assertEquals( specifier.getName(), "Array<char>");
		assertEquals( specifier.getClassKind(), ASTClassKind.CLASS );
	}
	
	/**
	 * Test code:  "class A { int floor( double input ), someInt; };"
	 */
	public void testMultipleDeclarators() throws Exception
	{
		// Parse and get the translaton unit
		Iterator declarations = parse("class A { int floor( double input ), someInt; };").getDeclarations();
		Iterator members = ((IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier()).getDeclarations(); 
		assertFalse( declarations.hasNext() );
		IASTMethod decl1 = (IASTMethod)members.next();
		assertEquals( ((IASTSimpleTypeSpecifier)decl1.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
		Iterator parameters = decl1.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.DOUBLE );
		assertFalse( parameters.hasNext());
		assertEquals( parm.getName(), "input");
		
		IASTField decl2 = (IASTField)members.next();
		assertEquals( decl2.getName(), "someInt");
		assertEquals( ((IASTSimpleTypeSpecifier)decl2.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
		assertFalse( members.hasNext());
	}

	public void testFunctionModifiers() throws Exception
	{
		Iterator declarations = parse( "class A {virtual void foo( void ) const throw ( yay, nay, we::dont::care ) = 0;};").getDeclarations();
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertFalse( declarations.hasNext());
		Iterator members = classSpec.getDeclarations();
		IASTMethod method = (IASTMethod)members.next(); 
		assertFalse( members.hasNext() );
		assertTrue( method.isVirtual());
		assertEquals( method.getName(), "foo");
		assertEquals( ((IASTSimpleTypeSpecifier)method.getReturnType().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.VOID );
		Iterator parameters = method.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.VOID );
		assertFalse( parameters.hasNext());
		assertEquals( parm.getName(), "");
		assertTrue( method.isConst() ); 
		assertTrue( method.isPureVirtual() );
		assertNotNull( method.getExceptionSpec() );
		Iterator exceptions = method.getExceptionSpec().getTypeIds();
		assertEquals( (String)exceptions.next(), "yay");
		assertEquals( (String)exceptions.next(), "nay");
		assertEquals( (String)exceptions.next(), "we::dont::care");
		assertFalse( exceptions.hasNext() );
	}


	public void testArrays() throws Exception
	{
		Iterator declarations = parse("int x [5][];").getDeclarations();
		IASTVariable x = (IASTVariable)declarations.next(); 
		assertFalse( declarations.hasNext() );
		assertEquals( ((IASTSimpleTypeSpecifier)x.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( x.getName(), "x");
		Iterator arrayMods = x.getAbstractDeclaration().getArrayModifiers();
		IASTArrayModifier mod = (IASTArrayModifier)arrayMods.next();
		assertEquals( mod.getExpression().getExpressionKind(), IASTExpression.Kind.PRIMARY_INTEGER_LITERAL );
		assertEquals( mod.getExpression().getLiteralString(), "5" );
		mod = (IASTArrayModifier)arrayMods.next();
		assertNull( mod.getExpression());
		assertFalse( arrayMods.hasNext() ); 
	}		

	public void testElaboratedParms() throws Exception
	{
		Iterator declarations = parse( "int x( struct A myA ) { /* junk */ }" ).getDeclarations();
		IASTFunction f = (IASTFunction)declarations.next();
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.INT );
		Iterator parms = f.getParameters(); 
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parms.next();
		assertFalse( parms.hasNext());
		assertEquals( parm.getName(), "myA");
		assertEquals( ((IASTElaboratedTypeSpecifier)parm.getTypeSpecifier()).getName(), "A" );
		assertEquals( ((IASTElaboratedTypeSpecifier)parm.getTypeSpecifier()).getClassKind(), ASTClassKind.STRUCT );
		assertFalse( declarations.hasNext());
	}
	
	public void testMemberDeclarations() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class A {\n" ); 
		code.write( "public:\n");
		code.write( " int is0;\n" );
		code.write( "private:\n");
		code.write( " int is1;\n" );
		code.write( "protected:\n");
		code.write( " int is2;\n" );
		code.write( "};");
		Iterator declarations = parse( code.toString()).getDeclarations();
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertFalse(declarations.hasNext());
		Iterator members = classSpec.getDeclarations();
		for( int i = 0; i < 3; ++i )
		{
			IASTField field = (IASTField)members.next();
			assertEquals( field.getName(), "is"+ new Integer( i ).toString());
			ASTAccessVisibility visibility = null; 
			switch( i )
			{
				case 0:
					visibility = ASTAccessVisibility.PUBLIC;
					break;

				case 1:
					visibility = ASTAccessVisibility.PRIVATE;
					break;
 
				default: 
					visibility = ASTAccessVisibility.PROTECTED;
					break;
			}
			assertEquals( field.getVisiblity(), visibility );
		}
		assertFalse( members.hasNext());
	}

	public void testPointerOperators() throws Exception
	{
		Iterator declarations = parse("int * x = 0, & y, * const * volatile * z;").getDeclarations();
		for( int i = 0; i < 3; ++i )
		{
			IASTVariable v = (IASTVariable)declarations.next();
			assertSimpleType( v, IASTSimpleTypeSpecifier.Type.INT );
			Iterator pointerOperators = v.getAbstractDeclaration().getPointerOperators();
			ASTPointerOperator pointerOp = (ASTPointerOperator)pointerOperators.next(); 
			
			switch( i )
			{
				case 0:
					assertEquals( v.getName(), "x");
					assertEquals( pointerOp, ASTPointerOperator.POINTER );
					assertFalse( pointerOperators.hasNext());
					break;
				case 1:
					assertEquals( v.getName(), "y");
					assertEquals( pointerOp, ASTPointerOperator.REFERENCE);
					assertFalse( pointerOperators.hasNext()); 
					break;
				case 2:  
					assertEquals( v.getName(), "z");
					assertEquals( pointerOp, ASTPointerOperator.CONST_POINTER );
					assertEquals( pointerOperators.next(), ASTPointerOperator.VOLATILE_POINTER );
					assertEquals( pointerOperators.next(), ASTPointerOperator.POINTER );
					assertFalse( pointerOperators.hasNext());
					break;
			}
		}
		assertFalse( declarations.hasNext() );
	}
	
	public void testBug26467() throws Exception
	{
		StringWriter code = new StringWriter(); 
		code.write(	"struct foo { int fooInt; char fooChar;	};\n" );
		code.write( "typedef struct foo fooStruct;\n" );
		code.write( "typedef struct { int anonInt; char anonChar; } anonStruct;\n" );
		Iterator declarations = parse( code.toString()).getDeclarations();
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertEquals( classSpec.getClassKind(), ASTClassKind.STRUCT);
		assertEquals( classSpec.getName(), "foo" );
		Iterator members = classSpec.getDeclarations();
		IASTField field = (IASTField)members.next(); 
		assertSimpleType(field, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( field.getName(), "fooInt");
		field = (IASTField)members.next(); 
		assertSimpleType(field, IASTSimpleTypeSpecifier.Type.CHAR );
		assertEquals( field.getName(), "fooChar");
		assertFalse( members.hasNext());
		IASTTypedefDeclaration firstTypeDef = (IASTTypedefDeclaration)declarations.next();
		assertEquals( ((IASTElaboratedTypeSpecifier)firstTypeDef.getAbstractDeclarator().getTypeSpecifier()).getClassKind(), ASTClassKind.STRUCT );
		assertEquals( ((IASTElaboratedTypeSpecifier)firstTypeDef.getAbstractDeclarator().getTypeSpecifier()).getName(), "foo");
		assertEquals( firstTypeDef.getName(), "fooStruct");
		IASTTypedefDeclaration secondTypeDef = (IASTTypedefDeclaration)declarations.next();
		classSpec = (IASTClassSpecifier)secondTypeDef.getAbstractDeclarator().getTypeSpecifier();
		assertEquals( classSpec.getClassKind(), ASTClassKind.STRUCT);
		assertEquals( classSpec.getName(), "" );
		members = classSpec.getDeclarations();
		field = (IASTField)members.next(); 
		assertSimpleType(field, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( field.getName(), "anonInt");
		field = (IASTField)members.next(); 
		assertSimpleType(field, IASTSimpleTypeSpecifier.Type.CHAR );
		assertEquals( field.getName(), "anonChar");
		assertFalse( members.hasNext());
		assertEquals( secondTypeDef.getName(), "anonStruct");
		
	}
	
	public void testASMDefinition() throws Exception
	{
		Iterator declarations = parse( "asm( \"mov ep1 ds2\");" ).getDeclarations();
		IASTASMDefinition asm = (IASTASMDefinition)declarations.next(); 
		assertFalse( declarations.hasNext());
		assertEquals( asm.getBody(), "mov ep1 ds2");
	}
	
	public void testConstructorChain() throws Exception
	{
		Iterator declarations = parse( "TrafficLight_Actor::TrafficLight_Actor( RTController * rtg_rts, RTActorRef * rtg_ref )	: RTActor( rtg_rts, rtg_ref ), myId( 0 ) {}" ).getDeclarations();
		IASTDeclaration d = (IASTDeclaration)declarations.next(); // cannot properly do this test now with new callback structure in quickparse mode
	}
	
	public void testBug36237() throws Exception
	{
		parse( "A::A():B( (char *)0 ){}" );   
	}
	
	public void testBug36532() throws Exception
	{
		try
		{
			parse( "template<int f() {\n" );
			fail( "We should not make it this far");
		}
		catch( ParserException pe )
		{
		}
		catch( Exception e )
		{
			fail( "We should have gotten a ParserException rather than" + e);
		}
	}
	
	public void testPreprocessor() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "#include <stdio.h>\n#define DEF VALUE\n");
		IASTCompilationUnit tu = parse( code.toString()  );
		assertFalse( tu.getDeclarations().hasNext());
		Iterator inclusions = quickParseCallback.getInclusions();
		Iterator macros = quickParseCallback.getMacros();
		
		IASTInclusion i = (IASTInclusion)inclusions.next(); 
		assertFalse( inclusions.hasNext());
		
		assertEquals( i.getName(), "stdio.h");
		assertEquals( i.getElementStartingOffset(), 0 ); 
		assertEquals( i.getElementNameOffset(), 10 ); 
		assertEquals( i.getElementEndingOffset(), 18 );
		
		
		IASTMacro m = (IASTMacro)macros.next();
		assertEquals( m.getName(), "DEF" ); 
		assertEquals( m.getElementStartingOffset(), 19 );
		assertEquals( m.getElementNameOffset(), 27 );
		assertEquals( m.getElementEndingOffset(), 18 + 19);
	}
	
	public void testTemplateDeclarationOfFunction() throws Exception
	{
		Iterator declarations = parse( "template<class A, typename B=C> A aTemplatedFunction( B bInstance );").getDeclarations();
		IASTTemplateDeclaration templateDeclaration = (IASTTemplateDeclaration)declarations.next();
		assertFalse( declarations.hasNext());
		Iterator templateParms = templateDeclaration.getTemplateParameters();
		IASTTemplateParameter parm = (IASTTemplateParameter)templateParms.next(); 
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.CLASS );
		assertEquals( parm.getIdentifier(), "A");
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.TYPENAME );
		assertEquals( parm.getIdentifier(), "B");
		assertEquals( parm.getDefaultValueIdExpression(), "C" );
		IASTFunction f = (IASTFunction)templateDeclaration.getOwnedDeclaration();
		assertEquals( f.getName(), "aTemplatedFunction" );
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME );
		assertEquals( ((IASTSimpleTypeSpecifier)f.getReturnType().getTypeSpecifier()).getTypename(), "A" );
		Iterator parameters = f.getParameters();
		IASTParameterDeclaration parmDeclaration = (IASTParameterDeclaration)parameters.next();
		assertFalse( parameters.hasNext() );
		assertEquals( parmDeclaration.getName(), "bInstance");
		assertEquals( ((IASTSimpleTypeSpecifier)parmDeclaration.getTypeSpecifier()).getType(),IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME );
		assertEquals( ((IASTSimpleTypeSpecifier)parmDeclaration.getTypeSpecifier()).getTypename(), "B" );
	}
	
	public void testTemplateDeclarationOfClass() throws Exception {
		Iterator declarations = parse( "template<class T, typename Tibor = junk, class, typename, int x, float y,template <class Y> class, template<class A> class AClass> class myarray { /* ... */ };").getDeclarations();
		IASTTemplateDeclaration templateDeclaration = (IASTTemplateDeclaration)declarations.next();
		assertFalse( declarations.hasNext());
		Iterator templateParms = templateDeclaration.getTemplateParameters();
		IASTTemplateParameter parm = (IASTTemplateParameter)templateParms.next(); 
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.CLASS );
		assertEquals( parm.getIdentifier(), "T");
		parm = (IASTTemplateParameter)templateParms.next(); 
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.TYPENAME );
		assertEquals( parm.getIdentifier(), "Tibor");
		assertEquals( parm.getDefaultValueIdExpression(), "junk");
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.CLASS );
		assertEquals( parm.getIdentifier(), "");
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.TYPENAME );
		assertEquals( parm.getIdentifier(), "");
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.PARAMETER );
		assertEquals( parm.getParameterDeclaration().getName(), "x");
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getParameterDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.PARAMETER );
		assertEquals( parm.getParameterDeclaration().getName(), "y");
		assertEquals( ((IASTSimpleTypeSpecifier)parm.getParameterDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.FLOAT ); 
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.TEMPLATE_LIST);
		assertEquals( parm.getIdentifier(), "");
		Iterator subParms = parm.getTemplateParameters();
		parm = (IASTTemplateParameter)subParms.next(); 
		assertFalse( subParms.hasNext() ); 
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.CLASS ); 
		assertEquals( parm.getIdentifier(), "Y" );
		parm = (IASTTemplateParameter)templateParms.next();
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.TEMPLATE_LIST);
		assertEquals( parm.getIdentifier(), "AClass");
		subParms = parm.getTemplateParameters();
		parm = (IASTTemplateParameter)subParms.next(); 
		assertFalse( subParms.hasNext() ); 
		assertEquals( parm.getTemplateParameterKind(), IASTTemplateParameter.ParamKind.CLASS ); 
		assertEquals( parm.getIdentifier(), "A" );
		assertFalse( templateParms.hasNext() );
		IASTClassSpecifier classSpec = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)templateDeclaration.getOwnedDeclaration()).getTypeSpecifier();
		assertEquals( classSpec.getName(), "myarray");
		assertFalse( classSpec.getDeclarations().hasNext() );
	}
	
	public void testBug35906() throws Exception
	{
		StringWriter code = new StringWriter(); 
		code.write( "void TTest::MTest() {}\n" ); 
		code.write( "struct TTest::STest *TTest::FTest (int i) {}\n" ); 
		Iterator declarations = parse( code.toString() ).getDeclarations();
		IASTFunction f = (IASTFunction)declarations.next(); 
		assertEquals( f.getName(), "TTest::MTest");
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.VOID 	);
		f = (IASTFunction)declarations.next();
		assertFalse( declarations.hasNext()); 
		assertEquals( f.getName(), "TTest::FTest");
		assertEquals( ((IASTElaboratedTypeSpecifier)f.getReturnType().getTypeSpecifier()).getClassKind(), ASTClassKind.STRUCT );
		assertEquals( ((IASTElaboratedTypeSpecifier)f.getReturnType().getTypeSpecifier()).getName(), "TTest::STest");
		Iterator pointerOperators = f.getReturnType().getPointerOperators(); 
		assertEquals( pointerOperators.next(), ASTPointerOperator.POINTER );
		assertFalse( pointerOperators.hasNext() ); 
		Iterator parameters = f.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertFalse( parameters.hasNext() );
		assertEquals( parm.getName(), "i");
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.INT );
	}
	
	public void testBug36288() throws Exception
	{
		Iterator declarations = parse( "int foo() {}\nlong foo2(){}" ).getDeclarations();  
		IASTFunction f = (IASTFunction)declarations.next();
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( f.getName(), "foo");
		f = (IASTFunction)declarations.next();
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.INT );
		assertTrue( ((IASTSimpleTypeSpecifier)f.getReturnType().getTypeSpecifier()).isLong() ); 
		assertEquals( f.getName(), "foo2");
		assertFalse( declarations.hasNext() );
	}

	public void testBug36250() throws Exception
	{
		Iterator declarations = parse( "int f( int = 0 );").getDeclarations();
		IASTFunction f = (IASTFunction)declarations.next(); 
		assertFalse( declarations.hasNext() );
		assertSimpleReturnType( f, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( f.getName(), "f");
		Iterator parameters = f.getParameters(); 
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertFalse( parameters.hasNext() );
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( parm.getName(), "" );
		assertEquals( parm.getDefaultValue().getKind(), IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION );
		assertEquals( parm.getDefaultValue().getAssigmentExpression().getExpressionKind(), IASTExpression.Kind.PRIMARY_INTEGER_LITERAL );
		assertEquals( parm.getDefaultValue().getAssigmentExpression().getLiteralString(), "0" );
	}

	public void testBug36240() throws Exception
	{
		Iterator declarations = parse( "A & A::operator=( A ){}").getDeclarations();
		IASTFunction f = (IASTFunction)declarations.next(); 
		IASTSimpleTypeSpecifier typeSpec = (IASTSimpleTypeSpecifier)f.getReturnType().getTypeSpecifier();
		assertEquals( typeSpec.getType(), IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME );
		assertEquals( typeSpec.getTypename(), "A");
		Iterator pointerOps = f.getReturnType().getPointerOperators();
		assertEquals( (ASTPointerOperator)pointerOps.next(), ASTPointerOperator.REFERENCE ); 
		assertFalse( pointerOps.hasNext() );
		assertEquals( f.getName(), "A::operator=");
		Iterator parms = f.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parms.next();
		assertEquals( parm.getName(), "" );
		typeSpec = (IASTSimpleTypeSpecifier)parm.getTypeSpecifier();
		assertEquals( typeSpec.getType(), IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME );
		assertEquals( typeSpec.getTypename(), "A" );
	}
	
	public void testBug36254() throws Exception
	{
		Iterator declarations = parse( "unsigned i;\nvoid f( unsigned p1 = 0 );").getDeclarations();
		IASTVariable v = (IASTVariable)declarations.next(); 
		assertSimpleType( v, IASTSimpleTypeSpecifier.Type.INT);
		assertTrue( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).isUnsigned() ); 
		IASTFunction f = (IASTFunction)declarations.next(); 
		assertSimpleReturnType(f, IASTSimpleTypeSpecifier.Type.VOID );
		assertEquals( f.getName(), "f");
		Iterator parms = f.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parms.next();
		assertEquals( parm.getName(), "p1");
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.INT );
		assertTrue( ((IASTSimpleTypeSpecifier)parm.getTypeSpecifier()).isUnsigned() ); 
		assertEquals( parm.getDefaultValue().getKind(), IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION );
		assertEquals( parm.getDefaultValue().getAssigmentExpression().getExpressionKind(), IASTExpression.Kind.PRIMARY_INTEGER_LITERAL );
		assertEquals( parm.getDefaultValue().getAssigmentExpression().getLiteralString(), "0" );
		assertFalse( declarations.hasNext());
	}
	
	public void testBug36432() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "#define CMD_GET		\"g\"\n" ); 	 
		code.write( "#define CMD_ACTION   	\"a\"\n" ); 	 
		code.write( "#define CMD_QUIT		\"q\"\n" );
		code.write( "static const memevent_cmd_func memevent_cmd_funcs[sizeof memevent_cmds - 1] = {\n");
		code.write( "memevent_get,\n");
		code.write( "memevent_action,\n");
		code.write( "memevent_quit,\n");
		code.write( "};\n");
		parse( code.toString() );
	}
	
	public void testBug36594() throws Exception
	{
		parse( "const int n = sizeof(A) / sizeof(B);");
	}
	
	public void testBug36794() throws Exception
	{
		parse( "template<> class allocator<void> {};");
		Iterator i = quickParseCallback.iterateOffsetableElements();
		while( i.hasNext() )
			assertNotNull( i.next() );
	}
	
	public void testBug36799() throws Exception
	{
		parse( "static const int __WORD_BIT = int(CHAR_BIT*sizeof(unsigned int));");
	}


	public void testBug36764() throws Exception
	{
		parse( "struct{ int x : 4; int y : 8; };" );
	}
	
	public void testOrder() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "#define __SGI_STL_INTERNAL_ALGOBASE_H\n" ); 
		code.write( "#include <string.h>\n" ); 
		code.write( "template <class _Tp>\n" ); 
		code.write( "inline void swap(_Tp& __a, _Tp& __b) {\n" ); 
		code.write( "__STL_REQUIRES(_Tp, _Assignable);\n" ); 
		code.write( "_Tp __tmp = __a;\n" ); 
		code.write( "__a = __b;\n" ); 
		code.write( "__b = __tmp;\n" ); 
		code.write( "}\n" ); 
		
		parse( code.toString() );
		Iterator i = quickParseCallback.iterateOffsetableElements();
		assertTrue( i.hasNext() );
		assertTrue( i.next() instanceof IASTMacro );  
		assertTrue( i.hasNext() );
		assertTrue( i.next() instanceof IASTInclusion );
		assertTrue( i.hasNext() );
		assertTrue( i.next() instanceof IASTDeclaration );
		assertFalse( i.hasNext() );
	}
	
	public void testBug36771() throws Exception {
		Writer code = new StringWriter();
		code.write("#include /**/ \"foo.h\"\n");
	
		parse( code.toString()  );
	
		Iterator includes = quickParseCallback.getInclusions();
	
		IASTInclusion include = (IASTInclusion)includes.next();
		assertTrue( include.getName().equals("foo.h") );
		assertFalse( includes.hasNext() );
	}
	
	
	public void testBug36811() throws Exception
	{
		Writer code = new StringWriter();  
		code.write( "using namespace std;\n" ); 
		code.write( "class Test {};" );
		parse( code.toString() );
		Iterator i = quickParseCallback.iterateOffsetableElements();
		while( i.hasNext() )
			assertNotNull( i.next() );
	}

	public void testBug36708() throws Exception {
		Iterator declarations = parse("enum { isPointer = PointerTraits<T>::result };").getDeclarations();
		IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		assertFalse( declarations.hasNext() );
		Iterator enumerators = enumSpec.getEnumerators();	
		IASTEnumerator enumerator = (IASTEnumerator)enumerators.next(); 
		assertFalse( enumerators.hasNext() );
		assertEquals( enumerator.getName(), "isPointer");
		assertEquals( enumerator.getInitialValue().getExpressionKind(), IASTExpression.Kind.ID_EXPRESSION ); 
		assertEquals( enumerator.getInitialValue().getLiteralString(), "PointerTraits<T>::result");
	}

	public void testBug36690() throws Exception {
		parse("Functor(const Functor& rhs) : spImpl_(Impl::Clone(rhs.spImpl_.get())){}").getDeclarations();
	}

	public void testBug36703() throws Exception {
		parse("const std::type_info& Get() const;");
	}
	
	public void testBug36692() throws Exception  {
		Writer code = new StringWriter();
		code.write("template <typename T, typename Destroyer>\n");
		code.write("void SetLongevity(T* pDynObject, unsigned int longevity,\n");
		code.write("Destroyer d = Private::Deleter<T>::Delete){}\n");
		parse(code.toString());
	}
	
	public void testBug36551() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class TextFrame {\n" ); 
		code.write( "BAD_MACRO()\n"); 
		code.write( "};");
		parse( code.toString(), true, false );
	}
	
	public void testBug36247() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "class A {\n" ); 
		code.write( "INLINE_DEF int f ();\n" ); 
		code.write( "INLINE_DEF A   g ();" ); 
		code.write( "INLINE_DEF A * h ();" ); 
		code.write( "INLINE_DEF A & unlock( void );");
		code.write( "};" );
		parse(code.toString());
	}
	
	public void testStruct() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "struct mad_bitptr { unsigned char const *byte;\n" );
		writer.write( "unsigned short cache;\n unsigned short left;};" );
		parse( writer.toString() );
	}
	
	public void testBug36559() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "namespace myNameSpace {\n" ); 
		code.write( "template<typename T=short> class B {};\n" );
		code.write( "template<> class B<int> {};\n" ); 
		code.write( "}\n" ); 
		parse( code.toString() ); 
	}
	
	public void testPointersToFunctions() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "void (*name)( void );\n");
		code.write( "static void * (* const orig_malloc_hook)(const char *file, int line, size_t size);\n");

		Iterator declarations = parse( code.toString() ).getDeclarations();
		IASTPointerToFunction p2f = (IASTPointerToFunction)declarations.next();
		assertSimpleReturnType( p2f, IASTSimpleTypeSpecifier.Type.VOID );
		assertEquals( p2f.getName(), "name");
		assertEquals( p2f.getPointerOperator(), ASTPointerOperator.POINTER);
		Iterator parameters = p2f.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertFalse( parameters.hasNext() );
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.VOID );
		assertEquals( parm.getName(), "" );
		
		p2f = (IASTPointerToFunction)declarations.next(); 
		assertSimpleReturnType( p2f, IASTSimpleTypeSpecifier.Type.VOID );
		assertTrue( p2f.isStatic() );
		Iterator rtPo = p2f.getReturnType().getPointerOperators();
		assertEquals( rtPo.next(), ASTPointerOperator.POINTER );
		assertFalse( rtPo.hasNext() );
		assertEquals( p2f.getPointerOperator(), ASTPointerOperator.CONST_POINTER);
		parameters = p2f.getParameters();
		parm = (IASTParameterDeclaration)parameters.next(); 
	    assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.CHAR );
	    assertEquals( parm.getName(), "file" );
	    assertTrue( parm.isConst() );
	    assertTrue( parm.getPointerOperators().hasNext() );
		parm = (IASTParameterDeclaration)parameters.next(); 
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.INT );
		assertEquals( parm.getName(), "line" );
		parm = (IASTParameterDeclaration)parameters.next(); 
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME );
		assertEquals( parm.getName(), "size" );
		assertFalse( parameters.hasNext() );		
	}
	
	public void testBug36600() throws Exception
	{
		IASTPointerToFunction p2f = (IASTPointerToFunction)parse( "enum mad_flow (*input_func)(void *, struct mad_stream *);").getDeclarations().next();
		IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier)p2f.getReturnType().getTypeSpecifier();
		assertEquals( elab.getName(), "mad_flow");
		assertEquals( elab.getClassKind(), ASTClassKind.ENUM );
		assertEquals( p2f.getPointerOperator(), ASTPointerOperator.POINTER );
		assertEquals( p2f.getName(), "input_func");
		Iterator parms = p2f.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parms.next();
		assertEquals( parm.getName(), "" );
		assertEquals( parm.getPointerOperators().next(), ASTPointerOperator.POINTER);
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.VOID);
		parm = (IASTParameterDeclaration)parms.next();
		assertEquals( parm.getName(), "" );
		assertEquals( parm.getPointerOperators().next(), ASTPointerOperator.POINTER);
		elab = (IASTElaboratedTypeSpecifier)parm.getTypeSpecifier();
		assertEquals( elab.getName(), "mad_stream");
		assertEquals( elab.getClassKind(), ASTClassKind.STRUCT );
		
		
	}

	public void testBug36713() throws Exception {
		Writer code = new StringWriter();
		code.write("A ( * const fPtr) (void *); \n");
		code.write("A (* const fPtr2) ( A * ); \n");
		Iterator declarations = parse(code.toString()).getDeclarations();
	}

	// K&R Test hasn't been ported from DOMTests
	// still need to figure out how to represent these in the AST
//	public void testOldKRFunctionDeclarations() throws Exception
//	{
//		// Parse and get the translaton unit
//		Writer code = new StringWriter();
//		code.write("bool myFunction( parm1, parm2, parm3 )\n");
//		code.write("const char* parm1;\n");
//		code.write("int (*parm2)(float);\n");
//		code.write("{}");
//		TranslationUnit translationUnit = parse(code.toString());
//
//		// Get the declaration
//		List declarations = translationUnit.getDeclarations();
//		assertEquals(1, declarations.size());
//		SimpleDeclaration simpleDeclaration = (SimpleDeclaration)declarations.get(0);
//		assertEquals( simpleDeclaration.getDeclSpecifier().getType(), DeclSpecifier.t_bool );
//		List declarators  = simpleDeclaration.getDeclarators(); 
//		assertEquals( 1, declarators.size() ); 
//		Declarator functionDeclarator = (Declarator)declarators.get( 0 ); 
//		assertEquals( functionDeclarator.getName().toString(), "myFunction" );
//        
//		ParameterDeclarationClause pdc = functionDeclarator.getParms(); 
//		assertNotNull( pdc ); 
//		List parameterDecls = pdc.getDeclarations(); 
//		assertEquals( 3, parameterDecls.size() );
//		ParameterDeclaration parm1 = (ParameterDeclaration)parameterDecls.get( 0 );
//		assertNotNull( parm1.getDeclSpecifier().getName() );
//		assertEquals( "parm1", parm1.getDeclSpecifier().getName().toString() );
//		List parm1Decls = parm1.getDeclarators(); 
//		assertEquals( 1, parm1Decls.size() ); 
//
//		ParameterDeclaration parm2 = (ParameterDeclaration)parameterDecls.get( 1 );
//		assertNotNull( parm2.getDeclSpecifier().getName() );
//		assertEquals( "parm2", parm2.getDeclSpecifier().getName().toString() );
//		List parm2Decls = parm2.getDeclarators(); 
//		assertEquals( 1, parm2Decls.size() );
//        
//		ParameterDeclaration parm3 = (ParameterDeclaration)parameterDecls.get( 2 );
//		assertNotNull( parm3.getDeclSpecifier().getName() );
//		assertEquals( "parm3", parm3.getDeclSpecifier().getName().toString() );
//		List parm3Decls = parm3.getDeclarators(); 
//		assertEquals( 1, parm3Decls.size() );
//        
//		OldKRParameterDeclarationClause clause = pdc.getOldKRParms(); 
//		assertNotNull( clause );
//		assertEquals( clause.getDeclarations().size(), 2 );
//		SimpleDeclaration decl1 = (SimpleDeclaration)clause.getDeclarations().get(0);
//		assertEquals( decl1.getDeclarators().size(), 1 );
//		assertTrue(decl1.getDeclSpecifier().isConst());
//		assertFalse(decl1.getDeclSpecifier().isVolatile());
//		assertEquals( decl1.getDeclSpecifier().getType(), DeclSpecifier.t_char);
//		Declarator declarator1 = (Declarator)decl1.getDeclarators().get( 0 );
//		assertEquals( declarator1.getName().toString(), "parm1" );
//		List ptrOps1 = declarator1.getPointerOperators();
//		assertNotNull( ptrOps1 );
//		assertEquals( 1, ptrOps1.size() );
//		PointerOperator po1 = (PointerOperator)ptrOps1.get(0);
//		assertNotNull( po1 ); 
//		assertFalse( po1.isConst() );
//		assertFalse( po1.isVolatile() );
//		assertEquals( po1.getType(), PointerOperator.t_pointer );
//        
//		SimpleDeclaration declaration = (SimpleDeclaration)clause.getDeclarations().get(1);
//		assertEquals( declaration.getDeclSpecifier().getType(), DeclSpecifier.t_int );
//		assertEquals( declaration.getDeclarators().size(), 1);
//		assertNull( ((Declarator)declaration.getDeclarators().get(0)).getName() );
//		assertNotNull( ((Declarator)declaration.getDeclarators().get(0)).getDeclarator() );
//		assertEquals( ((Declarator)declaration.getDeclarators().get(0)).getDeclarator().getName().toString(), "parm2" );
//		ParameterDeclarationClause clause2 = ((Declarator)declaration.getDeclarators().get(0)).getParms();
//		assertEquals( clause2.getDeclarations().size(), 1 );
//		assertEquals( ((ParameterDeclaration)clause2.getDeclarations().get(0)).getDeclarators().size(), 1 );  
//		assertNull( ((Declarator)((ParameterDeclaration)clause2.getDeclarations().get(0)).getDeclarators().get(0)).getName() );
//		assertEquals( ((ParameterDeclaration)clause2.getDeclarations().get(0)).getDeclSpecifier().getType(), DeclSpecifier.t_float );          
//	}
    
	public void testPointersToMemberFunctions() throws Exception
	{
		IASTPointerToMethod p2m  = (IASTPointerToMethod)parse("void (A::*name)(void);").getDeclarations().next();
		assertSimpleReturnType( p2m, IASTSimpleTypeSpecifier.Type.VOID );
		assertEquals( p2m.getName(), "A::name");
		assertEquals( p2m.getPointerOperator(), ASTPointerOperator.POINTER);
		Iterator parameters = p2m.getParameters();
		IASTParameterDeclaration parm = (IASTParameterDeclaration)parameters.next(); 
		assertFalse( parameters.hasNext() );
		assertParameterSimpleType( parm, IASTSimpleTypeSpecifier.Type.VOID );
		assertEquals( parm.getName(), "" );
	}
     
	
	
}