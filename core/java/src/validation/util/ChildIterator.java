package validation.util;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import exception.AnalyzeException;
import validation.Validation.VerboseLevel;

/* une classe pour avancer facilement dans les enfants d'�l�ments */
// NOTA : chaque retour de ligne du fichier xml g�n�re un tag de type TEXT_NODE
//      : il faut sauter les tag de fin

public class ChildIterator implements Cloneable{
    private int i;
    private NodeList child;
    private int size;

    private ChildIterator(int i, NodeList child, int size) {
    	this.i = i;
    	this.child=child;
    	this.size=size;
    }

    public ChildIterator(Node c) {
    	this(-1,c.getChildNodes(),c.getChildNodes().getLength());
    }
   
    
    
    // ---------------
public Object clone() {
	return new ChildIterator(i,child,size);
}

    // pour avancer au prochain element
    // on ne s'int�resse qu'au type comment ou �l�ment
    // on affiche simplement les commentaires en echo 
    public Node next() throws AnalyzeException {
	i++;
	Node n=null; ;
	while (i<size&& child.item(i).getNodeType()!=Node.ELEMENT_NODE) {
	    n=child.item(i);
	    if (n.getNodeType()==Node.COMMENT_NODE)
		echoComment(n);
	    i++;
	}
	if (i<size) n=child.item(i);
	return n;
    }

    // pour avancer sur la prochaine donnee
    public Node nextData() throws AnalyzeException {
	i++;
	Node n= null;
	while (i<size&& child.item(i).getNodeType()!=Node.TEXT_NODE) {
	    i++;
	}
	if (i<size) n=child.item(i);
	return n;
    }

    // pour savoir s'il y a encore des fils de type ELEMENT_NODE
    public boolean hasMoreChild() {
	int j = i;
	j++;
	while (j<size&& child.item(j).getNodeType()!=Node.ELEMENT_NODE) {
	    j++;
	}
	return j<size;
    }
				   
   // pour afficher les commentaires du source xml
    private void echoComment(Node n) {
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("comment line : " +  n.getNodeValue());
		}
    }
    
    public String toString() {
    	int j = i;
    	j++;
    	while (j<size&& child.item(j).getNodeType()!=Node.ELEMENT_NODE) {
    	    j++;
    	}
    	if (j<size) 
    		return child.item(j).getNodeName();
    	return "no more node";
    
     
    }

}
