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

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;

import sk.freemap.gpxAnimator.ui.MainFrame;


public class Main {

	public static void main(final String[] args) {
		try {
			final CommandLineConfigurationFactory cf = new CommandLineConfigurationFactory(args);
			final Configuration configuration = cf.getConfiguration();
			
			if (cf.isGui() && !GraphicsEnvironment.isHeadless()) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							final MainFrame frame = new MainFrame();
							frame.setVisible(true);
							frame.setConfiguration(configuration);
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
				});
			} else {
				new Renderer(configuration).render(new RenderingContext() {
					@Override
					public void setProgress1(final int pct, final String message) {
						System.out.printf("%03d% %s", pct, message);
					}

					@Override
					public boolean isCancelled1() {
						return false;
					}
				});
			}
		} catch (final UserException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

}
