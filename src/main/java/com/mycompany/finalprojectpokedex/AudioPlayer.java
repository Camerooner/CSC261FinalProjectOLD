/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.finalprojectpokedex;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Cameron
 */

public class AudioPlayer {

    private Clip buttonClip;
    private Clip musicClip;
    private boolean isMusicPaused = false;

    // Plays button press sounds
    public void playButtonSound(String soundFileName) {
        try {
            if (buttonClip != null && buttonClip.isRunning()) {
                buttonClip.stop(); // Stop the current button sound if it is still running
            }
            buttonClip = createClip(soundFileName);
            buttonClip.start(); // Play the button sound
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Plays toggle background music
    public void toggleMusic(String soundFileName) {
        try {
            if (musicClip == null || !musicClip.isOpen()) {
                musicClip = createClip(soundFileName);
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                isMusicPaused = false;
            } else {
                if (isMusicPaused) {
                    musicClip.start();
                    musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    musicClip.stop();
                }
                isMusicPaused = !isMusicPaused;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Stops the music
    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
        }
    }

    // Helper method to create a Clip for a given sound file name
    private Clip createClip(String soundFileName) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        URL url = getClass().getResource("/" + soundFileName);
        if (url == null) {
            throw new IllegalArgumentException("Resource not found: " + soundFileName);
        }
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        return clip;
    }

    // Checks if the music is playing
    public boolean isMusicPlaying() {
        return musicClip != null && musicClip.isRunning() && !isMusicPaused;
    }
}
