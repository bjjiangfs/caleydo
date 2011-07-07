package org.caleydo.view.heatmap.heatmap.renderer;

import static org.caleydo.view.heatmap.HeatMapRenderStyle.DENDROGRAM_BACKROUND;
import gleem.linalg.Vec3f;

import javax.media.opengl.GL2;

import org.caleydo.core.view.opengl.layout.LayoutRenderer;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.core.view.opengl.util.texture.TextureManager;
import org.caleydo.view.heatmap.heatmap.GLHeatMap;
import org.caleydo.view.heatmap.uncertainty.OverviewRenderer;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

/**
 * 
 * Renderer connects the overview with the detail.
 * 
 * @author Marc Streit
 * 
 */
public class OverviewDetailConnectorRenderer extends LayoutRenderer {

	private OverviewRenderer overviewHeatMap;
	private GLHeatMap detailHeatMap;

	private TextureManager textureManager = new TextureManager();

	public OverviewDetailConnectorRenderer(OverviewRenderer overviewHeatMap,
			GLHeatMap detailHeatMap) {

		this.overviewHeatMap = overviewHeatMap;
		this.detailHeatMap = detailHeatMap;
	}

	public void render(GL2 gl) {

		gl.glColor4f(0f, 0f, 0f, 0.5f);

		float yOverview = overviewHeatMap.getSelectedClusterY();

		// gl.glBegin(GL2.GL_POLYGON);
		// gl.glVertex3f(0, yOverview, 0);
		// gl.glVertex3f(0,
		// yOverview+overviewHeatMap.getSelectedClusterHeight(), 0);
		// gl.glVertex3f(x, y, 0);
		// gl.glVertex3f(x, 0, 0);
		// gl.glEnd();

		render(gl,
				new Vec3f(0, yOverview + overviewHeatMap.getSelectedClusterHeight(), 0),
				new Vec3f(x, y, 0), new Vec3f(0, yOverview, 0), new Vec3f(x, 0, 0));
	}

	/**
	 * Render a curved (nice looking) grey area between two views
	 * FIXME: This is an old method written by Bernhard. It needs some cleanup.
	 * 
	 * @param gl
	 * @param startpoint1
	 * @param endpoint1
	 * @param startpoint2
	 * @param endpoint2
	 */
	private void render(GL2 gl, Vec3f startpoint1, Vec3f endpoint1, Vec3f startpoint2,
			Vec3f endpoint2) {
		float fthickness = (endpoint1.x() - startpoint1.x()) / 4;

		// Scaling factor for textures: endpoint1.y() > startpoint1.y()
		float fScalFactor1 = 0;
		// Scaling factor for textures: endpoint2.y() < startpoint2.y()
		float fScalFactor2 = 0;
		// Scaling factor for textures: endpoint1.y() < startpoint1.y()
		float fScalFactor3 = 0;

		boolean bHandleEndpoint1LowerStartpoint1 = false;

		if (endpoint1.y() < startpoint1.y()) {
			bHandleEndpoint1LowerStartpoint1 = true;
			fScalFactor1 = 0;
			fScalFactor3 = 1;
			if (startpoint1.y() - endpoint1.y() < fthickness)
				fScalFactor3 = (startpoint1.y() - endpoint1.y()) * 5f;
		} else if (endpoint1.y() - startpoint1.y() < fthickness) {
			fScalFactor1 = (endpoint1.y() - startpoint1.y()) * 5f;
		} else {
			fScalFactor1 = 1;
		}

		if (startpoint2.y() - endpoint2.y() < 0.2f) {
			fScalFactor2 = (startpoint2.y() - endpoint2.y()) * 5f;
		} else {
			fScalFactor2 = 1;
		}

		Texture textureMask = null;
		Texture textureMaskNeg = null;

		textureMask = textureManager.getIconTexture(gl,
				EIconTextures.NAVIGATION_MASK_CURVE);
		textureMaskNeg = textureManager.getIconTexture(gl,
				EIconTextures.NAVIGATION_MASK_CURVE_NEG);
		gl.glColor4fv(DENDROGRAM_BACKROUND, 0);

		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(startpoint1.x(), startpoint1.y(), startpoint1.z());
		gl.glVertex3f(startpoint1.x() + 2 * fthickness, startpoint1.y(), startpoint1.z());
		gl.glVertex3f(startpoint2.x() + 2 * fthickness, startpoint2.y(), startpoint2.z());
		gl.glVertex3f(startpoint2.x(), startpoint2.y(), startpoint2.z());
		gl.glEnd();

		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(endpoint1.x(), endpoint1.y(), endpoint1.z());
		gl.glVertex3f(endpoint1.x() - 1 * fthickness, endpoint1.y(), endpoint1.z());
		gl.glVertex3f(endpoint2.x() - 1 * fthickness, endpoint2.y(), endpoint2.z());
		gl.glVertex3f(endpoint2.x(), endpoint2.y(), endpoint2.z());
		gl.glEnd();

		// fill gap
		gl.glBegin(GL2.GL_QUADS);
		if (bHandleEndpoint1LowerStartpoint1) {
			gl.glVertex3f(endpoint1.x() - 1 * fthickness, startpoint1.y() - fthickness
					* fScalFactor3, endpoint1.z());
			gl.glVertex3f(endpoint1.x() - 2 * fthickness, startpoint1.y() - fthickness
					* fScalFactor3, endpoint1.z());
		} else {
			gl.glVertex3f(endpoint1.x() - 1 * fthickness, endpoint1.y() - fthickness
					* fScalFactor1, endpoint1.z());
			gl.glVertex3f(endpoint1.x() - 2 * fthickness, endpoint1.y() - fthickness
					* fScalFactor1, endpoint1.z());
		}

		gl.glVertex3f(endpoint2.x() - 2 * fthickness, endpoint2.y() + fthickness
				* fScalFactor2, endpoint2.z());
		gl.glVertex3f(endpoint2.x() - 1 * fthickness, endpoint2.y() + fthickness
				* fScalFactor2, endpoint2.z());
		gl.glEnd();

		gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_LINE_BIT);
		gl.glColor4f(1, 1, 1, 1);
		gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA);

		textureMask.enable();
		textureMask.bind();

		TextureCoords texCoordsMask = textureMask.getImageTexCoords();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2f(texCoordsMask.right(), texCoordsMask.top());
		gl.glVertex3f(startpoint1.x() + 2 * fthickness, startpoint1.y(), startpoint1.z());
		gl.glTexCoord2f(texCoordsMask.left(), texCoordsMask.top());
		gl.glVertex3f(startpoint1.x() + 1 * fthickness, startpoint1.y(), startpoint1.z());
		gl.glTexCoord2f(texCoordsMask.left(), texCoordsMask.bottom());
		gl.glVertex3f(startpoint1.x() + 1 * fthickness, startpoint1.y() + fthickness
				* fScalFactor1, startpoint1.z());
		gl.glTexCoord2f(texCoordsMask.right(), texCoordsMask.bottom());
		gl.glVertex3f(startpoint1.x() + 2 * fthickness, startpoint1.y() + fthickness
				* fScalFactor1, startpoint1.z());
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2f(texCoordsMask.right(), texCoordsMask.top());
		gl.glVertex3f(startpoint2.x() + 2 * fthickness, startpoint2.y(), startpoint2.z());
		gl.glTexCoord2f(texCoordsMask.left(), texCoordsMask.top());
		gl.glVertex3f(startpoint2.x() + 1 * fthickness, startpoint2.y(), startpoint2.z());
		gl.glTexCoord2f(texCoordsMask.left(), texCoordsMask.bottom());
		gl.glVertex3f(startpoint2.x() + 1 * fthickness, startpoint2.y() - fthickness
				* fScalFactor2, startpoint2.z());
		gl.glTexCoord2f(texCoordsMask.right(), texCoordsMask.bottom());
		gl.glVertex3f(startpoint2.x() + 2 * fthickness, startpoint2.y() - fthickness
				* fScalFactor2, startpoint2.z());
		gl.glEnd();

		if (bHandleEndpoint1LowerStartpoint1) {
			gl.glBegin(GL2.GL_POLYGON);
			gl.glTexCoord2f(texCoordsMask.right(), texCoordsMask.top());
			gl.glVertex3f(endpoint1.x() - 1 * fthickness, endpoint1.y(), endpoint1.z());
			gl.glTexCoord2f(texCoordsMask.left(), texCoordsMask.top());
			gl.glVertex3f(endpoint1.x(), endpoint1.y(), endpoint1.z());
			gl.glTexCoord2f(texCoordsMask.left(), texCoordsMask.bottom());
			gl.glVertex3f(endpoint1.x(), endpoint1.y() + fthickness * fScalFactor3,
					endpoint1.z());
			gl.glTexCoord2f(texCoordsMask.right(), texCoordsMask.bottom());
			gl.glVertex3f(endpoint1.x() - 1 * fthickness, endpoint1.y() + fthickness
					* fScalFactor3, endpoint1.z());
			gl.glEnd();
		}

		textureMask.disable();

		textureMaskNeg.enable();
		textureMaskNeg.bind();

		TextureCoords texCoordsMaskNeg = textureMaskNeg.getImageTexCoords();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2f(texCoordsMaskNeg.left(), texCoordsMaskNeg.bottom());
		gl.glVertex3f(endpoint1.x() - 2 * fthickness, endpoint1.y() - fthickness
				* fScalFactor1, endpoint1.z());
		gl.glTexCoord2f(texCoordsMaskNeg.right(), texCoordsMaskNeg.bottom());
		gl.glVertex3f(endpoint1.x() - 1 * fthickness, endpoint1.y() - fthickness
				* fScalFactor1, endpoint1.z());
		gl.glTexCoord2f(texCoordsMaskNeg.right(), texCoordsMaskNeg.top());
		gl.glVertex3f(endpoint1.x() - 1 * fthickness, endpoint1.y(), endpoint1.z());
		gl.glTexCoord2f(texCoordsMaskNeg.left(), texCoordsMaskNeg.top());
		gl.glVertex3f(endpoint1.x() - 2 * fthickness, endpoint1.y(), endpoint1.z());
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glTexCoord2f(texCoordsMaskNeg.left(), texCoordsMaskNeg.bottom());
		gl.glVertex3f(endpoint2.x() - 2 * fthickness, endpoint2.y() + fthickness
				* fScalFactor2, endpoint2.z());
		gl.glTexCoord2f(texCoordsMaskNeg.right(), texCoordsMaskNeg.bottom());
		gl.glVertex3f(endpoint2.x() - 1 * fthickness, endpoint2.y() + fthickness
				* fScalFactor2, endpoint2.z());
		gl.glTexCoord2f(texCoordsMaskNeg.right(), texCoordsMaskNeg.top());
		gl.glVertex3f(endpoint2.x() - 1 * fthickness, endpoint2.y(), endpoint2.z());
		gl.glTexCoord2f(texCoordsMaskNeg.left(), texCoordsMaskNeg.top());
		gl.glVertex3f(endpoint2.x() - 2 * fthickness, endpoint2.y(), endpoint2.z());
		gl.glEnd();

		if (bHandleEndpoint1LowerStartpoint1) {
			gl.glBegin(GL2.GL_POLYGON);
			gl.glTexCoord2f(texCoordsMaskNeg.right(), texCoordsMaskNeg.bottom());
			gl.glVertex3f(startpoint1.x() + 2 * fthickness, startpoint1.y() - fthickness
					* fScalFactor3, startpoint1.z());
			gl.glTexCoord2f(texCoordsMaskNeg.left(), texCoordsMaskNeg.bottom());
			gl.glVertex3f(startpoint1.x() + 3 * fthickness, startpoint1.y() - fthickness
					* fScalFactor3, startpoint1.z());
			gl.glTexCoord2f(texCoordsMaskNeg.left(), texCoordsMaskNeg.top());
			gl.glVertex3f(startpoint1.x() + 3 * fthickness, startpoint1.y(),
					startpoint1.z());
			gl.glTexCoord2f(texCoordsMaskNeg.right(), texCoordsMaskNeg.top());
			gl.glVertex3f(startpoint1.x() + 2 * fthickness, startpoint1.y(),
					startpoint1.z());
			gl.glEnd();
		}

		textureMaskNeg.disable();
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glPopAttrib();
	}
}