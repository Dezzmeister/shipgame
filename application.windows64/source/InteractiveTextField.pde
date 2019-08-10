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
  
  void initializeTextField(int textFieldWidth, int textFieldHeight) {
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
  
  void setMaxChars(int max) {
    maxChars = max;
  }
  
  void setTextSize(float tSize) {
    sizeSet = true;
    size = tSize;
  }
  
  void setActivateKey(char tempKey) {
    activateKey = tempKey;
  }
  
  void setTextFieldLocation(int xPos, int yPos) {
    xCoord = xPos;
    yCoord = yPos;
  }
  
  //WARNING: GOOD LUCK TRYING TO GET CAPITALS TO WORK. THE SHIFT KEY IS A HOT MESS IN PROCESSING, WHICH HAS ALSO BEEN WITNESSED IN OTHER PROGRAMS OF MINE. (It sort of works in a different version of the class.) There is an easy solution, but it's annoying and long.
  void updateTextField() {
    
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
  void submitText() {
    if (textFieldType == FieldType.CONSOLE) {
      commandChecker();
    }
    if (textFieldType == FieldType.BASIC) {
      handleInput();
    }
  }
  
  String getInput() {
    return inputText;
  }
  
  void handleInput() {
    receivedInput = inputText;
    submitted = true;
  }
  
  String receiveInput() {
    return receivedInput;
  }
  
  boolean isSubmitted() {
    return submitted;
  }
  
  //if a command made sense and was responded to, commandParsed is true - but if not, you get an error message
  
  boolean commandParsed = false;
  
  void commandChecker() {
    
    //No commands in this version of InteractiveTextField
    
    if (!commandParsed) {
      returnText = "Invalid input";
    }
    commandParsed = false;
    inputText = "";
  }
}