package com.editor;


import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import javax.swing.*;

public class DatabaseLauncher {
  public static void main(String[] args) {

    SwingUtilities.invokeLater(() -> {
      try {
        URL url = new URL("https://morpheus-rsps.com/images/logo.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
      JFrame frame = new JFrame("Database Editor");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(1015, 728);
      frame.setLocationRelativeTo(null);
      FlatLaf.setup(new FlatDarkLaf());
      UIManager.setLookAndFeel(new FlatDarkLaf());
      frame.setIconImage(img);
      frame.setContentPane(new DatabasePanel());
      frame.setVisible(true);
      } catch (UnsupportedLookAndFeelException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (FontFormatException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
