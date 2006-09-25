/**
 * 
 */
package cerberus.view.gui.opengl;

import javax.media.opengl.GLCanvas;

import cerberus.view.gui.opengl.IGLCanvasDirector;

/**
 * @author java
 *
 */
public class GLObjectTriangleTest implements IGLCanvasUser
{

	protected GLCanvas refCanvas;
	
	protected IGLCanvasDirector refParentView;
	
	/**
	 * 
	 */
	public GLObjectTriangleTest()
	{
		
	}
	
	/* (non-Javadoc)
	 * @see cerberus.view.gui.opengl.IGLCanvasUser#link2GLCanvas(javax.media.opengl.GLCanvas, java.lang.Object)
	 */
	public void link2GLCanvasDirector( IGLCanvasDirector parentView ) {
		this.refParentView = parentView;
	}


	public final IGLCanvasDirector getGLCanvasDirector() {
		return refParentView;
	}
	
	/* (non-Javadoc)
	 * @see cerberus.view.gui.opengl.IGLCanvasUser#render(javax.media.opengl.GLCanvas)
	 */
	public void render( GLCanvas canvas ) {
		this.refCanvas = canvas;
	}
	
	/* (non-Javadoc)
	 * @see cerberus.view.gui.opengl.IGLCanvasUser#update(javax.media.opengl.GLCanvas)
	 */
	public void update( GLCanvas canvas ) {
		this.refCanvas = canvas;
	}
	
	/* (non-Javadoc)
	 * @see cerberus.view.gui.opengl.IGLCanvasUser#getGLCanvas()
	 */
	public final GLCanvas getGLCanvas() {
		return refCanvas;
	}
	
	public void destroy() {
		this.refCanvas = null;
		this.refParentView = null;
	}
}
