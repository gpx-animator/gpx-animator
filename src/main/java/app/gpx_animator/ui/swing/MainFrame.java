/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.ui.swing;

import app.gpx_animator.core.Constants;
import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.configuration.TrackConfiguration;
import app.gpx_animator.core.configuration.adapter.FileXmlAdapter;
import app.gpx_animator.core.data.MapTemplate;
import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.core.renderer.Renderer;
import app.gpx_animator.core.renderer.RenderingContext;
import app.gpx_animator.core.util.MapUtil;
import app.gpx_animator.core.util.Notification;
import app.gpx_animator.core.util.Sound;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.Color;
import java.awt.Cursor;
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
import java.io.Serial;
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
import java.util.concurrent.atomic.AtomicInteger;

import static app.gpx_animator.core.util.Utils.isEqual;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_F1;
import static java.awt.event.KeyEvent.VK_F4;
import static java.util.Objects.requireNonNull;
import static javax.swing.KeyStroke.getKeyStroke;

public final class MainFrame extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainFrame.class);

    private static final String PROJECT_FILENAME_SUFFIX = ".ga.xml"; //NON-NLS
    @Serial
    private static final long serialVersionUID = 8250354536303830558L;
    private static final int FIXED_TABS = 1;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();
    private final String unsavedMessage = resourceBundle.getString("ui.mainframe.dialog.message.unsaved.continue");
    private final String warningTitle = resourceBundle.getString("ui.mainframe.dialog.title.warning");
    private final String errorTitle = resourceBundle.getString("ui.mainframe.dialog.title.error");

    private final File defaultConfigFile = new File(Preferences.getConfigurationDir()
            + Preferences.FILE_SEPARATOR + "defaultConfig.ga.xml");
    private final JTabbedPane tabbedPane;
    private final JButton previewButton;
    private final JButton renderButton;
    private final JMenu openRecent;
    private final JFileChooser fileChooser = new JFileChooser();
    private final GeneralSettingsPanel generalSettingsPanel;

    private transient SwingWorker<Void, String> swingWorker;
    private File file;
    private boolean changed;

    private static final class InstanceHolder {
        static final MainFrame INSTANCE = new MainFrame();
    }

    public static MainFrame getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @SuppressWarnings("checkstyle:MethodLength") // TODO Refactor when doing the redesign task https://github.com/zdila/gpx-animator/issues/60
    private MainFrame() {
        Notification.init();

        final var addTrackActionListener = new TrackActionListener(MainFrame.this, resourceBundle);

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
                        new ImageIcon(requireNonNull(getClass().getResource("/icon_16.png"))).getImage(), //NON-NLS
                        new ImageIcon(requireNonNull(getClass().getResource("/icon_32.png"))).getImage() //NON-NLS
                )
        );
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 800, 750);

        final var menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        final var mnFile = new JMenu(resourceBundle.getString("ui.mainframe.menu.file"));
        menuBar.add(mnFile);

        final var mntmNew = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.new"));
        mntmNew.addActionListener(e -> {
            if (!changed || JOptionPane.showConfirmDialog(MainFrame.this, unsavedMessage, warningTitle,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                loadDefaults();
            }
        });
        mntmNew.setAccelerator(getKeyStroke('N', CTRL_DOWN_MASK));
        mnFile.add(mntmNew);

        final var mntmOpen = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.open"));
        mntmOpen.addActionListener(e -> {
            if (!changed || JOptionPane.showConfirmDialog(MainFrame.this, unsavedMessage, warningTitle,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                fileChooser.setCurrentDirectory(new File(Preferences.getLastWorkingDir()));
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                    final var fileToOpen = fileChooser.getSelectedFile();
                    Preferences.setLastWorkingDir(fileToOpen.getParent());
                    openFile(fileToOpen);
                }
            }
        });
        mntmOpen.setAccelerator(getKeyStroke('O', CTRL_DOWN_MASK));
        mnFile.add(mntmOpen);

        openRecent = new JMenu(resourceBundle.getString("ui.mainframe.menu.file.openrecent"));
        pupulateOpenRecentMenu();
        mnFile.add(openRecent);

        mnFile.addSeparator();

        final var mntmSave = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.save"));
        mntmSave.addActionListener(e -> {
            if (file == null) {
                saveAs();
            } else {
                save(file);
            }
        });
        mntmSave.setAccelerator(getKeyStroke('S', CTRL_DOWN_MASK));
        mnFile.add(mntmSave);

        final var mntmSaveAs = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.saveas"));
        mntmSaveAs.addActionListener(e -> saveAs());
        mntmSaveAs.setAccelerator(getKeyStroke('S', CTRL_DOWN_MASK + ALT_DOWN_MASK));
        mnFile.add(mntmSaveAs);

        mnFile.addSeparator();

        final var mntmSaveAsDefault = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.defaults.save"));
        mntmSaveAsDefault.addActionListener(e -> saveAsDefault());
        mnFile.add(mntmSaveAsDefault);

        final var mntmResetDefaults = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.defaults.reset"));
        mntmResetDefaults.addActionListener(e -> resetDefaults());
        mnFile.add(mntmResetDefaults);

        mnFile.addSeparator();

        final var preferencesMenu = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.preferences"));
        preferencesMenu.addActionListener(e -> SwingUtilities.invokeLater(() -> new PreferencesDialog(this).setVisible(true)));
        preferencesMenu.setAccelerator(getKeyStroke('.', CTRL_DOWN_MASK));
        mnFile.add(preferencesMenu);

        mnFile.addSeparator();

        final var updateMapsMenu = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.updatemaps"));
        mnFile.add(updateMapsMenu);

        mnFile.addSeparator();

        final var mntmExit = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.file.exit"));
        mntmExit.addActionListener(this::exitApplication);
        mntmExit.setAccelerator(getKeyStroke(VK_F4, ALT_DOWN_MASK));
        mnFile.add(mntmExit);

        final var mnTrack = new JMenu(resourceBundle.getString("ui.mainframe.menu.track"));
        menuBar.add(mnTrack);

        final var mntmAddTrack = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.track.add"));
        mntmAddTrack.addActionListener(addTrackActionListener);
        mntmAddTrack.setAccelerator(getKeyStroke('A', CTRL_DOWN_MASK));
        mnTrack.add(mntmAddTrack);

        final var mntmRemoveTrack = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.track.remove"));
        mntmRemoveTrack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final var index = tabbedPane.getSelectedIndex();
                if (index >= FIXED_TABS) {
                    tabbedPane.remove(index);
                    afterRemove();
                }
            }
        });
        mntmRemoveTrack.setAccelerator(getKeyStroke('R', CTRL_DOWN_MASK));
        mnTrack.add(mntmRemoveTrack);

        final var mnHelp = new JMenu(resourceBundle.getString("ui.mainframe.menu.help"));
        menuBar.add(mnHelp);

        final var aboutText = String.format(resourceBundle.getString("ui.mainframe.menu.help.about"), Constants.APPNAME);
        final var mntmAbout = new JMenuItem(aboutText);
        mntmAbout.addActionListener(e -> {
            final Map<String, String> variables = new HashMap<>();
            variables.put("APPNAME", Constants.APPNAME); //NON-NLS
            variables.put("VERSION", Constants.VERSION); //NON-NLS
            variables.put("COPYRIGHT", Constants.COPYRIGHT); //NON-NLS
            variables.put("DESCRIPTION", resourceBundle.getString("ui.dialog.about.description"));
            variables.put("LINK", String.format(resourceBundle.getString("ui.dialog.about.link"), //NON-NLS
                    "<a href=\"https://gpx-animator.app\">https://gpx-animator.app</a>")); //NON-NLS
            SwingUtilities.invokeLater(() -> new MarkdownDialog(this,
                    aboutText, "ABOUT.md", variables, 550, 350));
        });
        mntmAbout.setAccelerator(getKeyStroke(VK_F1, 0));
        mnHelp.add(mntmAbout);

        final var mntmLicense = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.help.license"));
        mntmLicense.addActionListener(e -> new MarkdownDialog(MainFrame.this, resourceBundle.getString("ui.mainframe.menu.help.license"),
                "LICENSE.md", 700, 500));
        mnHelp.add(mntmLicense);

        final var mntmUsage = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.help.usage"));
        mntmUsage.addActionListener(e -> {
            final var usageDialog = new UsageDialog(MainFrame.this);
            usageDialog.setLocationRelativeTo(MainFrame.this);
            usageDialog.setVisible(true);
        });
        mnHelp.add(mntmUsage);

        final var mntmFAQ = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.help.faq"));
        mntmFAQ.addActionListener(e -> {
            final var url = "https://gpx-animator.app/#faq"; //NON-NLS
            try {
                final var os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
                final var rt = Runtime.getRuntime();

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else if (os.contains("win")) { //NON-NLS

                    // this doesn't support showing urls in the form of "page.html#nameLink"
                    rt.exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", url }); //NON-NLS

                } else if (os.contains("mac")) { //NON-NLS

                    rt.exec(new String[] { "open", url }); //NON-NLS

                } else if (os.contains("nix") || os.contains("nux")) { //NON-NLS

                    // Do a best guess on unix until we get a platform independent way
                    // Build a list of browsers to try, in this order.
                    final String[] browsers = {"chrome", "firefox", "mozilla", "konqueror", //NON-NLS
                            "epiphany", "netscape", "opera", "links", "lynx"}; //NON-NLS

                    // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                    final var cmd = new StringBuilder();
                    for (var i = 0; i < browsers.length; i++) {
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

        final var changelogMenu = new JMenuItem(resourceBundle.getString("ui.mainframe.menu.help.changelog"));
        changelogMenu.addActionListener(e -> SwingUtilities.invokeLater(this::showChangelog));
        mnHelp.add(changelogMenu);

        final var contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        final var gblContentPane = new GridBagLayout();
        gblContentPane.columnWidths = new int[]{438, 0};
        gblContentPane.rowHeights = new int[]{264, 0, 0};
        gblContentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gblContentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gblContentPane);

        tabbedPane = new JTabbedPane(SwingConstants.TOP);
        final var gbcTabbedPane = new GridBagConstraints();
        gbcTabbedPane.insets = new Insets(0, 0, 5, 0);
        gbcTabbedPane.fill = GridBagConstraints.BOTH;
        gbcTabbedPane.gridx = 0;
        gbcTabbedPane.gridy = 0;
        contentPane.add(tabbedPane, gbcTabbedPane);

        tabbedPane.addChangeListener(e -> mntmRemoveTrack.setEnabled(tabbedPane.getSelectedIndex() > 0));

        final var generalScrollPane = new JScrollPane();
        tabbedPane.addTab(resourceBundle.getString("ui.mainframe.tab.general"), generalScrollPane);

        generalSettingsPanel = new GeneralSettingsPanel() {
            @Serial
            private static final long serialVersionUID = 9088070803139334820L;

            @Override
            protected void configurationChanged() {
                setChanged(true);
            }
        };
        generalScrollPane.setViewportView(generalSettingsPanel);
        updateMapsMenu.addActionListener(e -> new MapLoader(this, generalSettingsPanel, resourceBundle).execute());

        final var panel = new JPanel();
        final var gbcPanel = new GridBagConstraints();
        gbcPanel.fill = GridBagConstraints.BOTH;
        gbcPanel.gridx = 0;
        gbcPanel.gridy = 1;
        contentPane.add(panel, gbcPanel);
        final var gblPanel = new GridBagLayout();
        gblPanel.columnWidths = new int[]{174, 49, 0, 32, 0};
        gblPanel.rowHeights = new int[]{27, 0};
        gblPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gblPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel.setLayout(gblPanel);

        final var progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        final var gbcProgressBar = new GridBagConstraints();
        gbcProgressBar.fill = GridBagConstraints.HORIZONTAL;
        gbcProgressBar.insets = new Insets(0, 0, 0, 5);
        gbcProgressBar.gridx = 0;
        gbcProgressBar.gridy = 0;
        panel.add(progressBar, gbcProgressBar);

        final var addTrackButton = new JButton(resourceBundle.getString("ui.mainframe.button.addtrack"));
        final var gbcAddTrackButton = new GridBagConstraints();
        gbcAddTrackButton.anchor = GridBagConstraints.FIRST_LINE_START;
        gbcAddTrackButton.insets = new Insets(0, 0, 0, 5);
        gbcAddTrackButton.gridx = 1;
        gbcAddTrackButton.gridy = 0;
        panel.add(addTrackButton, gbcAddTrackButton);
        addTrackButton.addActionListener(addTrackActionListener);

        previewButton = new JButton(resourceBundle.getString("ui.mainframe.button.preview"));
        previewButton.setEnabled(false);
        final var gbcPreviewButton = new GridBagConstraints();
        gbcPreviewButton.anchor = GridBagConstraints.FIRST_LINE_START;
        gbcPreviewButton.insets = new Insets(0, 0, 0, 5);
        gbcPreviewButton.gridx = 3;
        gbcPreviewButton.gridy = 0;
        panel.add(previewButton, gbcPreviewButton);
        previewButton.addActionListener(e -> render(progressBar, true));

        //noinspection DuplicateStringLiteralInspection
        renderButton = new JButton(resourceBundle.getString("ui.mainframe.button.render"));
        renderButton.setEnabled(false);
        final var gbcRenderButton = new GridBagConstraints();
        gbcRenderButton.anchor = GridBagConstraints.FIRST_LINE_START;
        gbcRenderButton.gridx = 5;
        gbcRenderButton.gridy = 0;
        panel.add(renderButton, gbcRenderButton);
        renderButton.addActionListener(e -> render(progressBar, false));

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
        SwingUtilities.invokeLater(this::checkMapData);
        SwingUtilities.invokeLater(this::showChangelogOnce);
    }

    private void render(@NonNull final JProgressBar progressBar, final boolean preview) {
        if (swingWorker != null) {
            swingWorker.cancel(false);
            return;
        }

        final var cfg = createConfiguration(true, true, preview);
        if (cfg.getOutput().exists() && !cfg.isPreview()) {
            final var message = String.format(
                    resourceBundle.getString("ui.mainframe.dialog.message.overwrite"), cfg.getOutput());
            final var result = JOptionPane.showConfirmDialog(MainFrame.this,
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
                    if (!cfg.isPreview()) {
                        final var title = resourceBundle.getString("ui.mainframe.dialog.finished.title");
                        final var message = resourceBundle.getString("ui.mainframe.dialog.finished.message");
                        if (Notification.isSupported()) {
                            Notification.INFO.show(title, message);
                        } else {
                            Sound.SUCCESS.play();
                        }
                        JOptionPane.showMessageDialog(MainFrame.this, message, title, JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (final InterruptedException e) {
                    final var title = resourceBundle.getString("ui.mainframe.dialog.interrupted.title");
                    final var message = resourceBundle.getString("ui.mainframe.dialog.interrupted.message");
                    if (Notification.isSupported()) {
                        Notification.ERROR.show(title, message);
                    } else {
                        Sound.ERROR.play();
                    }
                    JOptionPane.showMessageDialog(MainFrame.this, message, title, JOptionPane.ERROR_MESSAGE);
                } catch (final ExecutionException e) {
                    final var cause = e.getCause();
                    if (cause instanceof UserException) {
                        final var title = resourceBundle.getString("ui.mainframe.dialog.title.error");
                        final var message = cause.getMessage();
                        if (Notification.isSupported()) {
                            Notification.ERROR.show(title, message);
                        } else {
                            Sound.ERROR.play();
                        }
                        JOptionPane.showMessageDialog(MainFrame.this, message, title, JOptionPane.ERROR_MESSAGE);
                    } else {
                        final var title = resourceBundle.getString("ui.mainframe.dialog.title.error");
                        final var message = String.format(
                                resourceBundle.getString("ui.mainframe.dialog.rendering.error.message"),
                                e.getCause().getMessage());
                        if (Notification.isSupported()) {
                            Notification.ERROR.show(title, message);
                        } else {
                            Sound.ERROR.play();
                        }
                        new ErrorDialog(MainFrame.this, message, e);
                    }
                } catch (final CancellationException e) {
                    final var title = resourceBundle.getString("ui.mainframe.dialog.cancelled.title");
                    final var message = resourceBundle.getString("ui.mainframe.dialog.cancelled.message");
                    if (Notification.isSupported()) {
                        Notification.WARNING.show(title, message);
                    } else {
                        Sound.ERROR.play();
                    }
                    JOptionPane.showMessageDialog(MainFrame.this, message, title, JOptionPane.WARNING_MESSAGE);
                }
            }
        };

        swingWorker.addPropertyChangeListener(evt -> {
            final var propertyName = evt.getPropertyName();
            if (isEqual("progress", propertyName)) { //NON-NLS
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        progressBar.setString("");
        progressBar.setValue(0);
        progressBar.setVisible(true);
        renderButton.setText(resourceBundle.getString("ui.mainframe.button.cancel"));
        swingWorker.execute();
    }

    @SuppressWarnings({"PMD.DoNotTerminateVM", "DuplicateStringLiteralInspection"}) // Exit the application on user request
    @SuppressFBWarnings(value = "DM_EXIT", justification = "Exit the application on user request") //NON-NLS
    private void exitApplication(final ActionEvent e) {
        if (!changed || JOptionPane.showConfirmDialog(MainFrame.this, unsavedMessage, warningTitle,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public Configuration createConfiguration(final boolean includeTracks, final boolean replacePlaceholders, final boolean preview) {
        final var builder = Configuration.createBuilder()
                .preview(preview);

        generalSettingsPanel.buildConfiguration(builder, replacePlaceholders);

        if (includeTracks) {
            for (int i = FIXED_TABS, n = tabbedPane.getTabCount(); i < n; i++) {
                final var tsp = (TrackSettingsPanel) ((JScrollPane) tabbedPane.getComponentAt(i)).getViewport().getView();
                builder.addTrackConfiguration(tsp.createConfiguration());
            }
        }

        return builder.build();
    }

    public void setConfiguration(final Configuration c) {
        generalSettingsPanel.setConfiguration(c);

        // remove all track tabs
        for (var i = tabbedPane.getTabCount() - 1; i >= FIXED_TABS; i--) {
            tabbedPane.remove(i);
        }
        afterRemove();

        for (final var tc : c.getTrackConfigurationList()) {
            addTrackSettingsTab(tc);
        }

        setChanged(false);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition") // maximum of 10 items can get a hotkey (see if-condition)
    private void pupulateOpenRecentMenu() {
        openRecent.removeAll();
        final var counter = new AtomicInteger(0);
        Preferences.getRecentFiles().forEach(recentFile -> {
            final var item = new JMenuItem(recentFile.getName());
            if (counter.incrementAndGet() < 10) {
                item.setAccelerator(getKeyStroke(48 + counter.get(), CTRL_DOWN_MASK));
            }
            openRecent.add(item);
            item.addActionListener(e -> openFile(recentFile));
        });
        openRecent.setEnabled(counter.get() > 0);
    }

    private void openFile(final File fileToOpen) {
        try {
            final var jaxbContext = JAXBContext.newInstance(Configuration.class);
            final var unmarshaller = jaxbContext.createUnmarshaller();
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
        final var filename = file != null ? file.getName() : resourceBundle.getString("ui.mainframe.filename.unnamed");
        setTitle(Constants.APPNAME_VERSION.concat(" - ").concat(filename).concat(changed ? " (*)" : ""));
    }

    void addTrackSettingsTab(final TrackConfiguration tc) {
        final var trackTabTitle = resourceBundle.getString("ui.mainframe.tab.track");
        final var trackScrollPane = new JScrollPane();
        final var trackSettingsPanel = new TrackSettingsPanel() {
            @Serial
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
        previewButton.setEnabled(true);

        setChanged(true);
    }

    private void afterRemove() {
        if (tabbedPane.getTabCount() == 1) { // NOPMD -- Ignore magic number literal
            renderButton.setEnabled(false);
            previewButton.setEnabled(false);
        }
        setChanged(true);
    }

    private void saveAs() {
        final var lastCwd = Preferences.getLastWorkingDir();
        fileChooser.setCurrentDirectory(new File(lastCwd));
        fileChooser.setSelectedFile(new File("")); // to forget previous file name
        if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
            var fileToSave = fileChooser.getSelectedFile();
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
                final var jaxbContext = JAXBContext.newInstance(Configuration.class);
                final var marshaller = jaxbContext.createMarshaller();
                marshaller.setAdapter(new FileXmlAdapter(fileToSave.getParentFile()));
                marshaller.marshal(createConfiguration(true, false, false), fileToSave);
                MainFrame.this.file = fileToSave;
                setChanged(false);
                addRecentFile(fileToSave);
            } catch (final JAXBException e) {
                throw new UserException(e.getMessage(), e);
            }
        } catch (final UserException e) {
            e.printStackTrace();
            new ErrorDialog(this,
                    String.format(resourceBundle.getString("ui.mainframe.dialog.message.saveconfig.error"),
                            e.getMessage()), e);
        }
    }

    private void saveAsDefault() {
        try {
            try {
                final var jaxbContext = JAXBContext.newInstance(Configuration.class);
                final var marshaller = jaxbContext.createMarshaller();
                marshaller.setAdapter(new FileXmlAdapter(null));
                marshaller.marshal(createConfiguration(false, false, false), defaultConfigFile);
            } catch (final JAXBException e) {
                throw new UserException(e.getMessage(), e);
            }
        } catch (final UserException e) {
            e.printStackTrace();
            new ErrorDialog(this,
                    String.format(resourceBundle.getString("ui.mainframe.dialog.message.savedefault.error"),
                            e.getMessage()), e);
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
            final var jaxbContext = JAXBContext.newInstance(Configuration.class);
            final var unmarshaller = jaxbContext.createUnmarshaller();
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

    private void checkMapData() {
        if (MapUtil.hasNoMaps() && JOptionPane.showConfirmDialog(this,
                resourceBundle.getString("ui.mainframe.dialog.message.mapdata"),
                resourceBundle.getString("ui.mainframe.dialog.title.mapdata"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new MapLoader(this, generalSettingsPanel, resourceBundle).execute();
        } else {
            MapUtil.touchMapFile();
        }
    }

    @SuppressFBWarnings(value = "DMI_RANDOM_USED_ONLY_ONCE", justification = "we need just ONE random number ONCE")
    static class TrackActionListener implements ActionListener {

        private final MainFrame mainFrame;
        private final ResourceBundle resourceBundle;

        TrackActionListener(@NotNull final MainFrame mainFrame, @NotNull final ResourceBundle resourceBundle) {
            this.mainFrame = mainFrame;
            this.resourceBundle = resourceBundle;
        }

        private float hue = new Random().nextFloat();

        @Override
        public void actionPerformed(final ActionEvent e) {

            final var gpxFileChooser = new JFileChooser();
            TrackSettingsPanel.configureGpxFileChooser(resourceBundle, gpxFileChooser);
            gpxFileChooser.setMultiSelectionEnabled(true);
            gpxFileChooser.setCurrentDirectory(new File(Preferences.getLastWorkingDir()));

            if (gpxFileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                final var gpxFiles = gpxFileChooser.getSelectedFiles();
                if (gpxFiles != null && gpxFiles.length > 0) {
                    Preferences.setLastWorkingDir(gpxFiles[0].getParent());
                    for (final var gpxFile : gpxFiles) {
                        final var trackColor = Preferences.getTrackColorRandom()
                                ? Color.getHSBColor(hue, 0.8f, 0.8f)
                                : Preferences.getTrackColorDefault();

                        mainFrame.addTrackSettingsTab(TrackConfiguration
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
    }

    static class MapLoader extends SwingWorker<List<MapTemplate>, Void> {

        private final MainFrame mainFrame;
        private final GeneralSettingsPanel generalSettingsPanel;
        private final ResourceBundle resourceBundle;

        MapLoader(@NotNull final MainFrame mainFrame,
                         @NotNull final GeneralSettingsPanel generalSettingsPanel,
                         @NotNull final ResourceBundle resourceBundle) {
            this.mainFrame = mainFrame;
            this.generalSettingsPanel = generalSettingsPanel;
            this.resourceBundle = resourceBundle;
            mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        @Override
        protected List<MapTemplate> doInBackground() {
            return MapUtil.updateMaps();
        }

        @Override
        public void done() {
            try {
                final var mapTemplateList = get();
                generalSettingsPanel.updateMaps(mapTemplateList);
                mainFrame.setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(mainFrame,
                        String.format(resourceBundle.getString("ui.mainframe.dialog.message.mapupdate.success"), mapTemplateList.size()),
                        resourceBundle.getString("ui.mainframe.dialog.title.success"),
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (final Exception e) {
                new ErrorDialog(mainFrame,
                        String.format(resourceBundle.getString("ui.mainframe.dialog.message.mapupdate.error"),
                                e.getMessage()), e);

            } finally {
                mainFrame.setCursor(Cursor.getDefaultCursor());
            }
        }
    }
}
