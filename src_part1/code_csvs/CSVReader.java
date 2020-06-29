import java.io.*;
import java.util.*;

public class CSVReader {
	public static class Triplet{
		double x;
		double y;
		int z=0;
		void setx(double a){x=a;}
		void sety(double a){y=a;}
		void setz(int a){z=a;}
		double getx(){return x;}
		double gety(){return y;}
		int getz(){return z;}
	}
	public static class Quad{
		double x;
		double y;
		int z=0;
		String w;
		void setx(double a){x=a;}
		void sety(double a){y=a;}
		void setz(int a){z=a;}
		void setw(String a){w=a;}
		double getx(){return x;}
		double gety(){return y;}
		int getz(){return z;}
		String getw(){return w;}
	}
	
	static double x_cor, y_cor;
	static List<Quad> taxis = new ArrayList<Quad>();
	static List<Quad> streets = new ArrayList<Quad>();
			
    public static void parser(String csvFile, int kk, Graph g) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "greek"));
            br.readLine(); //skip first line
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] country = line.split(cvsSplitBy);
	
				if(kk==1){
					Quad tri = new Quad();
					tri.setx(Double.parseDouble(country[0]));
					tri.sety(Double.parseDouble(country[1]));
					tri.setz(Integer.parseInt(country[2]));
					tri.setw("");
					taxis.add(tri);
				}
				else if(kk==2){
					x_cor = Double.parseDouble(country[0]);
					y_cor = Double.parseDouble(country[1]);
				}
				else{
					Quad tri = new Quad();
					tri.setx(Double.parseDouble(country[0]));
					tri.sety(Double.parseDouble(country[1]));
					tri.setz(Integer.parseInt(country[2]));
					if(country.length==4){tri.setw(country[3]);}
					else{tri.setw("");}
					//streets.add(tri);
					g.addVertex(tri);
				}

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


	public static class Graph<T> {
		final private LinkedHashMap<T, Set<T>> adjacencyList;
	   
		public Graph() {
		    this.adjacencyList = new LinkedHashMap<>();
		}
		
		public void addVertex(T v) {
		    if (this.adjacencyList.containsKey(v)) {
		        throw new IllegalArgumentException("Vertex already exists.");
		    }
		    this.adjacencyList.put(v, new HashSet<T>());
		}
		
		public void addEdge(T v, T u) {
		    if (!this.adjacencyList.containsKey(v) || !this.adjacencyList.containsKey(u)) {
		        throw new IllegalArgumentException();
		    }
		    
		    this.adjacencyList.get(v).add(u);
		}
	   
		
		public Iterable<T> getNeighbors(T v) {
		    return this.adjacencyList.get(v);
		}
		public Iterable<T> getAllVertices() {
		    return this.adjacencyList.keySet();
		}
	}
	
	public static class Klidi {

    private final double x;
    private final double y;

    public Klidi(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object O) {
        if (!(O instanceof Klidi)) return false;
    		if (((Klidi) O).x != x) return false;
    		if (((Klidi) O).y != y) return false;
    		return true;
    }

    @Override
    public int hashCode() {
        int f = (int)Double.doubleToLongBits(x);
        f = f^(f>>32);
        int k = (int)Double.doubleToLongBits(y);
        f = f^(k>>32);
        return f;
    }

}
	
	public static Graph CreateGraph(Graph g){
			Iterator <Quad> itr = (g.getAllVertices()).iterator();
    	LinkedHashMap<Klidi, Quad> temp = new LinkedHashMap<>();
    	Graph<Quad> gra = new Graph<Quad>();
    	
    	Quad local = (Quad)itr.next();
    	Klidi klidi1 = new Klidi(local.getx(), local.gety());
    	temp.put(klidi1, local);
    	gra.addVertex(local);
    	while(itr.hasNext()){
    		Quad key1 = (Quad)itr.next();
    		if((key1.getz()==local.getz())
    		   ||( local.getw().equals(key1.getw()) && !key1.getw().equals("") ) ){
    		  Klidi klidi2 = new Klidi(key1.getx(), key1.gety()); 
    			if(!temp.containsKey( klidi2 )){
    				gra.addVertex(key1);
    				Klidi klidi3 = new Klidi(key1.getx(), key1.gety()); 
    				temp.put(klidi3, key1);
    			}
    			Klidi klidi4 = new Klidi(local.getx(), local.gety());
    			Klidi klidi5 = new Klidi(key1.getx(), key1.gety()); 
    			Quad ll = temp.get(klidi5);
    			Quad kk = temp.get(klidi4);
    			gra.addEdge(ll, kk);
    			gra.addEdge(kk,ll);
    			    			
    		}
    		
    		Klidi klidi2 = new Klidi(key1.getx(), key1.gety()); 
    		if(!temp.containsKey( klidi2 )){
    				gra.addVertex(key1);
    				Klidi klidi3 = new Klidi(key1.getx(), key1.gety()); 
    				temp.put(klidi3, key1);
    			}
    		local = key1;
    		
    	
    	}
    	return gra;
	}
	
	public static double manh(Quad x, Quad y){
		//double dist = Math.abs(x.getx()-y.getx()) + Math.abs(x.gety() - y.gety());
		double dlon=deg2rad(x.getx())-deg2rad(y.getx()), dlat=deg2rad(x.gety())-deg2rad(y.gety());
		double a = Math.pow(Math.sin(dlat/2),2)+Math.cos(deg2rad(x.getx()))*Math.cos(deg2rad(y.getx()))*Math.pow(Math.sin(dlon/2),2);
		double dist = 6371*(2*Math.asin(Math.sqrt(a)));
		return dist;
	}
	
	public static double eucl(Quad x, Quad y){
		return ( Math.sqrt((Math.pow((x.getx()-y.getx()),2) + Math.pow((x.gety()-y.gety()),2))) );
	}
	
	public static double LatLon(Quad x, Quad y) {
		double lat1=x.getx(), lon1=x.gety(), lat2=y.getx(), lon2=y.gety();
		double R = 6371.0; // Radius of the earth in km
		double dLat = deg2rad(lat2-lat1);  // deg2rad below
		double dLon = deg2rad(lon2-lon1); 
		double a = 
		  Math.sin(dLat/2) * Math.sin(dLat/2) +
		  Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
		  Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c; // Distance in km
		return d;
	}
	public static double LatLon(double lat1, double lon1, double lat2, double lon2) {
		double R = 6371.0; // Radius of the earth in km
		double dLat = deg2rad(lat2-lat1);  // deg2rad below
		double dLon = deg2rad(lon2-lon1); 
		double a = 
		  Math.sin(dLat/2) * Math.sin(dLat/2) +
		  Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
		  Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c; // Distance in km
		return d;
	}

public static double deg2rad(double deg) {
  return deg * (Math.PI/180);
}

	public static class Node{
		Quad q;
		Klidi k;
		double g;
		double h;
		double f;
		Node parent;
		public Node(Quad qq, double d, double gg, Klidi kk, Node par){
			q = qq;
			parent = par;
			k = kk;
			g = d;
			h = gg;
			f = g + h;
		}
		@Override
    public boolean equals(Object O) {
        if (!(O instanceof Node)) return false;
    		if (((Node) O).q.getx()!= q.getx() ||((Node) O).q.gety()!= q.gety()) return false;
    		if(((Node) O).f > f) return false;
    		return true;
    }

    @Override
    public int hashCode() {
        int f = (int)Double.doubleToLongBits(g);
        f = f^(f>>32);
        int k = (int)Double.doubleToLongBits(h);
        f = f^(k>>32);
        return f;
    }
	}
		
		public static class Nodeco implements Comparator<Node>{
			@Override
			public int compare(Node x, Node y){
				if (x.f < y.f)
        {
            return -1;
        }
        if (x.f > y.f)
        {
            return 1;
        }
        return 0;
			}
		}
	
    public static void main(String[] args) throws Exception{
    	Graph<Quad> g = new Graph<Quad>();
    	parser("TN/taxis.csv",1,g);
    	parser("TN/client.csv",2,g);
    	parser("TN/nodes.csv",3,g);
    	
    	System.out.println("Cord "+x_cor+" "+y_cor);
    	
    	Graph<Quad> gra = CreateGraph(g);
    	LinkedHashMap<Klidi, Quad> goals = new LinkedHashMap<>();
    	LinkedHashMap</*Integer*/Klidi, Double> need_to_reach_taxi = new LinkedHashMap<>();
    	for(int i=0; i<taxis.size(); i++){
    		Quad tri = new Quad();
    		Quad mini = new Quad();
    		tri = taxis.get(i);
    		double min = 10000.0;
		  	for(Quad key: gra.getAllVertices() ){
		  		if(LatLon(tri,key) < min){
		  			mini = key;
		  			min = LatLon(tri, key);
		  		}
		  	}
		  	Klidi klidi = new Klidi(mini.getx(), mini.gety());
		  	goals.put(klidi, mini); //me ti sira ta kodinotera sta taxi
		  	need_to_reach_taxi.put(/*tri.getz()*/klidi, min); // me ti sira to kostos na pao sta kodinotera
		  }
		  Quad min_point = new Quad();
		  double min_dist = 10000.0;
		  	for(Quad key: gra.getAllVertices() ){
		  		if(LatLon(key.getx(),key.gety(),x_cor, y_cor) < min_dist){
		  			min_point = key;
		  			min_dist = LatLon(key.getx(),key.gety(),x_cor, y_cor);
		  		}
		  	} // min_point einai to kodinotero simio ston pelati
		  	// min_dist i elaxisti apostash apo ayto
		  //System.out.println(min_point.getw()+" "+min_dist);
		  
		  //for(Klidi key: goals.keySet()){System.out.println((goals.get(key)).getw());}
		 // for(/*Integer*/Klidi key: need_to_reach_taxi.keySet()){System.out.println((need_to_reach_taxi.get(key)));}
		  
		  //////////////////////// Implement A* ///////////////
		  double minimum_dist = 10000.0;
		  Quad stoxos=new Quad(); 
		  List<Node> res = new ArrayList<Node>();
		  for(Klidi keyyyy: goals.keySet()){
		  	stoxos = goals.get(keyyyy); 
				Comparator<Node> comparator = new Nodeco();
				PriorityQueue<Node> open = new PriorityQueue<Node>(comparator);
				PriorityQueue<Node> close = new PriorityQueue<Node>(comparator);
				List<Node> closed = new ArrayList<Node>();
				Klidi klidi = new Klidi(min_point.getx(), min_point.gety());
				
				Node no = new Node(min_point, 0.0, manh(stoxos,min_point), klidi,null);
				open.add(no);
				while(!open.isEmpty()){
					Node tmp = open.poll();
					Quad cur = tmp.q;
					Klidi klid = tmp.k;
					if(stoxos.equals(cur)){
						close.add(tmp);
						closed.add(tmp);	
						break;
					}
					for(Quad key: gra.getNeighbors(cur)){
						Klidi kli = new Klidi(key.getx(), key.gety());
						Node non = new Node(key, (tmp.g+LatLon(cur,key)), manh(stoxos,key),kli,tmp);
						if(!open.contains(non)){
							if(!close.contains(non)){
								open.add(non);
							}
						}
					}
					close.add(tmp);
					closed.add(tmp);
				}
				if((need_to_reach_taxi.get(keyyyy) + closed.get(closed.size()-1).g) < minimum_dist){
					minimum_dist = need_to_reach_taxi.get(keyyyy) + closed.get(closed.size()-1).g;
					res=new ArrayList<Node>(closed);
				}
		  }
		  
		  PrintWriter writer = new PrintWriter("rrr.txt", "UTF-8");
		  Quad kkk=res.get(res.size()-1).parent.q;
		  //System.out.println(res.get(res.size()-1).q.getw()+" "+res.get(res.size()-1).q.getx()+" "+res.get(res.size()-1).g);
		  writer.println(res.get(res.size()-1).q.getx()+","+res.get(res.size()-1).q.gety()+","+"0");
		  for(int i =res.size()-2; i>=0; i--){
		  	if(kkk.equals(res.get(i).q)){
		  		//System.out.println(res.get(i).q.getw()+" "+res.get(i).q.getx()+" "+res.get(i).g); 
		  		writer.println(res.get(i).q.getx()+","+res.get(i).q.gety()+","+"0");			
		  		if(i==0) break;
		  		kkk=res.get(i).parent.q;
		  		}
		  }
		  writer.close();
		  
		  
		  minimum_dist = 10000.0;
		  stoxos=new Quad();
		  stoxos = min_point; //gia anapodi diadromi 
		  res = new ArrayList<Node>();
		  for(Klidi keyyyy: goals.keySet()){
		  	min_point = goals.get(keyyyy); 
				Comparator<Node> comparator = new Nodeco();
				PriorityQueue<Node> open = new PriorityQueue<Node>(comparator);
				PriorityQueue<Node> close = new PriorityQueue<Node>(comparator);
				List<Node> closed = new ArrayList<Node>();
				Klidi klidi = new Klidi(min_point.getx(), min_point.gety());
				
				Node no = new Node(min_point, 0.0, manh(stoxos,min_point), klidi,null);
				open.add(no);
				while(!open.isEmpty()){
					Node tmp = open.poll();
					Quad cur = tmp.q;
					Klidi klid = tmp.k;
					if(stoxos.equals(cur)){
						close.add(tmp);
						closed.add(tmp);	
						break;
					}
					for(Quad key: gra.getNeighbors(cur)){
						Klidi kli = new Klidi(key.getx(), key.gety());
						Node non = new Node(key, (tmp.g+LatLon(cur,key)), manh(stoxos,key),kli,tmp);
						if(!open.contains(non)){
							if(!close.contains(non)){
								open.add(non);
							}
						}
					}
					close.add(tmp);
					closed.add(tmp);
				}
				if((need_to_reach_taxi.get(keyyyy) + closed.get(closed.size()-1).g) < minimum_dist){
					minimum_dist = need_to_reach_taxi.get(keyyyy) + closed.get(closed.size()-1).g;
					res=new ArrayList<Node>(closed);
				}
		  }
		  
		  writer = new PrintWriter("xrr.txt", "UTF-8");
		  kkk=res.get(res.size()-1).parent.q;
		  //System.out.println(res.get(res.size()-1).q.getw()+" "+res.get(res.size()-1).q.getx()+" "+res.get(res.size()-1).g);
		  writer.println(res.get(res.size()-1).q.getx()+","+res.get(res.size()-1).q.gety()+","+"0");
		  for(int i =res.size()-2; i>=0; i--){
		  	if(kkk.equals(res.get(i).q)){
		  	//	System.out.println(res.get(i).q.getw()+" "+res.get(i).q.getx()+" "+res.get(i).g); 
		  		writer.println(res.get(i).q.getx()+","+res.get(i).q.gety()+","+"0");			
		  		if(i==0) break;
		  		kkk=res.get(i).parent.q;
		  		}
		  }
		  writer.close();
    	
    	
    }

}
