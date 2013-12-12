package lparticlesystem;

import processing.core.PVector;

class Quaternion {

  public float x, y, z, w;

  public Quaternion() {
    w = 1.0f;
    x = y = z = 0.0f;
  }

  public Quaternion(final float x, final float y, final float z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = 0.0f;
  }

  public Quaternion(final PVector vector) {
    x = vector.x;
    y = vector.y;
    z = vector.z;
    w = 0.0f;
  }

  public Quaternion(final float scalar, final PVector vector) {
    x = vector.x;
    y = vector.y;
    z = vector.z;
    w = scalar;
  }

  public Quaternion(final float w, final float x, final float y, final float z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public PVector vector() {
    return new PVector(x, y, z);
  }
  
  public Quaternion get() {
    return new Quaternion(w, x, y, z);
  }

  public static Quaternion fromAxisAngle(final PVector axis, final float angle) {
    final PVector normalizedAxis = axis.get();
    normalizedAxis.setMag((float) Math.sin(angle / 2.0f));
    return new Quaternion((float) Math.cos(angle / 2.0f), normalizedAxis);
  }

  public static Quaternion ln(final Quaternion q) {
    final float magnitude = q.magnitude();
    final PVector normalized = q.vector().get();
    normalized.normalize();
    return new Quaternion(normalized).times((float) Math.acos(q.w / magnitude))
        .plus((float) Math.log(magnitude));
  }

  public Quaternion toAxisAngle() {
    final Quaternion logarithm = Quaternion.ln(this);
    final float magnitude = logarithm.magnitude();
    if (0.0f == magnitude) {
      return new Quaternion(0.0f, 1.0f, 0.0f, 0.0f);
    } else {
      return logarithm.over(magnitude).plus(2.0f * magnitude);
    }
  }
  
  public float dot(final Quaternion that) {
    return w * that.w + x * that.x + y * that.y + z * that.z;
  }

  public Quaternion negate() {
    return new Quaternion(-w, -x, -y, -z);
  }

  public float magnitude() {
    return (float) Math.sqrt(magnitudeSquared());
  }

  public float magnitudeSquared() {
    return dot(this);
  }

  public Quaternion normalized() {
    return over(magnitude());
  }

  public Quaternion conjugate() {
    return new Quaternion(w, -x, -y, -z);
  }

  public Quaternion reciprocal() {
    return conjugate().over(magnitudeSquared());
  }

  public Quaternion plus(final float that) {
    return new Quaternion(w + that, x, y, z);
  }

  public Quaternion plus(final PVector that) {
    return new Quaternion(w, x + that.x, y + that.y, z + that.z);
  }

  public Quaternion plus(final Quaternion that) {
    return new Quaternion(w + that.w, x + that.x, y + that.y, z + that.z);
  }

  public Quaternion minus(final float that) {
    return new Quaternion(w - that, x, y, z);
  }

  public Quaternion minus(final PVector that) {
    return new Quaternion(w, x - that.x, y - that.y, z - that.z);
  }

  public Quaternion minus(final Quaternion that) {
    return new Quaternion(w - that.w, x - that.x, y - that.y, z - that.z);
  }

  public Quaternion times(final float that) {
    return new Quaternion(w * that, x * that, y * that, z * that);
  }

  public Quaternion times(final PVector that) {
    return times(new Quaternion(that));
  }

  public Quaternion times(final Quaternion that) {
    final PVector vector0 = that.vector();
    vector0.mult(w);
    final PVector vector1 = vector();
    vector1.mult(that.w);
    final PVector vector2 = vector().cross(that.vector());
    vector0.add(vector1);
    vector0.add(vector2);
    return new Quaternion(w * that.w - vector().dot(that.vector()), vector0);
  }

  public Quaternion over(final float that) {
    return new Quaternion(w / that, x / that, y / that, z / that);
  }

  public Quaternion over(final PVector that) {
    return over(new Quaternion(that));
  }

  public Quaternion over(final Quaternion that) {
    return times(that.reciprocal());
  }

  public PVector transform(final PVector that) {
    return times(that).times(reciprocal()).vector();
  }
}
