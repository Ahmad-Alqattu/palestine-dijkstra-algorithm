package application;

import java.util.Comparator;

public class TableCompare implements Comparator<Node>{

	@Override
	public int compare(Node t1, Node t2) {
		System.out.println(t1.getSourceCity().getName()+t1.getCurrentCity().getName()+" d"+t1.getDistance());
		System.out.println(t2.getSourceCity().getName()+t2.getCurrentCity().getName()+" d"+t2.getDistance());
		System.out.println( (t1.getDistance() - t2.getDistance()));

		
		return (int) (t1.getDistance() - t2.getDistance());
	}

}
