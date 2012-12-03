package org.caleydo.core.view.opengl.layout.util.multiform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL2;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.view.IRemoteViewCreator;
import org.caleydo.core.view.ViewManager;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.AForwardingRenderer;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.LayoutRenderer;
import org.caleydo.core.view.opengl.layout.util.ViewLayoutRenderer;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;

/**
 * Renderer that allows to switch between different remotely rendered {@link AGLView}s or {@link LayoutRenderer}s and
 * handles remote view creation.
 *
 * @author Christian Partl
 *
 */
public class MultiFormRenderer extends AForwardingRenderer {

	/**
	 * Renderer that is used to render active remote views.
	 */
	private ViewLayoutRenderer viewRenderer = new ViewLayoutRenderer();

	/**
	 * The view that displays this renderer in its own canvas.
	 */
	private AGLView remoteRenderingView;

	/**
	 * Determines whether the views added to this renderer are immediately created or not until set used the first time.
	 */
	private boolean isLazyViewCreation;

	/**
	 * Map that stores all {@link ARendererInfo}s and associates it with an identifier.
	 */
	private Map<Integer, ARendererInfo> rendererInfos = new HashMap<>();

	/**
	 * The highest id of a {@link ARendererInfo} that is currently stored in {@link #rendererInfos}.
	 */
	private int currentMaxRendererID = 0;

	/**
	 * {@link ARendererInfo} that is currently active and whose renderer is therefore displayed.
	 */
	private ARendererInfo currentRendererInfo;

	/**
	 * Determines whether a default renderer is currently used without setting a renderer active.
	 */
	private boolean isDefaultRenderer = true;

	/**
	 * Set of {@link IMultiFormChangeListener}s that are informed, when this multiform renderer changes.
	 */
	private Set<IMultiFormChangeListener> changeListeners = new HashSet<>();

	/**
	 * Abstract base class for renderer information that is used by {@link MultiFormRenderer}.
	 *
	 * @author Christian Partl
	 *
	 */
	private abstract class ARendererInfo {

		/**
		 * ID used by {@link MultiFormRenderer} to identify the different renderers.
		 */
		protected int rendererID;

		/**
		 * Path to the icon file that is used to represent the renderer.
		 */
		protected String iconPath;

		/**
		 * Performs all necessary operations to set the associated rendering entity active in {@link MultiFormRenderer}.
		 */
		abstract void setActive();

		/**
		 * Called in every render cycle to prepare the renderer.
		 *
		 * @param gl
		 */
		abstract void prepareRenderer(GL2 gl);
	}

	/**
	 * Info that holds necessary information for remote rendered views.
	 *
	 * @author Christian Partl
	 */
	private class ViewInfo extends ARendererInfo {
		/**
		 * ID of the view type.
		 */
		private String viewID;
		/**
		 * ID that is used to identify the appropriate {@link IRemoteViewCreator} for this view.
		 */
		private String embeddingID;
		/**
		 * Table perspectives that shall be displayed by the view.
		 */
		private List<TablePerspective> tablePerspectives;
		/**
		 * The view.
		 */
		private AGLView view;

		/**
		 * Determines whether
		 * {@link AGLView#initRemote(GL2, AGLView, org.caleydo.core.view.opengl.mouse.GLMouseListener)} has already been
		 * called.
		 */
		private boolean isInitialized = false;

		@Override
		void setActive() {
			if (view == null) {
				view = createView(this);
			}
			viewRenderer.setView(view);
			currentRenderer = viewRenderer;
			currentRenderer.setLimits(x, y);
			MultiFormRenderer.this.isDisplayListDirty = true;
			currentRenderer.setDisplayListDirty();
		}

		@Override
		void prepareRenderer(GL2 gl) {
			if (!isInitialized) {
				view.initRemote(gl, remoteRenderingView, remoteRenderingView.getGLMouseListener());
				isInitialized = true;
			}
		}
	}

	/**
	 * Info that holds necessary information for {@link LayoutRenderer}s.
	 *
	 * @author Christian Partl
	 */
	private class LayoutRendererInfo extends ARendererInfo {
		/**
		 * The renderer.
		 */
		private LayoutRenderer renderer;

		@Override
		void setActive() {
			currentRenderer = renderer;
			currentRenderer.setLimits(x, y);
			MultiFormRenderer.this.isDisplayListDirty = true;
			currentRenderer.setDisplayListDirty();
		}

		@Override
		void prepareRenderer(GL2 gl) {
			// nothing to do
		}
	}

	public MultiFormRenderer(AGLView remoteRenderingView, boolean isLazyViewCreation) {
		this.remoteRenderingView = remoteRenderingView;
		this.isLazyViewCreation = isLazyViewCreation;
	}

	/**
	 * Adds a view to this {@link MultiFormRenderer}. Depending on whether lazy view creation is being used, the view is
	 * created immediately or the first time it is used.
	 *
	 * @param viewID
	 *            ID specifying the view type.
	 * @param embeddingID
	 *            ID that specifies the embedding in the parent view. This ID is used to determine the appropriate
	 *            {@link IRemoteViewCreator} for the embedded view.
	 * @param tablePerspectives
	 *            List of tablePerspectives that shall be displayed in the view.
	 * @return Identifier for the currently added view that can be used to set it active ({@link #setActive(int)}) or
	 *         remove.
	 */
	public int addView(String viewID, String embeddingID, List<TablePerspective> tablePerspectives) {

		ViewInfo info = new ViewInfo();
		info.viewID = viewID;
		info.embeddingID = embeddingID;
		info.tablePerspectives = tablePerspectives;
		info.iconPath = ViewManager.get().getRemotePlugInViewIcon(viewID, remoteRenderingView.getViewType(),
				embeddingID);
		if (info.iconPath == null) {
			info.iconPath = EIconTextures.NO_ICON_AVAILABLE.getFileName();
		}

		if (!isLazyViewCreation) {
			info.view = createView(info);
		}
		int rendererID = currentMaxRendererID++;
		info.rendererID = rendererID;
		rendererInfos.put(rendererID, info);

		// Set default renderer and info in case no activate is performed
		if (currentRendererInfo == null) {
			setDefaultRenderer(info, viewRenderer);
		}

		notifyAdded(rendererID);

		return rendererID;
	}

	/**
	 * Adds a {@link LayoutRenderer} to this {@link MultiFormRenderer}.
	 *
	 * @param renderer
	 *            The renderer to be added.
	 * @param iconPath
	 *            Path to the image file that shall be used for an iconic representation of the renderer. If null is
	 *            specified, a default icon will be used.
	 * @return Identifier for the currently added renderer that can be used to set it active ({@link #setActive(int)})
	 *         or remove.
	 */
	public int addLayoutRenderer(LayoutRenderer renderer, String iconPath) {
		LayoutRendererInfo info = new LayoutRendererInfo();
		info.renderer = renderer;
		if (info.iconPath == null) {
			info.iconPath = EIconTextures.NO_ICON_AVAILABLE.getFileName();
		} else {
			info.iconPath = iconPath;
		}

		int rendererID = currentMaxRendererID++;
		info.rendererID = rendererID;
		rendererInfos.put(rendererID, info);

		// Set default renderer and info in case no activate is performed
		if (currentRendererInfo == null) {
			setDefaultRenderer(info, renderer);
		}

		notifyAdded(rendererID);

		return rendererID;
	}

	/**
	 * Removes a renderer specified by its ID.
	 *
	 * @param rendererID
	 *            ID of the renderer.
	 * @param destroy
	 *            If true, the renderer will be destroyed.
	 */
	public void removeRenderer(int rendererID, boolean destroy) {

		ARendererInfo info = rendererInfos.get(rendererID);
		if (info == null)
			return;

		rendererInfos.remove(rendererID);

		if (info instanceof ViewInfo) {
			AGLView view = ((ViewInfo) info).view;
			if (view != null) {
				GeneralManager.get().getViewManager().unregisterGLView(view);
			}
		} else {
			LayoutRenderer renderer = ((LayoutRendererInfo) info).renderer;
			// The current renderer has already been destroyed by super
			if (currentRenderer != renderer) {
				GL2 gl = remoteRenderingView.getParentGLCanvas().asGLAutoDrawAble().getGL().getGL2();
				((LayoutRendererInfo) info).renderer.destroy(gl);
			}
		}

		notifyRemoved(rendererID);
	}

	/**
	 * Gets the file path of the icon that is associated with the {@link AGLView} or {@link LayoutRenderer} specified by
	 * the provided renderer ID.
	 *
	 * @param rendererID
	 *            ID that specifies the view or renderer.
	 * @return File path of the associated Icon. Null, if no renderer or view is associated with the specified ID.
	 */
	public String getIconPath(int rendererID) {
		ARendererInfo info = rendererInfos.get(rendererID);
		if (info != null) {
			return info.iconPath;
		}
		return null;
	}

	private void setDefaultRenderer(ARendererInfo info, LayoutRenderer renderer) {
		isDefaultRenderer = true;
		currentRendererInfo = info;
		currentRenderer = renderer;
	}

	private void setDefaultRendererActive() {
		if (isDefaultRenderer) {
			currentRendererInfo.setActive();
			isDefaultRenderer = false;
			notifyActive(currentRendererInfo.rendererID, -1);
		}
	}

	/**
	 * Sets a {@link AGLView} or {@link LayoutRenderer} previously added to this {@link MultiFormRenderer} active, so
	 * that it will be rendered. If the specified identifier is invalid, no operation is performed.
	 *
	 * @param rendererID
	 *            Identifier that specifies a view or layout renderer.
	 */
	public void setActive(int rendererID) {
		ARendererInfo info = rendererInfos.get(rendererID);
		if (info != null) {
			int previousRendererID = currentRendererInfo != null ? currentRendererInfo.rendererID : -1;
			currentRendererInfo = info;
			info.setActive();
			isDefaultRenderer = false;
			notifyActive(rendererID, previousRendererID);
		}
	}

	/**
	 * Creates a view using the given view info.
	 *
	 * @param viewInfo
	 * @return
	 */
	private AGLView createView(ViewInfo viewInfo) {
		return ViewManager.get().createRemotePlugInView(viewInfo.viewID, viewInfo.embeddingID, remoteRenderingView,
				viewInfo.tablePerspectives);
	}

	/**
	 * Gets the {@link AGLView} associated with the provided rendererID.
	 *
	 * @param rendererID
	 *            Identifier that specifies the view.
	 * @return The view that is associated with the specified ID. Null, if no view corresponds to this ID.
	 */
	public AGLView getView(int rendererID) {
		ARendererInfo info = rendererInfos.get(rendererID);
		if (info == null)
			return null;

		if (info instanceof ViewInfo) {
			ViewInfo viewInfo = (ViewInfo) info;
			if (viewInfo.view == null) {
				viewInfo.view = createView(viewInfo);
			}
			return viewInfo.view;
		}
		return null;
	}

	/**
	 * Gets the {@link LayoutRenderer} associated with the provided rendererID.
	 *
	 * @param rendererID
	 *            Identifier that specifies the layout.
	 * @return The renderer that is associated with the specified ID. Null, if no renderer corresponds to this ID.
	 */
	public LayoutRenderer getLayoutRenderer(int rendererID) {
		ARendererInfo info = rendererInfos.get(rendererID);
		if (info == null)
			return null;

		if (info instanceof LayoutRendererInfo) {
			LayoutRendererInfo layoutRendererInfo = (LayoutRendererInfo) info;
			return layoutRendererInfo.renderer;
		}
		return null;
	}

	/**
	 * @return The ids of all {@link AGLView}s and {@link LayoutRenderer}s added.
	 */
	public Set<Integer> getRendererIDs() {
		return new HashSet<>(rendererInfos.keySet());
	}

	/**
	 * @param rendererID
	 * @return True, if an {@link AGLView} is associated with the provided ID.
	 */
	public boolean isView(int rendererID) {
		ARendererInfo info = rendererInfos.get(rendererID);
		if (info == null)
			return false;
		return (info instanceof ViewInfo);
	}

	/**
	 * @param rendererID
	 * @return True, if a {@link LayoutRenderer} is associated with the provided ID.
	 */
	public boolean isLayoutRenderer(int rendererID) {
		ARendererInfo info = rendererInfos.get(rendererID);
		if (info == null)
			return false;
		return (info instanceof LayoutRendererInfo);
	}

	@Override
	protected void renderContent(GL2 gl) {
		setDefaultRendererActive();
		currentRendererInfo.prepareRenderer(gl);
		super.renderContent(gl);
	}

	@Override
	protected boolean permitsWrappingDisplayLists() {
		setDefaultRendererActive();
		return super.permitsWrappingDisplayLists();
	}

	@Override
	protected void prepare() {
		setDefaultRendererActive();
		super.setDisplayListDirty();
	}

	@Override
	public void setDisplayListDirty() {
		setDefaultRendererActive();
		super.setDisplayListDirty();
	}

	@Override
	public boolean isDisplayListDirty() {
		setDefaultRendererActive();
		return super.isDisplayListDirty();
	}

	@Override
	public void setLimits(float x, float y) {
		setDefaultRendererActive();
		super.setLimits(x, y);
	}

	@Override
	public int getMinHeightPixels() {
		setDefaultRendererActive();
		return currentRenderer.getMinHeightPixels();
	}

	@Override
	public int getMinWidthPixels() {
		setDefaultRendererActive();
		return currentRenderer.getMinWidthPixels();
	}

	@Override
	protected void setElementLayout(ElementLayout elementLayout) {
		setDefaultRendererActive();
		super.setElementLayout(elementLayout);
	}

	/**
	 * @return the isLazyViewCreation, see {@link #isLazyViewCreation}
	 */
	public boolean isLazyViewCreation() {
		return isLazyViewCreation;
	}

	/**
	 * @param isLazyViewCreation
	 *            setter, see {@link isLazyViewCreation}
	 */
	public void setLazyViewCreation(boolean isLazyViewCreation) {
		this.isLazyViewCreation = isLazyViewCreation;
	}

	/**
	 * Adds a {@link IMultiFormChangeListener} to this {@link MultiFormRenderer}, if it is not already added.
	 *
	 * @param listener
	 */
	public void addChangeListener(IMultiFormChangeListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * Removes a {@link IMultiFormChangeListener} from this {@link MultiFormRenderer}.
	 *
	 * @param listener
	 */
	public void removeChangeListener(IMultiFormChangeListener listener) {
		changeListeners.remove(listener);
	}

	/**
	 * Notifies all registered {@link IMultiFormChangeListener}s of the change of the currently active renderer.
	 *
	 * @param currentRendererID
	 *            ID of the renderer that is now set active.
	 * @param previousRendererID
	 *            ID of the renderer that was set active before. -1 if no renderer was active before.
	 */
	protected void notifyActive(int currentRendererID, int previousRendererID) {
		for (IMultiFormChangeListener listener : changeListeners) {
			listener.activeRendererChanged(this, currentRendererID, previousRendererID);
		}
	}

	/**
	 * Notifies all registered {@link IMultiFormChangeListener}s of the added renderer.
	 *
	 * @param rendererID
	 *            ID of the renderer that was added.
	 */
	protected void notifyAdded(int rendererID) {
		for (IMultiFormChangeListener listener : changeListeners) {
			listener.rendererAdded(this, rendererID);
		}
	}

	/**
	 * Notifies all registered {@link IMultiFormChangeListener}s of the removed renderer.
	 *
	 * @param rendererID
	 *            ID of the renderer that was removed.
	 */
	protected void notifyRemoved(int rendererID) {
		for (IMultiFormChangeListener listener : changeListeners) {
			listener.rendererRemoved(this, rendererID);
		}
	}

	@Override
	public void destroy(GL2 gl) {
		super.destroy(gl);
		for (ARendererInfo info : rendererInfos.values()) {
			if (info instanceof ViewInfo) {
				AGLView view = ((ViewInfo) info).view;
				if (view != null) {
					GeneralManager.get().getViewManager().unregisterGLView(view);
				}
			} else {
				LayoutRenderer renderer = ((LayoutRendererInfo) info).renderer;
				// The current renderer has already been destroyed by super
				if (currentRenderer != renderer) {
					((LayoutRendererInfo) info).renderer.destroy(gl);
				}
			}
		}

		// The view renderer has already been destroyed by super
		if (currentRenderer != viewRenderer) {
			viewRenderer.destroy(gl);
		}

		for (IMultiFormChangeListener listener : changeListeners) {
			listener.destroyed(this);
			removeChangeListener(listener);
		}
	}
}
