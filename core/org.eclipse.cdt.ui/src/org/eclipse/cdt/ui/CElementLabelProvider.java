package org.eclipse.cdt.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.internal.ui.CElementImageProvider;
import org.eclipse.cdt.internal.ui.ErrorTickAdornmentProvider;
import org.eclipse.cdt.internal.ui.IAdornmentProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The label provider for the c model elements.
 */
public class CElementLabelProvider extends LabelProvider {

	/**
	 * Flag (bit mask) indicating that methods labels include the method return type. (appended)
	 */
	public final static int SHOW_RETURN_TYPE = 0x001;

	/**
	 * Flag (bit mask) indicating that method label include method parameter types.
	 */
	public final static int SHOW_PARAMETERS = 0x002;

	/**
	 * Flag (bit mask) indicating that method label include thrown exception.
	 */
	public final static int SHOW_EXCEPTION = 0x004;

	/**
	 * Flag (bit mask) indicating that the label should show the icons with no space
	 * reserved for overlays.
	 */
	public final static int SHOW_SMALL_ICONS = 0x100;

	/**
	 * Flag (bit mask) indicating that the label should include overlay icons
	 * for element type and modifiers.
	 */
	public final static int SHOW_OVERLAY_ICONS = 0x010;

	/**
	 * Flag (bit mask) indicating that Complation Units, Class Files, Types, Declarations and Members
	 * should be rendered qualified.
	 * Examples: java.lang.String, java.util.Vector.size()
	 * 
	 * @since 2.0
	 */
	public final static int SHOW_QUALIFIED=				0x400;

	/**
	 * Flag (bit mask) indicating that Complation Units, Class Files, Types, Declarations and Members
	 * should be rendered qualified. The qualifcation is appended
	 * Examples: String - java.lang, size() - java.util.Vector
	 * 
	 * @since 2.0
	 */
	public final static int SHOW_POST_QUALIFIED=	0x800;	
	
	
	/**
	 * Constant (value <code>0</code>) indicating that the label should show 
	 * the basic images only.
	 */
	public final static int SHOW_BASICS= 0x000;
	
	
	public final static int SHOW_DEFAULT= new Integer(SHOW_PARAMETERS | SHOW_OVERLAY_ICONS).intValue();
	
	private WorkbenchLabelProvider fWorkbenchLabelProvider;
	protected CElementImageProvider fImageLabelProvider;
	private IAdornmentProvider[] fAdornmentProviders;

	private int fFlags;
	private int fImageFlags;
	private int fTextFlags;
	
	public CElementLabelProvider() {
		this(SHOW_DEFAULT);
	}

	public CElementLabelProvider(int flags) {
		this(flags, null);
	}

	/**
	 * @param textFlags Flags defined in <code>CElementLabels</code>.
	 * @param imageFlags Flags defined in <code>CElementImageProvider</code>.
	 */
	public CElementLabelProvider(int flags, IAdornmentProvider[] adormentProviders) {
		fWorkbenchLabelProvider= new WorkbenchLabelProvider();
		
		fImageLabelProvider= new CElementImageProvider();
		fAdornmentProviders= adormentProviders; 

		fFlags = flags;
	}

	/**
	 * @see ILabelProvider#getText
	 */
	public String getText(Object element) {
		if (element instanceof ICElement) {
			try {
				ICElement celem= (ICElement)element;
				
				StringBuffer name = new StringBuffer();
				switch(celem.getElementType()){
					case ICElement.C_FIELD:
					case ICElement.C_VARIABLE:
					case ICElement.C_VARIABLE_DECLARATION:
						IVariableDeclaration vDecl = (IVariableDeclaration) celem;
					name.append(vDecl.getElementName());
					if((vDecl.getTypeName() != null) &&(vDecl.getTypeName().length() > 0)){
						name.append(" : "); //$NON-NLS-1$
						name.append(vDecl.getTypeName());
					}
					break;
					case ICElement.C_FUNCTION:
					case ICElement.C_FUNCTION_DECLARATION:
					case ICElement.C_METHOD:
					case ICElement.C_METHOD_DECLARATION:
						IFunctionDeclaration fDecl = (IFunctionDeclaration) celem;
					name.append(fDecl.getSignature());
					if((fDecl.getReturnType() != null) &&(fDecl.getReturnType().length() > 0)){
						name.append(" : "); //$NON-NLS-1$
						name.append(fDecl.getReturnType());
					}
					break;
					case ICElement.C_STRUCT:
					case ICElement.C_UNION:
					case ICElement.C_ENUMERATION:
						if((celem.getElementName() != null) && (celem.getElementName().length() > 0)){
							name.append(celem.getElementName());
						} else if (celem instanceof IVariableDeclaration) {
							IVariableDeclaration varDecl = (IVariableDeclaration) celem;
							name.append(varDecl.getTypeName());				
						}
					break;
					case ICElement.C_TYPEDEF:
						ITypeDef tDecl = (ITypeDef) celem;
					name.append(tDecl.getElementName());
					if((tDecl.getTypeName() != null) &&(tDecl.getTypeName().length() > 0)){
						name.append(" : "); //$NON-NLS-1$
						name.append(tDecl.getTypeName());				
					}
					break;
					case ICElement.C_NAMESPACE:
						if((celem.getElementName() != null) && (celem.getElementName().length() > 0)){
							name.append(celem.getElementName());
						} else if (celem instanceof INamespace) {
							INamespace nDecl = (INamespace) celem;
							name.append(nDecl.getTypeName());				
						}
					break;
					case ICElement.C_TEMPLATE_CLASS:
					case ICElement.C_TEMPLATE_FUNCTION:
					case ICElement.C_TEMPLATE_METHOD:
					case ICElement.C_TEMPLATE_STRUCT:
					case ICElement.C_TEMPLATE_UNION:
					case ICElement.C_TEMPLATE_VARIABLE:
						ITemplate template = (ITemplate) celem;
					String signature = template.getTemplateSignature();
					name.append(signature);
					break;
					default:
						name.append(celem.getElementName());
					break;				
				}
				
				if (celem instanceof IBinary) {
					IBinary bin = (IBinary)celem;
					name.append(" - [" + bin.getCPU() + (bin.isLittleEndian() ? "le" : "be") + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				
				return name.toString();
			} catch (CModelException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
		return fWorkbenchLabelProvider.getText(element);
	}

	/**
	 * @see ILabelProvider#getImage
	 */
	public Image getImage(Object element) {
		return fImageLabelProvider.getImageLabel(element, evaluateImageFlags(element));
	}
	
	protected int evaluateImageFlags(Object element) {
		int imageFlags= getImageFlags();
		if (fAdornmentProviders != null) {
			for (int i= 0; i < fAdornmentProviders.length; i++) {
				imageFlags |= fAdornmentProviders[i].computeAdornmentFlags(element);
			}
		}
		return imageFlags;
	}
	
	protected Image decorateImage(Image image, Object element) {
//		if (fLabelDecorators != null && image != null) {
//			for (int i= 0; i < fLabelDecorators.size(); i++) {
//				ILabelDecorator decorator= (ILabelDecorator) fLabelDecorators.get(i);
//				image= decorator.decorateImage(image, element);
//			}
//		}
		return image;
	}

	/**
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		if (fAdornmentProviders != null) {
			for (int i= 0; i < fAdornmentProviders.length; i++) {
				fAdornmentProviders[i].dispose();
			}
		}
		if (fWorkbenchLabelProvider != null) {
			fWorkbenchLabelProvider.dispose();
			fWorkbenchLabelProvider= null;
		}
		if(fImageLabelProvider != null) {
			fImageLabelProvider.dispose();
		}
	}

	private boolean getFlag(int flag) {
		return (fFlags & flag) != 0;
	}

	/**
	 * Turns on the rendering options specified in the given flags.
	 *
	 * @param flags the options; a bitwise OR of <code>SHOW_* </code> constants
	 */
	public void turnOn(int flags) {
		fFlags |= flags;
	}

	/**
	 * Turns off the rendering options specified in the given flags.
	 *
	 * @param flags the initial options; a bitwise OR of <code>SHOW_* </code> constants
	 */
	public void turnOff(int flags) {
		fFlags &= (~flags);
	}
	
	/**
	 * Gets the image flags.
	 * Can be overwriten by super classes.
	 * @return Returns a int
	 */
	public int getImageFlags() {
		fImageFlags = 0;
		if (getFlag(SHOW_OVERLAY_ICONS)) {
			fImageFlags |= CElementImageProvider.OVERLAY_ICONS;
		}
		if (getFlag(SHOW_SMALL_ICONS)) {
			fImageFlags |= CElementImageProvider.SMALL_ICONS;
		}
		return fImageFlags;
	}

	/**
	 * Gets the text flags. Can be overwriten by super classes.
	 * @return Returns a int
	 */
	public int getTextFlags() {
		fTextFlags = 0;
		if (getFlag(SHOW_RETURN_TYPE)) {
			fTextFlags |= CElementLabels.M_APP_RETURNTYPE;
		}
		if (getFlag(SHOW_PARAMETERS)) {
			fTextFlags |= CElementLabels.M_PARAMETER_TYPES;
		}
		if (getFlag(SHOW_EXCEPTION)) {
			fTextFlags |= CElementLabels.M_EXCEPTIONS;
		}
		if (getFlag(SHOW_POST_QUALIFIED)) {
			fTextFlags |= CElementLabels.M_POST_QUALIFIED;
		}
		return fTextFlags;
	}
	
	public static IAdornmentProvider[] getAdornmentProviders(boolean errortick, IAdornmentProvider extra) {
		if (errortick) {
			if (extra == null) {
				return new IAdornmentProvider[] { new ErrorTickAdornmentProvider() };
			}
			return new IAdornmentProvider[] { new ErrorTickAdornmentProvider(), extra };
		}
		if (extra != null) {
			return new IAdornmentProvider[] { extra };
		}
		return null;
	}
}
