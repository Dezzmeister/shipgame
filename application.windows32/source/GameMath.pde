//A static class to provide functions for polar/cartesian conversion

public static class GameMath {
  
  //Polar to cartesian conversion
  static float findX(float theta, float r) {
    return (r * (cos(theta)));
  }
  
  static float findY(float theta, float r) {
    return (r * (sin(theta)));
  }
  
  //If cartesian to polar conversion is needed, it will be added here
}