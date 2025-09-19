package lab7_binarios_leonardolopez.logica;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import lab7_binarios_leonardolopez.Screen.ReproductoMusica;

public class ControladorGestion implements ActionListener {
    private ReproductoMusica reproductor; 
    
    public ControladorGestion(ReproductoMusica reproductor) { 
        this.reproductor = reproductor;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        
        if (source == reproductor.getBtnAgregar()) {
            agregarCancion();
        } else if (source == reproductor.getBtnEliminar()) {
            eliminarCancion();
        }
    }
    
    private void agregarCancion() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Archivos de audio", "mp3", "wav", "m4a", "flac"));
            
            if (fileChooser.showOpenDialog(reproductor) == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                
                DialogoAgregarCancion dialog = new DialogoAgregarCancion(
                    reproductor, archivo.getName());
                
                if (dialog.mostrar()) {
                    String rutaImagen = "";
                    int opcion = JOptionPane.showConfirmDialog(reproductor,
                        "¿Deseas agregar una imagen para esta canción?",
                        "Imagen", JOptionPane.YES_NO_OPTION);
                    
                    if (opcion == JOptionPane.YES_OPTION) {
                        JFileChooser imageChooser = new JFileChooser();
                        imageChooser.setFileFilter(new FileNameExtensionFilter(
                            "Imágenes", "jpg", "jpeg", "png", "gif", "bmp"));
                        
                        if (imageChooser.showOpenDialog(reproductor) == JFileChooser.APPROVE_OPTION) {
                            rutaImagen = imageChooser.getSelectedFile().getAbsolutePath();
                        }
                    }
                    
                    Music nuevaCancion = new Music(
                        dialog.getNombre(),
                        dialog.getArtista(),
                        dialog.getDuracion(),
                        archivo.getAbsolutePath(),
                        rutaImagen,
                        dialog.getGenero()
                    );
                    
                    reproductor.getPlaylist().add(nuevaCancion);
                    reproductor.cargarPlaylistEnLista();
                    reproductor.actualizarEstadoBotones();
                    
                    JOptionPane.showMessageDialog(reproductor, 
                        "Canción agregada exitosamente", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(reproductor, 
                "Error al agregar la canción: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void eliminarCancion() {
        try {
            int selectedIndex = reproductor.getListaCanciones().getSelectedIndex();
            if (selectedIndex >= 0) {
                int opcion = JOptionPane.showConfirmDialog(
                    reproductor,
                    "¿Estás seguro de eliminar esta canción?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (opcion == JOptionPane.YES_OPTION) {
                    // Detener reproducción si es la canción actual
                    if (selectedIndex == reproductor.getCancionActualIndex() && reproductor.getIsPlaying()) {
                        reproductor.detenerCancion();
                        reproductor.setCancionActualIndex(-1);
                        reproductor.labelCancionActual.setText("Ninguna canción seleccionada");
                        reproductor.labelImagen.setIcon(null);
                        reproductor.labelImagen.setText("Sin imagen");
                    }
                    
                    reproductor.getPlaylist().remove(selectedIndex);
                    reproductor.cargarPlaylistEnLista();
                    
                    // Ajustar índice actual
                    if (reproductor.getCancionActualIndex() > selectedIndex) {
                        reproductor.setCancionActualIndex(reproductor.getCancionActualIndex() - 1);
                    } else if (reproductor.getCancionActualIndex() == selectedIndex) {
                        reproductor.setCancionActualIndex(-1);
                    }
                    
                    reproductor.actualizarEstadoBotones();
                    
                    JOptionPane.showMessageDialog(reproductor, 
                        "Canción eliminada exitosamente", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(reproductor, 
                    "Por favor selecciona una canción para eliminar", 
                    "Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(reproductor, 
                "Error al eliminar la canción: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}