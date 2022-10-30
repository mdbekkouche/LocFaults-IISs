package validation;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class InterfacelocFaults {

	// Déclaration
	JFrame fenetre;
	JPanel pano;
	
	//Constructeur de la classe
	public InterfacelocFaults(){
		fenetre = new JFrame("LocFaults tool");
		fenetre.setBounds(300, 300, 410, 150);
		
		pano = new JPanel();
		pano.setLayout(new GridLayout(1,1));
		
		fenetre.add(pano);
		fenetre.setVisible(true);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new InterfacelocFaults();

	}

}
