package lparticlesystem;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;

import processing.core.PApplet;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class LSystem {
  
  public static LSystem load(final String filename) {
    final JsonParser parser = new JsonParser();
    JsonElement element;
    try {
      element = parser.parse(new JsonReader(new FileReader(new File(filename))));
    } catch (final Throwable rethrown) {
      throw Throwables.propagate(rethrown);
    }
    final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    if (null != element) {
      final JsonObject object = element.getAsJsonObject();
      final String start = object.entrySet().iterator().next().getKey();
      for (final Entry<String, JsonElement> entry : object.entrySet()) {
        builder.put(entry.getKey(), entry.getValue().getAsString());
      }
      return new LSystem(start, builder.build());
    } else {
      return new LSystem("L", builder.build());
    }
  }
  
  private static class State implements Cloneable {
    
    public static final float SIZE_GROWTH = 0.1f;
    public static final float ANGLE_GROWTH = 0.2f; 
    
    public float stepAngle = 30.0f;
    public float stepSize = 10.0f;
    
    public float h = 0.0f;
    public float s = 0.5f;
    public float b = 0.5f;
    
    public State clone() {
      try {
        return (State) super.clone();
      } catch (final Throwable rethrown) {
        throw Throwables.propagate(rethrown);
      }
    }
  }

  private final String start;
  private final Map<String, String> rules;
  private int cachedIterationCount;
  private String cachedSystem;
  
  private LSystem(final String start, final Map<String, String> rules) {
    this.start = start;
    this.rules = rules;
    this.cachedIterationCount = 0;
    this.cachedSystem = null;
  }
  
  public void draw(final int iterations, final PApplet applet) {
    final String system = maybeCacheSystem(iterations);
    State state = new State();
    final Deque<State> stack = new ArrayDeque<>(iterations);
    applet.scale(state.stepSize);
    applet.colorMode(PApplet.HSB, 360.0f, 1.0f, 1.0f);
    for (int i = 0; i < system.length(); ++i) {
      switch (system.charAt(i)) {
        case 'F': {
          applet.line(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
          applet.stroke(applet.color((state.h + 360.0f) % 360.0f, state.s, state.b));
          applet.translate(1.0f, 0.0f, 0.0f);
          break;
        } case '+': {
          state.h += state.stepAngle;
          applet.rotateZ(PApplet.radians(state.stepAngle));
          break;
        } case '-': {
          state.h -= state.stepAngle;
          applet.rotateZ(PApplet.radians(-state.stepAngle));
          break;
        } case '/': {
          state.h += state.stepAngle;
          applet.rotateX(PApplet.radians(state.stepAngle));
          break;
        } case '\\': {
          state.h -= state.stepAngle;
          applet.rotateX(PApplet.radians(-state.stepAngle));
          break;
        } case '}': {
          state.h += state.stepAngle;
          applet.rotateY(PApplet.radians(state.stepAngle));
          break;
        } case '{': {
          state.h -= state.stepAngle;
          applet.rotateY(PApplet.radians(-state.stepAngle));
          break;
        } case '<': {
          state.s *= (1.0f + State.SIZE_GROWTH);
          applet.scale(1.0f + State.SIZE_GROWTH);
          break;
        } case '>': {
          state.s *= (1.0f - State.SIZE_GROWTH);
          applet.scale(1.0f - State.SIZE_GROWTH);
          break;
        } case '(': {
          state.b *= (1.0f + State.ANGLE_GROWTH);
          state.stepAngle *= (1.0f - State.ANGLE_GROWTH);
          break;
        } case ')': {
          state.b *= (1.0f - State.ANGLE_GROWTH);
          state.stepAngle *= (1.0f + State.ANGLE_GROWTH);
          break;
        } case '[': {
          applet.pushMatrix();
          applet.pushStyle();
          stack.push(state.clone());
          break;
        } case ']': {
          applet.popMatrix();
          applet.popStyle();
          state = stack.pop();
          break;
        } case '!': {
          state.stepAngle *= -1.0f;
          break;
        } case '|': {
          applet.rotateZ(PApplet.radians(180.0f));
          break;
        } default: {
          break;
        }
      }
    }
  }

  private String maybeCacheSystem(final int iterations) {
    if (null == cachedSystem || iterations != cachedIterationCount) {
      cachedIterationCount = iterations;
      cachedSystem = start;
      for (int i = 0; i <= iterations; ++i) {
        final StringBuilder builder = new StringBuilder();
        for (int j = 0; j < cachedSystem.length(); ++j) {
          final String rule = cachedSystem.substring(j, j + 1);
          if (rules.containsKey(rule)) {
            if (i < iterations) {
              builder.append(rules.get(rule));
            }
          } else {
            builder.append(rule);
          }
        }
        cachedSystem = builder.toString();
      }
    }
    return cachedSystem;
  }
}
