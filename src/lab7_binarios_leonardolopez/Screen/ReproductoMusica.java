package lab7_binarios_leonardolopez.Screen;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javazoom.jl.player.advanced.AdvancedPlayer;
import lab7_binarios_leonardolopez.logica.ControladorGestion;
import lab7_binarios_leonardolopez.logica.GestionReproduccion;
import lab7_binarios_leonardolopez.logica.ListaEnlazadaMusic;
import lab7_binarios_leonardolopez.logica.Music;
import javax.swing.Timer;

/**
 *
 * @author hnleo (ajustado)
 */
public class ReproductoMusica extends JFrame {

    private ListaEnlazadaMusic playlist;
    private DefaultListModel<Music> listModel;
    private JList<Music> listaCanciones;
    public JLabel labelCancionActual;
    public JLabel labelImagen;
    private JButton btnPlay, btnPause, btnStop;
    private JButton btnAgregar, btnEliminar;
    public int cancionActualIndex = -1;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private JProgressBar barraProgreso;
    private JLabel labelTiempoActual;
    private JLabel labelTiempoTotal;
    private Timer timerProgreso;
    private long duracionTotalMs = 0;
    private long tiempoActualMs = 0;
    private long inicioReproduccion = 0;
    private AdvancedPlayer reproductorMP3;
    private Thread hiloReproduccion;
    private Clip clipWAV;

    private String rutaCancionActual;
    private int frameActual = 0;
    private boolean pausadoMP3 = false;

    public ReproductoMusica() {
        playlist = new ListaEnlazadaMusic();
        initComponents();
        cargarPlaylistEnLista();
    }

    private void initComponents() {
        setTitle("Reproductor de Musica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelSuperior = new JPanel(new BorderLayout());
        labelCancionActual = new JLabel("Ninguna canción seleccionada", JLabel.CENTER);
        labelCancionActual.setFont(new Font("Arial", Font.BOLD, 16));
        labelImagen = new JLabel("", JLabel.CENTER);
        labelImagen.setPreferredSize(new Dimension(200, 200));
        labelImagen.setBorder(BorderFactory.createEtchedBorder());

        panelSuperior.add(labelCancionActual, BorderLayout.NORTH);
        panelSuperior.add(labelImagen, BorderLayout.CENTER);

        JPanel panelControles = new JPanel(new FlowLayout());
        btnPlay = new JButton("Play");
        btnPause = new JButton("Pause");
        btnStop = new JButton("Stop");

        // Mantengo tu forma de conectar con los controladores (tus clases)
        btnPlay.addActionListener(new GestionReproduccion(this));
        btnPause.addActionListener(new GestionReproduccion(this));
        btnStop.addActionListener(new GestionReproduccion(this));

        panelControles.add(btnPlay);
        panelControles.add(btnPause);
        panelControles.add(btnStop);

        // *** Panel de progreso ***
        JPanel panelProgreso = new JPanel(new BorderLayout());
        panelProgreso.setBorder(BorderFactory.createTitledBorder("Progreso"));

        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("00:00 / 00:00");
        barraProgreso.setPreferredSize(new Dimension(400, 25));

        JPanel panelTiempo = new JPanel(new BorderLayout());
        labelTiempoActual = new JLabel("00:00", JLabel.LEFT);
        labelTiempoTotal = new JLabel("00:00", JLabel.RIGHT);
        panelTiempo.add(labelTiempoActual, BorderLayout.WEST);
        panelTiempo.add(labelTiempoTotal, BorderLayout.EAST);

        panelProgreso.add(barraProgreso, BorderLayout.CENTER);
        panelProgreso.add(panelTiempo, BorderLayout.SOUTH);

        // Combino controles y progreso en un solo bloque
        JPanel panelControlYProgreso = new JPanel(new BorderLayout());
        panelControlYProgreso.add(panelControles, BorderLayout.NORTH);
        panelControlYProgreso.add(panelProgreso, BorderLayout.SOUTH);

        panelSuperior.add(panelControlYProgreso, BorderLayout.SOUTH);

        // Lista de reproducción
        listModel = new DefaultListModel<>();
        listaCanciones = new JList<>(listModel);
        listaCanciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaCanciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarCancion();
            }
        });

        JScrollPane scrollPane = new JScrollPane(listaCanciones);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Reproducción"));

        JPanel panelGestion = new JPanel(new FlowLayout());
        btnAgregar = new JButton("Agregar Cancion");
        btnEliminar = new JButton("Eliminar Cancion");

        btnAgregar.addActionListener(new ControladorGestion(this));
        btnEliminar.addActionListener(new ControladorGestion(this));

        panelGestion.add(btnAgregar);
        panelGestion.add(btnEliminar);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(scrollPane, BorderLayout.CENTER);
        panelInferior.add(panelGestion, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelInferior, BorderLayout.CENTER);

        // Inicializar timer para actualizar progreso
        inicializarTimerProgreso();
        actualizarEstadoBotones();

        setSize(800, 700); // Aumentar altura para acomodar la barra
        setLocationRelativeTo(null);
    }

    public void cargarPlaylistEnLista() {
        listModel.clear();
        ArrayList<Music> canciones = playlist.obtenerTodas();
        for (Music cancion : canciones) {
            listModel.addElement(cancion);
        }
        actualizarEstadoBotones();
    }

    public void seleccionarCancion() {
        int selectedIndex = listaCanciones.getSelectedIndex();
        if (selectedIndex >= 0) {
            cancionActualIndex = selectedIndex;
            Music cancion = playlist.getMusic(selectedIndex);
            if (cancion != null) {
                labelCancionActual.setText(cancion.getNombre() + " - " + cancion.getArtista());
                cargarImagen(cancion.getRutaImagen());

                duracionTotalMs = obtenerDuracionArchivo(cancion.getRutaArchivo());
                labelTiempoTotal.setText(formatearTiempo(duracionTotalMs));

                if (!isPlaying) {
                    resetearProgreso();
                    labelTiempoTotal.setText(formatearTiempo(duracionTotalMs));
                    barraProgreso.setString("00:00 / " + formatearTiempo(duracionTotalMs));
                }

                if (isPlaying) {
                    // Si ya está reproduciendo y el usuario cambió la selección,
                    // detengo la reproducción anterior y arranco la nueva
                    reproducirCancion();
                }
            }
        }
        actualizarEstadoBotones();
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void cargarImagen(String rutaImagen) {
        if (rutaImagen != null && !rutaImagen.isEmpty() && new File(rutaImagen).exists()) {
            try {
                ImageIcon icon = new ImageIcon(rutaImagen);
                Image img = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
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
        boolean hayCancionSeleccionada = cancionActualIndex >= 0;
        boolean hayItems = !playlist.listaVacia();

        btnPlay.setEnabled(hayCancionSeleccionada && (!isPlaying || isPaused));
        btnPause.setEnabled(isPlaying);
        btnStop.setEnabled(isPlaying);
        btnEliminar.setEnabled(hayItems && listaCanciones.getSelectedIndex() >= 0);
    }

    public void reproducirCancion() {
        // Asegurarnos de tener una canción seleccionada
        if (cancionActualIndex < 0) {
            int sel = listaCanciones.getSelectedIndex();
            if (sel >= 0) {
                cancionActualIndex = sel;
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona una canción para reproducir.");
                return;
            }
        }

        // Detener cualquier reproducción previa limpia
        detenerCancionCorregida();

        isPlaying = true;
        isPaused = false;
        actualizarEstadoBotones();

        Music cancion = playlist.getMusic(cancionActualIndex);
        if (cancion == null) {
            JOptionPane.showMessageDialog(this, "Error: canción no encontrada en la lista.");
            return;
        }

        rutaCancionActual = cancion.getRutaArchivo();
        duracionTotalMs = obtenerDuracionArchivo(rutaCancionActual);
        inicioReproduccion = System.currentTimeMillis();
        // Arranco el timer de progreso
        timerProgreso.start();

        String rutaLower = rutaCancionActual.toLowerCase();
        if (rutaLower.endsWith(".wav")) {
            reproducirWAV(rutaCancionActual);
        } else if (rutaLower.endsWith(".mp3")) {
            reproducirMP3(rutaCancionActual);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Formato no soportado: " + obtenerExtension(rutaCancionActual),
                    "Error", JOptionPane.ERROR_MESSAGE);
            // detengo timer si no se reproduce
            timerProgreso.stop();
            isPlaying = false;
            actualizarEstadoBotones();
        }
    }

    public void reproducirWAV(String rutaArchivo) {
        try {
            File archivoAudio = new File(rutaArchivo);
            if (!archivoAudio.exists()) {
                JOptionPane.showMessageDialog(this, "Archivo no encontrado: " + rutaArchivo,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(archivoAudio);
            clipWAV = AudioSystem.getClip();
            clipWAV.open(audioStream);
            clipWAV.start();

            // Actualizar duración basada en clip
            duracionTotalMs = clipWAV.getMicrosecondLength() / 1000;
            inicioReproduccion = System.currentTimeMillis();

            // Listener para detectar fin de reproducción y limpiar
            clipWAV.addLineListener(event -> {
                switch (event.getType()) {
                    case STOP:
                        // Si llegó al final, detener y resetear
                        if (clipWAV != null && clipWAV.getMicrosecondLength() / 1000 <= (System.currentTimeMillis() - inicioReproduccion) + 1000) {
                            SwingUtilities.invokeLater(this::detenerCancionCorregida);
                        }
                        break;
                    default:
                        break;
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reproduciendo WAV: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            detenerCancionCorregida();
        }
    }

    public void reproducirMP3(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                JOptionPane.showMessageDialog(this, "Archivo no encontrado: " + rutaArchivo,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            FileInputStream fis = new FileInputStream(archivo);
            BufferedInputStream bis = new BufferedInputStream(fis);
            reproductorMP3 = new AdvancedPlayer(bis);

            // Hilo de reproducción para no bloquear el EDT
            hiloReproduccion = new Thread(() -> {
                try {
                    reproductorMP3.play();
                } catch (Exception e) {
                    // Si ocurre error, informamos y limpiamos estado
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Error reproduciendo MP3: " + e.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        detenerCancionCorregida();
                    });
                } finally {
                    // Al finalizar la reproducción, limpiar estado (en EDT)
                    SwingUtilities.invokeLater(this::detenerCancionCorregida);
                }
            }, "Hilo-Reproductor-MP3");
            hiloReproduccion.start();

            // Si Music tiene duración (campo duracion), usarla; si no, se usa el fallback en obtenerDuracionArchivo
            duracionTotalMs = obtenerDuracionArchivo(rutaArchivo);
            inicioReproduccion = System.currentTimeMillis();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando MP3: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            detenerCancionCorregida();
        }
    }

    public String obtenerExtension(String nombreArchivo) {
        int ultimoPunto = nombreArchivo.lastIndexOf('.');
        if (ultimoPunto > 0) {
            return nombreArchivo.substring(ultimoPunto + 1);
        }
        return "";
    }

    public void pausarCancionCorregida() {
        if (!isPlaying) return;

        if (!isPaused) {
            // Pausar
            isPaused = true;
            timerProgreso.stop();

            // Para WAV
            if (clipWAV != null && clipWAV.isRunning()) {
                clipWAV.stop();
            }

            // Para MP3: no es fácil hacer pause/resume exacto con AdvancedPlayer sin manejar frames.
            // Aquí hacemos un stop de la reproducción (cierra el player). Al reanudar se reiniciará desde el inicio.
            if (reproductorMP3 != null) {
                try {
                    reproductorMP3.close();
                } catch (Exception ex) {
                    // ignore
                }
                reproductorMP3 = null;
            }
            if (hiloReproduccion != null && hiloReproduccion.isAlive()) {
                hiloReproduccion.interrupt();
            }

            actualizarEstadoBotones();
            // Notificación opcional:
            // JOptionPane.showMessageDialog(this, "Canción pausada", "Información", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Reanudar (para WAV es posible; para MP3 se reinicia desde el inicio)
            isPaused = false;
            timerProgreso.start();

            if (clipWAV != null) {
                clipWAV.start();
            } else if (rutaCancionActual != null && rutaCancionActual.toLowerCase().endsWith(".mp3")) {
                // Reproducir MP3 de nuevo (no reanuda desde posición exacta con esta implementación)
                reproducirMP3(rutaCancionActual);
            }
            actualizarEstadoBotones();
            // JOptionPane.showMessageDialog(this, "Reanudado", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void detenerCancionCorregida() {
        if (!isPlaying && !isPaused) {
            // no hay nada que detener, sólo reseteo visual
            resetearProgreso();
            actualizarEstadoBotones();
            return;
        }

        isPlaying = false;
        isPaused = false;
        frameActual = 0;
        timerProgreso.stop();
        resetearProgreso();

        // Detener WAV
        if (clipWAV != null) {
            try {
                clipWAV.stop();
                clipWAV.close();
            } catch (Exception ex) {
                // ignore
            }
            clipWAV = null;
        }

        // Detener MP3
        if (reproductorMP3 != null) {
            try {
                reproductorMP3.close();
            } catch (Exception ex) {
                // ignore
            }
            reproductorMP3 = null;
        }

        if (hiloReproduccion != null && hiloReproduccion.isAlive()) {
            hiloReproduccion.interrupt();
        }
        hiloReproduccion = null;

        // Mostrar duración total de la canción seleccionada
        if (cancionActualIndex >= 0) {
            Music cancion = playlist.getMusic(cancionActualIndex);
            if (cancion != null) {
                duracionTotalMs = obtenerDuracionArchivo(cancion.getRutaArchivo());
                labelTiempoTotal.setText(formatearTiempo(duracionTotalMs));
                barraProgreso.setString("00:00 / " + formatearTiempo(duracionTotalMs));
            }
        }

        actualizarEstadoBotones();
        // JOptionPane.showMessageDialog(this, "Reproducción detenida", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public JProgressBar getBarraProgreso() {
        return barraProgreso;
    }

    public JLabel getLabelTiempoActual() {
        return labelTiempoActual;
    }

    public JLabel getLabelTiempoTotal() {
        return labelTiempoTotal;
    }

    // Estos métodos reemplazan las versiones recursivas del pasado
    public void pausarCancion() {
        pausarCancionCorregida();
    }

    public void detenerCancion() {
        detenerCancionCorregida();
    }

    private void inicializarTimerProgreso() {
        timerProgreso = new Timer(1000, e -> actualizarProgreso()); // Actualizar cada segundo
    }

    private void actualizarProgreso() {
        if (isPlaying && !isPaused) {
            long tiempoTranscurrido = System.currentTimeMillis() - inicioReproduccion;
            tiempoActualMs = tiempoTranscurrido;

            int porcentaje = 0;
            if (duracionTotalMs > 0) {
                porcentaje = (int) ((tiempoActualMs * 100) / duracionTotalMs);
                porcentaje = Math.min(porcentaje, 100); // No exceder 100%
            }

            barraProgreso.setValue(porcentaje);

            String tiempoActualStr = formatearTiempo(tiempoActualMs);
            String tiempoTotalStr = formatearTiempo(duracionTotalMs);

            labelTiempoActual.setText(tiempoActualStr);
            labelTiempoTotal.setText(tiempoTotalStr);
            barraProgreso.setString(tiempoActualStr + " / " + tiempoTotalStr);

            if (tiempoActualMs >= duracionTotalMs && duracionTotalMs > 0) {
                detenerCancionCorregida();
            }
        }
    }

    private String formatearTiempo(long milisegundos) {
        long segundosTotales = milisegundos / 1000;
        long minutos = segundosTotales / 60;
        long segundos = segundosTotales % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    private void resetearProgreso() {
        barraProgreso.setValue(0);
        labelTiempoActual.setText("00:00");
        labelTiempoTotal.setText("00:00");
        barraProgreso.setString("00:00 / 00:00");
        tiempoActualMs = 0;
        duracionTotalMs = 0;
        inicioReproduccion = 0;
    }

    private long obtenerDuracionArchivo(String rutaArchivo) {
        try {
            if (rutaArchivo == null) return 180000;
            if (rutaArchivo.toLowerCase().endsWith(".wav")) {
                File archivo = new File(rutaArchivo);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(archivo);
                javax.sound.sampled.AudioFormat formato = audioStream.getFormat();
                long frames = audioStream.getFrameLength();
                double duracionSegundos = (frames) / formato.getFrameRate();
                audioStream.close();
                return (long) (duracionSegundos * 1000);
            } else {
                // Si el objeto Music tiene duración, la usamos (formato mm:ss)
                Music cancion = (cancionActualIndex >= 0) ? playlist.getMusic(cancionActualIndex) : null;
                if (cancion != null && cancion.getDuracion() != null && !cancion.getDuracion().isEmpty()) {
                    return parsearDuracion(cancion.getDuracion());
                }
                // fallback aproximado 3 minutos
                return 180000;
            }
        } catch (Exception e) {
            System.out.println("Error obteniendo duración: " + e.getMessage());
            return 180000;
        }
    }

    private long parsearDuracion(String duracionStr) {
        try {
            String[] partes = duracionStr.split(":");
            if (partes.length == 2) {
                int minutos = Integer.parseInt(partes[0]);
                int segundos = Integer.parseInt(partes[1]);
                return (minutos * 60 + segundos) * 1000L;
            }
        } catch (Exception e) {
            System.out.println("Error parseando duración: " + e.getMessage());
        }
        return 180000;
    }

    public JButton getBtnPlay() {
        return btnPlay;
    }

    public void setBtnPlay(JButton btnPlay) {
        this.btnPlay = btnPlay;
    }

    public JButton getBtnPause() {
        return btnPause;
    }

    public ListaEnlazadaMusic getPlaylist() {
        return playlist;
    }

    public JList<Music> getListaCanciones() {
        return listaCanciones;
    }

    public void setListaCanciones(JList<Music> listaCanciones) {
        this.listaCanciones = listaCanciones;
    }

    public void setPlaylist(ListaEnlazadaMusic playlist) {
        this.playlist = playlist;
    }

    public void setBtnPause(JButton btnPause) {
        this.btnPause = btnPause;
    }

    public JButton getBtnStop() {
        return btnStop;
    }

    public void setBtnStop(JButton btnStop) {
        this.btnStop = btnStop;
    }

    public JButton getBtnAgregar() {
        return btnAgregar;
    }

    public void setBtnAgregar(JButton btnAgregar) {
        this.btnAgregar = btnAgregar;
    }

    public JButton getBtnEliminar() {
        return btnEliminar;
    }

    public void setBtnEliminar(JButton btnEliminar) {
        this.btnEliminar = btnEliminar;
    }

    public int getCancionActualIndex() {
        return cancionActualIndex;
    }

    public void setCancionActualIndex(int cancionActualIndex) {
        this.cancionActualIndex = cancionActualIndex;
    }

    public boolean isIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isIsPaused() {
        return isPaused;
    }

    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public Thread getHiloReproduccion() {
        return hiloReproduccion;
    }

    public void setHiloReproduccion(Thread hiloReproduccion) {
        this.hiloReproduccion = hiloReproduccion;
    }

    public int getFrameActual() {
        return frameActual;
    }

    public void setFrameActual(int frameActual) {
        this.frameActual = frameActual;
    }

    public boolean isPausadoMP3() {
        return pausadoMP3;
    }

    public void setPausadoMP3(boolean pausadoMP3) {
        this.pausadoMP3 = pausadoMP3;
    }

}
