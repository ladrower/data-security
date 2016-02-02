/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Frame.java
 *
 * Created on 01.03.2011, 16:07:12
 */

package datasecurity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author WorkSpace
 */
public class Frame extends javax.swing.JFrame {

    private String text = new String();
    private char[] textArray;
    private StringBuilder enteredText;
    private char[] enteredTextArray;
    private int symbols;
    private int errors;
    private long startTime;
    private long time;
    private long halfTime;
    private Timer timer;
    private JFileChooser fileChooser = new JFileChooser();
    private ArrayList<TestResults> results = new ArrayList<TestResults>();
    
    private long expectedHalfTime = 0;
    private long expectedTime = 0;
    private double expectederrorsPerSymbols = 0.0;




    /** Creates new form Frame */
    public Frame() {
        initComponents();
    }

    public void assignListeners(){
        rootPane.setFocusable(true);
        rootPane.addKeyListener(new KeyAdapter() {

        @Override
        public void keyReleased(KeyEvent e) {
                    if( text.isEmpty() )
                          return;

                    boolean isNormalSymbol = false;
                    boolean backspace = false;
                    int keyCode = e.getKeyCode();
                    char keyChar = e.getKeyChar();

                    if( startTime == 0 )
                        startTimer();

                    if( symbols < 0 )
                        symbols = 0;

                    String buffer = Paper.getText();

                    if( buffer.length() > 42  ){
                        buffer = buffer.substring( 6 , buffer.length() - 37 );
                    }
     
                    enteredText = new StringBuilder( buffer );
                    String Appendex = null;

                    switch( keyCode ){

                        case 32:
                            Appendex = "&nbsp;";
                            isNormalSymbol = true;
                            break;
                        case 10:
                            Appendex = "<br>";
                            isNormalSymbol = true;
                            break;
                        case 16:
                        case 17:
                        case 18:
                        case 20:
                            isNormalSymbol = false;
                            break;
                        case 8:
                            backspace = true;
                            isNormalSymbol = false;
                            break;
                        default:
                            Appendex = keyChar+"";
                            isNormalSymbol = true;

                    }

                    if( backspace ){
                        symbols--;
                        enteredText = new StringBuilder();
                        for (int i = 0; i < symbols; i++) {

                            if( enteredTextArray[i] == ' ' )
                                Appendex = "&nbsp;";

                            else if(enteredTextArray[i] == '\n')
                                Appendex = "<br>";
                            else
                                Appendex = enteredTextArray[i]+"";

                            if( textArray[i] != enteredTextArray[i] )
                                Appendex = "<font color=\"red\" style=\"text-decoration:underline\">" + Appendex + "</font>";
                            
                            enteredText.append( Appendex );
                            
                        }

                    }

                    if( symbols == textArray.length/2 ){
                        fixFirstHalfTime();
                    }

                    
                    if( symbols >= textArray.length ){
                        System.out.println("достигнут лимит количества символов");
                        return;
                     }

                    if( isNormalSymbol ){
                        symbols++;
                        if( textArray[symbols-1] != keyChar ){
                            if( timer.isRunning() ){
                            errors++;
                            java.awt.Toolkit.getDefaultToolkit().beep();
                            ErrorsLabel.setText("<html>Вы допустили <b>" + errors + " " + errorsEnding(errors) +"</b>" );
                            }
                            Appendex = "<font color=\"red\" style=\"text-decoration:underline\">" + Appendex + "</font>";
                        }
                        enteredText.append(Appendex);
                    }

                    if( symbols == textArray.length ){
                        stopTimer();
                    }
                

                    if( symbols >= 0 && symbols <= enteredTextArray.length && isNormalSymbol )
                        enteredTextArray[symbols-1] = keyChar;


                    enteredText.append("<font color=\"#c0c0c0\">|</font>");
                    refreshPaper( enteredText.toString() );
                    indicatePosition();

                }

         });


    }

    private void refreshPaper( String typed ){
        Paper.setText("<html>" + typed + "</html>");
        Paper.setPreferredSize(Paper.getSize());
    }

    private void indicatePosition(){
        if( symbols < 0 )
            return;
        textSource.setText("<html><font color=\"#c0c0c0\" style=\"text-decoration:underline\">" + text.substring(0, symbols)
                + "</font>"
                + text.substring(symbols, text.length() ) + "</html>");
    }

    private void startTimer(){
        startTime = System.currentTimeMillis();
        Informer.setText("<html><b><font color=\"green\">Время пошло!</font></b></html>");
        timer = new Timer(1000, new ActionListener()
          {
              public void actionPerformed(ActionEvent e)
              {
                updateTime();
              }
          } );
         timer.setRepeats(true);
         timer.start();


    }

    private void stopTimer(){
        if( !timer.isRunning() )
            return;

        timer.stop();
        time = System.currentTimeMillis() - startTime;
        int minutes = (int) (time/1000)/60;
        int seconds = (int) (time/1000)%60;

        Informer.setText("<html><b><font color=\"green\">Ваше время: " + ( (minutes == 0)?"":minutes + "мин." )
                + ( (seconds == 0)?"":" " + seconds + "сек." ) + "</font></b></html>" );

        saveParamsBtn.setEnabled(true);

        addResultsToCollection();
        checkAccess();

    }

    private void checkAccess(){
        boolean authorized = true;
        TestResults TestResults = results.get( results.size() - 1 );
        if( expectedHalfTime > ( TestResults.firstHalfTime + TestResults.firstHalfTime * 0.1 )
                || expectedHalfTime < ( TestResults.firstHalfTime - TestResults.firstHalfTime * 0.1 ) ){
            authorized = false;
            System.out.println("авторизация не пройдена по половине времени "
                    + expectedHalfTime + " " + TestResults.firstHalfTime );
        }


        if( expectedTime > ( TestResults.fullTime + TestResults.fullTime * 0.1 )
                || expectedTime < ( TestResults.fullTime - TestResults.fullTime * 0.1 ) ){
            authorized = false;
            System.out.println("авторизация не пройдена по полному времени" );

        }

            
        if( expectederrorsPerSymbols > ( TestResults.errorsPerSymbols + 0.01 )
                || expectederrorsPerSymbols < ( TestResults.errorsPerSymbols - 0.01 ) ){
            authorized = false;
            System.out.println("авторизация не пройдена по соотношению кол-ва ошибок к длине текста" );
        }

        System.out.println("HalfTime " + TestResults.firstHalfTime );
        System.out.println("fullTime " + TestResults.fullTime );
        System.out.println("errorsPerSymbols " + TestResults.errorsPerSymbols );
        System.out.println("Результат авторизации " + authorized );

        if( authorized )
            JOptionPane.showMessageDialog(null,"Добро пожаловать!","Авторизация успешна!" , 1);
        else
            JOptionPane.showMessageDialog(null,"Ваши биометрические показатели не совпадают со статистическими","Доступ запрещен!" , 2);

    }

    private void updateTime(){
        long diff = (System.currentTimeMillis() - startTime )/1000;
        int minutes = (int) diff/60;
        int seconds = (int) diff%60;

        Informer.setText("Ваше время: " + ( (minutes == 0)?"":minutes + "мин." )
                + ( (seconds == 0)?"":" " + seconds + "сек." ) );
    }

    private void fixFirstHalfTime(){
        halfTime = (System.currentTimeMillis() - startTime);
        System.out.println( "Время половины набора = " + halfTime/1000 );
    }

    private void prepareProgram(){
        if( text.isEmpty() ){
            Informer.setText(null);
            return;
        }

        symbols = 0;
        errors = 0;
        startTime = 0;
        try{
            timer.stop();
        }
        catch( NullPointerException e ){
            System.out.println("Timer is not started yet " + e );
        }
        saveParamsBtn.setEnabled(false);

        Paper.setText("");
        Informer.setText("Можете приступать к набору текста!");
        ErrorsLabel.setText("");

        enteredTextArray = new char[text.length()];
        explodeString();

        textSource.setText("<html>" + text.replace("\n", "<br/>").replace(" ", "&nbsp;") + "</html>");
        
        rootPane.requestFocus();
    }

    private void explodeString(){

        textArray = new char[text.length()];
        textArray = text.toCharArray();

    }


    public void loadTextData(){

        if( fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ){

            try {
                String filename = fileChooser.getSelectedFile().getPath();
                ObjectInput in = new ObjectInputStream(new FileInputStream(filename));
                text =  in.readObject().toString();
                in.close();
            } catch (FileNotFoundException e) {
                System.out.println("file not found ERROR: " + e + "\n");
            } catch (ClassNotFoundException e) {
                System.out.println("class not found ERROR: " + e + "\n");
            } catch (IOException e) {
                System.out.println("IO ERROR: " + e + "\n");
            }

        }

        prepareProgram();

    }


    public boolean saveData( String text ){

        if( fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION ){

            try {
                String filename = fileChooser.getSelectedFile().getPath();
                System.out.print("Saving to " + filename + "\n" + text + "\n");
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(filename));
                out.writeObject( text );
                out.close();

            } catch (IOException e) {
                System.out.println("IO ERROR: " + e + "\n");
            }

            return true;

        }
        return false;
    }

    public void createNewTextData(){
        final JFrame NTDFrame = new JFrame("Создание нового текстового файла");
        final JPanel NTDPanel = new JPanel();
        final JPanel butPanel = new JPanel();
        final JTextArea textArea = new JTextArea();
        final JButton saveButton = new JButton("Сохранить в файл");

        saveButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if( !textArea.getText().trim().isEmpty() ){
                            if( saveData( textArea.getText().trim().toString() ) )
                                NTDFrame.setVisible(false);
                        }
                        else{
                            textArea.setText("");
                        }
                    }
        });

        textArea.setPreferredSize(new Dimension(400, 200));
        textArea.setFont(new Font("Verdana", Font.PLAIN, 12));
        
        NTDPanel.add(textArea);
        butPanel.add(saveButton);
        NTDFrame.getContentPane().setLayout(new BorderLayout());
        NTDFrame.getContentPane().add(butPanel, BorderLayout.SOUTH);
        NTDFrame.getContentPane().add(NTDPanel, BorderLayout.CENTER);

        NTDFrame.setPreferredSize(new Dimension(480, 240));
        NTDFrame.pack();
        NTDFrame.setLocationRelativeTo(null);
        NTDFrame.setVisible(true);

    }


    private String errorsEnding(int count){

        count = count % 100;

	if ( count < 15 && count > 10){
            return "ошибок";
	}else{
            count = count % 10;

            switch ( count ) {

                case 1:
                    return "ошибку";
                case 2:
                case 3:
                case 4:
                    return "ошибки";
                case 0:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                    return "ошибок";

           }
        }
	return "ошибок";
   }

    public void addResultsToCollection(){

        double errorsPerSymbols = errors/enteredTextArray.length;
        TestResults testResults = new TestResults( halfTime , time , errorsPerSymbols );
        results.add(testResults);

    }

    public void saveTestResults(){


        if( fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION ){

            try {
                String filename = fileChooser.getSelectedFile().getPath();
                System.out.print("Saving to " + filename);
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(filename));
                out.writeObject( results );
                out.close();

            } catch (IOException e) {
                System.out.println("IO ERROR: " + e + "\n");
            }

        }

    }
    
    public void openTestResults() {
        
        if( fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ){

            ObjectInput in = null;
            
            try {
                String filename = fileChooser.getSelectedFile().getPath();
                in = new ObjectInputStream(new FileInputStream(filename));
                results = (ArrayList<TestResults>)in.readObject();
                
                int resultsSize = results.size();
                if (resultsSize == 0) {
                    throw new Exception("Test results file is empty");
                } 
                
                long firstHalfTime = 0;
                long fullTime = 0;
                double errorsPerSymbols = 0;
                
                for (TestResults result : results) {
                    firstHalfTime += result.firstHalfTime;
                    fullTime += result.fullTime;
                    errorsPerSymbols += result.errorsPerSymbols;
                }
                
                expectedHalfTime = firstHalfTime/resultsSize;
                expectedTime = fullTime/resultsSize;
                expectederrorsPerSymbols = errorsPerSymbols/resultsSize;
                
                System.out.println("Expectations: expectedHalfTime (" 
                        + expectedHalfTime + "), expectedTime(" 
                        + expectedTime +"), expectederrorsPerSymbols("
                        + expectederrorsPerSymbols + ") \n");
                
                
            } catch (FileNotFoundException e) {
                System.out.println("file not found ERROR: " + e + "\n");
            } catch (ClassNotFoundException e) {
                System.out.println("class not found ERROR: " + e + "\n");
            } catch (IOException e) {
                System.out.println("IO ERROR: " + e + "\n");
            } catch (Exception e) {
                System.out.println("IO ERROR: " + e + "\n");
            } finally {
                if (in != null) {
                    try{
                        in.close();   
                    } catch (IOException e) {
                        System.out.println("IO ERROR: " + e + "\n");
                    }
                }
            }

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Paper = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        textSource = new javax.swing.JLabel();
        ErrorsLabel = new javax.swing.JLabel();
        Informer = new javax.swing.JLabel();
        saveParamsBtn = new javax.swing.JButton();
        openTestResults = new java.awt.Button();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Paper.setBackground(new java.awt.Color(255, 255, 255));
        Paper.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        Paper.setAutoscrolls(true);
        Paper.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Paper.setOpaque(true);
        Paper.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Текст для прохождения теста", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(102, 102, 102))); // NOI18N

        textSource.setText("Откройте файл текста (CTRL+O)...");
        textSource.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        textSource.setAutoscrolls(true);
        textSource.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(textSource, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(textSource, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
        );

        saveParamsBtn.setText("Сохранить мои результаты");
        saveParamsBtn.setEnabled(false);
        saveParamsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveParamsBtnActionPerformed(evt);
            }
        });

        openTestResults.setActionCommand("openTestResults");
        openTestResults.setLabel("Загрузить мои результаты");
        openTestResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTestResultsActionPerformed(evt);
            }
        });

        jMenu1.setText("Меню");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Создать файл текста");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Открыть файл текста");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(32, 32, 32))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Paper, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(Informer, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(ErrorsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(openTestResults, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(saveParamsBtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Informer, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ErrorsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(openTestResults, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(saveParamsBtn))
                    .addComponent(Paper, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
                .addGap(21, 21, 21))
        );

        openTestResults.getAccessibleContext().setAccessibleName("openTestResults");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        loadTextData();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        createNewTextData();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void saveParamsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveParamsBtnActionPerformed
        saveTestResults();
    }//GEN-LAST:event_saveParamsBtnActionPerformed

    private void openTestResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTestResultsActionPerformed
        openTestResults();
    }//GEN-LAST:event_openTestResultsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ErrorsLabel;
    private javax.swing.JLabel Informer;
    private javax.swing.JLabel Paper;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private java.awt.Button openTestResults;
    private javax.swing.JButton saveParamsBtn;
    private javax.swing.JLabel textSource;
    // End of variables declaration//GEN-END:variables

}
