package org.caleydo.view.tabular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.caleydo.core.data.collection.IStorage;
import org.caleydo.core.data.collection.storage.EDataRepresentation;
import org.caleydo.core.data.mapping.EIDCategory;
import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.selection.ESelectionCommandType;
import org.caleydo.core.data.selection.ESelectionType;
import org.caleydo.core.data.selection.EVAType;
import org.caleydo.core.data.selection.IVirtualArray;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.delta.DeltaConverter;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.data.selection.delta.IVirtualArrayDelta;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.data.selection.delta.VADeltaItem;
import org.caleydo.core.manager.IIDMappingManager;
import org.caleydo.core.manager.event.data.ReplaceVirtualArrayEvent;
import org.caleydo.core.manager.event.view.ClearSelectionsEvent;
import org.caleydo.core.manager.event.view.SelectionCommandEvent;
import org.caleydo.core.manager.event.view.storagebased.RedrawViewEvent;
import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
import org.caleydo.core.manager.event.view.storagebased.VirtualArrayUpdateEvent;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.manager.usecase.EDataDomain;
import org.caleydo.core.manager.usecase.EDataFilterLevel;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.IView;
import org.caleydo.core.view.opengl.canvas.listener.ClearSelectionsListener;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionCommandHandler;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
import org.caleydo.core.view.opengl.canvas.listener.IVirtualArrayUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.RedrawViewListener;
import org.caleydo.core.view.opengl.canvas.listener.ReplaceVirtualArrayListener;
import org.caleydo.core.view.opengl.canvas.listener.SelectionCommandListener;
import org.caleydo.core.view.opengl.canvas.listener.SelectionUpdateListener;
import org.caleydo.core.view.opengl.canvas.listener.VirtualArrayUpdateListener;
import org.caleydo.core.view.swt.ASWTView;
import org.caleydo.core.view.swt.ISWTView;
import org.caleydo.rcp.dialog.LabelEditorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * View shows data from a set in a tabular format.
 * 
 * @author Marc Streit
 */
public class TabularDataView extends ASWTView implements
		ISelectionUpdateHandler, IVirtualArrayUpdateHandler,
		ISelectionCommandHandler, IViewCommandHandler, IView, ISWTView {

	public final static String VIEW_ID = "org.caleydo.view.tabular";
	private final static int COLUMN_OFFSET = 3;

	/**
	 * This manager is responsible for the content in the storages (the indices)
	 */
	protected SelectionManager contentSelectionManager;

	/**
	 * This manager is responsible for the management of the storages in the set
	 */
	protected SelectionManager storageSelectionManager;

	/**
	 * The virtual array that manages the contents (the indices) in the storages
	 */
	protected IVirtualArray contentVA;

	/**
	 * The virtual array that manages the storage references in the set
	 */
	protected IVirtualArray storageVA;
	/**
	 * The type of the content VA
	 */
	protected EVAType contentVAType = EVAType.CONTENT;

	/**
	 * The type of the storage VA
	 */
	protected EVAType storageVAType = EVAType.STORAGE;

	/**
	 * Define what level of filtering on the data should be applied
	 */
	protected EDataFilterLevel dataFilterLevel;

	private IIDMappingManager idMappingManager;

	private Composite composite;

	// private Table labelTable;
	private Table contentTable;
	// private Table storageRemoverTable;

	private TableCursor contentTableCursor;

	// private Label lastRemoveContent;
	// private Label lastRemoveStorage;

	protected SelectionUpdateListener selectionUpdateListener = null;
	protected VirtualArrayUpdateListener virtualArrayUpdateListener = null;
	protected SelectionCommandListener selectionCommandListener = null;

	protected RedrawViewListener redrawViewListener = null;
	protected ClearSelectionsListener clearSelectionsListener = null;
	protected ReplaceVirtualArrayListener replaceVirtualArrayListener = null;

	// private Table labelTable;

	// private int[] iArCurrentColumnOrder;

	/**
	 * Constructor.
	 */
	public TabularDataView(final int iParentContainerId, final String sLabel) {
		super(iParentContainerId, sLabel, GeneralManager.get().getIDManager()
				.createID(EManagedObjectType.VIEW_SWT_TABULAR_DATA_VIEWER));

		this.viewType = VIEW_ID;

		contentSelectionManager = new SelectionManager.Builder(
				EIDType.EXPRESSION_INDEX).build();
		storageSelectionManager = new SelectionManager.Builder(
				EIDType.EXPERIMENT_INDEX).build();

		idMappingManager = generalManager.getIDMappingManager();
	}

	@Override
	public void initViewSWTComposite(Composite parentComposite) {
		composite = new Composite(parentComposite, SWT.NULL);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = layout.horizontalSpacing = 0;
		composite.setLayout(layout);

		initData();
		createPreviewTable();
	}

	@Override
	public void drawView() {

	}

	public void initData() {

		if (set == null) {
			contentSelectionManager.resetSelectionManager();
			storageSelectionManager.resetSelectionManager();
			return;
		}

		dataDomain = useCase.getDataDomain();
		contentVA = useCase.getVA(contentVAType);
		storageVA = useCase.getVA(storageVAType);

		contentSelectionManager.resetSelectionManager();
		storageSelectionManager.resetSelectionManager();

		contentSelectionManager.setVA(contentVA);
		storageSelectionManager.setVA(storageVA);

		int iNumberOfColumns = contentVA.size();
		int iNumberOfRows = storageVA.size();

		for (int iRowCount = 0; iRowCount < iNumberOfRows; iRowCount++) {
			storageSelectionManager.initialAdd(storageVA.get(iRowCount));

		}

		// this for loop executes one per axis
		for (int iColumnCount = 0; iColumnCount < iNumberOfColumns; iColumnCount++) {
			contentSelectionManager.initialAdd(contentVA.get(iColumnCount));
		}
	}

	private void createPreviewTable() {

		contentTable = new Table(composite, SWT.MULTI | SWT.BORDER
				| SWT.FULL_SELECTION | SWT.VIRTUAL);
		contentTable.setLinesVisible(true);
		contentTable.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 300;
		data.widthHint = 700;
		contentTable.setLayoutData(data);

		// Clear table if not empty
		contentTable.removeAll();

		contentTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);

				// TableItem selectedItem = ((TableItem)e.item);
				// final int iSelectedRowIndex =
				// contentTable.indexOf(selectedItem);
				// addRemoveIcon(iSelectedRowIndex);
				// triggerContentSelectionEvent(iSelectedRowIndex,
				// ESelectionType.SELECTION);

			}
		});

		contentTable.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Rectangle clientArea = contentTable.getClientArea();
				Point pt = new Point(event.x, event.y);

				int index = 0; // only make caption line editable

				boolean visible = false;
				final TableItem item = contentTable.getItem(index);
				for (int iColIndex = 1; iColIndex < contentTable
						.getColumnCount(); iColIndex++) {
					Rectangle rect = item.getBounds(iColIndex);
					if (rect.contains(pt)) {
						final int column = iColIndex;
						final Text text = new Text(contentTable, SWT.NONE);
						Listener textListener = new Listener() {
							public void handleEvent(final Event e) {
								switch (e.type) {
								case SWT.FocusOut:
									item.setText(column, text.getText());
									text.dispose();
									break;
								case SWT.Traverse:
									switch (e.detail) {
									case SWT.TRAVERSE_RETURN:
										item.setText(column, text.getText());

										// FALL THROUGH
									case SWT.TRAVERSE_ESCAPE:
										text.dispose();
										e.doit = false;
									}
									break;
								}
							}
						};

						text.addListener(SWT.FocusOut, textListener);
						text.addListener(SWT.Traverse, textListener);
						// editor.setEditor(text, item, iColIndex);
						text.setText(item.getText(iColIndex));
						text.selectAll();
						text.setFocus();
						return;
					}

					if (!visible && rect.intersects(clientArea)) {
						visible = true;
					}
				}

				if (!visible)
					return;
				index++;
			}
		});

		TableColumn column;
		TableItem item;
		float fValue;

		column = new TableColumn(contentTable, SWT.NONE);
		column.setText("#");
		column.setWidth(50);

		if (dataDomain == EDataDomain.GENETIC_DATA) {

			column = new TableColumn(contentTable, SWT.NONE);
			column.setText("RefSeq ID");
			column.setWidth(110);

			column = new TableColumn(contentTable, SWT.NONE);
			column.setText("Gene Symbol");
			column.setWidth(110);
		} else if (dataDomain == EDataDomain.UNSPECIFIED) {

			column = new TableColumn(contentTable, SWT.NONE);
			column.setText("ID");
			column.setWidth(200);

			column = new TableColumn(contentTable, SWT.NONE);
			column.setText("Not specified");
			column.setWidth(0);
		} else {
			throw new IllegalStateException("The data domain " + dataDomain
					+ " is not implemented in the tabular data viewer.");
		}

		for (final Integer iStorageIndex : storageVA) {
			column = new TableColumn(contentTable, SWT.NONE);
			column.setText(set.get(iStorageIndex).getLabel());
			column.setWidth(120);
			column.setMoveable(true);

			column.addSelectionListener(new SelectionAdapter() {
				// label changer
				@Override
				public void widgetSelected(SelectionEvent e) {
					LabelEditorDialog dialog = new LabelEditorDialog(
							new Shell());
					String sLabel = dialog.open(set.get(iStorageIndex)
							.getLabel());

					if (sLabel != null && !sLabel.isEmpty()) {
						set.get(iStorageIndex).setLabel(sLabel);
						contentTable.getColumn(iStorageIndex).setText(sLabel);
						RedrawViewEvent event = new RedrawViewEvent();
						event.setSender(this);
						eventPublisher.triggerEvent(event);
					}
				}
			});
		}

		int index = 0;
		for (Integer iContentIndex : contentVA) {
			// line number
			item = new TableItem(contentTable, SWT.NONE);
			// item.setData(iContentIndex);
			item.setText(0, Integer.toString(index));

			if (dataDomain == EDataDomain.GENETIC_DATA) {
				String sGeneSymbol = null;
				String srefSeqID = null;

				// FIXME: Due to new mapping system, a mapping involving
				// expression index can return a Set of
				// values, depending on the IDType that has been specified when
				// loading expression data.
				// Possibly a different handling of the Set is required.
				Set<String> setRefSeqIDs = idMappingManager.getIDAsSet(
						EIDType.EXPRESSION_INDEX, EIDType.REFSEQ_MRNA,
						iContentIndex);

				if ((setRefSeqIDs != null && !setRefSeqIDs.isEmpty())) {
					srefSeqID = (String) setRefSeqIDs.toArray()[0];
				}

				item.setText(1, srefSeqID);

				// FIXME: Due to new mapping system, a mapping involving
				// expression index can return a Set of
				// values, depending on the IDType that has been specified when
				// loading expression data.
				// Possibly a different handling of the Set is required.
				Set<String> setGeneSymbols = idMappingManager.getIDAsSet(
						EIDType.EXPRESSION_INDEX, EIDType.GENE_SYMBOL,
						iContentIndex);

				if ((setGeneSymbols != null && !setGeneSymbols.isEmpty())) {
					sGeneSymbol = (String) setGeneSymbols.toArray()[0];
				}

				if (sGeneSymbol != null) {
					item.setText(2, sGeneSymbol);
				} else {
					item.setText(2, "Unknown");
				}
			} else if (dataDomain == EDataDomain.UNSPECIFIED) {

				item.setText(1, (String) idMappingManager.getID(
						EIDType.EXPRESSION_INDEX, EIDType.UNSPECIFIED,
						iContentIndex));
			} else {
				throw new IllegalStateException("The use case type "
						+ dataDomain
						+ " is not implemented in the tabular data viewer.");
			}

			int i = 3;
			for (Integer iStorageIndex : storageVA) {
				fValue = set.get(iStorageIndex).getFloat(
						EDataRepresentation.RAW, iContentIndex);

				item.setText(i++, Float.toString(fValue));
			}

			index++;
		}
		for (TableColumn tempColumn : contentTable.getColumns()) {
			tempColumn.pack();
		}

		contentTableCursor = new TableCursor(contentTable, SWT.NONE);
		contentTableCursor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int iColIndex = contentTableCursor.getColumn();
				int iRowIndex = contentTable.indexOf(contentTableCursor
						.getRow());
				contentTable.setSelection(iRowIndex);

				int iRefSeqID = contentVA.get(iRowIndex);
				int iStorageIndex = storageVA.get(iColIndex);

				triggerStorageSelectionEvent(iStorageIndex,
						ESelectionType.SELECTION);
				triggerContentSelectionEvent(iRefSeqID,
						ESelectionType.SELECTION);

				// addContentRemoveIcon(iRowIndex);
				// addStorageRemoveIcon(iStorageIndex);
			}
		});
		composite.redraw();
	}

	@Override
	public void handleRedrawView() {
		// nothing to do here
	}

	@Override
	public void handleUpdateView() {
		// nothing to do here
	}

	@Override
	public void handleClearSelections() {
		clearAllSelections();
	}

	@Override
	public void handleSelectionCommand(EIDCategory category,
			SelectionCommand selectionCommand) {
		if (EIDCategory.GENE == category)
			contentSelectionManager.executeSelectionCommand(selectionCommand);
		else
			storageSelectionManager.executeSelectionCommand(selectionCommand);
	}

	@Override
	public void handleSelectionUpdate(ISelectionDelta selectionDelta,
			boolean scroolToSelection, String info) {
		// Check for type that can be handled
		if (selectionDelta.getIDType() == EIDType.REFSEQ_MRNA_INT
				|| selectionDelta.getIDType() == EIDType.EXPRESSION_INDEX) {
			contentSelectionManager.setDelta(selectionDelta);
		} else if (selectionDelta.getIDType() == EIDType.EXPERIMENT_INDEX) {
			storageSelectionManager.setDelta(selectionDelta);
		}

		reactOnExternalSelection();
	}

	@Override
	public void handleVirtualArrayUpdate(IVirtualArrayDelta delta, String info) {
		SelectionManager selectionManager;
		if (delta.getIDType() == EIDType.EXPERIMENT_INDEX) {
			storageSelectionManager.setVADelta(delta);

			for (VADeltaItem deltaItem : delta.getAllItems()) {
				final int iVAIndex = deltaItem.getIndex();

				switch (deltaItem.getType()) {
				case REMOVE:
					composite.getDisplay().asyncExec(new Runnable() {
						public void run() {
							contentTable.getColumn(iVAIndex + 3).dispose();
						}
					});
					break;
				case ADD:
					addColumn(deltaItem.getIndex() + COLUMN_OFFSET, deltaItem
							.getPrimaryID());
					break;
				case COPY:
					addColumn(deltaItem.getIndex() + 1 + COLUMN_OFFSET,
							storageVA.get(deltaItem.getIndex()));

					break;
				case MOVE:
					// case MOVE_LEFT:
					// case MOVE_RIGHT:
					int[] orig = contentTable.getColumnOrder();

					System.out.println(Arrays.toString(orig));
					ArrayList<Integer> ordered = new ArrayList<Integer>(
							orig.length);

					for (int index : orig) {
						ordered.add(index);
					}

					Integer item = ordered.remove(deltaItem.getIndex()
							+ COLUMN_OFFSET);
					System.out.println(ordered);
					ordered.add(deltaItem.getTargetIndex() + COLUMN_OFFSET,
							item);
					System.out.println(ordered);
					for (int count = 0; count < ordered.size(); count++) {
						orig[count] = ordered.get(count);
					}

					// order.

					// // Integer iElement = virtualArray.remove(iSrcIndex);
					// virtualArray.add(iTargetIndex, iElement);

					// int[] columnOrder = new int[storageVA.size()
					// + COLUMN_OFFSET];
					// columnOrder[0] = 0;
					// columnOrder[1] = 1;
					// columnOrder[2] = 2;
					// for (int i = COLUMN_OFFSET; i < storageVA.size()
					// + COLUMN_OFFSET; i++)
					// columnOrder[i] = storageVA.get(i - COLUMN_OFFSET)
					// + COLUMN_OFFSET;
					System.out.println(Arrays.toString(orig));
					contentTable.setColumnOrder(orig);
					break;
				default:
					throw new IllegalStateException(
							"EVAOperation not implemented");
				}
			}

		} else if (delta.getIDType() == EIDType.REFSEQ_MRNA_INT) {
			delta = DeltaConverter
					.convertDelta(EIDType.EXPRESSION_INDEX, delta);
			selectionManager = contentSelectionManager;
			selectionManager.setVADelta(delta);
		} else if (delta.getIDType() == EIDType.EXPRESSION_INDEX) {
			selectionManager = contentSelectionManager;
			selectionManager.setVADelta(delta);
		} else
			return;

		// reactOnVAChanges(delta);

		// int[] columnOrder = contentTable.getColumnOrder();
		// for (int i = 0; i < order.length / 2; i++) {
		// int temp = order[i];
		// order[i] = order[order.length - i - 1];
		// order[order.length - i - 1] = temp;
		// }

		// int[] order = contentTable.getColumnOrder();
		// for (int i = 0; i < order.length / 2; i++) {
		// int temp = order[i];
		// order[i] = order[order.length - i - 1];
		// order[order.length - i - 1] = temp;
		// }
		// contentTable.setColumnOrder(order);

		// createPreviewTable();
		// contentTable.pack();
		// reactOnExternalSelection();
	}

	private void addColumn(final int index, final int storageNumber) {
		composite.getDisplay().asyncExec(new Runnable() {
			public void run() {
				TableColumn column = new TableColumn(contentTable, SWT.NONE,
						index);
				IStorage storage = set.get(storageNumber);
				column.setText(storage.getLabel());
				TableItem[] items = contentTable.getItems();
				for (int i = 0; i < items.length; i++) {
					TableItem item = items[i];
					float value = set.get(storageNumber).getFloat(
							EDataRepresentation.RAW, contentVA.get(i));
					item.setText(index, Float.toString(value));

				}
				column.pack();

			}
		});
	}

	/**
	 * Highlight the selected cell in the table. Only the first element is
	 * taken, since we cannot handle multiple selections ATM.
	 */
	private void reactOnExternalSelection() {
		composite.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (contentTable.isDisposed())
					return;

				int iRowIndex = 0;
				int iColIndex = 0;
				contentTable.deselectAll();

				Iterator<Integer> iterContentIndex = contentSelectionManager
						.getElements(ESelectionType.SELECTION).iterator();

				// FIXME: currently we do not handle multiple selections (->
				// replace if with while)
				while (iterContentIndex.hasNext()) {
					iRowIndex = contentVA.indexOf(iterContentIndex.next());
					contentTable.select(iRowIndex);
				}

				// FIXME: currently we do not handle multiple selections (->
				// replace if with while)
				Iterator<Integer> iterStorageIndex = storageSelectionManager
						.getElements(ESelectionType.SELECTION).iterator();
				while (iterStorageIndex.hasNext()) {
					iColIndex = storageVA.indexOf(iterStorageIndex.next()) + 3;
				}

				contentTableCursor.setSelection(iRowIndex, contentTable
						.getColumnOrder()[iColIndex]);
			}
		});
	}

	private void triggerContentSelectionEvent(int iContentIndex,
			ESelectionType eSelectionType) {
		if (contentSelectionManager.checkStatus(eSelectionType, iContentIndex))
			return;

		contentSelectionManager.clearSelection(eSelectionType);
		contentSelectionManager.addToType(eSelectionType, iContentIndex);

		if (dataDomain == EDataDomain.GENETIC_DATA) {
			// Resolve multiple spotting on chip and add all to the
			// selection manager.
			Integer iRefSeqID = null;
			// FIXME: Due to new mapping system, a mapping involving expression
			// index can return a Set of
			// values, depending on the IDType that has been specified when
			// loading expression data.
			// Possibly a different handling of the Set is required.
			Set<Integer> setRefSeqIDs = idMappingManager.getIDAsSet(
					EIDType.EXPRESSION_INDEX, EIDType.REFSEQ_MRNA_INT,
					iContentIndex);

			if ((setRefSeqIDs != null && !setRefSeqIDs.isEmpty())) {
				iRefSeqID = (Integer) setRefSeqIDs.toArray()[0];
			}
			if (iRefSeqID != null) {
				for (Object iExpressionIndex : idMappingManager
						.<Integer, Object> getIDAsSet(EIDType.REFSEQ_MRNA_INT,
								EIDType.EXPRESSION_INDEX, iRefSeqID)) {
					contentSelectionManager.addToType(eSelectionType,
							(Integer) iExpressionIndex);
				}
			}
		}

		ISelectionDelta selectionDelta = contentSelectionManager.getDelta();

		SelectionCommand command = new SelectionCommand(
				ESelectionCommandType.CLEAR, eSelectionType);
		sendSelectionCommandEvent(EIDType.EXPRESSION_INDEX, command);

		SelectionUpdateEvent event = new SelectionUpdateEvent();
		event.setSender(this);
		event.setSelectionDelta((SelectionDelta) selectionDelta);
		eventPublisher.triggerEvent(event);
	}

	private void triggerStorageSelectionEvent(int iStorageIndex,
			ESelectionType eSelectionType) {
		if (storageSelectionManager.checkStatus(eSelectionType, iStorageIndex))
			return;

		storageSelectionManager.clearSelection(eSelectionType);
		storageSelectionManager.addToType(eSelectionType, iStorageIndex);

		SelectionCommand command = new SelectionCommand(
				ESelectionCommandType.CLEAR, eSelectionType);
		sendSelectionCommandEvent(EIDType.EXPERIMENT_INDEX, command);

		ISelectionDelta selectionDelta = storageSelectionManager.getDelta();
		SelectionUpdateEvent event = new SelectionUpdateEvent();
		event.setSender(this);
		event.setSelectionDelta((SelectionDelta) selectionDelta);
		eventPublisher.triggerEvent(event);
	}

	// private void addContentRemoveIcon(int iRowIndex) {
	// final Label lblRemove = new Label(labelTable, SWT.CENTER);
	// lblRemove.setBackground(lblRemove.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
	// lblRemove.setImage(generalManager.getResourceLoader().getImage(lblRemove.getDisplay(),
	// "resources/icons/view/tabular/remove.png"));
	// lblRemove.setData(contentVA.get(iRowIndex));
	//
	// // Only one remove icon should be shown at a time
	// if (lastRemoveContent != null) {
	// lastRemoveContent.dispose();
	// }
	//
	// lastRemoveContent = lblRemove;
	//
	// final TableEditor editor = new TableEditor(labelTable);
	// editor.grabHorizontal = true;
	// editor.setEditor(lblRemove, labelTable.getItem(iRowIndex), 0);
	//
	// lblRemove.addMouseListener(new MouseAdapter() {
	// @Override
	// public void mouseDown(MouseEvent e) {
	// Integer iExpressionIndex = (Integer) lblRemove.getData();
	//
	// contentVA.remove(iExpressionIndex);
	//
	// IVirtualArrayDelta vaDelta = new VirtualArrayDelta(EVAType.CONTENT,
	// EIDType.EXPRESSION_INDEX);
	// vaDelta.add(VADeltaItem.remove(iExpressionIndex));
	// VirtualArrayUpdateEvent virtualArrayUpdateEvent = new
	// VirtualArrayUpdateEvent();
	// virtualArrayUpdateEvent.setSender(this);
	// virtualArrayUpdateEvent.setVirtualArrayDelta((VirtualArrayDelta)
	// vaDelta);
	// virtualArrayUpdateEvent.setInfo(null);
	// eventPublisher.triggerEvent(virtualArrayUpdateEvent);
	//
	// // Dispose the removed row
	// TableItem item = (TableItem) e.widget.getData();
	// editor.getEditor().dispose();
	// editor.dispose();
	// item.dispose();
	// }
	// });
	// }

	// private void addStorageRemoveIcon(int iStorageIndex) {
	// final Label lblRemove = new Label(storageRemoverTable, SWT.CENTER);
	// lblRemove.setBackground(lblRemove.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
	// lblRemove.setImage(generalManager.getResourceLoader().getImage(lblRemove.getDisplay(),
	// "resources/icons/view/tabular/remove.png"));
	// lblRemove.setData(storageVA.get(iStorageIndex));
	//
	// // Only one remove icon should be shown at a time
	// if (lastRemoveStorage != null) {
	// lastRemoveStorage.dispose();
	// }
	//
	// lastRemoveStorage = lblRemove;
	//
	// final TableEditor editor = new TableEditor(storageRemoverTable);
	// editor.grabHorizontal = true;
	// editor.setEditor(lblRemove, storageRemoverTable.getItem(0),
	// iStorageIndex);
	//
	// lblRemove.addMouseListener(new MouseAdapter() {
	// @Override
	// public void mouseDown(MouseEvent e) {
	// Integer iStorageIndex = (Integer) lblRemove.getData();
	//
	// storageVA.remove(iStorageIndex);
	//
	// IVirtualArrayDelta vaDelta = new VirtualArrayDelta(EVAType.STORAGE,
	// EIDType.EXPERIMENT_INDEX);
	// vaDelta.add(VADeltaItem.remove(iStorageIndex));
	// VirtualArrayUpdateEvent virtualArrayUpdateEvent = new
	// VirtualArrayUpdateEvent();
	// virtualArrayUpdateEvent.setSender(this);
	// virtualArrayUpdateEvent.setVirtualArrayDelta((VirtualArrayDelta)
	// vaDelta);
	// virtualArrayUpdateEvent.setInfo(null);
	// eventPublisher.triggerEvent(virtualArrayUpdateEvent);
	//
	// // Dispose the removed row
	// editor.getEditor().dispose();
	// editor.dispose();
	// lblRemove.dispose();
	//
	// contentTable.getColumn(iStorageIndex).dispose();
	// storageRemoverTable.getColumn(iStorageIndex).dispose();
	// }
	// });
	// }

	private void clearAllSelections() {
		contentSelectionManager.clearSelections();
		storageSelectionManager.clearSelections();
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedTabularDataView serializedForm = new SerializedTabularDataView(
				dataDomain);
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public void initFromSerializableRepresentation(ASerializedView ser) {
		// this implementation does not initialize anything yet
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();
		selectionUpdateListener = new SelectionUpdateListener();
		selectionUpdateListener.setHandler(this);
		eventPublisher.addListener(SelectionUpdateEvent.class,
				selectionUpdateListener);

		virtualArrayUpdateListener = new VirtualArrayUpdateListener();
		virtualArrayUpdateListener.setHandler(this);
		eventPublisher.addListener(VirtualArrayUpdateEvent.class,
				virtualArrayUpdateListener);

		selectionCommandListener = new SelectionCommandListener();
		selectionCommandListener.setHandler(this);
		eventPublisher.addListener(SelectionCommandEvent.class,
				selectionCommandListener);

		redrawViewListener = new RedrawViewListener();
		redrawViewListener.setHandler(this);
		eventPublisher.addListener(RedrawViewEvent.class, redrawViewListener);

		clearSelectionsListener = new ClearSelectionsListener();
		clearSelectionsListener.setHandler(this);
		eventPublisher.addListener(ClearSelectionsEvent.class,
				clearSelectionsListener);

		replaceVirtualArrayListener = new ReplaceVirtualArrayListener();
		replaceVirtualArrayListener.setHandler(this);
		eventPublisher.addListener(ReplaceVirtualArrayEvent.class,
				replaceVirtualArrayListener);
	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();
		if (selectionUpdateListener != null) {
			eventPublisher.removeListener(selectionUpdateListener);
			selectionUpdateListener = null;
		}
		if (virtualArrayUpdateListener != null) {
			eventPublisher.removeListener(virtualArrayUpdateListener);
			virtualArrayUpdateListener = null;
		}
		if (selectionCommandListener != null) {
			eventPublisher.removeListener(selectionCommandListener);
			selectionCommandListener = null;
		}
		if (redrawViewListener != null) {
			eventPublisher.removeListener(redrawViewListener);
			redrawViewListener = null;
		}
		if (clearSelectionsListener != null) {
			eventPublisher.removeListener(clearSelectionsListener);
			clearSelectionsListener = null;
		}

		if (replaceVirtualArrayListener != null) {
			eventPublisher.removeListener(replaceVirtualArrayListener);
			replaceVirtualArrayListener = null;
		}
	}

	@Override
	public void replaceVirtualArray(EIDCategory idCategory, EVAType vaType) {

		String primaryVAType = useCase.getVATypeForIDCategory(idCategory);
		if (primaryVAType == null)
			return;

		EVAType suggestedVAType = EVAType
				.getVATypeForPrimaryVAType(primaryVAType);

		if (vaType != suggestedVAType
				|| vaType.getPrimaryVAType() != primaryVAType)
			return;

		if (vaType == storageVAType)
			storageVA = useCase.getVA(vaType);
		else if (vaType == contentVAType)
			contentVA = useCase.getVA(vaType);
		else
			return;

		initData();
	}

}
