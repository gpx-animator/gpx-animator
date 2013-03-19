package sk.freemap.gpxAnimator;

import java.awt.image.BufferedImage;
import java.io.Closeable;

public interface FrameWriter extends Closeable {

	void addFrame(BufferedImage bi);
	
}
