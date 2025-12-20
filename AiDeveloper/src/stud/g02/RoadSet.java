package stud.g02;

import java.util.HashSet;

@SuppressWarnings("serial")
public class RoadSet extends HashSet<Road> {
	public void addRoad(Road road) {
		add(road);
	}
	public void removeRoad(Road road) {
		remove(road);
	}
}
