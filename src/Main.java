/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Homework 2
 * Description: Implementation of several basic spatial filters
 */

import javax.swing.*;

class Main {
   private static final boolean TEST = true;
   
   public static void main(String[] args) {
      if (!TEST) {
         SwingUtilities.invokeLater(MainWindow::new);
      } else {
         SwingUtilities.invokeLater(MultiChannelWindow::new);
      }
      
   }
}
