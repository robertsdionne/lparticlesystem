package lparticlesystem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import processing.core.PApplet;
import processing.core.PVector;

import com.google.common.base.Throwables;

@SuppressWarnings("serial")
public class LParticleSystem extends PApplet {

  private static final PVector X = new PVector(1.0f, 0.0f, 0.0f);
  private static final PVector Y = new PVector(0.0f, 1.0f, 0.0f);
  private static final PVector NZ = new PVector(0.0f, 0.0f, -1.0f);
  
  private final Map<Character, Boolean> keys = new HashMap<>();
  private final Map<Integer, Boolean> keyCodes = new HashMap<>();
  private LSystem lsystem;
  private PVector eye = new PVector(0.0f, 0.0f, 1000.0f),
      forward = new PVector(0.0f, 0.0f, -1.0f),
      right = new PVector(1.0f, 0.0f, 0.0f),
      up = new PVector(0.0f, 1.0f, 0.0f);
  private Quaternion orientation = new Quaternion();
  private boolean sys = false;

  @Override public void setup() {
    size(1024, 768, OPENGL);
    smooth();
    background(300, 100, 0.19f);
    lsystem = LSystem.load(chooseFile());
  }
  
  private File chooseFile() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final Throwable rethrown) {
      throw Throwables.propagate(rethrown);
    }
    final JFileChooser fileChooser = new JFileChooser("lparticlesystem");
    fileChooser.setFileFilter(new FileFilter() {
      
      @Override public String getDescription() {
        return "L-System JSON Files";
      }
      
      @Override public boolean accept(final File f) {
        return f.getName().endsWith(".json");
      }
    });
    final int returnValue = fileChooser.showOpenDialog(this);
    if (JFileChooser.APPROVE_OPTION == returnValue) {
      return fileChooser.getSelectedFile();
    }
    return null;
  }

  @Override public void draw() {
    lights();
    background(300, 100, 0.19f);
//    background(0);
    
    input();

    camera(0.0f, 0.0f, 0.0f,
        NZ.x, NZ.y, NZ.z,
        Y.x, Y.y, Y.z);
    final Quaternion axisAngle = orientation.toAxisAngle();
    rotate(axisAngle.w, axisAngle.x, axisAngle.y, axisAngle.z);
    translate(-eye.x, -eye.y, -eye.z);
    
    stroke(255);
    strokeWeight(2.0f);
    lsystem.draw(map(mouseX, 0, width, 1.0f - 0.01f, 1.0f + 0.01f),
        map(mouseY, 0, height, 1.0f - 0.1f, 1.0f + 0.1f), this, sys);
//    strokeWeight(5.0f);
//    line(-100, 0, 0, 100, 0, 0);
//    line(0, -100, 0, 0, 100, 0);
//    line(0, 0, -100, 0, 0, 100);
    
    final Quaternion inverse = orientation.reciprocal();
    forward = inverse.transform(NZ);
    right = inverse.transform(X);
    up = inverse.transform(Y);
  }

  private void input() {
    if (keyDown('a')) {
      final PVector step = right.get();
      step.mult(10.0f);
      eye.sub(step);
    }
    if (keyDown('d')) {
      final PVector step = right.get();
      step.mult(10.0f);
      eye.add(step);
    }
    if (keyDown('q')) {
      final PVector step = up.get();
      step.mult(10.0f);
      eye.sub(step);
    }
    if (keyDown('z')) {
      final PVector step = up.get();
      step.mult(10.0f);
      eye.add(step);
    }
    if (keyDown('w')) {
      final PVector step = forward.get();
      step.mult(10.0f);
      eye.add(step);
    }
    if (keyDown('s')) {
      final PVector step = forward.get();
      step.mult(10.0f);
      eye.sub(step);
    }
    if (keyCodeDown(RIGHT)) {
      orientation = Quaternion.fromAxisAngle(Y, 0.1f).times(orientation);
    }
    if (keyCodeDown(LEFT)) {
      orientation = Quaternion.fromAxisAngle(Y, -0.1f).times(orientation);
    }
    if (keyCodeDown(DOWN)) {
      orientation = Quaternion.fromAxisAngle(X, -0.1f).times(orientation);
    }
    if (keyCodeDown(UP)) {
      orientation = Quaternion.fromAxisAngle(X, 0.1f).times(orientation);
    }
    if (keyDown(',')) {
      orientation = Quaternion.fromAxisAngle(NZ, -0.1f).times(orientation);
    }
    if (keyDown('.')) {
      orientation = Quaternion.fromAxisAngle(NZ, 0.1f).times(orientation);
    }
  }
  
  @Override public void mousePressed() {
    lsystem.addParticle(new PVector(mouseX, mouseY, 0.0f),
        map(mouseX, 0, width, 1.0f - 0.01f, 1.0f + 0.01f),
        map(mouseY, 0, height, 1.0f - 0.1f, 1.0f + 0.1f));
  }
  
  private boolean keyDown(char key) {
    return keys.containsKey(key) && keys.get(key);
  }
  
  private boolean keyCodeDown(int keyCode) {
    return keyCodes.containsKey(keyCode) && keyCodes.get(keyCode);
  }

  @Override public void keyPressed() {
    if (CODED == key) {
      keyCodes.put(keyCode, true);
    } else {
      keys.put(key, true);
    }
    if ('\t' == key) {
      sys = !sys;
    }
  }
  
  @Override public void keyReleased() {
    if (CODED == key) {
      keyCodes.put(keyCode, false);
    } else {
      keys.put(key, false);
    }
  }
}
