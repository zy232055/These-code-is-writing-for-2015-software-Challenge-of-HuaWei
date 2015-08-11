import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


/**
 * HuaWei SoftWare
 * @author zhangyu201507011-1  <br> email : zhangyuchnqd@163.com
 */
public class tbbf {

	static File file = new File("60.txt");
	private static final String[] POINTS_STRING = { "A", "2", "3", "4", "5",
			"6", "7", "8", "9", "10", "J", "Q", "K", "A" };
	
	private static final int[] POINTS_INT = { 1,2, 3, 4, 5, 6, 7, 8, 9, 10,
			11, 12, 13, 14 };
	static boolean fireWN = false;
	private static int playCount = 0;
	static int myBet = 0;
	static boolean fire = false;
	static boolean isHold = false;
	static boolean isFlop = false;
	static boolean isTurn = false;
	static boolean isRiver = false;
	static int blind = 40;
	static boolean isBigBlind = false;
	static boolean isSmallBlind = false;
	static boolean isPerfeckSeat = false;
	static boolean isTwoPeople = true;
	static boolean isSixQian = false;
	static boolean isSixBai = false;
	/** -------------------------------Main Method---------------------------------------- 
	 * @throws IOException 
	 * @throws InterruptedException **/
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
		BufferedReader sin = null;
		PrintWriter pw = null;
		boolean sbAllIn = false;
		int pokerLevel = 13;
		boolean hasPocketPair = false;
		int highCardType = -1;
		int flushType = -1;
		int straightType = -1;
		int currentPlayerCount = 0;
		boolean fireInHold = false;
		LinkedList<Poker> allPoker = new LinkedList<Poker>();
		
		LinkedList<LinkedList<Poker>> maxLevelPokerCombin = new LinkedList<LinkedList<Poker>>();
		initPockerList(allPoker);
		
		Map<Integer, LinkedList<Poker>> allCombination  =  new HashMap<Integer, LinkedList<Poker>>();
		
	//	Map<String,Map<String, List<Integer>>> notifyMap = new HashMap<String, Map<String,List<Integer>>>();
		
	//	Map<String, List<Map<String, Object>>> notifyMap = new HashMap<String, List<Map<String,Object>>>();
		Map<Integer,Double> lastBet = new HashMap<Integer, Double>();
		Map<Integer,FIFO<A>> otherRaise = new HashMap<Integer, FIFO<A>>();
		Map<Integer,Double> AveRaise = new HashMap<Integer, Double>();
		Map<Integer,Integer> RaiseCount = new HashMap<Integer, Integer>();
		List<String> allPlayer = new ArrayList<String>();
		
		long maxPokerGrade = 0l;
		Socket socket = null;
		try {
			socket = new Socket();
			while(!socket.isConnected()){
			try {
				
				socket.setReuseAddress(true);

				InetSocketAddress inetSocketAddress = new InetSocketAddress(
						args[2], Integer.parseInt(args[3]));

				socket.bind(inetSocketAddress);
				socket.connect(new InetSocketAddress(args[0], Integer.parseInt(args[1])));

				}  catch (Exception e) {
					socket = new Socket();
					//System.out.println("register failed and try");
					Thread.sleep(500);
					
				}
			}
			sin = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream());
			pw.print("reg: " + args[4] + " myplay2 need_notify \n");
			pw.flush();
			String readLine = null;
			boolean needPlay = true;
			while (true) {
				readLine = sin.readLine();
				//System.out.println(readLine);
				if (readLine.contains("game-over")) {
					needPlay = true;
					break;
				} else {
					if (readLine.contains("seat/")) {// seat info
						currentPlayerCount = 0;
						fire = false;
						myBet = 0;
						isBigBlind = false;
						isSmallBlind = false;
						isPerfeckSeat = false;
						readLine = sin.readLine();
						String islast = "";
						int maxC = 0;
						// check me is big or small blind and isPerfeckSeat
						while (!readLine.contains("/seat")) {
							if(maxC >= 9)
								break;
							maxC ++;
							currentPlayerCount ++;
							String[] s = readLine.split(" ");
							if(playCount == 0 || playCount == 1)
							{
								if(s.length == 4){
									int total = pareseToInt(s[2]) + pareseToInt(s[3]);
									if(total > 5000)
										isSixQian = true;
									if(total < 700)
										isSixBai = true;
									System.out.println(total);
								}
								if(readLine.contains("big"))
									isTwoPeople = false;
							}
							// is me?
							if (s.length == 5) {
								checkMyMoney(args[4],s[2],s[3],s[4]);
								checkTheBlind(args, s, 2, 0);
							}else if(s.length == 4){
								checkMyMoney(args[4],s[1],s[2],s[3]);
								checkThePerFectSeat(args, s, 1);
							}else {
								checkMyMoney(args[4],s[0],s[1],s[2]);
							}
							islast = readLine;
							readLine = sin.readLine();
							if (!isBigBlind && !isSmallBlind && !isPerfeckSeat) {
								if (readLine.contains("/seat")) {
									// check am i the PerfeckSeat?
								//	checkThePerFectSeat(args, islast.split(" "), 0);
								}
							}
						}
					}
					// fa pai le !!!
					if (readLine.contains("blind/")) {
						readLine = sin.readLine();
						String s[] = readLine.split(" ");
						blind = pareseToInt(s[1].trim()) * 2;
						if(blind == 20)
							blind = blind * 2;
					}
					if (readLine.contains("hold/")) {
						FileWriter out = null;
						try{
						out = new FileWriter(file, true);
						}catch (IOException e){
							e.printStackTrace();
						}
						fireInHold = false;
						fire = false ;
						fireWN = false;
						initPockerList(allPoker);
						playCount++;
						isTurn = false;
						isFlop = false;
						isHold = true;
						isRiver = false;
						System.out.println(playCount);
						try{
							out.write(playCount + "\n");
							out.close();
							}catch (IOException e){
								e.printStackTrace();
							}
						checkAllPlayersCL(RaiseCount, AveRaise, lastBet, otherRaise, null, 0);
						readLine = sin.readLine();
						for (int i = 0; i < 2; i++) {
							String[] s = readLine.split(" ");
							if (i == 0) {
								allPoker.set(i,addPoker(s[0],s[1]));
							} else {
								allPoker.set(i,addPoker(s[0],s[1]));
							}
							readLine = sin.readLine();
						}
					}
					// fa pai jieshu

					// server ask me how to play
					if (readLine.contains("inquire/")) {

						System.out.println("isSixQian="+isSixQian);
						System.out.println("isSixBai"+isSixBai);
						boolean ifHaveDog = false;
						boolean ifHaveCat = false;
						int NotFoldPeopleCount = 0; 
						int sbCheckCount = 0;
						int raiseBet = 0;
						int maxTotalBet = 40;//max give bet the oher player
						int totalPot = 0 ; //current pot
						boolean sbRaise = false;
						sbAllIn = false;
						boolean sbNotFold = false;
						int myBottom = 0;
						readLine = sin.readLine();
						StringBuilder sb = new StringBuilder();
						//check the raiseMaxBet and totalPot
						int maxC = 0;
						while (!readLine.contains("/inquire")) {
							if(maxC > 15){
							pw.print("fold \n");
							pw.flush();
							}
							maxC ++;
							String s[] = readLine.split(" ");
							int Bet = 0;
							if(s[1].contains("pot")){
								totalPot = pareseToInt(s[2].trim());
							} else {
								if(readLine.contains("raise") || readLine.contains("call") || readLine.contains("all_in")){ 
									sbNotFold = true;
								}
								if(!readLine.contains(args[4]) && (readLine.contains("raise")|| readLine.contains("call")|| readLine.contains("all_in")|| readLine.contains("check"))){
									NotFoldPeopleCount ++;
								}
								if(!readLine.contains(args[4]) && readLine.contains("check")){
									sbCheckCount ++;
								}
								sb.append(readLine + "\n");
								if(readLine.contains(args[4])){
									myBottom = pareseToInt(s[3].trim());
								}
								Bet = pareseToInt(s[3].trim());
								if (s[4].contains("all_in")) {
									sbAllIn = true;
									sbRaise = true;
								} else if (s[4].contains("raise")) {
									sbRaise = true;
								}
								if(maxTotalBet < Bet){
									maxTotalBet = Bet;
								}
							}
							readLine = sin.readLine();
						}
						String[] s = sb.toString().split("\n");
								for(int len = s.length, i = 0 , j = 0; i < len; i++)
								{
								//	System.out.println(s[i]);
									if((s[i].contains("raise") || s[i].contains("all_in") 
										|| s[i].contains("check") || s[i].contains("call")
											|| s[i].contains("blind")) && !s[i].contains(args[4])){ 
												for(j = i + 1; j < len; j++)
											{
													if((s[j].contains("raise") || s[j].contains("all_in") 
															|| s[j].contains("check") || s[j].contains("call")
																|| s[j].contains("blind")))
													{
														String[] s1 = s[i].split(" ");
														String[] s2 = s[j].split(" ");
														int raise = pareseToInt(s1[3]) - pareseToInt(s2[3]);  
														//System.out.println(raise);
														checkAllPlayersCL(RaiseCount, AveRaise, lastBet, otherRaise, s[i].split(" "), raise);
														break;
													}
											}
												if(j == len)
													{
														String[] s1 = s[i].split(" ");
														int raise = pareseToInt(s1[3]);
														checkAllPlayersCL(RaiseCount, AveRaise, lastBet, otherRaise, s[i].split(" "), raise);
													}
									}
								}
								int minAve = 55555;
								boolean aveIs0 = false;
								boolean HitCat = false;
								boolean dangerous = false;
								for(int len = s.length, i = 0; i < len; i++)
								{
								//	System.out.println(s[i]);
									if((s[i].contains("raise") || s[i].contains("all_in") 
										|| s[i].contains("check") || s[i].contains("call")
											|| s[i].contains("blind")) && !s[i].contains(args[4])){ 
										String[] s1 = s[i].split(" ");
										if(AveRaise.get(pareseToInt(s1[0])) != null)
										{
											int count = RaiseCount.get(pareseToInt(s1[0]));
											double ave = AveRaise.get(pareseToInt(s1[0]));
											if(ave >= 10 * blind && (double)count/playCount >= 0.35)
												{
													dangerous = true;
												}
											if((double)count/playCount >= 0.35)
											{
												HitCat = true;
											}
											if(minAve > ave && ave > 0)
												minAve =(int) ave;
										 	else if(ave == 0)
												aveIs0 = true;
										}
									}
								}
						
						boolean HitDog = false;
						boolean HitMouse = false;
						if(minAve == 55555)
						{
							minAve = blind * 3;
						//	System.out.println("error" + playCount);
						}
						else if(aveIs0 || minAve <= blind * 3)
						{
							minAve = blind * 3 - 1;
						}
						if(dangerous)
						{
							HitDog = true;
						//	System.out.println("HitDog True");
						}
						else 
						{
							HitMouse = true;
						}
						raiseBet = maxTotalBet - myBottom;
						if(myBottom == maxTotalBet){
							sbAllIn = false;
							sbRaise = false;
						}
						if(HitCat)
							{
								HitMouse = false;
						//		System.out.println("HitCat");
							}
						else{
							//System.out.println("HitMouse True");
						}
						if(minAve < 3 * blind)
							minAve = 3 * blind;
						int NBRaise = blind * 15;
						if(!isSixQian && !isSixBai && NBRaise < (int)(myBet/6))
							NBRaise = (int)(myBet/6);
						else if((isSixQian || isSixBai )&& NBRaise < (int)(myBet/9)){
							NBRaise = (int)(myBet/9);
						}
						//System.out.println("isBigBlind="+isBigBlind+" isSmallBlind"+isSmallBlind+" "+" maxTotalBet="+maxTotalBet+" totalPot="+totalPot);
						//sent to server I how to play
						//System.out.println("currentPlayerCount="+currentPlayerCount+" myBet="+myBet +" playCount="+playCount);
						int avgTotalBet = 17000;
						if(isTwoPeople)
							avgTotalBet = 4100;
						if(isSixQian)
							avgTotalBet = 25000;
						if(isSixBai)
							avgTotalBet = 320;
						if (!NeedToPlay(currentPlayerCount,avgTotalBet)) { //if Need to play . I join in 
							needPlay = false;
						}
						if(!needPlay) {
							pw.print("fold \n");
							pw.flush();
						}else{
							//I have two poker
							if(isHold){
								if(isTwoPeople){
									hasPocketPair = checkPocketPair(allPoker.get(0).getNum(),allPoker.get(1).getNum());
									
									if (hasPocketPair) {
										if (allPoker.get(0).getNum() >= 10 && !fireInHold) {
											pw.print("raise "+ (blind * 3 - 1) + " \n");
											pw.flush();
										}
										else if (allPoker.get(0).getNum() >= 10 && fireInHold) {
												pw.print("call \n");
												pw.flush();
										} 
										 else {
											 if (allPoker.get(0).getNum() > 5 
													&& raiseBet <= (3 * blind + 1) && !fireInHold)
														 {
												 	pw.print("raise "+ (blind * 4 - 1)+ " \n");
													pw.flush();
													fireInHold = true;
											}
											else if (allPoker.get(0).getNum() >= 2 
													&& raiseBet <= (3 * blind + 1)
														) {
												pw.print("call \n");
												pw.flush();
											}
											else if(isSmallBlind && currentPlayerCount <= 2&& raiseBet == 0
													 ){
												pw.print("raise " + (3 * blind - 1) +" \n");
												pw.flush();
												fireInHold = true;
											}
											else if(isSmallBlind && currentPlayerCount <= 2&& raiseBet <= minAve
													 ){
												pw.print("call \n");
												pw.flush();
											}
											else {
													pw.print("fold \n");
													pw.flush();
											}
										}
	
									} else {
										// 2. I hava a high card
										highCardType = checkAandMaxCard(allPoker.get(0).getNum(),allPoker.get(1).getNum());
	
										// 3. I hava A and other poker is max than 10
										switch (highCardType) {
										case -1: // no A
											if(allPoker.get(0).getNum()>=11
												&&allPoker.get(1).getNum()>=11 
													&& raiseBet < 1.5 * minAve
														&& !fireInHold)
											{
												pw.print("raise "+ minAve  + " \n");
												pw.flush();
												fireInHold = true;
											}
											else if(allPoker.get(0).getNum()>=11
													&&allPoker.get(1).getNum()>=11 
														)
												{
													pw.print("call \n");
													pw.flush();
												}
											else if(allPoker.get(0).getNum() > 8
													&&allPoker.get(1).getNum() > 8 
														&&allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&&raiseBet <= minAve && !fireInHold)
															
												{
													pw.print("raise "+ minAve + " \n");
													pw.flush();
													fireInHold = true;
												}
											else if((allPoker.get(0).getNum() > 11
													||allPoker.get(1).getNum() > 11 )
													&& raiseBet <= 1.5 * minAve
														)
												{
													pw.print("call \n");
													pw.flush();
												}
											else if((allPoker.get(0).getNum() > 11
													||allPoker.get(1).getNum() > 11 )
													&& raiseBet == 0 && !fireInHold
														)
												
												{
													pw.print("raise "+ minAve + " \n");
													pw.flush();
													fireInHold = true;
												}
											else if((allPoker.get(0).getNum() > 10
													||allPoker.get(1).getNum() > 10 )
													&& raiseBet <= minAve && !fireInHold
														)
												
												{
												pw.print("call \n");
												pw.flush();
												}
											else if(allPoker.get(0).getNum() > 6 && allPoker.get(1).getNum() > 6
															&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
																&& ((allPoker.get(0).getNum() + 1 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 1 == allPoker.get(1).getNum())
																	&& raiseBet <= minAve )
											{
												pw.print("call \n");
												pw.flush();
											}
											
											else if(allPoker.get(0).getNum() > 6 && allPoker.get(1).getNum() > 6
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&& ((allPoker.get(0).getNum() + 2 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 2 == allPoker.get(1).getNum())
															&& raiseBet <= minAve )
											{
												pw.print("call \n");
												pw.flush();
											}
											else if (isSmallBlind && !sbAllIn && !sbNotFold) {
												pw.print("check \n");
												pw.flush();
											}
											else {
													pw.print("fold \n");
													pw.flush();
											}
											
											break;
										case 1: // high and has A
										case 0: // min and has A
										case 2: // min and has A
												if(raiseBet <= 2 * minAve && !fireInHold){
													pw.print("raise "+ minAve + " \n");
													pw.flush();
													fireInHold = true;
												}else {
														pw.print("call \n");
														pw.flush();
													}
											break;	
										}
										
									}
					
								}
								else if(playCount < 10)
										{
										if(raiseBet == 0 &&(isSmallBlind||isBigBlind))
												{
													pw.print("call \n");
													pw.flush();
												}
												
										else{
													pw.print("fold \n");
													pw.flush();
											}
										}
									else if(currentPlayerCount <= 4){
									hasPocketPair = checkPocketPair(allPoker.get(0).getNum(),allPoker.get(1).getNum());
									
									if (hasPocketPair) {
										if (allPoker.get(0).getNum() >= 10 && !fireInHold) {
											pw.print("raise "+ (blind * 3 - 1) + " \n");
											pw.flush();
										}
										else if (allPoker.get(0).getNum() >= 10 && fireInHold) {
												pw.print("call \n");
												pw.flush();
										} 
										 else {
											 if (allPoker.get(0).getNum() > 5 
													&& raiseBet <= 1.5 * minAve && !fireInHold)
														 {
												 	pw.print("raise "+ minAve + " \n");
													pw.flush();
													fireInHold = true;
											}
											else if (allPoker.get(0).getNum() > 5 
													&& raiseBet <= 1.5 * minAve
														) {
												pw.print("call \n");
												pw.flush();
											}
											else if (isBigBlind && !sbRaise && !sbAllIn && !sbNotFold) {
												pw.print("check \n");
												pw.flush();
											}else if (isBigBlind && raiseBet < minAve && !fireInHold) {
												pw.print("raise " + minAve +" \n");
												pw.flush();
												fireInHold = true;
											}else if (isBigBlind && raiseBet <= minAve && fireInHold) {
												pw.print("call \n");
												pw.flush();
											}
											else if(isSmallBlind && currentPlayerCount <= 2&& raiseBet <= minAve && !fireInHold
													 ){
												pw.print("raise " + minAve +" \n");
												pw.flush();
												fireInHold = true;
											}
											else if(isSmallBlind && currentPlayerCount <= 2&& raiseBet <= minAve && fireInHold
													 ){
												pw.print("call \n");
												pw.flush();
											}
											else if (isBigBlind && !sbRaise && !sbAllIn && sbNotFold && raiseBet == 0) {
												pw.print("call \n");
												pw.flush();
											}
											else {
													pw.print("fold \n");
													pw.flush();
											}
										}
	
									} else {
										// 2. I hava a high card
										highCardType = checkAandMaxCard(allPoker.get(0).getNum(),allPoker.get(1).getNum());
	
										// 3. I hava A and other poker is max than 10
										switch (highCardType) {
										case -1: // no A
											if(allPoker.get(0).getNum()>=11
												&&allPoker.get(1).getNum()>=11 
													&& raiseBet < 1.5 * minAve
														&& !fireInHold)
											{
												pw.print("raise "+ minAve  + " \n");
												pw.flush();
												fireInHold = true;
											}
											else if(allPoker.get(0).getNum()>=11
													&&allPoker.get(1).getNum()>=11 
														)
												{
													pw.print("call \n");
													pw.flush();
												}
											else if(allPoker.get(0).getNum() > 8
													&&allPoker.get(1).getNum() > 8 
														&&allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&&raiseBet <= minAve && !fireInHold)
															
												{
													pw.print("raise "+ minAve + " \n");
													pw.flush();
													fireInHold = true;
												}
											else if((allPoker.get(0).getNum() > 11
													||allPoker.get(1).getNum() > 11 )
													&& raiseBet <= 1.5 * minAve
														)
												{
													pw.print("call \n");
													pw.flush();
												}

											else if(allPoker.get(0).getNum() > 6 && allPoker.get(1).getNum() > 6
															&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
																&& ((allPoker.get(0).getNum() + 1 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 1 == allPoker.get(1).getNum())
																	&& raiseBet <= minAve )
											{
												pw.print("call \n");
												pw.flush();
											}
											
											else if(allPoker.get(0).getNum() > 6 && allPoker.get(1).getNum() > 6
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&& ((allPoker.get(0).getNum() + 2 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 2 == allPoker.get(1).getNum())
															&& raiseBet <= minAve )
											{
												pw.print("call \n");
												pw.flush();
											}
											else if (isBigBlind && !sbAllIn && !sbNotFold) {
												pw.print("check \n");
												pw.flush();
											}
											else if (isBigBlind && !sbAllIn && sbNotFold && raiseBet < blind * 2) {
												pw.print("call \n");
												pw.flush();
											}
											else {
													pw.print("fold \n");
													pw.flush();
											}
											
											break;
										case 1: // high and has A
										case 0: // min and has A
										case 2: // min and has A
												if(raiseBet <= 2 * minAve && !fireInHold){
													pw.print("raise "+ minAve + " \n");
													pw.flush();
													fireInHold = true;
												}else {
														pw.print("call \n");
														pw.flush();
													}
											break;	
										}
										
									}
					
								}
									else if(dangerous && raiseBet < blind * 3){

										hasPocketPair = checkPocketPair(allPoker.get(0).getNum(),allPoker.get(1).getNum());
									if (hasPocketPair) {
										if (allPoker.get(0).getNum() >= 11 && !fireInHold ) {
											pw.print("raise "+ minAve + " \n");
											pw.flush();
											fireInHold = true;
										}
										else if (allPoker.get(0).getNum() >= 11 && fireInHold) {
												pw.print("call \n");
												pw.flush();
										} 
										 else {
											 if (allPoker.get(0).getNum() > 8 
													&& raiseBet <= 1.5 * minAve && !fireInHold
														) {
												pw.print("raise " + minAve +" \n");
												pw.flush();
												fireInHold = true;
											}else if (allPoker.get(0).getNum() > 8 
													&& raiseBet <= 1.5 * minAve && fireInHold
													) {
											pw.print("call \n");
											pw.flush();
										}
											
											else if (isBigBlind && !sbRaise && !sbAllIn && !sbNotFold) {
												pw.print("check \n");
												pw.flush();
											}else if (isBigBlind && raiseBet < minAve && !fireInHold) {
												pw.print("raise " + minAve +" \n");
												pw.flush();
												fireInHold = true;
											}else if (isBigBlind  && raiseBet <= minAve && fireInHold) {
												pw.print("call \n");
												pw.flush();
											}
											else if(isSmallBlind && raiseBet <= minAve && !fireInHold
													 ){
												pw.print("raise " + minAve +" \n");
												pw.flush();
												fireInHold = true;
											}
											else if(isSmallBlind && raiseBet <= minAve && fireInHold
													 ){
												pw.print("call \n");
												pw.flush();
											}
											else if (isBigBlind && !sbRaise && !sbAllIn && sbNotFold && raiseBet == 0) {
												pw.print("call \n");
												pw.flush();
											}
											else {
													pw.print("fold \n");
													pw.flush();
											}
										}
	
									} else {
										// 2. I hava a high card
										highCardType = checkAandMaxCard(allPoker.get(0).getNum(),allPoker.get(1).getNum());
	
										// 3. I hava A and other poker is max than 10
										switch (highCardType) {
										case -1: // no A
											if(allPoker.get(0).getNum()>=12
												&&allPoker.get(1).getNum()>=12 
													&& raiseBet < 1.5 * minAve
														&& !fireInHold)
											{
												pw.print("raise "+ minAve + " \n");
												pw.flush();
												fireInHold = true;
											}
											else if(allPoker.get(0).getNum()>=12
													&&allPoker.get(1).getNum()>=12 
														)
												{
													pw.print("call \n");
													pw.flush();
												}
											else if(allPoker.get(0).getNum() > 10
													&&allPoker.get(1).getNum() > 10 
														&&allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&&raiseBet <= minAve && !fireInHold
															)
												{
													pw.print("raise "+ blind * 2 + " \n");
													pw.flush();
													fireInHold = true;
												}
											else if(allPoker.get(0).getNum() > 10
													&&allPoker.get(1).getNum() > 10 
														&&allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														)
												{
													pw.print("call \n");
													pw.flush();
												}
											else if((allPoker.get(0).getNum() > 12
													||allPoker.get(1).getNum() > 12 )
														&&allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														)
												{
													pw.print("call \n");
													pw.flush();
												}
											else if((isSmallBlind||isPerfeckSeat||isBigBlind) 
														&& allPoker.get(0).getNum() > 8 && allPoker.get(1).getNum() > 8
															&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
																&& ((allPoker.get(0).getNum() + 1 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 1 == allPoker.get(1).getNum())
																	&& raiseBet <= (2 * blind + 1)&& !fireInHold)
											{
												pw.print("raise " + 2 * blind + 1 + " \n");
												pw.flush();
												fireInHold = true;
											}
											else if((isSmallBlind||isPerfeckSeat||isBigBlind) 
														&& allPoker.get(0).getNum() > 8 && allPoker.get(1).getNum() > 8
															&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
																&& ((allPoker.get(0).getNum() + 1 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 1 == allPoker.get(1).getNum())
																	&& raiseBet <= minAve  && fireInHold)
											{
												pw.print("call \n");
												pw.flush();
											}
											
											else if((isSmallBlind||isPerfeckSeat||isBigBlind) && allPoker.get(0).getNum() > 8 && allPoker.get(1).getNum() > 8
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&& ((allPoker.get(0).getNum() + 2 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 2 == allPoker.get(1).getNum())
															&& raiseBet <= (3 * blind - 1)&& !fireInHold)
											{
												pw.print("raise " + 2 * blind + 1 + " \n");
												pw.flush();
												fireInHold = true;
											}
											else if((isSmallBlind||isPerfeckSeat||isBigBlind) && allPoker.get(0).getNum() > 8 && allPoker.get(1).getNum() > 8
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&& ((allPoker.get(0).getNum() + 2 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 2 == allPoker.get(1).getNum())
															&& raiseBet <= minAve  && fireInHold)
											{
												pw.print("call \n");
												pw.flush();
											}
											else if (isBigBlind && !sbRaise && !sbAllIn && !sbNotFold) {
												pw.print("check \n");
												pw.flush();
											}
											else if (isBigBlind && raiseBet <= blind ) 
											{
												pw.print("call \n");
												pw.flush();
											}
											else {
													pw.print("fold \n");
													pw.flush();
											}
											
											break;
										case 1: // high and has A
												if(raiseBet <= 2 * minAve && !fireInHold){
													pw.print("raise "+ minAve + " \n");
													pw.flush();
													fireInHold = true;
												}else {
														pw.print("call \n");
														pw.flush();
													}
												
											break;
										case 0: // min and has A
										case 2: // min and has A
											
												if (isBigBlind && !sbRaise && !sbAllIn && !sbNotFold) {
													pw.print("check \n");
													pw.flush();
												}else if((isSmallBlind||isPerfeckSeat||isBigBlind) 
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&& raiseBet <= 1.5 * minAve && !fireInHold)
											{
												pw.print("raise " + minAve + " \n");
												pw.flush();
												fireInHold = true;
											}
											else if((isSmallBlind||isPerfeckSeat||isBigBlind)
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&&  raiseBet <= 1.5 * minAve  && fireInHold)
											{
												pw.print("call \n");
												pw.flush();
											}
											else if(raiseBet <= 1.5 * minAve
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
													 ){
														pw.print("call \n");
														pw.flush();
													}
												else {
														pw.print("fold \n");
														pw.flush();
												}
											break;
										}
									}
								}
									else if(dangerous){
										if(allPoker.get(0).getNum() > 11 && allPoker.get(1).getNum() > 11)
										{
											pw.print("call \n");
											pw.flush();
										}
										else{
											pw.print("fold \n");
											pw.flush();
										}
										
									}
									else{
										hasPocketPair = checkPocketPair(allPoker.get(0).getNum(),allPoker.get(1).getNum());
									if (hasPocketPair) {
										if (allPoker.get(0).getNum() >= 11 && !fireInHold ) {
											pw.print("raise "+ minAve + " \n");
											pw.flush();
											fireInHold = true;
										}
										else if (allPoker.get(0).getNum() >= 11 && fireInHold) {
												pw.print("call \n");
												pw.flush();
										} 
										 else {
											 if (allPoker.get(0).getNum() > 8 
													&& raiseBet <= (2 * blind + 1)&& !fireInHold
														) {
												pw.print("raise " + minAve +" \n");
												pw.flush();
												fireInHold = true;
											}else if (allPoker.get(0).getNum() > 8 
													&& raiseBet <= minAve && fireInHold
													) {
											pw.print("call \n");
											pw.flush();
										}
											
											else if (isBigBlind && !sbRaise && !sbAllIn && !sbNotFold) {
												pw.print("check \n");
												pw.flush();
											}else if (isBigBlind && raiseBet <= 2 * blind) {
												pw.print("call \n");
												pw.flush();
											}
											else if(isSmallBlind && raiseBet <= (2 * blind + 1) && !fireInHold
													 ){
												pw.print("raise " + minAve +" \n");
												pw.flush();
												fireInHold = true;
											}
											else if(isSmallBlind && raiseBet <= minAve && fireInHold
													 ){
												pw.print("call \n");
												pw.flush();
											}
											else if (isBigBlind && !sbRaise && !sbAllIn && sbNotFold && raiseBet == 0) {
												pw.print("call \n");
												pw.flush();
											}
											else {
													pw.print("fold \n");
													pw.flush();
											}
										}
	
									} else {
										// 2. I hava a high card
										highCardType = checkAandMaxCard(allPoker.get(0).getNum(),allPoker.get(1).getNum());
	
										// 3. I hava A and other poker is max than 10
										switch (highCardType) {
										case -1: // no A
											if(allPoker.get(0).getNum()>=12
												&&allPoker.get(1).getNum()>=12 
													&& raiseBet < 1.5 * minAve
														&& !fireInHold)
											{
												pw.print("raise "+ minAve + " \n");
												pw.flush();
												fireInHold = true;
											}
											else if(allPoker.get(0).getNum()>=12
													&&allPoker.get(1).getNum()>=12 
														)
												{
													pw.print("call \n");
													pw.flush();
												}
											else if(allPoker.get(0).getNum() > 10
													&&allPoker.get(1).getNum() > 10 
														&&allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&&raiseBet <= minAve && !fireInHold
															)
												{
													pw.print("raise "+ (blind * 3 - 1) + " \n");
													pw.flush();
													fireInHold = true;
												}
											else if(allPoker.get(0).getNum() > 10
													&&allPoker.get(1).getNum() > 10 
														&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														)
												{
													pw.print("call \n");
													pw.flush();
												}
											else if(((allPoker.get(0).getNum() > 12 && allPoker.get(0).getNum() > 8)
													||(allPoker.get(1).getNum() > 12 && allPoker.get(0).getNum() > 8) )
														&&allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														)
												{
													pw.print("call \n");
													pw.flush();
												}
											else if((isSmallBlind||isPerfeckSeat||isBigBlind) 
														&& allPoker.get(0).getNum() > 8 && allPoker.get(1).getNum() > 8
															&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
																&& ((allPoker.get(0).getNum() + 1 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 1 == allPoker.get(1).getNum())
																	&& raiseBet <= (2 * blind + 1)&& !fireInHold)
											{
												pw.print("raise " + minAve + " \n");
												pw.flush();
												fireInHold = true;
											}
											else if((isSmallBlind||isPerfeckSeat||isBigBlind) 
														&& allPoker.get(0).getNum() > 8 && allPoker.get(1).getNum() > 8
															&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
																&& ((allPoker.get(0).getNum() + 1 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 1 == allPoker.get(1).getNum())
																	&& raiseBet <= minAve  && fireInHold)
											{
												pw.print("call \n");
												pw.flush();
											}
											
											else if((isSmallBlind||isPerfeckSeat||isBigBlind) && allPoker.get(0).getNum() > 8 && allPoker.get(1).getNum() > 8
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&& ((allPoker.get(0).getNum() + 2 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 2 == allPoker.get(1).getNum())
															&& raiseBet <= (2 * blind + 1)&& !fireInHold)
											{
												pw.print("raise " + minAve + " \n");
												pw.flush();
												fireInHold = true;
											}
											else if((isSmallBlind||isPerfeckSeat||isBigBlind) && allPoker.get(0).getNum() > 8 && allPoker.get(1).getNum() > 8
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&& ((allPoker.get(0).getNum() + 2 == allPoker.get(1).getNum())||allPoker.get(0).getNum() - 2 == allPoker.get(1).getNum())
															&& raiseBet <= minAve  && fireInHold)
											{
												pw.print("call \n");
												pw.flush();
											}
											else if (isBigBlind && !sbRaise && !sbAllIn && !sbNotFold) {
												pw.print("check \n");
												pw.flush();
											}
											else if (isBigBlind && raiseBet == 0 && !fireInHold) 
											{
												pw.print("call \n");
												pw.flush();
											}
											else {
													pw.print("fold \n");
													pw.flush();
											}
											
											break;
										case 1: // high and has A
												if(raiseBet <= 2 * minAve && !fireInHold){
													pw.print("raise "+ minAve + " \n");
													pw.flush();
													fireInHold = true;
												}else {
														pw.print("call \n");
														pw.flush();
													}
												
											break;
										case 0: // min and has A
										case 2: // min and has A
											
												if (isBigBlind && !sbRaise && !sbAllIn && !sbNotFold) {
													pw.print("check \n");
													pw.flush();
												}else if((isSmallBlind||isPerfeckSeat||isBigBlind) 
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&& raiseBet <= (3 * blind - 1) && !fireInHold)
											{
												pw.print("raise " + minAve + " \n");
												pw.flush();
												fireInHold = true;
											}
											else if((isSmallBlind||isPerfeckSeat||isBigBlind)
													&& allPoker.get(0).getColor().equals(allPoker.get(1).getColor())
														&&  raiseBet <= (3 * blind - 1)  && fireInHold)
											{
												pw.print("call \n");
												pw.flush();
											}
											else if(raiseBet <= minAve
													){
														pw.print("call \n");
														pw.flush();
													}
												else {
														pw.print("fold \n");
														pw.flush();
												}
											break;
										}
									}
								}
				
							}
							else if(isFlop){
								if(isTwoPeople && HitCat){
									System.out.println("isTwoCat");
									HitTwoPeopleCat(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom , raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
								}else if(isTwoPeople && HitMouse){
									System.out.println("isTwoMouse");
									HitTwoPeopleMouse(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom , raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
								}
								else if(dangerous && !HitCat)
								{System.out.println("isdangeous");
									minAve = blind * 3;
									HitMouseFlopTurn(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom , raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
								}
								// now I Have Five Poker
								else if(HitDog){
									System.out.println("HitDogflop");
									HitDogFlopTurn(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom , raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
									
									
								}
								else if(HitMouse){
									System.out.println("HitMouseflop");
									HitMouseFlopTurn(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom , raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
									
									
								}
								else{

									System.out.println("HitCatflop");
									HitCatFlopTurn(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom , raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
								}
							}else if(isTurn){
								if(isTwoPeople && HitCat){

									System.out.println("HitCatturnisTwoPeople");
									HitTwoPeopleCat(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom , raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
								}else if(isTwoPeople && HitMouse){

									System.out.println("HitMouseturnisTwoPeople");
									HitTwoPeopleMouse(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom, raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
								}
								else if(dangerous && !HitCat)
								{
									System.out.println("Hitdanreous");
									minAve = blind * 3;
									HitMouseFlopTurn(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom, raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
								}
								else if(HitDog){

									System.out.println("HitDogturn");
									HitDogFlopTurn(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom, raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
								}
								else if(HitMouse){

									System.out.println("HitMouseturn");
									HitMouseFlopTurn(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom, raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
								}
								else{

									System.out.println("HitCatturn");
									HitCatFlopTurn(totalPot,pokerLevel, flushType, straightType, allPoker, pw, flushType, NotFoldPeopleCount,
											sbCheckCount, myBottom, raiseBet, NBRaise, minAve , 
											ifHaveDog , ifHaveCat, pokerLevel);
									
								}
								
							}else if (isRiver) {
								System.out.println("isRiverIn~~~~~~~~~~~~~~~~~~~~~~~");
								if (HitDog) {
									flushType =  checkFlush(allPoker);
								//	System.out.println("flushType isFlop---------->"+flushType);
									switch (flushType) {
									case 2:
										pw.print("all_in \n");
										pw.flush();
										break;
									case 4:
										pw.print("all_in \n");
										pw.flush();
										break;
									case 1:
									case 3:
										if(raiseBet < (blind * 3)){
											pw.print("call \n");
											pw.flush();
										}else {
											pw.print("fold \n");
											pw.flush();
										}
										break;
									}
									
									if(flushType == -1){ //not the flush
										straightType = checkStraight(allPoker);
										//System.out.println("straightType isFlop---------->"+straightType);
										switch (straightType) {
										case 2:
											if(totalPot < 20 * blind){
												pw.print("raise "+ blind * 16 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 40 * blind){
												pw.print("raise "+ blind * 11 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 60 * blind){
												pw.print("raise "+ blind * 6 +" \n");
												pw.flush();
												break;
											}else{
												pw.print("call \n");
												pw.flush();
												break;
											}
										case 1:
											if(raiseBet < (blind * 3)){
												pw.print("call \n");
												pw.flush();
											}else {
												pw.print("fold \n");
												pw.flush();
											}
											break;
										}
										if(straightType == 0){ // not the straight
											
											pokerLevel = calculatePokerLevel(allPoker);
											
											switch (pokerLevel) {
											
											case 15:
											case 13:
											case 12:
											case 10:
											case 4:
												if(totalPot < 20 * blind){
													pw.print("raise "+ blind * 21 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 40 * blind){
													pw.print("raise "+ blind * 16 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 60 * blind){
													pw.print("raise "+ blind * 11 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 80 * blind){
													pw.print("raise "+ blind * 5 +" \n");
													pw.flush();
													break;
												}else{
													pw.print("call \n");
													pw.flush();
													break;
												}
											case 14:
											case 11:
											case 9:
												if(totalPot < 20 * blind){
													pw.print("raise "+ blind * 16 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 40 * blind){
													pw.print("raise "+ blind * 11 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 60 * blind){
													pw.print("raise "+ blind * 6 +" \n");
													pw.flush();
													break;
												}else if(raiseBet < (blind * 18)){
													pw.print("call \n");
													pw.flush();
													break;
												}
												else{
													pw.print("fold \n");
													pw.flush();
													break;
												}
											case 8:
											case 6:
												if(totalPot < 20 * blind){
													pw.print("raise "+ blind * 16 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 40 * blind){
													pw.print("raise "+ blind * 11 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 60 * blind){
													pw.print("raise "+ blind * 6 +" \n");
													pw.flush();
													break;
												}else{
													pw.print("call \n");
													pw.flush();
													break;
												}
											case 5:
												if(totalPot < 20 * blind){
													pw.print("raise "+ blind * 11 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 40 * blind){
													pw.print("raise "+ blind * 6 +" \n");
													pw.flush();
													break;
												}else{
													pw.print("call \n");
													pw.flush();
													break;
												}
											case 7:
											case 3:
												if(totalPot < 20 * blind){
													pw.print("raise "+ blind * 16 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 40 * blind){
													pw.print("raise "+ blind * 11 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 60 * blind){
													pw.print("raise "+ blind * 6 +" \n");
													pw.flush();
													break;
												}else{
													pw.print("call \n");
													pw.flush();
													break;
												}
											case 2:	
												if(totalPot < 20 * blind){
													pw.print("raise "+ blind * 8 +" \n");
													pw.flush();
													break;
												}else if(totalPot < 40 * blind){
													pw.print("raise "+ blind * 6 +" \n");
													pw.flush();
													break;
												}else{
													pw.print("call \n");
													pw.flush();
													break;
												}
											case 1:
												if(totalPot < 20 * blind){
													pw.print("raise "+ blind * 6 +" \n");
													pw.flush();
													break;
												}else{
													pw.print("call \n");
													pw.flush();
													break;
												}
											case -1:
													pw.print("call \n");
													pw.flush();
													break;
											default:
												if(!sbRaise){
													pw.print("call \n");
													pw.flush();
												}
												else 
												if(raiseBet <= (blind * 2 + 5)){
													pw.print("call \n");
													pw.flush();
												}else {
													pw.print("fold \n");
													pw.flush();
												}
												break;
											}
											
										}//straightType over
									}//flushType over
								
							}else{
								flushType =  checkFlush(allPoker);
							//	System.out.println("flushType isFlop---------->"+flushType);
								switch (flushType) {
								case 2:
									pw.print("all_in \n");
									pw.flush();
									break;
								case 4:
									pw.print("all_in \n");
									pw.flush();
									break;
								case 1:
								case 3:
									if(raiseBet < minAve){
										pw.print("call \n");
										pw.flush();
									}else {
										pw.print("fold \n");
										pw.flush();
									}
									break;
								}
								
								if(flushType == -1){ //not the flush
									straightType = checkStraight(allPoker);
									//System.out.println("straightType isFlop---------->"+straightType);
									switch (straightType) {
									case 2:
										if(totalPot < 20 * blind){
											pw.print("raise "+ blind * 16 +" \n");
											pw.flush();
											break;
										}else if(totalPot < 40 * blind){
											pw.print("raise "+ blind * 11 +" \n");
											pw.flush();
											break;
										}else if(totalPot < 60 * blind){
											pw.print("raise "+ blind * 6 +" \n");
											pw.flush();
											break;
										}else {
											pw.print("call \n");
											pw.flush();
											break;
										}
									case 1:
										if(raiseBet < minAve){
											pw.print("call \n");
											pw.flush();
										}else {
											pw.print("fold \n");
											pw.flush();
										}
										break;
									}
									if(straightType == 0){ // not the straight
										
										pokerLevel = calculatePokerLevel(allPoker);
										
										switch (pokerLevel) {
										
										case 15:
										case 13:
										case 12:
										case 10:
										case 4:
											if(totalPot < 20 * blind){
												pw.print("raise "+ blind * 26 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 40 * blind){
												pw.print("raise "+ blind * 16 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 60 * blind){
												pw.print("raise "+ blind * 11 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 110 * blind){
												pw.print("raise "+ blind * 6 +" \n");
												pw.flush();
												break;
											}
											else{
												pw.print("call \n");
												pw.flush();
												break;
											}
										case 14:
										case 11:
										case 9:
											if(totalPot < 20 * blind){
												pw.print("raise "+ blind * 16 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 40 * blind){
												pw.print("raise "+ blind * 11 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 60 * blind){
												pw.print("raise "+ blind * 6 +" \n");
												pw.flush();
												break;
											}else{
												pw.print("call \n");
												pw.flush();
												break;
											}
										case 8:
										case 6:
											if(totalPot < 20 * blind && raiseBet < 2 * minAve){
												pw.print("raise "+ blind * 16 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 40 * blind && raiseBet < 2 * minAve){
												pw.print("raise "+ blind * 11 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 60 * blind && raiseBet < 2 * minAve){
												pw.print("raise "+ blind * 6 +" \n");
												pw.flush();
												break;
											}else if(raiseBet < 2 * minAve){
												pw.print("call \n");
												pw.flush();
												break;
											}
											else{
												pw.print("fold \n");
												pw.flush();
												break;
											}
										case 5:
											if(totalPot < 20 * blind && raiseBet < 2 * minAve){
												pw.print("raise "+ blind * 11 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 40 * blind && raiseBet < 2 * minAve){
												pw.print("raise "+ blind * 6 +" \n");
												pw.flush();
												break;
											}else if(raiseBet < 2 * minAve){
												pw.print("call \n");
												pw.flush();
												break;
											}
											else{
												pw.print("fold \n");
												pw.flush();
												break;
											}
										case 7:
										case 3:
											if(totalPot < 20 * blind ){
												pw.print("raise "+ blind * 16 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 40 * blind ){
												pw.print("raise "+ blind * 11 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 60 * blind ){
												pw.print("raise "+ blind * 6 +" \n");
												pw.flush();
												break;
											}
											else{
												pw.print("call \n");
												pw.flush();
												break;
											}
										case 2:	
											if(totalPot < 20 * blind && raiseBet < 2 * minAve){
												pw.print("raise "+ blind * 8 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 40 * blind && raiseBet < 2 * minAve){
												pw.print("raise "+ blind * 6 +" \n");
												pw.flush();
												break;
											}else if(raiseBet < 2 * minAve){
												pw.print("call \n");
												pw.flush();
												break;
											}
											else{
												pw.print("fold \n");
												pw.flush();
												break;
											}
										case 1:
											if(totalPot < 20 * blind && raiseBet <= 2 * minAve){
												pw.print("raise "+ blind * 6 +" \n");
												pw.flush();
												break;
											}else if(totalPot < 40 * blind && raiseBet <= 2 * minAve){
												pw.print("raise "+ blind * 4 +" \n");
												pw.flush();
												break;
											}else if(raiseBet < 2 * minAve){
												pw.print("call \n");
												pw.flush();
												break;
											}
											else{
												pw.print("fold \n");
												pw.flush();
												break;
											}
										case -1:
											if(totalPot < 20 * blind && raiseBet <=  minAve){
												pw.print("raise"+ (blind + 1)+"\n");
												pw.flush();
												break;
											}
											else if(raiseBet < 1.5 * minAve){
												pw.print("call \n");
												pw.flush();
												break;
											}
											else{
												pw.print("fold \n");
												pw.flush();
												break;
											}
										default:
											if(raiseBet < minAve){
												pw.print("call \n");
												pw.flush();
											}else {
												pw.print("fold \n");
												pw.flush();
											}
											break;
										}
										
									}//straightType over
								}//flushType over
							}
						} else {
							if(raiseBet < minAve){
								pw.print("call \n");
								pw.flush();
							}else {
								pw.print("fold \n");
								pw.flush();
							}
							break;
						}
						}
					}
					// recv notify msg
					if(readLine.contains("notify/")){
						boolean sbRaise = false ;
						int maxC= 0;
						readLine = sin.readLine();
						StringBuilder sb = new StringBuilder();
						while (!readLine.contains("/notify")) {
							int Bet = 0;
							if(maxC > 15){
								pw.print("fold \n");
								pw.flush();
							}
							maxC ++;
							if(readLine.contains("raise")){
								sbRaise = true ;
							}
							if (readLine.contains("all_in")) {
								sbAllIn = true;
							}
							sb.append(readLine + "\n");
							readLine = sin.readLine();
						}
						String[] s = sb.toString().split("\n");
						for(int len = s.length, i = 0 , j = 0; i < len; i++)
						{
							if((s[i].contains("raise") || s[i].contains("all_in") 
								|| s[i].contains("check") || s[i].contains("call")
									|| s[i].contains("blind")) && !s[i].contains(args[4]) && (sbRaise || !sbAllIn)){ 
										for(j = i + 1; j < len; j++)
									{
											if((s[j].contains("raise") || s[j].contains("all_in") 
													|| s[j].contains("check") || s[j].contains("call")
														|| s[j].contains("blind")))
											{
												String[] s1 = s[i].split(" ");
												String[] s2 = s[j].split(" ");
												int raise = pareseToInt(s1[3]) - pareseToInt(s2[3]);  
												checkAllPlayersCL(RaiseCount, AveRaise, lastBet, otherRaise, s[i].split(" "), raise);
												break;
											}
									}
										if(j == len)
											{
												String[] s1 = s[i].split(" ");
												int raise = pareseToInt(s1[3]);
												checkAllPlayersCL(RaiseCount, AveRaise, lastBet, otherRaise, s[i].split(" "), raise);
											}
							}
						}
					}
					
					//flop poker msg
					if(readLine.contains("flop/")){
						pokerLevel = 13;
						fire = false;
						isFlop = true;
						isHold = false;
						isRiver = false;
						isTurn = false;
						readLine = sin.readLine();
						for (int i = 2; i < 5; i++) {
							String[] s = readLine.split(" ");
							allPoker.set(i,addPoker(s[0],s[1]));
							readLine = sin.readLine();
						}
					}
					//turn poker msg
					if(readLine.contains("turn/")){
						pokerLevel = 13;
						fire = false;
						isTurn = true;
						isFlop = false;
						isHold = false;
						isRiver = false;
						readLine = sin.readLine();
						String[] s = readLine.split(" ");
						allPoker.set(5,addPoker(s[0],s[1]));
						readLine = sin.readLine();
					}
					//river poker msg
					if(readLine.contains("river/")){
						System.out.println("isRiverContain---");
						pokerLevel = 13;
						fire = false;
						isTurn = false;
						isFlop = false;
						isHold = false;
						isRiver = true;
						readLine = sin.readLine();
						String[] s = readLine.split(" ");
						allPoker.set(6,addPoker(s[0],s[1]));
						readLine = sin.readLine();
					}
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();			
				if (sin != null)
					sin.close();
				if (socket != null)
					socket.close();
				}
				finally {
					// System.out.println("close");
					if (sin != null)
						sin.close();
					if (pw != null)
						pw.close();
					if (socket != null)
						socket.close();
				}
		}

	private static void HitTwoPeopleMouse(int totalPot, int pokerLevel,
			int flushType, int straightType, LinkedList<Poker> allPoker,
			PrintWriter pw, int flushType2, int NotFoldPeopleCount,
			int sbCheckCount, int myBottom, int raiseBet,
			int NBRaise, int minAve, boolean ifHaveDog, boolean ifHaveCat,
			int pokerLevel2) {
		NBRaise = totalPot * 3;
		if(NBRaise > 20 * blind)
			NBRaise = 20 * blind;
		flushType =  checkFlush(allPoker);
		switch (flushType) {
		
		case 1:
			if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
				break;
			}
			else if(raiseBet <= minAve)
			{ 
				pw.print("call \n");
				pw.flush();
				break;
			}else
			{ 
				pw.print("fold \n");
				pw.flush();
				break;
			}
			break;
		case 2:
			pw.print("raise "+ minAve + " \n");
		 	pw.flush();
			break;
		case 3:
			if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
				break;
			}
			else if(raiseBet <= minAve)
			{ 
				pw.print("call \n");
				pw.flush();
				break;
			}else
			{ 
				pw.print("fold \n");
				pw.flush();
				break;
			}
			break;
		case 4:
			pw.print("raise "+ minAve + " \n");
			pw.flush();
			break;	
		}
		if(flushType == -1){ //not the flush
			straightType = checkStraight(allPoker);
			//System.out.println("straightType isFlop---------->"+straightType);
		
			switch (straightType) {
			case 2:
				if(totalPot <= blind * 50)
				{
					pw.print("raise "+ (blind * 4 - 1)+ " \n");
					pw.flush();
					break;
				}
				else if(totalPot > blind * 50)
				{
					pw.print("call \n");
					pw.flush();
					break;
				}
			case 1:
				if(NotFoldPeopleCount == sbCheckCount && !fire){
					pw.print("raise "+ NBRaise + " \n");
					pw.flush();
					fire = true;
				}
				else if(NotFoldPeopleCount == sbCheckCount && fire){
					pw.print("call \n");
					pw.flush();
				}else
				if(raiseBet == 0){
					pw.print("call \n");
					pw.flush();
					break;
				}else if(raiseBet <= minAve && isTurn && !fire)
				{ 
					pw.print("raise "+ NBRaise + " \n");
					pw.flush();
					fire = true;
					break;
				}
				else if(raiseBet <= minAve)
				{ 
					pw.print("call \n");
					pw.flush();
					break;
				}else
				{ 
					pw.print("fold \n");
					pw.flush();
					break;
				}
				break;
			}
			if(straightType == 0){ // not the straight
				
				pokerLevel = calculatePokerLevel(allPoker);
				//System.out.println("pokerLevel isFlop---------->"+straightType);
				//os.writeUTF("pokerLevel isFlop---------->"+straightType);
				switch (pokerLevel) {
				
				case 15:
				case 13:
				case 12:
				case 10:
				case 4:
					if(totalPot <= blind * 50)
					{
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}
					else if(totalPot > blind * 50)
					{
						pw.print("call \n");
						pw.flush();
						break;
					}
				case 14:
				case 11:
				case 9:
					if( raiseBet <= 1.5 * minAve && myBottom <=  (blind * 40))
					{
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else 
					{
						pw.print("call \n");
						pw.flush();
						break;
					}
				case 8:
				case 6:
					if( raiseBet <=  1.5 * minAve && myBottom <=  (blind * 30))
					{ 
						pw.print("raise " + minAve +" \n");
						pw.flush();
						break;
					}else if( raiseBet <=  2 * minAve && myBottom > (blind * 30))
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("fold \n");
						pw.flush();
					}
					break;
				case 5:
					if( raiseBet <=  minAve && myBottom <=  (blind * 10))
					{ 
						pw.print("raise " + minAve +" \n");
						pw.flush();
						break;
					}else if( raiseBet <=  1.5 * minAve && myBottom > (blind * 10))
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("fold \n");
						pw.flush();
					}
					break;
				case 7:
				case 3:
					if( raiseBet <= 2 * minAve && myBottom < (blind * 30))
					{
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}
					else
					{
						pw.print("call \n");
						pw.flush();
						break;
					}
				case 2:
					if(raiseBet == 0 && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}
					else if(raiseBet <= minAve && isTurn)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else if(raiseBet <= 2 * minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;

				case 1:
					if(raiseBet == 0 && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}
					else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
						break;
					}else if(raiseBet <= 1.5 * minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				case -1:
					if(raiseBet == 0 && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}
					else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
						break;
					}else if(raiseBet <=  minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				default:
					if(NotFoldPeopleCount == sbCheckCount && !fireWN){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fireWN = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fireWN){
						pw.print("call \n");
						pw.flush();
					}else if(raiseBet <= minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				}
				
			}//straightType over
		}//flushType over
	}






	
	private static void HitTwoPeopleCat(int totalPot, int pokerLevel,
			int flushType, int straightType, LinkedList<Poker> allPoker,
			PrintWriter pw, int flushType2, int NotFoldPeopleCount,
			int sbCheckCount, int myBottom, int raiseBet,
			int NBRaise, int minAve, boolean ifHaveDog, boolean ifHaveCat,
			int pokerLevel2) {
		NBRaise = totalPot * 3;
		if(NBRaise > 20 * blind)
			NBRaise = 20 * blind;
		flushType =  checkFlush(allPoker);
		switch (flushType) {
		case 1:
			if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
				break;
			}
			else if(raiseBet <= minAve)
			{ 
				pw.print("call \n");
				pw.flush();
				break;
			}else
			{ 
				pw.print("fold \n");
				pw.flush();
				break;
			}
			break;
		case 2:
			if(raiseBet != 0 && isFlop){
				pw.print("raise "+ minAve + " \n");
				pw.flush();
				break;
			}
			else if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ (6 * blind +1 ) + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ (6 * blind +1 ) + " \n");
				pw.flush();
				fire = true;
				break;
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}
			else {
				pw.print("raise "+ minAve + " \n");
				pw.flush();
				break;
			}
			break;
		case 3:
			
			if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
				break;
			}
			else if(raiseBet <= minAve)
			{ 
				pw.print("call \n");
				pw.flush();
				break;
			}else
			{ 
				pw.print("fold \n");
				pw.flush();
				break;
			}
			break;
		case 4:
			if(raiseBet != 0 && isFlop){
				pw.print("raise "+ (blind * 3 - 1) + " \n");
				pw.flush();
				break;
			}
			else if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ (6 * blind +1 ) + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ (6 * blind +1 ) + " \n");
				pw.flush();
				fire = true;
				break;
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}
			else {
				pw.print("raise "+ (blind * 3 - 1) + " \n");
				pw.flush();
				break;
			}
			break;
		}
		
		if(flushType == -1){ //not the flush
			straightType = checkStraight(allPoker);
			//System.out.println("straightType isFlop---------->"+straightType);
		
			switch (straightType) {
			case 2:
				if(raiseBet != 0 && isFlop){
					pw.print("raise "+ (blind * 3 - 1) + " \n");
					pw.flush();
					break;
				}
				else if(NotFoldPeopleCount == sbCheckCount && !fire){
					pw.print("raise "+ (6 * blind +1 ) + " \n");
					pw.flush();
					fire = true;
				}
				else if(NotFoldPeopleCount == sbCheckCount && fire){
					pw.print("call \n");
					pw.flush();
				}else if(raiseBet <= minAve && isTurn && !fire)
				{ 
					pw.print("raise "+ (6 * blind +1 ) + " \n");
					pw.flush();
					fire = true;
					break;
				}else
				if(raiseBet == 0){
					pw.print("call \n");
					pw.flush();
					break;
				}
				else {
					pw.print("raise "+ minAve + " \n");
					pw.flush();
					break;
				}
				break;
			case 1:
				if(NotFoldPeopleCount == sbCheckCount && !fire){
					pw.print("raise "+ NBRaise + " \n");
					pw.flush();
					fire = true;
				}
				else if(NotFoldPeopleCount == sbCheckCount && fire){
					pw.print("call \n");
					pw.flush();
				}else
				if(raiseBet == 0){
					pw.print("call \n");
					pw.flush();
					break;
				}else if(raiseBet <= minAve && isTurn && !fire)
				{ 
					pw.print("raise "+ NBRaise + " \n");
					pw.flush();
					fire = true;
					break;
				}
				else if(raiseBet <= minAve)
				{ 
					pw.print("call \n");
					pw.flush();
					break;
				}else
				{ 
					pw.print("fold \n");
					pw.flush();
					break;
				}
				break;
			}
			if(straightType == 0){ // not the straight
				
				pokerLevel = calculatePokerLevel(allPoker);
				//System.out.println("pokerLevel isFlop---------->"+straightType);
				//os.writeUTF("pokerLevel isFlop---------->"+straightType);
				switch (pokerLevel) {
				
				case 15:
				case 13:
				case 12:
				case 10:
				case 4:
					if(raiseBet != 0 && isFlop){
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					else if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					break;
				case 14:
				case 11:
				case 9:
					if(raiseBet != 0 && isFlop){
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else if(raiseBet <=2 * minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					break;
				case 5:
				case 8:
				case 6:
					if(raiseBet != 0 && raiseBet <= 2 * minAve && isFlop){
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else if(raiseBet <=1.5 * minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else if(raiseBet <= 2 * minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				case 7:
				case 3:
					if(raiseBet != 0 && isFlop){
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else if(raiseBet <=1.5 * minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					break;
				case 2:
				case 1:
					if(raiseBet != 0 && raiseBet <= minAve && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
						break;
					}else if(raiseBet <= 2 * minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;

				case -1:
					if(raiseBet != 0 && raiseBet <= minAve && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
						break;
					}
					else if(raiseBet <= minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				default:
					if(NotFoldPeopleCount == sbCheckCount && !fireWN){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fireWN = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fireWN){
						pw.print("call \n");
						pw.flush();
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else if(raiseBet <=2 * blind)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				}
				
			}//straightType over
		}//flushType over
		
	}
		
	









	private static void HitCatFlopTurn(int totalPot, int pokerLevel,
			int flushType, int straightType, LinkedList<Poker> allPoker,
			PrintWriter pw, int flushType2, int NotFoldPeopleCount,
			int sbCheckCount, int myBottom, int raiseBet,
			int NBRaise, int minAve, boolean ifHaveDog, boolean ifHaveCat,
			int pokerLevel2) {

		if(minAve < 3 * blind)
			minAve = 3 * blind;
		flushType =  checkFlush(allPoker);
		switch (flushType) {
		case 1:
			if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");  
				pw.flush();
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
				break;
			}
			else if(raiseBet <= minAve)
			{ 
				pw.print("call \n");
				pw.flush();
				break;
			}else
			{ 
				pw.print("fold \n");
				pw.flush();
				break;
			}
			break;
		case 2:
			if(raiseBet != 0 && isFlop){
				pw.print("raise "+ minAve + " \n");
				pw.flush();
				break;
			}
			else if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ (6 * blind +1 ) + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ (6 * blind +1 ) + " \n");
				pw.flush();
				fire = true;
				break;
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}
			else {
				pw.print("raise "+ minAve + " \n");
				pw.flush();
				break;
			}
			break;
		case 3:
			
			if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
				break;
			}
			else if(raiseBet <= minAve)
			{ 
				pw.print("call \n");
				pw.flush();
				break;
			}else
			{ 
				pw.print("fold \n");
				pw.flush();
				break;
			}
			break;
		case 4:
			if(raiseBet != 0 && isFlop){
				pw.print("raise "+ (blind * 3 - 1) + " \n");
				pw.flush();
				break;
			}
			else if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ (6 * blind +1 ) + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ (6 * blind +1 ) + " \n");
				pw.flush();
				fire = true;
				break;
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}
			else {
				pw.print("raise "+ (blind * 3 - 1) + " \n");
				pw.flush();
				break;
			}
			break;
		}
		
		if(flushType == -1){ //not the flush
			straightType = checkStraight(allPoker);
			//System.out.println("straightType isFlop---------->"+straightType);
		
			switch (straightType) {
			case 2:
				if(raiseBet != 0 && isFlop){
					pw.print("raise "+ (blind * 3 - 1) + " \n");
					pw.flush();
					break;
				}
				else if(NotFoldPeopleCount == sbCheckCount && !fire){
					pw.print("raise "+ (6 * blind +1 ) + " \n");
					pw.flush();
					fire = true;
				}
				else if(NotFoldPeopleCount == sbCheckCount && fire){
					pw.print("call \n");
					pw.flush();
				}else if(raiseBet <= minAve && isTurn && !fire)
				{ 
					pw.print("raise "+ (6 * blind +1 ) + " \n");
					pw.flush();
					fire = true;
					break;
				}else
				if(raiseBet == 0){
					pw.print("call \n");
					pw.flush();
					break;
				}
				else {
					pw.print("raise "+ minAve + " \n");
					pw.flush();
					break;
				}
				break;
			case 1:
				if(NotFoldPeopleCount == sbCheckCount && !fire){
					pw.print("raise "+ NBRaise + " \n");
					pw.flush();
					fire = true;
				}
				else if(NotFoldPeopleCount == sbCheckCount && fire){
					pw.print("call \n");
					pw.flush();
				}else
				if(raiseBet == 0){
					pw.print("call \n");
					pw.flush();
					break;
				}else if(raiseBet <= minAve && isTurn && !fire)
				{ 
					pw.print("raise "+ NBRaise + " \n");
					pw.flush();
					fire = true;
					break;
				}
				else if(raiseBet <= minAve)
				{ 
					pw.print("call \n");
					pw.flush();
					break;
				}else
				{ 
					pw.print("fold \n");
					pw.flush();
					break;
				}
				break;
			}
			if(straightType == 0){ // not the straight
				
				pokerLevel = calculatePokerLevel(allPoker);
				//System.out.println("pokerLevel isFlop---------->"+straightType);
				//os.writeUTF("pokerLevel isFlop---------->"+straightType);
				switch (pokerLevel) {
				
				case 15:
				case 13:
				case 12:
				case 10:
				case 4:
					if(raiseBet != 0 && isFlop){
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					else if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					break;
				case 14:
				case 11:
				case 9:
					if(raiseBet != 0 && isFlop){
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else if(raiseBet <=2 * minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					break;
				case 5:
				case 8:
				case 6:
					if(raiseBet != 0 && raiseBet <= 2 * minAve && isFlop){
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else if(raiseBet <=1.5 * minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else if(raiseBet <= 2 * minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				case 7:
				case 3:
					if(raiseBet != 0 && isFlop){
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else if(raiseBet <=1.5 * minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					break;
				case 2:
					if(raiseBet != 0 && raiseBet <= minAve && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
						break;
					}else if(raiseBet <= 2 * minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;

				case 1:
					if(raiseBet != 0 && raiseBet <= minAve && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
						break;
					}else if(raiseBet <= 1.5 * minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;

				case -1:
					if(raiseBet != 0 && raiseBet <= minAve && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
						break;
					}
					else if(raiseBet <= minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				default:
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}else
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
						break;
					}
					else if(raiseBet <= minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				}
				
			}//straightType over
		}//flushType over
		
	}









	private static void HitDogFlopTurn(int totalPot, int pokerLevel,
			int flushType, int straightType, LinkedList<Poker> allPoker,
			PrintWriter pw, int flushType2, int NotFoldPeopleCount,
			int sbCheckCount, int myBottom, int raiseBet,
			int NBRaise, int minAve, boolean ifHaveDog, boolean ifHaveCat,
			int pokerLevel2) {
		
		flushType =  checkFlush(allPoker);
		switch (flushType) {
		case 1:
			if(raiseBet < blind * 3){
				pw.print("call \n");
				pw.flush();
			}
			else {
				pw.print("fold \n");
				pw.flush();
			}
			break;
		case 2:
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}
			else {
				pw.print("raise "+ (blind * 3 - 1) + " \n");
				pw.flush();
				break;
			}
		case 3:
			if(raiseBet < blind * 3){
				pw.print("call \n");
				pw.flush();
			}
			else {
				pw.print("fold \n");
				pw.flush();
			}
			break;
		case 4:
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}
			else {
				pw.print("raise "+ (blind * 3 - 1) + " \n");
				pw.flush();
				break;
			}
		}
		
		if(flushType == -1){ //not the flush
			straightType = checkStraight(allPoker);
			//System.out.println("straightType isFlop---------->"+straightType);
		
			switch (straightType) {
			case 2:
				if(raiseBet == 0){
					pw.print("call \n");
					pw.flush();
					break;
				}
				else {
					pw.print("raise "+ (blind * 3 - 1) + " \n");
					pw.flush();
					break;
				}
			case 1:
				if(raiseBet < blind + 5){
					pw.print("call \n");
					pw.flush();
				}
				else {
					pw.print("fold \n");
					pw.flush();
				}
				break;
			}
			if(straightType == 0){ // not the straight
				
				pokerLevel = calculatePokerLevel(allPoker);
				//System.out.println("pokerLevel isFlop---------->"+straightType);
				//os.writeUTF("pokerLevel isFlop---------->"+straightType);
				switch (pokerLevel) {
				
				case 15:
				case 13:
				case 12:
				case 10:
				case 4:
				case 14:
				case 11:
				case 9:
				case 8:
				case 6:
				case 7:
				case 3:
				case 5:
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
				case 2:
				case 1:
					if(raiseBet == 0){
						pw.print("call \n");
						pw.flush();
						break;
					}
					else if(myBottom < (blind * 5)){
						pw.print("raise "+ (blind * 3 - 1) + " \n");
						pw.flush();
						break;
					}
					else {
						pw.print("call \n");
						pw.flush();
						break;
					}
				case -1:
					if(raiseBet < blind * 3){
						pw.print("call \n");
						pw.flush();
					}
					else {
						pw.print("fold \n");
						pw.flush();
					}
					break;
				default:
					if(raiseBet <blind * 3){
						pw.print("call \n");
						pw.flush();
					}
					else {
						pw.print("fold \n");
						pw.flush();
					}
					break;
				}
				
			}//straightType over
		}//flushType over
		
	}




	private static void HitMouseFlopTurn(int totalPot, int flushType, int flushType2, int straightType, LinkedList<Poker> allPoker, 
			PrintWriter pw, int NotFoldPeopleCount,
			int sbCheckCount, int myBottom, int myBottom2, int raiseBet,
			int NBRaise, int minAve, boolean ifHaveDog, boolean ifHaveCat, int pokerLevel) {
		flushType =  checkFlush(allPoker);

		if(minAve < 3 * blind)
			minAve = 3 * blind;
		switch (flushType) {
		
		case 1:
			if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
				break;
			}
			else if(raiseBet <= minAve)
			{ 
				pw.print("call \n");
				pw.flush();
				break;
			}else
			{ 
				pw.print("fold \n");
				pw.flush();
				break;
			}
			break;
		case 2:
			pw.print("raise "+ minAve + " \n");
		 	pw.flush();
			break;
		case 3:
			if(NotFoldPeopleCount == sbCheckCount && !fire){
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
			}
			else if(NotFoldPeopleCount == sbCheckCount && fire){
				pw.print("call \n");
				pw.flush();
			}else
			if(raiseBet == 0){
				pw.print("call \n");
				pw.flush();
				break;
			}else if(raiseBet <= minAve && isTurn && !fire)
			{ 
				pw.print("raise "+ NBRaise + " \n");
				pw.flush();
				fire = true;
				break;
			}
			else if(raiseBet <= minAve)
			{ 
				pw.print("call \n");
				pw.flush();
				break;
			}else
			{ 
				pw.print("fold \n");
				pw.flush();
				break;
			}
			break;
		case 4:
			pw.print("raise "+ minAve + " \n");
			pw.flush();
			break;	
		}
		if(flushType == -1){ //not the flush
			straightType = checkStraight(allPoker);
			//System.out.println("straightType isFlop---------->"+straightType);
		
			switch (straightType) {
			case 2:
				if(totalPot <= blind * 50)
				{
					pw.print("raise "+ (blind * 4 - 1)+ " \n");
					pw.flush();
					break;
				}
				else if(totalPot > blind * 50)
				{
					pw.print("call \n");
					pw.flush();
					break;
				}
			case 1:
				if(NotFoldPeopleCount == sbCheckCount && !fire){
					pw.print("raise "+ NBRaise + " \n");
					pw.flush();
					fire = true;
				}
				else if(NotFoldPeopleCount == sbCheckCount && fire){
					pw.print("call \n");
					pw.flush();
				}else
				if(raiseBet == 0){
					pw.print("call \n");
					pw.flush();
					break;
				}else if(raiseBet <= minAve && isTurn && !fire)
				{ 
					pw.print("raise "+ NBRaise + " \n");
					pw.flush();
					fire = true;
					break;
				}
				else if(raiseBet <= minAve)
				{ 
					pw.print("call \n");
					pw.flush();
					break;
				}else
				{ 
					pw.print("fold \n");
					pw.flush();
					break;
				}
				break;
			}
			if(straightType == 0){ // not the straight
				
				pokerLevel = calculatePokerLevel(allPoker);
				//System.out.println("pokerLevel isFlop---------->"+straightType);
				//os.writeUTF("pokerLevel isFlop---------->"+straightType);
				switch (pokerLevel) {
				
				case 15:
				case 13:
				case 12:
				case 10:
				case 4:
					if(totalPot <= blind * 50)
					{
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}
					else if(totalPot > blind * 50)
					{
						pw.print("call \n");
						pw.flush();
						break;
					}
				case 14:
				case 11:
				case 9:
					if( raiseBet <= 1.5 * minAve && myBottom <=  (blind * 40))
					{
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else 
					{
						pw.print("call \n");
						pw.flush();
						break;
					}
				case 8:
				case 6:
					if( raiseBet <=  1.5 * minAve && myBottom <=  (blind * 30))
					{ 
						pw.print("raise " + minAve +" \n");
						pw.flush();
						break;
					}else if( raiseBet <=  2 * minAve && myBottom > (blind * 30))
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("fold \n");
						pw.flush();
					}
					break;
				case 5:
					if( raiseBet <=  minAve && myBottom <=  (blind * 10))
					{ 
						pw.print("raise " + minAve +" \n");
						pw.flush();
						break;
					}else if( raiseBet <=  1.5 * minAve && myBottom > (blind * 10))
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else {
						pw.print("fold \n");
						pw.flush();
					}
					break;
				case 7:
				case 3:
					if( raiseBet <= 2 * minAve && myBottom < (blind * 30))
					{
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}
					else
					{
						pw.print("call \n");
						pw.flush();
						break;
					}
				case 2:
					if(raiseBet == 0 && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}
					else if(raiseBet <= minAve && isTurn)
					{ 
						pw.print("raise "+ (blind * 5 - 1) + " \n");
						pw.flush();
						break;
					}else if(raiseBet <= 2 * minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;

				case 1:
					if(raiseBet == 0 && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}
					else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
						break;
					}else if(raiseBet <= 1.5 * minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				case -1:
					if(raiseBet == 0 && isFlop){
						pw.print("raise "+ (blind * 4 - 1)+ " \n");
						pw.flush();
						break;
					}
					else 
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}
					else if(raiseBet <= minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
						break;
					}else if(raiseBet <=  minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				default:
					if(NotFoldPeopleCount == sbCheckCount && !fire){
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
					}
					else if(NotFoldPeopleCount == sbCheckCount && fire){
						pw.print("call \n");
						pw.flush();
					}
					else if(raiseBet < minAve && isTurn && !fire)
					{ 
						pw.print("raise "+ NBRaise + " \n");
						pw.flush();
						fire = true;
						break;
					}else if(raiseBet <= minAve)
					{ 
						pw.print("call \n");
						pw.flush();
						break;
					}
					else
					{ 
						pw.print("fold \n");
						pw.flush();
						break;
					}
					break;
				}
				
			}//straightType over
		}//flushType over
		
	}


	private static void checkMyMoney(String myId, String currentId,
			String jetton, String money) {
		if(myId.equals(currentId)){
			myBet = pareseToInt(jetton)+pareseToInt(money);
		}
	}



	/**
	 * 
	 * @param fiveCards
	 * @return FOUR_OF_A_KIND 18  ;<br>FULL_HOUSE 17 ; <br>THREE_OF_A_KIND 16;<br> TWO PAIR 15;<br> ONE PAIR 14;<br>HIGH_CARD 13<br>
	 */
	private static int calculatePokerLevel(LinkedList<Poker> cards) {
		
		int card1 = 0;
		int card2 = 0;
		card1 = cards.get(0).getNum();
		card2 = cards.get(1).getNum();
		
		boolean ifCardEquals = false;
		if(card1 == card2)
			ifCardEquals = true;
		
		if(card1 < card2) {
			int temp = card1;
			card1 = card2;
			card2 = temp;
		}
		int[] isMax = new int[8];
		int[] pairCount = new int[4];
		int pairMax = 0;
		int[] cardExceptHold  = new int[15];
		int n = 0;
		if(isFlop)
			n = 5;
		else if(isTurn)
			n = 6;
		else if(isRiver)
			n = 7;
		for(int i = 2; i < n; i++)
		{
			if(cards.get(i)!= null)
				cardExceptHold[cards.get(i).getNum()]++;
		}
		for(int i = 14,count = 0; i > 0; i--){
			if(cardExceptHold[i] > 0)
				isMax[count++] = i;
			if(cardExceptHold[i] == 4)
				{
					pairCount[3]++;
				}
			if(cardExceptHold[i] == 3)
				{
					pairCount[2]++;
					if(pairMax == 0)
						pairMax = i;
				}
			if(cardExceptHold[i] == 2)
				{
					pairCount[1]++;
					if(pairMax == 0)
						pairMax = i;
				}
		}
		//System.out.println("isMax" + isMax[0]);
		if(pairCount[3] == 1)
			{
			if(card1 >= isMax[0])
				return 15; //all in
			else 
				return 14; //fold
		}
		if(pairCount[2] == 1)
		{
		if(cardExceptHold[card1] == 3 || cardExceptHold[card1] == 3 )
			return 13;  //all in
		else if(ifCardEquals && cardExceptHold[card1] == 2)
			return 15;
		else if(cardExceptHold[pairMax] == 3 
				&& (cardExceptHold[card1] == 1 ||cardExceptHold[card2] == 1) 
					&& (card1 == isMax[1]||card2 == isMax[1]))
			return 12; //raise 1000
		else if(cardExceptHold[pairMax] == 3 
				&& (cardExceptHold[card1] == 1 ||cardExceptHold[card2] == 1) 
					&& card1 != isMax[1] 
							&& card2 != isMax[1])
			return 11; //call
		else if(cardExceptHold[pairMax] != 3 
				&& (cardExceptHold[card1] == 1 ||cardExceptHold[card2] == 1) 
					&& (card1 != isMax[0]||card2 != isMax[0]))
			return 12; //raise 1000
		else if(cardExceptHold[pairMax] != 3 
				&& (cardExceptHold[card1] == 1 ||cardExceptHold[card2] == 1) 
					&& card1 != isMax[0] 
							&& card2 != isMax[0])
			return 11; //call
		}
		if(pairCount[1] == 2)
		{
			if(card1 == pairMax || card2 == pairMax)//22top pair hulu
				return 10; //all in
			else if(cardExceptHold[card1] == 2 && cardExceptHold[card2] == 2)
				return 9;
			else if(card1 == isMax[0]
					||card2 == isMax[0])
				return 8; //check or call
			else if(ifCardEquals && card1 >= isMax[0]
					)
				return 8; //check or call
		}
		if(pairCount[1] == 1)
		{
			if(ifCardEquals && cardExceptHold[card1] == 2)
				return 15;
			else if(ifCardEquals && cardExceptHold[card1] == 1)
				return 15;
			else if(cardExceptHold[card1] == 2 || cardExceptHold[card2] == 2)
				return 7; //san tiao		
			else if((card1 == isMax[0] || card2 == isMax[0]) 
					&& (cardExceptHold[card1] == 1 || cardExceptHold[card2] == 1))
				return 6; //two pair with top
			else if(ifCardEquals && card1 == isMax[0])
				return 6; //two pair with top
			else if(ifCardEquals && card1 >= 10)
				return 5; //two pair with top
			else if((card1 == isMax[0] || card2 == isMax[0])  
					&& ((cardExceptHold[card1] == 1 && card1 >= 10 )||(cardExceptHold[card2] == 1 && card2 >= 10 )))
				return 5; //two pair without top
			
		}
		if(pairCount[1] == 0)
		{
			if(ifCardEquals && cardExceptHold[card1] == 1)
				return 4;//san tiao
			else if(cardExceptHold[card1] == 1 && cardExceptHold[card2] == 1)
				return 3 ;//two pair
			else if (card1 == isMax[0] || card2 == isMax[0])
				return 2 ;
			else if (ifCardEquals && card1 >= isMax[0])
				return 2 ;
			else if ((cardExceptHold[card1] == 1 && card1 > 10)
					|| (cardExceptHold[card2] == 1 && card2 > 10)
					 )
				return 1 ;
			else if (ifCardEquals && card1 > 10)
				return 1 ;
			else if ((cardExceptHold[card1] == 1)
					|| (cardExceptHold[card2] == 1)
					 )
				return -1 ;
			else if (ifCardEquals)
				return -1 ;
		}
		 return 0;
	}

	private static int checkStraight(LinkedList<Poker> allPoker) {
		    int card[] = new int[15];
		    int maxStr = 0;
		    int n = 0;
			if(isFlop)
				n = 5;
			else if(isTurn)
				n = 6;
			else if(isRiver)
				n = 7;
		    for (int i = 0; i < n; i++) {
		    	card[allPoker.get(i).getNum()] ++;
			}
		    if(card[14]!= 0)
		    	card[1] = card[14];
		    for(int i = 1; i <= 14; i++)  
			{
				int currentStr = 0;
				for(int j = i; j <= 14; )
				{
					if(card[j] >= 1)
					{
						j++;
						currentStr++;
						if(maxStr < currentStr)
							maxStr = currentStr;
					}
					else 
						break;
						
				}
			}
		    if (maxStr >= 5){
		         return 2;
		    }else if (maxStr == 4)
		         return 1;
		      else
		    	return 0;
			
	}


	/**
	 * 
	 * @param allPoker
	 * @return 1 is flush ; 0 is almost flush such as similar four color ; -1 no
	 *         flush must fold
	 */
	private static int checkFlush(LinkedList<Poker> cards) {
		Map<Integer, String> colorMap=new HashMap<Integer, String>();
		colorMap.put(0, "SPADES");
		colorMap.put(1, "HEARTS");
		colorMap.put(2, "CLUBS");
		colorMap.put(3, "DIAMONDS");
		String holdColor0 = cards.get(0).getColor();
		String holdColor1 = cards.get(1).getColor();
		int[] color = new int[4];
		int n = 0;
		if(isFlop)
			n = 5;
		else if(isTurn)
			n = 6;
		else if(isRiver)
			n = 7;
		
		for(int i = 0; i < n; i++)
		{
			if(cards.get(i)!= null)
				insertColor(cards.get(i).getColor(), color);
		}
		
		if(holdColor0==holdColor1){
			for(int i=0;i<4;i++){
				if(color[i]>=5 && holdColor0.contains(colorMap.get(i))){
					return 2;   //five same color with two  holdcolor
			}
		  }
		}
		if(holdColor0!=holdColor1){
			for(int i=0;i<4;i++){
				if(color[i]>=5 && (holdColor0.contains(colorMap.get(i)) || holdColor0.contains(colorMap.get(i)))){
					return 4;   //five same color with one holdcolor
			}
		  }
		}
		if(holdColor0==holdColor1){
			for(int i=0;i<4;i++){
				if(color[i]==4 && holdColor0.contains(colorMap.get(i))){
					return 1;      //four same color with two  holdcolor
				}
			}
		}
		if(holdColor0!=holdColor1){
			for(int i=0;i<4;i++){
				if(color[i]==4 &&(holdColor0.contains(colorMap.get(i)) || holdColor0.contains(colorMap.get(i)))){
					return 3;      //four same color with one holdcolor
				}
			}
		}
	 return -1;
	 }

	private static int checkAandMaxCard(int num, int num2) {
		if (num == 14) {
			if (num2 > 11)
				return 1; //max Card
			else if(num2 <= 5)
				return 2;
			else 
				return 0;//min Card
		}
		if (num2 == 14) {
			if (num > 11)
				return 1;
			else if(num <= 5) 
				return 2;
			else
				return 0;
		}
		return -1;// no A
	}

	private static boolean checkPocketPair(int num, int num2) {
		return num == num2;
	}


	private static void initPockerList(LinkedList<Poker> allPoker) {
		allPoker.clear();
		for (int i = 0; i <7; i++) {
			allPoker.add(new Poker());
		}	
	}



	/**
	 * add poker to list
	 * @param color  huase
	 * @param point  dianshu 
	 * @return Poker
	 */
	private static Poker addPoker(String color , String point) {
		Poker poker= new Poker();
		poker.setColor(color);
		poker.setNum(covertPoint(point));
		return poker;
	}

	/**
	 * covert the string point to int 
	 * @param point String Point
	 * @return Int Point
	 */
	private static int covertPoint(String point) {
		for(int i=0;i<13;i++){
			if(point.equals("A"))
				return 14;
			else 
			  if(POINTS_STRING[i].equals(point))
				return POINTS_INT[i];
		}
		return -1;
	}

	
	private static void checkTheBlind(String[] args, String[] s, int flag1,
			int flag2) {
		//System.out.println(" s[flag1]="+s[flag1]);
		if (s[flag1].contains(args[4])) {
			// is small blind
			//System.out.println(" s[flag2]="+s[flag2]);
			if (s[flag2].contains("small")) {
				isSmallBlind = true;
			} else if (s[flag2].contains("big")) { // is big blind
				isBigBlind = true;
			}
		}
	}
	
	
	private static void checkThePerFectSeat(String[] args, String[] s, int flag) {
		if (s[flag].contains(args[4])) {
			isPerfeckSeat = true;
		}
	}
	
	private static void insertColor(String string, int[] color) {
		if(string.contains("SPADES"))
			color[0]++;
		else if(string.contains("HEARTS"))
			color[1]++;
		else if(string.contains("CLUBS"))
			color[2]++;
		else if(string.contains("DIAMONDS"))
			color[3]++;
		
	}
	private static int pareseToInt(String s){
		return Integer.parseInt(s);
	}
	
	static class Poker implements Comparable<Poker>
	{
	    private String color; 
	    private int num; 
	 
	   
	    public Poker() {
		}
	 
	    public int getNum()
	    {
	        return num;
	    }
	 
	    public void setNum(int num)
	    {
	        this.num = num;
	    }

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}
		
		
		public int compareTo(Poker p) {
			if(this.num< p.getNum()){
				return 1;
			}else if(this.num > p.getNum()){
				return -1;
			}else{
				return 0;
			}
		}
	} 
	
	private static void checkAllPlayersCL(Map<Integer, Integer> RaiseCount, Map<Integer,Double> AveRaise, Map<Integer, Double> lastBet, Map<Integer, FIFO<A>> otherRaise,String[] s, double raise) {
		FileWriter out = null;
		try{
		out = new FileWriter(file, true);
		}
		catch (IOException e){
			e.printStackTrace();
		}
		if(s == null){
			for(int key : lastBet.keySet())
				lastBet.put(key, 0.0);
			}
		else if(s.length==5){
			FIFO<A> tempFIFO =null;
			if(otherRaise.get(pareseToInt(s[0]))==null){
				FIFO<A> fifo = new FIFOImpl<A>(10);
				otherRaise.put(pareseToInt(s[0]),fifo);
				lastBet.put(pareseToInt(s[0]),0.0);
				AveRaise.put(pareseToInt(s[0]),0.0);
				RaiseCount.put(pareseToInt(s[0]), 0);
			}
			int tempCount = RaiseCount.get(pareseToInt(s[0]));
			double tempAve = AveRaise.get(pareseToInt(s[0]));
			tempFIFO = otherRaise.get(pareseToInt(s[0]));
			double tempLastBet =lastBet.get(pareseToInt(s[0]));
			if(s[4].contains("raise") || s[4].contains("all_in")){
					if(s[4].contains("all_in") && pareseToInt(s[3]) > blind * 15)
						raise = 0;
					if(raise > 0 && pareseToInt(s[3]) != tempLastBet)
					{
						if(raise > pareseToInt(s[3]) - tempLastBet && pareseToInt(s[3]) - tempLastBet > 0)
							{
								raise = pareseToInt(s[3]) - tempLastBet;
							}
					//	System.out.println("raise+" + raise);
						A a = new A(raise);
						A head = tempFIFO.addLastSafe(a);
						int size = tempFIFO.size();
						double ave = 0.0;
						if(size != 10 || head == null){
							ave =((double)((int)(((tempAve * (size - 1) + raise)/size) *100)/1))/100;
						//	System.out.println(pareseToInt(s[0])+"\traiseBet:" + (int)raise + "\thead.size:" +tempFIFO.size() +"\tAve:" + (int)ave);
							if(pareseToInt(s[0].trim())== 3333){
								System.out.println(pareseToInt(s[0].trim()) + "\traiseBet:" + (int)raise + "\thead.size:" +tempFIFO.size() +"\tAve:" + (int)ave);
								try{
								out.write(pareseToInt(s[0])+"\traiseBet:" + raise + "\thead.size:" +tempFIFO.size() +"\tAve:" + ave + "\n");
								out.close();
								}catch (IOException e){
									e.printStackTrace();
								}
							}
						}
						else {
							ave = ((double)((int)(((tempAve * size + raise - head.getRaiseBet())/size)*100)/1))/100 ;
							System.out.println(pareseToInt(s[0]) +"\traiseBet:" + (int)raise + (int)head.getRaiseBet() + "\tAve " + (int)ave);
							if(pareseToInt(s[0].trim())== 3333){
								System.out.println(pareseToInt(s[0].trim()) + head.getRaiseBet() + "\tAve " + ave);
								try{
								out.write(pareseToInt(s[0])+"\traiseBet:" + raise +"\thead.getRaiseBet()"+head.getRaiseBet()+"\tAve:" + ave + "\n");
								out.close();
								}catch (IOException e){
									e.printStackTrace();
								}
							}
						
						}
					tempCount ++;
					RaiseCount.put(pareseToInt(s[0]), tempCount);
					if(pareseToInt(s[0].trim())== 3333){
						System.out.println("raiseCount " + tempCount); 
					}
		            lastBet.put(pareseToInt(s[0]),(double)pareseToInt(s[3]));
		            AveRaise.put(pareseToInt(s[0]),ave);
					}
			}
			else if(s[4].contains("call") || s[4].contains("check")
						||s[4].contains("blind")){
	            lastBet.put(pareseToInt(s[0]),(double)pareseToInt(s[3]));
			}
			
		}
	}

	

	interface FIFO<T> extends List<T>, Deque<T>, Cloneable, java.io.Serializable {

	    T addLastSafe(T addLast);

	    T pollSafe();

	    int getMaxSize();

	   
	    List<T> setMaxSize(int maxSize);

	}

	public static class FIFOImpl<T> extends LinkedList<T> implements FIFO<T> {

	    private int maxSize = Integer.MAX_VALUE;
	    private final Object synObj = new Object();

	    public FIFOImpl() {
	        super();
	    }

	    public FIFOImpl(int maxSize) {
	        super();
	        this.maxSize = maxSize;
	    }

	    @Override
	    public T addLastSafe(T addLast) {
	        synchronized (synObj) {
	            T head = null;
	            while (size() >= maxSize) {
	                head = poll();
	            }
	            addLast(addLast);
	            return head;
	        }
	    }

	    @Override
	    public T pollSafe() {
	        synchronized (synObj) {
	            return poll();
	        }
	    }

	    @Override
	    public List<T> setMaxSize(int maxSize) {
	        List<T> list = null;
	        if (maxSize < this.maxSize) {
	            list = new ArrayList<T>();
	            synchronized (synObj) {
	                while (size() > maxSize) {
	                    list.add(poll());
	                }
	            }
	        }
	        this.maxSize = maxSize;
	        return list;
	    }

	    @Override
	    public int getMaxSize() {
	        return this.maxSize;
	    }
	}

	public static class A {

	    private double raiseBet;

	    public A() {
	    }

	    public A(double raiseBet) {
	        this.raiseBet = raiseBet;
	    }

	    public double getRaiseBet() {
			return raiseBet;
		}

		public void setRaiseBet(int raiseBet) {
			this.raiseBet = raiseBet;
		}

	}
	/**
	 * check we should play the game
	 * @param currentPlayerCount 
	 * @param avgTotalBet  (8 people 17000) & (2 people 4100)
	 * @return
	 */
	private static boolean NeedToPlay(int currentPlayerCount,int avgTotalBet) {
		
		int finallyBet = (600-playCount)/2*blind/2+avgTotalBet;
		
		switch (currentPlayerCount) {
		case 8:
		case 7:
		case 6:
		case 5:
		case 4:
		case 3:
		case 2:
			if(myBet >finallyBet )
				return false;
			else 
				return true;
		default:
			return true;
		}
	}

}
