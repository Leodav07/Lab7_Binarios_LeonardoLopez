package lab7_binarios_leonardolopez.logica;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import lab7_binarios_leonardolopez.Screen.ReproductoMusica;

public class GestionReproduccion implements ActionListener {
    private ReproductoMusica reproductor; // Cambio: recibir la instancia
    
    public GestionReproduccion(ReproductoMusica reproductor) { // Constructor
        this.reproductor = reproductor;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (reproductor.getCancionActualIndex() < 0) {
            JOptionPane.showMessageDialog(reproductor, 
                "Por favor selecciona una canción", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JButton source = (JButton) e.getSource();
        if (source == reproductor.getBtnPlay()) {
            reproductor.reproducirCancion();
        } else if (source == reproductor.getBtnPause()) {
            reproductor.pausarCancionCorregida(); // Método corregido
        } else if (source == reproductor.getBtnStop()) {
            reproductor.detenerCancionCorregida(); // Método corregido
        }
    }
}