package sk.freemap.gpxAnimator.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import sk.freemap.gpxAnimator.Configuration;
import sk.freemap.gpxAnimator.Constants;
import sk.freemap.gpxAnimator.FileXmlAdapter;
import sk.freemap.gpxAnimator.Renderer;
import sk.freemap.gpxAnimator.RenderingContext;
import sk.freemap.gpxAnimator.TrackConfiguration;
import sk.freemap.gpxAnimator.UserException;

public class MainFrame extends JFrame {

  static final String PREF_LAST_CWD = "lastCWD";

  private static final String PROJECT_FILENAME_SUFFIX = ".ga.xml";

  private static final String UNSAVED_MSG = "There are unsaved changes. Continue?";

  private static final String TITLE = "GPX Animator " + Constants.VERSION;
  public static final String RECENT_FILES = "recent_files";
  public static final String RECENT_FILE_DELIMITER = ",";

  private static int FIXED_TABS = 1; // TODO set to 2 for MapPanel

  private static final long serialVersionUID = 190371886979948114L;

  private final JPanel contentPane;
  private final JTabbedPane tabbedPane;
  private final JButton renderButton;
  private final JMenu openRecent;

  private SwingWorker<Void, String> swingWorker;

  private final JFileChooser fileChooser = new JFileChooser();

  private File file;

  private boolean changed;


  private GeneralSettingsPanel generalSettingsPanel;

  private final Preferences prefs = Preferences.userRoot().node("app");

  private final ActionListener addTrackActionListener;
  private final ActionListener resetGeneralSettings;
  private final ActionListener saveGeneralSettings;


  public Configuration createConfiguration() throws UserException {
    final Configuration.Builder b = Configuration.createBuilder();

    generalSettingsPanel.buildConfiguration(b);

    for (int i = FIXED_TABS, n = tabbedPane.getTabCount(); i < n; i++) {
      final TrackSettingsPanel tsp = (TrackSettingsPanel) ((JScrollPane) tabbedPane.getComponentAt(i)).getViewport().getView();
      b.addTrackConfiguration(tsp.createConfiguration());
    }

    return b.build();
  }


  public void setConfiguration(final Configuration c) {
    try {
      generalSettingsPanel.setConfiguration(c);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // remove all track tabs
    for (int i = tabbedPane.getTabCount() - 1; i >= FIXED_TABS; i--) {
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
    addTrackActionListener = new ActionListener() {
      float hue = new Random().nextFloat();

      @Override
      public void actionPerformed(final ActionEvent e) {
        try {
          addTrackSettingsTab(TrackConfiguration
              .createBuilder()
              .color(Color.getHSBColor(hue, 0.8f, 0.8f))
              .build());
          hue += 0.275f;
          while (hue >= 1f) {
            hue -= 1f;
          }
        } catch (final UserException ex) {
          throw new RuntimeException(ex);
        }
      }
    };

    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.addChoosableFileFilter(new FileFilter() {
      @Override
      public String getDescription() {
        return "GPX Animator Configuration Files";
      }

      @Override
      public boolean accept(final File f) {
        return f.isDirectory() || f.getName().endsWith(PROJECT_FILENAME_SUFFIX);
      }
    });

    setTitle(TITLE);
    setIconImages(
        Arrays.asList(
            new ImageIcon(getClass().getResource("/icon_16.png")).getImage(),
            new ImageIcon(getClass().getResource("/icon_32.png")).getImage()
        )
    );
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setBounds(100, 100, 800, 750);

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

          final String lastCwd = prefs.get(PREF_LAST_CWD, null);
          fileChooser.setCurrentDirectory(new File(lastCwd == null ? System.getProperty("user.dir") : lastCwd));
          if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            prefs.put(PREF_LAST_CWD, file.getParent());

            openFile(file);
          }
        }

      }
    });
    mnFile.add(mntmOpen);

    openRecent = new JMenu("Open Recent");
    pupulateOpenRecentMenu();
    mnFile.add(openRecent);

    mnFile.addSeparator();

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
    mnFile.addSeparator();

    final JMenuItem mntmExit = new JMenuItem("Exit");
    mntmExit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (!changed || JOptionPane.showConfirmDialog(MainFrame.this, UNSAVED_MSG, "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
          System.exit(0);
        }
      }
    });
    mnFile.add(mntmExit);

    final JMenu mnTrack = new JMenu("Track");
    menuBar.add(mnTrack);

    final JMenuItem mntmAddTrack = new JMenuItem("Add");
    mntmAddTrack.addActionListener(addTrackActionListener);
    mnTrack.add(mntmAddTrack);

    final JMenuItem mntmRemoveTrack = new JMenuItem("Remove");
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

    final JMenuItem mntmFAQ = new JMenuItem("FAQ");
    mntmFAQ.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final String url = "https://gpx-animator.app/#faq";
        try {
          final String os = System.getProperty("os.name").toLowerCase();
          final Runtime rt = Runtime.getRuntime();

          if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
          } else if (os.indexOf("win") >= 0) {

            // this doesn't support showing urls in the form of "page.html#nameLink"
            rt.exec("rundll32 url.dll,FileProtocolHandler " + url);

          } else if (os.indexOf("mac") >= 0) {

            rt.exec("open " + url);

          } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {

            // Do a best guess on unix until we get a platform independent way
            // Build a list of browsers to try, in this order.
            final String[] browsers = {"chrome", "firefox", "mozilla", "konqueror",
                "epiphany", "netscape", "opera", "links", "lynx"};

            // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
            final StringBuffer cmd = new StringBuffer();
            for (int i = 0; i < browsers.length; i++) {
              cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + url + "\" ");
            }
            rt.exec(new String[]{"sh", "-c", cmd.toString()});
          }
        } catch (final Exception exception) {
          JOptionPane.showMessageDialog(
              null,
              "I was not able to start a web browser for the FAQ:\n" + url,
              "Sorry",
              JOptionPane.WARNING_MESSAGE
          );
        }
      }
    });
    mnHelp.add(mntmFAQ);

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
    tabbedPane.addTab("General", generalScrollPane);

    generalSettingsPanel = new GeneralSettingsPanel() {
      private static final long serialVersionUID = 9088070803139334820L;

      @Override
      protected void configurationChanged() {
        changed(true);
      }
    };

    generalScrollPane.setViewportView(generalSettingsPanel);

    // TODO tabbedPane.addTab("Map", new MapPanel());

    final JPanel panel = new JPanel();
    final GridBagConstraints gbc_panel = new GridBagConstraints();
    gbc_panel.fill = GridBagConstraints.BOTH;
    gbc_panel.gridx = 0;
    gbc_panel.gridy = 1;
    contentPane.add(panel, gbc_panel);
    final GridBagLayout gbl_panel = new GridBagLayout();
    gbl_panel.columnWidths = new int[]{174, 49, 0, 32, 0};
    gbl_panel.rowHeights = new int[]{27, 0};
    gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
    addTrackButton.addActionListener(addTrackActionListener);


    resetGeneralSettings = e -> {
      try {
        generalSettingsPanel.updateProperty(8,30);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    };

    saveGeneralSettings = e -> {
      try {
        generalSettingsPanel.updateProperty(-1,-1);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    };


    final JButton resetSettings = new JButton("Reset settings");
    final GridBagConstraints resetSettingsButton = new GridBagConstraints();
    resetSettingsButton.anchor = GridBagConstraints.NORTHWEST;
    resetSettingsButton.insets = new Insets(0, 0, 0, 5);
    resetSettingsButton.gridx = 2;
    resetSettingsButton.gridy = 0;
    panel.add(resetSettings, resetSettingsButton);
    addTrackButton.addActionListener(resetGeneralSettings);

    final JButton saveSettings = new JButton("Save settings");
    final GridBagConstraints saveSettingsButton = new GridBagConstraints();
    saveSettingsButton.anchor = GridBagConstraints.NORTHWEST;
    saveSettingsButton.insets = new Insets(0, 0, 0, 5);
    saveSettingsButton.gridx = 3;
    saveSettingsButton.gridy = 0;
    panel.add(saveSettings, saveSettingsButton);
    addTrackButton.addActionListener(saveGeneralSettings);

//		final JButton btnComputeBbox = new JButton("Compute BBox");
//		final GridBagConstraints gbc_btnComputeBbox = new GridBagConstraints();
//		gbc_btnComputeBbox.anchor = GridBagConstraints.NORTHWEST;
//		gbc_btnComputeBbox.insets = new Insets(0, 0, 0, 5);
//		gbc_btnComputeBbox.gridx = 2;
//		gbc_btnComputeBbox.gridy = 0;
//		panel.add(btnComputeBbox, gbc_btnComputeBbox);
//		btnComputeBbox.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(final ActionEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//		});

    renderButton = new JButton("Render");
    renderButton.setEnabled(false);
    final GridBagConstraints gbc_renderButton = new GridBagConstraints();
    gbc_renderButton.anchor = GridBagConstraints.NORTHWEST;
    gbc_renderButton.gridx = 4;
    gbc_renderButton.gridy = 0;
    panel.add(renderButton, gbc_renderButton);
    renderButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (swingWorker != null) {
          swingWorker.cancel(false);
          return;
        }

        final Configuration cfg;
        try {
          cfg = createConfiguration();
          if (cfg.getOutput().exists()) {
            final String message = String.format("A file with the name \"%s\" already exists.\nDo you really want to overwrite this file?", cfg.getOutput());
            final int result = JOptionPane.showConfirmDialog(MainFrame.this, message, "Warning", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) {
              return;
            }
          }
        } catch (final UserException ex) {
          ex.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, "Configuration error:\n" + ex.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        swingWorker = new SwingWorker<Void, String>() {
          @Override
          protected Void doInBackground() throws Exception {
            new Renderer(cfg).render(new RenderingContext() {
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
            renderButton.setText("Render");

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
        renderButton.setText("Cancel");
        swingWorker.execute();
      }
    });

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        if (!changed || JOptionPane.showConfirmDialog(MainFrame.this,
            "There are unsaved changes. Close anyway?", "Error", JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION) {
          System.exit(0);
        }

        if (swingWorker != null && !swingWorker.isDone()) {
          swingWorker.cancel(false);
        }
      }
    });
  }

  private void pupulateOpenRecentMenu() {
    openRecent.removeAll();
    getRecentFiles().forEach(file -> {
          JMenuItem item = new JMenuItem(file.getName());
          openRecent.add(item);
          item.addActionListener(e -> openFile(file));
        }
    );
  }

  private void openFile(final File file) {
    try {
      final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
      final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      unmarshaller.setAdapter(new FileXmlAdapter(file.getParentFile()));
      setConfiguration((Configuration) unmarshaller.unmarshal(file));
      MainFrame.this.file = file;
      addRecentFile(file);
    } catch (final JAXBException e1) {
      e1.printStackTrace();
      JOptionPane.showMessageDialog(MainFrame.this, "Error opening configuration: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }


  private void addRecentFile(File file) {
    final String result = Stream.concat(Stream.of(file), getRecentFiles().stream())
                                .distinct()
                                .limit(5)
                                .map(File::getAbsolutePath)
                                .collect(Collectors.joining(RECENT_FILE_DELIMITER));

    getPreferences().put(RECENT_FILES, result);

    pupulateOpenRecentMenu();
  }

  private List<File> getRecentFiles() {
    return Arrays.stream(getPreferences().get(RECENT_FILES, "").split(RECENT_FILE_DELIMITER))
                 .map(File::new)
                 .filter(File::exists)
                 .collect(Collectors.toList());
  }

  private Preferences getPreferences() {
    return Preferences.userRoot().node("app/gpx-animator");
  }

  private void changed(final boolean changed) {
    this.changed = changed;
    setTitle(TITLE + (changed ? " (*)" : ""));
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

      @Override
      protected void labelChanged(final String label) {
        tabbedPane.setTitleAt(tabbedPane.indexOfComponent(trackScrollPane), label == null || label.isEmpty() ? "Track" : label);
      }
    };

    tabbedPane.addTab("Track", trackScrollPane);
    trackScrollPane.setViewportView(trackSettingsPanel);
    tabbedPane.setSelectedComponent(trackScrollPane);
    trackSettingsPanel.setConfiguration(tc);

    renderButton.setEnabled(true);

    changed(true);
  }

  private void afterRemove() {
    if (tabbedPane.getTabCount() == 1) {
      renderButton.setEnabled(false);
    }
    changed(true);
  }

  private void saveAs() {
    final String lastCwd = prefs.get(PREF_LAST_CWD, null);
    fileChooser.setCurrentDirectory(new File(lastCwd == null ? System.getProperty("user.dir") : lastCwd));
    fileChooser.setSelectedFile(new File("")); // to forget previous file name
    if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      prefs.put(PREF_LAST_CWD, file.getParent());

      if (!file.getName().endsWith(PROJECT_FILENAME_SUFFIX)) {
        file = new File(file.getPath() + PROJECT_FILENAME_SUFFIX);
      }
      save(file);
    }
  }

  private void save(final File file) {
    try {
      try {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setAdapter(new FileXmlAdapter(file.getParentFile()));
        marshaller.marshal(createConfiguration(), file);
        MainFrame.this.file = file;
        changed(false);
        addRecentFile(file);
      } catch (final JAXBException e) {
        throw new UserException(e.getMessage(), e);
      }
    } catch (final UserException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Error saving configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

}
