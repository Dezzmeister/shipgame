import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.net.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class shipgame extends PApplet {



Server server;
Client client;

PGraphics ship;
PGraphics eShip;

PGraphics menu;

PGraphics cursor;

PGraphics loading;

PGraphics space;

PImage stars;

float shipX;
float shipY;
float shipR = 0;
float shipXInterp;
float shipYInterp;
boolean[] keys;
float noiseVal;
float noiseScale = 0.02f;
int location = 0;
float shipAngle;
float moveDist = 10;
float angleInterval = PI/60;
float interpDist;

float minWidthBoundary;
float minHeightBoundary;

int bulletInc = 1;

int playerScore = 0;
int eScore = 0;

PGraphics playerScoreNum;
PGraphics eScoreNum;

ShipProjectile[] bullets;

Planet planet;

Planet redPlanet;

Planet greyPlanet;

ShipProjectile testBullet;

Button connect;
Button host;
Button playOffline;
Button exitButton;

boolean inMenu = true;
boolean inHostMenu = false;
boolean inConnectMenu = false;
boolean initialized = false;

final int HI = 1;
final int LO = 0;
final int DOUBLE = 1;
final int SINGLE = 0;

boolean connecting = false;
boolean connected = false;
boolean hosting = true;
boolean initializedMultiplayer = false;
boolean hasData = false;

float eShipAngle;
float eShipX;
float eShipY;

float colRadius = 25;

float sentPacket;
float receivedPacket;

ShipProjectile[] eBullets;

int port = 10221;
String ip;

String input;
String[] data;

PFont harambe100;
PFont harambe150;
PFont harambe29;
PFont harambe75;
PFont harambe200;

public void setup() {
  //size(700,700);
  
  stars = loadImage("/data/stars.jpg");
  
  noCursor();
  
  harambe100 = createFont("/fonts/harambe8/harambe8.ttf",100,false);
  harambe150 = createFont("/fonts/harambe8/harambe8.ttf",150,false);
  harambe29 = createFont("/fonts/harambe8/harambe8.ttf",29,false);
  harambe75 = createFont("/fonts/harambe8/harambe8.ttf",75,false);
  harambe200 = createFont("/fonts/harambe8/harambe8.ttf",200,false);
  
  minWidthBoundary = width;
  minHeightBoundary = height;
  //minWidthBoundary = 500;
  //minHeightBoundary = 500;
  
  //1366 by 768
  
  space = createGraphics(width,height);
  space.beginDraw();
  space.background(0);
  space.endDraw();  
  
  playerScoreNum = createGraphics(300,300,JAVA2D);
  playerScoreNum.beginDraw();
  playerScoreNum.background(0,0);
  playerScoreNum.fill(0,0,255);
  playerScoreNum.endDraw();
  
  eScoreNum = createGraphics(300,300,JAVA2D);
  eScoreNum.beginDraw();
  eScoreNum.background(0,0);
  eScoreNum.fill(0,0,255);
  eScoreNum.endDraw();
  
  keys = new boolean[5];
  shipX = minWidthBoundary/2;
  shipY = minHeightBoundary/2;
  shipXInterp = minWidthBoundary/2;
  shipYInterp = minHeightBoundary/2;
  
  ship = createGraphics((int)map(65,0,1920,0,width),(int)map(65,0,1080,0,height));
  eShip = createGraphics((int)map(65,0,1920,0,width),(int)map(65,0,1080,0,height));
  menu = createGraphics(width,height,JAVA2D);
  loading = createGraphics(width,height,JAVA2D);
  cursor = createGraphics(20,20);
  cursor.beginDraw();
  cursor.strokeWeight(2);
  cursor.background(0,0);
  cursor.stroke(0);
  cursor.line(cursor.width/2,0,cursor.width/2,cursor.height);
  cursor.line(0,cursor.height/2,width,cursor.height/2);
  cursor.endDraw();
  
  connect = new Button(menu.width/2,(6*menu.height/10),200,50);
  host = new Button(menu.width/2,(7*menu.height/10),200,50);
  playOffline = new Button(menu.width/2,(8*menu.height/10),200,50);
  exitButton = new Button(menu.width/2,(9*menu.height/10),200,50);
  
  menu.beginDraw();
  menu.background(175);
  menu.textSize(150);
  menu.textFont(harambe150);
  menu.textAlign(CENTER,CENTER);
  menu.fill(0);
  menu.text("SPES GEM",menu.width/2,menu.height/7);
  menu.endDraw();
  
  loading.beginDraw();
  loading.background(175);
  loading.textSize(200);
  loading.textFont(harambe200);
  loading.textAlign(CENTER,CENTER);
  loading.fill(0);
  loading.text("Loading...",loading.width/2,(loading.height/2)-(loading.height/64));
  loading.textSize(75);
  loading.textFont(harambe75);
  loading.text("Generating noise textures...",loading.width/2,(3*loading.height/4)-(loading.height/64));
  loading.endDraw();
  
  noiseDetail(32);
  planet = new Planet(100,color(0,100,157));
  
  redPlanet = new Planet(300,color(200,40,75));
  
  redPlanet.setStretch(2,1);
  
  greyPlanet = new Planet(500,color(120));
  greyPlanet.setStretch(1,1);
  
  testBullet = new ShipProjectile();
  
  bullets = new ShipProjectile[30];
  eBullets = new ShipProjectile[bullets.length];
  
  for (int i = 0; i < bullets.length; i++) {
    bullets[i] = new ShipProjectile();
    eBullets[i] = new ShipProjectile();
  }
  
  data = new String[bullets.length+3];
  
  surface.setTitle("Teh Ship Game");
}

public void initServer() {
  server = new Server(this, port);
  initializedMultiplayer = true;
}

public void initClient() {
  client = new Client(this, ip, port);
  initializedMultiplayer = true;
}

public void writeServerData() {
  String projData = "a" + shipAngle + " x" + shipX + " y" + shipY + " " + playerScore + " ";
  for (int i = 0; i < bullets.length; i++) {
    projData  = projData + bullets[i].convToString() + " ";
      //println(bullets[i].convToString()+"clientdata");
      bullets[i].sent();
  }
  projData = projData + "end";
  sentPacket = millis();
  server.write(projData);
}

public void writeClientData() {
  String projData = "a" + shipAngle + " x" + shipX + " y" + shipY + " " + playerScore + " ";
  for (int i = 0; i < bullets.length; i++) {
    projData  = projData + bullets[i].convToString() + " ";
      //println(bullets[i].convToString()+"clientdata");
      bullets[i].sent();
  }
  projData = projData + "end";
  sentPacket = millis();
  client.write(projData);
}

public void readClientData() {
  client = server.available();
  if (client != null) {
    connected = true;
    //println(input);
    input = client.readString();
    if (input.length() > 20 && input.indexOf("end") != -1) {
      input = input.substring(0,input.indexOf("end"));
      data = split(input,' ');
      if (data.length > 5) {
        if (data[0].indexOf('a') == 0) {
          eShipAngle = PApplet.parseFloat(data[0].substring(1));
        }
        if (data[1].indexOf('x') == 0) {
          eShipX = PApplet.parseFloat(data[1].substring(1));
        }
        if (data[2].indexOf('y') == 0) {
          eShipY = PApplet.parseFloat(data[2].substring(1));
        }
        if (eScore + 2 > PApplet.parseInt(data[3]) && eScore - 2 < PApplet.parseInt(data[3])) {
          eScore = PApplet.parseInt(data[3]);
        }
      }
      hasData = true;
      for (int i = 4; i < data.length; i++) {
        if (data[i] != null && data[i] != "" && i < bullets.length-7) {
          eBullets[i-4].interpretString(data[i]);
        }
      }
    }
    receivedPacket = millis();
    if (sentPacket < receivedPacket) {
      println("Ping: "+(receivedPacket-sentPacket));
    }
  }
  if (hasData) {
    
  }
}

public void handleMultiplayer() {
  if (initializedMultiplayer) {
    if (hosting) {
      writeServerData();
      readClientData();
    }
    if (connecting) {
      writeClientData();
      readServerData();
    }
    if (hasData) {
      drawEBullets();
      drawEShip();
    }
  }
}

public void drawEBullets() {
  for (int i = 0; i < eBullets.length; i++) {
    eBullets[i].drawBullet();
  }
}

public void drawEShip() {
  imageMode(CENTER);
  image(eShip,eShipX,eShipY);
}

public void readServerData() {
  if (client.available() > 0) {
    connected = true;
    input = client.readString();
    //println(input);
    if (input.length() > 20 && input.indexOf("end") != -1) {
      input = input.substring(0,input.indexOf("end"));
      data = split(input,' ');
      if (data.length > 5) {
        if (data[0].indexOf('a') == 0) {
          eShipAngle = PApplet.parseFloat(data[0].substring(1));
        }
        if (data[1].indexOf('x') == 0) {
          eShipX = PApplet.parseFloat(data[1].substring(1));
        }
        if (data[2].indexOf('y') == 0) {
          eShipY = PApplet.parseFloat(data[2].substring(1));
        }
        if (eScore + 2 > PApplet.parseInt(data[3]) && eScore - 2 < PApplet.parseInt(data[3])) {
          eScore = PApplet.parseInt(data[3]);
        }
      }
      hasData = true;
      for (int i = 4; i < data.length; i++) {
        if (data[i] != null && data[i] != "" && i < bullets.length-7) {
          eBullets[i-4].interpretString(data[i]);
        }
      }
    }
    receivedPacket = millis();
    if (sentPacket < receivedPacket) {
      println("Ping: "+(receivedPacket-sentPacket));
    }
  }
  if (hasData) {
    
  }
}

public void initMultiplayer() {
  if (hosting) {
    initServer();
  }
  if (connecting) {
    initClient();
  }
}

public void draw() {
  if (inMenu) {
    menuAction();
    checkMenuButtons();
  } else {
    if (!initializedMultiplayer && (hosting || connecting)) {
      initMultiplayer();
    }
    initBG();
    drawText();
    drawPlanets();
    drawBoundary();
    drawGUI();
    drawBullets();
    drawShip();
    handleMultiplayer();
    keyAction();
  }
}

public void drawGUI() {
  playerScoreNum.beginDraw();
  playerScoreNum.background(0,0);
  playerScoreNum.fill(0,0,255);
  playerScoreNum.textSize(100);
  playerScoreNum.textFont(harambe100);
  playerScoreNum.textAlign(CENTER,CENTER);
  playerScoreNum.text(playerScore,playerScoreNum.width/2,(playerScoreNum.height/2)-(playerScoreNum.height/64));
  playerScoreNum.endDraw();
  
  eScoreNum.beginDraw();
  eScoreNum.background(0,0);
  eScoreNum.fill(255,0,0);
  eScoreNum.textSize(100);
  eScoreNum.textFont(harambe100);
  eScoreNum.textAlign(CENTER,CENTER);
  eScoreNum.text(eScore,eScoreNum.width/2,(eScoreNum.height/2)-(eScoreNum.height/64));
  eScoreNum.endDraw();
  
  imageMode(CORNER);
  image(playerScoreNum,10,10);
  if (initializedMultiplayer) {
    image(eScoreNum,minWidthBoundary-(eScoreNum.width+10),10);
  }
}

public void initBG() {
  if (!initialized) {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
      }
    }
    
    planet.createPlanet();
    redPlanet.createPlanet();
    greyPlanet.createPlanet();
    initialized = true;
  }
}

public void drawPlanets() {
  imageMode(CENTER);
  planet.drawPlanet(map(1580,0,1920,0,width),map(700,0,1080,0,height));
  redPlanet.drawPlanet(600,800);
  greyPlanet.drawPlanet(1107,456);
}

public void keyAction() {
  if (keys[0]) {
    shipR = moveDist;
  }
  if (keys[1]) {
    shipR = -moveDist;
  }
  if (!keys[0] && !keys[1]) {
    shipR = 0;
  }
  if (keys[2]) {
    shipAngle -= angleInterval;
  }
  if (keys[3]) {
    shipAngle += angleInterval;
  }
  polarConversion();
  interpDist = dist(shipX,shipY,shipXInterp,shipYInterp);
}

public void polarConversion() {
  shipXInterp += GameMath.findX(shipAngle + (PI/2),shipR);
  shipYInterp += GameMath.findY(shipAngle + (PI/2),shipR);
}
    
public void drawShip() {
  imageMode(CENTER);
  image(ship,shipX,shipY);
  shipX = lerp(shipX,shipXInterp,0.05f);
  shipY = lerp(shipY,shipYInterp,0.05f);
  shipX = constrain(shipX,1,minWidthBoundary);
  shipY = constrain(shipY,1,minHeightBoundary);
  shipXInterp = constrain(shipXInterp,-50-(colRadius),minWidthBoundary+50+colRadius);
  shipYInterp = constrain(shipYInterp,-50-(colRadius),minHeightBoundary+50+colRadius);
  
  //shipAngle = atan2(shipY-shipYInterp,shipX-shipXInterp)+PI/2;
  
  ship.beginDraw();
  ship.background(0,0);
  ship.translate(ship.width/2,ship.height/2);
  ship.rotate(shipAngle);
  ship.noStroke();
  ship.fill(0,0,255);
  ship.beginShape();
  ship.vertex(0,25);
  ship.vertex(-15,-25);
  ship.vertex(0,-20);
  ship.vertex(15,-25);
  ship.endShape(CLOSE);
  ship.endDraw();
  
  eShip.beginDraw();
  eShip.background(0,0);
  eShip.translate(eShip.width/2,eShip.height/2);
  eShip.rotate(eShipAngle);
  eShip.noStroke();
  eShip.fill(255,0,0);
  eShip.beginShape();
  eShip.vertex(0,25);
  eShip.vertex(-15,-25);
  eShip.vertex(0,-20);
  eShip.vertex(15,-25);
  eShip.endShape(CLOSE);
  eShip.endDraw();
}

public void menuAction() {
  imageMode(CORNER);
  image(menu,0,0);
  
  connect.setTextSize(map(35,0,1080,0,height));
  connect.setText("Connect");
  connect.drawButton();
  
  host.setTextSize(map(35,0,1080,0,height));
  host.setText("Host");
  host.drawButton();
  
  playOffline.setTextSize(map(35,0,1080,0,height));
  playOffline.setText("Play Offline");
  playOffline.drawButton();
  
  exitButton.setTextSize(map(35,0,1080,0,height));
  exitButton.setText("Exit");
  exitButton.drawButton();
}

public void drawText() {
  background(0);
  fill(255);
  //text(bullets[bulletInc-1].toString(),200,200);
}

public void drawBoundary() {
  fill(25);
  noStroke();
  rectMode(CORNER);
  rect(minWidthBoundary,0,width-minWidthBoundary,height);
  rect(0,minHeightBoundary,width,height-minHeightBoundary);
}

public void fireBullet() {
  bullets[bulletInc].fire();
  bulletInc++;
  if (bulletInc >= bullets.length-10) {
    bulletInc = 0;
  }
}

public void drawBullets() {
  for (int i = 0; i < bullets.length; i++) {
    bullets[i].drawBullet();
    if (!bullets[i].isCollided() && hasData && (dist(eShipX,eShipY,bullets[i].getX(),bullets[i].getY()) < colRadius)) {
      playerScore++;
      bullets[i].collide();
      bullets[i].setX(bullets[i].getX()+5000);
      bullets[i].setY(bullets[i].getY()+5000);
      bullets[i].setEndX(bullets[i].getEndX()+5000);
      bullets[i].setEndY(bullets[i].getEndY()+5000);
    }
  }
}

public void checkMenuButtons() {
  if (inMenu) {
    if (!connect.isHovering() && !host.isHovering() && !playOffline.isHovering() && !exitButton.isHovering()) {
      cursor.beginDraw();
      cursor.strokeWeight(2);
      cursor.stroke(0);
      cursor.background(0,0);
      cursor.line(cursor.width/2,0,cursor.width/2,cursor.height);
      cursor.line(0,cursor.height/2,width,cursor.height/2);
      cursor.endDraw();
    } else {
      cursor.beginDraw();
      cursor.noStroke();
      cursor.background(0,0);
      cursor.fill(0xffA09B6A);
      cursor.ellipse(cursor.width/2,cursor.height/2,cursor.width,cursor.height);
      cursor.endDraw();
    }
    imageMode(CENTER);
    image(cursor,mouseX,mouseY);
  }
}

public void loadingScreen() {
  imageMode(CORNER);
  image(loading,0,0);
  imageMode(CENTER);
}

public void mouseReleased() {
  if (inMenu) {
    if (connect.isHovering()) {
      inConnectMenu = true;
    }
    if (host.isHovering()) {
      inHostMenu = true;
    }
    if (playOffline.isHovering()) {
      inMenu = false;
      loadingScreen();
    }
    if (exitButton.isHovering()) {
      exit();
    }
  } else {
    fireBullet();
  }
}

public void keyPressed() {
  switch (keyCode) {
    case 87: //w
    case UP:
      keys[0] = true;
      break;
    case 83: //s
    case DOWN:
      keys[1] = true;
      break;
    case 65: //a
    case LEFT:
      keys[2] = true;
      break;
    case 68: //d
    case RIGHT:
      keys[3] = true;
      break;
    case 32: //SPACE
    case 10: //ENTER
    case 13: //RETURN
    if (!inMenu) {
      if (!keys[4]) {
        fireBullet();
      }
    }
      keys[4] = true;
      break;
  }
}

public void keyReleased() {
  switch (keyCode) {
    case 87: //w
    case UP:
      keys[0] = false;
      break;
    case 83: //s
    case DOWN:
      keys[1] = false;
      break;
    case 65: //a
    case LEFT:
      keys[2] = false;
      break;
    case 68: //d
    case RIGHT:
      keys[3] = false;
      break;
    case 32: //SPACE
    case 10: //ENTER
    case 13: //RETURN
      keys[4] = false;
      break;
  }
}    
public class Button {  
  float buttonWidth;
  float buttonHeight;
  float xCoor;
  float yCoor;
  
  float buttonTextSize = 10;
  String buttonText = "";
  
  int buttonMode = CORNER;
  
  boolean enabled = true;
  
  Button() {
    xCoor = width/2;
    yCoor = height/2;
    buttonWidth = width/16;
    buttonHeight = height/16; 
  }
  
  Button(float x, float y) {
    xCoor = x;
    yCoor = y;
    buttonWidth = width/16;
    buttonHeight = height/16;
  }
  
  Button(float x, float y, float bWidth, float bHeight) {
    xCoor = x;
    yCoor = y;
    buttonWidth = bWidth;
    buttonHeight = bHeight;
  }
  
  public void setLocation(float x, float y) {
    if (buttonMode == CENTER) {
      xCoor = x;
      yCoor = y;
    } else {
      xCoor = x+(buttonWidth/2);
      yCoor = y+(buttonHeight/2);
    }
  }
  
  public void setSize(float bWidth, float bHeight) {
    buttonWidth = bWidth;
    buttonHeight = bHeight;
  }
  
  public void buttonMode(int mode) {
    if (mode == CENTER) {
      buttonMode = CENTER;
    } else {
      buttonMode = CORNER;
    }
  }
  
  public void setButton(float x, float y, float bWidth, float bHeight) {
    if (buttonMode == CENTER) {
      xCoor = x;
      yCoor = y;
    } else {
      xCoor = x+(bWidth/2);
      yCoor = y+(bHeight/2);
    }
    buttonWidth = bWidth;
    buttonHeight = bHeight;
  }
  
  public void setTextSize(float size) {
    buttonTextSize = size;
  }
  
  public void setText(String text) {
    buttonText = text;
  }
  
  public void enableButton() {
    enabled = true;
  }
  
  public void disableButton() {
    enabled = false;
  }
  
  public void drawButton() {
    if (isHovering()) {
      noStroke();
    } else {
      strokeWeight(2);
      stroke(0);
    }
    fill(120);
    rectMode(CENTER);
    rect(xCoor,yCoor,buttonWidth,buttonHeight);
    textAlign(CENTER,CENTER);
    fill(0);
    textSize(buttonTextSize);
    textFont(harambe29);
    text(buttonText,xCoor,yCoor-(buttonWidth/64));
  }
      
  public boolean isHovering() {
    if (enabled) {
      return (mouseX >= xCoor-(buttonWidth/2) && mouseX <= xCoor+(buttonWidth/2) && mouseY >= yCoor-(buttonHeight/2) && mouseY <= yCoor+(buttonHeight/2));
    }
    return false;
  }
   
}
//A static class to provide functions for polar/cartesian conversion

public static class GameMath {
  
  //Polar to cartesian conversion
  public static float findX(float theta, float r) {
    return (r * (cos(theta)));
  }
  
  public static float findY(float theta, float r) {
    return (r * (sin(theta)));
  }
  
  //If cartesian to polar conversion is needed, it will be added here
}
//The InteractiveTextField class from Teh Game, optimized for Gingrich

public class InteractiveTextField {
  FieldType textFieldType;
  
  PGraphics textField;
  PGraphics returnField;
  
  int fieldWidth;
  int fieldHeight;
  String inputText = "";
  String returnText = "";
  String tempInputText = "";
  PFont inputFont;
  boolean inTextField = true;
  char activateKey;
  float xCoord;
  float yCoord;
  boolean keyHasBeenPressed = false;
  boolean shiftPressed = false;
  char shiftKey = SHIFT;
  char backspaceKey = BACKSPACE;
  boolean[] keyArray = new boolean[2];
  StringBuilder fieldBuilder = new StringBuilder(inputText);
  boolean playerNameCanBeDezzy = false;
  String receivedInput = "";
  boolean submitted = false;
  
  float size;
  boolean sizeSet = false;
  
  int maxChars = 50;
  
  InteractiveTextField(FieldType type) {
    textFieldType = type;
    
    if (type == FieldType.CONSOLE) {
      activateKey = TAB;
    }
    
    textField = createGraphics(fieldWidth,fieldHeight,JAVA2D);
    textField.beginDraw();
    textField.background(150);
    textField.endDraw();
    
    returnField = createGraphics(fieldWidth,fieldHeight,JAVA2D);
    returnField.beginDraw();
    returnField.background(0,0);
    returnField.endDraw();
  }
  
  InteractiveTextField(int textFieldWidth, int textFieldHeight, FieldType type) {
    fieldWidth = textFieldWidth;
    fieldHeight = textFieldHeight;
    textFieldType = type;
    
    if (type == FieldType.CONSOLE) {
      activateKey = TAB;
    }
    
    textField = createGraphics(fieldWidth,fieldHeight,JAVA2D);
    textField.beginDraw();
    textField.background(150);
    textField.endDraw();
    
    returnField = createGraphics(fieldWidth,fieldHeight,JAVA2D);
    returnField.beginDraw();
    returnField.background(0,0);
    returnField.endDraw();
  }
  
  public void initializeTextField(int textFieldWidth, int textFieldHeight) {
    fieldWidth = textFieldWidth;
    fieldHeight = textFieldHeight;
    
    textField = createGraphics(fieldWidth,fieldHeight,JAVA2D);
    textField.beginDraw();
    textField.background(150);
    textField.endDraw();
    
    returnField = createGraphics(fieldWidth,fieldHeight,JAVA2D);
    returnField.beginDraw();
    returnField.background(0,0);
    returnField.endDraw();
  }
  
  public void setMaxChars(int max) {
    maxChars = max;
  }
  
  public void setTextSize(float tSize) {
    sizeSet = true;
    size = tSize;
  }
  
  public void setActivateKey(char tempKey) {
    activateKey = tempKey;
  }
  
  public void setTextFieldLocation(int xPos, int yPos) {
    xCoord = xPos;
    yCoord = yPos;
  }
  
  //WARNING: GOOD LUCK TRYING TO GET CAPITALS TO WORK. THE SHIFT KEY IS A HOT MESS IN PROCESSING, WHICH HAS ALSO BEEN WITNESSED IN OTHER PROGRAMS OF MINE. (It sort of works in a different version of the class.) There is an easy solution, but it's annoying and long.
  public void updateTextField() {
    
    //Checks if you are in the text field and adds keys to inputText as you type
    if (inTextField) {
      //inConsole = true;
      textField.beginDraw();
      textField.background(150);
      textField.strokeWeight(3);
      textField.fill(150);
      textField.rect(0,0,textField.width,textField.height);
      textField.strokeWeight(1);
      textField.fill(0);
      textField.textAlign(LEFT,CENTER);
      if (sizeSet) {
        textField.textSize(size);
      }
      //textField.textFont(harambe20);
      textField.text(inputText,5,(textField.height)/2);
      textField.endDraw();
      
      returnField.beginDraw();
      returnField.background(0,0);
      returnField.strokeWeight(1);
      returnField.fill(180);
      returnField.textAlign(LEFT,CENTER);
      returnField.text(returnText,5,(returnField.height)/2);
      returnField.endDraw();
      
      fieldBuilder.insert(0,inputText);
      imageMode(CENTER);
      image(textField,xCoord,yCoord);
      
      if (textFieldType != FieldType.BASIC) {
        image(returnField,xCoord,yCoord-fieldHeight);
      }
      
      if (keyPressed && !keyHasBeenPressed) {
        if (key != ENTER) {
          if (keys[11]) {
            inputText = inputText + Character.toUpperCase(key);
            shiftPressed = true;
          } else {
              if (keys[12]) {
                if (inputText.length() > 0) {
                  inputText = inputText.substring(0,inputText.length()-1);
                }
              } else {
                if (!keys[11] && !keys[12] && inputText.length() < maxChars) {
                  inputText = inputText + key;
                }
              }
          }
          if (keyCode == 8 && inputText.length() >= 2) {
            inputText = inputText.substring(0,inputText.length()-2);
          }
        }
        if (keys[13]) {
          submitText();
          //inTextField = false;
        }
        keyHasBeenPressed = true;
      }
      
      //removes non-ASCII keys from the field so you can't press backspace or something and get a box
      inputText = inputText.replaceAll("[^\\x00-\\x7F]", "");
      inputText = inputText.replaceAll(Character.toString(backspaceKey), "");
    }
    
    if (!keyPressed && !keys[11]) {
      keyHasBeenPressed = false;
    }
  }
  
  //Other types of fields will be added and used soon
  public void submitText() {
    if (textFieldType == FieldType.CONSOLE) {
      commandChecker();
    }
    if (textFieldType == FieldType.BASIC) {
      handleInput();
    }
  }
  
  public String getInput() {
    return inputText;
  }
  
  public void handleInput() {
    receivedInput = inputText;
    submitted = true;
  }
  
  public String receiveInput() {
    return receivedInput;
  }
  
  public boolean isSubmitted() {
    return submitted;
  }
  
  //if a command made sense and was responded to, commandParsed is true - but if not, you get an error message
  
  boolean commandParsed = false;
  
  public void commandChecker() {
    
    //No commands in this version of InteractiveTextField
    
    if (!commandParsed) {
      returnText = "Invalid input";
    }
    commandParsed = false;
    inputText = "";
  }
}
public class Planet {
  float noiseVal;
  float noiseScale = 0.02f;
  int location = 0;
  
  int hue;
  
  float xCoor, yCoor;
  
  int planetWidth;
  
  PGraphics planet;
  
  boolean collided;
  
  float xStretch = 1;
  float yStretch = 1;
  
  Planet(int xWidth, int hue2) {
    planetWidth = xWidth;
    hue = hue2;
    planet = createGraphics(xWidth,xWidth);    
  }
  
  //Stretches/compresses the perlin noise texture
  public void setStretch(float x, float y) {
    xStretch = x;
    yStretch = y;
  }
  
  //Uses perlin noise to draw the planet within a circle
  public void createPlanet() {
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
  public void drawPlanet(float x, float y) {
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
  
  public void checkForCollisions() {
    
    if (dist(xCoor,yCoor,shipX,shipY) <= planet.width/2) {
      collided = true;
    }
  }
  
}
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
  
  public float getX() {
    return xCoor;
  }
  
  public float getY() {
    return yCoor;
  }
  
  public float getEndY() {
    return endYCoor;
  }
  
  public float getEndX() {
    return endXCoor;
  }
  
  public void setX(float x) {
    xCoor = x;
  }
  
  public void setY(float y) {
    yCoor = y;
  }
  
  public void setEndX(float endX) {
    endXCoor = endX;
  }
  
  public void setEndY(float endY) {
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
  
  public void collide() {
    collided = true;
  }
  
  public boolean isCollided() {
    return collided;
  }
  
  //Places a bullet, but doesn't draw it yet
  public void fire() {
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
  public String convToString() {
    return (getX()+"a"+getY()+"b"+getEndX()+"c"+getEndY());
  }
  
  public ShipProjectile readStringOld(String data) {
    float tempXCoor = PApplet.parseFloat(data.substring(0,data.indexOf('a')));
    float tempYCoor = PApplet.parseFloat(data.substring(data.indexOf('a')+1,data.indexOf('b')));
    float tempEndXCoor = PApplet.parseFloat(data.substring(data.indexOf('b')+1,data.indexOf('c')));
    float tempEndYCoor = PApplet.parseFloat(data.substring(data.indexOf('c')+1));
    
    return new ShipProjectile(tempXCoor,tempYCoor,tempEndXCoor,tempEndYCoor);
  }
  
  public float[] readString(String data) {
    float tempXCoor = PApplet.parseFloat(data.substring(0,data.indexOf('a')));
    float tempYCoor = PApplet.parseFloat(data.substring(data.indexOf('a')+1,data.indexOf('b')));
    float tempEndXCoor = PApplet.parseFloat(data.substring(data.indexOf('b')+1,data.indexOf('c')));
    float tempEndYCoor = PApplet.parseFloat(data.substring(data.indexOf('c')+1));
    
    float[] coords = {tempXCoor,tempYCoor,tempEndXCoor,tempEndYCoor};
    
    return coords;
  }
  
   public void interpretString(String dat) {
    initialized = true;
    //println(dat+"interpret");
    if (dat.length() != 0) {
      xCoor = PApplet.parseFloat(dat.substring(0,dat.indexOf('a')));
      yCoor = PApplet.parseFloat(dat.substring(dat.indexOf('a')+1,dat.indexOf('b')));
      endXCoor = PApplet.parseFloat(dat.substring(dat.indexOf('b')+1,dat.indexOf('c')));
      endYCoor = PApplet.parseFloat(dat.substring(dat.indexOf('c')+1));
    }
  }
  
  //Moves and draws a bullet
  public void drawBullet() {
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
  
  public void sent() {
    sent = true;
  }
  
  public boolean isSent() {
    return sent;
  }
  
  //Checks if the bullet is still on the screen
  public boolean checkIfVisible() {
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
//final ints are boring

public enum FieldType {
  BASIC,
  CHAT,
  CONSOLE;
}

public enum Commands {
  
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "shipgame" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
