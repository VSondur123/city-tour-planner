package fi.citytour.planner.model.traffic;

import java.util.List;

/** Represents the shape of an area on the map as a list of paths. */
public record GeoShape(List<List<Coordinate>> paths) {}
