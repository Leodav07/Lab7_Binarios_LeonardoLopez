/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_binarios_leonardolopez.logica;

import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.ImageIcon;

/**
 *
 * @author hnleo
 */
public class Music {
    private String nombre;
    private String artista;
    private String duracion;
    private String rutaArchivo;
    private String rutaImagen;
    private String genero;
    
    public Music(){
        
    }
    
    public Music(String nombre, String artista, String duracion, String rutaArchivo, String rutaImagen, String genero){
        this.nombre = nombre;
        this.artista = artista;
        this.duracion = duracion;
        this.rutaArchivo = rutaArchivo;
        this.rutaImagen = rutaImagen;
        this.genero = genero;
    }
    
    private String ajustar(String cadena, int max){
        if(cadena == null){
            cadena = "";
        }
        
        if(cadena.length() > max - 3)
        {
            return cadena.substring(0, max - 3);
        }
        
        return cadena;
    }
    
    public void escribirArchivo(RandomAccessFile raf) throws IOException{
        raf.writeUTF(ajustar(nombre, 100));
        raf.writeUTF(ajustar(artista, 50));
        raf.writeUTF(ajustar(duracion, 10));
        raf.writeUTF(ajustar(rutaArchivo, 200));
        raf.writeUTF(ajustar(rutaImagen, 200));
        raf.writeUTF(ajustar(genero, 30));
    }
    
    public static Music leerArchivo(RandomAccessFile raf) throws IOException {
        String nombre = raf.readUTF().trim();
        String artista = raf.readUTF().trim();
        String duracion = raf.readUTF().trim();
        String rutaArchivo = raf.readUTF().trim();
        String rutaImagen = raf.readUTF().trim();
        String genero = raf.readUTF().trim();
        return new Music(nombre, artista, duracion, rutaArchivo, rutaImagen, genero);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    @Override
    public String toString() {
        return "Music{" + "nombre = " + nombre + ", artista = " + artista + ", duracion = " + duracion + '}';
    }
    
    
}
