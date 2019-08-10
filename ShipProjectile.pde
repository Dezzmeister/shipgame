public class ShipProjectile {
  
  //Coordinates of the rear end of the bullet
  float xCoor = -100;
  float yCoor = -100;
  
  float bulletLength = 40;
  int bulletMode = SINGLE;
  boolean doubleSwitcher = false;
  
  //Coordinates of the front end of the bullet
  float endXCoor;
  float endYCoor;
  
  //How much to increase x and y coordinates of bullet endpoints every frame
  float yChange;
  float xChange;
  
  float xCoor2 = -100;
  float yCoor2 = -100;
  
  float endXCoor2;
  float endYCoor2;
  
  float xChange2;
  float yChange2;
  
  float yMult;
  float xMult;
  
  boolean onScreen = false;
  
  boolean clientSide = false;
  
  boolean initialized = false;
  
  boolean sent = false;
  
  boolean collided = false;
  
  float getX() {
    return xCoor;
  }
  
  float getY() {
    return yCoor;
  }
  
  float getEndY() {
    return endYCoor;
  }
  
  float getEndX() {
    return endXCoor;
  }
  
  void setX(float x) {
    xCoor = x;
  }
  
  void setY(float y) {
    yCoor = y;
  }
  
  void setEndX(float endX) {
    endXCoor = endX;
  }
  
  void setEndY(float endY) {
    endYCoor = endY;
  }
  
  ShipProjectile() {
    
  }
  
  ShipProjectile(float tempX, float tempY, float tempEndX, float tempEndY) {
    xCoor = tempX;
    yCoor = tempY;
    endXCoor = tempEndX;
    endYCoor = tempEndY;
    xChange = endXCoor-xCoor;
    yChange = endYCoor-yCoor;
    
    clientSide = true;
  }
  
  void collide() {
    collided = true;
  }
  
  boolean isCollided() {
    return collided;
  }
  
  //Places a bullet, but doesn't draw it yet
  void fire() {
    if (!checkIfVisible()) {
      collided = false;
      //Sets location, length, and slope of bullet path based on where ship is pointed
      xCoor = shipX;
      yCoor = shipY;
      endXCoor = shipX+(GameMath.findX(shipAngle+(PI/2),bulletLength));
      endYCoor = shipY+(GameMath.findY(shipAngle+(PI/2),bulletLength));
      xChange = endXCoor-xCoor;
      yChange = endYCoor-yCoor;
      
      if (bulletMode == DOUBLE) {
        xCoor = shipX;
        yCoor = shipY;
        endXCoor = shipX+(GameMath.findX(shipAngle+(PI/2),bulletLength));
        endYCoor = shipY+(GameMath.findY(shipAngle+(PI/2),bulletLength));
        xChange = endXCoor-xCoor;
        yChange = endYCoor-yCoor;
        
        xCoor2 = shipX;
        yCoor2 = shipY;
        endXCoor2 = shipX+(GameMath.findX(shipAngle+(PI/2),bulletLength));
        endYCoor2 = shipY+(GameMath.findY(shipAngle+(PI/2),bulletLength));
        xChange2 = endXCoor-xCoor;
        yChange2 = endYCoor-yCoor;
      }
    }
  }
  
  //Converts info about a bullet into a string to be sent over a TCP socket and read by the client
  //Format: "x,y,endX,endY,isVisible"
  String convToString() {
    return (getX()+"a"+getY()+"b"+getEndX()+"c"+getEndY());
  }
  
  ShipProjectile readStringOld(String data) {
    float tempXCoor = float(data.substring(0,data.indexOf('a')));
    float tempYCoor = float(data.substring(data.indexOf('a')+1,data.indexOf('b')));
    float tempEndXCoor = float(data.substring(data.indexOf('b')+1,data.indexOf('c')));
    float tempEndYCoor = float(data.substring(data.indexOf('c')+1));
    
    return new ShipProjectile(tempXCoor,tempYCoor,tempEndXCoor,tempEndYCoor);
  }
  
  float[] readString(String data) {
    float tempXCoor = float(data.substring(0,data.indexOf('a')));
    float tempYCoor = float(data.substring(data.indexOf('a')+1,data.indexOf('b')));
    float tempEndXCoor = float(data.substring(data.indexOf('b')+1,data.indexOf('c')));
    float tempEndYCoor = float(data.substring(data.indexOf('c')+1));
    
    float[] coords = {tempXCoor,tempYCoor,tempEndXCoor,tempEndYCoor};
    
    return coords;
  }
  
   void interpretString(String dat) {
    initialized = true;
    //println(dat+"interpret");
    if (dat.length() != 0) {
      xCoor = float(dat.substring(0,dat.indexOf('a')));
      yCoor = float(dat.substring(dat.indexOf('a')+1,dat.indexOf('b')));
      endXCoor = float(dat.substring(dat.indexOf('b')+1,dat.indexOf('c')));
      endYCoor = float(dat.substring(dat.indexOf('c')+1));
    }
  }
  
  //Moves and draws a bullet
  void drawBullet() {
    if(xCoor != -100 && yCoor != -100 && (checkIfVisible())) {
      //Draws a line of length bulletLength on a path of slope yChange/xChange, which are set in fire()
      stroke(0,255,0);
      
      if (bulletMode == SINGLE) {
        line(xCoor,yCoor,endXCoor,endYCoor);
      }
      if (bulletMode == DOUBLE) {
        line(xCoor,yCoor,endXCoor,endYCoor);
        line(xCoor2,yCoor2,endXCoor2,endYCoor2);
        
        xCoor2 += xChange2;
        yCoor2 += yChange2;
        
        endXCoor2 += xChange2;
        endYCoor2 += yChange2;
      }
      stroke(0);
      xCoor+=xChange;
      yCoor+=yChange;
      endXCoor+=xChange;
      endYCoor+=yChange;
    } else {
      sent = false;
      collided = false;
    }
  }
  
  void sent() {
    sent = true;
  }
  
  boolean isSent() {
    return sent;
  }
  
  //Checks if the bullet is still on the screen
  boolean checkIfVisible() {
    if (bulletMode == SINGLE) {
      if (xCoor >= minWidthBoundary || xCoor <= 0 || yCoor >= minHeightBoundary || yCoor <= 0) {
        xCoor = -100;
        yCoor = -100;
        return false;
      }
    }
    if (bulletMode == DOUBLE) {
      if ((xCoor >= minWidthBoundary || xCoor <= 0 || yCoor >= minHeightBoundary || yCoor <= 0) && (xCoor2 >= minWidthBoundary || xCoor2 <= 0 || yCoor2 >= minHeightBoundary || yCoor2 <= 0)) {
        xCoor = -100;
        yCoor = -100;
        xCoor2 = -100;
        yCoor2 = -100;
        return false;
      }
    }      
    return true;
  }
}