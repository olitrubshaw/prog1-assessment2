package Challenge_sol;

import java.util.Set;

//initialises photo variables for main class
public class Photo {
    int id;
    char orientation;
    Set<String> tags;
    Photo(int id, char orientation, Set<String> tags) {
        this.id = id;
        this.orientation = orientation;
        this.tags = tags;
    }
}
