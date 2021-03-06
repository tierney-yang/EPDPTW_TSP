import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.Random;



/*
 * 问题，全局最优解为含历史最优解。
 * 标准算法
 */
public class pso2 {
	
	private int clent_num; //客户个数
	private int grain_num; //粒子个数
	private int car_num; //车辆个数
	private double [][] grain_p; //粒子的位置向量
	private double [][] grain_v; //粒子的速度向量
	private int gbest_g; //当前全局最优解粒子
	private double[] gbest_s; //当前全局最优解粒子位置
	private double[] evaluate_array; //粒子评价值数组
	private double[] a; //粒子评价值数组
	private double[] b; //粒子评价值数组
	
	private int[][] clientCostValue;//客户间距离
	private int[][] HomeCostValue;//客户到充电点距离
	private int[] clientBasicValue;//客户起点到终点距离
	
	private Random random ;
	private int x = 25, y = 25;//充电桩
	
	private int basic_distance;
	
	private double c1 = 2,c2 = 2,w = 0.7;
	

	
	//随机生成初始粒子位置向量和粒子的速度向量
	public void init(int car_num, int grain_num, int clent_num){
		this.clent_num = clent_num;
		this.car_num = car_num;
		this.grain_num =grain_num;
		this.grain_p = new double [grain_num][clent_num];
		this.grain_v = new double [grain_num][clent_num];
		for (int i = 0; i < grain_num; i++) {
			
			for (int j = 0; j < clent_num; j++) {
				random = new Random();
				this.grain_p[i][j] = random.nextDouble() * car_num;
				//System.out.print(Math.floor(this.grain_p[i][j]) +"\t");
				random = new Random();
				this.grain_v[i][j] =(car_num - random.nextDouble() * car_num *2) * 0.1;
				
			} 
			//System.out.println(i);
		}
	}
	
	public double getSatisfaction (double a,double b,int cometime){
		double satisfaction = 0;
		if(cometime >= a-80 && cometime< a){
			satisfaction = Math.sqrt((cometime - a + 80)/80);
		}else if(cometime >= a && cometime <= b){
			satisfaction = 1;
		}else if (cometime > b && cometime <= b+80) {
			satisfaction = Math.sqrt((b+80 - cometime)/80);
		}else {
			satisfaction = 0;
		}
		return satisfaction;
	}

	/*
	 * int[] clientBasicValue 客户上车点和下车点距离
	 * int[][] clientCostValue 客户间距离
	 * double[][] grain_p 粒子位子矩阵
	 */

	private void getEvaluateList(int[][] clientCostValue,int[] clientBasicValue, int[][] HomeCostValue, double[][] grain_p, double[] a, double[] b) {
	evaluate_array = new double[grain_num];
	for (int i = 0; i < grain_num; i++){
		double[] grainPosition_i = new double[clent_num];
		
		//向下取整
		for (int j = 0; j < clent_num; j++){
			if(grain_p[i][j] < 0){
				grainPosition_i[j] = 0;
			}else if(grain_p[i][j] > car_num){
				grainPosition_i[j] = car_num -1;
			}else {
				grainPosition_i[j] = Math.floor(grain_p[i][j]);
			}
			
			
		}
		
		//求解粒子i的评价值
		evaluate_array[i] = 0;
		int evaluatetotal = 0;
		double satisfactiontotal = 0;
		for (int j = 0; j < car_num; j++) {
			int evaluate = 0;
			double satisfaction = 0;
			List<Integer> carClentList = new ArrayList<>();
			for (int j2 = 0; j2 < grainPosition_i.length; j2++) {
				if(grainPosition_i[j2] == j){
					carClentList.add(j2);//将客户分配给车辆
				}
			}
			if (carClentList.size() != 0) {
				evaluate = evaluate + HomeCostValue[0][carClentList.get(0)] + clientBasicValue[carClentList.get(0)];
				satisfaction = satisfaction + getSatisfaction(a[carClentList.get(0)], b[carClentList.get(0)], evaluate-clientBasicValue[carClentList.get(0)]);
			}
			if (carClentList.size() > 1) {
				for (int j3 = 0 ; j3 < carClentList.size()-1; j3++) {
					if((evaluate + clientBasicValue[carClentList.get(j3+1)] + clientCostValue[carClentList.get(j3)][carClentList.get(j3+1)]+HomeCostValue[1][carClentList.get(j3+1)]) > 300){
						evaluate = evaluate + HomeCostValue[1][carClentList.get(j3)] + HomeCostValue[0][carClentList.get(j3+1)] + clientBasicValue[carClentList.get(j3+1)];
						satisfaction = satisfaction + getSatisfaction(a[j3+1], b[j3+1], evaluate-clientBasicValue[carClentList.get(j3+1)]);
					}else {
						evaluate = evaluate + clientCostValue[carClentList.get(j3)][carClentList.get(j3+1)] + clientBasicValue[carClentList.get(j3+1)];
						satisfaction = satisfaction + getSatisfaction(a[j3+1], b[j3+1], evaluate-clientBasicValue[carClentList.get(j3+1)]);
					}
				}
			}
			if (carClentList.size() != 0) {
				evaluate = evaluate + HomeCostValue[1][carClentList.get(carClentList.size()-1)];
				
			}
			
			evaluatetotal = evaluatetotal + evaluate;
			satisfactiontotal = satisfactiontotal + satisfaction;	
		}
		//System.out.println(i+ "\t" + satisfactiontotal/clent_num);
		evaluate_array[i] = 0.3 * (evaluatetotal -basic_distance) + 0.7 * 5500 * (1-satisfactiontotal/clent_num);
	}
}
	//获取无效距离
	private void getUnseDistance(int[][] clientCostValue,int[] clientBasicValue, int[][] HomeCostValue, double[] Gbest_a, double[] a, double[] b) {
	
		double[] grainPosition_i = new double[clent_num];
		
		//向下取整
		for (int j = 0; j < clent_num; j++){
			if(Gbest_a[j] < 0){
				grainPosition_i[j] = 0;
			}else if(Gbest_a[j] > car_num){
				grainPosition_i[j] = car_num -1;
			}else {
				grainPosition_i[j] = Math.floor(Gbest_a[j]);
			}
			
			
		}
		
		//求解粒子i的评价值
		int evaluatetotal = 0;
		double satisfactiontotal = 0;
		for (int j = 0; j < car_num; j++) {
			int evaluate = 0;
			double satisfaction = 0;
			List<Integer> carClentList = new ArrayList<>();
			for (int j2 = 0; j2 < grainPosition_i.length; j2++) {
				if(grainPosition_i[j2] == j){
					carClentList.add(j2);//将客户分配给车辆
				}
			}
			if (carClentList.size() != 0) {
				evaluate = evaluate + HomeCostValue[0][carClentList.get(0)] + clientBasicValue[carClentList.get(0)];
				satisfaction = satisfaction + getSatisfaction(a[carClentList.get(0)], b[carClentList.get(0)], evaluate-clientBasicValue[carClentList.get(0)]);
			}
			if (carClentList.size() > 1) {
				for (int j3 = 0 ; j3 < carClentList.size()-1; j3++) {
					if((evaluate + clientBasicValue[carClentList.get(j3+1)] + clientCostValue[carClentList.get(j3)][carClentList.get(j3+1)]+HomeCostValue[1][carClentList.get(j3+1)]) > 300){
						evaluate = evaluate + HomeCostValue[1][carClentList.get(j3)] + HomeCostValue[0][carClentList.get(j3+1)] + clientBasicValue[carClentList.get(j3+1)];
						satisfaction = satisfaction + getSatisfaction(a[j3+1], b[j3+1], evaluate-clientBasicValue[carClentList.get(j3+1)]);
					}else {
						evaluate = evaluate + clientCostValue[carClentList.get(j3)][carClentList.get(j3+1)] + clientBasicValue[carClentList.get(j3+1)];
						satisfaction = satisfaction + getSatisfaction(a[j3+1], b[j3+1], evaluate-clientBasicValue[carClentList.get(j3+1)]);
					}
				}
			}
			if (carClentList.size() != 0) {
				evaluate = evaluate + HomeCostValue[1][carClentList.get(carClentList.size()-1)];
				
			}
			
			evaluatetotal = evaluatetotal + evaluate;
			
			satisfactiontotal = satisfactiontotal + satisfaction;	
		}
		System.out.println("无效距离："+(evaluatetotal-4096));
		System.out.println("满意度："+satisfactiontotal/200);
}
	/*获取时间t时的粒子速度、粒子位置
	 * history_p 粒子最优解
	 * gbest_s 全局最优解
	 * gbest_s 邻域最优解
	 */
	public void getStatusT(double[][] grain_v, double[][] grain_p, double[] gbest_s ,double[][] history_p) {
		double[][] grain_vTemp = grain_v.clone();
		double[][] grain_pTemp = grain_p.clone();
		
		for (int i = 0; i < grain_vTemp.length; i++) {
			for (int j = 0; j < grain_vTemp[i].length; j++) {
				random = new Random();
				double r1 = random.nextDouble();
				double r2 = random.nextDouble();
				double mother = c1 * r1 + c2 * r2;
				grain_v[i][j] = w * grain_vTemp[i][j] + (c1 * r1 *(1-w) /mother) *(history_p[i][j]-grain_pTemp[i][j])
						+  (c2 * r2 *(1-w) /mother)* (gbest_s[j]-grain_pTemp[i][j]);
//				if(grain_v[i][j] <(0-car_num)){
//					grain_v[i][j] = -car_num;
//				}else if (grain_v[i][j] >car_num) {
//					grain_v[i][j] = car_num;
//				}
				
				grain_p[i][j] = grain_pTemp[i][j] + grain_v[i][j];
				
				//System.out.print(grain_v[i][j]+"\t");
			}
			//System.out.println();
		}
		
	}

	
	/*获取当前最优粒子
	 *evaluate_array : 粒子的评价值
	 *int[][] grain_p : 粒子位子矩阵
	 */
	public void getGbest_s(double[] evaluate_array, double [][] grain_p) {
		int best_g = 0;
		double temp = evaluate_array[0];
		for (int i = 1; i < evaluate_array.length; i++) {
			if(evaluate_array[i] <= temp) {
				best_g = i;
				temp = evaluate_array[i];
			}
		}
		gbest_g = best_g;
		gbest_s = grain_p[best_g].clone();
	}
	  
	public void clentInit() throws IOException {
		clentint clentinit = new clentint(clent_num);
		int[][] clientstart = clentinit.getClientStart("src/data.txt", 1, 2);
		int[][] clientdone = clentinit.getClientStart("src/data.txt", 3, 4);
		a = clentinit.getTime("src/data2.txt", 1);
		b = clentinit.getTime("src/data2.txt", 2);
		System.out.println("clent_num="+clent_num);
		clientCostValue = clentinit.getClientCostValue(clientstart, clientdone);
		HomeCostValue = clentinit.getHomeCostValue(clientstart, clientdone, x, y);
		clientBasicValue = clentinit.getClientBasicValue(clientstart, clientdone);
		basic_distance = 0;
		for (int i = 0; i < clientBasicValue.length; i++) {
			basic_distance = basic_distance + clientBasicValue[i];
		}
		System.out.println("有效距离： "+basic_distance);
	}
	public void solve() throws IOException {
		clentInit();
		double Gbest = 99999;
		double[] Gbest_a = new double[clent_num]; 
		double[][] history_p = null;
		
		double[] history_e = null;//粒子历史最优解
		
		for(int t = 0; t < 200; t++) {
			getEvaluateList(clientCostValue,clientBasicValue, HomeCostValue, grain_p, a, b);
			

			if(history_e == null || history_e.length == 0){
				history_e = evaluate_array.clone();
				history_p = grain_p.clone();
			}else {
				for (int i = 0; i < history_e.length; i++) {
					if(evaluate_array[i] <= history_e[i]) {
						history_e[i] = evaluate_array[i];//粒子历史最优解评价值
						history_p[i] = grain_p[i].clone();//粒子历史最优解的位置向量
					}
				}
			}
			
			getGbest_s(evaluate_array, grain_p);//获取当前全局最优解
			
			if(evaluate_array[gbest_g] <= Gbest) { //获取历史全局最优解
				Gbest = evaluate_array[gbest_g];
				Gbest_a = gbest_s.clone();
			}
			
//			System.out.print("t="+t+"时最优解评价值为"+Gbest+"粒子位子");
//			for(int i = 0; i < Gbest_a.length; i++){
//				System.out.print(Gbest_a[i]+"\t");
//			}
//			System.out.println();
			
		
	
			//System.out.print("t="+t+"时最优解评价值为"+Gbest+"粒子位子");
			System.out.print(Gbest+",");
//			System.out.println(gbest_g+"\t" + evaluate_array[gbest_g]);
//			for(int i = 0; i < evaluate_array.length; i++){
//				System.out.print(evaluate_array[i]+"\t");
//			}
			
			getStatusT(grain_v, grain_p, Gbest_a, history_p);
		}
		
		System.out.println();
		for(int c =0 ;c<car_num;c++){
			System.out.print(c+":\t");
		for(int i = 0; i < Gbest_a.length; i++){
			if(Gbest_a[i]<0&& c==0 ){
				System.out.print(i+"\t");
			}else if(Gbest_a[i]>car_num & c == car_num-1){
				System.out.print(i+"\t");
			}else if(Math.floor(Gbest_a[i]) == c){
				System.out.print(i+"\t");
			}
			
		}
		System.out.println();
		}
		
		getUnseDistance(clientCostValue,clientBasicValue, HomeCostValue, Gbest_a, a, b);
	}
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Start....");
		pso2 pso = new pso2();
//		for(int t=0 ; t < 9 ; t++){
//			Map<Integer, List<Integer>> clent_p = pso.getClent_p(pso.getClent_array(9), t);
//		}
		pso.init(25, 100, 200);
		pso.solve();
	}

}

