package org.caleydo.datadomain.tissue;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.manager.datadomain.ADataDomain;
import org.caleydo.core.manager.datadomain.DataDomainManager;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;

/**
 * The data domain for tissue input data.
 * 
 * @author Marc Streit
 */
@XmlType
@XmlRootElement
public class TissueDataDomain extends ADataDomain {

	public final static String DATA_DOMAIN_TYPE = "org.caleydo.datadomain.tissue";
	
	/**
	 * Counter used for determining the extension that together with the type
	 * builds the data domain ID.
	 */
	private static int extensionID = 0;

	/**
	 * Constructor.
	 */
	public TissueDataDomain() {
		
		super(DATA_DOMAIN_TYPE, DATA_DOMAIN_TYPE + DataDomainManager.DATA_DOMAIN_INSTANCE_DELIMITER + extensionID++);
		
		icon = EIconTextures.DATA_DOMAIN_TISSUE;

		// possibleIDCategories.put(EIDCategory.GENE, null);
	}
	
	@Override
	protected void initIDMappings() {
		// nothing to do ATM
	}

	@Override
	public void registerEventListeners() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterEventListeners() {
		// TODO Auto-generated method stub
		
	}
}
