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
  
  void setLocation(float x, float y) {
    if (buttonMode == CENTER) {
      xCoor = x;
      yCoor = y;
    } else {
      xCoor = x+(buttonWidth/2);
      yCoor = y+(buttonHeight/2);
    }
  }
  
  void setSize(float bWidth, float bHeight) {
    buttonWidth = bWidth;
    buttonHeight = bHeight;
  }
  
  void buttonMode(int mode) {
    if (mode == CENTER) {
      buttonMode = CENTER;
    } else {
      buttonMode = CORNER;
    }
  }
  
  void setButton(float x, float y, float bWidth, float bHeight) {
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
  
  void setTextSize(float size) {
    buttonTextSize = size;
  }
  
  void setText(String text) {
    buttonText = text;
  }
  
  void enableButton() {
    enabled = true;
  }
  
  void disableButton() {
    enabled = false;
  }
  
  void drawButton() {
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
      
  boolean isHovering() {
    if (enabled) {
      return (mouseX >= xCoor-(buttonWidth/2) && mouseX <= xCoor+(buttonWidth/2) && mouseY >= yCoor-(buttonHeight/2) && mouseY <= yCoor+(buttonHeight/2));
    }
    return false;
  }
   
}