package sk.freemap.gpxAnimator.ui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import sk.freemap.gpxAnimator.Configuration;
import sk.freemap.gpxAnimator.Renderer;
import sk.freemap.gpxAnimator.TrackConfiguration;
import sk.freemap.gpxAnimator.UserException;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 190371886979948114L;
	
	private final JPanel contentPane;
	private final JSpinner heightSpinner;
	private final FileSelector frameFileNamePatternFileSelector;
	private final JSpinner widthSpinner;
	private final JSpinner zoomSpinner;
	private final JSpinner marginSpinner;
	private final JSpinner speedupSpinner;
	private final JSpinner markerSizeSpinner;
	private final JSpinner waypintSizeSpinner;
	private final JSpinner tailDurationSpinner;
	private final JSpinner fpsSpinner;
	private final JComboBox tmsUrlTemplateComboBox;
	private final JSlider backgroundMapVisibilitySlider;
	private final JSpinner fontSizeSpinner;
	private final JCheckBox keepIdleCheckBox;
	private final ColorSelector flashbackColorSelector;
	private final JSpinner flashbackDurationSpinner;
	private final JSpinner totalTimeSpinner;
	private final JTabbedPane tabbedPane;

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

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final MainFrame frame = new MainFrame();
					frame.setVisible(true);
					frame.setConfiguration(Configuration.createBuilder().build());
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	public Configuration createConfiguration() throws UserException {
		final Configuration.Builder b = Configuration.createBuilder();
		
		b.height((Integer) heightSpinner.getValue());
		b.width((Integer) widthSpinner.getValue());
		b.margin((Integer) marginSpinner.getValue());
		b.zoom((Integer) zoomSpinner.getValue());
		b.speedup((Double) speedupSpinner.getValue());
		b.tailDuration((Long) tailDurationSpinner.getValue());
		b.fps((Double) fpsSpinner.getValue());
		b.totalTime((Long) totalTimeSpinner.getValue());
		b.backgroundMapVisibility(backgroundMapVisibilitySlider.getValue() / 100f);
		b.tmsUrlTemplate((String) tmsUrlTemplateComboBox.getSelectedItem());
		b.skipIdle(!keepIdleCheckBox.isSelected());
		b.flashbackColor(flashbackColorSelector.getColor());
		b.flashbackDuration((Float) flashbackDurationSpinner.getValue());
		b.frameFilePattern(frameFileNamePatternFileSelector.getFilename());
		b.fontSize((Integer) fontSizeSpinner.getValue());
		b.markerSize((Double) markerSizeSpinner.getValue());
		b.waypointSize((Double) waypintSizeSpinner.getValue());
		b.flashbackColor(flashbackColorSelector.getColor());
		b.flashbackDuration((Float) flashbackDurationSpinner.getValue());
		
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
		tmsUrlTemplateComboBox.setSelectedItem(c.getTmsUrlTemplate());
		keepIdleCheckBox.setSelected(!c.isSkipIdle());
		flashbackColorSelector.setColor(c.getFlashbackColor());
		frameFileNamePatternFileSelector.setFilename(c.getFrameFilePattern());
		fontSizeSpinner.setValue(c.getFontSize());
		markerSizeSpinner.setValue(c.getMarkerSize());
		waypintSizeSpinner.setValue(c.getWaypointSize());
		flashbackColorSelector.setColor(c.getFlashbackColor());
		flashbackDurationSpinner.setValue(c.getFlashbackDuration());
		
		// remove all track tabs
		for (int i = tabbedPane.getTabCount() - 1; i > 0; i--) {
			tabbedPane.remove(i);
		}
		
		for (final TrackConfiguration tc : c.getTrackConfigurationList()) {
			addTrackSettingsTab(tc);
		}
	}
	

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(filter);

		setTitle("GPX Animator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 681, 606);
		
		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		final JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		final JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					setConfiguration(Configuration.createBuilder().build());
				} catch (final UserException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		mnFile.add(mntmNew);
		
		final JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					final File file = fileChooser.getSelectedFile();
					try {
						final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
						final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
						setConfiguration((Configuration) unmarshaller.unmarshal(file));
					} catch (final JAXBException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		mnFile.add(mntmOpen);
		
		final JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (!file.getName().endsWith(".ga.xml")) {
						file = new File(file.getPath() + ".ga.xml");
					}
					try {
						final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
						final Marshaller marshaller = jaxbContext.createMarshaller();
						marshaller.marshal(createConfiguration(), file);
					} catch (final JAXBException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (final UserException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		mnFile.add(mntmSave);
		
		final JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				MainFrame.this.dispose();
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
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		final GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{438, 0, 0};
		gbl_contentPane.rowHeights = new int[]{264, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		final GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.gridwidth = 2;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		contentPane.add(tabbedPane, gbc_tabbedPane);
		
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
		
		frameFileNamePatternFileSelector = new FileSelector();
		final GridBagConstraints gbc_frameFileNamePatternFileSelector = new GridBagConstraints();
		gbc_frameFileNamePatternFileSelector.insets = new Insets(0, 0, 5, 0);
		gbc_frameFileNamePatternFileSelector.fill = GridBagConstraints.BOTH;
		gbc_frameFileNamePatternFileSelector.gridx = 1;
		gbc_frameFileNamePatternFileSelector.gridy = 0;
		tabContentPanel.add(frameFileNamePatternFileSelector, gbc_frameFileNamePatternFileSelector);
		
		final JLabel lblWidth = new JLabel("Width");
		final GridBagConstraints gbc_lblWidth = new GridBagConstraints();
		gbc_lblWidth.anchor = GridBagConstraints.EAST;
		gbc_lblWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblWidth.gridx = 0;
		gbc_lblWidth.gridy = 1;
		tabContentPanel.add(lblWidth, gbc_lblWidth);
		
		widthSpinner = new JSpinner();
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
		speedupSpinner.setModel(new EmptyNullSpinnerModel(new Double(0), new Double(0), null, new Double(1)));
		speedupSpinner.setEditor(new EmptyZeroNumberEditor(speedupSpinner, Double.class));
		final GridBagConstraints gbc_sppedupSpinner = new GridBagConstraints();
		gbc_sppedupSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_sppedupSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_sppedupSpinner.gridx = 1;
		gbc_sppedupSpinner.gridy = 5;
		tabContentPanel.add(speedupSpinner, gbc_sppedupSpinner);
		
		final JLabel lblTotalTime = new JLabel("Total Time");
		final GridBagConstraints gbc_lblTotalTime = new GridBagConstraints();
		gbc_lblTotalTime.anchor = GridBagConstraints.EAST;
		gbc_lblTotalTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalTime.gridx = 0;
		gbc_lblTotalTime.gridy = 6;
		tabContentPanel.add(lblTotalTime, gbc_lblTotalTime);
		
		totalTimeSpinner = new JSpinner();
		totalTimeSpinner.setModel(new EmptyNullSpinnerModel(new Long(1), new Long(0), null, new Long(60)));
		totalTimeSpinner.setEditor(new EmptyZeroNumberEditor(totalTimeSpinner, Long.class));
		final GridBagConstraints gbc_totalTimeSpinner = new GridBagConstraints();
		gbc_totalTimeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalTimeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_totalTimeSpinner.gridx = 1;
		gbc_totalTimeSpinner.gridy = 6;
		tabContentPanel.add(totalTimeSpinner, gbc_totalTimeSpinner);
		
		final JLabel lblMarkerSize = new JLabel("Marker Size");
		final GridBagConstraints gbc_lblMarkerSize = new GridBagConstraints();
		gbc_lblMarkerSize.anchor = GridBagConstraints.EAST;
		gbc_lblMarkerSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMarkerSize.gridx = 0;
		gbc_lblMarkerSize.gridy = 7;
		tabContentPanel.add(lblMarkerSize, gbc_lblMarkerSize);
		
		markerSizeSpinner = new JSpinner();
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
		
		waypintSizeSpinner = new JSpinner();
		waypintSizeSpinner.setModel(new SpinnerNumberModel(new Double(1), new Double(1), null, new Double(1)));
		final GridBagConstraints gbc_waypintSizeSpinner = new GridBagConstraints();
		gbc_waypintSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_waypintSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_waypintSizeSpinner.gridx = 1;
		gbc_waypintSizeSpinner.gridy = 8;
		tabContentPanel.add(waypintSizeSpinner, gbc_waypintSizeSpinner);
		
		final JLabel lblTailDuration = new JLabel("Tail Duration");
		final GridBagConstraints gbc_lblTailDuration = new GridBagConstraints();
		gbc_lblTailDuration.insets = new Insets(0, 0, 5, 5);
		gbc_lblTailDuration.anchor = GridBagConstraints.EAST;
		gbc_lblTailDuration.gridx = 0;
		gbc_lblTailDuration.gridy = 9;
		tabContentPanel.add(lblTailDuration, gbc_lblTailDuration);
		
		tailDurationSpinner = new JSpinner();
		tailDurationSpinner.setModel(new SpinnerNumberModel(new Long(0), new Long(0), null, new Long(60)));
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
		fpsSpinner.setModel(new SpinnerNumberModel(new Double(0.1), new Double(0.1), null, new Double(1)));
		final GridBagConstraints gbc_fpsSpinner = new GridBagConstraints();
		gbc_fpsSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fpsSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_fpsSpinner.gridx = 1;
		gbc_fpsSpinner.gridy = 10;
		tabContentPanel.add(fpsSpinner, gbc_fpsSpinner);
		
		final JLabel lblTmsUrlTemplate = new JLabel("TMS URL Template");
		final GridBagConstraints gbc_lblTmsUrlTemplate = new GridBagConstraints();
		gbc_lblTmsUrlTemplate.anchor = GridBagConstraints.EAST;
		gbc_lblTmsUrlTemplate.insets = new Insets(0, 0, 5, 5);
		gbc_lblTmsUrlTemplate.gridx = 0;
		gbc_lblTmsUrlTemplate.gridy = 11;
		tabContentPanel.add(lblTmsUrlTemplate, gbc_lblTmsUrlTemplate);
		
		tmsUrlTemplateComboBox = new JComboBox();
		tmsUrlTemplateComboBox.setEditable(true);
		tmsUrlTemplateComboBox.setModel(new DefaultComboBoxModel(new String[] {"", "http://tile.openstreetmap.org/{zoom}/{x}/{y}.png", "http://t{switch:1,2,3,4}.freemap.sk/T/{zoom}/{x}/{y}.png"}));
		final GridBagConstraints gbc_tmsUrlTemplateComboBox = new GridBagConstraints();
		gbc_tmsUrlTemplateComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_tmsUrlTemplateComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_tmsUrlTemplateComboBox.gridx = 1;
		gbc_tmsUrlTemplateComboBox.gridy = 11;
		tabContentPanel.add(tmsUrlTemplateComboBox, gbc_tmsUrlTemplateComboBox);
		
		final JLabel lblVisibility = new JLabel("Visibility");
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
		fontSizeSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		final GridBagConstraints gbc_fontSizeSpinner = new GridBagConstraints();
		gbc_fontSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_fontSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fontSizeSpinner.gridx = 1;
		gbc_fontSizeSpinner.gridy = 13;
		tabContentPanel.add(fontSizeSpinner, gbc_fontSizeSpinner);
		
		final JLabel lblKeepIdle = new JLabel("Keep Idle");
		final GridBagConstraints gbc_lblKeepIdle = new GridBagConstraints();
		gbc_lblKeepIdle.anchor = GridBagConstraints.EAST;
		gbc_lblKeepIdle.insets = new Insets(0, 0, 5, 5);
		gbc_lblKeepIdle.gridx = 0;
		gbc_lblKeepIdle.gridy = 14;
		tabContentPanel.add(lblKeepIdle, gbc_lblKeepIdle);
		
		keepIdleCheckBox = new JCheckBox("");
		final GridBagConstraints gbc_keepIdleCheckBox = new GridBagConstraints();
		gbc_keepIdleCheckBox.anchor = GridBagConstraints.WEST;
		gbc_keepIdleCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_keepIdleCheckBox.gridx = 1;
		gbc_keepIdleCheckBox.gridy = 14;
		tabContentPanel.add(keepIdleCheckBox, gbc_keepIdleCheckBox);
		
		final JLabel lblFlashbackColor = new JLabel("Flashback Color");
		final GridBagConstraints gbc_lblFlashbackColor = new GridBagConstraints();
		gbc_lblFlashbackColor.anchor = GridBagConstraints.EAST;
		gbc_lblFlashbackColor.insets = new Insets(0, 0, 5, 5);
		gbc_lblFlashbackColor.gridx = 0;
		gbc_lblFlashbackColor.gridy = 15;
		tabContentPanel.add(lblFlashbackColor, gbc_lblFlashbackColor);
		
		flashbackColorSelector = new ColorSelector();
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
		flashbackDurationSpinner.setModel(new EmptyNullSpinnerModel(new Float(0), new Float(0), null, new Float(10)));
		flashbackDurationSpinner.setEditor(new EmptyZeroNumberEditor(flashbackDurationSpinner, Float.class));
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
		
		final JPanel buttonPanel = new JPanel();
		final GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.anchor = GridBagConstraints.EAST;
		gbc_buttonPanel.gridwidth = 2;
		gbc_buttonPanel.fill = GridBagConstraints.VERTICAL;
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 1;
		contentPane.add(buttonPanel, gbc_buttonPanel);
		
		final JButton addTrackButton = new JButton("Add Track");
		addTrackButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					addTrackSettingsTab(TrackConfiguration.createBuilder().build());
				} catch (final UserException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		buttonPanel.add(addTrackButton);
		
		final JButton startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							new Renderer(createConfiguration()).render();
							JOptionPane.showMessageDialog(MainFrame.this, "Success", "Finished", JOptionPane.INFORMATION_MESSAGE);
						} catch (final UserException e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
			}
		});
		buttonPanel.add(startButton);
	}
	

	private void addTrackSettingsTab(final TrackConfiguration tc) {
		final JScrollPane trackScrollPane = new JScrollPane();
		final TrackSettingsPanel trackSettingsPanel = new TrackSettingsPanel(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				tabbedPane.remove(trackScrollPane);
			}
		});
		trackSettingsPanel.setConfiguration(tc);
		
		tabbedPane.addTab("Track", null, trackScrollPane, null);
		trackScrollPane.setViewportView(trackSettingsPanel);
		tabbedPane.setSelectedComponent(trackScrollPane);
	}
	
}
