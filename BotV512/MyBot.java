// This Java API uses camelCase instead of the snake_case as documented in the API doc
//     Otherwise the names of methods are consistent.

import hlt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.lang.Math;
import java.time.*; 
import java.util.Collections;

//Null Pointer on Drop Offs
//Equations still funny
public class MyBot {
 
    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }
        final Random rng = new Random(rngSeed);

        Game game = new Game();
        
        Map ship_status = new HashMap();
        Map ship_has_moved = new HashMap ();
        boolean saving_for_dropoff = false;
        int saving_ship = -1;
        Position goal = new Position(0,0);
        double max_halite = 0;
        
        
        game.ready("Mep");

        Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        for (;;) {
            game.updateFrame();
            long start = System.nanoTime();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;
            final ArrayList<Command> commandQueue = new ArrayList<>();
			boolean dropoff_made = false;
			int current_halite = 0;
			int[][] smallest_distance = new int[gameMap.height][gameMap.width];
			int[][] closest_dropoff = new int[gameMap.height][gameMap.width];
			int [][] halite_on_space = new int[gameMap.height][gameMap.width];
			double smallestDistance = 0;
			int nearest_dropoff = -1;
			boolean[][] is_taken = new boolean[gameMap.height][gameMap.width];
			boolean[][] inspired = new boolean[gameMap.height][gameMap.width];
			boolean[][] clumped = new boolean[gameMap.height][gameMap.width];
			boolean[][] returning_ship = new boolean[gameMap.height][gameMap.width];
			HashMap <Integer, Integer> ship_location = new HashMap();
			Map id_to_ship = new HashMap();
			Map still_ship = new HashMap();
			Map ship_moving = new HashMap();
			Map final_location = new HashMap();
			Map ship_has_goal = new HashMap();
			Map ship_swapping = new HashMap();
			double num_returning = 0;
			int[][] friendly = new int[gameMap.height][gameMap.width];
			int[][] enemy = new int[gameMap.height][gameMap.width];
			int removed =  game.turnNumber;
			        int cheaper_dropoff = gameMap.at(goal).halite;
		for (final Ship ship : me.ships.values()){
			if(ship.id.id == saving_ship){
				cheaper_dropoff +=  ship.halite;
			}
		}
		if(cheaper_dropoff > 4000) cheaper_dropoff = 4000;
		// && me.halite > 4000-cheaper_dropoff
			if(saving_for_dropoff){
				me.dropoffs.put(new EntityId(1000), new Dropoff(me.id, new EntityId(1000), goal));
			}

			//if(game.players.size() == 4) removed /=2;
			for(int i = 0; i < gameMap.width; i++){
				for(int j = 0; j < gameMap.width; j++){
					smallestDistance = gameMap.calculateDistance(new Position(i,j), me.shipyard.position);
					nearest_dropoff = -1;
					for (final Dropoff dropoff : me.dropoffs.values()){
						if (smallestDistance > gameMap.calculateDistance(new Position(i,j), dropoff.position)){
							smallestDistance = gameMap.calculateDistance(new Position(i,j), dropoff.position);
							nearest_dropoff = dropoff.id.id;
							//Log.log("Changed nearest dropoff to " + nearest_dropoff + " from position" + i + "," + j);
						}
					}
					int enemy_ships = 0;
					int owner_ships = 0;
						for(int x = 0; x < 3;x++){
							for(int y = 0; y + x <= 3;y++){
							Position temp = new Position(i+x, j+y);
								if(gameMap.at(temp).isOccupied()){
									if(!(gameMap.at(temp).ship.owner.equals(me.shipyard.owner))){
									enemy_ships ++;
									} else {
									owner_ships++;
									}
								}
								if(!(x==0)){
									temp = new Position(i-x, j+y);
									if(gameMap.at(temp).isOccupied()){
										if(!(gameMap.at(temp).ship.owner.equals(me.shipyard.owner))){
											enemy_ships ++;
										} else {
											owner_ships++;
										}
									}
								}
								if(!(y==0)){
									temp = new Position(i+x, j-y);
									if(gameMap.at(temp).isOccupied()){
										if(!(gameMap.at(temp).ship.owner.equals(me.shipyard.owner))){
											enemy_ships ++;
										} else {
											owner_ships++;
										}
									}
								}
								if(!(x==0) && !(y==0)){
									temp = new Position(i-x, j-y);
									if(gameMap.at(temp).isOccupied()){
										if(!(gameMap.at(temp).ship.owner.equals(me.shipyard.owner))){
											enemy_ships ++;
										} else {
											owner_ships++;
										}
									}
								}
							}
						}
						if(enemy_ships > 1){
						inspired[i][j] = true;
						} else{
						inspired[i][j] = false;
						}
						if(owner_ships>4){
						clumped[i][j] = true;
						} else{
						clumped[i][j] = false;
						}
						friendly[i][j] = owner_ships;
						enemy[i][j] = enemy_ships;
						Position temp = new Position(i,j);
						if(game.players.size() == 2 && owner_ships-1>enemy_ships && gameMap.at(temp).isOccupied() && !(gameMap.at(temp).ship.owner.equals(me.shipyard.owner))){
							halite_on_space[i][j] += gameMap.at(temp).ship.halite;
							gameMap.at(temp).ship = null;
						}



					smallest_distance[i][j] = (int) smallestDistance;
					closest_dropoff[i][j] = nearest_dropoff;
					//if (smallestDistance == 0){
					//	halite_on_space[i][j] = 0;
					//} else{
					int local_halite = gameMap.at(new Position(i,j)).halite;
					//if(inspired[i][j] && game.players.size()==2) local_halite *= 3; 
					halite_on_space[i][j] += local_halite;
					if(i != 0) halite_on_space[i-1][j] += local_halite/4;
					if(j != 0) halite_on_space[i][j-1] += local_halite/4;
					if(i != gameMap.width -1) halite_on_space[i+1][j] += local_halite/4;
					if(j != gameMap.width -1) halite_on_space[i][j+1] += local_halite/4;
					if(local_halite==0) halite_on_space[i][j] = 0;
					//}
					current_halite += local_halite;
					

				}
			}
			double mine_when_over = 0.7 * current_halite/(gameMap.width * gameMap.height);
			if (game.players.size() == 4){
				mine_when_over = 0.65 * current_halite/(gameMap.width * gameMap.height);
			}


			long end = System.nanoTime();
			long curr = end - start;
			//Log.log("Starting Code " + curr);
			
			
			if (me.ships.values().size() == 0){
				max_halite = current_halite;
			}
			

			if (game.players.size() == 4 && Constants.MAX_TURNS * 0.95 > game.turnNumber){
				for (Player player : game.players){
					//Log.log("Something is working");
					if(player.equals(me)){
					//	Log.log("removed me");
						continue;
					}
					for (Ship ship : player.ships.values()){
						int amount_to_remove = 2;
						if(gameMap.height == 48) amount_to_remove = 0;
						if(gameMap.height == 40) amount_to_remove = -1;
						if(gameMap.height == 32) amount_to_remove = -1;
						if(friendly[ship.position.x][ship.position.y]-amount_to_remove >= enemy[ship.position.x][ship.position.y]){
							if(Constants.MAX_TURNS * 0.75 < game.turnNumber && friendly[ship.position.x][ship.position.y]-1 >= enemy[ship.position.x][ship.position.y] && gameMap.height >= 45){
								//halite_on_space[ship.position.x][ship.position.y] += ship.halite;
								//gameMap.at(ship.position).ship = null;
							}
						continue;
						}
						if(gameMap.at(ship.position).halite >= mine_when_over && (Constants.MAX_TURNS * 0.5 > game.turnNumber || gameMap.height > 45)) continue;
					    gameMap.at(ship.position.directionalOffset((Direction.NORTH))).markUnsafe(ship);
						gameMap.at(ship.position.directionalOffset((Direction.SOUTH))).markUnsafe(ship);
						gameMap.at(ship.position.directionalOffset((Direction.EAST))).markUnsafe(ship);
		    			gameMap.at(ship.position.directionalOffset((Direction.WEST))).markUnsafe(ship);
					}

				}

				
			}
			for (final Dropoff dropoff : me.dropoffs.values()){
				gameMap.at(dropoff.position).ship = null;
			}
   			gameMap.at(me.shipyard.position).ship = null;
    		gameMap.at(me.shipyard.position.directionalOffset((Direction.NORTH))).ship = null;
			gameMap.at(me.shipyard.position.directionalOffset((Direction.SOUTH))).ship = null;
			gameMap.at(me.shipyard.position.directionalOffset((Direction.EAST))).ship = null;
		    gameMap.at(me.shipyard.position.directionalOffset((Direction.WEST))).ship = null;
		  
		    for (final Ship ship : me.ships.values()) {
		    	gameMap.at(ship.position).ship = null;
		    	ship_has_moved.put(ship.id.id,false);
		    	ship_swapping.put(ship.id.id,false);
		    	id_to_ship.put(ship.id.id, ship);
		    if (!ship_status.containsKey(ship.id.id)){
				ship_status.put(ship.id.id, "exploring");
			}
			if (ship.halite > 900 - removed){
				ship_status.put(ship.id.id,"returning");
			}
			boolean matched = false;
			if(ship_status.get(ship.id.id).equals("returning")){
				for (final Dropoff dropoff : me.dropoffs.values()){
					if(ship.position.equals(dropoff.position)){
					matched = true;
					}
				}
				//Log.log("My position is: " + ship.position.x +"," + ship.position.y + " The shipyard is at:" + me.shipyard.position.x + "," + me.shipyard.position.y);
				if (matched || ship.position.equals(me.shipyard.position)){
					//Log.log("Set Back to Exploring");
					ship_status.put(ship.id.id, "exploring");
				} else {
					returning_ship[ship.position.x][ship.position.y] = true;
					num_returning ++;
				}

	    	}
    	}

	    	int needed_for_dropoff = 19;
			if (game.players.size() == 2){
				needed_for_dropoff = 16;
			}
			if (gameMap.height == 32 && game.players.size() == 4){
				//needed_for_dropoff = 200;
			}
			//Log.log("This percent returning home" + num_returning/(me.ships.values().size()));
			boolean want_drop_off = (num_returning/(me.ships.values().size()) > 0.3) && (me.ships.values().size() > (me.dropoffs.values().size()+1)*13);
//(me.ships.values().size() > (me.dropoffs.values().size()+1)*needed_for_dropoff)
			if (want_drop_off && !saving_for_dropoff){
			//	end = System.nanoTime();
        		//curr = end-start;
        		//Log.log("Started DropOff: " + curr);
				int current_smallest = 10000;
				int best_nearby_amount=0;
				Position best_location = new Position(0,0);
				//end = System.nanoTime();
        		//curr = end-start;
        		//Log.log("Starting first cycle: " + curr);
				for (int i =0; i < gameMap.height; i++){
					//end = System.nanoTime();
        			//curr = end-start;
        			//Log.log("Started loop 1: " + curr);
					for (int j =0; j < gameMap.height; j++){
						if(i == 0 && j==0) continue;
						//end = System.nanoTime();
        				//curr = end-start;
        				//Log.log("Started loop 2: " + curr);
        				int nearby_amount = 0;
        				int enemy_ships = 0;
						int owner_ships = 0;
						for(int x = 0; x < 5;x++){
							for(int y = 0; y + x <= 5;y++){
								Position temp = new Position(i+x, j+y);
								nearby_amount += 1.25 * gameMap.at(temp).halite;
								if(gameMap.at(temp).isOccupied()){
									if(!(gameMap.at(temp).ship.owner.equals(me.shipyard.owner))){
									enemy_ships ++;
									} else {
									owner_ships++;
									}
								}
								if(!(x==0)){
									temp = new Position(i-x, j+y);
								if(gameMap.at(temp).isOccupied()){
									if(!(gameMap.at(temp).ship.owner.equals(me.shipyard.owner))){
									enemy_ships ++;
									} else {
									owner_ships++;
									}
								}
									nearby_amount +=gameMap.at(temp).halite;
								}
								if(!(y==0)){
									temp = new Position(i+x, j-y);
									if(gameMap.at(temp).isOccupied()){
										if(!(gameMap.at(temp).ship.owner.equals(me.shipyard.owner))){
											enemy_ships ++;
										} else {
											owner_ships++;
										}
								}	
									nearby_amount +=gameMap.at(temp).halite;
								}
								if(!(x==0) && !(y==0)){
									temp = new Position(i-x, j-y);
									if(gameMap.at(temp).isOccupied()){
										if(!(gameMap.at(temp).ship.owner.equals(me.shipyard.owner))){
											enemy_ships ++;
										} else {
											owner_ships++;
										}
									}
									nearby_amount +=gameMap.at(temp).halite;
								}
							}
						}
						Position new_temp = new Position(i, j);
						//end = System.nanoTime();
        				//curr = end-start;
        				//Log.log("Finished a space in searching for a dropoff at time: " + curr);
        				int closest_distance = 1000;		
        				for (final Ship ship : me.ships.values()){
       						int current_distance = gameMap.calculateDistance(ship.position, new_temp);
       						if (current_distance < closest_distance){
   								closest_distance = current_distance;
   							}
   						}
 			boolean close_enough = (closest_distance <= gameMap.width/4);
			//Log.log("This space is close enough " + closest_distance);
//        	if (owner_ships >= enemy_ships && nearby_amount > 4000 && (smallest_distance[i][j] > gameMap.width/5) && nearby_amount-Math.pow(smallest_distance[i][j],2) > best_nearby_amount-Math.pow(current_smallest,2) && (smallest_distance[i][j] < gameMap.width/2)){
        	if (owner_ships >= enemy_ships && nearby_amount > 4000 && (smallest_distance[i][j] > gameMap.width/4) && nearby_amount-Math.pow(smallest_distance[i][j],2) > best_nearby_amount-Math.pow(current_smallest,2) && (close_enough)){
        			//Log.log("This happened");
        			current_smallest = smallest_distance[i][j];
        			best_location = new Position(i,j);
        			best_nearby_amount = nearby_amount;
    				}
    			}
			}
			int position = best_location.x*100 + best_location.y;
			//Log.log("Best position is " + position);
			if(!(best_location.equals(new Position(0,0)))){
	        	goal = best_location;
	        	saving_for_dropoff = true;
	        	Ship closest_ship = null;
	        	int closest_distance = 1000;
	       		for (final Ship ship : me.ships.values()){
	       			int current_distance = gameMap.calculateDistance(ship.position, goal);
	       			if (current_distance < closest_distance){
	   					closest_ship = ship;
	   					closest_distance = current_distance;
	   				}
	   			}
   				saving_ship = closest_ship.id.id;
   			} else {
   			//Log.log("No good spot");
   			}
        	//end = System.nanoTime();
        	//curr = end-start;
        	//Log.log("Time for drop off is" + curr);
        }

        if (saving_for_dropoff){
        	for (final Ship ship : me.ships.values()){
        		//Log.log("Checking for dropoff");
        		//Log.log(ship.id.id + "---" + saving_ship);
        		if (ship.id.id == saving_ship){
        			//Log.log(ship.position.x+ "," + ship.position.y + "---" + goal.x + "," + goal.y);
    			}
    			if(gameMap.at(goal).halite == 0){
					saving_for_dropoff = false;
					saving_ship = -1;
				}

        		if(ship.id.id == saving_ship && me.halite > (4000-cheaper_dropoff) && ship.position.equals(goal)){
        		 	commandQueue.add(ship.makeDropoff());
        			ship_has_moved.put(ship.id.id, true);
        			saving_ship = -1;
        			me.halite -= (4000-cheaper_dropoff);
        			saving_for_dropoff = false;
        			break;
				}


        		
    		}

    	}
    	   // end = System.nanoTime();
        	//curr = end-start;
        	//Log.log("Time entering ship moves is" + curr);

    	for (final Ship ship : me.ships.values()){
    		
    		if (smallest_distance[ship.position.x][ship.position.y] + 5 >= Constants.MAX_TURNS - game.turnNumber){
    			if(ship.halite < gameMap.at(ship.position).halite/10){
    				commandQueue.add(ship.stayStill());
    				gameMap.at(ship.position).markUnsafe(ship);
    				ship_has_moved.put(ship.id.id, true);
    				continue;
				}

    			gameMap.at(me.shipyard.position).ship = null;
    			Direction move = Direction.STILL;
    			for (final Dropoff dropoff : me.dropoffs.values()){
    				gameMap.at(dropoff.position).ship = null;
    				if(ship.position.equals(dropoff.position)){
						ship_has_moved.put(ship.id.id, true);
						commandQueue.add(ship.move(move));
						continue;
					}
				}
				
				if (ship.position.equals(me.shipyard.position)){
					ship_has_moved.put(ship.id.id, true);
					commandQueue.add(ship.move(move));
					continue;	
				}
				if ((boolean)ship_has_moved.get(ship.id.id)){
					continue;
				}

				if (closest_dropoff[ship.position.x][ship.position.y] == -1){
					move = new_naive_navigate(ship, me.shipyard.position, gameMap, true);
				}else {
					for (final Dropoff dropoff : me.dropoffs.values()){
						if (dropoff.id.id == closest_dropoff[ship.position.x][ship.position.y]){
							move = new_naive_navigate(ship, dropoff.position, gameMap, true);
							ship_has_moved.put(ship.id.id, true);
							break;
						}

					}
				}
				commandQueue.add(ship.move(move));    	
				ship_has_moved.put(ship.id.id, true);
				continue;
			}
			
			if(!(boolean)ship_has_moved.get(ship.id.id) && !(boolean)gameMap.at(ship.position).isOccupied()){
				if(ship.halite < gameMap.at(ship.position).halite/10){
				commandQueue.add(ship.stayStill());
				gameMap.at(ship.position).markUnsafe(ship);
				//Log.log("My ID is " + ship.id.id + " I don't have the halite to move");
				ship_has_moved.put(ship.id.id, true);
				ship_location.put(ship.id.id, (ship.position.x *100 + ship.position.y));
				ship_moving.put(ship.id.id, Direction.STILL);
				continue;
				}
				
				if(gameMap.at(ship.position).halite > mine_when_over && (ship.halite + gameMap.at(ship.position).halite/4 > 900 - removed) && ((ship.halite + gameMap.at(ship.position).halite/4) < 1000)){
				commandQueue.add(ship.stayStill());
				gameMap.at(ship.position).markUnsafe(ship);
				//Log.log("My ID is " + ship.id.id + " I want to top off");
				ship_has_moved.put(ship.id.id, true);
				ship_location.put(ship.id.id, (ship.position.x *100 + ship.position.y));
				ship_moving.put(ship.id.id, Direction.STILL);
				continue;				
				}
				boolean similar_values = true;
				/*
				int curr_hal = halite_on_space[ship.position.x][ship.position.y];
				if(inspired[ship.position.x][ship.position.y] && game.players.size() == 4) curr_hal *= 3;
				int north = halite_on_space[gameMap.normalize(ship.position.directionalOffset(Direction.NORTH)).x][gameMap.normalize(ship.position.directionalOffset(Direction.NORTH)).y];
				int south = halite_on_space[gameMap.normalize(ship.position.directionalOffset(Direction.SOUTH)).x][gameMap.normalize(ship.position.directionalOffset(Direction.SOUTH)).y];
				int east = halite_on_space[gameMap.normalize(ship.position.directionalOffset(Direction.EAST)).x][gameMap.normalize(ship.position.directionalOffset(Direction.EAST)).y];
				int west = halite_on_space[gameMap.normalize(ship.position.directionalOffset(Direction.WEST)).x][gameMap.normalize(ship.position.directionalOffset(Direction.WEST)).y];
				*/
				int curr_hal = gameMap.at(ship.position).halite;
				if(inspired[ship.position.x][ship.position.y] && game.players.size() == 4) curr_hal *= 3;
				int north = gameMap.at(ship.position.directionalOffset(Direction.NORTH)).halite;
				int south = gameMap.at(ship.position.directionalOffset(Direction.SOUTH)).halite;
				int east = gameMap.at(ship.position.directionalOffset(Direction.EAST)).halite;
				int west = gameMap.at(ship.position.directionalOffset(Direction.WEST)).halite;
				
				if(curr_hal < 0.5 * north || curr_hal < 0.5 * south || curr_hal < 0.5 * east || curr_hal < 0.5 * west) similar_values = false;
				if((gameMap.at(ship.position).halite) > mine_when_over && ship.halite < 900 - removed && similar_values){
				//Log.log("My ID is " + ship.id.id + " There is a good amount of halite here");
				commandQueue.add(ship.stayStill());
				gameMap.at(ship.position).markUnsafe(ship);
				ship_has_moved.put(ship.id.id, true);
				ship_location.put(ship.id.id, (ship.position.x *100 + ship.position.y));
				ship_moving.put(ship.id.id, Direction.STILL);
				continue;				
				}
			}
		}
        if (!(saving_ship == -1) && (Constants.MAX_TURNS - game.turnNumber > 50)){
			boolean still_around = false;
			for (final Ship ship : me.ships.values()){
				if (saving_ship == ship.id.id) still_around = true;
				//Log.log("Saving Ship: " + saving_ship + " Current Ship: " + ship.id.id);
				if(saving_ship == ship.id.id && !(smallest_distance[ship.position.x][ship.position.y] + 15 >= Constants.MAX_TURNS - game.turnNumber)){
						if(ship.position.equals(goal)){
							//continue;
						}

						if(!(boolean)ship_has_moved.get(ship.id.id)){
							//Log.log("Moving towards dropoff location");
							Direction move = new_naive_navigate(ship, goal, gameMap, true);
							commandQueue.add(ship.move(move));
							gameMap.at(ship.position.directionalOffset(move)).markUnsafe(ship);
							ship_has_moved.put(ship.id.id, true);
							still_around = true;
							ship_location.put(ship.id.id, (ship.position.directionalOffset(move).x *100 + ship.position.directionalOffset(move).y));
							ship_moving.put(ship.id.id,move);
							continue;
						}
				}	
			}
			if (!still_around){
				//Log.log("Ship dead");
		    	for (final Ship ship : me.ships.values()){
		    		saving_for_dropoff = false;
		    		break;
	    			
       				/*saving_ship = ship.id.id;
       				Direction move = new_naive_navigate(ship, goal, gameMap);
					commandQueue.add(ship.move(move));
					ship_location.put(ship.id.id, (ship.position.directionalOffset(move).x *100 + ship.position.directionalOffset(move).y));
					ship_moving.put(ship.id.id,move);
					gameMap.at(ship.position.directionalOffset(move)).markUnsafe(ship);
					ship_has_moved.put(ship.id.id, true);
					still_around = true;
					ship_location.put(ship.id.id, (ship.position.directionalOffset(move).x *100 + ship.position.directionalOffset(move).y));
					ship_moving.put(ship.id.id,move);
       				break;*/
   			}
   		}
	}



		for (final Ship ship : me.ships.values()){
			/*boolean matched = false;
			if((boolean)ship_has_moved.get(ship.id.id)){
				continue;
			}
			if (!ship_status.containsKey(ship.id.id)){
				ship_status.put(ship.id.id, "exploring");
			}
			if (ship.halite > 900 - game.turnNumber){
				ship_status.put(ship.id.id,"returning");
			}
			if(ship_status.get(ship.id.id).equals("returning")){
				for (final Dropoff dropoff : me.dropoffs.values()){
					if(ship.position.equals(dropoff.position)){
					matched = true;
					}
				}
				//Log.log("My position is: " + ship.position.x +"," + ship.position.y + " The shipyard is at:" + me.shipyard.position.x + "," + me.shipyard.position.y);
				if (matched || ship.position.equals(me.shipyard.position)){
					//Log.log("Set Back to Exploring");
					ship_status.put(ship.id.id, "exploring");
				} else{
					Direction move = Direction.STILL;
					if (closest_dropoff[ship.position.x][ship.position.y] == -1){
						move = new_naive_navigate(ship, me.shipyard.position, gameMap, false);
					}	else  {
						for (final Dropoff dropoff : me.dropoffs.values()){
							if (dropoff.id.id == closest_dropoff[ship.position.x][ship.position.y]){
								move = new_naive_navigate(ship, dropoff.position, gameMap, false);
								break;
							}

						}
						//Log.log("My ID is " + ship.id.id + " I am returning with move " + move);
					}
					
					commandQueue.add(ship.move(move));
					ship_location.put(ship.id.id, (ship.position.directionalOffset(move).x *100 + ship.position.directionalOffset(move).y));
					ship_has_moved.put(ship.id.id, true);
					ship_moving.put(ship.id.id,move);
					//still_ship.put(ship.id.id, move==Direction.Still);
					continue;
					
				}

			}*/
			
			if(ship_status.get(ship.id.id).equals("exploring")){
				//double current_value = -10000000;
				//current_best = 0;
				//double time_taken = 0;
    			//double base_exchange = Math.log(0.75);
				/*Position best_location = new Position(0,0);
				for(int i = 0; i < gameMap.width; i++){
					for(int j = 0; j < gameMap.width; j++){
						double local_halite = gameMap.at(new Position(i,j)).halite;
						if (local_halite < mine_when_over){
						continue;
						}

						time_taken =  Math.log(mine_when_over)/base_exchange - Math.log(local_halite)/base_exchange;
						if((boolean)inspired[i][j]){
							time_taken /= 3;
							//Log.log("Inspired Works");
						}
						current_value = local_halite/(gameMap.calculateDistance(ship.position, new Position(i,j)) + time_taken + smallest_distance[i][j]);
						//current_value = Math.pow(local_halite,3) / Math.pow((Math.pow(gameMap.calculateDistance(ship.position, new Position(i,j)),2) + Math.pow(smallest_distance[i][j],1.36)),2.5);
						//if(me.dropoffs.values().size()==0){
						//	current_value = local_halite / Math.pow(1.6,(gameMap.calculateDistance(ship.position, new Position(i,j))+  0*smallest_distance[i][j]));
						//} else{
						//current_value = Math.pow(local_halite,1.15) /(smallest_distance[i][j]*0.5 + Math.pow(1.2,gameMap.calculateDistance(ship.position, new Position(i,j))));						
						//current_value = local_halite / Math.pow(1.35,(0.9*gameMap.calculateDistance(ship.position, new Position(i,j))+ 0.45 * smallest_distance[i][j]));
						//}
						//if (gameMap.height == 64){
						//current_value = Math.pow(local_halite,1.2) /(smallest_distance[i][j] + Math.pow(1.2,gameMap.calculateDistance(ship.position, new Position(i,j))));						
						//}

						//current_value = (local_halite  - 450 * gameMap.calculateDistance(ship.position, new Position(i,j))) - 300*smallest_distance[i][j];
						//current_value = halite_on_space[i][j] + local_halite / gameMap.calculateDistance(ship.position, new Position(i,j));
						//Log.log("The current value for position(" + i +"," +j +") is" + current_value);
						

						if((boolean)clumped[i][j]||is_taken[i][j]){
							//current_value *= 0.2;
						}
						if(gameMap.at(new Position(i,j)).isOccupied()){
							current_value = -1;
						}
						if(current_value > current_best){
							current_best = current_value;
							best_location = new Position(i,j);
							
						}
					}
				}*/
				//Log.log("My ID is " + ship.id.id + " The position I should move to is " + best_location.x + "," + best_location.y + ". I evaluate it at " + current_best);
				ship_has_goal.put(ship.id.id, (int)best_location(ship, halite_on_space, inspired, mine_when_over, smallest_distance, gameMap, clumped, game));
				//int working_location = best_location.x*100 + best_location.y;
				//int test_location = (int)best_location(ship, halite_on_space, inspired, mine_when_over, smallest_distance, gameMap, clumped);
				//Log.log(working_location + " the good move and the bad move " + test_location);
				//Log.log("Halite on the good space is " + gameMap.at(best_location).halite + " or " + halite_on_space[best_location.x][best_location.y]);
				//Log.log("Halite on the bad space is " gameMap.at(new Position(test_location/100,test_location%100)).halite + " or " + halite_on_space[test_location/100][test_location%100]
				//is_taken[best_location.x][best_location.y] = true;
				/*Direction move = new_naive_navigate(ship, best_location, gameMap);
				ship_location.put(ship.id.id, (ship.position.directionalOffset(move).x *100 + ship.position.directionalOffset(move).y));
				ship_moving.put(ship.id.id,move);
				commandQueue.add(ship.move(move));    	
				ship_has_moved.put(ship.id.id, true);*/
				//still_ship.put(ship.id.id, move==Direction.Still);
				
				continue;
			}		
		}
		end = System.nanoTime();
        curr = end-start;
        //Log.log("Time after inputting ship moves is" + curr);
		int needed_amount = 1000;
		if (saving_for_dropoff){
			needed_amount = 5000;
			needed_amount -= cheaper_dropoff;
		}
		boolean same_location = true;
		boolean might_break = false;
		while (same_location){
			same_location = false;
			might_break = false;
			for (final Ship ship : me.ships.values()){
				if(!ship_has_goal.containsKey(ship.id.id)){
					//Log.log("This ship has no goal " + ship.id.id);
					continue;
				}
				//Log.log("This ship has a goal " + ship.id.id);
				
				for (final Ship check_ship : me.ships.values()){
					if(!ship_has_goal.containsKey(check_ship.id.id)) {
						//Log.log("This ship has a null goal " + check_ship.id.id);
						continue;
					}
				//	Log.log(ship.id.id + "s goal is + " + ship_has_goal.get(ship.id.id) + " and " + check_ship.id.id + "s goal is " + ship_has_goal.get(check_ship.id.id));
					if(!(ship.id.id == check_ship.id.id) && (int)ship_has_goal.get(ship.id.id) == (int)ship_has_goal.get(check_ship.id.id)){
						//Log.log("Two different ships have the same goal " + ship.id.id + " and " + check_ship.id.id + " and it is at " + (int)ship_has_goal.get(ship.id.id)/100 + " , " + (int)ship_has_goal.get(ship.id.id)%100);
						Position temp = new Position((int)ship_has_goal.get(ship.id.id)/100, (int)ship_has_goal.get(ship.id.id)%100);
						halite_on_space[(int)ship_has_goal.get(ship.id.id)/100][(int)ship_has_goal.get(ship.id.id)%100] = -1;
						if (gameMap.calculateDistance(ship.position, temp) <= gameMap.calculateDistance(check_ship.position, temp)){
int best_location = best_location(check_ship, halite_on_space, inspired, mine_when_over, smallest_distance, gameMap, clumped, game);
					               ship_has_goal.put(check_ship.id.id, best_location);
					               //Log.log("I changed the second ship to location " + ship_has_goal.get(check_ship.id.id));
					               might_break = true;
					               same_location = true;
					               break;
						} else{
int best_location = best_location(ship, halite_on_space, inspired, mine_when_over, smallest_distance, gameMap, clumped, game);						
					       ship_has_goal.put(ship.id.id, best_location);
					       //Log.log("I changed the first ship to location " + ship_has_goal.get(ship.id.id));
					       might_break = true;
					       same_location = true;
					       break;
					       }

					}

			             }
			              if(might_break){
			              //Log.log("Broken");
			               break;
		               }


			}
		}
	for (final Ship ship : me.ships.values()){
            if(!ship_has_goal.containsKey(ship.id.id) || (boolean)ship_has_moved.get(ship.id.id)){
                continue;
            }
            //Log.log(ship.id.id + " is moving to " + (int)ship_has_goal.get(ship.id.id));
            Position temp = new Position((int)ship_has_goal.get(ship.id.id)/100, (int)ship_has_goal.get(ship.id.id)%100); 
            Direction move = new_naive_navigate(ship, temp, gameMap, true);
            temp = gameMap.normalize(ship.position.directionalOffset(move));
            if((returning_ship[temp.x][temp.y] == true)){
            	for (final Ship random : me.ships.values()){
        			if(random.position.equals(temp)){
        				if((boolean)ship_has_moved.get(random.id.id)) break;
        				Log.log("Swapped");
        				Direction temp_move = move.invertDirection();
	  	 				ship_location.put(random.id.id, (gameMap.normalize(random.position.directionalOffset(temp_move)).x *100 + gameMap.normalize(random.position.directionalOffset(temp_move)).y));
	   					ship_moving.put(random.id.id,temp_move);
	  			 		commandQueue.add(random.move(temp_move));
	  			 		gameMap.at(ship.position).markUnsafe(ship);
	   					ship_has_moved.put(random.id.id, true);  
	   					ship_swapping.put(ship.id.id,true);
	   					ship_swapping.put(random.id.id,true);
    					break;
    				}
        		}
        	}
            //Log.log(ship.id.id + "is making the move " + move);
	  	 	ship_location.put(ship.id.id, (gameMap.normalize(ship.position.directionalOffset(move)).x *100 + gameMap.normalize(ship.position.directionalOffset(move)).y));
	   		ship_moving.put(ship.id.id,move);
	   		commandQueue.add(ship.move(move));    	
	   		ship_has_moved.put(ship.id.id, true);
        }
		if ((game.players.size() == 2) && (gameMap.height ==48)){
			for (Player player : game.players){
				if(player.equals(me)){
					continue;
				}
				for (Ship ship : player.ships.values()){
					boolean skip = false;
					for (final Dropoff dropoff : me.dropoffs.values()){
						if(ship.position.x *100 + ship.position.y == dropoff.position.x * 100 + dropoff.position.y){
						 skip = true;
						 break;
						}
					}
					if(skip){
						continue;
					}
					gameMap.at(ship.position.directionalOffset((Direction.NORTH))).markUnsafe(ship);
					gameMap.at(ship.position.directionalOffset((Direction.SOUTH))).markUnsafe(ship);
					gameMap.at(ship.position.directionalOffset((Direction.EAST))).markUnsafe(ship);
		    		gameMap.at(ship.position.directionalOffset((Direction.WEST))).markUnsafe(ship);
				}
			}
		}

          for (final Ship ship : me.ships.values()){
			if((boolean)ship_has_moved.get(ship.id.id)){
				continue;
			}
			if(ship_status.get((int)ship.id.id).equals("returning")){
					Direction move = Direction.STILL;
					if (closest_dropoff[ship.position.x][ship.position.y] == -1){
						move = new_naive_navigate(ship, me.shipyard.position, gameMap, false);
					}	else  {
						for (final Dropoff dropoff : me.dropoffs.values()){
							if (dropoff.id.id == closest_dropoff[ship.position.x][ship.position.y]){
								move = new_naive_navigate(ship, dropoff.position, gameMap, false);
								break;
							}

						}
						//Log.log("My ID is " + ship.id.id + " I am returning with move " + move);
					}
					commandQueue.add(ship.move(move));
					ship_location.put(ship.id.id, (gameMap.normalize(ship.position.directionalOffset(move)).x *100 + gameMap.normalize(ship.position.directionalOffset(move)).y));
					ship_has_moved.put(ship.id.id, true);
					ship_moving.put(ship.id.id,move);
					//still_ship.put(ship.id.id, move==Direction.Still);
					continue;
				}
			}







					
		boolean collisions = true;
		boolean broken = false;
		while (collisions){
		//Log.log("This code ran");
			collisions = false;
			broken = false;
			for (final Ship ship : me.ships.values()){
				if(ship_location.get(ship.id.id) == null) continue;
				for (final Ship check_ship : me.ships.values()){
					if(ship_location.get(check_ship.id.id) == null){
						continue;
					}

					//Log.log("start id " + ship.id.id + " second id " + check_ship.id.id);
					//Log.log("Looking at position " + entry +" and " + checkLoop + "with ships" + i + "and" + j);
					if(!(ship.id.id == check_ship.id.id) && (int)ship_location.get(ship.id.id) == (int)ship_location.get(check_ship.id.id)){
						Log.log("There will be a crash at position " + ship_location.get(ship.id.id) + " or " + ship_location.get(check_ship.id.id) + " with ships " + ship.id.id + " and " + check_ship.id.id);
						Log.log("ship" + ship.id.id + " is moving " + ship_moving.get(ship.id.id) + " the other ship is " + check_ship.id.id +  "and is moving"  + ship_moving.get(check_ship.id.id));
						if(!(boolean)ship_swapping.get(ship.id.id)){
							Command want_to_remove = ship.move((Direction)ship_moving.get(ship.id.id));
							commandQueue.add(ship.move(Direction.STILL));
							ship_moving.put(ship.id.id,Direction.STILL);
							ship_location.put(ship.id.id, (ship.position.x *100 + ship.position.y));
							commandQueue.remove(want_to_remove);
							gameMap.at(ship.position).markUnsafe(ship);
						} else {
						Log.log("ship" + ship.id.id + " is swapping");					
						}

						if(!(boolean)ship_swapping.get(check_ship.id.id)){
							Command want_to_remove = check_ship.move((Direction)ship_moving.get(check_ship.id.id));
							commandQueue.remove(want_to_remove);
							commandQueue.add(check_ship.move(Direction.STILL));
							ship_moving.put(check_ship.id.id,Direction.STILL);
							ship_location.put(check_ship.id.id, (check_ship.position.x *100 + check_ship.position.y));
							gameMap.at(check_ship.position).markUnsafe(check_ship);
						}else {
						Log.log("ship" + check_ship.id.id + " is swapping");					
						}
						collisions = true;
						Log.log("Crash averted at  " + ship_location.get(ship.id.id) + " or " + ship_location.get(check_ship.id.id) + " with ships " + ship.id.id + " and " + check_ship.id.id);
						Log.log("ship" + ship.id.id + " is moving " + ship_moving.get(ship.id.id) + " the other ship is " + check_ship.id.id + " and is moving " + ship_moving.get(check_ship.id.id));
						broken = true;
					}
					if(broken) {
					Log.log("broken");
					break;
					}

				}
				if(broken) break;

			}
		}

		
		//Constants.MAX_TURNS* 0.7
		double avg_halite = current_halite/(gameMap.width * gameMap.height);
		double percent_halite = current_halite / max_halite;

		if (game.players.size() == 2){
			boolean want_to_build = false;
			int enemy_ships_num = 0;
			for (Player player : game.players){
				if(player.equals(me)){
					continue;
				}
				enemy_ships_num = player.ships.values().size();
			}

			if (me.ships.values().size() <= enemy_ships_num+3 && me.halite >= needed_amount && !gameMap.at(me.shipyard.position).isOccupied() &&  Constants.MAX_TURNS* 0.7 > game.turnNumber){
				commandQueue.add(me.shipyard.spawn());
			}
		} else {
			
			if (me.halite >= needed_amount && !gameMap.at(me.shipyard.position).isOccupied() ){
				if(gameMap.height == 32 && avg_halite > 60 &&  Constants.MAX_TURNS * 0.5 > game.turnNumber) commandQueue.add(me.shipyard.spawn());
				if(gameMap.height == 40 && avg_halite > 55 &&  Constants.MAX_TURNS * 0.5 > game.turnNumber) commandQueue.add(me.shipyard.spawn());
				if(gameMap.height == 48 && avg_halite > 50 &&  Constants.MAX_TURNS * 0.5 > game.turnNumber) commandQueue.add(me.shipyard.spawn());
				if(gameMap.height == 56 && avg_halite > 40 &&  Constants.MAX_TURNS * 0.6 > game.turnNumber) commandQueue.add(me.shipyard.spawn());
				if(gameMap.height == 64 && avg_halite > 35 &&  Constants.MAX_TURNS * 0.65 > game.turnNumber) commandQueue.add(me.shipyard.spawn());
			}
		}


			//Log.log("command queue length" + commandQueue.size());
            game.endTurn(commandQueue);
        }
    }
    public static int best_location(Ship ship, int[][] halite_amount, boolean[][] inspired, double mine_when_over, int[][] smallest_distance, GameMap gameMap, boolean[][] clumped, Game game){
				double current_value = -10000000;
				double current_best = 0;
				double time_taken = 0;
    			double base_exchange = Math.log(0.75);
				Position best_location = new Position(0,0);
				for(int i = 0; i < gameMap.width; i++){
					for(int j = 0; j < gameMap.width; j++){
						double local_halite = halite_amount[i][j];
						if (local_halite < mine_when_over){
						continue;
						}
						//if(inspired[i][j] && game.players.size()==2 && gameMap.calculateDistance(ship.position, new Position(i,j))>4)  local_halite /= 3; 
						time_taken =  Math.log(mine_when_over)/base_exchange - Math.log(local_halite)/base_exchange;
						int distance = 10;
						//if(gameMap.height < 50) distance = 7;
						if((boolean)inspired[i][j] && game.players.size() != 7 && gameMap.calculateDistance(ship.position, new Position(i,j)) < distance){
							if(gameMap.height == 32) local_halite *= 3.75;
							if(gameMap.height == 40) local_halite *= 3.5;
							if(gameMap.height == 48) local_halite *= 3.5;
							if(gameMap.height == 56) local_halite *= 3.25;
							if(gameMap.height == 64) local_halite *= 3;
							//Log.log("Inspired Works");
						}
						if((boolean)inspired[i][j] && game.players.size()==4){
							//local_halite *= 1.25;
						}

						current_value = (local_halite/4)/(gameMap.calculateDistance(ship.position, new Position(i,j)) + 1 + smallest_distance[i][j]);
						if((boolean)clumped[i][j]){
							//current_value *= 0.2;
						}
						if(gameMap.at(new Position(i,j)).isOccupied()){
							current_value = -1;
						}
						if(current_value > current_best){
							current_best = current_value;
							best_location = new Position(i,j);		
						}
					}
				}
		if(current_best == 0 ){
			return (ship.position.x *100 + ship.position.y);
		}
		return best_location.x*100 + best_location.y;
	}

    public static Direction new_naive_navigate(final Ship ship, final Position destination, final GameMap gameMap, boolean exploring) {
        // getUnsafeMoves normalizes for us
        Direction final_move = Direction.NORTH;
        ArrayList<Direction> possible_moves = gameMap.getUnsafeMoves(ship.position, destination);
        if(exploring||true) Collections.shuffle(possible_moves);
        for (final Direction direction : possible_moves) {
            final Position targetPos = ship.position.directionalOffset(direction);
            if (!gameMap.at(targetPos).isOccupied()) {
                gameMap.at(targetPos).markUnsafe(ship);
                return direction;
            }
        }
        if (!gameMap.at(ship.position).isOccupied()){
        	gameMap.at(ship.position).markUnsafe(ship);
        	//Log.log("I am staying still because this is where I want to go");
        	return Direction.STILL;
    	}

			
    		List can_move = new ArrayList();
    		if (!gameMap.at(ship.position.directionalOffset(Direction.NORTH)).isOccupied()){
    			can_move.add(Direction.NORTH);
			}
			if (!gameMap.at(ship.position.directionalOffset(Direction.SOUTH)).isOccupied()){
    			can_move.add(Direction.SOUTH);
			}
			if (!gameMap.at(ship.position.directionalOffset(Direction.EAST)).isOccupied()){
    			can_move.add(Direction.EAST);
			}			
			if (!gameMap.at(ship.position.directionalOffset(Direction.WEST)).isOccupied()){
    			can_move.add(Direction.WEST);
			}
			if (can_move.size() == 0) {
				gameMap.at(ship.position).markUnsafe(ship);
				return Direction.STILL;
			}

			
			int random = new Random().nextInt(can_move.size());
			final_move = (Direction)can_move.get(random);
			gameMap.at(ship.position.directionalOffset(final_move)).markUnsafe(ship);
			return final_move;
    
		}

    
    
}
