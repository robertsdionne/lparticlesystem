package lparticlesystem;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;

import processing.core.PApplet;
import processing.core.PVector;

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

  private static final PVector X = new PVector(1.0f, 0.0f, 0.0f);
  private static final PVector Y = new PVector(0.0f, 1.0f, 0.0f);
  private static final PVector Z = new PVector(0.0f, 0.0f, 1.0f);
  private static final PVector NX = new PVector(-1.0f, 0.0f, 0.0f);
  private static final PVector NY = new PVector(0.0f, -1.0f, 0.0f);
  private static final PVector NZ = new PVector(0.0f, 0.0f, -1.0f);
  
  private static class State implements Cloneable {
    
    public static final float SIZE_GROWTH = -1.359672f;
    public static final float ANGLE_GROWTH = -0.138235f;
    
    public float stepAngle = -3963.7485f;
    public float stepSize = 1.411f;
    
    public PVector position0 = new PVector(), position1 = new PVector();
    public Quaternion orientation = new Quaternion();
    
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
    applet.colorMode(PApplet.HSB, 360.0f, 1.0f, 1.0f);
    for (int i = 0; i < system.length(); ++i) {
      switch (system.charAt(i)) {
        case 'F': {
          state.position0 = state.position1.get();
          final PVector step = state.orientation.transform(new PVector(state.stepSize, 0.0f, 0.0f));
          state.position1.add(step);
//          applet.stroke(applet.color((state.h + 360.0f) % 360.0f, state.s, state.b));
          applet.line(state.position0.x, state.position0.y, state.position0.z,
              state.position1.x, state.position1.y, state.position1.z);
          break;
        } case '+': {
          state.h += state.stepAngle;
          state.orientation = Quaternion.fromAxisAngle(
              Z, PApplet.radians(state.stepAngle)).times(state.orientation);
          break;
        } case '-': {
          state.h -= state.stepAngle;
          state.orientation = Quaternion.fromAxisAngle(
              NZ, PApplet.radians(state.stepAngle)).times(state.orientation);
          break;
        } case '/': {
          state.h += state.stepAngle;
          state.orientation = Quaternion.fromAxisAngle(
              X, PApplet.radians(state.stepAngle)).times(state.orientation);
          break;
        } case '\\': {
          state.h -= state.stepAngle;
          state.orientation = Quaternion.fromAxisAngle(
              NX, PApplet.radians(state.stepAngle)).times(state.orientation);
          break;
        } case '}': {
          state.h += state.stepAngle;
          state.orientation = Quaternion.fromAxisAngle(
              Y, PApplet.radians(state.stepAngle)).times(state.orientation);
          break;
        } case '{': {
          state.h -= state.stepAngle;
          state.orientation = Quaternion.fromAxisAngle(
              NY, PApplet.radians(state.stepAngle)).times(state.orientation);
          break;
        } case '<': {
          state.s *= (1.0f + State.SIZE_GROWTH);
          state.stepSize *= (1.0f + State.SIZE_GROWTH);
          break;
        } case '>': {
          state.s *= (1.0f - State.SIZE_GROWTH);
          state.stepSize *= (1.0f - State.SIZE_GROWTH);
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
          stack.push(state.clone());
          break;
        } case ']': {
          state = stack.pop();
          break;
        } case '!': {
          state.stepAngle *= -1.0f;
          break;
        } case '|': {
          state.orientation = Quaternion.fromAxisAngle(
              X, PApplet.radians(180.0f)).times(state.orientation);
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
