package eu.atanasio.catastrophe.middleware;

import eu.atanasio.catastrophe.model.Cleaner;
import eu.atanasio.catastrophe.model.Drone;
import eu.atanasio.catastrophe.model.Rubble;
import eu.atanasio.catastrophe.model.Waypoint;
import eu.atanasio.catastrophe.singletons.PointMap;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by victorperez on 16/04/17.
 */
public class TroubleMaker {
    public static void make() {
        PointMap map = PointMap.getInstance();
        List<Cleaner> cleaners = map.getListfromParticipants(Cleaner.class);
        List<Drone> drones = map.getListfromParticipants(Drone.class);
        List<Rubble> rubbles = map.getListfromParticipants(Rubble.class);
        List<Cleaner> cleanersEnd = map.getListfromFinal(Cleaner.class);
        List<Drone> dronesEnd = map.getListfromFinal(Drone.class);
        List<Rubble> rubblesEnd = map.getListfromFinal(Rubble.class);
        String out = "(define (problem pickup1234) (:domain Nuclear)\n" +
                "(:objects\n";
        for (Cleaner c : cleaners){
            out += "        (:private " + c.getName() + "\n" +
                    "            " + c.getName() + " - cleaner\n" +
                    "        )\n";
        }
        for (Drone d : drones) {
            out += "        (:private " + d.getName() + "\n" +
                    "            " + d.getName() + " - drone\n" +
                    "        )\n";
        }
        for (Waypoint w : map.getWaypoints()) {
            if (w.isDump())
                out += "        " + w.getId() + " - dump\n";
            else
                out += "        " + w.getId() + " - waypoint\n";
        }
        for (Rubble r : rubbles) {
            out += "        " + r.getName() + " - rubble\n";
        }
        out += ")\n" +
                "(:init\n";
        for (Drone d : drones) {
            out += "        (is_active " + d.getName() + ")\n" +
                    "        (at " + d.getName();
            for (Waypoint w : map.getWaypoints()) {
                if (d.getPosition().equals(w))
                    out += " " + w.getId() + ")\n";
            }
            if (d.isBroken()){
                out += "        (is_broken " + d.getName() + ")\n";
            }
            else {
                out += "        (is_active " + d.getName() + ")\n";
            }
        }
        for (Cleaner c : cleaners) {
            if (c.getCargo() == null){
                out += "        (empty " + c.getName() + ")\n" ;
            }
            else {
                out += "        (full " + c.getCargo().getName() + " " + c.getName() + ")\n" ;
            }
            out += "        (is_active " + c.getName() + ")\n" +
                    "        (at " + c.getName();
            for (Waypoint w : map.getWaypoints()) {
                if (c.getPosition().equals(w))
                    out += " " + w.getId() + ")\n";
            }
            if (c.isBroken()){
                out += "        (is_broken " + c.getName() + ")\n";
            }
            else {
                out += "        (is_active " + c.getName() + ")\n";
            }
        }
        for (Rubble r : rubbles) {
            out += "        (at " + r.getName();
            for (Waypoint w : map.getWaypoints()) {
                if (r.getPosition().equals(w))
                    out += " " + w.getId() + ")\n";
            }
            if (r.isAssessed() && r.isRadioactive()) {
                out += "        (is_radioactive " + r.getName() + ")\n";
            }
        }
        for (Waypoint x : map.getWaypoints()) {
            for (Waypoint y : map.getWaypoints()) {
                if (x.getConnectedWaypoints().contains(y))
                    out += "        (visible " + x.getId() + " " + y.getId() + ")\n";
                if (x.getConnectedWaypointsByFlight().contains(y))
                    out += "        (traversable_flight " + x.getId() + " " + y.getId() + ")\n";
                if (x.getConnectedWaypointsByLand().contains(y))
                    out += "        (traversable_land " + x.getId() + " " + y.getId() + ")\n";
            }
        }
        out += ")\n" +
                "(:goal (and\n";
        for (Cleaner c : cleanersEnd) {
            out += "            (at " + c.getName();
            for (Waypoint w : map.getWaypoints()) {
                if (c.getPosition().equals(w))
                    out += " " + w.getId() + ")\n";
            }
        }
        for (Drone d : dronesEnd) {
            out += "            (at " + d.getName();
            for (Waypoint w : map.getWaypoints()) {
                if (d.getPosition().equals(w))
                    out += " " + w.getId() + ")\n";
            }
        }
        for (Rubble r : rubbles) {
            if (!r.isAssessed()){
                out += "            (assessed " + r.getName() + ")\n";
            }
            else if (r.isRadioactive()){
                out += "            (is_clean " + r.getName() + ")\n";
            }
            else { //Is assessed but not radioactive
                out += "            (at " + r.getName();
                for (Waypoint w : map.getWaypoints()) {
                    if (rubblesEnd.get(rubbles.indexOf(r)).getPosition().equals(w))
                        out += " " + w.getId() + ")\n";
                }
            }
        }
        out += "       )\n" +
                ")\n" +
                ")\n";
        try {
            PrintWriter printer = new PrintWriter("/home/victorperez/ATA/output/p01.pddl");
            printer.print(out);
            printer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
