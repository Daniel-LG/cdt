package org.eclipse.cdt.internal.core.dom;



/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable 
"typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ElaboratedTypeSpecifier extends TypeSpecifier {

	ClassKey classKey = new ClassKey(); 
	public int getClassKey() { return classKey.getClassKey(); }
	
	public void setClassKey( int classKey ) 
	{ 
		this.classKey.setClassKey( classKey ); 
	}
	
	public ElaboratedTypeSpecifier(int classKey, TypeSpecifier.IOwner owner) {
		super(owner);
		this.classKey.setClassKey( classKey );
	}

	private String name;
	public void setName(String n) { name = n; }
	public String getName() { return name; }
	
	private ClassSpecifier classSpec = null;

	/**
	 * @return
	 */
	public ClassSpecifier getClassSpec() {
		return classSpec;
	}

	/**
	 * @param specifier
	 */
	public void setClassSpec(ClassSpecifier specifier) {
		classSpec = specifier;
	}

}
