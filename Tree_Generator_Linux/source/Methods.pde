float endOfRotationX(float radius, float angle){
 return radius*cos(angle); 
}

float endOfRotationY(float radius, float angle){
 return radius*sin(angle); 
}

//gets x coord of a point along a line
float findXAlongLine(float x1, float x2, float proportion){
  if(proportion <= 0)return x1;
  if(proportion >= 1)return x2;
  return x1 + proportion*(x2-x1);
}

//gets y coord of a point along a line
float findYAlongLine(float y1, float y2, float proportion){
  if(proportion <= 0)return y1;
  if(proportion >= 1)return y2;
  return y1 + proportion*(y2-y1);
}

int getPixelX(PImage img, int index){
  return index%img.width;
}

int getPixelY(PImage img, int index){
  return index/img.width;
}

boolean pixelEmpty(PImage img, int index){
 return img.pixels[index] == color(0,0,0,0);
}
