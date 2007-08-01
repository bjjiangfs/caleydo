package cerberus.util.opengl;

import java.util.ArrayList;

import javax.media.opengl.GL;

/**
 * 
 * @author Marc Streit
 */
public class GLStarEffect {
	
	public static void main(String [ ] args) {
		
		calculateStarPoints(3, 1, 0, 0);
	}
	
	public static final ArrayList<float[]> calculateStarPoints(int iVertexCount,
			float fRadius,
			float fCenterPointX, 
			float fCenterPointY) {
		
		float fAngleRad = (float) (2 * Math.PI / iVertexCount);
		ArrayList<float[]> alStarPoints = new ArrayList<float[]>();

		float[] fArPoint = null;
		
		// Store center point in index 0
		fArPoint = new float[2];
		fArPoint[0] = fCenterPointX;
		fArPoint[1] = fCenterPointY;
		alStarPoints.add(fArPoint);
		
		fCenterPointY += fRadius;
		
		for (int iVertexIndex = 0; iVertexIndex < iVertexCount; iVertexIndex++)
		{
			fArPoint = new float[2];
			 
			fArPoint[0] = (float) (fCenterPointX * Math.cos(fAngleRad * iVertexIndex) - 
					fCenterPointY * Math.sin(fAngleRad * iVertexIndex));
			fArPoint[1] = (float) (fCenterPointY * Math.cos(fAngleRad * iVertexIndex) + 
					fCenterPointX * Math.sin(fAngleRad * iVertexIndex));
			
			alStarPoints.add(fArPoint);
			
		}
		
		return alStarPoints;
	}
	
	public static void drawStar(final GL gl,
			final ArrayList<float[]> alStarPoints) {
		
		float[] fArPoint = new float[2];

		gl.glLineWidth(3);
		gl.glColor3f(1, 0, 0);
		
		float[] fArCenterPoint = alStarPoints.get(0);

		gl.glBegin(GL.GL_LINES);
		for (int iVertexIndex = 1; iVertexIndex < alStarPoints.size(); iVertexIndex++)
		{
			fArPoint = alStarPoints.get(iVertexIndex);

			gl.glVertex3f(fArCenterPoint[0], fArCenterPoint[1], 0);
			gl.glVertex3f(fArPoint[0], fArPoint[1], 0);
		}
		gl.glEnd();	
	}
}
