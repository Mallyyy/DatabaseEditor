package com.editor;

import java.awt.*;
import javax.swing.*;

class FadingPanel extends JPanel {
  private float alpha = 0.0f;

  public FadingPanel() {
    setOpaque(false);
  }

  public void setAlpha(float alpha) {
    this.alpha = Math.max(0f, Math.min(1f, alpha));
    repaint();
  }

  public float getAlpha() {
    return alpha;
  }

  @Override
  protected void paintComponent( Graphics g) {
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2d.setColor(getBackground());
    g2d.fillRect(0, 0, getWidth(), getHeight());
    g2d.dispose();
    super.paintComponent(g);
  }

}

