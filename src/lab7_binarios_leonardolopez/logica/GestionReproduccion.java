package lab7_binarios_leonardolopez.logica;

import lab7_binarios_leonardolopez.Screen.ReproductoMusica;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class GestionReproduccion implements ActionListener {
    private ReproductoMusica reproductor;

    public GestionReproduccion(ReproductoMusica reproductor) {
        this.reproductor = reproductor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();

        if (source == reproductor.getBtnPlay()) {
            reproductor.reproducirCancion();
        } else if (source == reproductor.getBtnPause()) {
            reproductor.pausarCancion();
        } else if (source == reproductor.getBtnStop()) {
            reproductor.detenerCancion();
        }
    }
}