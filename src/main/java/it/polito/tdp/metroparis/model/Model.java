package it.polito.tdp.metroparis.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

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
		System.out.format("Grafo caricato con %d vertici e %d archi", this.graph.vertexSet().size(), this.graph.edgeSet().size());
		
		
		
	
		
	}
	
	public static void main(String arg[]) {
		Model m =new Model();
		
	}
}
