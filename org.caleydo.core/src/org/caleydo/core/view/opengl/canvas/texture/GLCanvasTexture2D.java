// /**
// *
// */
// package org.caleydo.core.view.opengl.canvas.texture;
//
// import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.IOException;
//
// import javax.media.opengl.GL;
// import javax.media.opengl.GLException;
//
// import org.caleydo.core.data.collection.ISet;
// import org.caleydo.core.manager.IGeneralManager;
// import org.caleydo.core.manager.ILoggerManager.LoggerType;
// import org.caleydo.core.view.opengl.canvas.AGLCanvasUser;
//
// import com.sun.opengl.util.texture.Texture;
// import com.sun.opengl.util.texture.TextureCoords;
// import com.sun.opengl.util.texture.TextureIO;
//
// /**
// * @author Michael Kalkusch
// *
// * @see org.caleydo.core.view.opengl.IGLCanvasUser
// */
// public class GLCanvasTexture2D
// extends AGLCanvasUser
// {
//	
// private boolean bUseGLWireframe = false;
//	
// private boolean bGLBindTextureOnInitGL = true;
//	
// //private int iTextureId;
//	
// //private int iSetCacheId = 0;
//	 
// //private String sTextureFileName = "";
//	
// /**
// * Defien number of histogram slots.
// * Default is 0 to ensure valid settings.
// *
// * @see org.caleydo.core.view.opengl.canvas.histogram.GLCanvasHistogram2D#
// createHistogram(int)
// */
// private int iCurrentHistogramLength = 0;
//	
// private float [][] viewingFrame;
//	
// //private int iGridSize = 40;
//	
// //private float fPointSize = 1.0f;
//	
// /**
// * Color for grid (0,1,2)
// * grid text (3,4,5)
// * and point color (6,7,8)
// */
// // private float[] colorGrid = { 0.1f, 0.1f , 0.9f,
// // 0.1f, 0.9f, 0.1f,
// // 0.9f, 0.1f, 0.1f };
//	
// protected float[][] fAspectRatio;
//	
// protected float[] fResolution;
//	
// protected ISet targetSet;
//	
// protected Texture gLTexture = null;
//	
// protected boolean bEnableMipMapping = false;
//	
// public String sTextureLoadFromFile = null;
//	
// private static final int X = GLCanvasStatics.X;
// private static final int Y = GLCanvasStatics.Y;
// private static final int Z = GLCanvasStatics.Z;
// private static final int MIN = GLCanvasStatics.MIN;
// private static final int MAX = GLCanvasStatics.MAX;
// private static final int OFFSET = GLCanvasStatics.OFFSET;
//
//	
// /**
// * @param setGeneralManager
// */
// public GLCanvasTexture2D( final IGeneralManager setGeneralManager,
// int iViewId,
// int iParentContainerId,
// String sLabel )
// {
// super( setGeneralManager,
// null,
// iViewId,
// iParentContainerId,
// sLabel );
//		
// fAspectRatio = new float [2][3];
// viewingFrame = new float [3][2];
//		
// fAspectRatio[X][MIN] = 0.0f;
// fAspectRatio[X][MAX] = 20.0f;
// fAspectRatio[Y][MIN] = 0.0f;
// fAspectRatio[Y][MAX] = 20.0f;
//		
// fAspectRatio[Y][OFFSET] = 0.0f;
// fAspectRatio[Y][OFFSET] = -2.0f;
//		
// viewingFrame[X][MIN] = -1.0f;
// viewingFrame[X][MAX] = 1.0f;
// viewingFrame[Y][MIN] = 1.0f;
// viewingFrame[Y][MAX] = -1.0f;
//		
// viewingFrame[Z][MIN] = -1.0f;
// viewingFrame[Z][MAX] = -1.0f;
//		
// }
//	
//
//
// protected void loadTextureFromFile( final String sTextureFromFile ) {
//	
// try
// {
// gLTexture = TextureIO.newTexture(new File(sTextureFromFile),
// bEnableMipMapping);
// }
// catch ( FileNotFoundException fnfe)
// {
// System.out.println("GLCanvasTexture2D Error: can not find fiel for texture "
// + sTextureFromFile);
// }
// catch ( GLException gle)
// {
// System.out.println(
// "GLCanvasTexture2D Error: GLError while accessing texture from file " +
// sTextureFromFile + "  " + gle.toString() );
// }
// catch (IOException ioe)
// {
// System.out.println("GLCanvasTexture2D Error loading texture " +
// sTextureFromFile );
// }
//			
// try {
// gLTexture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
// gLTexture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
// }
// catch ( GLException gle)
// {
// System.out.println(
// "GLCanvasTexture2D Error: GLError while accessing texture from file " +
// sTextureFromFile + "  " + gle.toString() );
// }
// }
//	
// public void setResolution( float[] setResolution ) {
//		
// // if ( fResolution.length < 6 ) {
// // throw new RuntimeException(
// "GLCanvasMinMaxScatterPlot2D.setResolution() array must contain 3 items.");
// // }
//		
// this.fResolution = setResolution;
//		
// fAspectRatio[X][MIN] = fResolution[0];
// fAspectRatio[X][MAX] = fResolution[1];
// fAspectRatio[Y][MIN] = fResolution[2];
// fAspectRatio[Y][MAX] = fResolution[3];
//		
// fAspectRatio[X][OFFSET] = fResolution[4];
// fAspectRatio[Y][OFFSET] = fResolution[5];
//		
// viewingFrame[X][MIN] = fResolution[6];
// viewingFrame[X][MAX] = fResolution[7];
// viewingFrame[Y][MIN] = fResolution[8];
// viewingFrame[Y][MAX] = fResolution[9];
//		
// viewingFrame[Z][MIN] = fResolution[10];
// viewingFrame[Z][MAX] = fResolution[11];
//			
// }
//	
//	
// public void setFileNameForTexture( final String sTextureLoadFromFile ) {
// this.sTextureLoadFromFile = sTextureLoadFromFile;
// }
//	
//	
// public String getFileNameForTexture() {
// return sTextureLoadFromFile;
// }
//	
//	
// public void reloadTexture() {
//		
// if ( sTextureLoadFromFile != null ) {
// loadTextureFromFile( sTextureLoadFromFile );
// }
// }
//	
// public void setTargetSetId( final int iTargetCollectionSetId ) {
//		
// targetSet =
// generalManager.getSingelton().getSetManager(
// ).getItemSet( iTargetCollectionSetId );
//		
// if ( targetSet == null ) {
// generalManager.getSingelton().logMsg(
// "GLCanvasScatterPlot2D.setTargetSetId(" +
// iTargetCollectionSetId + ") failed, because Set is not registed!",
// LoggerType.ERROR );
// }
//		
// generalManager.getSingelton().logMsg(
// "GLCanvasScatterPlot2D.setTargetSetId(" +
// iTargetCollectionSetId + ") done!",
// LoggerType.STATUS );
//		
// if ( iCurrentHistogramLength > 0 )
// {
// reloadTexture();
// }
// }
//	
// /*
// * (non-Javadoc)
// * @see org.caleydo.core.view.opengl.IGLCanvasUser#init(javax.media.opengl.
// GLAutoDrawable)
// */
// public void initGLCanvasUser( GL gl ) {
// setInitGLDone();
// System.err.println(" Texture2D ! init( * )");
// reloadTexture();
//		
// if ( bGLBindTextureOnInitGL ) {
// gl.glEnable(GL.GL_TEXTURE_2D);
// // iTextureId = genTextures_Id(gl);
// // gl.glBindTexture(GL.GL_TEXTURE_2D, iTextureId);
// //
// gLTexture.bind();
// }
// }
//	
// // private int genTextures_Id(GL gl) {
// // final int[] tmp = new int[1];
// // gl.glGenTextures(1, tmp, 0);
// // return tmp[0];
// // }
//	
// @Override
// public void renderPart(GL gl)
// {
// System.err.println(" Texture2D ! render( * )");
//		
// gl.glTranslatef( 0,0, 0.01f);
//	
// if ( gLTexture == null) {
// this.reloadTexture();
// }
//		
// displayHistogram( gl );
//		
// //System.err.println(" MinMax ScatterPlot2D .render(GLCanvas canvas)");
// }
//
//	
// public void update(GL gl)
// {
// System.err.println(" GLCanvasHistogram2D.update(GLCanvas canvas)");
//		
// reloadTexture();
// }
//
// public void destroyGLCanvas()
// {
// generalManager.getSingelton().logMsg(
// "GLCanvasHistogram2D.destroy(GLCanvas canvas)  id=" + this.iUniqueId,
// LoggerType.FULL );
// }
//	
//
//  
// public void displayHistogram(GL gl) {
//
// //gl.glClear(GL.GL_COLOR_BUFFER_BIT|GL.GL_DEPTH_BUFFER_BIT);
//
//
// if (bUseGLWireframe) {
// gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
// }
//	    
// // else
// // {
// // gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
// // }
//	    
//
// // draw background
// // gl.glDisable(GL.GL_DEPTH_TEST);
// // drawSkyBox(gl);
// // gl.glEnable(GL.GL_DEPTH_TEST);
//
//
// // gl.glDisable( GL.GL_LIGHTING );
//	    
// gl.glEnable( GL.GL_LIGHTING );
//	    
// gl.glEnable(GL.GL_TEXTURE_2D);
//	  
//	    	
//    	
// // float fNowX = viewingFrame[X][MIN];
// // float fNextX = viewingFrame[X][MAX];
// //
// // float fNowY = viewingFrame[Y][MIN];
// // float fNextY = viewingFrame[Y][MAX];
//    	
// gl.glNormal3f( 0.0f, 0.0f, 1.0f );
//
//    	
//    
// // gl.glBegin( GL.GL_TRIANGLE_FAN );
// // gl.glBegin( GL.GL_LINE_LOOP );
//		    			  
// //
// // gl.glColor3f( 1,
// // 1 ,
// // 0 );
// //
// // gl.glVertex3f( fNowX, fNowY , viewingFrame[Z][MIN] );
// // gl.glVertex3f( fNextX, fNowY, viewingFrame[Z][MIN] );
// // gl.glVertex3f( fNextX, fNextY, viewingFrame[Z][MIN] );
// // gl.glVertex3f( fNowX, fNextY, viewingFrame[Z][MIN] );
// //
// //
// // gl.glEnd();
//				
// System.out.println("GLCanvasTexture2D - TEXTURE!");
//				
// if ( gLTexture == null ) {
// System.err.println("GLCanvasTexture2D TEXTURE not bound!");
// return;
// }
//				
// gLTexture.bind();
//			
// TextureCoords texCoords = gLTexture.getImageTexCoords();
//
// // System.err.println("Height: "+(float)pathwayTexture.getImageHeight());
// // System.err.println("Width: "+(float)pathwayTexture.getImageWidth());
// //
// // System.err.println("Aspect ratio: " +fPathwayTextureAspectRatio);
// // System.err.println("texCoords left: " +texCoords.left());
// // System.err.println("texCoords right: " +texCoords.right());
// // System.err.println("texCoords top: " +texCoords.top());
// // System.err.println("texCoords bottom: " +texCoords.bottom());
//			
//				
//				 
//				
// //else {
// gl.glBegin( GL.GL_TRIANGLES );
// gl.glNormal3f( 0.0f, 0.0f, 1.0f );
// gl.glColor3f( 1,0,0 );
//				
// gl.glTexCoord2f(texCoords.left(), texCoords.top());
// gl.glVertex3f( -1.0f, -1.0f, -0.5f );
// //gl.glColor3f( 1,0,1 );
//				
// gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
// gl.glVertex3f( 1.0f, 1.0f, -0.5f );
// //gl.glColor3f( 0,1,0 );
//				
// gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
// gl.glVertex3f( 1.0f, -1.0f, -0.5f );
// gl.glEnd();
// //
// // float fmin = -2.0f;
// // float fmax = 2.0f;
// //
// // float fshiftX = -1.0f;
// // float fshiftY = -2.0f;
// //
// // gl.glBegin( GL.GL_TRIANGLES );
// // gl.glNormal3f( 0.0f, 0.0f, 0.0f );
// // gl.glColor3f( 1,1,0 );
// //
// // gl.glVertex3f( fmin+fshiftX, fmax+fshiftY, 0.0f );
// // gl.glColor3f( 1,0,1 );
// // gl.glVertex3f( fmax+fshiftX, fmin+fshiftY, 0.0f );
// // gl.glColor3f( 0,1,1 );
// // gl.glVertex3f( fmax+fshiftX, fmax+fshiftY, 0.0f );
// // gl.glEnd();
// //}
//	    
// gl.glDisable( GL.GL_LIGHTING );
//			
// // gl.glEnable( GL.GL_LIGHTING );
//	    
// //gl.glMatrixMode(GL.GL_MODELVIEW);
// //gl.glPopMatrix();
// }
//  
// }
