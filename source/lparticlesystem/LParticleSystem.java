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
  private PVector eye = new PVector(),
      forward = new PVector(0.0f, 0.0f, -1.0f),
      right = new PVector(1.0f, 0.0f, 0.0f),
      up = new PVector(0.0f, 1.0f, 0.0f);
  private Quaternion orientation = new Quaternion();

  @Override public void setup() {
    size(1024, 768, P3D);
    smooth();
    lsystem = LSystem.load("lparticlesystem/lsystem.json");
    
    final Quaternion q = Quaternion.fromAxisAngle(right, (float) Math.PI / 2.0f);
    final PVector result = q.transform(forward);
    System.out.println(result.x + " " + result.y + " " + result.z);
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
    strokeWeight(0.1f);
    lsystem.draw(mouseY / 50, this);
    
    final Quaternion inverse = orientation.reciprocal();
    forward = inverse.transform(NZ);
    right = inverse.transform(X);
    up = inverse.transform(Y);
  }

  private void input() {
    if (keys.containsKey('a') && keys.get('a')) {
      eye.sub(right);
    }
    if (keys.containsKey('d') && keys.get('d')) {
      eye.add(right);
    }
    if (keys.containsKey('q') && keys.get('q')) {
      eye.sub(up);
    }
    if (keys.containsKey('z') && keys.get('z')) {
      eye.add(up);
    }
    if (keys.containsKey('w') && keys.get('w')) {
      eye.add(forward);
    }
    if (keys.containsKey('s') && keys.get('s')) {
      eye.sub(forward);
    }
    if (keyCodes.containsKey(RIGHT) && keyCodes.get(RIGHT)) {
      orientation = Quaternion.fromAxisAngle(Y, 0.1f).times(orientation);
    }
    if (keyCodes.containsKey(LEFT) && keyCodes.get(LEFT)) {
      orientation = Quaternion.fromAxisAngle(Y, -0.1f).times(orientation);
    }
    if (keyCodes.containsKey(DOWN) && keyCodes.get(DOWN)) {
      orientation = Quaternion.fromAxisAngle(X, -0.1f).times(orientation);
    }
    if (keyCodes.containsKey(UP) && keyCodes.get(UP)) {
      orientation = Quaternion.fromAxisAngle(X, 0.1f).times(orientation);
    }
    if (keys.containsKey(',') && keys.get(',')) {
      orientation = Quaternion.fromAxisAngle(NZ, -0.1f).times(orientation);
    }
    if (keys.containsKey('.') && keys.get('.')) {
      orientation = Quaternion.fromAxisAngle(NZ, 0.1f).times(orientation);
    }
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
