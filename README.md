# City Route Finder (PS 88)

A Java console application that finds routes between cities. The complete Java implementation is in one source file: `one.java`. It uses only the Java standard library.

## Requirements

- JDK 8 or newer
- VS Code or another editor with a terminal
- No external libraries

## Files

- `one.java` — menu, graph, city and road classes, Dijkstra shortest-route search, BFS connectivity search, and road blocking/unblocking.
- `out/` — generated Java `.class` files after compilation. This folder is not source code.

The main class is named `one` to match the required filename `one.java`.

## Initial road network

| Road | Distance |
| --- | ---: |
| Durg – Bhilai | 10 km |
| Durg – Raipur | 35 km |
| Bhilai – Raipur | 30 km |
| Raipur – Bilaspur | 115 km |

Roads are undirected, so each can be travelled either way.

## Concepts and features

- **Graph and adjacency list:** each city stores its neighboring cities and the distance to each one.
- **HashMap lookup:** cities are stored in a `HashMap<String, City>` using lowercase keys, so lookup is case-insensitive and does not scan all city names.
- **Dijkstra's algorithm:** menu option 1 uses a priority queue to find the route with the lowest total distance.
- **BFS:** menu option 4 uses a queue to check whether any path exists. It checks connectivity and does not calculate distance.
- **Road blocking:** menu options 2 and 3 block or unblock an existing road. Dijkstra and BFS both ignore blocked roads.
- Unknown cities, blank input, and same-city requests are handled.

Blocked roads are remembered only while the program is running. Restarting the program restores the initial roads.

## Run from VS Code on Windows

Open the project folder in VS Code, choose **Terminal → New Terminal**, then run these commands in PowerShell:

```powershell
cd "C:\Users\USER\Desktop\prj.java"
New-Item -ItemType Directory -Force out | Out-Null
javac -d out one.java
java -cp out one
```

If your terminal is already in the project folder, skip the `cd` command. If `javac` or `java` is not recognized, install a JDK and add its `bin` directory to PATH.

## Menu

```text
1. Find the shortest route
2. Block a road
3. Unblock a road
4. Check whether a path exists (BFS)
5. Exit
```

For route lookup or BFS, enter a starting city and destination. To block or unblock a road, enter the two connected city names in either order.

## Examples

With no roads blocked, option 1 from Durg to Bilaspur displays:

```text
Route: Durg -> Raipur -> Bilaspur
Total distance: 150 km
```

After blocking Durg–Raipur, the route changes to:

```text
Route: Durg -> Bhilai -> Raipur -> Bilaspur
Total distance: 155 km
```

Option 4 reports whether a path exists using the roads that are currently available.

## Verification

Compiled with JDK 25.0.2 and ran the console program. Verified the 150 km shortest route, the 155 km route after blocking Durg–Raipur, route restoration after unblocking, reachable and unreachable BFS cases, mixed-case city names, unknown cities, and same-city requests.
