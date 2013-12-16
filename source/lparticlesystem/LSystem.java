package lparticlesystem;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
  
  public static LSystem load(final File file) {
    final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    if (null == file) {
      return new LSystem(new Parameters(), "L", builder.build());
    }
    final JsonParser parser = new JsonParser();
    JsonObject object;
    try {
      object = parser.parse(new JsonReader(new FileReader(file))).getAsJsonObject();
    } catch (final Throwable rethrown) {
      throw Throwables.propagate(rethrown);
    }
    if (null != object) {
      final int iterations = object.get("iterations").getAsInt();
      final JsonObject parameters = object.get("parameters").getAsJsonObject();
      final float angleGrowth = parameters.get("angleGrowth").getAsFloat();
      final float sizeGrowth = parameters.get("sizeGrowth").getAsFloat();
      final float stepAngle = parameters.get("stepAngle").getAsFloat();
      final float stepSize = parameters.get("stepSize").getAsFloat();
      final JsonObject system = object.get("system").getAsJsonObject();
      final String start = system.entrySet().iterator().next().getKey();
      for (final Entry<String, JsonElement> entry : system.entrySet()) {
        builder.put(entry.getKey(), entry.getValue().getAsString());
      }
      return new LSystem(new Parameters(iterations, angleGrowth, sizeGrowth, stepAngle, stepSize),
          start, builder.build());
    } else {
      return new LSystem(new Parameters(), "L", builder.build());
    }
  }

  private static final PVector X = new PVector(1.0f, 0.0f, 0.0f);
  private static final PVector Y = new PVector(0.0f, 1.0f, 0.0f);
  private static final PVector Z = new PVector(0.0f, 0.0f, 1.0f);
  private static final PVector NX = new PVector(-1.0f, 0.0f, 0.0f);
  private static final PVector NY = new PVector(0.0f, -1.0f, 0.0f);
  private static final PVector NZ = new PVector(0.0f, 0.0f, -1.0f);
  
  private static class Parameters {

    public final int iterations;
    public final float angleGrowth;
    public final float sizeGrowth; 
    public final float stepAngle;
    public final float stepSize;
    
    public Parameters() {
      iterations = 0;
      angleGrowth = sizeGrowth = stepAngle = stepSize = 0.0f;
    }
    
    public Parameters(final int iterations, final float angleGrowth,
        final float sizeGrowth, final float stepAngle, final float stepSize) {
      this.iterations = iterations;
      this.angleGrowth = angleGrowth;
      this.sizeGrowth = sizeGrowth;
      this.stepAngle = stepAngle;
      this.stepSize = stepSize;
    }
  }
  
  private static class State implements Cloneable {
    
    public float stepAngle = 0.0f;
    public float stepSize = 10.0f;
    
    public PVector position0 = new PVector(), position1 = new PVector();
    public Quaternion orientation = new Quaternion();
    
    public float h = 0.0f;
    public float s = 0.5f;
    public float b = 0.5f;
    
    public State clone() {
      try {
        final State result = (State) super.clone();
        result.position0 = position0.get();
        result.position1 = position1.get();
        result.orientation = orientation.get();
        return result;
      } catch (final Throwable rethrown) {
        throw Throwables.propagate(rethrown);
      }
    }
  }
  
  private static class Node {
    public final List<State> states;
    public final List<Node> children;
    
    public Node(final State state) {
      states = new ArrayList<>();
      children = new ArrayList<>();
      states.add(state);
    }
  }

  private final Parameters parameters;
  private final String start;
  private final Map<String, String> rules;
  private int cachedIterationCount;
  private String cachedSystem;
  
  private LSystem(final Parameters parameters,
      final String start, final Map<String, String> rules) {
    this.parameters = parameters;
    this.start = start;
    this.rules = rules;
    this.cachedIterationCount = 0;
    this.cachedSystem = null;
  }
  
  public void draw(float angleMod, float growMod, final PApplet applet) {
    final String system = maybeCacheSystem(parameters.iterations);
    State state = new State();
    Node node = new Node(state.clone());
    final Node root = node;
    state.stepAngle = parameters.stepAngle;
    state.stepSize = parameters.stepSize;
    final Deque<State> stack = new ArrayDeque<>(parameters.iterations);
    final Deque<Node> tree = new ArrayDeque<>(parameters.iterations);
    for (int i = 0; i < system.length(); ++i) {
      switch (system.charAt(i)) {
        case 'F': {
          state.position0 = state.position1.get();
          final PVector step = state.orientation.transform(
              new PVector(state.stepSize * angleMod, 0.0f, 0.0f));
          state.position1.add(step);
//          applet.stroke(applet.color((state.h + 360.0f) % 360.0f, state.s, state.b));
//          applet.line(state.position0.x, state.position0.y, state.position0.z,
//              state.position1.x, state.position1.y, state.position1.z);
          final Node child = new Node(state.clone());
          node.children.add(child);
          node = child;
          break;
        } case '+': {
          state.h += state.stepAngle * angleMod;
          state.orientation = Quaternion.fromAxisAngle(
              Z, PApplet.radians(state.stepAngle * angleMod)).times(state.orientation);
          break;
        } case '-': {
          state.h -= state.stepAngle * angleMod;
          state.orientation = Quaternion.fromAxisAngle(
              NZ, PApplet.radians(state.stepAngle * angleMod)).times(state.orientation);
          break;
        } case '/': {
          state.h += state.stepAngle * angleMod;
          state.orientation = Quaternion.fromAxisAngle(
              X, PApplet.radians(state.stepAngle * angleMod)).times(state.orientation);
          break;
        } case '\\': {
          state.h -= state.stepAngle * angleMod;
          state.orientation = Quaternion.fromAxisAngle(
              NX, PApplet.radians(state.stepAngle * angleMod)).times(state.orientation);
          break;
        } case '}': {
          state.h += state.stepAngle * angleMod;
          state.orientation = Quaternion.fromAxisAngle(
              Y, PApplet.radians(state.stepAngle * angleMod)).times(state.orientation);
          break;
        } case '{': {
          state.h -= state.stepAngle * angleMod;
          state.orientation = Quaternion.fromAxisAngle(
              NY, PApplet.radians(state.stepAngle * angleMod)).times(state.orientation);
          break;
        } case '<': {
          state.s *= (1.0f + parameters.sizeGrowth);
          state.stepSize *= (1.0f + parameters.sizeGrowth);
          break;
        } case '>': {
          state.s *= (1.0f - parameters.sizeGrowth);
          state.stepSize *= (1.0f - parameters.sizeGrowth);
          break;
        } case '(': {
          state.b *= (1.0f + parameters.angleGrowth * growMod);
          state.stepAngle *= (1.0f - parameters.angleGrowth * growMod);
          break;
        } case ')': {
          state.b *= (1.0f - parameters.angleGrowth * growMod);
          state.stepAngle *= (1.0f + parameters.angleGrowth * growMod);
          break;
        } case '[': {
          stack.push(state.clone());
          final Node child = new Node(stack.peek());
          node.children.add(child);
          node = child;
          tree.push(node);
          break;
        } case ']': {
          state = stack.pop().clone();
          node = tree.pop();
          node.states.add(state.clone());
          break;
        } case '!': {
          state.stepAngle *= -1.0f;
          break;
        } case '|': {
          state.orientation = Quaternion.fromAxisAngle(
              Z, PApplet.radians(180.0f)).times(state.orientation);
          break;
        } default: {
          break;
        }
      }
    }

    applet.colorMode(PApplet.HSB, 360.0f, 1.0f, 1.0f);
    drawNode(root, applet);
  }
  
  private void drawNode(final Node node, final PApplet applet) {
    for (int i = 0; i < node.children.size(); ++i) {
      final Node child = node.children.get(i);
      final State state0 = node.states.get(i);
      final State state1 = child.states.get(0);
      applet.stroke(applet.color((state1.h + 360.0f) % 360.0f, state1.s, state1.b));
      applet.line(state0.position1.x, state0.position1.y, state0.position1.z,
          state1.position1.x, state1.position1.y, state1.position1.z);
      drawNode(child, applet);
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
