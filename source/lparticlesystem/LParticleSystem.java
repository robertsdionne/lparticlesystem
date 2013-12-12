package lparticlesystem;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;

@SuppressWarnings("serial")
public class LParticleSystem extends PApplet {

  private static final PVector X = new PVector(1.0f, 0.0f, 0.0f);
  private static final PVector Y = new PVector(0.0f, 1.0f, 0.0f);
  private static final PVector NZ = new PVector(0.0f, 0.0f, -1.0f);
  
  private final Map<Character, Boolean> keys = new HashMap<>();
  private final Map<Integer, Boolean> keyCodes = new HashMap<>();
  private LSystem lsystem;
  private PVector eye = new PVector(0.0f, 0.0f, 100.0f),
      forward = new PVector(0.0f, 0.0f, -1.0f),
      right = new PVector(1.0f, 0.0f, 0.0f),
      up = new PVector(0.0f, 1.0f, 0.0f);
  private Quaternion orientation = new Quaternion();

  @Override public void setup() {
    size(1024, 768, OPENGL);
    smooth();
    lsystem = LSystem.load("lparticlesystem/lsystem.json");
  }
  
  @Override public void draw() {
    lights();
    background(0);
    
    input();

    camera(0.0f, 0.0f, 0.0f,
        NZ.x, NZ.y, NZ.z,
        Y.x, Y.y, Y.z);
    final Quaternion axisAngle = orientation.toAxisAngle();
    rotate(axisAngle.w, axisAngle.x, axisAngle.y, axisAngle.z);
    translate(-eye.x, -eye.y, -eye.z);
    
    stroke(255);
    strokeWeight(1.0f);
    lsystem.draw(12, this);
    strokeWeight(5.0f);
    line(-100, 0, 0, 100, 0, 0);
    line(0, -100, 0, 0, 100, 0);
    line(0, 0, -100, 0, 0, 100);
    
    final Quaternion inverse = orientation.reciprocal();
    forward = inverse.transform(NZ);
    right = inverse.transform(X);
    up = inverse.transform(Y);
  }

  private void input() {
    if (keyDown('a')) {
      eye.sub(right);
    }
    if (keyDown('d')) {
      eye.add(right);
    }
    if (keyDown('q')) {
      eye.sub(up);
    }
    if (keyDown('z')) {
      eye.add(up);
    }
    if (keyDown('w')) {
      eye.add(forward);
    }
    if (keyDown('s')) {
      eye.sub(forward);
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
  }
  
  @Override public void keyReleased() {
    if (CODED == key) {
      keyCodes.put(keyCode, false);
    } else {
      keys.put(key, false);
    }
  }
}
