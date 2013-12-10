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
    
    public float x0 = 0.0f, y0 = 0.0f;
    public float x1 = 0.0f, y1 = 0.0f;
    public float orientation = 0.0f;
    public float stepAngle = 30.0f;
    public float stepSize = 10.0f;
    
    public float radians() {
      return (float) Math.PI / 180.0f * orientation;
    }
    
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
    final Deque<State> stack = new ArrayDeque<State>(iterations);
    for (int i = 0; i < system.length(); ++i) {
      switch (system.charAt(i)) {
        case 'F': {
          state.x0 = state.x1;
          state.y0 = state.y1;
          state.x1 += state.stepSize * Math.cos(state.radians());
          state.y1 += state.stepSize * Math.sin(state.radians());
          applet.line(state.x0, state.y0, 0.0f, state.x1, state.y1, 0.0f);
          break;
        } case '+': {
          state.orientation += state.stepAngle;
          break;
        } case '-': {
          state.orientation -= state.stepAngle;
          break;
        } case '<': {
          state.stepSize *= (1.0f + State.SIZE_GROWTH);
          break;
        } case '>': {
          state.stepSize *= (1.0f - State.SIZE_GROWTH);
          break;
        } case '(': {
          state.stepAngle *= (1.0f - State.ANGLE_GROWTH);
          break;
        } case ')': {
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
          state.orientation += 180.0f;
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
