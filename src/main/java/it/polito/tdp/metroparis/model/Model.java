package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {

	private Graph<Fermata, DefaultEdge>graph; 
	private List<Fermata> fermate;
	private Map<Integer,Fermata>fermataIdMap;
	
	public Model() {
		graph=new SimpleDirectedGraph<>(DefaultEdge.class);
		
		MetroDAO dao=new MetroDAO();
		// CREAZIONE DEGLI VERTICI
		this.fermate=dao.getAllFermate();
		this.fermataIdMap=new HashMap<>();
		for(Fermata f:this.fermate) {
			this.fermataIdMap.put(f.getIdFermata(), f);
		}
		Graphs.addAllVertices(this.graph, this.fermate); 
		System.out.println(this.graph);
		
		// aggiunta degli archi
		// Metodo 1: tra coppia di stazioni c'è o no l'arco --> di solito è il meno efficace
		// Sto analizzando a vedere uno per uno gli archi. 
		
		/*for(Fermata fp: this.fermate) {
			for(Fermata fa:this.fermate) {
				if(dao.fermateConnesse(fp, fa)) {
					this.graph.addEdge(fp, fa);
				}
			}
		}*/
		
		// Metodo 2: da un vertice trova tutti i connessi --> lavora più il db e meno il codice
		/*for(Fermata fp:this.fermate) {
			List<Fermata>connesse=dao.fermataSuccessive(fp, fermataIdMap) ;
			for(Fermata fa: connesse) {
				this.graph.addEdge(fp, fa);
			}
		}*/
		
		// Metodo 3: farci dare dal db gli archi che ci servono
		List<CoppiaFermate>coppie=dao.coppieFermate(this.fermataIdMap);
		for(CoppiaFermate c:coppie) {
			this.graph.addEdge(c.getFp(), c.getFa());
		}
		
		
		//System.out.println(this.graph);
		System.out.format("Grafo caricato con %d vertici e %d archi\n", this.graph.vertexSet().size(), this.graph.edgeSet().size());
	}
	/**
	 * Visita grafo on la strategia Breatdth First 
	 * @param source vertice di partenza
	 * @return l'insieme dei vertici incontrati
	 */
	
	public List<Fermata> visitaAmpiezza(Fermata source) {
		List<Fermata>visita=new ArrayList<>();
		GraphIterator<Fermata,DefaultEdge>bfv=new BreadthFirstIterator<>(graph, source);
		while(bfv.hasNext()) {
			visita.add(bfv.next());
		}
		return visita;
		
	}
	/**
	 * Dalle slides
	 * <r,s> r l'ho scoperto da s
	 * <v,r>
	 * <w,s> 
	 * <vertice nuovo scoperto, vertice Precedente da cui Arrivo>
	 */
	public Map<Fermata, Fermata> alberoVisita(Fermata source) {
		Map<Fermata,Fermata> albero = new HashMap<>();
		// la metto perchè poi dentro al metodo edgeTraversed non avrei nessuno dei due noti
		albero.put(source, null) ;
		
		GraphIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<>(graph, source);
		
		bfv.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>() {
			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
				// la visita sta considerando un nuovo arco.
				// questo arco ha scoperto un nuovo vertice?
				// se sì, provenendo da dove?
				DefaultEdge edge = e.getEdge(); // (a,b) : ho scoperto 'a' partendo da 'b' oppure 'b' da 'a'
				Fermata a = graph.getEdgeSource(edge);
				Fermata b = graph.getEdgeTarget(edge);
				if(albero.containsKey(a) && !albero.containsKey(b)) {
					// a è già noto ( già tra le chiavi), quindi ho scoperto b provenendo da a
					albero.put(b,a) ;
				} else if(albero.containsKey(b) && !albero.containsKey(a)){
					// b è già noto, quindi ho scoperto a provenendo da b
					albero.put(a,b) ;
				}
			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {}

		});
		
		while(bfv.hasNext()) {
			bfv.next() ; // estrai l'elemento e ignoralo
		}
		
		return albero ;
		
	}
	
	
	
	public List<Fermata> visitaProfondita(Fermata source) {
		List<Fermata>visita2=new ArrayList<>();
		GraphIterator<Fermata,DefaultEdge>dfv=new DepthFirstIterator<>(graph, source);
		while(dfv.hasNext()) {
			visita2.add(dfv.next());
		}
		return visita2;
		
	}
	
	
	public static void main(String arg[]) {
		Model m =new Model();
		List<Fermata>visita=m.visitaAmpiezza(m.fermate.get(0));
		System.out.println(visita);
		List<Fermata>visita2=m.visitaProfondita(m.fermate.get(0));
		System.out.println(visita2);
		
		Map<Fermata,Fermata> albero = m.alberoVisita(m.fermate.get(0)) ;
		for(Fermata f: albero.keySet()) {
			System.out.format( "%s <- %s\n", f, albero.get(f)) ;
		}
		
	}
}
