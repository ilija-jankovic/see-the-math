Tree tree = null;

int TREE_X, TREE_Y;
int TREE_FRAME_SIZE = 300;

Button generate, save;

Textbox detail, spread, variation, size, maxBranches, maxLeaves, leafLayers, wind, framerate, frames;
Textbox loadingTime, iterations, shadow;

float TEXTBOX_GAP_FROM_BUTTONS = 100;
float TEXTBOX_GAP = 20;
float TEXTBOX_HEIGHT = 20;
float TEXTBOX_WIDTH = 300;
float TEXTBOX_WIDTH_OFFSET = 20;

float BUTTON_GAP_FROM_TREE = 20;
float BUTTON_WIDTH = 200;
float BUTTON_HEIGHT = 50;

ArrayList<Item> items = new ArrayList<Item>();
PImage grass;

Textbox FOCUS = null;

int FRAMES = 30;

float loadingProgress = 1;

void setup() {
  size(800, 900);
  frameRate(60);
  
  grass = generateGrass(width,height);
  
  TREE_X = width/2;
  TREE_Y = TREE_FRAME_SIZE;
  items.add(tree);
  
  //initalises buttons and textboxes
  
  generate = new Button(TREE_X, TREE_Y+BUTTON_GAP_FROM_TREE+BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, "Generate");
  save = new Button(generate.x, generate.y + generate.h, BUTTON_WIDTH, BUTTON_HEIGHT, "Save");
  
  items.add(generate);
  items.add(save);
  
  spread = createEditableNumbersText(width-TEXTBOX_WIDTH/2-TEXTBOX_WIDTH_OFFSET,save.y+TEXTBOX_HEIGHT+TEXTBOX_GAP_FROM_BUTTONS,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Spread: ","0.20",4,true);
  variation = createEditableNumbersText(width-TEXTBOX_WIDTH/2-TEXTBOX_WIDTH_OFFSET,spread.y+TEXTBOX_HEIGHT+TEXTBOX_GAP,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Variation: ","0.30",4,true);
  size = createEditableNumbersText(width-TEXTBOX_WIDTH/2-TEXTBOX_WIDTH_OFFSET,variation.y+TEXTBOX_HEIGHT+TEXTBOX_GAP,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Size: ","20",2,false);
  wind = createEditableNumbersText(width-TEXTBOX_WIDTH/2-TEXTBOX_WIDTH_OFFSET,size.y+TEXTBOX_HEIGHT+TEXTBOX_GAP,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Wind: ","0.80",4,true);
  shadow = new Textbox(width-TEXTBOX_WIDTH/2-TEXTBOX_WIDTH_OFFSET,wind.y+TEXTBOX_HEIGHT+TEXTBOX_GAP,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Shadows: On",false);
  detail = createEditableNumbersText(TEXTBOX_WIDTH/2+TEXTBOX_WIDTH_OFFSET,save.y+TEXTBOX_HEIGHT+TEXTBOX_GAP_FROM_BUTTONS,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Branch Layers: ","5",1,false);
  maxBranches = createEditableNumbersText(TEXTBOX_WIDTH/2+TEXTBOX_WIDTH_OFFSET,detail.y+TEXTBOX_HEIGHT+TEXTBOX_GAP,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Max Branches: ","5",2,false);
  leafLayers = createEditableNumbersText(TEXTBOX_WIDTH/2+TEXTBOX_WIDTH_OFFSET,maxBranches.y+TEXTBOX_HEIGHT+TEXTBOX_GAP,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Leaf Layers: ","3",1,false);
  maxLeaves = createEditableNumbersText(TEXTBOX_WIDTH/2+TEXTBOX_WIDTH_OFFSET,leafLayers.y+TEXTBOX_HEIGHT+TEXTBOX_GAP,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Max Leaves per Branch: ","50",3,false);
  framerate = createEditableNumbersText(TEXTBOX_WIDTH/2+TEXTBOX_WIDTH_OFFSET,maxLeaves.y+TEXTBOX_HEIGHT+TEXTBOX_GAP*3,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Animation Speed: ","0.5",3,true);
  frames = createEditableNumbersText(TEXTBOX_WIDTH/2+TEXTBOX_WIDTH_OFFSET,framerate.y+TEXTBOX_HEIGHT+TEXTBOX_GAP,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,"Frames: ","30",2,false);
  
  items.add(detail);
  items.add(spread);
  items.add(variation);
  items.add(size);
  items.add(wind);
  items.add(shadow);
  items.add(maxBranches);
  items.add(leafLayers);
  items.add(maxLeaves);
  items.add(framerate);
  items.add(frames);
  
  iterations = new Textbox(save.x,frames.y+TEXTBOX_HEIGHT+TEXTBOX_GAP,400,TEXTBOX_HEIGHT,"Max Iterations: "+calculateMaxIterations(),false);
  loadingTime = new Textbox(width/2,TEXTBOX_HEIGHT,TEXTBOX_WIDTH,TEXTBOX_HEIGHT,getLoadingProgress(),false);
  
  items.add(iterations);
  items.add(loadingTime);
}

Textbox createEditableText(float x, float y, float w, float h, String staticText, String input, int maxLength){
  Textbox textbox = new Textbox(x,y,w,h,staticText,true);
  textbox.editableText = input;
  textbox.maxLength = maxLength;
  return textbox;
}

Textbox createEditableNumbersText(float x, float y, float w, float h, String staticText, String input, int maxLength, boolean decimal){
  Textbox textbox = createEditableText(x,y,w,h,staticText,input,maxLength);
  textbox.numbersOnly = true;
  textbox.decimal = decimal;
  return textbox;
}

//creates grass background
PImage generateGrass(int w, int h) {
  PGraphics grass = createGraphics(w, h);
  grass.beginDraw();
  grass.background(0, 50, 0);
  for (int i = 0; i < width*height/10; i++) {
    grass.stroke(0, random(50, 100), 0, 100);
    float x = random(0, grass.width);
    float y = random(0, grass.height);
    float size = random(2, 6);
    float angle = 3*PI/2 + map(noise(x/20, y/20), 0, 1, 0, 2);
    grass.line(x, y, x+endOfRotationX(size, angle), y+endOfRotationY(size, angle));
  }
  grass.endDraw();
  return (PImage)grass;
}

boolean inputDown = false;
boolean saved = false;
boolean generating = false;

//main looping method of program
void draw() {
  fill(150);
  noStroke();

  image(grass,0,0);

  if(loadingProgress >= 1)
    generating = false;

  updateItems();
  checkFocus();
  checkButtonsSelectable();
  updateText();

  checkUserInput();
}

void checkButtonsSelectable(){
  if(!validNumbers() || generating)
    generate.selectable = false;
  else
    generate.selectable = true;
    
  if(saved || tree == null || generating)
    save.selectable = false;
  else
    save.selectable = true;
}

void updateText(){
  FRAMES = Integer.parseInt(frames.getInput());
  if(FRAMES <= 0){
    FRAMES= 1;
    frames.editableText = "1";
  }
  
  iterations.text = getMaxIterations();
  loadingTime.text = getLoadingProgress();
}

void updateItems(){
  for (int i = 0; i < items.size(); i++) {
    Item item = items.get(i);
    if (item != null) {
      image(item.getSprite(), item.displayX(), item.displayY());
      item.update();
    }
  }
}

//checks which button selected
void checkFocus(){
  if (FOCUS == save && tree != null && !saved)
    selectFolder("Select a folder to process:", "saveTree");

  if (FOCUS == generate && !generating){
    generating = true;
    FOCUS = null;
    thread("createTree");
  }
  
  if(FOCUS == shadow)
    if(shadow.text == "Shadows: On")
      shadow.text = "Shadows: Off";
    else
      shadow.text = "Shadows: On";
}

void checkUserInput(){
  if (!keyPressed && !mousePressed)
    inputDown = false;
  else
    inputDown = true;
}

//approximates how many iterations the tree algorithm will go through
int calculateMaxIterations(){
  if(validNumbers()){
    int branches = 0;
    int leaves = 0;
    int detail = Integer.parseInt(this.detail.getInput());
    int maxBranches = Integer.parseInt(this.maxBranches.getInput());
    int maxLeaves = Integer.parseInt(this.maxLeaves.getInput());
    int leafLayers = Integer.parseInt(this.leafLayers.getInput());
    
    for(int i = 0; i < detail+1; i++){
      branches+=pow(maxBranches,i);
    }
    for(int i = max(0,detail-leafLayers); i < detail+1; i++){
      leaves+=pow(maxBranches,i)*maxLeaves;
    }
    return (branches+leaves)*FRAMES;
  }
  return -1;
}

//checks if user input is valid
boolean validNumbers(){
  for(int i = 0; i < items.size(); i++){
    if(items.get(i) instanceof Textbox){
      Textbox text = (Textbox)items.get(i);
      if(text.numbersOnly){
        try {
          if(!text.decimal)
            Integer.parseInt(text.getInput());
          else
            Float.parseFloat(text.getInput());
        } 
        catch (NumberFormatException e) {
        return false;
      }
      }
    }
  }
  return true;
}

//gets iterations as string
String getMaxIterations(){
  int maxIterations = calculateMaxIterations();
  if(maxIterations != -1)
    return iterations.text = "Max Iterations: "+maxIterations;
  return iterations.text = "Max Iterations: Error";
}

//gets loading progress as string
String getLoadingProgress(){
 if(loadingProgress >= 1)
   return "DONE";
 return String.format("%.2f", loadingProgress*100) + "%";
}

//makes new thread to generate tree
void createTree(){
  items.remove(tree);
  tree = new Tree(TREE_X, TREE_Y, TREE_FRAME_SIZE, TREE_FRAME_SIZE, 
         Integer.parseInt(detail.getInput()), 
         Float.parseFloat(spread.getInput()), 
         Float.parseFloat(variation.getInput()), 
         Integer.parseInt(size.getInput()),
         Integer.parseInt(maxBranches.getInput()),
         Integer.parseInt(leafLayers.getInput()),
         Integer.parseInt(maxLeaves.getInput()),
         Float.parseFloat(wind.getInput()));
  items.add(tree);
  saved = false;
}

//saves tree to system
void saveTree(File path) {
  if(path != null){
  File[] files = listFiles(path);
  String[] xOffsets = new String[tree.animation.length];
  for (int i = 0; i < tree.animation.length; i++) {
    PImage frame = tree.animation[i];
    frame.save(path+"//tree-"+files.length+"\\"+i+".png");
    xOffsets[i] = Float.toString(tree.xOffsets[i]);
  }
  saveStrings(path+"//tree-"+files.length+"//xOffsets.txt", xOffsets);
  saved = true;
  }
}
