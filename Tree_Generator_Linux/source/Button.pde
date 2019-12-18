class Button extends Textbox{
  private color fillColor;
  private color NORMAL_COLOUR = color(100);
  private color HOVER_COLOUR = color(80);
  private color UNSELECTABLE_COLOUR = color(50);
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
