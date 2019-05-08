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
	
	private MetroDAO dao = new MetroDAO();
	private Graph<Fermata, DefaultEdge> grafo;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	
	public void creaGrafo() {
		// Creo l'oggetto grafo
		this.grafo = new SimpleDirectedGraph<>(DefaultEdge.class);
		
		// Aggiungo i vertici
		this.fermate = dao.getAllFermate();
		Graphs.addAllVertices(this.grafo, fermate);
		
		// Creo idMap
		this.fermateIdMap = new HashMap<>();
		for(Fermata f : this.fermate)
			fermateIdMap.put(f.getIdFermata(), f);
		
		/* Aggiungo gli archi (opzione 1)
		for(Fermata partenza : this.grafo.vertexSet()) {
			for(Fermata arrivo : this.grafo.vertexSet()) {
				if(dao.esisteConnessione(partenza, arrivo))
					this.grafo.addEdge(partenza, arrivo);
			}
		}
		*/
		
		// Aggiungo gli archi (opzione 2)
		for(Fermata partenza : this.grafo.vertexSet()) {
			List<Fermata> arrivi = dao.stazioniArrivo(partenza, fermateIdMap);
			
			for(Fermata arrivo : arrivi)
				this.grafo.addEdge(partenza, arrivo);
		}
		
		/* Aggiungo gli archi (opzione 3)
		 * utilizzo in modo diretto le colonne id_stazP e id_stazA della tabella connessione
		 */
	}

	public Graph<Fermata, DefaultEdge> getGrafo() {
		return grafo;
	}

	public List<Fermata> getFermate() {
		return fermate;
	}
	
}
