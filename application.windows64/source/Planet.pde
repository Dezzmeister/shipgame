public class Planet {
  float noiseVal;
  float noiseScale = 0.02;
  int location = 0;
  
  color hue;
  
  float xCoor, yCoor;
  
  int planetWidth;
  
  PGraphics planet;
  
  boolean collided;
  
  float xStretch = 1;
  float yStretch = 1;
  
  Planet(int xWidth, color hue2) {
    planetWidth = xWidth;
    hue = hue2;
    planet = createGraphics(xWidth,xWidth);    
  }
  
  //Stretches/compresses the perlin noise texture
  void setStretch(float x, float y) {
    xStretch = x;
    yStretch = y;
  }
  
  //Uses perlin noise to draw the planet within a circle
  void createPlanet() {
    planet.beginDraw();
    for (int y = 0; y < planet.height; y++) {
      for (int x = 0; x < planet.width; x++) {
        
        //Tests if (x,y) is in a circle with radius planetWidth
        if (dist(x,y,planet.width/2,planet.height/2) <= planet.width/2) {
          
          //If it is, perlin noise is used to generate a color for the point
          noiseVal = noise(yStretch*x*noiseScale,xStretch*y*noiseScale);
          planet.stroke(noiseVal*red(hue),noiseVal*green(hue),noiseVal*blue(hue));
          planet.point(x,y);
        } else {
          
          //If not, the point is transparent
          planet.stroke(0,0);
          planet.point(x,y);
        }
      }
    }
    planet.endDraw();
  }
  
  //Draws the planet on the screen
  void drawPlanet(float x, float y) {
    xCoor = x;
    yCoor = y;
    image(planet,x,y);
    
    //This program is used in a bigger game (wip) where this code is relevant
    
    /*
    checkForCollisions();
    
    if (collided) {
      //displayOptions();
    }
    collided = false;
    
    */
  }
  
  //This program is used in a bigger game (wip) where this code is relevant
  
  /*
  
  void displayOptions() {
    fill(255);
    rect(shipX-50,shipY-50,95,25);
    fill(0);
    textAlign(CENTER);
    textSize(10);
    text("Click to land",shipX-3,shipY-33);
  }
  
  */
  
  void checkForCollisions() {
    
    if (dist(xCoor,yCoor,shipX,shipY) <= planet.width/2) {
      collided = true;
    }
  }
  
}