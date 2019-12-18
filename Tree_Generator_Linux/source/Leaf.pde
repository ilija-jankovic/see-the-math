class Leaf{
 float angle,size;
 float angleOffset = 0;
 float rotationOffset;
 float swingAngle;
 color colour;
 Branch branch;
 float proportion;
 
 public Leaf(Branch b, float proportion, float size, float angle, color colour){
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
