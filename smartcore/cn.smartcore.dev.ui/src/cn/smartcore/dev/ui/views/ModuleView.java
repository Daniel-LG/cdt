package cn.smartcore.dev.ui.views;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class ModuleView extends ViewPart {

	private TreeViewer fViewer;

	// private DrillDownAdapter drillDownAdapter;

	// private IBindingService bindingService;

	@Override
	public void createPartControl(Composite parent) {
		fViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		fViewer.setUseHashlookup(true);
		fViewer.setContentProvider(new ModuleContentProvider());
		fViewer.setLabelProvider(new ModuleLabelProvider());
		// initDragAndDrop();

		// drillDownAdapter = new DrillDownAdapter(fViewer);

//		fViewer.addDoubleClickListener(new IDoubleClickListener() {
//			@Override
//			public void doubleClick(DoubleClickEvent event) {
//				ISelection selection = event.getSelection();
//				if (selection instanceof IStructuredSelection) {
//					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
//					Object element = structuredSelection.getFirstElement();
//					if (element instanceof IFile) {
//						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//						IFile file = (IFile) element;
//						IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
//								.getDefaultEditor(file.getName());
//						if (desc == null) {
//							desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("a.txt");
//						}
//						try {
//							page.openEditor(new FileEditorInput(file), desc.getId());
//						} catch (PartInitException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//		});

		// fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
		//
		// @Override
		// public void selectionChanged(SelectionChangedEvent event) {
		// handleSelectionChanged(event);
		// }
		// });

		// fViewer.setSorter(new ViewerSorter() {
		// @Override
		// public int category(Object element) {
		// if (element instanceof TargetSourceContainer) {
		// return 1;
		// } else if (element instanceof IResource) {
		// return 2;
		// }
		// return 3;
		// }
		// });
		fViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		getSite().setSelectionProvider(fViewer);

		// makeActions();
		// hookContextMenu();
		// contributeToActionBars();

		// updateActions((IStructuredSelection)fViewer.getSelection());

		// bindingService =
		// PlatformUI.getWorkbench().getService(IBindingService.class);
		// if (bindingService != null) {
		// bindingService.addBindingManagerListener(bindingManagerListener);
		// }
	}

	// protected void handleDoubleClick(DoubleClickEvent event) {
	// buildTargetAction.run();
	// }

	// void handleSelectionChanged(SelectionChangedEvent event) {
	// IStructuredSelection sel = (IStructuredSelection)event.getSelection();
	// updateActions(sel);
	// }
	//
	// void updateActions(IStructuredSelection sel) {
	// newTargetAction.selectionChanged(sel);
	// buildTargetAction.selectionChanged(sel);
	// buildLastTargetAction.selectionChanged(sel);
	// deleteTargetAction.selectionChanged(sel);
	// editTargetAction.selectionChanged(sel);
	// copyTargetAction.selectionChanged(sel);
	// pasteTargetAction.selectionChanged(sel);
	// }

	// private void initDragAndDrop() {
	// int opers= DND.DROP_COPY | DND.DROP_MOVE;

	// LocalSelectionTransfer is used inside Make Target View
	// TextTransfer is used to drag outside the View or eclipse
	// Transfer[] dragTransfers= {
	// LocalSelectionTransfer.getTransfer(),
	// MakeTargetTransfer.getInstance(),
	// TextTransfer.getInstance(),
	// };
	//
	// AbstractSelectionDragAdapter[] dragListeners = {
	// new LocalTransferDragSourceListener(fViewer),
	// new MakeTargetTransferDragSourceListener(fViewer),
	// new TextTransferDragSourceListener(fViewer),
	// };
	//
	// DelegatingDragAdapter delegatingDragAdapter = new
	// DelegatingDragAdapter();
	// for (AbstractSelectionDragAdapter dragListener : dragListeners) {
	// delegatingDragAdapter.addDragSourceListener(dragListener);
	// }
	// fViewer.addDragSupport(opers, dragTransfers, delegatingDragAdapter);

	// Transfer[] dropTransfers= {
	// LocalSelectionTransfer.getTransfer(),
	// MakeTargetTransfer.getInstance(),
	// FileTransfer.getInstance(),
	// TextTransfer.getInstance(),
	// };
	// AbstractContainerAreaDropAdapter[] dropListeners = {
	// new LocalTransferDropTargetListener(fViewer),
	// new MakeTargetTransferDropTargetListener(fViewer),
	// new FileTransferDropTargetListener(fViewer),
	// new TextTransferDropTargetListener(fViewer),
	// };
	// DelegatingDropAdapter delegatingDropAdapter = new
	// DelegatingDropAdapter();
	// for (AbstractContainerAreaDropAdapter dropListener : dropListeners) {
	// delegatingDropAdapter.addDropTargetListener(dropListener);
	// }
	// fViewer.addDropSupport(opers | DND.DROP_DEFAULT, dropTransfers,
	// delegatingDropAdapter);
	// }

	@Override
	public void setFocus() {
		fViewer.getTree().setFocus();
	}

}
