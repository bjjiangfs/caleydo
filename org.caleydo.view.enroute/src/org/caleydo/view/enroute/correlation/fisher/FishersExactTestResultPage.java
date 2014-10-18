/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.enroute.correlation.fisher;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.io.gui.dataimport.widget.table.NatTableToolTip;
import org.caleydo.view.enroute.correlation.ContingencyTableConfiguration;
import org.caleydo.view.enroute.correlation.DataCellInfo;
import org.caleydo.view.enroute.correlation.IDataClassifier;
import org.caleydo.view.enroute.correlation.SimpleCategory;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.Lists;

import edu.northwestern.at.utils.math.statistics.FishersExactTest;

/**
 * @author Christian
 *
 */
public class FishersExactTestResultPage extends WizardPage implements IPageChangedListener {

	protected boolean visited = false;

	protected NatTable table;

	protected int[][] contingencyTable = new int[2][2];

	protected CategoryHeaderProvider columnHeaderProvider = new CategoryHeaderProvider(true);
	protected CategoryHeaderProvider rowHeaderProvider = new CategoryHeaderProvider(false);
	protected ContingencyTableBodyProvider bodyProvider = new ContingencyTableBodyProvider();

	protected Composite parentComposite;
	protected Group contingencyTableGroup;

	private Set<Color> colorRegistry = new HashSet<>();

	protected Label twoSidedPValueLabel;
	protected Label leftTailPValueLabel;
	protected Label rightTailPValueLabel;

	private static class CategoryHeaderProvider implements IDataProvider {

		private boolean isColumnHeader;
		private IDataClassifier classifier;

		/**
		 * @param isColumnHeader
		 * @param classifier
		 */
		public CategoryHeaderProvider(boolean isColumnHeader) {
			this.isColumnHeader = isColumnHeader;
		}

		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			if (classifier == null) {
				return "C" + (isColumnHeader ? columnIndex : rowIndex);
			}
			List<SimpleCategory> categories = classifier.getDataClasses();
			return isColumnHeader ? categories.get(columnIndex).name : categories.get(rowIndex).name;
		}

		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			// TODO Auto-generated method stub

		}

		@Override
		public int getColumnCount() {

			return isColumnHeader ? 2 : 1;
		}

		@Override
		public int getRowCount() {
			return isColumnHeader ? 1 : 2;
		}

		/**
		 * @param classifier
		 *            setter, see {@link classifier}
		 */
		public void setClassifier(IDataClassifier classifier) {
			this.classifier = classifier;
		}

	}

	private class ContingencyTableBodyProvider implements IRowDataProvider<Object> {

		// Dummy row objects
		private List<Object> rowObjects = Lists.newArrayList(new Object(), new Object());

		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			Integer value = contingencyTable[columnIndex][rowIndex];
			return value.toString();
		}

		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			// TODO Auto-generated method stub

		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return 2;
		}

		@Override
		public Object getRowObject(int rowIndex) {
			return rowObjects.get(rowIndex);
		}

		@Override
		public int indexOfRowObject(Object rowObject) {
			return rowObjects.indexOf(rowObject);
		}

	}

	private static class MyRowGroupHeaderLayer<T> extends RowGroupHeaderLayer<T> {

		private ILayerPainter myLayerPainter = new GridLineCellLayerPainter();

		/**
		 * @param rowHeaderLayer
		 * @param selectionLayer
		 * @param rowGroupModel
		 */
		public MyRowGroupHeaderLayer(ILayer rowHeaderLayer, SelectionLayer selectionLayer,
				IRowGroupModel<T> rowGroupModel) {
			super(rowHeaderLayer, selectionLayer, rowGroupModel);
		}

		@Override
		public ILayerPainter getLayerPainter() {
			return myLayerPainter;
		}

	}

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	protected FishersExactTestResultPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(Composite parent) {
		parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		parentComposite.setLayout(new GridLayout(1, false));

		contingencyTableGroup = new Group(parentComposite, SWT.SHADOW_ETCHED_IN);
		contingencyTableGroup.setText("Contingency Table");
		contingencyTableGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		contingencyTableGroup.setLayout(new GridLayout(1, false));

		buildTable(contingencyTableGroup);

		Group pValuesGroup = new Group(parentComposite, SWT.SHADOW_ETCHED_IN);
		pValuesGroup.setText("P-Values");
		pValuesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		pValuesGroup.setLayout(new GridLayout(1, false));

		twoSidedPValueLabel = createLabel(pValuesGroup, "Two-Sided: ");
		leftTailPValueLabel = createLabel(pValuesGroup, "Left-Tail: ");
		rightTailPValueLabel = createLabel(pValuesGroup, "Right-Tail: ");

		setControl(parentComposite);

	}

	private Label createLabel(Composite parentComposite, String text) {
		Label resultLabel = new Label(parentComposite, SWT.NONE);
		resultLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		resultLabel.setText(text);
		return resultLabel;
	}

	protected void buildTable(Composite parentComposite) {

		disposeColors();
		final DataLayer bodyDataLayer = new DataLayer(bodyProvider, 180, 36);
		// bodyDataLayer.addLayerListener(this);

		SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
		// selectionLayer.addLayerListener(this);
		ViewportLayer bodyLayer = new ViewportLayer(selectionLayer);

		final DataLayer columnDataLayer = new DataLayer(columnHeaderProvider, 180, 36);
		ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnDataLayer, bodyLayer, selectionLayer);

		ColumnGroupModel columnGroupModel = new ColumnGroupModel();

		ColumnGroupHeaderLayer columnGroupHeaderLayer = new ColumnGroupHeaderLayer(columnHeaderLayer, selectionLayer,
				columnGroupModel);

		final FishersExactTestWizard wizard = (FishersExactTestWizard) getWizard();

		columnGroupHeaderLayer.addColumnsIndexesToGroup(getInfoString(wizard.getInfo1()), 0, 1);
		columnGroupHeaderLayer.setRowHeight(64);
		columnGroupHeaderLayer.clearConfiguration();

		DataLayer rowDataLayer = new DataLayer(rowHeaderProvider, 180, 36);
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowDataLayer, bodyLayer, selectionLayer, true,
				new GridLineCellLayerPainter());

		RowGroupModel<Object> rowGroupModel = new RowGroupModel<>();
		rowGroupModel.setDataProvider(bodyProvider);
		MyRowGroupHeaderLayer<Object> rowGroupHeaderLayer = new MyRowGroupHeaderLayer<Object>(rowHeaderLayer,
				selectionLayer, rowGroupModel);
		rowGroupHeaderLayer.setColumnWidth(180);
		rowGroupHeaderLayer.clearConfiguration();
		rowGroupHeaderLayer.addConfiguration(new DefaultRowHeaderStyleConfiguration());

		RowGroup<Object> rowGroup = new RowGroup<Object>(rowGroupModel, getInfoString(wizard.getInfo2()), false);
		rowGroup.addMemberRow(bodyProvider.getRowObject(0));
		rowGroup.addStaticMemberRow(bodyProvider.getRowObject(1));
		rowGroupModel.addRowGroup(rowGroup);

		DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderProvider,
				rowHeaderProvider);
		CornerLayer cornerLayer = new CornerLayer(new DataLayer(cornerDataProvider), rowGroupHeaderLayer,
				columnGroupHeaderLayer);
		GridLayer gridLayer = new GridLayer(bodyLayer, columnGroupHeaderLayer, rowGroupHeaderLayer, cornerLayer);
		if (table == null) {
			table = new NatTable(parentComposite, gridLayer, false);
		} else {
			table.setLayer(gridLayer);
		}
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 720;
		gd.heightHint = 160;
		table.setLayoutData(gd);

		table.addConfiguration(new ContingencyTableConfiguration());
		DefaultToolTip toolTip = new NatTableToolTip(table);
		toolTip.activate();
		toolTip.setShift(new Point(10, 10));

		ColumnOverrideLabelAccumulator acc = new ColumnOverrideLabelAccumulator(columnDataLayer);
		columnDataLayer.setConfigLabelAccumulator(acc);
		acc.registerColumnOverrides(0, "C0");
		acc.registerColumnOverrides(1, "C1");

		rowDataLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {

			@Override
			public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
				configLabels.addLabel("R" + rowPosition);
			}
		});

		// NatTableUtil.applyDefaultNatTableStyling(table);
		//
		table.addConfiguration(new AbstractRegistryConfiguration() {

			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				addBackgroundStyles(configRegistry, wizard.getCell1Classifier(), "C");
				addBackgroundStyles(configRegistry, wizard.getCell2Classifier(), "R");
			}

			private void addBackgroundStyles(IConfigRegistry configRegistry, IDataClassifier classifier,
					String labelPrefix) {
				if (classifier == null)
					return;
				for (int i = 0; i < classifier.getDataClasses().size(); i++) {
					Color color = classifier.getDataClasses().get(i).color.getSWTColor(Display.getCurrent());
					Style cellStyle = new Style();
					cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, color);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.NORMAL, labelPrefix + i);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.SELECT, labelPrefix + i);
					colorRegistry.add(color);
				}
			}
		});

		table.configure();

	}

	private String getInfoString(DataCellInfo info) {
		if (info == null)
			return "undefined";
		StringBuilder b = new StringBuilder("Dataset: " + info.getDataDomainLabel() + System.lineSeparator());
		b.append("Group: " + info.getGroupLabel() + System.lineSeparator());
		b.append("Row: " + info.getRowLabel());
		return b.toString();
	}

	private void disposeColors() {
		for (Color c : colorRegistry) {
			c.dispose();
		}
		colorRegistry.clear();
	}

	@Override
	public void dispose() {
		disposeColors();
		super.dispose();
	}

	@Override
	public void pageChanged(PageChangedEvent event) {
		if (event.getSelectedPage() == this) {
			FishersExactTestWizard wizard = (FishersExactTestWizard) getWizard();
			DataCellInfo info1 = wizard.getInfo1();
			DataCellInfo info2 = wizard.getInfo2();
			IDataClassifier classifier1 = wizard.getCell1Classifier();
			IDataClassifier classifier2 = wizard.getCell2Classifier();

			IDMappingManager mappingManager = IDMappingManagerRegistry.get().getIDMappingManager(
					info1.columnPerspective.getIdType());
			IIDTypeMapper<Object, Object> mapper = mappingManager.getIDTypeMapper(info1.columnPerspective.getIdType(),
					info2.columnPerspective.getIdType());
			contingencyTable = new int[2][2];

			for (int cell1ColumnID : info1.columnPerspective.getVirtualArray()) {

				int index1 = getContingencyIndex(info1.dataDomain, info1.columnPerspective.getIdType(), cell1ColumnID,
						info1.rowIDType, info1.rowID, classifier1);
				if (index1 == -1)
					continue;

				Set<Object> cell2ColumnIDs = mapper.apply(cell1ColumnID);
				if (cell2ColumnIDs != null && !cell2ColumnIDs.isEmpty()) {
					Integer cell2ColumnID = (Integer) cell2ColumnIDs.iterator().next();
					if (info2.columnPerspective.getVirtualArray().contains(cell2ColumnID)) {
						int index2 = getContingencyIndex(info2.dataDomain, info2.columnPerspective.getIdType(),
								cell2ColumnID, info2.rowIDType, info2.rowID, classifier2);
						if (index2 == -1)
							continue;
						contingencyTable[index1][index2]++;
					}
				}

			}

			double[] result = FishersExactTest.fishersExactTest(contingencyTable[0][0], contingencyTable[0][1],
					contingencyTable[1][0], contingencyTable[1][1]);

			twoSidedPValueLabel.setText(String.format(Locale.ENGLISH, "Two-Sided: %.6e", result[0], result[0]));
			leftTailPValueLabel.setText(String.format(Locale.ENGLISH, "Left-Tail:  %.6e", result[1]));
			rightTailPValueLabel.setText(String.format(Locale.ENGLISH, "Right-Tail:  %.6e", result[2]));
			visited = true;

			columnHeaderProvider.setClassifier(classifier1);
			rowHeaderProvider.setClassifier(classifier2);

			buildTable(contingencyTableGroup);

			table.refresh();
			getShell().layout(true, true);
			getShell().pack();
			getWizard().getContainer().updateButtons();
		}

	}

	private int getContingencyIndex(ATableBasedDataDomain dataDomain, IDType columnIDType, int columnID,
			IDType rowIDType, int rowID, IDataClassifier classifier) {
		Object value = dataDomain.getRaw(columnIDType, columnID, rowIDType, rowID);
		List<SimpleCategory> classes = classifier.getDataClasses();
		SimpleCategory c = classifier.apply(value);
		if (c == null)
			return -1;
		return classes.indexOf(c);
	}

	@Override
	public boolean isPageComplete() {

		return visited && super.isPageComplete();
	}

}
