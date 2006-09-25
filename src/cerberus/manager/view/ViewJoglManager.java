package cerberus.manager.view;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;

import cerberus.manager.IGeneralManager;
import cerberus.manager.ILoggerManager.LoggerType;
import cerberus.manager.IViewManager;
import cerberus.manager.IViewGLCanvasManager;
import cerberus.manager.base.AAbstractManager;
import cerberus.manager.type.ManagerObjectType;
import cerberus.manager.type.ManagerType;
import cerberus.util.exception.CerberusRuntimeException;
import cerberus.view.gui.IView;
import cerberus.view.gui.swt.data.explorer.DataExplorerViewRep;
import cerberus.view.gui.swt.data.DataTableViewRep;
import cerberus.view.gui.swt.pathway.jgraph.PathwayViewRep;
import cerberus.view.gui.swt.progressbar.ProgressBarViewRep;
import cerberus.view.gui.swt.gears.jogl.GearsViewRep;
import cerberus.view.gui.swt.heatmap.jogl.SwtJogHistogram2DViewRep;
import cerberus.view.gui.swt.jogl.SwtJoglGLCanvasViewRep;
import cerberus.view.gui.swt.jogl.sample.TestTriangleViewRep;
import cerberus.view.gui.swt.scatterplot.jogl.Scatterplot2DViewRep;
import cerberus.view.gui.swt.slider.SliderViewRep;
import cerberus.view.gui.swt.heatmap.jogl.Heatmap2DViewRep;
import cerberus.view.gui.swt.test.TestTableViewRep;

public class ViewJoglManager 
extends AAbstractManager
implements IViewManager, IViewGLCanvasManager
{
	
	/**
	 * Stores, which GLEventListener are registered to one GLCanvas.
	 * hashGLCanvasId2Vector provices mapping of unique-GLCanvas-Id to position inside this vector.
	 */
	private  Hashtable <Integer, Vector<GLEventListener> > hashGLCanvasId_2_vecGLEventListener;

	
	
	protected Hashtable<Integer, IView> hashViewId2View;
	
	protected Hashtable <Integer,GLCanvas> hashGLCanvas;
	
	/** speed up removal of GLCanvastbon2!WS objects. */
	protected Hashtable <GLCanvas,Integer> hashGLCanvas_revert;
	
	protected Hashtable <Integer,GLEventListener> hashGLEventListener;
	
	/** speed up removal of GLEventListener objects. */
	protected Hashtable <GLEventListener,Integer> hashGLEventListener_revert;
	
		
	public ViewJoglManager(IGeneralManager setGeneralManager)
	{
		super(setGeneralManager,
				IGeneralManager.iUniqueId_TypeOffset_GUI_AWT );

		assert setGeneralManager != null : "Constructor with null-pointer to singelton";

		hashViewId2View = new Hashtable<Integer, IView>();
		
		hashGLCanvas = new  Hashtable <Integer,GLCanvas> ();
		hashGLCanvas_revert = new  Hashtable <GLCanvas,Integer> ();
		
		hashGLEventListener = new Hashtable <Integer,GLEventListener> ();		
		hashGLEventListener_revert = new Hashtable <GLEventListener,Integer> (); 
		
		/** internal datastructure to map GLCanvas to GLEventListeners .. */
		hashGLCanvasId_2_vecGLEventListener = 
			new Hashtable <Integer, Vector<GLEventListener> > ();
		
		
		refGeneralManager.getSingelton().setViewGLCanvasManager(this);
	}

	public boolean hasItem(int iItemId)
	{
		if (hashViewId2View.containsKey(iItemId))
			return true;
		
		if (hashGLCanvas.containsKey(iItemId))
			return true;
		
		if (hashGLEventListener.containsKey(iItemId))
			return true;
		
		return false;
	}

	public Object getItem(int iItemId)
	{
		IView bufferIView = hashViewId2View.get(iItemId);
		
		if ( bufferIView != null ) 
			return bufferIView;
		
		GLCanvas bufferCanvas = hashGLCanvas.get( iItemId );
		
		if ( bufferCanvas != null ) 
			return bufferCanvas;
		
		return hashGLEventListener.get( iItemId );
	}

	public int size()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public ManagerObjectType getManagerType()
	{
		assert false:"not done yet";
		return null;
	}

	public boolean registerItem(Object registerItem, int iItemId,
			ManagerObjectType type)
	{
		IView registerView = (IView) registerItem;
		
		hashViewId2View.put(iItemId, registerView);
		
		refGeneralManager.getSingelton().getLoggerManager().logMsg( 
				"registerItem( " + iItemId + " ) as View",
				LoggerType.VERBOSE.getLevel() );
		
		return true;
	}

	public boolean unregisterItem(int iItemId, ManagerObjectType type)
	{
		assert false : "not done yet";
		return false;
	}

//	/**
//	 * Method creates a new ID and 
//	 * calls createView(ManagerObjectType useViewType, int iUniqueId).
//	 */
//	public IView createView(final ManagerObjectType useViewType)
//	{
//		final int iUniqueId = this.createNewId(useViewType);
//		
//		return createView(useViewType, iUniqueId);
//	}

	/**
	 * Method creates a new view representation according to the 
	 * type parameter.
	 */
	public IView createView(ManagerObjectType useViewType, int iViewId,
			int iParentContainerId, String sLabel)
	{
		if (useViewType.getGroupType() != ManagerType.VIEW)
		{
			throw new CerberusRuntimeException(
					"try to create object with wrong type "
							+ useViewType.name());
		}

		//final int iNewId = this.createNewId(useViewType);

		switch (useViewType)
		{
		case VIEW:

		case VIEW_SWT_PATHWAY:
			return new PathwayViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);
		case VIEW_SWT_DATA_EXPLORER:
			return new DataExplorerViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);
		case VIEW_SWT_PROGRESS_BAR:
			return new ProgressBarViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);
		case VIEW_SWT_TEST_TABLE:
			return new TestTableViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);	
		case VIEW_SWT_GEARS:
			return new GearsViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);
		case VIEW_SWT_HEATMAP2D:
			return new Heatmap2DViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);
		case VIEW_SWT_HISTOGRAM2D:
			return new SwtJogHistogram2DViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);
			//return new Heatmap2DViewRep(iNewId, this.refGeneralManager);
		case VIEW_SWT_SCATTERPLOT2D:
			return new Scatterplot2DViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);
		case VIEW_SWT_SCATTERPLOT3D:
			return new Scatterplot2DViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);
		case VIEW_SWT_SLIDER:
			return new SliderViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);
		case VIEW_SWT_JOGL_TEST_TRIANGLE:
			return new TestTriangleViewRep(
					this.refGeneralManager, iViewId, iParentContainerId, sLabel);
		case VIEW_SWT_JOGL_MULTI_GLCANVAS:
				return new SwtJoglGLCanvasViewRep(
						this.refGeneralManager, iViewId, iParentContainerId, sLabel);
		

		default:
			throw new CerberusRuntimeException(
					"StorageManagerSimple.createView() failed due to unhandled type ["
							+ useViewType.toString() + "]");
		}
	}

	public GLCanvas getGLCanvas( final int iId)
	{
		return hashGLCanvas.get( iId );
	}

	public boolean registerGLCanvas( final GLCanvas canvas, final int iCanvasId)
	{
		if ( hashGLCanvas.containsKey( iCanvasId ) ) 
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg("registerGLCanvas() id " +
					iCanvasId + " is already registerd!");
			return false;
		}
		if ( hashGLCanvas.containsValue( canvas ) ) 
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg("registerGLCanvas() canvas bound to id " +
					iCanvasId + " is already registerd!");
			return false;
		}
		
		hashGLCanvas.put( iCanvasId, canvas );
		hashGLCanvas_revert.put( canvas, iCanvasId );
		
		synchronized( getClass() ) {
			hashGLCanvasId_2_vecGLEventListener.put( iCanvasId, new Vector <GLEventListener> () );
		}
		
		return true;
	}

	public boolean unregisterGLCanvas( final GLCanvas canvas)
	{
		if ( hashGLCanvas_revert.containsKey( canvas ) ) 
		{
			
			int iCanvasId = hashGLCanvas_revert.get( canvas );
			
			hashGLCanvas.remove( iCanvasId );
			hashGLCanvas_revert.remove( canvas );
			
			synchronized( getClass() ) 
			{
				/* get all GLEventListeners registerd to this GLCanvas... */
				Vector <GLEventListener> vecListOfRemoveableGLEventListeners =  
					hashGLCanvasId_2_vecGLEventListener.get( iCanvasId );
				
				Iterator <GLEventListener> iterListeners = 
					vecListOfRemoveableGLEventListeners.iterator();
				
				while ( iterListeners.hasNext() ) 
				{
					canvas.removeGLEventListener( iterListeners.next() );
				}
				
				/* Clean up Hashtable with id -> Vector<GLEventListener> .. */
				hashGLCanvasId_2_vecGLEventListener.remove( iCanvasId );
			}
			/** Unregister all GLListeners to! */
			
			return true;
		}
			
		refGeneralManager.getSingelton().getLoggerManager().logMsg(
			"unregisterGLCanvas() canvas object was not found inside ViewJogleManager!");

		return false;
	}

	public GLEventListener getGLEventListener( final int iId)
	{
		return hashGLEventListener.get( iId );
	}

	/**
	 * Register a new GLEventListener with a new Id.
	 */
	public boolean registerGLEventListener(GLEventListener canvasListener, int iId)
	{
		if ( hashGLEventListener.containsKey( iId ) ) 
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg("registerGLEventListener() id " +
					iId + " is already registerd!");
			return false;
		}
		if ( hashGLEventListener.containsValue( canvasListener ) ) 
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg("registerGLEventListener() canvas bound to id " +
					iId + " is already registerd!");
			return false;
		}
		
		hashGLEventListener.put( iId, canvasListener );
		
		return true;
	}

	/**
	 * Attention: Unregister GLEventListener at GLCanvas before callint this methode.
	 * 
	 */
	public boolean unregisterGLEventListener(GLEventListener canvasListener)
	{
		if ( ! hashGLEventListener_revert.containsKey( canvasListener ) ) {
			refGeneralManager.getSingelton().getLoggerManager().logMsg("unregisterGLEventListener() because GLEventListern is unkown!");
			return false;
		}
		
		int icanvasListenerId = hashGLEventListener_revert.get( canvasListener );
		
		hashGLEventListener.remove( icanvasListenerId );
		hashGLEventListener_revert.remove( canvasListener );
		
		//TODO: Unregister GLEventListener at GLCanvas before!
		
		return true;
	}

	public boolean addGLEventListener2GLCanvasById(int iCanvasListenerId, int iCanvasId)
	{
		GLEventListener listener = hashGLEventListener.get( iCanvasListenerId );
		
		if ( listener == null )
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg("addGLEventListener2GLCanvasById() because GLEventListern is not registered!");			
			return false;
		}
		
		GLCanvas canvas = hashGLCanvas.get( iCanvasId );
		
		if ( canvas==null )
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg("addGLEventListener2GLCanvasById() because GLCanvas is not registered!");			
			return false;
		}
		
		canvas.addGLEventListener( listener );
		
		return true;
	}
	
	public boolean removeGLEventListener2GLCanvasById(int iCanvasListenerId, int iCanvasId)
	{
		GLEventListener listener = hashGLEventListener.get( iCanvasListenerId );
		
		if ( listener == null )
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg("removeGLEventListener2GLCanvasById() because GLEventListern is not registered!");			
			return false;
		}
		
		GLCanvas canvas = hashGLCanvas.get( iCanvasId );
		
		if ( canvas==null )
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg("removeGLEventListener2GLCanvasById() because GLCanvas is not registered!");			
			return false;
		}
		
		canvas.removeGLEventListener( listener );
		
		return true;
	}
}
