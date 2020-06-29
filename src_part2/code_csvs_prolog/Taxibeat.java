import java.io.*;
import java.util.*;
import static java.lang.System.out;
import com.ugos.jiprolog.engine.JIPEngine;
import com.ugos.jiprolog.engine.JIPQuery;
import com.ugos.jiprolog.engine.JIPSyntaxErrorException;
import com.ugos.jiprolog.engine.JIPTerm;
import com.ugos.jiprolog.engine.JIPTermParser;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

public class Taxibeat {
	public static PrintWriter create(){	
		try{
			 PrintWriter writer = new PrintWriter("genfacts.pl", "UTF-8");
			 return writer;
		} catch (Exception e){
			System.out.println(e.getMessage());
			return null;
		}  
	}

  	public static class Position {
    public double x;
    public double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    

    public String stringify() {
        return this.x + " " + this.y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public static Position parsePosition(String positionString) {
        String[] parts = positionString.split(" ");
        double x = Double.valueOf(parts[0].trim());
        double y = Double.valueOf(parts[1].trim());

        return new Position(x, y);
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
    
    public double distanceTo(Position b) {
	int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    	double startLat=this.y, startLong=this.x,endLat=b.y,endLong=b.x;

        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // <-- d
    }

}

    
    public static PrologParser prologSystem;

public static class RouteComparator implements Comparator<Route> {
    @Override
    public int compare(Route a, Route b) {
        if (a.getCost() < b.getCost()) {
            return -1;
        }

        if (a.getCost() > b.getCost()) {
            return 1;
        }

        return 0;
    }
}


	public static class Route {
    private ArrayList<String> nodes;
    private double cost;
    public Taxibeat.Taxi driver;

    public Route(SearchNode goal, double cost) {
        nodes = new ArrayList<String>();
        SearchNode current = goal;
        while (current != null) {
            nodes.add(0, current.stringify());
            current = current.previous;
        }
        this.nodes = nodes;
        this.cost = cost;
    }

    public void assignDriver(Taxibeat.Taxi driver) {
        this.driver = driver;
    }

    public ArrayList<String> getNodesString() {
        return nodes;
    }

    public double getCost() {
        return cost;
    }
}


	public static class Taxi extends Position {
    public int id;
    public double rating;

    public Taxi(int id, double x, double y) {
        super(x, y);
        this.id = id;
    }
 

    public static ArrayList<Taxi> parse(PrologParser prologSystem, PrintWriter writer) {
        BufferedReader reader = null;
        ArrayList<Taxi> fleet = new ArrayList<Taxi>();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("taxis.csv"), "greek"));

            String line = null;
            String[] numbers = null;
            reader.readLine(); 
            Taxi taxi = null;
            while ((line = reader.readLine()) != null) {
                numbers = line.split(",");

                String predicate = "taxi(" + Double.valueOf(numbers[0].trim()) + "," + Double.valueOf(numbers[1].trim()) + "," + Integer.valueOf(numbers[2].trim()) + "," + numbers[3].trim() + "," + Double.valueOf(numbers[6].trim()) + "," + numbers[7].trim() + "," + numbers[8].trim() + ")";
                prologSystem.asserta(predicate, writer);

                for (String language : numbers[5].trim().split("\\|")) {
                    prologSystem.asserta("speaksDriver(" + Integer.valueOf(numbers[2].trim()) + "," + language + ")", writer);
                }

                prologSystem.asserta("maxPassengers(" + Integer.valueOf(numbers[2].trim()) + "," + (numbers[4].trim().split("-"))[1] + ")", writer);

                fleet.add(new Taxi(Integer.valueOf(numbers[2].trim()), Double.valueOf(numbers[0].trim()), Double.valueOf(numbers[1].trim())));
            }
        } catch (IOException e) {
            System.err.println("Exception:" + e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Exception:" + e.toString());
                }
            }
        }

        return fleet;
    }
}


public static class Line {
    public static void parse(PrintWriter writer) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("lines.csv"), "greek"));
            PrologParser prologSystem = PrologParser.getInstance();

            int id, lanes, maxspeed;
            String line, highway;
            String[] parts = null;

            reader.readLine(); 
            while ((line = reader.readLine()) != null) {
                line = replaceEscapedCommas(line);
                line = line.replace(",", " , ");
                parts = line.split(",");

                for (int i = 0; i < parts.length; ++i) {
                    parts[i] = parts[i].trim();
                    if (parts[i].length() == 0) {
                        parts[i] = "n";
                    }
                }

                highway = parts[1].equals("n") ? "unknown" : parts[1];
                lanes = !parts[5].equals("n") ? Integer.valueOf(parts[5]) : 0;
                maxspeed = !parts[6].equals("n") ? Integer.valueOf(parts[6]) : 0;
               

                String predicate = "lineSpecs(" + Integer.valueOf(parts[0]) + "," + highway + "," + parts[3] + "," + parts[4] + "," + lanes + "," + maxspeed + "," + parts[7] + "," + parts[8] + "," + parts[9] + "," + parts[10] + "," + parts[11] + "," + parts[12] + "," + parts[13] + "," + parts[14].replace('%', ' ') + "," + parts[15] + "," + parts[16] + "," + parts[17] + ")";
                prologSystem.asserta(predicate, writer);
            }
        } catch (IOException e) {
            System.err.println("Exception:" + e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Exception:" + e.toString());
                }
            }
        }
    }

    private static String replaceEscapedCommas(String line) {
        if (line.contains("\"")) {
            String[] chars = line.split("");
            int index;
            boolean replaceCommas = false;
            for (index = 0; index < chars.length; index++) {
                if (chars[index].equals("\"")) {
                    replaceCommas = !replaceCommas;
                }
                if (chars[index].equals(",") && replaceCommas) {
                    chars[index] = " "; 
                }
            }
            line = String.join("", chars);
        }
        return line;
    }
}
public static class Client extends Position {
    int id;

    public Client(double x, double y) {
        super(x, y);
    }

    

    public static Client parse(PrologParser prologSystem, PrintWriter writer) {
        BufferedReader reader = null;

        Client position = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("client.csv"), "greek"));
            
            String[] numbers = null;
            String time, language;

            reader.readLine(); 
            String line = reader.readLine();
            numbers = line.split(",");

            String predicate = "client(" + Double.valueOf(numbers[0].trim()) + "," + Double.valueOf(numbers[1].trim()) + "," + Double.valueOf(numbers[2].trim()) + "," + Double.valueOf(numbers[3].trim()) + "," + numbers[4].replace(":", "") + "," + Integer.valueOf(numbers[5].trim()) + "," + numbers[6] + "," + Integer.valueOf(numbers[7].trim()) + ")";
            prologSystem.asserta(predicate, writer);

            position = new Client(Double.valueOf(numbers[0].trim()), Double.valueOf(numbers[1].trim()));
        } catch (IOException e) {
            System.err.println("Exception:" + e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Exception:" + e.toString());
                }
            }
        }

        return position;
    }
}


	public static class SearchNode {
    Taxibeat.Node positionNode;
    private ArrayList<GraphEdge> neighbors;
    public double routeCost;
    public boolean isGoal;
    public double h;
    private double haversine;
    public double factor;
    private double distance;
    SearchNode previous;

    public double getHeuristic() {
        return factor * h;
    }


    public SearchNode(Taxibeat.Node A, boolean isGoal, double haversine) {
        previous = null;
        this.neighbors = new ArrayList<GraphEdge>();
        routeCost = 0;
        this.isGoal = isGoal;
        this.haversine = haversine;
        this.h = haversine;
        positionNode = A;
        factor = 1;
        distance = 0;
    }


    public void addNeighbor(GraphEdge edge) {
        neighbors.add(edge);
    }

    public ArrayList<GraphEdge> getNeighbors() {
        return neighbors;
    }

    public String stringify() {
        return positionNode.stringify();
    }

    public void setPrevious(SearchNode A) {
        this.previous = A;
    }
   

    public Taxibeat.Node getNode() {
        return positionNode;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

}


public static class TaxiComparator implements Comparator<Taxi> {
    @Override
    public int compare(Taxi a, Taxi b) {
        if (a.rating < b.rating) {
            return 1;
        }

        if (a.rating > b.rating) {
            return -1;
        }

        return 0;
    }
}

	public static class GraphEdge {
    private Taxibeat.SearchNode leadsToNode;
    private double weight;
    public double h;

    public GraphEdge(Taxibeat.SearchNode node, double weight, double h) {
        this.leadsToNode = node;
        this.weight = weight;
        this.h = h;
    }

    public Taxibeat.SearchNode getNode() {
        return leadsToNode;
    }

    public double getWeight() {
        return weight;
    }

    public double getHeuristic() {
        return h;
    }

    
}

public static class Traffic {
    public static void parse(PrintWriter writer) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("traffic.csv"), "greek"));
            PrologParser prologSystem = PrologParser.getInstance();

            int startTime = 0, endTime = 0;
            String trafficInfo, line;
            String[] parts = null, trafficParts;
//int ccc=0;
            reader.readLine(); 
            while ((line = reader.readLine()) != null) {
                parts = line.split(",");
//ccc++;
  //System.out.println(ccc);              
                if (parts.length >= 3 && !parts[2].equals("")) {
                    trafficInfo = parts[2];
                    trafficParts = trafficInfo.split("\\|");
                    for (String trafficPart : trafficParts) {

                        String predicate = "lineTraffic(" + Integer.valueOf(parts[0]) + "," + Integer.valueOf((trafficPart.split("=")[0]).split("-")[0].replace(":", "")) + "," + Integer.valueOf((trafficPart.split("=")[0]).split("-")[1].replace(":", "")) + "," + trafficPart.split("=")[1] + ")";
                        prologSystem.asserta(predicate, writer);
                    }

                }
            }
        } catch (IOException e) {
            System.err.println("Exception:" + e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Exception:" + e.toString());
                }
            }
        }
    }
}

	public static class Node extends Position {
    private int streetID;

    public Node(double x, double y,  int streetID) {
        super(x, y);
	this.streetID = streetID;
    }
}

		public static class World {
    private ArrayList<Node> nodes;
    PrologParser prologSystem;

    private static final World instance = new World();
    private World() {
        nodes = new ArrayList<Node>();
        prologSystem = PrologParser.getInstance();
    };
    public static World getInstance() {
        return instance;
    }


    public void parseNodes(PrintWriter writer) {
        BufferedReader reader = null;

        String line = null;
        String[] parts = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("nodes.csv"), "greek"));

            reader.readLine(); 
            while ((line = reader.readLine()) != null) {
                parts = line.split(",");
                

                nodes.add(new Node(Double.valueOf(parts[0].trim()), Double.valueOf(parts[1].trim()), Integer.valueOf(parts[2].trim())));
                prologSystem.asserta("node(" + Double.valueOf(parts[0].trim()) + "," + Double.valueOf(parts[1].trim()) + "," + Integer.valueOf(parts[2].trim()) +  ")", writer);
            }
        } catch (IOException e) {
            System.err.println("Exception:" + e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Exception:" + e.toString());
                }
            }
        }
    }
    
    
    

    public SearchSpace generateSearchSpace(Client clientPosition) {
        HashMap<String, SearchNode> searchSpace = new HashMap<String, SearchNode>();

        double nodesDistance;
        int previousStreetId = -1;
        Node previousNode = null;
        Node targetNode = closestNode(clientPosition);

        for (Node currentNode : nodes) {
            boolean isGoal = targetNode.stringify().equals(currentNode.stringify());
            if (!searchSpace.containsKey(currentNode.stringify())) {
                searchSpace.put(currentNode.stringify(), new SearchNode(currentNode, isGoal, currentNode.distanceTo(targetNode)));
            }
            if (previousStreetId != currentNode.streetID) {
                previousStreetId = currentNode.streetID;
            } else {

                if (previousNode != null) {
                    nodesDistance = previousNode.distanceTo(currentNode);
                    searchSpace.get(currentNode.stringify()).addNeighbor(new GraphEdge(
                        searchSpace.get(previousNode.stringify()),
                        nodesDistance,
                        previousNode.distanceTo(targetNode)
                    ));
                    searchSpace.get(previousNode.stringify()).addNeighbor(new GraphEdge(
                        searchSpace.get(currentNode.stringify()),
                        nodesDistance,
                        currentNode.distanceTo(targetNode)
                    ));
                }
            }

            previousNode = currentNode;
        }
        return new SearchSpace(searchSpace);
    }

    public Node closestNode(Position position) {
        double minDistance = -1, distance;
        Node closest = null;
        for (Node currentNode : nodes) {
            distance = currentNode.distanceTo(position);
            if (minDistance == -1 || distance < minDistance) {
                minDistance = distance;
                closest = currentNode;
            }
        }

        return closest;
    }

}

public static class SearchSpace {
    public HashMap<String, SearchNode> searchMap;
    private ArrayList<SearchNode> dirtyNodes;

    public SearchSpace(HashMap<String, SearchNode> searchMap){
        dirtyNodes = new ArrayList<SearchNode>();
        this.searchMap = searchMap;
    }

    public HashMap<String, SearchNode> getMap() {
        return this.searchMap;
    }

    public void setDirtyEntry(SearchNode node) {
        dirtyNodes.add(node);
    }

    public void adeiasma() {
        for (SearchNode dirtyNode : dirtyNodes) {
            dirtyNode.routeCost = 0;
            dirtyNode.setPrevious(null);
        }
        dirtyNodes.clear();
    }

    
}



public static class SearchNodeComparator implements Comparator<SearchNode> {
    @Override
    public int compare(SearchNode a, SearchNode b) {
        double aG = a.routeCost, bG = b.routeCost;
        double aH = a.getHeuristic(), bH = b.getHeuristic();

        if (a.stringify().equals(b.stringify())) {
            return 0;
        }

        if (aG + aH < bG + bH) {
            return -1;
        }

        return 1;
    }
}

    public static Route findRoute(SearchSpace searchSpace, Position startPosition, int maxFrontier) {
        HashMap<String, SearchNode> searchSpaceMap = searchSpace.getMap();
        Comparator<SearchNode> comparator = new SearchNodeComparator();
        SortedSet<SearchNode> queue = new TreeSet<SearchNode>(comparator);
        Set<String> visited = new TreeSet<String>();
        HashMap<String,String> inQueueHash = new HashMap<String, String>();

        SearchNode startNode = searchSpaceMap.get(startPosition.stringify());
        for (GraphEdge neighbor : (startNode.getNeighbors())) {
            SearchNode theNode = neighbor.getNode();

            if (!prologSystem.canMoveFromTo(startNode, theNode)) {
                continue;
            }

            double weightFactor = prologSystem.calculateFactor(startNode, theNode);

            theNode.factor = weightFactor;
            theNode.routeCost = weightFactor * neighbor.getWeight();
            theNode.setDistance(neighbor.getWeight());
            theNode.setPrevious(startNode);
            searchSpace.setDirtyEntry(theNode);
            queue.add(theNode);
            inQueueHash.put(theNode.stringify(), "true");
        }
        visited.add(startPosition.stringify());

        SearchNode top = null;
        SearchNode frontier;
        ArrayList<String> route;
        int counter, stepsCounter = 0, actualMaxFrontier = 0;
        boolean foundRoute = false;
        while (queue.size() > 0) {
            if (queue.size() > actualMaxFrontier) {
                actualMaxFrontier = queue.size();
            }

            ++stepsCounter;
            top = queue.first();
            queue.remove(top);
            inQueueHash.remove(top.stringify());
            visited.add(top.stringify());

            if (top.isGoal) {
                foundRoute = true;
                break;
            }

            for (GraphEdge neighbor : top.getNeighbors()) {
                SearchNode theNode = neighbor.getNode();

                if (!prologSystem.canMoveFromTo(top, theNode)) {
                    continue;
                }

                if (!visited.contains(theNode.stringify())) {
                    double weightFactor = prologSystem.calculateFactor(top, theNode);
                    double theCost = top.routeCost + weightFactor * neighbor.getWeight();
                    double theDistance = top.getDistance() + neighbor.getWeight();

                    if (inQueueHash.containsKey(theNode.stringify())) {
                        if (theCost < theNode.routeCost) {
                            queue.remove(theNode);
                            theNode.factor = weightFactor;
                            theNode.routeCost = theCost;
                            theNode.setDistance(theDistance);
                            theNode.setPrevious(top);
                            searchSpace.setDirtyEntry(theNode);
                            queue.add(theNode);
                        }
                    } else {
                        theNode.setPrevious(top);
                        theNode.factor = weightFactor;
                        theNode.routeCost = theCost;
                        theNode.setDistance(theDistance);
                        searchSpace.setDirtyEntry(theNode);
                        queue.add(theNode);
                        inQueueHash.put(theNode.stringify(), "a");
                    }
                }
            }

            if (queue.size() > maxFrontier) {
                inQueueHash.remove(queue.last().stringify());
                queue.remove(queue.last());
            }
        }

        if (foundRoute) {
            return new Route(top, top.getDistance());
        }
        return null;
    }	
    
    public static class PrologParser {
    private static final PrologParser instance = new PrologParser();

    private JIPEngine jip;
    private JIPTermParser parser;
    private JIPQuery jipQuery;
    private JIPTerm term;

    private PrologParser() {
        try {
            jip = new JIPEngine();
            jip.consultFile("kosmos.pl");
            parser = jip.getTermParser();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static PrologParser getInstance() {
        return instance;
    }

    void asserta(String predicate, PrintWriter writer) {
        jip.asserta(parser.parseTerm(predicate));
	writer.println(predicate);
    }

    public boolean canMoveFromTo(Taxibeat.SearchNode A, Taxibeat.SearchNode B) {
        double Ax = A.getNode().getX();
        double Ay = A.getNode().getY();
        double Bx = B.getNode().getX();
        double By = B.getNode().getY();

        String queryString = "canMoveFromTo(" + Ax + "," + Ay + "," + Bx + ","  + By + ").";
        jipQuery = jip.openSynchronousQuery(parser.parseTerm(queryString));
        if (jipQuery.nextSolution() != null) {
            return true;
        } else {
            return false;
        }
    }

    public double calculateFactor(Taxibeat.SearchNode A, Taxibeat.SearchNode B) {
        double Ax = A.getNode().getX();
        double Ay = A.getNode().getY();
        double Bx = B.getNode().getX();
        double By = B.getNode().getY();

        String queryString = "weightFactor(" + Ax + "," + Ay + "," + Bx + ","  + By + ", Value).";
        jipQuery = jip.openSynchronousQuery(parser.parseTerm(queryString));
		term = jipQuery.nextSolution();
		if (term != null) {
            String factorString = term.getVariablesTable().get("Value").toString();
            double factor = Double.parseDouble(factorString);
            return factor;
		} else {
            System.out.println("Factor calculation failed :(");
            return -1;
        }
    }

    public boolean isQualifiedDriver(int driverID) {
        String queryString = "isQualifiedDriverForClient(" + driverID + ").";
        jipQuery = jip.openSynchronousQuery(parser.parseTerm(queryString));
        if (jipQuery.nextSolution() != null) {
            return true;
        } else {
            return false;
        }
    }
    public double getDriverRank(int driverID) {
        String queryString = "driverRank(" + driverID + ",Rank).";
        jipQuery = jip.openSynchronousQuery(parser.parseTerm(queryString));
		term = jipQuery.nextSolution();
		if (term != null) {
            String rankString = term.getVariablesTable().get("Rank").toString();
            double rank = Double.parseDouble(rankString);
            return rank;
		} else {
            System.out.println("Rank calculation failed :(");
            return -1;
        }
    }



}


public static class XMLFile {
    private Document doc;
    private Transformer transformer;
    private DOMSource source;
    private StreamResult consoleResult;
    private StreamResult result;
    public XMLFile(String filename) {
        try {
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.newDocument();

			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			source = new DOMSource(doc);
			result = new StreamResult(new File(filename));

          
			consoleResult = new StreamResult(System.out);
        } catch (Exception e) {
            System.out.println("Error creating doc");
        }
    }


    private Element createStyleElement(String styleName, String color) {
        Element style = doc.createElement("Style");
        Attr attrType = doc.createAttribute("id");
        attrType.setValue(styleName);
        style.setAttributeNode(attrType);
        Element lineStyle = doc.createElement("LineStyle");
        style.appendChild(lineStyle);

        Element colorElement = doc.createElement("color");
        colorElement.appendChild(doc.createTextNode(color));
        lineStyle.appendChild(colorElement);

        Element widthElement = doc.createElement("width");
        widthElement.appendChild(doc.createTextNode("4"));
        lineStyle.appendChild(widthElement);

        return style;
    }

    private Element createPlacemarkElement(String name,
            String style, ArrayList<String> coordinates) {
        Element placemarkElement = doc.createElement("Placemark");
        Element nameElement = doc.createElement("name");
        nameElement.appendChild(doc.createTextNode(name));
        placemarkElement.appendChild(nameElement);

        Element styleElement = doc.createElement("styleUrl");
        styleElement.appendChild(doc.createTextNode("#" + style));
        placemarkElement.appendChild(styleElement);

        Element lineStringElement = doc.createElement("LineString");
        placemarkElement.appendChild(lineStringElement);

        Element altElement = doc.createElement("altitudeMode");
        altElement.appendChild(doc.createTextNode("relative"));
        lineStringElement.appendChild(altElement);

        Element coordElement = doc.createElement("coordinates");
        lineStringElement.appendChild(coordElement);

        for (String item: coordinates) {
            String[] parts = item.split(" ");
            coordElement.appendChild(doc.createTextNode(
                parts[0] + "," + parts[1]  + ",0" + "\n"
            ));
//System.out.print(parts[0] + "," + parts[1]  + ",0" + "\n");
        };
        return placemarkElement;
    }


    public void write(SortedSet<Taxibeat.Route> routes) {
        try {
            
            Element rootElement = doc.createElement("kml");
            Attr attrType = doc.createAttribute("xmlns");
            attrType.setValue("http://earth.google.com/kml/2.1");
            rootElement.setAttributeNode(attrType);
            doc.appendChild(rootElement);

            
            Element docElement = doc.createElement("Document");
            rootElement.appendChild(docElement);

           
            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode("Taxi Routes"));
            docElement.appendChild(name);

            docElement.appendChild(createStyleElement("green", "ff009900"));
            docElement.appendChild(createStyleElement("red", "ff0000ff"));

            Iterator<Taxibeat.Route> iterator = routes.iterator();
            iterator.next(); 
            Taxibeat.Route route;
            while (iterator.hasNext()) {
                route = iterator.next();
                docElement.appendChild(createPlacemarkElement("Taxi " + route.driver.id, "red", route.getNodesString()));
            }
            docElement.appendChild(createPlacemarkElement("Taxi " + routes.first().driver.id, "green", routes.first().getNodesString()));

            routes.remove(routes.first());

			transformer.transform(source, result);
		} catch (Exception pce) {
            System.out.println("Error while writing file...");
			pce.printStackTrace();
		}
    }
}

    public static void main(String[] args) {
        
        int maxFrontier = 23;
        
	PrintWriter writer = create();
        prologSystem = PrologParser.getInstance();

        World myWorld = World.getInstance();
        ArrayList<Taxi> fleet = Taxi.parse(prologSystem, writer);
        Client clientPosition = Client.parse(prologSystem, writer);

        myWorld.parseNodes(writer);
	Line.parse(writer);
	Traffic.parse(writer);

        Comparator<Route> routeComparator = new RouteComparator();
        SortedSet<Route> routes = new TreeSet<Route>(routeComparator);
        double minRouteDistance = -1;

        SearchSpace searchSpace = myWorld.generateSearchSpace(clientPosition);
	Comparator<Taxi> taxiComparator = new TaxiComparator();
        SortedSet<Taxi> availableTaxis = new TreeSet<Taxi>(taxiComparator);
        for (Taxi taxi : fleet) {
            if (!prologSystem.isQualifiedDriver(taxi.id)) {
                continue;
            }
	    taxi.rating = prologSystem.getDriverRank(taxi.id);
            searchSpace.adeiasma();
            Node driverNode = myWorld.closestNode(taxi);
            Route route = findRoute(searchSpace, driverNode, maxFrontier);
            if (route != null) {
                route.assignDriver(taxi);
                routes.add(route);
		availableTaxis.add(taxi);
            }
        }
        for (Route route : routes) {
            System.out.println(route.driver.id + " " + route.getCost());
        }
	
        for (Taxi taxi : availableTaxis) {
            System.out.println(taxi.id + " Rating: " + taxi.rating);
        }
        if (routes.size() > 0) {
            XMLFile outFile = new XMLFile("output.kml");
            outFile.write(routes);
        }
	writer.close();
    }    
}
