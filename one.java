import java.util.*;

/**
 * City Route Finder (PS 88)
 * Complete console application contained in this one source file.
 */
public class one {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final RouteGraph GRAPH = new RouteGraph();

    public static void main(String[] args) {
        GRAPH.addRoad("Durg", "Bhilai", 10);
        GRAPH.addRoad("Durg", "Raipur", 35);
        GRAPH.addRoad("Bhilai", "Raipur", 30);
        GRAPH.addRoad("Raipur", "Bilaspur", 115);

        System.out.println("=== City Route Finder (PS 88) ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readNonEmptyLine("Choose an option: ");
            switch (choice) {
                case "1":
                    findRoute();
                    break;
                case "2":
                    changeRoadStatus(true);
                    break;
                case "3":
                    changeRoadStatus(false);
                    break;
                case "4":
                    checkPathWithBfs();
                    break;
                case "5":
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1, 2, 3, 4, or 5.");
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("1. Find the shortest route");
        System.out.println("2. Block a road");
        System.out.println("3. Unblock a road");
        System.out.println("4. Check whether a path exists (BFS)");
        System.out.println("5. Exit");
    }

    private static void findRoute() {
        String source = readNonEmptyLine("Enter starting city: ");
        String destination = readNonEmptyLine("Enter destination city: ");
        GRAPH.findRoute(source, destination);
    }

    private static void changeRoadStatus(boolean block) {
        String firstCity = readNonEmptyLine("Enter the first city: ");
        String secondCity = readNonEmptyLine("Enter the second city: ");
        if (block) {
            GRAPH.blockRoad(firstCity, secondCity);
        } else {
            GRAPH.unblockRoad(firstCity, secondCity);
        }
    }

    private static void checkPathWithBfs() {
        String source = readNonEmptyLine("Enter starting city: ");
        String destination = readNonEmptyLine("Enter destination city: ");
        GRAPH.checkPathWithBfs(source, destination);
    }

    private static String readNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!SCANNER.hasNextLine()) {
                System.out.println("\nInput ended. Goodbye!");
                System.exit(0);
            }
            String value = SCANNER.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input cannot be blank. Please try again.");
        }
    }

    /** Weighted undirected graph with HashMap city lookup and adjacency lists. */
    private static class RouteGraph {
        // Normalized names make lookup average O(1) and case-insensitive.
        private final HashMap<String, City> citiesByName = new HashMap<>();
        private final Set<RoadKey> blockedRoads = new HashSet<>();

        private void addRoad(String firstName, String secondName, int distance) {
            City first = getOrCreateCity(firstName);
            City second = getOrCreateCity(secondName);
            first.adjacentRoads.add(new Edge(second, distance));
            second.adjacentRoads.add(new Edge(first, distance));
        }

        /** Runs Dijkstra with a priority queue to find the minimum-distance route. */
        private void findRoute(String sourceInput, String destinationInput) {
            City source = findCity(sourceInput);
            if (source == null) {
                printUnknownCity(sourceInput);
                return;
            }
            City destination = findCity(destinationInput);
            if (destination == null) {
                printUnknownCity(destinationInput);
                return;
            }
            if (source == destination) {
                System.out.println("Route: " + source.name);
                System.out.println("Distance: 0 km");
                return;
            }

            Map<City, Integer> distances = new HashMap<>();
            Map<City, City> previousCities = new HashMap<>();
            PriorityQueue<PathState> queue = new PriorityQueue<>(
                    Comparator.comparingInt(state -> state.distance));
            for (City city : citiesByName.values()) {
                distances.put(city, Integer.MAX_VALUE);
            }
            distances.put(source, 0);
            queue.add(new PathState(source, 0));

            while (!queue.isEmpty()) {
                PathState current = queue.poll();
                if (current.distance != distances.get(current.city)) {
                    continue; // Skip an outdated entry in the priority queue.
                }
                if (current.city == destination) {
                    break;
                }

                for (Edge edge : current.city.adjacentRoads) {
                    if (isBlocked(current.city, edge.destination)) {
                        continue; // Blocked roads are never used by Dijkstra.
                    }
                    int newDistance = current.distance + edge.distance;
                    if (newDistance < distances.get(edge.destination)) {
                        distances.put(edge.destination, newDistance);
                        previousCities.put(edge.destination, current.city);
                        queue.add(new PathState(edge.destination, newDistance));
                    }
                }
            }

            if (distances.get(destination) == Integer.MAX_VALUE) {
                printNoRoute(source, destination);
                return;
            }

            LinkedList<String> route = new LinkedList<>();
            City city = destination;
            while (city != null) {
                route.addFirst(city.name);
                if (city == source) {
                    break;
                }
                city = previousCities.get(city);
            }
            System.out.println("Route: " + String.join(" -> ", route));
            System.out.println("Distance: " + distances.get(destination) + " km");
        }

        /** BFS uses a FIFO queue to check connectivity, not distance. */
        private void checkPathWithBfs(String sourceInput, String destinationInput) {
            City source = findCity(sourceInput);
            if (source == null) {
                printUnknownCity(sourceInput);
                return;
            }
            City destination = findCity(destinationInput);
            if (destination == null) {
                printUnknownCity(destinationInput);
                return;
            }
            if (source == destination || hasPathWithBfs(source, destination)) {
                System.out.println("A path exists from " + source.name + " to " + destination.name + ".");
            } else {
                printNoRoute(source, destination);
            }
        }

        private boolean hasPathWithBfs(City source, City destination) {
            Queue<City> queue = new ArrayDeque<>();
            Set<City> visited = new HashSet<>();
            queue.add(source);
            visited.add(source);

            while (!queue.isEmpty()) {
                City current = queue.remove();
                for (Edge edge : current.adjacentRoads) {
                    if (isBlocked(current, edge.destination)) {
                        continue; // BFS also ignores blocked roads.
                    }
                    if (edge.destination == destination) {
                        return true;
                    }
                    if (visited.add(edge.destination)) {
                        queue.add(edge.destination);
                    }
                }
            }
            return false;
        }

        private void blockRoad(String firstInput, String secondInput) {
            City first = findCity(firstInput);
            if (first == null) {
                printUnknownCity(firstInput);
                return;
            }
            City second = findCity(secondInput);
            if (second == null) {
                printUnknownCity(secondInput);
                return;
            }
            if (!roadExists(first, second)) {
                System.out.println("Road " + first.name + " - " + second.name + " does not exist in the network.");
                return;
            }
            if (!blockedRoads.add(new RoadKey(first, second))) {
                System.out.println("Road " + first.name + " - " + second.name + " is already blocked.");
                return;
            }
            System.out.println("Road " + first.name + " - " + second.name + " has been blocked.");
        }

        private void unblockRoad(String firstInput, String secondInput) {
            City first = findCity(firstInput);
            if (first == null) {
                printUnknownCity(firstInput);
                return;
            }
            City second = findCity(secondInput);
            if (second == null) {
                printUnknownCity(secondInput);
                return;
            }
            if (!roadExists(first, second)) {
                System.out.println("Road " + first.name + " - " + second.name + " does not exist in the network.");
                return;
            }
            if (!blockedRoads.remove(new RoadKey(first, second))) {
                System.out.println("Road " + first.name + " - " + second.name + " is not blocked.");
                return;
            }
            System.out.println("Road " + first.name + " - " + second.name + " has been unblocked.");
        }

        private City getOrCreateCity(String name) {
            String key = normalize(name);
            City city = citiesByName.get(key);
            if (city == null) {
                city = new City(name.trim());
                citiesByName.put(key, city);
            }
            return city;
        }

        private City findCity(String name) {
            return citiesByName.get(normalize(name));
        }

        private String normalize(String name) {
            return name.trim().toLowerCase(Locale.ROOT);
        }

        private boolean roadExists(City first, City second) {
            for (Edge edge : first.adjacentRoads) {
                if (edge.destination == second) {
                    return true;
                }
            }
            return false;
        }

        private boolean isBlocked(City first, City second) {
            return blockedRoads.contains(new RoadKey(first, second));
        }

        private void printUnknownCity(String name) {
            System.out.println("City " + name.trim() + " not found in the network.");
        }

        private void printNoRoute(City source, City destination) {
            System.out.println("No available route from " + source.name + " to " + destination.name + ".");
        }
    }

    /** One city vertex and its adjacency list. */
    private static class City {
        private final String name;
        private final List<Edge> adjacentRoads = new ArrayList<>();

        private City(String name) {
            this.name = name;
        }
    }

    /** Neighbor and distance stored in a city's adjacency list. */
    private static class Edge {
        private final City destination;
        private final int distance;

        private Edge(City destination, int distance) {
            this.destination = destination;
            this.distance = distance;
        }
    }

    /** City and tentative distance stored in Dijkstra's priority queue. */
    private static class PathState {
        private final City city;
        private final int distance;

        private PathState(City city, int distance) {
            this.city = city;
            this.distance = distance;
        }
    }

    /** Unordered road key so a road is the same in either direction. */
    private static class RoadKey {
        private final String first;
        private final String second;

        private RoadKey(City firstCity, City secondCity) {
            String firstName = firstCity.name.toLowerCase(Locale.ROOT);
            String secondName = secondCity.name.toLowerCase(Locale.ROOT);
            if (firstName.compareTo(secondName) <= 0) {
                first = firstName;
                second = secondName;
            } else {
                first = secondName;
                second = firstName;
            }
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof RoadKey)) return false;
            RoadKey road = (RoadKey) other;
            return first.equals(road.first) && second.equals(road.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}
