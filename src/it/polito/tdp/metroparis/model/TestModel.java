package it.polito.tdp.metroparis.model;

import java.util.List;

public class TestModel {

	public static void main(String[] args) {
		
		Model model = new Model();
		
		model.creaGrafo();
		
		// System.out.println(model.getGrafo());
		System.out.format("Creati %d vertici e %d archi\n", model.getGrafo().vertexSet().size(), model.getGrafo().edgeSet().size());
		
		Fermata source = model.getFermate().get(0);
		System.out.println("Parto da: " + source);
		List<Fermata> raggiungibili = model.fermateRaggiungibili(source);
		System.out.println("Fermate raggiunte: " + raggiungibili + "\ndim: " + raggiungibili.size());
		
		Fermata target = model.getFermate().get(150);
		List<Fermata> percorso = model.percorsoFinoA(target);
		System.out.println("Arrivo a: " + target);
		System.out.println(percorso);
	}

}
