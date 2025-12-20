package stud.g12;

import java.util.HashSet;

@SuppressWarnings("serial")
public class RoadSet extends HashSet<Road> {
	//继承了HashSet之后，addRoad和removeRoad
	//方法的效率更高，速度加快了
	public void addRoad(Road road) {
		add(road);
	}
	public void removeRoad(Road road) {
		remove(road);
	}
}
