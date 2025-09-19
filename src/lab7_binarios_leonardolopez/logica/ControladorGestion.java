package lab7_binarios_leonardolopez.logica;

import lab7_binarios_leonardolopez.Screen.ReproductoMusica;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class ControladorGestion implements ActionListener {
    private ReproductoMusica reproductor;

    public ControladorGestion(ReproductoMusica reproductor) {
        this.reproductor = reproductor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == reproductor.getBtnAgregar()) {
            agregarCancion();
        } else if (e.getSource() == reproductor.getBtnEliminar()) {
            eliminarCancion();
        }
    }

    private void agregarCancion() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de audio", "mp3", "wav"));
        fileChooser.setDialogTitle("Seleccionar archivo de audio");

        if (fileChooser.showOpenDialog(reproductor) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            DialogoAgregarCancion dialog = new DialogoAgregarCancion(reproductor, archivo.getName());

            if (dialog.mostrar()) {
                String rutaImagen = "";
                int opcion = JOptionPane.showConfirmDialog(reproductor,
                        "¿Deseas agregar una imagen para esta canción?",
                        "Imagen de la Canción", JOptionPane.YES_NO_OPTION);

                if (opcion == JOptionPane.YES_OPTION) {
                    JFileChooser imageChooser = new JFileChooser();
                    imageChooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif"));
                    imageChooser.setDialogTitle("Seleccionar imagen");
                    if (imageChooser.showOpenDialog(reproductor) == JFileChooser.APPROVE_OPTION) {
                        rutaImagen = imageChooser.getSelectedFile().getAbsolutePath();
                    }
                }

                try {
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
                    JOptionPane.showMessageDialog(reproductor, "Canción agregada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(reproductor, "Error al guardar la canción: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void eliminarCancion() {
        int selectedIndex = reproductor.getListaCanciones().getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(reproductor, "Por favor, selecciona una canción para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(reproductor,
                "¿Estás seguro de que deseas eliminar esta canción?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (selectedIndex == reproductor.getCancionActualIndex()) {
                    reproductor.detenerCancion();
                    reproductor.setCancionActualIndex(-1);
                    reproductor.labelCancionActual.setText("Ninguna canción seleccionada");
                    reproductor.labelImagen.setIcon(null);
                    reproductor.labelImagen.setText("Sin imagen");
                }
                
                reproductor.getPlaylist().remove(selectedIndex);
                reproductor.cargarPlaylistEnLista();

                if (reproductor.getCancionActualIndex() > selectedIndex) {
                    reproductor.setCancionActualIndex(reproductor.getCancionActualIndex() - 1);
                }
                
                JOptionPane.showMessageDialog(reproductor, "Canción eliminada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(reproductor, "Error al eliminar la canción: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}