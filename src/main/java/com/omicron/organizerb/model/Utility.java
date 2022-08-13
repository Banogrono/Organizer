/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 07-Jul-22
 * Time: 01:22
 * =============================================================
 **/

package com.omicron.organizerb.model;

import com.omicron.organizerb.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;


/**
 *  Utility class containing mostly low-level methods that are used in various places across entire application.
 *  I absolutely hate the idea of 'utility class' with random methods, but I have no idea how to do it better.
 *
 *  On the one hand, having those methods here satisfy the DRY principle and simplifies de-clutters classes.
 *  On the other hand, it breaks Single responsibility principle of the SOLID design pattern.
 */
public class Utility {

    public static FXMLLoader getFXMLLoader(String path) {
        try {
            URL dialogFXML = new File(path).toURI().toURL();
            return new FXMLLoader(dialogFXML);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ImageView getIcon(String path) {
        String imageLocation = Objects.requireNonNull(Utility.class.getResource(path)).toExternalForm();
        ImageView img = new ImageView(new Image(imageLocation));
        img.fitWidthProperty().setValue(24);
        img.fitHeightProperty().setValue(24);
        return img;
    }

    public static File getFile(String pathname) {
        return new File(pathname);
    }

    public static Object deserializeObject(String objectPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(objectPath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            Object deserializedObject = objectInputStream.readObject();

            objectInputStream.close();
            fileInputStream.close();

            return deserializedObject;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void serializeObject(Object toSerialize, String path) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(toSerialize);

            objectOutputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] getAllFilesInDirectory(String path) {
        File file = Utility.getFile(path);
        return file.list();
    }

    public static void playSound(File soundFile) throws MalformedURLException {
        Media sound = new Media(soundFile.toURI().toURL().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }

    public static synchronized void playSound(final String url) {
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                            Main.class.getResourceAsStream(url));
                    clip.open(inputStream);
                    clip.start();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }


}
