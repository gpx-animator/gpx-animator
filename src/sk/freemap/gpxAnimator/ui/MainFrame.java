package sk.freemap.gpxAnimator.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import sk.freemap.gpxAnimator.Configuration;
import sk.freemap.gpxAnimator.Help;
import sk.freemap.gpxAnimator.Renderer;
import sk.freemap.gpxAnimator.RenderingContext;
import sk.freemap.gpxAnimator.TrackConfiguration;
import sk.freemap.gpxAnimator.UserException;

public class MainFrame extends JFrame {

	private static final String UNSAVED_MSG = "There are unsaved changes. Continue?";

	private static final String TITLE = "GPX Animator 1.0.1";

	private static final long serialVersionUID = 190371886979948114L;
	
	private final JPanel contentPane;
	private final JSpinner heightSpinner;
	private final FileSelector outputFileSelector;
	private final JSpinner widthSpinner;
	private final JSpinner zoomSpinner;
	private final JSpinner marginSpinner;
	private final JSpinner speedupSpinner;
	private final JSpinner markerSizeSpinner;
	private final JSpinner waypointSizeSpinner;
	private final JSpinner tailDurationSpinner;
	private final JSpinner fpsSpinner;
	private final JComboBox tmsUrlTemplateComboBox;
	private final JSlider backgroundMapVisibilitySlider;
	private final JSpinner fontSizeSpinner;
	private final JCheckBox skipIdleCheckBox;
	private final ColorSelector flashbackColorSelector;
	private final JSpinner flashbackDurationSpinner;
	private final JSpinner totalTimeSpinner;
	private final JTabbedPane tabbedPane;
	private final JButton startButton;
	
	private SwingWorker<Void, String> swingWorker;

	
	private final FileFilter filter = new FileFilter() {
		@Override
		public String getDescription() {
			return "GPX Animator Configuration Files";
		}
		
		@Override
		public boolean accept(final File f) {
			return f.isDirectory() || f.getName().endsWith("ga.xml");
		}
	};
	
	private final JFileChooser fileChooser = new JFileChooser();

	private List<LabeledItem> mapTamplateList;

	private File file;

	private boolean changed;
	
	
	private final ChangeListener changeListener = new ChangeListener() {
		@Override
		public void stateChanged(final ChangeEvent e) {
			changed(true);
		}
	};
	
	private final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			changed(true);
		}
	};
	
	
	public Configuration createConfiguration() throws UserException {
		final Configuration.Builder b = Configuration.createBuilder();
		
		b.height((Integer) heightSpinner.getValue());
		b.width((Integer) widthSpinner.getValue());
		b.margin((Integer) marginSpinner.getValue());
		b.zoom((Integer) zoomSpinner.getValue());
		b.speedup((Double) speedupSpinner.getValue());
		final Long td = (Long) tailDurationSpinner.getValue();
		b.tailDuration(td == null ? 0l : td.longValue());
		b.fps((Double) fpsSpinner.getValue());
		b.totalTime((Long) totalTimeSpinner.getValue());
		b.backgroundMapVisibility(backgroundMapVisibilitySlider.getValue() / 100f);
		final Object tmsItem = tmsUrlTemplateComboBox.getSelectedItem();
		final String tmsUrlTemplate = tmsItem instanceof LabeledItem ? ((LabeledItem) tmsItem).getValue() : (String) tmsItem;
		b.tmsUrlTemplate(tmsUrlTemplate == null || tmsUrlTemplate.isEmpty() ? null : tmsUrlTemplate);
		b.skipIdle(skipIdleCheckBox.isSelected());
		b.flashbackColor(flashbackColorSelector.getColor());
		b.flashbackDuration((Long) flashbackDurationSpinner.getValue());
		b.output(outputFileSelector.getFilename());
		b.fontSize((Integer) fontSizeSpinner.getValue());
		b.markerSize((Double) markerSizeSpinner.getValue());
		b.waypointSize((Double) waypointSizeSpinner.getValue());
		
		for (int i = 1, n = tabbedPane.getTabCount(); i < n; i++) {
			final TrackSettingsPanel tsp = (TrackSettingsPanel) ((JScrollPane) tabbedPane.getComponentAt(i)).getViewport().getView();
			b.addTrackConfiguration(tsp.createConfiguration());
		}
		
		return b.build();
	}
	
	
	public void setConfiguration(final Configuration c) {
		heightSpinner.setValue(c.getHeight());
		widthSpinner.setValue(c.getWidth());
		marginSpinner.setValue(c.getMargin());
		zoomSpinner.setValue(c.getZoom());
		speedupSpinner.setValue(c.getSpeedup());
		tailDurationSpinner.setValue(c.getTailDuration());
		fpsSpinner.setValue(c.getFps());
		totalTimeSpinner.setValue(c.getTotalTime());
		backgroundMapVisibilitySlider.setValue((int) (c.getBackgroundMapVisibility() * 100));

		final String tmsUrlTemplate = c.getTmsUrlTemplate();
		found: {
			for (final LabeledItem labeledItem : mapTamplateList) {
				if (labeledItem.getValue().equals(tmsUrlTemplate)) {
					tmsUrlTemplateComboBox.setSelectedItem(labeledItem);
					break found;
				}
			}
			tmsUrlTemplateComboBox.setSelectedItem(tmsUrlTemplate);
		}
		
		skipIdleCheckBox.setSelected(c.isSkipIdle());
		flashbackColorSelector.setColor(c.getFlashbackColor());
		outputFileSelector.setFilename(c.getOutput());
		fontSizeSpinner.setValue(c.getFontSize());
		markerSizeSpinner.setValue(c.getMarkerSize());
		waypointSizeSpinner.setValue(c.getWaypointSize());
		flashbackColorSelector.setColor(c.getFlashbackColor());
		flashbackDurationSpinner.setValue(c.getFlashbackDuration());
		
		// remove all track tabs
		for (int i = tabbedPane.getTabCount() - 1; i > 0; i--) {
			tabbedPane.remove(i);
		}
		afterRemove();
		
		for (final TrackConfiguration tc : c.getTrackConfigurationList()) {
			addTrackSettingsTab(tc);
		}
		
		changed(false);
	}
	

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		try {
			mapTamplateList = readMaps();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(filter);

		setTitle(TITLE);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 681, 606);
		
		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		final JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		final JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (!changed || JOptionPane.showConfirmDialog(MainFrame.this, UNSAVED_MSG, "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					try {
						setConfiguration(Configuration.createBuilder().build());
					} catch (final UserException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		});
		mnFile.add(mntmNew);
		
		final JMenuItem mntmOpen = new JMenuItem("Open...");
		mntmOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (!changed || JOptionPane.showConfirmDialog(MainFrame.this, UNSAVED_MSG, "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					
					if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						final File file = fileChooser.getSelectedFile();
						try {
							final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
							final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
							setConfiguration((Configuration) unmarshaller.unmarshal(file));
							MainFrame.this.file = file;
						} catch (final JAXBException e1) {
							JOptionPane.showMessageDialog(MainFrame.this, "Error opening configuration: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				
			}
		});
		mnFile.add(mntmOpen);
		
		final JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (file == null) {
					saveAs();
				} else {
					save(file);
				}
			}
		});
		mnFile.add(mntmSave);
		
		final JMenuItem mntmSaveAs = new JMenuItem("Save As...");
		mntmSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				saveAs();
			}
		});
		mnFile.add(mntmSaveAs);
		
		final JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (!changed || JOptionPane.showConfirmDialog(MainFrame.this, UNSAVED_MSG, "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					MainFrame.this.dispose();
				}
			}
		});
		mnFile.add(mntmExit);
		
		final JMenu mnTrack = new JMenu("Track");
		menuBar.add(mnTrack);
		
		final JMenuItem mntmAddTrack = new JMenuItem("Add");
		mntmAddTrack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					addTrackSettingsTab(TrackConfiguration.createBuilder().build());
				} catch (final UserException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
		mnTrack.add(mntmAddTrack);
		
		final JMenuItem mntmRemoveTrack = new JMenuItem("Remove");
		mntmRemoveTrack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int index = tabbedPane.getSelectedIndex();
				if (index > 0) {
					tabbedPane.remove(index);
					afterRemove();
				}
			}
		});
		mnTrack.add(mntmRemoveTrack);
		
		final JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		final JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final AboutDialog aboutDialog = new AboutDialog();
				aboutDialog.setLocationRelativeTo(MainFrame.this);
				aboutDialog.setVisible(true);
			}
		});
		mnHelp.add(mntmAbout);
		
		final JMenuItem mntmUsage = new JMenuItem("Usage");
		mntmUsage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final UsageDialog usageDialog = new UsageDialog();
				usageDialog.setLocationRelativeTo(MainFrame.this);
				usageDialog.setVisible(true);
			}
		});
		mnHelp.add(mntmUsage);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		final GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{438, 0};
		gbl_contentPane.rowHeights = new int[]{264, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		final GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		contentPane.add(tabbedPane, gbc_tabbedPane);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				mntmRemoveTrack.setEnabled(tabbedPane.getSelectedIndex() > 0);
			}
		});
		
		final JScrollPane generalScrollPane = new JScrollPane();
		tabbedPane.addTab("General", null, generalScrollPane, null);
		
		final JPanel tabContentPanel = new JPanel();
		tabContentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		generalScrollPane.setViewportView(tabContentPanel);
//		tabbedPane.addTab("General", null, panel, null);
		final GridBagLayout gbl_tabContentPanel = new GridBagLayout();
		gbl_tabContentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_tabContentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_tabContentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_tabContentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		tabContentPanel.setLayout(gbl_tabContentPanel);
		
		final JLabel lblOutput = new JLabel("Output");
		final GridBagConstraints gbc_lblOutput = new GridBagConstraints();
		gbc_lblOutput.anchor = GridBagConstraints.EAST;
		gbc_lblOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutput.gridx = 0;
		gbc_lblOutput.gridy = 0;
		tabContentPanel.add(lblOutput, gbc_lblOutput);
		
		outputFileSelector = new FileSelector() {
			private static final long serialVersionUID = 7372002778976603239L;

			@Override
			protected Type configure(final JFileChooser gpxFileChooser) {
				gpxFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Image Frames", "jpg"));
				gpxFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG Image Frames", "png"));
				gpxFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("H.264 Encoded Video Files (*.mp4, *.mov, *.mkv)", "mp4", "mov", "mkv"));
				gpxFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("MPEG-1 Encoded Video Files (*.mpg)", "mpg"));
				gpxFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("MPEG-4 Encoded Video Files (*.avi)", "avi"));
				gpxFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("MS MPEG-4 Encoded Video Files (*.wmv, *.asf)", "wmv", "asf"));
				gpxFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Theora Encoded Video Files (*.ogv)", "ogv"));
				gpxFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FLV Encoded Video Files (*.flv)", "flv"));
				gpxFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("RV10 Encoded Video Files (*.rm)", "rm"));
				return Type.SAVE;
			}
			
			@Override
			protected String transformFilename(final String filename) {
				if ((filename.toLowerCase().endsWith(".png") || filename.toLowerCase().endsWith(".jpg"))
						&& String.format(filename, 100).equals(String.format(filename, 200))) {
					final int n = filename.lastIndexOf('.');
					return filename.substring(0, n) + "%08d" + filename.substring(n);
				} else {
					return filename;
				}
			}
		};
		
		outputFileSelector.setToolTipText(Help.HELP_OUTPUT);
		
		final GridBagConstraints gbc_frameFileNamePatternFileSelector = new GridBagConstraints();
		gbc_frameFileNamePatternFileSelector.insets = new Insets(0, 0, 5, 0);
		gbc_frameFileNamePatternFileSelector.fill = GridBagConstraints.BOTH;
		gbc_frameFileNamePatternFileSelector.gridx = 1;
		gbc_frameFileNamePatternFileSelector.gridy = 0;
		tabContentPanel.add(outputFileSelector, gbc_frameFileNamePatternFileSelector);
		
		final JLabel lblWidth = new JLabel("Width");
		final GridBagConstraints gbc_lblWidth = new GridBagConstraints();
		gbc_lblWidth.anchor = GridBagConstraints.EAST;
		gbc_lblWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblWidth.gridx = 0;
		gbc_lblWidth.gridy = 1;
		tabContentPanel.add(lblWidth, gbc_lblWidth);
		
		widthSpinner = new JSpinner();
		widthSpinner.setToolTipText(Help.HELP_WIDTH);
		widthSpinner.setModel(new EmptyNullSpinnerModel(new Integer(1), new Integer(0), null, new Integer(10)));
		widthSpinner.setEditor(new EmptyZeroNumberEditor(widthSpinner, Integer.class));
		final GridBagConstraints gbc_widthSpinner = new GridBagConstraints();
		gbc_widthSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_widthSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_widthSpinner.gridx = 1;
		gbc_widthSpinner.gridy = 1;
		tabContentPanel.add(widthSpinner, gbc_widthSpinner);
		
		final JLabel lblHeight = new JLabel("Height");
		final GridBagConstraints gbc_lblHeight = new GridBagConstraints();
		gbc_lblHeight.anchor = GridBagConstraints.EAST;
		gbc_lblHeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeight.gridx = 0;
		gbc_lblHeight.gridy = 2;
		tabContentPanel.add(lblHeight, gbc_lblHeight);
		
		heightSpinner = new JSpinner();
		heightSpinner.setToolTipText(Help.HELP_HEIGHT);
		heightSpinner.setModel(new EmptyNullSpinnerModel(new Integer(1), new Integer(0), null, new Integer(10)));
		heightSpinner.setEditor(new EmptyZeroNumberEditor(heightSpinner, Integer.class));
		final GridBagConstraints gbc_heightSpinner = new GridBagConstraints();
		gbc_heightSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_heightSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_heightSpinner.gridx = 1;
		gbc_heightSpinner.gridy = 2;
		tabContentPanel.add(heightSpinner, gbc_heightSpinner);
		
		final JLabel lblZoom = new JLabel("Zoom");
		final GridBagConstraints gbc_lblZoom = new GridBagConstraints();
		gbc_lblZoom.anchor = GridBagConstraints.EAST;
		gbc_lblZoom.insets = new Insets(0, 0, 5, 5);
		gbc_lblZoom.gridx = 0;
		gbc_lblZoom.gridy = 3;
		tabContentPanel.add(lblZoom, gbc_lblZoom);
		
		zoomSpinner = new JSpinner();
		zoomSpinner.setToolTipText(Help.HELP_ZOOM);
		zoomSpinner.setModel(new EmptyNullSpinnerModel(new Integer(1), new Integer(0), new Integer(18), new Integer(1)));
		zoomSpinner.setEditor(new EmptyZeroNumberEditor(zoomSpinner, Integer.class));

		final GridBagConstraints gbc_zoomSpinner = new GridBagConstraints();
		gbc_zoomSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_zoomSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_zoomSpinner.gridx = 1;
		gbc_zoomSpinner.gridy = 3;
		tabContentPanel.add(zoomSpinner, gbc_zoomSpinner);
		
		final JLabel lblMargin = new JLabel("Margin");
		final GridBagConstraints gbc_lblMargin = new GridBagConstraints();
		gbc_lblMargin.anchor = GridBagConstraints.EAST;
		gbc_lblMargin.insets = new Insets(0, 0, 5, 5);
		gbc_lblMargin.gridx = 0;
		gbc_lblMargin.gridy = 4;
		tabContentPanel.add(lblMargin, gbc_lblMargin);
		
		marginSpinner = new JSpinner();
		marginSpinner.setToolTipText(Help.HELP_MARGIN);
		marginSpinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		final GridBagConstraints gbc_marginSpinner = new GridBagConstraints();
		gbc_marginSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_marginSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_marginSpinner.gridx = 1;
		gbc_marginSpinner.gridy = 4;
		tabContentPanel.add(marginSpinner, gbc_marginSpinner);
		
		final JLabel lblSpeedup = new JLabel("Speedup");
		final GridBagConstraints gbc_lblSpeedup = new GridBagConstraints();
		gbc_lblSpeedup.anchor = GridBagConstraints.EAST;
		gbc_lblSpeedup.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpeedup.gridx = 0;
		gbc_lblSpeedup.gridy = 5;
		tabContentPanel.add(lblSpeedup, gbc_lblSpeedup);
		
		speedupSpinner = new JSpinner();
		speedupSpinner.setToolTipText(Help.HELP_SPEEDUP);
		speedupSpinner.setModel(new EmptyNullSpinnerModel(new Double(0), new Double(0), null, new Double(1)));
		speedupSpinner.setEditor(new EmptyZeroNumberEditor(speedupSpinner, Double.class));
		final GridBagConstraints gbc_sppedupSpinner = new GridBagConstraints();
		gbc_sppedupSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_sppedupSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_sppedupSpinner.gridx = 1;
		gbc_sppedupSpinner.gridy = 5;
		tabContentPanel.add(speedupSpinner, gbc_sppedupSpinner);
		
		speedupSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				final boolean enabled = speedupSpinner.getValue() == null;
				if (!enabled) {
					totalTimeSpinner.setValue(null);
				}
				totalTimeSpinner.setEnabled(enabled);
			}
		});
		
		final JLabel lblTotalTime = new JLabel("Total Time");
		final GridBagConstraints gbc_lblTotalTime = new GridBagConstraints();
		gbc_lblTotalTime.anchor = GridBagConstraints.EAST;
		gbc_lblTotalTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalTime.gridx = 0;
		gbc_lblTotalTime.gridy = 6;
		tabContentPanel.add(lblTotalTime, gbc_lblTotalTime);
		
		totalTimeSpinner = new JSpinner();
		totalTimeSpinner.setToolTipText(Help.HELP_TOTAL_LENGTH);
		totalTimeSpinner.setModel(new DurationSpinnerModel());
		totalTimeSpinner.setEditor(new DurationEditor(totalTimeSpinner));
		final GridBagConstraints gbc_totalTimeSpinner = new GridBagConstraints();
		gbc_totalTimeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalTimeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_totalTimeSpinner.gridx = 1;
		gbc_totalTimeSpinner.gridy = 6;
		tabContentPanel.add(totalTimeSpinner, gbc_totalTimeSpinner);
		
		totalTimeSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				final boolean enabled = totalTimeSpinner.getValue() == null;
				if (!enabled) {
					speedupSpinner.setValue(null);
				}
				speedupSpinner.setEnabled(enabled);
			}
		});
		
		final JLabel lblMarkerSize = new JLabel("Marker Size");
		final GridBagConstraints gbc_lblMarkerSize = new GridBagConstraints();
		gbc_lblMarkerSize.anchor = GridBagConstraints.EAST;
		gbc_lblMarkerSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMarkerSize.gridx = 0;
		gbc_lblMarkerSize.gridy = 7;
		tabContentPanel.add(lblMarkerSize, gbc_lblMarkerSize);
		
		markerSizeSpinner = new JSpinner();
		markerSizeSpinner.setToolTipText(Help.HELP_MARKER_SIZE);
		markerSizeSpinner.setModel(new SpinnerNumberModel(new Double(1), new Double(1), null, new Double(1)));
		final GridBagConstraints gbc_markerSizeSpinner = new GridBagConstraints();
		gbc_markerSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_markerSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_markerSizeSpinner.gridx = 1;
		gbc_markerSizeSpinner.gridy = 7;
		tabContentPanel.add(markerSizeSpinner, gbc_markerSizeSpinner);
		
		final JLabel lblWaypointSize = new JLabel("Waypoint Size");
		final GridBagConstraints gbc_lblWaypointSize = new GridBagConstraints();
		gbc_lblWaypointSize.anchor = GridBagConstraints.EAST;
		gbc_lblWaypointSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblWaypointSize.gridx = 0;
		gbc_lblWaypointSize.gridy = 8;
		tabContentPanel.add(lblWaypointSize, gbc_lblWaypointSize);
		
		waypointSizeSpinner = new JSpinner();
		waypointSizeSpinner.setToolTipText(Help.HELP_WAYPOINT_SIZE);
		waypointSizeSpinner.setModel(new SpinnerNumberModel(new Double(1), new Double(1), null, new Double(1)));
		final GridBagConstraints gbc_waypintSizeSpinner = new GridBagConstraints();
		gbc_waypintSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_waypintSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_waypintSizeSpinner.gridx = 1;
		gbc_waypintSizeSpinner.gridy = 8;
		tabContentPanel.add(waypointSizeSpinner, gbc_waypintSizeSpinner);
		
		final JLabel lblTailDuration = new JLabel("Tail Duration");
		final GridBagConstraints gbc_lblTailDuration = new GridBagConstraints();
		gbc_lblTailDuration.insets = new Insets(0, 0, 5, 5);
		gbc_lblTailDuration.anchor = GridBagConstraints.EAST;
		gbc_lblTailDuration.gridx = 0;
		gbc_lblTailDuration.gridy = 9;
		tabContentPanel.add(lblTailDuration, gbc_lblTailDuration);
		
		tailDurationSpinner = new JSpinner();
		tailDurationSpinner.setToolTipText(Help.HELP_TAIL_DURATION);
		tailDurationSpinner.setModel(new DurationSpinnerModel());
		tailDurationSpinner.setEditor(new DurationEditor(tailDurationSpinner));
		final GridBagConstraints gbc_tailDurationSpinner = new GridBagConstraints();
		gbc_tailDurationSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_tailDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_tailDurationSpinner.gridx = 1;
		gbc_tailDurationSpinner.gridy = 9;
		tabContentPanel.add(tailDurationSpinner, gbc_tailDurationSpinner);
		
		final JLabel lblFps = new JLabel("FPS");
		final GridBagConstraints gbc_lblFps = new GridBagConstraints();
		gbc_lblFps.anchor = GridBagConstraints.EAST;
		gbc_lblFps.insets = new Insets(0, 0, 5, 5);
		gbc_lblFps.gridx = 0;
		gbc_lblFps.gridy = 10;
		tabContentPanel.add(lblFps, gbc_lblFps);
		
		fpsSpinner = new JSpinner();
		fpsSpinner.setToolTipText(Help.HELP_FPS);
		fpsSpinner.setModel(new SpinnerNumberModel(new Double(0.1), new Double(0.1), null, new Double(1)));
		final GridBagConstraints gbc_fpsSpinner = new GridBagConstraints();
		gbc_fpsSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fpsSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_fpsSpinner.gridx = 1;
		gbc_fpsSpinner.gridy = 10;
		tabContentPanel.add(fpsSpinner, gbc_fpsSpinner);
		
		final JLabel lblTmsUrlTemplate = new JLabel("Background Map");
		final GridBagConstraints gbc_lblTmsUrlTemplate = new GridBagConstraints();
		gbc_lblTmsUrlTemplate.anchor = GridBagConstraints.EAST;
		gbc_lblTmsUrlTemplate.insets = new Insets(0, 0, 5, 5);
		gbc_lblTmsUrlTemplate.gridx = 0;
		gbc_lblTmsUrlTemplate.gridy = 11;
		tabContentPanel.add(lblTmsUrlTemplate, gbc_lblTmsUrlTemplate);
		
		tmsUrlTemplateComboBox = new JComboBox();
		tmsUrlTemplateComboBox.setToolTipText(Help.HELP_TMS_URL_TEMPLATE);
		tmsUrlTemplateComboBox.setEditable(true);
		tmsUrlTemplateComboBox.setModel(new DefaultComboBoxModel(mapTamplateList.toArray()));
		final GridBagConstraints gbc_tmsUrlTemplateComboBox = new GridBagConstraints();
		gbc_tmsUrlTemplateComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_tmsUrlTemplateComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_tmsUrlTemplateComboBox.gridx = 1;
		gbc_tmsUrlTemplateComboBox.gridy = 11;
		tabContentPanel.add(tmsUrlTemplateComboBox, gbc_tmsUrlTemplateComboBox);
		
		final JLabel lblVisibility = new JLabel("Map Visibility");
		final GridBagConstraints gbc_lblVisibility = new GridBagConstraints();
		gbc_lblVisibility.anchor = GridBagConstraints.EAST;
		gbc_lblVisibility.insets = new Insets(0, 0, 5, 5);
		gbc_lblVisibility.gridx = 0;
		gbc_lblVisibility.gridy = 12;
		tabContentPanel.add(lblVisibility, gbc_lblVisibility);
		
		backgroundMapVisibilitySlider = new JSlider();
		backgroundMapVisibilitySlider.setMinorTickSpacing(5);
		backgroundMapVisibilitySlider.setPaintTicks(true);
		backgroundMapVisibilitySlider.setMajorTickSpacing(10);
		backgroundMapVisibilitySlider.setPaintLabels(true);
		backgroundMapVisibilitySlider.setToolTipText(Help.HELP_BG_MAP_VISIBILITY);
		final GridBagConstraints gbc_backgroundMapVisibilitySlider = new GridBagConstraints();
		gbc_backgroundMapVisibilitySlider.insets = new Insets(0, 0, 5, 0);
		gbc_backgroundMapVisibilitySlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_backgroundMapVisibilitySlider.gridx = 1;
		gbc_backgroundMapVisibilitySlider.gridy = 12;
		tabContentPanel.add(backgroundMapVisibilitySlider, gbc_backgroundMapVisibilitySlider);
		
		final JLabel lblFontSize = new JLabel("Font Size");
		final GridBagConstraints gbc_lblFontSize = new GridBagConstraints();
		gbc_lblFontSize.anchor = GridBagConstraints.EAST;
		gbc_lblFontSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblFontSize.gridx = 0;
		gbc_lblFontSize.gridy = 13;
		tabContentPanel.add(lblFontSize, gbc_lblFontSize);
		
		fontSizeSpinner = new JSpinner();
		fontSizeSpinner.setToolTipText(Help.HELP_FONT_SIZE);
		fontSizeSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		final GridBagConstraints gbc_fontSizeSpinner = new GridBagConstraints();
		gbc_fontSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_fontSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fontSizeSpinner.gridx = 1;
		gbc_fontSizeSpinner.gridy = 13;
		tabContentPanel.add(fontSizeSpinner, gbc_fontSizeSpinner);
		
		final JLabel lblSkipIdle = new JLabel("Skip Idle");
		final GridBagConstraints gbc_lblSkipIdle = new GridBagConstraints();
		gbc_lblSkipIdle.anchor = GridBagConstraints.EAST;
		gbc_lblSkipIdle.insets = new Insets(0, 0, 5, 5);
		gbc_lblSkipIdle.gridx = 0;
		gbc_lblSkipIdle.gridy = 14;
		tabContentPanel.add(lblSkipIdle, gbc_lblSkipIdle);
		
		skipIdleCheckBox = new JCheckBox("");
		skipIdleCheckBox.setToolTipText(Help.HELP_SKIP_IDLE);
		final GridBagConstraints gbc_keepIdleCheckBox = new GridBagConstraints();
		gbc_keepIdleCheckBox.anchor = GridBagConstraints.WEST;
		gbc_keepIdleCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_keepIdleCheckBox.gridx = 1;
		gbc_keepIdleCheckBox.gridy = 14;
		tabContentPanel.add(skipIdleCheckBox, gbc_keepIdleCheckBox);
		
		final JLabel lblFlashbackColor = new JLabel("Flashback Color");
		final GridBagConstraints gbc_lblFlashbackColor = new GridBagConstraints();
		gbc_lblFlashbackColor.anchor = GridBagConstraints.EAST;
		gbc_lblFlashbackColor.insets = new Insets(0, 0, 5, 5);
		gbc_lblFlashbackColor.gridx = 0;
		gbc_lblFlashbackColor.gridy = 15;
		tabContentPanel.add(lblFlashbackColor, gbc_lblFlashbackColor);
		
		flashbackColorSelector = new ColorSelector();
		flashbackColorSelector.setToolTipText(Help.HELP_FLASHBACK_COLOR);
		final GridBagConstraints gbc_flashbackColorSelector = new GridBagConstraints();
		gbc_flashbackColorSelector.insets = new Insets(0, 0, 5, 0);
		gbc_flashbackColorSelector.fill = GridBagConstraints.BOTH;
		gbc_flashbackColorSelector.gridx = 1;
		gbc_flashbackColorSelector.gridy = 15;
		tabContentPanel.add(flashbackColorSelector, gbc_flashbackColorSelector);
		
		final JLabel lblFlashbackDuration = new JLabel("Flashback Duration");
		final GridBagConstraints gbc_lblFlashbackDuration = new GridBagConstraints();
		gbc_lblFlashbackDuration.anchor = GridBagConstraints.EAST;
		gbc_lblFlashbackDuration.insets = new Insets(0, 0, 0, 5);
		gbc_lblFlashbackDuration.gridx = 0;
		gbc_lblFlashbackDuration.gridy = 16;
		tabContentPanel.add(lblFlashbackDuration, gbc_lblFlashbackDuration);
		
		flashbackDurationSpinner = new JSpinner();
		flashbackDurationSpinner.setToolTipText(Help.HELP_FLASHBACK_DURATION);
		flashbackDurationSpinner.setModel(new DurationSpinnerModel());
		flashbackDurationSpinner.setEditor(new DurationEditor(flashbackDurationSpinner));
		final GridBagConstraints gbc_flashbackDurationSpinner = new GridBagConstraints();
		gbc_flashbackDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_flashbackDurationSpinner.gridx = 1;
		gbc_flashbackDurationSpinner.gridy = 16;
		tabContentPanel.add(flashbackDurationSpinner, gbc_flashbackDurationSpinner);
		
		final GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.anchor = GridBagConstraints.EAST;
		gbc_button.gridwidth = 3;
		gbc_button.gridx = 0;
		gbc_button.gridy = 7;
		
		final JPanel panel = new JPanel();
		final GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		contentPane.add(panel, gbc_panel);
		final GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{174, 49, 32, 0};
		gbl_panel.rowHeights = new int[]{27, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		final GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.insets = new Insets(0, 0, 0, 5);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 0;
		panel.add(progressBar, gbc_progressBar);
		
		final JButton addTrackButton = new JButton("Add Track");
		final GridBagConstraints gbc_addTrackButton = new GridBagConstraints();
		gbc_addTrackButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_addTrackButton.insets = new Insets(0, 0, 0, 5);
		gbc_addTrackButton.gridx = 1;
		gbc_addTrackButton.gridy = 0;
		panel.add(addTrackButton, gbc_addTrackButton);
		addTrackButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					addTrackSettingsTab(TrackConfiguration.createBuilder().build());
				} catch (final UserException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
		
		startButton = new JButton("Start");
		startButton.setEnabled(false);
		final GridBagConstraints gbc_startButton = new GridBagConstraints();
		gbc_startButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_startButton.gridx = 2;
		gbc_startButton.gridy = 0;
		panel.add(startButton, gbc_startButton);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (swingWorker != null) {
					swingWorker.cancel(false);
					return;
				}
				
				swingWorker = new SwingWorker<Void, String>() {
					@Override
					protected Void doInBackground() throws Exception {
						new Renderer(createConfiguration()).render(new RenderingContext() {
							@Override
							public void setProgress1(final int pct, final String message) {
								System.out.printf("[%3d%%] %s\n", pct, message);
								setProgress(pct);
								publish(message + " (" + pct + "%)");
							}

							@Override
							public boolean isCancelled1() {
								return isCancelled();
							}
						});

						return null;
					}
					
					@Override
					protected void process(final List<String> chunks) {
						if (!chunks.isEmpty()) {
							progressBar.setString(chunks.get(chunks.size() - 1));
						}
					}
					
					@Override
					protected void done() {
						swingWorker = null;
						progressBar.setVisible(false);
						startButton.setText("Start");

						try {
							get();
							JOptionPane.showMessageDialog(MainFrame.this, "Rendering has finished successfully.", "Finished", JOptionPane.INFORMATION_MESSAGE);
						} catch (final InterruptedException e) {
							JOptionPane.showMessageDialog(MainFrame.this, "Rendering has been interrupted.", "Interrupted", JOptionPane.ERROR_MESSAGE);
						} catch (final ExecutionException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(MainFrame.this, "Error while rendering:\n" + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						} catch (final CancellationException e) {
							JOptionPane.showMessageDialog(MainFrame.this, "Rendering has been cancelled.", "Cancelled", JOptionPane.WARNING_MESSAGE);
						}
					}
				};
				
				swingWorker.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if ("progress".equals(evt.getPropertyName())) {
							progressBar.setValue((Integer) evt.getNewValue());
						}
					}
				});
				
				progressBar.setVisible(true);
				startButton.setText("Cancel");
				swingWorker.execute();
			}
		});
		
		outputFileSelector.addPropertyChangeListener("filename", propertyChangeListener);
		widthSpinner.addChangeListener(changeListener);
		heightSpinner.addChangeListener(changeListener);
		zoomSpinner.addChangeListener(changeListener);
		marginSpinner.addChangeListener(changeListener);
		speedupSpinner.addChangeListener(changeListener);
		totalTimeSpinner.addChangeListener(changeListener);
		markerSizeSpinner.addChangeListener(changeListener);
		waypointSizeSpinner.addChangeListener(changeListener);
		tailDurationSpinner.addChangeListener(changeListener);
		fpsSpinner.addChangeListener(changeListener);
//		tmsUrlTemplateComboBox.addChangeListener(listener);
		backgroundMapVisibilitySlider.addChangeListener(changeListener);
		fontSizeSpinner.addChangeListener(changeListener);
		skipIdleCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				changed(true);
			}
		});
		flashbackColorSelector.addPropertyChangeListener("color", propertyChangeListener);
		flashbackDurationSpinner.addChangeListener(changeListener);
		
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				if (!changed || JOptionPane.showConfirmDialog(MainFrame.this,
						"There are unsaved changes. Close anyway?", "Error", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
				
				if (swingWorker != null && !swingWorker.isDone()) {
					swingWorker.cancel(false);
				}
			}
		});
	}
	
	
	private void changed(final boolean changed) {
		this.changed = changed;
		setTitle(TITLE + (changed ? " (*)" : ""));
	}
	
	
	private List<LabeledItem> readMaps() throws IOException {
		final List<LabeledItem> labeledItems = new ArrayList<LabeledItem>();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(MainFrame.class.getResourceAsStream("maps")));
		String line;
		String label = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#") || line.trim().isEmpty()) {
				// nothing
			} else if (label == null) {
				label = line;
			} else {
				labeledItems.add(new LabeledItem(label, line));
				label = null;
			}
		}
		reader.close();
		
		Collections.sort(labeledItems, new Comparator<LabeledItem>() {
			@Override
			public int compare(final LabeledItem o1, final LabeledItem o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		return labeledItems;
	}
	

	private void addTrackSettingsTab(final TrackConfiguration tc) {
		final JScrollPane trackScrollPane = new JScrollPane();
		final TrackSettingsPanel trackSettingsPanel = new TrackSettingsPanel() {
			private static final long serialVersionUID = 308660875202822183L;

			@Override
			protected void remove() {
				tabbedPane.remove(trackScrollPane);
				afterRemove();
			}

			@Override
			protected void configurationChanged() {
				changed(true);
			}
		};
		
		trackSettingsPanel.setConfiguration(tc);
		
		tabbedPane.addTab("Track", null, trackScrollPane, null);
		trackScrollPane.setViewportView(trackSettingsPanel);
		tabbedPane.setSelectedComponent(trackScrollPane);
		
		trackSettingsPanel.addPropertyChangeListener("trackConfiguration", propertyChangeListener);
		
		startButton.setEnabled(true);
		
		changed(true);
	}

	
	private void afterRemove() {
		if (tabbedPane.getTabCount() == 1) {
			startButton.setEnabled(false);
		}
		changed(true);
	}

	
	private void saveAs() {
		if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (!file.getName().endsWith(".ga.xml")) {
				file = new File(file.getPath() + ".ga.xml");
			}
			save(file);
		}
	}


	private void save(final File file) {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
			final Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(createConfiguration(), file);
			MainFrame.this.file = file;
			changed(false);
		} catch (final JAXBException e) {
			JOptionPane.showMessageDialog(this, "Error saving configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (final UserException e) {
			JOptionPane.showMessageDialog(this, "Error saving configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
