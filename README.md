# Halite3
Chessjawa's Halite 3 Bot ~ Rank 30 Pending Verification - 31st before Finals
If you are here for good quality code then you should turn around. There is nothing in this bot that one should try to duplicate.
This is the first time I have competed in such a manner, and while I performed decently my bot was significantly under developed compared to the others.


Now we start the list of people that I should thank for all they did for me in the competition.

Thanks to Two Sigma and Janzert for all the work they have done on the competition and making my first AI competition like this such a good time. Specifically to Janzert, thank you for tolerating me asking for all ranking information and the other usually stupid requests I made.

Also thanks to Teccles, reCurse, SiestaGuru, BlasterPoard, and Dmahan93 for all the advice they gave me during the compeition. I couldn't have made it nearly as far as I did without their help and advice for what my bot wasn't doing well and what I could do to improve.

Thank you to Dhallstr, Lunariz and all the other people who were around my level that motivated me to stay moving forward to try to keep up (I didn't manage to keep up, but I was close).

Lastly, thank you to Mlomb. Not only did he make a godly site that gave me the vast majority of my information and let know how to progress, but he also gave me a target in the sky to shoot for. I didn't land close to the title of top high schooler this year because of Mlomb, but he was always at the top as a lofty goal that proved high schoolers could compete at the top.


With that out of the way we  arrive at the code explanation section.

                              EDITOR
The first important part of my bot was choosing what editor I wanted to use. I was too lazy to actually look for one, so I settled with the online editor. Everyone else seems to hate it, and it has large problems, but it wasn't the worst thing I could have used.
The editor probably had a noticeable effect on my bot because I was unable to test 4p games. This led to lots of strange and usually very flawed 4p strategies that culminated in the garbled mess I will explain later.

Some quick notes before hand. I still use the markUnsafe and gameMap.at(position).ship as key parts of my bot, so if I mention mark unsafe I mean that. Since it was in the online editor all my code is in myBot and the hlt files are untouched. Returning is also basically untouched from the tutorial bot, so nothing innovative there.

                            EARLY PROBLEMS
The very early versions of my bot gridlocked because I was using Naive Navigate, and that was the first big problem my bot encounters. Figuring out how to have ships swap positions effciently was the cornerstone of how my bot was built. My solution to this problem was to have all my ships mark their spaces as safe to go to. This gives the assumption that my ships will move off the space. However, this leaves the problems with ships that want to stay still. If I just ran my movement code now there would be problems with ships moving onto ships that wanted to mine. That led my to code in a movement cycle, but only to check if ships wanted to stay still and give them priority. This decision to split mining on the second day lead to how my bot was built for the rest of the competition.

                            Mining
That leads nicely into how I decided if I wanted a ship to mine or not. I ran a couple of checks, but it basically boiled down to comparing the halite on their space to a variable.
The variable "mine_when_over" was simply a value based on average halite on the board that was used to check if you should mine. It is set lower than avg halite because it worked - More specifically was because once the halite on the map becomes roughly level, mining when at or close to average becomes very scarce especially if there was a large deposit increasing the average. 
The other check that I ran was making sure that a adajcent space wasn't a ton better. I wouldn't mine on a space if the space next to it was twice as large. I had tested 0.571 as the multiplier because that was the result of mining twice on the current space vs moving once and mining on the adajcent space, but it was not as useful.

                          Target Selection
I did my target selection in a relatively basic manner. Every ship evaluated every space for halite per turn and selected the best one. Once all my ships had selected their best target I dealt with conflicts. I resolved all of them based on the ships distance from the target location. I didn't use any kind of search algorithm to create paths or anything of the nature. There were a couple of other ways that I had thought about resolving conflicts, but it wasn't a high enough priority for me.

                        Pathing
Once I selected a target for all my ships, I used a slightly modified naive navigate to get there. There were a couple big changes that made it more useful.
1. Randomizing the order that it looks at moves. You don't want to cut clean paths when exploring. You want to mine the entire areas, and that causes ships prioritizing vertically or horizontally to be bad. There are better ways to do it than random, but random worked well enough and I never got around to implementing anything else.
2. Making a random move when you can't stay still. This is more for my bot because of my 2 movement cycles, but this fixed a lot of later collision problems.
3. Renaming it new_naive navigation. I was pretty sure I had changed 3 things, but I must have reverted the 3rd at some point, so my final improvement was giving it a better name. I didn't want to be insulting my own navigation every time I called it.

                      Drop Offs
 Drop offs were not all that complex for me. I decided I wanted to build a drop off if at any time more than 30% of my ships were returning home and num_ships > 13 * num_dropoffs
 I decided on that because I didn't want to build drop offs super early or a ton in a short period of time, but the % of returning ships also makes it more need base than just basing it off ships. 
 I decided where to build drop offs by finding the places on the map with the most halite in a radius around my ships exact numbers are there, but for the most part it was benefitting drop offs that were close to halite and closer to my other drop offs.
 Closer to my old drop offs was a relic from when I placed them super aggresivly. 20/20 hindsight tells me that I maybe should have tried not having that in the new system.

                      Inspiration
Halite *  3 DONE
That isn't actually quite accurate. My ships only check in a radius of 10 around them since inspiration was fleeting. If I checked the entire map then my ships would travel too far for too little gain. I also increased the * x value as the map size decreased. Inspiration is more important on smaller maps, so I valued it more highly there.

                    Collision Avoidance
In 2p don't. In 4p cry
My 4p collision avoidance was 4 around enemy ships, but with exceptions.
If I had more friendlies than enemies around I wouldn't avoid.
I wouldn't avoid if the space the enemy was on had more halite than my mine_when_over value (Basically if I would mine there)
This didn't work at all. If you want clear example of this look at my 4p winrates. Currently at 260 games it is 50 winrate and 70 h2h in 64x64 where collisions are less important and 5.41 winrate and 35.14 h2h in 32x32 where avoidance is the most important.

That is basically my bot.

There are technical details I left out that you can find in my code, but it is dangerous in there. You see things like dozens of lines of code commented out with smaller comments inside that.
If there is anything I left out or needs clarification then bother me on discord.
Thanks,
Chessjawa
