package Challenge_sol;

import java.util.List;
import java.util.*;

//initialises variables for slide in main class
public class Slide {
    //creates lists for variables to be stored in
    List<Integer> photoIds;
    Set<String> tags;

    Slide(List<Integer> photoIds, Set<String> tags) {
        this.photoIds = photoIds;
        this.tags = tags;
    }
}
