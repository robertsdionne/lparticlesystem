package lparticlesystem;

import processing.core.PApplet;

@SuppressWarnings("serial")
public class LParticleSystem extends PApplet {
  
  private LSystem lsystem;

  public void setup() {
    size(1024, 768, P3D);
    smooth();
    lsystem = LSystem.load("lparticlesystem/lsystem.json");
  }
  
  public void draw() {
    lights();
    background(0);
    
    camera(30.0f, -mouseY, 220.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f);
    
    stroke(255);
    strokeWeight(5.0f);
    line(-100, 0, 0, 100, 0, 0);
    line(0, -100, 0, 0, 100, 0);
    line(0, 0, -100, 0, 0, 100);
    strokeWeight(1.0f);
    lsystem.draw(12, this);
  }
}
