/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_binarios_leonardolopez.logica;

/**
 *
 * @author hnleo
 */
public class MusicNode {

    Music cancion;
    MusicNode siguiente;

    public MusicNode(Music cancion) {
        this.cancion = cancion;
        this.siguiente = null;
    }

}

