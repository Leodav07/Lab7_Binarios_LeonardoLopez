/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_binarios_leonardolopez.logica;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 *
 * @author hnleo
 */
public class ListaEnlazadaMusic {

    private MusicNode inicio;
    private int tamano;
    private final String archivo = "musica/playlist.music";

    public ListaEnlazadaMusic() {
        this.inicio = null;
        this.tamano = 0;
        File dir = new File("musica");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        cargarArchivo();
    }

    public boolean isEmpty() {
        if (inicio == null) {
            return true;
        }

        return false;
    }

    public void add(Music music) throws IOException {
        MusicNode musicaNueva = new MusicNode(music);

        if (isEmpty()) {
            inicio = musicaNueva;
        } else {
            MusicNode temp = inicio;
            while (temp.siguiente != null) {
                temp = temp.siguiente;
            }

            temp.siguiente = musicaNueva;

        }

        tamano++;
        guardarArchivo();

    }

    public boolean remove(int index) throws IOException {
        if (index < 0 || index > tamano) {
            return false;
        }
        if (index == 0) {
            inicio = inicio.siguiente;
        } else {
            MusicNode temp = inicio;
            for (int i = 0; i < index - 1; i++) {
                temp = temp.siguiente;
            }
            temp.siguiente = temp.siguiente.siguiente;
        }
        tamano--;
        guardarArchivo();
        return true;
    }

    public Music getMusic(int index) {
        if (index < 0 || index > tamano) {
            return null;
        }

        MusicNode temp = inicio;
        for (int i = 0; i < index; i++) {
            temp = temp.siguiente;
        }
        return temp.cancion;
    }

    public ArrayList<Music> obtenerTodas() {
        ArrayList<Music> lista = new ArrayList<>();
        MusicNode actual = inicio;

        while (actual != null) {
            lista.add(actual.cancion);
            actual = actual.siguiente;
        }
        return lista;
    }

    private void cargarArchivo() {
        File archivoF = new File(archivo);

        if (!archivoF.exists()) {
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
            if (raf.length() == 0) {
                return;
            }

            int numCanciones = raf.readInt();

            inicio = null;
            tamano = 0;

            for (int i = 0; i < numCanciones; i++) {
                try {
                    Music music = Music.leerArchivo(raf);
                    MusicNode nuevaCancion = new MusicNode(music);

                    if (inicio == null) {
                        inicio = nuevaCancion;
                    } else {
                        MusicNode temp = inicio;
                        while (temp.siguiente != null) {
                            temp = temp.siguiente;
                        }
                        temp.siguiente = nuevaCancion;
                    }
                    tamano++;
                } catch (IOException e) {
                    System.out.println("Error al leer canción " + i + ": " + e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar desde archivo binario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void guardarArchivo() throws IOException {
        File dir = new File("musica");
    if (!dir.exists()) {
        dir.mkdirs();
    }
        try {
            RandomAccessFile rmusic = new RandomAccessFile(archivo, "rw");
            rmusic.writeInt(tamano);

            MusicNode temp = inicio;
            while (temp != null) {
                temp.cancion.escribirArchivo(rmusic);
                temp = temp.siguiente;
            }
        } catch (IOException io) {
            System.out.println("Error al guardar el archivo: " + io.getMessage());
        }
    }

    public void limpiar() throws IOException {
        inicio = null;
        tamano = 0;
        guardarArchivo();
    }

    public int getTamano() {
        return tamano;
    }

    public boolean listaVacia() {
        return tamano == 0;
    }

}
