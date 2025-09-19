package lab7_binarios_leonardolopez.Screen;

import lab7_binarios_leonardolopez.logica.ControladorGestion;
import lab7_binarios_leonardolopez.logica.GestionReproduccion;
import lab7_binarios_leonardolopez.logica.ListaEnlazadaMusic;
import lab7_binarios_leonardolopez.logica.Music;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class ReproductoMusica extends JFrame {

    private DefaultListModel<String> listModel;
    private JList<String> listaCanciones;
    public JLabel labelCancionActual;
    public JLabel labelImagen;
    private JButton btnPlay, btnPause, btnStop;
    private JButton btnAgregar, btnEliminar;
    private JProgressBar barraProgreso;
    private JLabel labelTiempoActual;
    private JLabel labelTiempoTotal;
    private Timer timerProgreso;

    private ListaEnlazadaMusic playlist;
    public int cancionActualIndex = -1;
    private long duracionTotalMs = 0;
    private long tiempoTranscurridoMs = 0;
    private long puntoDePausaMs = 0;
    private long inicioReproduccionSystemTime = 0;

    private Clip clipWAV; 
    private PausableMP3Player mp3Player; 

    public ReproductoMusica() {
        playlist = new ListaEnlazadaMusic();
        initComponents();
        cargarPlaylistEnLista();
    }

    private void initComponents() {
        setTitle("Reproductor de Música Corregido");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        labelCancionActual = new JLabel("Ninguna canción seleccionada", JLabel.CENTER);
        labelCancionActual.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelImagen = new JLabel("Sin imagen", JLabel.CENTER);
        labelImagen.setPreferredSize(new Dimension(250, 250));
        labelImagen.setBorder(BorderFactory.createEtchedBorder());
        panelSuperior.add(labelCancionActual, BorderLayout.NORTH);
        panelSuperior.add(labelImagen, BorderLayout.CENTER);

        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPlay = new JButton("▶ Play");
        btnPause = new JButton("⏸ Pause");
        btnStop = new JButton("⏹ Stop");

        GestionReproduccion gestorReproduccion = new GestionReproduccion(this);
        btnPlay.addActionListener(gestorReproduccion);
        btnPause.addActionListener(gestorReproduccion);
        btnStop.addActionListener(gestorReproduccion);

        panelControles.add(btnPlay);
        panelControles.add(btnPause);
        panelControles.add(btnStop);

        JPanel panelProgreso = new JPanel(new BorderLayout(5, 5));
        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        labelTiempoActual = new JLabel("00:00");
        labelTiempoTotal = new JLabel("00:00");
        panelProgreso.add(labelTiempoActual, BorderLayout.WEST);
        panelProgreso.add(barraProgreso, BorderLayout.CENTER);
        panelProgreso.add(labelTiempoTotal, BorderLayout.EAST);

        JPanel panelControlYProgreso = new JPanel(new BorderLayout());
        panelControlYProgreso.add(panelControles, BorderLayout.NORTH);
        panelControlYProgreso.add(panelProgreso, BorderLayout.CENTER);
        panelSuperior.add(panelControlYProgreso, BorderLayout.SOUTH);

        listModel = new DefaultListModel<>();
        listaCanciones = new JList<>(listModel);
        listaCanciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaCanciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarCancion(listaCanciones.getSelectedIndex());
            }
        });
        JScrollPane scrollPane = new JScrollPane(listaCanciones);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Reproducción"));

        JPanel panelGestion = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnAgregar = new JButton("➕ Agregar");
        btnEliminar = new JButton("🗑️ Eliminar");

        ControladorGestion gestorArchivos = new ControladorGestion(this);
        btnAgregar.addActionListener(gestorArchivos);
        btnEliminar.addActionListener(gestorArchivos);
        panelGestion.add(btnAgregar);
        panelGestion.add(btnEliminar);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(scrollPane, BorderLayout.CENTER);
        panelInferior.add(panelGestion, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelInferior, BorderLayout.CENTER);

        inicializarTimerProgreso();
        actualizarEstadoBotones();

        pack();
        setSize(500, 700);
        setLocationRelativeTo(null);
    }

    public void cargarPlaylistEnLista() {
        listModel.clear();
        ArrayList<Music> canciones = playlist.obtenerTodas();
        for (Music cancion : canciones) {
            listModel.addElement(cancion.getNombre() + " - " + cancion.getArtista());
        }
        actualizarEstadoBotones();
    }

    public void seleccionarCancion(int index) {
        if (index >= 0) {
            cancionActualIndex = index;
            if (!isPlaying() && !isPaused()) {
                Music cancion = playlist.getMusic(index);
                if (cancion != null) {
                    actualizarInfoCancion(cancion);
                }
            }
        }
        actualizarEstadoBotones();
    }

    private void actualizarInfoCancion(Music cancion) {
        labelCancionActual.setText(cancion.getNombre() + " - " + cancion.getArtista());
        cargarImagen(cancion.getRutaImagen());
        duracionTotalMs = obtenerDuracionMs(cancion);
        resetearProgresoVisual();
    }

    public void cargarImagen(String rutaImagen) {
        if (rutaImagen != null && !rutaImagen.isEmpty() && new File(rutaImagen).exists()) {
            try {
                ImageIcon icon = new ImageIcon(rutaImagen);
                Image img = icon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                labelImagen.setIcon(new ImageIcon(img));
                labelImagen.setText("");
            } catch (Exception e) {
                labelImagen.setIcon(null);
                labelImagen.setText("Imagen no disponible");
            }
        } else {
            labelImagen.setIcon(null);
            labelImagen.setText("Sin imagen");
        }
    }

    public void actualizarEstadoBotones() {
        boolean cancionSeleccionada = cancionActualIndex != -1;
        boolean reproduciendo = isPlaying();
        boolean pausado = isPaused();

        btnPlay.setEnabled(cancionSeleccionada && !reproduciendo);
        btnPause.setEnabled(reproduciendo || pausado);
        btnStop.setEnabled(reproduciendo || pausado);
        btnEliminar.setEnabled(listaCanciones.getSelectedIndex() != -1);
    }


    public void reproducirCancion() {
        if (cancionActualIndex < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona una canción.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Music cancion = playlist.getMusic(cancionActualIndex);
        if (cancion == null) return;

        if (isPaused()) {
            pausarCancion();
            return;
        }

        detenerCancion();

        actualizarInfoCancion(cancion);
        String rutaArchivo = cancion.getRutaArchivo();
        try {
            File file = new File(rutaArchivo);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "El archivo de audio no se encuentra.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (rutaArchivo.toLowerCase().endsWith(".mp3")) {
                reproducirMP3(file);
            } else if (rutaArchivo.toLowerCase().endsWith(".wav")) {
                reproducirWAV(file);
            } else {
                JOptionPane.showMessageDialog(this, "Formato de archivo no soportado.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al reproducir la canción: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            detenerCancion();
        }
    }

    private void reproducirMP3(File archivo) throws Exception {
        FileInputStream fis = new FileInputStream(archivo);
        BufferedInputStream bis = new BufferedInputStream(fis);
        
        Runnable onFinished = () -> SwingUtilities.invokeLater(this::detenerCancion);
        
        mp3Player = new PausableMP3Player(bis, onFinished);
        mp3Player.play();
        
        inicioReproduccionSystemTime = System.currentTimeMillis();
        timerProgreso.start();
        actualizarEstadoBotones();
    }

    private void reproducirWAV(File archivo) throws Exception {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(archivo);
        clipWAV = AudioSystem.getClip();
        clipWAV.open(audioStream);
        clipWAV.start();

        inicioReproduccionSystemTime = System.currentTimeMillis();
        timerProgreso.start();
        actualizarEstadoBotones();
    }

    public void pausarCancion() {
        if (isPlaying()) {
            timerProgreso.stop();
            if (mp3Player != null) {
                mp3Player.pause();
            } else if (clipWAV != null) {
                clipWAV.stop();
            }
            puntoDePausaMs = tiempoTranscurridoMs; 
        } else if (isPaused()) { 
            if (mp3Player != null) {
                mp3Player.resume();
            } else if (clipWAV != null) {
                clipWAV.start();
            }
            inicioReproduccionSystemTime = System.currentTimeMillis() - puntoDePausaMs;
            timerProgreso.start();
        }
        actualizarEstadoBotones();
    }

    public void detenerCancion() {
        timerProgreso.stop();
        puntoDePausaMs = 0;

        if (mp3Player != null) {
            mp3Player.stop();
            mp3Player = null;
        }
        if (clipWAV != null) {
            clipWAV.stop();
            clipWAV.close();
            clipWAV = null;
        }
        
        if (cancionActualIndex != -1) {
            actualizarInfoCancion(playlist.getMusic(cancionActualIndex));
        } else {
            resetearProgresoVisual();
        }

        actualizarEstadoBotones();
    }
    
    private boolean isPlaying() {
        return (mp3Player != null && mp3Player.getStatus() == PausableMP3Player.Status.PLAYING) ||
               (clipWAV != null && clipWAV.isRunning());
    }
    
    private boolean isPaused() {
        return (mp3Player != null && mp3Player.getStatus() == PausableMP3Player.Status.PAUSED) ||
               (clipWAV != null && !clipWAV.isRunning() && clipWAV.getMicrosecondPosition() > 0 && clipWAV.getMicrosecondPosition() < clipWAV.getMicrosecondLength());
    }


    private void inicializarTimerProgreso() {
        timerProgreso = new Timer(200, e -> actualizarProgreso());
    }

    private void actualizarProgreso() {
        if (duracionTotalMs <= 0 || !isPlaying()) return;

        if (mp3Player != null) {
            tiempoTranscurridoMs = System.currentTimeMillis() - inicioReproduccionSystemTime;
        } else if (clipWAV != null) {
            tiempoTranscurridoMs = clipWAV.getMicrosecondPosition() / 1000;
        }

        if (tiempoTranscurridoMs >= duracionTotalMs) {
            detenerCancion();
            return;
        }

        int porcentaje = (int) ((tiempoTranscurridoMs * 100) / duracionTotalMs);
        barraProgreso.setValue(porcentaje);
        labelTiempoActual.setText(formatearTiempo(tiempoTranscurridoMs));
    }
    
    private void resetearProgresoVisual() {
        tiempoTranscurridoMs = 0;
        puntoDePausaMs = 0;
        barraProgreso.setValue(0);
        labelTiempoActual.setText("00:00");
        labelTiempoTotal.setText(formatearTiempo(duracionTotalMs));
    }

    private String formatearTiempo(long milis) {
        long totalSegundos = milis / 1000;
        long minutos = totalSegundos / 60;
        long segundos = totalSegundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }
    
    
    private long obtenerDuracionMs(Music cancion) {
        if (cancion.getDuracion() != null && cancion.getDuracion().matches("\\d{2}:\\d{2}")) {
            try {
                String[] partes = cancion.getDuracion().split(":");
                long minutos = Long.parseLong(partes[0]);
                long segundos = Long.parseLong(partes[1]);
                return (minutos * 60 + segundos) * 1000;
            } catch (Exception ignored) {}
        }
        
        File file = new File(cancion.getRutaArchivo());
        if (file.exists() && file.getName().toLowerCase().endsWith(".wav")) {
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(file)) {
                return (long) (audioStream.getFrameLength() / audioStream.getFormat().getFrameRate() * 1000);
            } catch (Exception ignored) {}
        }
        return 180000;
    }

    public ListaEnlazadaMusic getPlaylist() { return playlist; }
    public JList<String> getListaCanciones() { return listaCanciones; }
    public int getCancionActualIndex() { return cancionActualIndex; }
    public void setCancionActualIndex(int index) { this.cancionActualIndex = index; }
    public JButton getBtnPlay() { return btnPlay; }
    public JButton getBtnPause() { return btnPause; }
    public JButton getBtnStop() { return btnStop; }
    public JButton getBtnAgregar() { return btnAgregar; }
    public JButton getBtnEliminar() { return btnEliminar; }
}


class PausableMP3Player {
    public enum Status { NOT_STARTED, PLAYING, PAUSED, FINISHED, STOPPED }
    private final Player player;
    private final Object lock = new Object();
    private final Runnable onFinishedCallback;
    private Thread playThread;
    private volatile Status status;

    public PausableMP3Player(InputStream inputStream, Runnable onFinished) throws JavaLayerException {
        this.player = new Player(inputStream);
        this.onFinishedCallback = onFinished;
        this.status = Status.NOT_STARTED;
    }

    public void play() {
        synchronized (lock) {
            if (status == Status.NOT_STARTED) {
                playThread = new Thread(this::run);
                playThread.setDaemon(true);
                status = Status.PLAYING;
                playThread.start();
            }
        }
    }

    public void pause() {
        synchronized (lock) {
            if (status == Status.PLAYING) {
                status = Status.PAUSED;
            }
        }
    }

    public void resume() {
        synchronized (lock) {
            if (status == Status.PAUSED) {
                status = Status.PLAYING;
                lock.notifyAll(); 
            }
        }
    }

    public void stop() {
        synchronized (lock) {
            if (status != Status.STOPPED && status != Status.FINISHED) {
                status = Status.STOPPED;
                player.close();
                lock.notifyAll(); 
            }
        }
    }

    private void run() {
        try {
            while (status != Status.STOPPED && status != Status.FINISHED) {
                synchronized (lock) {
                    while (status == Status.PAUSED) {
                        lock.wait();
                    }
                }
                
                if (status == Status.PLAYING) {
                    if (!player.play(1)) {
                        status = Status.FINISHED;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error en la reproducción MP3: " + e.getMessage());
        } finally {
            if (onFinishedCallback != null) {
                SwingUtilities.invokeLater(onFinishedCallback);
            }
        }
    }

    public Status getStatus() {
        return status;
    }
}