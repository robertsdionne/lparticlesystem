package lparticlesystem;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;

@SuppressWarnings("serial")
public class LParticleSystem extends PApplet {
  
  private final Map<Character, Boolean> keys = new HashMap<Character, Boolean>();
  private final Map<Integer, Boolean> keyCodes = new HashMap<Integer, Boolean>();
  private LSystem lsystem;
  private PVector eye = new PVector();
  private float altitude = 0.0f, azimuth = 0.0f;

  @Override public void setup() {
    size(1024, 768, P3D);
    smooth();
    lsystem = LSystem.load("lparticlesystem/lsystem.json");
  }
  
  @Override public void draw() {
    lights();
    background(0);
    
    input();

    camera(0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, -1.0f,
        0.0f, 1.0f, 0.0f);
    translate(-eye.x, eye.y, -eye.z - 100.0f);
    rotateX(-altitude);
    rotateY(-azimuth);
    
    stroke(255);
    strokeWeight(0.1f);
    lsystem.draw(12, this);
  }

  private void input() {
    if (keys.containsKey('a') && keys.get('a')) {
      eye.x -= 1.0f;
    }
    if (keys.containsKey('d') && keys.get('d')) {
      eye.x += 1.0f;
    }
    if (keys.containsKey('q') && keys.get('q')) {
      eye.y += 1.0f;
    }
    if (keys.containsKey('z') && keys.get('z')) {
      eye.y -= 1.0f;
    }
    if (keys.containsKey('w') && keys.get('w')) {
      eye.z -= 1.0f;
    }
    if (keys.containsKey('s') && keys.get('s')) {
      eye.z += 1.0f;
    }
    if (keyCodes.containsKey(RIGHT) && keyCodes.get(RIGHT)) {
      azimuth -= 0.1f;
    }
    if (keyCodes.containsKey(LEFT) && keyCodes.get(LEFT)) {
      azimuth += 0.1f;
    }
    if (keyCodes.containsKey(DOWN) && keyCodes.get(DOWN)) {
      altitude -= 0.1f;
    }
    if (keyCodes.containsKey(UP) && keyCodes.get(UP)) {
      altitude += 0.1f;
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
