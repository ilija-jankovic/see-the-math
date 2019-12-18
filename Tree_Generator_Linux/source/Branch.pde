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
