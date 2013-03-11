/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package sk.freemap.gpxAnimator;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class Map {

	private static final Pattern SWITCH_PATTERN = Pattern.compile("\\{switch:([^}]*)\\}");

	
	public static void drawMap(final BufferedImage bi, final String tmsUrlTemplate, final float backgroundMapVisibility, final int zoom,
			final double minX, final double maxX, final double minY, final double maxY, final RenderingContext rc) throws UserException {
		final Graphics2D ga = (Graphics2D) bi.getGraphics();

		final double tileDblX = xToTileX(zoom, minX);
		final int tileX = (int) Math.floor(tileDblX);
		final int offsetX = (int) Math.floor(256.0 * (tileX - tileDblX));

		final double tileDblY = yToTileY(zoom, minY);
		final int tileY = (int) Math.floor(tileDblY);
		final int offsetY = (int) Math.floor(256.0 * (tileDblY - tileY));

		final int maxXtile = (int) Math.floor(xToTileX(zoom, maxX));
		final int maxYtile = (int) Math.floor(yToTileY(zoom, maxY));
		
		final int total = (maxXtile - tileX + 1) * (tileY - maxYtile + 1);
		int  i = 0;
		
		final Matcher m = SWITCH_PATTERN.matcher(tmsUrlTemplate); // note that only one switch in pattern is supported
		final String[] options = m.find() ? m.group(1).split(",") : null;
		
		for (int x = tileX; x <= maxXtile; x++) {
			for (int y = tileY; y >= maxYtile; y--) {
				if (rc.isCancelled1()) {
					return;
				}
				
				i++;
				
				String url = tmsUrlTemplate
						.replace("{zoom}", Integer.toString(zoom))
						.replace("{x}", Integer.toString(x))
						.replace("{y}", Integer.toString(y));
				
				if (options != null) {
					final StringBuffer sb = new StringBuffer();
					final Matcher matcher = SWITCH_PATTERN.matcher(url);
					if (matcher.find()) {
						matcher.appendReplacement(sb, options[i % options.length]);
					}
					matcher.appendTail(sb);
					url = sb.toString();
				}
				
				rc.setProgress1((int) (100.0 * i / total), "Reading Map Tile: " + i + "/" + total);
				
				final BufferedImage tile;
				try {
					tile = ImageIO.read(new URL(url));
				} catch (final IOException e) {
					throw new UserException("error reading tile " + url);
				}
				
				// convert to RGB format
				final BufferedImage tile1 = new BufferedImage(tile.getWidth(), tile.getHeight(), BufferedImage.TYPE_INT_RGB);
				tile1.getGraphics().drawImage(tile, 0, 0, null);
				
				ga.drawImage(tile1,
						new RescaleOp(backgroundMapVisibility, (1f - backgroundMapVisibility) * 255f, null),
						256 * (x - tileX) + offsetX,
						bi.getHeight() - (256 * (tileY - y) + offsetY));
			}
		}
	}


	private static double yToTileY(final int zoom, final double minY) {
		return latToTileY(zoom, yToLat(minY));
	}


	private static double xToTileX(final int zoom, final double minX) {
		return lonToTileX(zoom, xToLon(minX));
	}


	private static double lonToTileX(final int zoom, final double lon) {
		return (lon + 180.0) / 360.0 * (1 << zoom);
	}


	private static double latToTileY(final int zoom, final double lat) {
		return (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom);
	}

	
	private static double xToLon(final double x) {
		return Math.toDegrees(x);
	}


	private static double yToLat(final double y) {
		return Math.toDegrees(2.0 * (Math.atan(Math.exp(y)) - Math.PI / 4.0));
	}
	
}
