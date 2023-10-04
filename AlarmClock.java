import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmClock extends JFrame {
    private Timer timer;
    private int remainingSeconds;
    private int count = 0;
    private boolean paused = false;
    private int oneHour = 60*1;
    private int tenMin = 10*1;
    private boolean study = true;


    private JLabel countdownLabel;
    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;
    private JLabel numberCount;

    public AlarmClock() {
        setTitle("Java Alarm Clock");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));

        countdownLabel = new JLabel("Study time remaining: 60:00", JLabel.CENTER);
        startButton = new JButton("Start");
        pauseButton = new JButton("Pause");
        resetButton = new JButton("Reset");
        numberCount = new JLabel("Today you have studied 0 hour(s)", JLabel.CENTER);

        startButton.setEnabled(true);
        pauseButton.setEnabled(false);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer == null) {
                    startTimer(oneHour);
                    startButton.setEnabled(false);
                    pauseButton.setEnabled(true);
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
                        startTimer(remainingSeconds);
                    }
                }
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                setCountToZero();
                countdownLabel.setText("Time remaining: 60:00");
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                paused = false;
                pauseButton.setText("Pause");
            }
        });

        add(countdownLabel);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resetButton);

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
                    startTimer(tenMin); // Start a new 60-minute timer
                }else{
                    startTimer(oneHour); // Start a new 60-minute timer
                } 
                startButton.setEnabled(false);
                pauseButton.setEnabled(true);
            }
        });
    }

    private void startTimer(int seconds) {
        timer = new Timer();
        remainingSeconds = seconds;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingSeconds > 0) {
                    int minutes = remainingSeconds / 60;
                    int secs = remainingSeconds % 60;
                    if(study){
                        countdownLabel.setText("Study time remaining: " + String.format("%02d:%02d", minutes, secs));
                    }else{
                        countdownLabel.setText("Rest time remaining: " + String.format("%02d:%02d", minutes, secs));
                    }
                    remainingSeconds--;
                } else {
                    countdownLabel.setText("Time remaining: " + String.format("%02d:%02d", 0, 0));
                    timer.cancel();
                    timer = null;
                    beep();
                    if(study)updateCount();
                    study = !study;
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
