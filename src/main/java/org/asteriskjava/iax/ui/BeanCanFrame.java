
package org.asteriskjava.iax.ui;


import org.asteriskjava.iax.util.DTMFGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;


public class BeanCanFrame extends JFrame {

    JPanel contentPane;
    JMenuBar jMenuBar1 = new JMenuBar();
    JMenu jMenuFile = new JMenu();
    JMenuItem jMenuFileExit = new JMenuItem();
    JMenu jMenuHelp = new JMenu();
    JMenuItem jMenuHelpAbout = new JMenuItem();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    JPanel jPanel3 = new JPanel();
    JPanel jPanel4 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JTextField dialString = new JTextField();
    GridLayout gridLayout1 = new GridLayout();
    JButton jButton1 = new JButton();
    JButton jButton2 = new JButton();
    JButton jButton3 = new JButton();
    JButton jButton4 = new JButton();
    JButton jButton5 = new JButton();
    JButton jButton6 = new JButton();
    JButton jButton7 = new JButton();
    JButton jButton8 = new JButton();
    JButton jButton9 = new JButton();
    JButton jButton0 = new JButton();
    JButton jButton11 = new JButton();
    JButton jButton12 = new JButton();
    BorderLayout borderLayout3 = new BorderLayout();
    JLabel status = new JLabel();
    JButton act = new JButton();
    JButton clear = new JButton();
    JPanel jPanel5 = new JPanel();
    GridLayout gridLayout2 = new GridLayout();
    JPanel jPanel6 = new JPanel();
    JPanel jPanel7 = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    BorderLayout borderLayout5 = new BorderLayout();
    BorderLayout borderLayout6 = new BorderLayout();

    //Construct the frame
    public BeanCanFrame() {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Component initialization
    private void jbInit() throws Exception {
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(borderLayout1);
        this.setSize(new Dimension(300, 200));
        this.setTitle("Integra CCS IaxJphone");
        this.setAlwaysOnTop(true);

        jPanel1.setLayout(borderLayout2);
        dialString.setText("");
        dialString.addActionListener(new BeanCanFrame_dialString_actionAdapter(this));
        jPanel3.setLayout(gridLayout1);
        gridLayout1.setColumns(3);
        gridLayout1.setRows(4);
        jButton1.setMnemonic('1');
        jButton1.setText("1");
        jButton1.addActionListener(new BeanCanFrame_jButton1_actionAdapter(this));
        jButton2.setMnemonic('2');
        jButton2.setText("2");
        jButton2.addActionListener(new BeanCanFrame_jButton2_actionAdapter(this));
        jButton3.setMnemonic('3');
        jButton3.setText("3");
        jButton3.addActionListener(new BeanCanFrame_jButton3_actionAdapter(this));
        jButton4.setText("4");
        jButton4.addActionListener(new BeanCanFrame_jButton4_actionAdapter(this));
        jButton5.setText("5");
        jButton5.addActionListener(new BeanCanFrame_jButton5_actionAdapter(this));
        jButton6.setText("6");
        jButton6.addActionListener(new BeanCanFrame_jButton6_actionAdapter(this));
        jButton7.setText("7");
        jButton7.addActionListener(new BeanCanFrame_jButton7_actionAdapter(this));
        jButton8.setText("8");
        jButton8.addActionListener(new BeanCanFrame_jButton8_actionAdapter(this));
        jButton9.setText("9");
        jButton9.addActionListener(new BeanCanFrame_jButton9_actionAdapter(this));
        jButton0.setText("0");
        jButton0.addActionListener(new BeanCanFrame_jButton0_actionAdapter(this));
        jButton11.setText("*");
        jButton11.addActionListener(new BeanCanFrame_jButton11_actionAdapter(this));
        jButton12.setAction(null);
        jButton12.setText("#");
        jButton12.addActionListener(new BeanCanFrame_jButton12_actionAdapter(this));
        jPanel2.setLayout(borderLayout3);
        status.setText("No Conectado");
        jPanel4.setOpaque(true);
        jPanel4.setLayout(borderLayout6);
        act.setText("Llamar");
        act.addActionListener(new BeanCanFrame_act_actionAdapter(this));
        contentPane.setActionMap(null);
        clear.setText("Borrar");
        clear.addActionListener(new BeanCanFrame_clear_actionAdapter(this));
        jPanel5.setLayout(gridLayout2);
        gridLayout2.setColumns(2);
        jPanel7.setLayout(borderLayout4);
        jPanel6.setLayout(borderLayout5);
        jMenuFile.add(jMenuFileExit);
        jMenuHelp.add(jMenuHelpAbout);
        jMenuBar1.add(jMenuFile);
        jMenuBar1.add(jMenuHelp);
        contentPane.add(jPanel1, BorderLayout.CENTER);
        jPanel1.add(jPanel2, BorderLayout.NORTH);
        jPanel2.add(dialString, BorderLayout.CENTER);
        jPanel1.add(jPanel3, BorderLayout.CENTER);
        jPanel3.add(jButton1, null);
        jPanel3.add(jButton2, null);
        jPanel3.add(jButton3, null);
        jPanel3.add(jButton4, null);
        jPanel3.add(jButton5, null);
        jPanel3.add(jButton6, null);
        jPanel3.add(jButton7, null);
        jPanel3.add(jButton8, null);
        jPanel3.add(jButton9, null);
        jPanel3.add(jButton11, null);
        jPanel3.add(jButton0, null);
        jPanel3.add(jButton12, null);
        contentPane.add(jPanel4, BorderLayout.SOUTH);
        jPanel4.add(status, BorderLayout.CENTER);
        contentPane.add(jPanel5, BorderLayout.EAST);
        jPanel5.add(jPanel7, null);
        jPanel5.add(jPanel6, null);
        jPanel2.add(act, BorderLayout.EAST);
        jPanel2.add(clear, BorderLayout.WEST);
        this.setJMenuBar(jMenuBar1);

    }


    void hold() {

    }

    void button_action(ActionEvent e) {
        String t = e.getActionCommand();
        String s = this.dialString.getText();
        s = s + t;
        dialString.setText(s);
    }

    void dialString_actionPerformed(ActionEvent e) {
        String num = dialString.getText();
        status.setText("Marcando: " + num);
    }

    void clear_actionPerformed(ActionEvent e) {
        dialString.setText("");
    }

    void act_actionPerformed(ActionEvent e) {
        dialString_actionPerformed(e);
    }


    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.exit(0);
        }
    }

    /**
     * Acciones de Botones
     *
     * @author Sebastian
     */

    class BeanCanFrame_jButton1_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton1_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("1");

            adaptee.button_action(e);
        }
    }

    class BeanCanFrame_jButton2_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton2_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("2");
            adaptee.button_action(e);
        }
    }

    class BeanCanFrame_jButton3_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton3_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("3");
            adaptee.button_action(e);
        }
    }

    class BeanCanFrame_jButton4_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton4_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("4");
            adaptee.button_action(e);
        }
    }

    class BeanCanFrame_jButton5_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton5_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("5");
            adaptee.button_action(e);
        }
    }

    class BeanCanFrame_jButton6_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton6_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("6");
            adaptee.button_action(e);
        }
    }

    class BeanCanFrame_jButton7_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton7_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("7");
            adaptee.button_action(e);
        }
    }

    class BeanCanFrame_jButton8_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton8_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("8");
            adaptee.button_action(e);
        }
    }

    class BeanCanFrame_jButton9_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton9_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("9");
            adaptee.button_action(e);
        }
    }

    class BeanCanFrame_jButton11_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton11_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("*");
            adaptee.button_action(e);

        }
    }

    class BeanCanFrame_jButton0_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton0_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("0");
            adaptee.button_action(e);
        }
    }

    class BeanCanFrame_jButton12_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_jButton12_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DTMFGenerator.playTone("#");
            adaptee.button_action(e);
        }
    }


    class BeanCanFrame_dialString_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_dialString_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            adaptee.dialString_actionPerformed(e);
        }
    }

    class BeanCanFrame_clear_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_clear_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            adaptee.clear_actionPerformed(e);
        }
    }

    class BeanCanFrame_act_actionAdapter implements java.awt.event.ActionListener {
        BeanCanFrame adaptee;

        BeanCanFrame_act_actionAdapter(BeanCanFrame adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            adaptee.act_actionPerformed(e);
        }
    }


}