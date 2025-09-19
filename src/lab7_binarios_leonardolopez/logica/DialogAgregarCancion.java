/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_binarios_leonardolopez.logica;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**
 *
 * @author hnleo
 */
class DialogoAgregarCancion extends JDialog {
    private JTextField txtNombre, txtArtista, txtDuracion, txtGenero;
    private boolean confirmado = false;
    
    public DialogoAgregarCancion(Frame parent, String nombreArchivo) {
        super(parent, "Agregar CanciÃ³n", true);
        initComponents(nombreArchivo);
    }
    
    private void initComponents(String nombreArchivo) {
        setLayout(new BorderLayout());
        
        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panelCampos.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtNombre = new JTextField(nombreArchivo.replaceFirst("[.][^.]+$", ""), 20);
        panelCampos.add(txtNombre, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panelCampos.add(new JLabel("Artista:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtArtista = new JTextField("Artista Desconocido", 20);
        panelCampos.add(txtArtista, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panelCampos.add(new JLabel("Duracion:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtDuracion = new JTextField("00:00", 20);
        panelCampos.add(txtDuracion, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        panelCampos.add(new JLabel("Genero:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtGenero = new JTextField("Sin clasificar", 20);
        panelCampos.add(txtGenero, gbc);
        
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnOK = new JButton("Agregar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnOK.addActionListener(e -> {
            if (validarCampos()) {
                confirmado = true;
                dispose();
            }
        });
        
        btnCancelar.addActionListener(e -> {
            confirmado = false;
            dispose();
        });
        
        panelBotones.add(btnOK);
        panelBotones.add(btnCancelar);
        
        add(panelCampos, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(getParent());
    }
    
    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    public boolean mostrar() {
        setVisible(true);
        return confirmado;
    }
    
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getArtista() { return txtArtista.getText().trim(); }
    public String getDuracion() { return txtDuracion.getText().trim(); }
    public String getGenero() { return txtGenero.getText().trim(); }
}
