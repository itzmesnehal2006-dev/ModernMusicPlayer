import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javazoom.jl.player.Player;
import javazoom.jl.decoder.JavaLayerException;

public class ModernMusicPlayer extends JFrame {
    // UI Components
    JButton playBtn, pauseBtn, stopBtn, nextBtn, prevBtn, browseBtn;
    JLabel songLabel, albumArtLabel;
    JList<String> playlistUI;
    DefaultListModel<String> playlistModel;
    JProgressBar progressBar;
    JSlider volumeSlider;
    JFileChooser fileChooser;

    // Playback
    ArrayList<File> playlist = new ArrayList<>();
    int currentSongIndex = 0;
    Player player;
    Thread playThread;

    public ModernMusicPlayer() {
        setTitle("🎵 Modern Music Player");
        setSize(600, 400);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try { UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf"); } catch(Exception ignored) {}

        // North Panel: Song Info + Album Art
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        songLabel = new JLabel("No song selected", SwingConstants.CENTER);
        songLabel.setFont(new Font("Arial", Font.BOLD, 16));
        albumArtLabel = new JLabel();
        albumArtLabel.setPreferredSize(new Dimension(100,100));
        albumArtLabel.setBorder(BorderFactory.createLineBorder(Color.gray));
        topPanel.add(songLabel, BorderLayout.CENTER);
        topPanel.add(albumArtLabel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Playlist + Progress
        playlistModel = new DefaultListModel<>();
        playlistUI = new JList<>(playlistModel);
        JScrollPane scrollPane = new JScrollPane(playlistUI);
        scrollPane.setPreferredSize(new Dimension(200,200));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        JPanel centerPanel = new JPanel(new BorderLayout(5,5));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(progressBar, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // South Panel: Controls
        JPanel controlPanel = new JPanel(new GridLayout(2,1,5,5));

        JPanel buttonsPanel = new JPanel(new GridLayout(1,6,5,5));
        browseBtn = new JButton("📂 Browse");
        prevBtn = new JButton("⏮ Prev");
        playBtn = new JButton("▶ Play");
        pauseBtn = new JButton("⏸ Pause");
        stopBtn = new JButton("⏹ Stop");
        nextBtn = new JButton("⏭ Next");

        for(JButton b: new JButton[]{browseBtn, prevBtn, playBtn, pauseBtn, stopBtn, nextBtn}) buttonsPanel.add(b);

        volumeSlider = new JSlider(0,100,80);
        volumeSlider.setMajorTickSpacing(20);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);

        controlPanel.add(buttonsPanel);
        controlPanel.add(volumeSlider);

        add(controlPanel, BorderLayout.SOUTH);

        // File chooser
        fileChooser = new JFileChooser();

        // Event Listeners
        browseBtn.addActionListener(e -> chooseFiles());
        playBtn.addActionListener(e -> playSelectedSong());
        stopBtn.addActionListener(e -> stopSong());
        pauseBtn.addActionListener(e -> pauseSong());
        nextBtn.addActionListener(e -> nextSong());
        prevBtn.addActionListener(e -> prevSong());
        playlistUI.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) {
                currentSongIndex = playlistUI.getSelectedIndex();
            }
        });

        setVisible(true);
    }

    // File selection
    void chooseFiles() {
        fileChooser.setMultiSelectionEnabled(true);
        if(fileChooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for(File f: files) {
                playlist.add(f);
                playlistModel.addElement(f.getName());
            }
        }
    }

    // Play song
    void playSelectedSong() {
        if(playlist.isEmpty()) return;
        if(currentSongIndex >= playlist.size()) currentSongIndex = 0;
        playSong(playlist.get(currentSongIndex));
    }

    void playSong(File songFile) {
        stopSong(); // Stop previous
        songLabel.setText("Playing: "+songFile.getName());
        albumArtLabel.setIcon(new ImageIcon(new ImageIcon("default_album.png").getImage().getScaledInstance(100,100,Image.SCALE_SMOOTH))); // placeholder
        playThread = new Thread(() -> {
            try(FileInputStream fis = new FileInputStream(songFile);
                BufferedInputStream bis = new BufferedInputStream(fis)) {

                player = new Player(bis);
                player.play();
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this,"Cannot play file!");
            }
        });
        playThread.start();
    }

    void stopSong() {
        if(player != null) { player.close(); player = null; }
    }

    void pauseSong() { stopSong(); /* JLayer does not support native pause */ }

    void nextSong() { currentSongIndex++; if(currentSongIndex>=playlist.size()) currentSongIndex=0; playSelectedSong(); }
    void prevSong() { currentSongIndex--; if(currentSongIndex<0) currentSongIndex=playlist.size()-1; playSelectedSong(); }

    public static void main(String[] args) {
        new ModernMusicPlayer();
    }
}
