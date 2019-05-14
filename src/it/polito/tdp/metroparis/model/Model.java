package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private class EdgeTraversedGraphListener implements TraversalListener<Fermata, DefaultWeightedEdge> {

		@Override
		public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {			
		}

		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {			
		}

		@Override
		public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
			Fermata sourceVertex = grafo.getEdgeSource(e.getEdge());
			Fermata targetVertex = grafo.getEdgeTarget(e.getEdge());
			
			if(!backVisit.containsKey(targetVertex) && backVisit.containsKey(sourceVertex))
				backVisit.put(targetVertex, sourceVertex);
			else if(!backVisit.containsKey(sourceVertex) && backVisit.containsKey(targetVertex)) 
				backVisit.put(sourceVertex, targetVertex);
		}

		@Override
		public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {			
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {			
		}
	}
	
	private MetroDAO dao = new MetroDAO();
	private Graph<Fermata, DefaultWeightedEdge> grafo;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	private Map<Fermata, Fermata> backVisit;
	
	public void creaGrafo() {
		// Creo l'oggetto grafo
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
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
		
		// Aggiungo pesi
		List<ConnessioneVelocita> archipesati = dao.getConnessioniVelocita();
		for(ConnessioneVelocita cv : archipesati) {
			Fermata partenza = fermateIdMap.get(cv.getStazP());
			Fermata arrivo = fermateIdMap.get(cv.getStazA());
			double velocita =  cv.getVelocita();
			double distanza = LatLngTool.distance(partenza.getCoords(), arrivo.getCoords(), LengthUnit.KILOMETER);
			double peso = distanza/velocita * 3600;
			
			grafo.setEdgeWeight(partenza, arrivo, peso);
			
			/* ALTERNATIVA: aggiungo i pesi direttamente in fase di aggiunta degli archi:
			 * 
			 * Graphs.addEdgeWithVertices(g, sourceVertex, targetVertex, weight); 
			 */
		}
	}
	
	public List<Fermata> fermateRaggiungibili(Fermata source) {
		List<Fermata> result = new ArrayList<>();
		backVisit = new HashMap<>();
		
		GraphIterator<Fermata, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo, source);
		// GraphIterator<Fermata, DefaultWeightedEdge> it = new DepthFirstIterator<>(this.grafo, source);
		
		it.addTraversalListener(new Model.EdgeTraversedGraphListener());
		
		backVisit.put(source, null);
		
		while(it.hasNext()) {
			result.add(it.next());
		}
		
		// System.out.println(backVisit);
		
		return result;
	}
	
	public List<Fermata> percorsoFinoA(Fermata target) {
		if(!backVisit.containsKey(target)) {
			// target non raggiungibile dalla source
			return null;
		}
		
		List<Fermata> percorso = new LinkedList<>();
		Fermata f = target;
		
		while(f != null) {
			percorso.add(0, f);
			f = backVisit.get(f);
		}
		
		return percorso;
	}

	public Graph<Fermata, DefaultWeightedEdge> getGrafo() {
		return grafo;
	}

	public List<Fermata> getFermate() {
		return fermate;
	}
	
	public List<Fermata> trovaCamminoMinimo(Fermata partenza, Fermata arrivo) {
		DijkstraShortestPath<Fermata, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(this.grafo);
		GraphPath<Fermata, DefaultWeightedEdge> path = dijkstra.getPath(partenza, arrivo);
		
		return path.getVertexList();
	}
	
}
