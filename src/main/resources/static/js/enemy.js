var Enemy = new Phaser.Class({

      Extends: Phaser.GameObjects.Image,
      pathNum: 0,
      direction: 1,
      hp: 0,
      initialize: function Enemy (scene)
      {
          Phaser.GameObjects.Image.call(this, scene, 0, 0, 'sprites', 'enemy');

          this.follower = { t: 0, vec: new Phaser.Math.Vector2() };
          this.hp = 100;
      },
      startOnPath: function ()
      {
          if (nests.getChildren().length<=0) {
              return;
          }
          this.follower.t = 0;

          var nestNum = getRandomInt(nests.getChildren().length);

          var x = 0;
          var y = 0;

          for (var i = 0; i < paths.length; i++) {
            if (paths[i].curves) {
                for (var j = 0; j < paths[i].curves.length; j++) {
                  var nestChildren = nests.getChildren();
                  if (paths[i].curves[j].p0.x ==  nestChildren[nestNum].x && paths[i].curves[j].p0.y ==  nestChildren[nestNum].y) {
                      this.pathNum = i;
                      x = paths[i].curves[j].p0.x;
                      y = paths[i].curves[j].p0.y;

                      this.follower.t = getTFromPointOnPath(x, y, paths[i]);
                      break;
                  }
                }
            }
          }

          paths[this.pathNum].getPoint(this.follower.t, this.follower.vec);

          this.setPosition(x, y);
          $("#messages").text("An enemy is coming on "+paths[this.pathNum].name+"!!");
      },
      receiveDamage: function(damage) {
          this.hp -= damage;

          // if hp drops below 0 we deactivate this enemy
          if(this.hp <= 0) {
              this.setActive(false);
              this.setVisible(false);

              addScore(10);
          }
      },
      update: function (time, delta)
      {
          var path = paths[this.pathNum];

          var startingPoint = path.getPoint(this.follower.t, this.follower.vec);
          this.follower.t += this.direction * ENEMY_SPEED * delta * path.speed;
          var endingPoint = path.getPoint(this.follower.t, this.follower.vec);

          if (startingPoint && endingPoint) {
              for (var c = 0 ; c < path.curves.length; c++) {
                var curveX = Math.round(path.curves[c].p0.x);
                var curveY = Math.round(path.curves[c].p0.y);

                var startingX = Math.round(startingPoint.x);
                var startingY = Math.round(startingPoint.y);
                var endingX = Math.round(endingPoint.x);
                var endingY = Math.round(endingPoint.y);

                if (((curveX >= startingX) && (curveX <= endingX) &&
                    (curveY >= startingY) && (curveY <= endingY)) ||
                    ((curveX <= startingX) && (curveX >= endingX) &&
                    (curveY <= startingY) && (curveY >= endingY))) {

                    var intersections = [];
                    intersections[0] = {
                        x: curveX,
                        y: curveY,
                        pathNum: this.pathNum
                    };
                    for (var i = 0; i < path.intersections.length; i++) {
                        if ((path.intersections[i].x == curveX) &&
                            (path.intersections[i].y == curveY)) {
                            var intersectionPathNum = path.intersections[i].pathNum;
                            var intersectionPath = paths[intersectionPathNum];
                            //console.log("Crossing intersection on "+path.name+" and "+intersectionPath.name);
                            intersections[intersections.length] = {
                                x: path.intersections[i].x,
                                y: path.intersections[i].y,
                                pathNum: intersectionPathNum
                            }
                        }
                    }

                    if (intersections.length>1) {
                        var intersectionNum = getRandomInt(intersections.length);
                        if (intersections[intersectionNum].pathNum != this.pathNum) {
                            this.pathNum = intersections[intersectionNum].pathNum;
                            // figure out how far along the path we are
                            this.follower.t = getTFromPointOnPath(intersections[intersectionNum].x,
                                                                  intersections[intersectionNum].y,
                                                                  paths[this.pathNum]);

                            console.log(intersections.length+" intersections new t="+this.follower.t+" on "+paths[this.pathNum].name+" with mph="+paths[this.pathNum].speed);
                        }
                    }
                }
              }
          }

          this.setPosition(this.follower.vec.x, this.follower.vec.y);

          if ((this.follower.t >= 1) || (this.follower.t < 0))
          {
              var foundNewPath = false;
              for (var i = 0 ; i < paths.length; i++) {
                  //console.log("Comparing existing="+paths[i].curves[0].p0.x+"x"+paths[i].curves[0].p0.y+" vs path["+i+"]="+paths[i].curves[0].p0.x+"x"+paths[i].curves[0].p0.y);
                  if ((paths[i].curves[0].p0.x == Math.round(this.follower.vec.x)) &&
                      (paths[i].curves[0].p0.y == Math.round(this.follower.vec.y))) {
                      //console.log(this.pathNum+" found new path["+i+"]="+paths[i].curves[0].p0.x+"x"+paths[i].curves[0].p0.y+" with speed "+paths[i].speed);
                      this.pathNum = i;
                      foundNewPath = true;
                      this.follower.t = 0;
                      continue;
                  }
              }

              if (!foundNewPath) {
                  //console.log (this.pathNum+" enemy ended at "+Math.round(this.follower.vec.x)+"x"+Math.round(this.follower.vec.y));

                  this.direction *= -1;
              }
          }
      },

});

function getTFromPointOnPath(pointX, pointY, path) {
    var startX = path.curves[0].p0.x;
    var startY = path.curves[0].p0.y;

    var pathNumCurves = path.curves.length - 1;
    var endX = path.curves[pathNumCurves].p1.x;
    var endY = path.curves[pathNumCurves].p1.y;

    var lenX = Math.abs(startX - endX);
    var lenY = Math.abs(startY - endY);

    if (lenX > lenY) {
        var distX = Math.abs(pointX - startX);
        if (distX == 0) {
            return 0;
        } else {
            return distX/lenX;
        }
    } else {
        var distY = Math.abs(pointY - startY);
        if (distY == 0) {
            return 0;
        } else {
            return distY /lenY ;
        }
    }

}

var LargeEnemy = new Phaser.Class({
    Extends: Enemy,
    initialize: function Enemy (scene)
          {
              Phaser.GameObjects.Image.call(this, scene, 0, 0, 'sprites', 'turret');

              this.follower = { t: 0, vec: new Phaser.Math.Vector2() };
              this.hp = 1000;
              console.log("Large Enemy")
          }
    });

LargeEnemy.prototype = Object.create(Enemy.prototype);
LargeEnemy.prototype.constructor = LargeEnemy;
