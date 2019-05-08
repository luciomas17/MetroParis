package it.polito.tdp.metroparis.model;

public class TestModel {

	public static void main(String[] args) {
		
		Model model = new Model();
		
		model.creaGrafo();
		
		System.out.println(model.getGrafo());
		
	}

}
