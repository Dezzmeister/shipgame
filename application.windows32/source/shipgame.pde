import processing.net.*;

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
float noiseScale = 0.02;
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

void setup() {
  //size(700,700);
  
  stars = loadImage("/data/stars.jpg");
  fullScreen();
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

void initServer() {
  server = new Server(this, port);
  initializedMultiplayer = true;
}

void initClient() {
  client = new Client(this, ip, port);
  initializedMultiplayer = true;
}

void writeServerData() {
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

void writeClientData() {
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

void readClientData() {
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
          eShipAngle = float(data[0].substring(1));
        }
        if (data[1].indexOf('x') == 0) {
          eShipX = float(data[1].substring(1));
        }
        if (data[2].indexOf('y') == 0) {
          eShipY = float(data[2].substring(1));
        }
        if (eScore + 2 > int(data[3]) && eScore - 2 < int(data[3])) {
          eScore = int(data[3]);
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

void handleMultiplayer() {
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

void drawEBullets() {
  for (int i = 0; i < eBullets.length; i++) {
    eBullets[i].drawBullet();
  }
}

void drawEShip() {
  imageMode(CENTER);
  image(eShip,eShipX,eShipY);
}

void readServerData() {
  if (client.available() > 0) {
    connected = true;
    input = client.readString();
    //println(input);
    if (input.length() > 20 && input.indexOf("end") != -1) {
      input = input.substring(0,input.indexOf("end"));
      data = split(input,' ');
      if (data.length > 5) {
        if (data[0].indexOf('a') == 0) {
          eShipAngle = float(data[0].substring(1));
        }
        if (data[1].indexOf('x') == 0) {
          eShipX = float(data[1].substring(1));
        }
        if (data[2].indexOf('y') == 0) {
          eShipY = float(data[2].substring(1));
        }
        if (eScore + 2 > int(data[3]) && eScore - 2 < int(data[3])) {
          eScore = int(data[3]);
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

void initMultiplayer() {
  if (hosting) {
    initServer();
  }
  if (connecting) {
    initClient();
  }
}

void draw() {
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

void drawGUI() {
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

void initBG() {
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

void drawPlanets() {
  imageMode(CENTER);
  planet.drawPlanet(map(1580,0,1920,0,width),map(700,0,1080,0,height));
  redPlanet.drawPlanet(600,800);
  greyPlanet.drawPlanet(1107,456);
}

void keyAction() {
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

void polarConversion() {
  shipXInterp += GameMath.findX(shipAngle + (PI/2),shipR);
  shipYInterp += GameMath.findY(shipAngle + (PI/2),shipR);
}
    
void drawShip() {
  imageMode(CENTER);
  image(ship,shipX,shipY);
  shipX = lerp(shipX,shipXInterp,0.05);
  shipY = lerp(shipY,shipYInterp,0.05);
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

void menuAction() {
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

void drawText() {
  background(0);
  fill(255);
  //text(bullets[bulletInc-1].toString(),200,200);
}

void drawBoundary() {
  fill(25);
  noStroke();
  rectMode(CORNER);
  rect(minWidthBoundary,0,width-minWidthBoundary,height);
  rect(0,minHeightBoundary,width,height-minHeightBoundary);
}

void fireBullet() {
  bullets[bulletInc].fire();
  bulletInc++;
  if (bulletInc >= bullets.length-10) {
    bulletInc = 0;
  }
}

void drawBullets() {
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

void checkMenuButtons() {
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
      cursor.fill(#A09B6A);
      cursor.ellipse(cursor.width/2,cursor.height/2,cursor.width,cursor.height);
      cursor.endDraw();
    }
    imageMode(CENTER);
    image(cursor,mouseX,mouseY);
  }
}

void loadingScreen() {
  imageMode(CORNER);
  image(loading,0,0);
  imageMode(CENTER);
}

void mouseReleased() {
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

void keyPressed() {
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

void keyReleased() {
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