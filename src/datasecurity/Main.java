/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datasecurity;

/**
 *
 * @author WorkSpace
 */
public class Main {
    public static void main(String[] args) {
       try {
            // Set System L&F
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName());
        }
        catch (javax.swing.UnsupportedLookAndFeelException e) {
           // handle exception
        }
        catch (ClassNotFoundException e) {
           // handle exception
        }
        catch (InstantiationException e) {
           // handle exception
        }
        catch (IllegalAccessException e) {
           // handle exception
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Frame Frame = new Frame();
                Frame.setTitle("Клавиатурный почерк");
                try{
                    java.net.URL imgURL = getClass().getResource("favicon.gif");
                    java.awt.Image img = java.awt.Toolkit.getDefaultToolkit().getImage( imgURL );
                    Frame.setIconImage(img);
                }catch(Exception e){}
                Frame.setLocationRelativeTo(null);
                Frame.setVisible(true);
                Frame.assignListeners();
            }

        });
    }
}
