package com.editor;

import java.awt.*;
import javax.swing.*;

public class Loader extends JPanel {
  public static Timer fadeInTimer;
  public static Timer fadeOutTimer;
  public static JPanel loadingOverlay;
  public static JLabel loadingLabel;
  public static JProgressBar spinnerBar;
  public static void fadeInOverlay() {
    if (fadeOutTimer != null && fadeOutTimer.isRunning()) {
      fadeOutTimer.stop();
    }
    if (fadeInTimer != null && fadeInTimer.isRunning()) {
      return; // Already fading in
    }

    loadingOverlay.setVisible(true);
    fadeInTimer = new Timer(15, null);
    fadeInTimer.addActionListener(e -> {
      float alpha = ((FadingPanel) loadingOverlay).getAlpha();
      if (alpha < 1f) {
        ((FadingPanel) loadingOverlay).setAlpha(Math.min(1f, alpha + 0.05f));
      } else {
        ((FadingPanel) loadingOverlay).setAlpha(1f);
        fadeInTimer.stop();
      }
    });
    fadeInTimer.start();
  }

  public static void fadeOutOverlay() {
    if (fadeInTimer != null && fadeInTimer.isRunning()) {
      fadeInTimer.stop();
    }
    if (fadeOutTimer != null && fadeOutTimer.isRunning()) {
      return; // Already fading out
    }

    fadeOutTimer = new Timer(15, null);
    fadeOutTimer.addActionListener(e -> {
      float alpha = ((FadingPanel) loadingOverlay).getAlpha();
      if (alpha > 0f) {
        ((FadingPanel) loadingOverlay).setAlpha(Math.max(0f, alpha - 0.05f));
      } else {
        ((FadingPanel) loadingOverlay).setAlpha(0f);
        loadingOverlay.setVisible(false);
        fadeOutTimer.stop();
      }
    });
    fadeOutTimer.start();
  }
  public static void showLoadingOverlay( String message ) {
    loadingLabel.setText(message);
    loadingLabel.setForeground(new Color(0xFFFFFF));

    boolean showSpinner = message.toLowerCase().contains("loading") ||
                          message.toLowerCase().contains("connecting") ||
                          message.toLowerCase().contains("saving");
    if (spinnerBar != null) {
      spinnerBar.setVisible(showSpinner);
    }
    loadingOverlay.setBounds(0, 0, loadingOverlay.getWidth(), loadingOverlay.getHeight());
    Loader.fadeInOverlay();
  }

  public static void hideLoadingOverlay() {
    Loader.fadeOutOverlay();
  }
}
