var Enemy = new Phaser.Class({

        Extends: Phaser.GameObjects.Image,
        pathNum: 0,
        direction: 1,
        initialize: function Enemy (scene)
        {
            Phaser.GameObjects.Image.call(this, scene, 0, 0, 'sprites', 'enemy');

            this.follower = { t: 0, vec: new Phaser.Math.Vector2() };
            this.hp = 0;
        },

        startOnPath: function ()
        {
            if (paths.length<=0) {
                return;
            }
            this.follower.t = 0;
            this.hp = 100;

            var availablePathNums = [];
            for (var i = 0; i < paths.length; i++) {
                if (paths[i].startPoint.x<0 || paths[i].startPoint.y<0) {
                    availablePathNums[availablePathNums.length] = i;
                }
            }

            this.pathNum = availablePathNums[getRandomInt(availablePathNums.length-1)];
            this.direction=1;
            paths[this.pathNum].getPoint(this.follower.t, this.follower.vec);

            this.setPosition(this.follower.vec.x, this.follower.vec.y);
            //$("#messages").text("An enemy is coming on "+paths[this.pathNum].name+"!!");
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

            var intersections = this.getIntersections(path, time, delta);
            if (intersections.length>0) {
                var names = "";
                for (var i = 0; i < intersections.length;i++){
                    names += paths[intersections[i]].name+"["+intersections[i]+"], ";
                }
                $("#messages").text(path.name+" intersects at "+names);
                //console.log(path.name+" intersects at "+names);

                /*console.log("Can move from "+path.name+" to:");
                for (var i = 0; i < intersections.length;i++){
                    console.log("  "+paths[i].name);
                }*/
            }

            this.follower.t += this.direction * ENEMY_SPEED * delta * path.speed;
            path.getPoint(this.follower.t, this.follower.vec);

            this.setPosition(this.follower.vec.x, this.follower.vec.y);

            if (this.follower.t >= 1)
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
                      console.log (this.pathNum+" enemy ended at "+Math.round(this.follower.vec.x)+"x"+Math.round(this.follower.vec.y));

                    this.direction *= -1;
                }
            }
        },
        getIntersections: function(path, time, delta) {
             var intersections = [];

             path.getPoint(this.follower.t, this.follower.vec);
             var oldPoint = {x: Math.round(this.follower.vec.x), y: Math.round(this.follower.vec.y)};

             this.follower.t += this.direction * ENEMY_SPEED * delta * path.speed;
             path.getPoint(this.follower.t, this.follower.vec);
             var newPoint = {x: Math.round(this.follower.vec.x), y: Math.round(this.follower.vec.y)};

             var messages = path.startPoint.x+","+path.startPoint.y+" through "+path.curves[path.curves.length-1].p1.x+","+path.curves[path.curves.length-1].p1.y+"<br>";
             messages += oldPoint.x+","+oldPoint.y+"  -  "+newPoint.x+","+newPoint.y+"<br>";

             for (var i = 0; i < path.intersections.length; i++) {
                var x = path.intersections[i].x;
                var y = path.intersections[i].y;
                messages += x+","+y+"<br>";
                if ((x>=oldPoint.x) && (y>=oldPoint.y) && (x<=newPoint.x) && (y<=newPoint.y)) {
                    intersections[intersections.length] = path.intersections[i].pathNum;
                } else if ((x>=newPoint.x) && (y>=newPoint.y) && (x<=oldPoint.x) && (y<=oldPoint.y)) {
                    intersections[intersections.length] = path.intersections[i].pathNum;
                }
             }

             //$("#messages").html(messages);

             return intersections;
        }

});
