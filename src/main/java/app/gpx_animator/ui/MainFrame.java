package app.gpx_animator.ui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.gpx_animator.Configuration;
import app.gpx_animator.Constants;
import app.gpx_animator.FileXmlAdapter;
import app.gpx_animator.Preferences;
import app.gpx_animator.Renderer;
import app.gpx_animator.RenderingContext;
import app.gpx_animator.TrackConfiguration;
import app.gpx_animator.UserException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static app.gpx_animator.Utils.isEqual;

public final class MainFrame extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainFrame.class);

    private static final String PROJECT_FILENAME_SUFFIX = ".ga.xml"; //NON-NLS
    private static final long serialVersionUID = 190371886979948114L;
    private static final int FIXED_TABS = 1;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();
    private final transient String unsavedMessage = resourceBundle.getString("ui.mainframe.dialog.message.unsaved.continue");
    private final transient String warningTitle = resourceBundle.getString("ui.mainframe.dialog.title.warning");
    private final transient String errorTitle = resourceBundle.getString("ui.mainframe.dialog.title.error");

    private final transient Random random = new Random();
    private final transient File defaultConfigFile = new File(Preferences.getConfigurationDir()
            + Preferences.FILE_SEPARATOR + "defaultConfig.ga.xml");
    private final transient JTabbedPane tabbedPane;
    private final transient JButton renderButton;
    private final transient JMenu openRecent;
    private final transient JFileChooser fileChooser = new JFileChooser();
    private final transient GeneralSettingsPanel generalSettingsPanel;

    private transient SwingWorker<Void, String> swingWorker;
    private transient File file;
    private transient boolean changed;

    @SuppressWarnings("checkstyle:MethodLength") // TODO Refactor when doing the redesign task https://github.com/zdila/gpx-animator/issues/60
    public MainFrame() {
        final ActionListener addTrackActionListener = new ActionListener() {
            private float hue = random.nextFloat();

            @Override
            public void actionPerformed(final ActionEvent e) {

                final JFileChooser gpxFileChooser = new JFileChooser();
                TrackSettingsPanel.configureGpxFileChooser(resourceBundle, gpxFileChooser);
                gpxFileChooser.setMultiSelectionEnabled(true);
                gpxFileChooser.setCurrentDirectory(new File(Preferences.getLastWorkingDir()));

                if (gpxFileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                    final File[] gpxFiles = gpxFileChooser.getSelectedFiles();
                    if (gpxFiles != null && gpxFiles.length > 0) {
                        Preferences.setLastWorkingDir(gpxFiles[0].getParent());
                        for (final File gpxFile : gpxFiles) {
                            final Color trackColor = Preferences.getTrackColorRandom()
                                    ? Color.getHSBColor(hue, 0.8f, 0.8f)
                                    : Preferences.getTrackColorDefault();

                            addTrackSettingsTab(TrackConfiguration
                                    .createBuilder()
                                    .inputGpx(gpxFile)
                                    .color(trackColor)
                                    .build());

                            hue += 0.275f;
                            while (hue >= 1f) {
                                hue -= 1f;
                            }
                        }
                    }
                }
            }
        };

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return resourceBundle.getString("ui.mainframe.dialog.opensave.filtertext");
            }

            @Override
            public boolean accept(final File f) {
                return f.isDirectory() || f.getName().endsWith(PROJECT_FILENAME_SUFFIX);
            }
        });

        setTitle(Constants.APPNAME_VERSION);
        setIconImages(
                Arrays.asList(
                        new ImageIcon(getClass().getResource("/icon_16.png")).getImage(), //NON-NLS
                        new ImageIcon(getClass().getResource("/icon_32.png")).getImage() //NON-NLS
                )
        );
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 800, 750);

        final JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        final JMenu mnFile = new JMenu(resourceBundle.getString("ui.mainframe.menu.file"));
        menuBar.add(mnFile);

        final JMenuItem mntmNew = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.new"));
        mntmNew.addActionListener(e -> {
            if (!changed || JOptionPane.showConfirmDialog(MainFrame.this, unsavedMessage, warningTitle,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                loadDefaults();
            }
        });
        mnFile.add(mntmNew);

        final JMenuItem mntmOpen = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.open"));
        mntmOpen.addActionListener(e -> {
            if (!changed || JOptionPane.showConfirmDialog(MainFrame.this, unsavedMessage, warningTitle,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                fileChooser.setCurrentDirectory(new File(Preferences.getLastWorkingDir()));
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                    final File fileToOpen = fileChooser.getSelectedFile();
                    Preferences.setLastWorkingDir(fileToOpen.getParent());
                    openFile(fileToOpen);
                }
            }
        });
        mnFile.add(mntmOpen);

        openRecent = new JMenu(resourceBundle.getString("ui.mainframe.menu.file.openrecent"));
        pupulateOpenRecentMenu();
        mnFile.add(openRecent);

        mnFile.addSeparator();

        final JMenuItem mntmSave = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.save"));
        mntmSave.addActionListener(e -> {
            if (file == null) {
                saveAs();
            } else {
                save(file);
            }
        });
        mnFile.add(mntmSave);

        final JMenuItem mntmSaveAs = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.saveas"));
        mntmSaveAs.addActionListener(e -> saveAs());
        mnFile.add(mntmSaveAs);

        mnFile.addSeparator();

        final JMenuItem mntmSaveAsDefault = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.defaults.save"));
        mntmSaveAsDefault.addActionListener(e -> saveAsDefault());
        mnFile.add(mntmSaveAsDefault);

        final JMenuItem mntmResetDefaults = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.defaults.reset"));
        mntmResetDefaults.addActionListener(e -> resetDefaults());
        mnFile.add(mntmResetDefaults);

        mnFile.addSeparator();

        final JMenuItem preferencesMenu = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.preferences"));
        preferencesMenu.addActionListener(e -> SwingUtilities.invokeLater(() -> new PreferencesDialog(this).setVisible(true)));
        mnFile.add(preferencesMenu);

        mnFile.addSeparator();

        final JMenuItem mntmExit = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.exit"));
        mntmExit.addActionListener(this::exitApplication);
        mnFile.add(mntmExit);

        final JMenu mnTrack = new JMenu(resourceBundle.getString("ui.mainframe.menu.track"));
        menuBar.add(mnTrack);

        final JMenuItem mntmAddTrack = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.track.add"));
        mntmAddTrack.addActionListener(addTrackActionListener);
        mnTrack.add(mntmAddTrack);

        final JMenuItem mntmRemoveTrack = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.track.remove"));
        mntmRemoveTrack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int index = tabbedPane.getSelectedIndex();
                if (index >= FIXED_TABS) {
                    tabbedPane.remove(index);
                    afterRemove();
                }
            }
        });
        mnTrack.add(mntmRemoveTrack);

        final JMenu mnHelp = new JMenu(resourceBundle.getString("ui.mainframe.menu.help"));
        menuBar.add(mnHelp);

        final String aboutText = String.format(resourceBundle.getString("ui.mainframe.menu.help.about"), Constants.APPNAME);
        final JMenuItem mntmAbout = new JMenuItem(aboutText);
        mntmAbout.addActionListener(e -> {
            final Map<String, String> variables = new HashMap<>();
            variables.put("APPNAME", Constants.APPNAME); //NON-NLS
            variables.put("VERSION", Constants.VERSION); //NON-NLS
            variables.put("YEAR", Constants.YEAR); //NON-NLS
            variables.put("DESCRIPTION", resourceBundle.getString("ui.dialog.about.description"));
            variables.put("LINK", String.format(resourceBundle.getString("ui.dialog.about.link"), //NON-NLS
                    "<a href=\"https://gpx-animator.app\">https://gpx-animator.app</a>")); //NON-NLS
            SwingUtilities.invokeLater(() -> new MarkdownDialog(this,
                    aboutText, "ABOUT.md", variables, 550, 330));
        });
        mnHelp.add(mntmAbout);

        final JMenuItem mntmUsage = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.help.usage"));
        mntmUsage.addActionListener(e -> {
            final UsageDialog usageDialog = new UsageDialog();
            usageDialog.setLocationRelativeTo(MainFrame.this);
            usageDialog.setVisible(true);
        });
        mnHelp.add(mntmUsage);

        final JMenuItem mntmFAQ = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.help.faq"));
        mntmFAQ.addActionListener(e -> {
            final String url = "https://gpx-animator.app/#faq"; //NON-NLS
            try {
                final String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
                final Runtime rt = Runtime.getRuntime();

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else if (os.contains("win")) { //NON-NLS

                    // this doesn't support showing urls in the form of "page.html#nameLink"
                    rt.exec("rundll32 url.dll,FileProtocolHandler ".concat(url)); //NON-NLS

                } else if (os.contains("mac")) { //NON-NLS

                    rt.exec("open ".concat(url)); //NON-NLS

                } else if (os.contains("nix") || os.contains("nux")) { //NON-NLS

                    // Do a best guess on unix until we get a platform independent way
                    // Build a list of browsers to try, in this order.
                    final String[] browsers = {"chrome", "firefox", "mozilla", "konqueror", //NON-NLS
                            "epiphany", "netscape", "opera", "links", "lynx"}; //NON-NLS

                    // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                    final StringBuilder cmd = new StringBuilder();
                    for (int i = 0; i < browsers.length; i++) {
                        cmd.append(i == 0 ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");
                    }
                    rt.exec(new String[]{"sh", "-c", cmd.toString()});
                }
            } catch (final IOException | URISyntaxException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        String.format(resourceBundle.getString("ui.mainframe.faq.open.errormessage"), url),
                        resourceBundle.getString("ui.mainframe.faq.open.errortitle"),
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });
        mnHelp.add(mntmFAQ);

        final JMenuItem changelogMenu = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.help.changelog"));
        changelogMenu.addActionListener(e -> SwingUtilities.invokeLater(this::showChangelog));
        mnHelp.add(changelogMenu);

        final JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        final GridBagLayout gblContentPane = new GridBagLayout();
        gblContentPane.columnWidths = new int[]{438, 0};
        gblContentPane.rowHeights = new int[]{264, 0, 0};
        gblContentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gblContentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gblContentPane);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        final GridBagConstraints gbcTabbedPane = new GridBagConstraints();
        gbcTabbedPane.insets = new Insets(0, 0, 5, 0);
        gbcTabbedPane.fill = GridBagConstraints.BOTH;
        gbcTabbedPane.gridx = 0;
        gbcTabbedPane.gridy = 0;
        contentPane.add(tabbedPane, gbcTabbedPane);

        tabbedPane.addChangeListener(e -> mntmRemoveTrack.setEnabled(tabbedPane.getSelectedIndex() > 0));

        final JScrollPane generalScrollPane = new JScrollPane();
        tabbedPane.addTab(resourceBundle.getString("ui.mainframe.tab.general"), generalScrollPane);

        generalSettingsPanel = new GeneralSettingsPanel() {
            private static final long serialVersionUID = 9088070803139334820L;

            @Override
            protected void configurationChanged() {
                setChanged(true);
            }
        };

        generalScrollPane.setViewportView(generalSettingsPanel);

        final JPanel panel = new JPanel();
        final GridBagConstraints gbcPanel = new GridBagConstraints();
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.gridx = 0;
        gbcPanel.gridy = 1;
        contentPane.add(panel, gbcPanel);
        final GridBagLayout gblPanel = new GridBagLayout();
        gblPanel.columnWidths = new int[]{174, 49, 0, 32, 0};
        gblPanel.rowHeights = new int[]{27, 0};
        gblPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gblPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel.setLayout(gblPanel);

        final JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        final GridBagConstraints gbcProgressBar = new GridBagConstraints();
        gbcProgressBar.fill = GridBagConstraints.HORIZONTAL;
        gbcProgressBar.insets = new Insets(0, 0, 0, 5);
        gbcProgressBar.gridx = 0;
        gbcProgressBar.gridy = 0;
        panel.add(progressBar, gbcProgressBar);

        final JButton addTrackButton = new JButton(resourceBundle.getString("ui.mainframe.button.addtrack"));
        final GridBagConstraints gbcAddTrackButton = new GridBagConstraints();
        gbcAddTrackButton.anchor = GridBagConstraints.FIRST_LINE_START;
        gbcAddTrackButton.insets = new Insets(0, 0, 0, 5);
        gbcAddTrackButton.gridx = 1;
        gbcAddTrackButton.gridy = 0;
        panel.add(addTrackButton, gbcAddTrackButton);
        addTrackButton.addActionListener(addTrackActionListener);

        //noinspection DuplicateStringLiteralInspection
        renderButton = new JButton(resourceBundle.getString("ui.mainframe.button.render"));
        renderButton.setEnabled(false);
        final GridBagConstraints gbcRenderButton = new GridBagConstraints();
        gbcRenderButton.anchor = GridBagConstraints.FIRST_LINE_START;
        gbcRenderButton.gridx = 3;
        gbcRenderButton.gridy = 0;
        panel.add(renderButton, gbcRenderButton);
        renderButton.addActionListener(e -> {
            if (swingWorker != null) {
                swingWorker.cancel(false);
                return;
            }

            final Configuration cfg = createConfiguration(true, true);
            if (cfg.getOutput().exists()) {
                final String message = String.format(
                        resourceBundle.getString("ui.mainframe.dialog.message.overwrite"), cfg.getOutput());
                final int result = JOptionPane.showConfirmDialog(MainFrame.this,
                        message, warningTitle, JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            swingWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    new Renderer(cfg).render(new RenderingContext() {
                        @Override
                        public void setProgress1(final int pct, final String message) {
                            LOGGER.info("[{}%] {}", pct, message);
                            setProgress(pct);
                            publish(String.format("%s (%d%%)", message, pct)); //NON-NLS
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
                    swingWorker = null; // NOPMD -- dereference the SwingWorker to make it available for garbage collection
                    progressBar.setVisible(false);
                    //noinspection DuplicateStringLiteralInspection
                    renderButton.setText(resourceBundle.getString("ui.mainframe.button.render"));

                    try {
                        get();
                        JOptionPane.showMessageDialog(MainFrame.this,
                                resourceBundle.getString("ui.mainframe.dialog.finished.message"),
                                resourceBundle.getString("ui.mainframe.dialog.finished.title"), JOptionPane.INFORMATION_MESSAGE);
                    } catch (final InterruptedException e) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                resourceBundle.getString("ui.mainframe.dialog.interrupted.message"),
                                resourceBundle.getString("ui.mainframe.dialog.interrupted.title"), JOptionPane.ERROR_MESSAGE);
                    } catch (final ExecutionException e) {
                        e.printStackTrace();
                        new ErrorDialog(MainFrame.this, String.format(
                                resourceBundle.getString("ui.mainframe.dialog.rendering.error.message"),
                                e.getCause().getMessage()), e);
                    } catch (final CancellationException e) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                resourceBundle.getString("ui.mainframe.dialog.cancelled.message"),
                                resourceBundle.getString("ui.mainframe.dialog.cancelled.title"), JOptionPane.WARNING_MESSAGE);
                    }
                }
            };

            swingWorker.addPropertyChangeListener(evt -> {
                final String propertyName = evt.getPropertyName();
                if (isEqual("progress", propertyName)) { //NON-NLS
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            });

            progressBar.setString("");
            progressBar.setValue(0);
            progressBar.setVisible(true);
            renderButton.setText(resourceBundle.getString("ui.mainframe.button.cancel"));
            swingWorker.execute();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                if (!changed || JOptionPane.showConfirmDialog(MainFrame.this,
                        resourceBundle.getString("ui.mainframe.dialog.message.unsaved.exit"), warningTitle, JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION) {
                    System.exit(0); // NOPMD -- Exit on user request
                }

                if (swingWorker != null && !swingWorker.isDone()) {
                    swingWorker.cancel(false);
                }
            }
        });

        SwingUtilities.invokeLater(this::loadDefaults);
        SwingUtilities.invokeLater(this::showChangelogOnce);
    }

    @SuppressWarnings({"PMD.DoNotCallSystemExit", "DuplicateStringLiteralInspection"}) // Exit the application on user request
    @SuppressFBWarnings(value = "DM_EXIT", justification = "Exit the application on user request") //NON-NLS
    private void exitApplication(final ActionEvent e) {
        if (!changed || JOptionPane.showConfirmDialog(MainFrame.this, unsavedMessage, warningTitle,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public Configuration createConfiguration(final boolean includeTracks, final boolean replacePlaceholders) {
        final Configuration.Builder builder = Configuration.createBuilder();

        generalSettingsPanel.buildConfiguration(builder, replacePlaceholders);

        if (includeTracks) {
            for (int i = FIXED_TABS, n = tabbedPane.getTabCount(); i < n; i++) {
                final TrackSettingsPanel tsp = (TrackSettingsPanel) ((JScrollPane) tabbedPane.getComponentAt(i)).getViewport().getView();
                builder.addTrackConfiguration(tsp.createConfiguration());
            }
        }

        return builder.build();
    }

    public void setConfiguration(final Configuration c) {
        generalSettingsPanel.setConfiguration(c);

        // remove all track tabs
        for (int i = tabbedPane.getTabCount() - 1; i >= FIXED_TABS; i--) {
            tabbedPane.remove(i);
        }
        afterRemove();

        for (final TrackConfiguration tc : c.getTrackConfigurationList()) {
            addTrackSettingsTab(tc);
        }

        setChanged(false);
    }

    private void pupulateOpenRecentMenu() {
        openRecent.removeAll();
        Preferences.getRecentFiles().forEach(recentFile -> {
                    JMenuItem item = new JMenuItem(recentFile.getName());
                    openRecent.add(item);
                    item.addActionListener(e -> openFile(recentFile));
                }
        );
    }

    private void openFile(final File fileToOpen) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setAdapter(new FileXmlAdapter(fileToOpen.getParentFile()));
            setConfiguration((Configuration) unmarshaller.unmarshal(fileToOpen));
            MainFrame.this.file = fileToOpen;
            addRecentFile(fileToOpen);
            setChanged(false);
        } catch (final JAXBException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this,
                    String.format(resourceBundle.getString("ui.mainframe.dialog.message.openconfig.error"), e1.getMessage()),
                    errorTitle, JOptionPane.ERROR_MESSAGE);
        }
    }


    private void addRecentFile(final File recentFile) {
        Preferences.addRecentFile(recentFile);
        pupulateOpenRecentMenu();
    }


    private void setChanged(final boolean changed) {
        this.changed = changed;
        updateTitle();
    }

    private void updateTitle() {
        final String filename = file != null ? file.getName() : resourceBundle.getString("ui.mainframe.filename.unnamed");
        setTitle(Constants.APPNAME_VERSION.concat(" - ").concat(filename).concat(changed ? " (*)" : ""));
    }

    private void addTrackSettingsTab(final TrackConfiguration tc) {
        final String trackTabTitle = resourceBundle.getString("ui.mainframe.tab.track");
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
                setChanged(true);
            }

            @Override
            protected void labelChanged(final String label) {
                tabbedPane.setTitleAt(tabbedPane.indexOfComponent(trackScrollPane), label == null || label.isEmpty() ? trackTabTitle : label);
            }
        };

        tabbedPane.addTab(trackTabTitle, trackScrollPane);
        trackScrollPane.setViewportView(trackSettingsPanel);
        tabbedPane.setSelectedComponent(trackScrollPane);
        trackSettingsPanel.setConfiguration(tc);

        renderButton.setEnabled(true);

        setChanged(true);
    }

    private void afterRemove() {
        if (tabbedPane.getTabCount() == 1) { // NOPMD -- Ignore magic number literal
            renderButton.setEnabled(false);
        }
        setChanged(true);
    }

    private void saveAs() {
        final String lastCwd = Preferences.getLastWorkingDir();
        fileChooser.setCurrentDirectory(new File(lastCwd));
        fileChooser.setSelectedFile(new File("")); // to forget previous file name
        if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            Preferences.setLastWorkingDir(fileToSave.getParent());

            if (!fileToSave.getName().endsWith(PROJECT_FILENAME_SUFFIX)) {
                fileToSave = new File(fileToSave.getPath() + PROJECT_FILENAME_SUFFIX);
            }
            save(fileToSave);
        }
    }

    private void save(final File fileToSave) {
        try {
            try {
                final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
                final Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setAdapter(new FileXmlAdapter(fileToSave.getParentFile()));
                marshaller.marshal(createConfiguration(true, false), fileToSave);
                MainFrame.this.file = fileToSave;
                setChanged(false);
                addRecentFile(fileToSave);
            } catch (final JAXBException e) {
                throw new UserException(e.getMessage(), e);
            }
        } catch (final UserException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    String.format(resourceBundle.getString("ui.mainframe.dialog.message.saveconfig.error"), e.getMessage()),
                    errorTitle, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAsDefault() {
        try {
            try {
                final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
                final Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setAdapter(new FileXmlAdapter(null));
                marshaller.marshal(createConfiguration(false, false), defaultConfigFile);
            } catch (final JAXBException e) {
                throw new UserException(e.getMessage(), e);
            }
        } catch (final UserException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    String.format(resourceBundle.getString("ui.mainframe.dialog.message.savedefault.error"), e.getMessage()),
                    errorTitle, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDefaults() {
        file = null; // NOPMD -- Loading defaults = resetting everything means unsetting the filename, too
        setChanged(false);

        if (defaultConfigFile == null || !defaultConfigFile.exists()) {
            setConfiguration(Configuration.createBuilder().build());
            return;
        }

        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setAdapter(new FileXmlAdapter(null));
            setConfiguration((Configuration) unmarshaller.unmarshal(defaultConfigFile));
        } catch (final JAXBException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this,
                    String.format(resourceBundle.getString("ui.mainframe.dialog.message.loaddefault.error"), e1.getMessage()),
                    errorTitle, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetDefaults() {
        if (defaultConfigFile != null) {
            if (defaultConfigFile.delete()) {
                loadDefaults();
            } else {
                JOptionPane.showMessageDialog(MainFrame.this,
                        resourceBundle.getString("ui.mainframe.dialog.message.resetdefault.error"),
                        errorTitle, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showChangelog() {
        SwingUtilities.invokeLater(() -> new MarkdownDialog(this,
                String.format(resourceBundle.getString("ui.dialog.changelog.title"), Constants.APPNAME_VERSION),
                "CHANGELOG.md", this.getWidth() - 150, this.getHeight() - 150));
    }

    private void showChangelogOnce() {
        if (Collator.getInstance().compare(Preferences.getChangelogVersion(), Constants.VERSION) != 0) {
            showChangelog();
            Preferences.setChangelogVersion(Constants.VERSION);
        }
    }

}
