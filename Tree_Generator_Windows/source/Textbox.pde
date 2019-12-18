class Textbox extends Item {
  protected boolean selectable;

  String text;
  boolean editable;
  
  boolean numbersOnly;
  boolean decimal = true;
  
  String editableText = "";
  int maxLength;
  
  private String prevText;
  
  color EDIT_SELECTED = color(100);
  color EDIT_UNSELECTED = color(255);

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
