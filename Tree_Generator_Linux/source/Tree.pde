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
 float animSpeed = 0.5;
 
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
    animSpeed = 0.5;
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
    color c = shadow.pixels[i];
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
  Branch base = new Branch(this,0,2*size+random(-size/4,size/4),3*PI/2+random(-0.1,0.1),0,random(branchSwing*0.5,branchSwing*1.5));
  
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
       float angle = lowerBranch.angle + (random(-randomness-spread,randomness+spread))*pow(1.2,i);
       float proportion = random(0.5,1);
       if(angle > PI/2 && angle < 5*PI/2){
       fill(255/i,255/(2*i),0);
       
       if(k==0&&lowerBranch.layer==0){
         proportion = 1;
       }
       
       Branch newBranch = new Branch(tree,proportion,lowerBranch.size/(1.1+random(0,randomness)),angle,i,random(branchSwing*0.5,branchSwing*1.5));
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
     
     float noiseMap = map(noise(x/5,y/5),0,1,0.5,1);
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
        color c = color(0,random(150,255),0);
        
        for(int j = 1; j < (int)random(MIN_LEAVES_PER_BRANCH,MAX_LEAVES_PER_BRANCH+1); j++){
          float radius = random(2,5);
          float proportion = random(0.5,1);
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
      color c = tree.pixels[i];
      tree.pixels[i] = color(red(c)*0.5,green(c)*0.5,blue(c)*0.5);
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
  xOffsets[frame] = 0.5*(coords[0]+coords[2])-img.width/2;
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
float displayX(){
  return x + xOffsets[getFrame()]-getSprite().width/2; 
}

float displayY(){
  return y-getSprite().height;
}
}
