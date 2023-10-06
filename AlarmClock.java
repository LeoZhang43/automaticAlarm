import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AlarmClock extends JFrame {
    private Timer timer;
    private int restStudyTime = 10;
    private int restRestTime = 0;
    private int count = 0;
    private int remainingSeconds;
    private boolean paused = false;
    private boolean study = true;

    private JLabel studyLabel;
    private JLabel restLabel;
    private JButton startButton;
    private JButton pauseButton;
    private JButton skipButton;
    private JButton restButton;
    private JLabel numberCount;

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(AlarmClock.class);

    public AlarmClock() {
        setTitle("Alarm Clock");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));

        studyLabel = new JLabel("Study time remaining: 60:00", JLabel.CENTER);
        restLabel = new JLabel("Rest time remaining: 0:00", JLabel.CENTER);
        startButton = new JButton("Start");
        pauseButton = new JButton("Pause");
        skipButton = new JButton("Skip");
        restButton = new JButton("Rest");
        numberCount = new JLabel("Today you have studied 0 hour(s)", JLabel.CENTER);

        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        skipButton.setEnabled(false);
        restButton.setEnabled(false);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer == null) {
                    startTimer();
                    startButton.setEnabled(false);
                    pauseButton.setEnabled(true);
                    skipButton.setEnabled(false);
                }
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer != null) {
                    if (!paused) {
                        paused = true;
                        pauseButton.setText("Resume");
                        timer.cancel();
                    } else {
                        paused = false;
                        pauseButton.setText("Pause");
                        startTimer();
                    }
                }
            }
        });

        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                study = true;
                pcs.firePropertyChange("skipListener", null, study);
                pcs.firePropertyChange("restRestTime", null, restRestTime);
                paused = false;
                pauseButton.setText("Pause");
                timer.cancel();
                startTimer();
            }
        });

        restButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                study = false;
                pcs.firePropertyChange("skipListener", null, study);
                paused = false;
                pauseButton.setText("Pause");
                timer.cancel();
                startTimer();
            }
        });

        PropertyChangeListener restListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (restRestTime > 0 && study) {
                    restButton.setEnabled(true);
                } else {
                    restButton.setEnabled(false);
                }
            }
        };
        pcs.addPropertyChangeListener("restRestTime", restListener);

        PropertyChangeListener skipListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (!study) {
                    skipButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                } else {
                    skipButton.setEnabled(false);
                    pauseButton.setEnabled(true);
                }
            }
        };
        pcs.addPropertyChangeListener("skipListener", skipListener);

        JPanel timerPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        timerPanel.add(studyLabel);
        timerPanel.add(restLabel);
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(skipButton);
        buttonPanel.add(restButton);

        add(timerPanel);
        add(buttonPanel);
        add(numberCount);

        setVisible(true);
    }

    private void beep() {
        for (int i = 0; i < 5; i++) {
            Toolkit.getDefaultToolkit().beep();
            try {
                Thread.sleep(500); // Sleep for 1 second (1000 milliseconds) between beeps
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateCount(){
        count++;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run(){
                numberCount.setText("Today you have studied: " + count + " hours");
            }
        });
    }

    private void setCountToZero(){
        count = 0;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run(){
                numberCount.setText("Today you have studied: " + count + " hours");
            }
        });
    }

    private void showTimeIsUpDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                paused = false;
                pauseButton.setText("Pause");
                if(!study){
                    JOptionPane.showMessageDialog(null, "Time to rest!", "Alarm", JOptionPane.WARNING_MESSAGE);
                    restRestTime += 10;
                    pcs.firePropertyChange("restRestTime", null, restRestTime);
                    startTimer(); 
                }else{
                    startTimer(); 
                } 
                startButton.setEnabled(false);
                pauseButton.setEnabled(true);
            }
        });
    }

    private void startTimer() {
        timer = new Timer();
        if(study){
            remainingSeconds = restStudyTime;
        }else{
            remainingSeconds = restRestTime;
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingSeconds > 0) {
                    int minutes = remainingSeconds / 60;
                    int secs = remainingSeconds % 60;
                    if(study){
                        studyLabel.setText("Study time remaining: " + String.format("%02d:%02d", minutes, secs));
                        restStudyTime--;
                    }else{
                        restLabel.setText("Rest time remaining: " + String.format("%02d:%02d", minutes, secs));
                        restRestTime--;
                        pcs.firePropertyChange("restRestTime", null, restRestTime);
                    }
                    remainingSeconds--;
                } else {
                    if(study){
                        studyLabel.setText("Study time remaining: " + String.format("%02d:%02d", 0, 0));
                        restStudyTime = 16;
                    }else{
                        restLabel.setText("Rest time remaining: " + String.format("%02d:%02d", 0, 0));
                    }
                    timer.cancel();
                    timer = null;
                    beep();
                    if(study)updateCount();
                    study = !study;
                    pcs.firePropertyChange("skipListener", null, study);
                    showTimeIsUpDialog();
                }
            }
        }, 0, 1000);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AlarmClock();
            }
        });
    }
}
