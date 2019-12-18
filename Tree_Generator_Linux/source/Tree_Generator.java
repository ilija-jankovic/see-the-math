import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Tree_Generator extends PApplet {

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

public void setup() {
  
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

public Textbox createEditableText(float x, float y, float w, float h, String staticText, String input, int maxLength){
  Textbox textbox = new Textbox(x,y,w,h,staticText,true);
  textbox.editableText = input;
  textbox.maxLength = maxLength;
  return textbox;
}

public Textbox createEditableNumbersText(float x, float y, float w, float h, String staticText, String input, int maxLength, boolean decimal){
  Textbox textbox = createEditableText(x,y,w,h,staticText,input,maxLength);
  textbox.numbersOnly = true;
  textbox.decimal = decimal;
  return textbox;
}

//creates grass background
public PImage generateGrass(int w, int h) {
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
public void draw() {
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

public void checkButtonsSelectable(){
  if(!validNumbers() || generating)
    generate.selectable = false;
  else
    generate.selectable = true;
    
  if(saved || tree == null || generating)
    save.selectable = false;
  else
    save.selectable = true;
}

public void updateText(){
  FRAMES = Integer.parseInt(frames.getInput());
  if(FRAMES <= 0){
    FRAMES= 1;
    frames.editableText = "1";
  }
  
  iterations.text = getMaxIterations();
  loadingTime.text = getLoadingProgress();
}

public void updateItems(){
  for (int i = 0; i < items.size(); i++) {
    Item item = items.get(i);
    if (item != null) {
      image(item.getSprite(), item.displayX(), item.displayY());
      item.update();
    }
  }
}

//checks which button selected
public void checkFocus(){
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

public void checkUserInput(){
  if (!keyPressed && !mousePressed)
    inputDown = false;
  else
    inputDown = true;
}

//approximates how many iterations the tree algorithm will go through
public int calculateMaxIterations(){
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
public boolean validNumbers(){
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
public String getMaxIterations(){
  int maxIterations = calculateMaxIterations();
  if(maxIterations != -1)
    return iterations.text = "Max Iterations: "+maxIterations;
  return iterations.text = "Max Iterations: Error";
}

//gets loading progress as string
public String getLoadingProgress(){
 if(loadingProgress >= 1)
   return "DONE";
 return String.format("%.2f", loadingProgress*100) + "%";
}

//makes new thread to generate tree
public void createTree(){
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
public void saveTree(File path) {
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
class Branch{
 Tree tree;
 float size,angle,layer;
 float angleOffset = 0;
 float rotationOffset = random(0,1);
 float swingAngle;
 
 Branch lowerBranch;
 float proportion;
 
 public Branch(Tree tree, float proportion, float size, float angle, float layer, float swingAngle) {
  this.tree = tree;
  this.proportion = proportion;
  this.size = size;
  this.angle = angle;
  this.layer = layer;
  this.swingAngle = swingAngle/size;
 }
 
 public float finalAngle(){
   return angle+angleOffset;
 }
 
 //recursively goes through parent branches to find x coord
 public float x(){
  if(lowerBranch == null)
    return tree.w/2;
  return findXAlongLine(lowerBranch.x(),lowerBranch.endX(),proportion);
 }
 
 //recursively goes through parent branches to find y coord
 public float y(){
  if(lowerBranch == null)
    return tree.h;
  return findYAlongLine(lowerBranch.y(),lowerBranch.endY(),proportion);
 }
 
 public float endX(){
  return x()+endOfRotationX(size,finalAngle()); 
 }
 
 public float endY(){
  return y()+endOfRotationY(size,finalAngle());
 }
}
class Button extends Textbox{
  private int fillColor;
  private int NORMAL_COLOUR = color(100);
  private int HOVER_COLOUR = color(80);
  private int UNSELECTABLE_COLOUR = color(50);
  public Button(float x, float y, float w, float h, String text){
    super(x,y,w,h,text,false);
    selectable = true;
  }
  
  public PImage getSprite(){
    PGraphics display = createEmptyGraphics();
    
    display.beginDraw();
    display.fill(fillColor);
    display.rect(0,0,w-2,h-2);
    display.endDraw();
    
    display = writeText(display);
     
    return display;
  }
  
  public void update(){
    super.update();
    if(selectable){
      if(overlapping(mouseX, mouseY))
        fillColor = HOVER_COLOUR;
      else
        fillColor = NORMAL_COLOUR;
    }
    else
      fillColor = UNSELECTABLE_COLOUR;
  }
}
class Item{
 float x,y;
 public Item(float x, float y){
  this.x = x;
  this.y = y;
 }
 
 public void update(){}
 
 public PImage getSprite(){ return null; }
 
 public float displayX(){ return x; }
 
 public float displayY(){ return y; }
}
class Leaf{
 float angle,size;
 float angleOffset = 0;
 float rotationOffset;
 float swingAngle;
 int colour;
 Branch branch;
 float proportion;
 
 public Leaf(Branch b, float proportion, float size, float angle, int colour){
  this.branch = b;
  this.proportion = proportion;
  this.size = size;
  this.angle = angle;
  this.colour = colour;
  rotationOffset = noise(x()/10,y()/10);
  swingAngle = PI*random(0,1)/4;
 }
 
 public float x(){
   return findXAlongLine(branch.x(),branch.endX(),proportion);
 }
 
 public float y(){
   return findYAlongLine(branch.y(),branch.endY(),proportion);
 }
 
 public float endX(){
  return x()+endOfRotationX(size,angle+angleOffset); 
 }
 
 public float endY(){
  return y()+endOfRotationY(size,angle+angleOffset);
 }
}
public float endOfRotationX(float radius, float angle){
 return radius*cos(angle); 
}

public float endOfRotationY(float radius, float angle){
 return radius*sin(angle); 
}

//gets x coord of a point along a line
public float findXAlongLine(float x1, float x2, float proportion){
  if(proportion <= 0)return x1;
  if(proportion >= 1)return x2;
  return x1 + proportion*(x2-x1);
}

//gets y coord of a point along a line
public float findYAlongLine(float y1, float y2, float proportion){
  if(proportion <= 0)return y1;
  if(proportion >= 1)return y2;
  return y1 + proportion*(y2-y1);
}

public int getPixelX(PImage img, int index){
  return index%img.width;
}

public int getPixelY(PImage img, int index){
  return index/img.width;
}

public boolean pixelEmpty(PImage img, int index){
 return img.pixels[index] == color(0,0,0,0);
}
class Textbox extends Item {
  protected boolean selectable;

  String text;
  boolean editable;
  
  boolean numbersOnly;
  boolean decimal = true;
  
  String editableText = "";
  int maxLength;
  
  private String prevText;
  
  int EDIT_SELECTED = color(100);
  int EDIT_UNSELECTED = color(255);

  float w, h, fontSize;

  public Textbox(float x, float y, float w, float h, String text, boolean editable) {
    super(x, y);
    this.text = text;
    selectable = true;
    this.editable = editable;
    maxLength = 20;
    fontSize = 20;
    this.w = w;
    this.h = h;
  }

  public boolean overlapping(float mX, float mY) {
    return mX >= displayX() && mX <= displayX()+w && mY >= displayY() && mY <= displayY()+h;
  }
  
  public String getInput(){
    if(editableText.equals(""))
      return prevText;
    return editableText;
  }

  //updates selection behaviour
  public void update() {
    if (selectable) {
      if(FOCUS == this){
        if(!editable)
          FOCUS = null;
        else{
          typeText();
          if (!overlapping(mouseX, mouseY) && mousePressed)
            FOCUS = null;
        }
      }
      else if (overlapping(mouseX, mouseY) && mousePressed && !inputDown){
        FOCUS = this;
        prevText = editableText;
        editableText = "";
      }
    }
    if(editable && FOCUS != this && editableText.equals(""))
      editableText = prevText;
  }
  
  //allows numbers to be typed and deleted
  private void typeText(){
    if(!inputDown && keyPressed)
      if(key == BACKSPACE){
        if(editableText.length() > 0)
          editableText = editableText.substring(0,editableText.length()-1);
      }
      else if(editableText.length() < maxLength)
        if((numbersOnly && (key >= 48 && key <= 57 || (decimal && key == 46))) || !numbersOnly)
          editableText+=key;
  }

  public float displayX() {
    return x-w/2;
  }

  public float displayY() {
    return y-h/2;
  }

  public PImage getSprite() {
    return writeText(createEmptyGraphics());
  }

  protected PGraphics createEmptyGraphics() {
    return createGraphics((int)w, (int)h);
  }

  //returns text as pimage
  protected PGraphics writeText(PGraphics display) {
    display.beginDraw();

    display.textAlign(CENTER);
    display.textSize(fontSize);
    
    if(!editable || FOCUS != this)
      display.fill(EDIT_UNSELECTED);
    else
      display.fill(EDIT_SELECTED);
    display.text(text+editableText, w/2, h/2+5);

    display.endDraw();

    return display;
  }
}
class Tree extends Item{
 int w,h;
 
 int MIN_BRANCHES = 2;
 private int MAX_BRANCHES;
 
 private int LEAF_LAYERS;
 int MIN_LEAVES_PER_BRANCH = 0;
 private int MAX_LEAVES_PER_BRANCH;

 private int detail;
 private float spread, randomness, size, branchSwing;

 private PImage[] animation = new PImage[FRAMES];
 private float frame = 0;
 float animSpeed = 0.5f;
 
 float[] xOffsets = new float[FRAMES];
 
 public Tree(float x, float y, int w, int h, int detail, float spread, float randomness, float size, int maxBranches, int leafLayers, int maxLeaves, float branchSwing){
   super(x,y);
   this.w = w;
   this.h = h;
   this.detail = detail;
   this.spread = spread;
   this.randomness = randomness;
   this.size = size;
   this.branchSwing = branchSwing;
   MAX_BRANCHES = maxBranches;
   LEAF_LAYERS = leafLayers;
   MAX_LEAVES_PER_BRANCH = maxLeaves;
   generateTree();
 }

//main method
private void generateTree(){
  setLoadingProgress(0);
  ArrayList<Branch> branches = generateBranches();
  ArrayList<Leaf> leaves = generateLeaves(branches);

  for(int i = 0; i < animation.length; i++){
    PGraphics tree = createGraphics(w,h);
    swayBranches(branches,i);
    drawBranches(tree, branches);
    drawLeaves(tree,leaves,i);
    
    if(shadow.text == "Shadows: On")
      drawShadow(tree);

    animation[i] = cropToSize(tree,i);
    
    setLoadingProgress(i);
  }
}

private void setLoadingProgress(int frame){
  loadingProgress = (float)frame/(animation.length-1);
}

public void update(){
  updateFrame();
  if(validNumbers())
    animSpeed = Float.parseFloat(framerate.getInput());
  else
    animSpeed = 0.5f;
}

private void updateFrame(){
  frame+=animSpeed;
}

private int getFrame(){
 return (int)(frame)%animation.length;
}

public PImage getSprite(){
  return animation[getFrame()];
}

//makes shadow and distorts it, puts it on an empty background, then puts tree on top
private void drawShadow(PGraphics tree){
  PGraphics shadow = createGraphics(tree.width,tree.height);
  
  shadow.beginDraw();
  shadow.image(tree,0,0);
  shadow.endDraw();
    
  shadow.loadPixels();
  for(int i = 0; i < shadow.pixels.length; i++){
    int c = shadow.pixels[i];
    float alpha = alpha(c);
    if(alpha > 0)
      shadow.pixels[i] = color(0,0,0,min(alpha,150));
  }
  shadow.updatePixels();
  
  PImage shadowImg = shadow.get(0,0,shadow.width,shadow.height);
  shadowImg.resize(shadow.width,shadow.height/2);
  
  
  PGraphics background = createGraphics(tree.width,tree.height);
  background.beginDraw();
  background.image(shadowImg,0,tree.height/2);
  background.image(tree,0,0);
  background.endDraw();
  
  tree.clear();
  tree.beginDraw();
  tree.image(background,0,0);
  tree.endDraw();
}

//grows tree out of base branch layer by layer
private ArrayList<Branch> generateBranches(){
  Branch base = new Branch(this,0,2*size+random(-size/4,size/4),3*PI/2+random(-0.1f,0.1f),0,random(branchSwing*0.5f,branchSwing*1.5f));
  
  ArrayList<Branch> branches = new ArrayList<Branch>();
  branches.add(base);
  
  ArrayList<Branch> topLayer = new ArrayList<Branch>();
  topLayer.add(base);
  
  for(int i = 1; i <= detail; i++){
   ArrayList<Branch> newLayer = new ArrayList<Branch>();
   for(int j = 0; j < topLayer.size(); j++){
     Branch lowerBranch = topLayer.get(j);
     
     int newBranches = (int)random(MIN_BRANCHES,MAX_BRANCHES+1);
     for(int k = 0; k < newBranches; k++){
       float angle = lowerBranch.angle + (random(-randomness-spread,randomness+spread))*pow(1.2f,i);
       float proportion = random(0.5f,1);
       if(angle > PI/2 && angle < 5*PI/2){
       fill(255/i,255/(2*i),0);
       
       if(k==0&&lowerBranch.layer==0){
         proportion = 1;
       }
       
       Branch newBranch = new Branch(tree,proportion,lowerBranch.size/(1.1f+random(0,randomness)),angle,i,random(branchSwing*0.5f,branchSwing*1.5f));
       newBranch.lowerBranch = lowerBranch;
       branches.add(newBranch);
       newLayer.add(newBranch);
       }
     }
   }
   topLayer = newLayer;
  }
  
  return branches;
}

//sways branches (except base branch) so they can loop smoothly
private void swayBranches(ArrayList<Branch> branches, int frame){
  for(int i = 0; i < branches.size(); i++){
    Branch b = branches.get(i);
    if(b.layer > 0){
      float angleOffset = map(b.rotationOffset,0,1,0,2*PI);
      float angle = map(frame,0,animation.length,0,2*PI);
      float rotation = sin(angle+angleOffset);
    
      b.angleOffset = map(rotation,-1,1,-b.swingAngle,b.swingAngle);
    }
  }
}

private void drawBranches(PGraphics tree, ArrayList<Branch> branches){
  tree.beginDraw();
  for(int i = 0; i < branches.size(); i++){
   Branch b = branches.get(i);
   tree.strokeWeight(pow(50,1/(b.layer+2)));
   tree.line(b.x(),b.y(),b.endX(),b.endY());
  }
  colourBranches(tree);
  tree.endDraw();
}

//draws bark texture with a noisemap
private void colourBranches(PGraphics tree){
  tree.loadPixels();
  
  float h = map(tree.height,0,tree.height,50,150);
  float fadeHeight = 5;
  
  for(int i = 0; i < tree.pixels.length; i++){
   if(!pixelEmpty(tree,i)){
     float x = getPixelX(tree,i);
     float y = getPixelY(tree,i);
     float alpha = alpha(tree.pixels[i]);

     if(y >= tree.height-fadeHeight){
      alpha = map(y,tree.height-fadeHeight,tree.height,255,0);
     }
     
     float noiseMap = map(noise(x/5,y/5),0,1,0.5f,1);
     tree.pixels[i] = color(h*noiseMap,h*noiseMap/2,0,alpha);
   }
  }
  tree.updatePixels();
}

private ArrayList<Leaf> generateLeaves(ArrayList<Branch> branches){
  ArrayList<Leaf> leaves = new ArrayList<Leaf>();
  for(int i = 0; i < branches.size(); i++){
       Branch b = branches.get(i);
       if(b.layer > detail-LEAF_LAYERS){
        int c = color(0,random(150,255),0);
        
        for(int j = 1; j < (int)random(MIN_LEAVES_PER_BRANCH,MAX_LEAVES_PER_BRANCH+1); j++){
          float radius = random(2,5);
          float proportion = random(0.5f,1);
          if(j == 1)
            proportion = 1;
          Leaf l = new Leaf(b,proportion,radius,0,c);
          leaves.add(l);
          l.angle = b.angle+map(noise(l.x()/20,l.y()/20),0,1,-PI,PI);
        }
       }
  }
  return leaves;
}

private void drawLeaves(PGraphics tree, ArrayList<Leaf> leaves, int frame){
  PGraphics leavesImage = createGraphics(w,h);

  leavesImage.beginDraw();
  leavesImage.noStroke();
  
  for(int i = 0; i < leaves.size(); i++){
    Leaf l = leaves.get(i);
    
    float angleOffset = map(l.rotationOffset,0,1,0,2*PI);
    float angle = map(frame,0,animation.length,0,2*PI);
    float rotation = sin(angle+angleOffset);
    
    l.angleOffset = map(rotation,-1,1,-l.swingAngle,l.swingAngle);
    leavesImage.stroke(l.colour);
    leavesImage.line(l.x(),l.y(),l.endX(),l.endY());
  }
  
  leavesImage.loadPixels();
    
  int[] croppedCoords = croppedCoords(leavesImage);
  if(croppedCoords != null){
  for(int i = 0; i < leavesImage.pixels.length; i++){
    float shade = map(i/leavesImage.width,croppedCoords[1],croppedCoords[3],-50,100);
    leavesImage.pixels[i] = color(red(leavesImage.pixels[i])-shade,green(leavesImage.pixels[i])-shade,blue(leavesImage.pixels[i])-shade,alpha(leavesImage.pixels[i]));
  }
  }
  
  leavesImage.updatePixels();
  
  leavesImage.endDraw();
  
  generateLeavesShadowOnTree(tree,leavesImage);
  placeLeavesOnTree(tree,leavesImage);
}

private void placeLeavesOnTree(PGraphics tree, PGraphics leavesImage){
  tree.beginDraw();
  tree.image(leavesImage,0,0);
  tree.endDraw();
}

//puts shade on branches specifically
private void generateLeavesShadowOnTree(PGraphics tree, PGraphics leaves){
  int yOffset = 20;
  int pixelOffset = yOffset*leaves.width;
  tree.loadPixels();
  leaves.loadPixels();
  for(int i = pixelOffset; i < tree.pixels.length; i++){
    if(!pixelEmpty(tree,i) && !pixelEmpty(leaves, i-pixelOffset)){
      int c = tree.pixels[i];
      tree.pixels[i] = color(red(c)*0.5f,green(c)*0.5f,blue(c)*0.5f);
    }
  }
  tree.updatePixels();
  leaves.updatePixels();
}

//removes empty pixels
private PImage cropToSize(PGraphics img, int frame){
  int[] coords = croppedCoords(img);
  
  if(coords != null){
  PImage croppedImg = img.get(coords[0],coords[1],abs(coords[2]-coords[0]),abs(coords[3]-coords[1]));
  stroke(255);
  xOffsets[frame] = 0.5f*(coords[0]+coords[2])-img.width/2;
  return croppedImg;
  }
  return createImage(0,0,ARGB);
}

//gets first and last pixel
private int[] croppedCoords(PGraphics img){
  int xStart = img.width;
  int yStart = img.height;
  int xEnd = 0;
  int yEnd = 0;
  boolean firstPixel = false;

  for(int i = 0; i < img.pixels.length; i++){
   int x = getPixelX(img,i);
   int y = getPixelY(img,i);
   if(!pixelEmpty(img,i)){
   if(!firstPixel){
    xStart = x;
    xEnd = x;
    yStart = y;
    yEnd = y;
    firstPixel = true;
   }
   if(x < xStart)
    xStart = x;
   if(x > xEnd)
    xEnd = x;
   if(y < yStart)
    yStart = y;
   if(y > yEnd)
    yEnd = y;
   }
  }
  
  if(firstPixel){
    return new int[] {xStart,yStart,xEnd,yEnd};
  }
  return null;
}

//centers x on base branch
public float displayX(){
  return x + xOffsets[getFrame()]-getSprite().width/2; 
}

public float displayY(){
  return y-getSprite().height;
}
}
  public void settings() {  size(800, 900); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Tree_Generator" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
