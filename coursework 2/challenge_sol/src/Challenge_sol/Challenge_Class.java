package Challenge_sol;
import java.io.*;
import java.util.*;

public class Challenge_Class {
    public static void main(String[] args) {
        //create an array to store the values of the dataset
        String[] dataset = {
                "b_lovely_landscapes.txt",
                "c_memorable_moments.txt",
                "d_pet_pictures.txt",
                "e_shiny_selfies.txt",
        };
        for (String datasetFile : dataset) {
            solveDataset(datasetFile);
        }
    }
    //create function to calculate best interest pairing for both
    static int interest(Slide a, Slide b){
        //stores a copy of slide a's tags and only keep common tags from b
        Set<String> common = new HashSet<>(a.tags);
        common.retainAll(b.tags);
        //keep tags that appear in a but not b
        Set<String> onlyA = new HashSet<>(a.tags);
        onlyA.removeAll(b.tags);
        //keep tags that appear in b not a
        Set<String> onlyB = new HashSet<>(b.tags);
        onlyB.removeAll(a.tags);
        return Math.min(common.size(), Math.min(onlyA.size(), onlyB.size()));
    }
    //solver for each dataset
    public static void solveDataset(String datasetFile) {
        try {
            //create a new list for the txt files
            List<Photo> photos = readInput(datasetFile);
            //create new lists for horizontal and vertical photos
            List<Photo> hPhotos = new ArrayList<>();
            List<Photo> vPhotos = new ArrayList<>();
            for (Photo photo : photos) {
                if (photo.orientation == 'H') {
                    hPhotos.add(photo);
                } else {
                    vPhotos.add(photo);
                }
            }
            List<Slide> slides = pair_vPhotos(vPhotos);
            add_Hslides(slides, hPhotos);
            //implement tag builder function to order the pictures
            Map<String, List<Integer>> tagIndex = buildTagIndex(slides);
            //builds the slideshow ready for output
            List<Integer> slideshow = buildSlideshow(slides, tagIndex);
            //outputs slideshow
            String outputFile = datasetFile + "_slideshow.txt";
            writeOutput(outputFile, slideshow, slides);
            System.out.println("built " + datasetFile);
            //https://www.w3schools.com/java/java_map.asp
            //https://stackoverflow.com/questions/8231631/creating-a-simple-index-on-a-text-file-in-java
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static List<Photo> readInput(String filename) throws IOException{
        List<Photo> photos = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        //first line will contain the total number of photos
        int n = Integer.parseInt(reader.readLine().trim());
        for (int i = 0; i < n; i++) {
            //parses the txt files to read orientation
            String[] parts = reader.readLine().trim().split(" ");
            char orientation = parts[0].charAt(0);
            //parses txt file to read tags
            int M = Integer.parseInt(parts[1].trim());
            Set <String> tags = new HashSet<>();
            for (int j = 0; j < M; j++) {
                tags.add(parts[2 + j].trim());
            }
            //creates new value and adds it to array photos
            photos.add(new Photo(i, orientation, tags));
            }
        reader.close();
        return photos;
    }
    //add_Hslides to create separate slide for horizontal photos
    static void add_Hslides(List<Slide> slides, List<Photo> hPhotos) {
        for (Photo photo : hPhotos) {
            slides.add(new Slide(
                    Arrays.asList(photo.id),
                    new HashSet<>(photo.tags)));
        }
    }
    //pairs vertical photos comparing tags
    static List<Slide> pair_vPhotos(List<Photo> verticals){
        //.sort to sort by number of tags
        verticals.sort(Comparator.comparingInt(p -> p.tags.size()));
        boolean[] used = new boolean[verticals.size()];
        List<Slide> slides = new ArrayList<>();
        //search algorithm with a limit of 50 values before or after selected value
        int K = 50;
        for (int i = 0; i < verticals.size(); i++) {
            if (used[i]) continue;
            int bestJ = -1;
            //overlap used to pair tags based on common and uncommon
            int bestOverlap = Integer.MAX_VALUE;
            for (int j = i + 1; j < Math.min(i + K, verticals.size()); j++){
                if(used[j]) continue;
                Set<String> temp = new HashSet<>(verticals.get(i).tags);
                temp.retainAll(verticals.get(j).tags);
                int overlap = temp.size();
                if (overlap < bestOverlap){
                    bestOverlap = overlap;
                    bestJ = j;
                }
            }
            if(bestJ != -1){
                used[i] = true;
                used[bestJ] = true;
                //combines the tags of the photos
                Set<String> combinedTags = new HashSet<>(verticals.get(i).tags);
                combinedTags.addAll(verticals.get(bestJ).tags);
                //adds the v photos to a slide
                slides.add(new Slide(
                        Arrays.asList(verticals.get(i).id, verticals.get(bestJ).id),
                                combinedTags));
            }
        }
        return slides;
    }
    //tag index builder
    static Map<String, List<Integer>> buildTagIndex(List<Slide> slides){
        Map<String, List<Integer>> tagIndex = new HashMap<>();
        for (int i = 0; i < slides.size(); i++){
            //if tag isnt in index yet create new index and add it
            for (String tag : slides.get(i).tags){
                tagIndex.computeIfAbsent(tag, k -> new ArrayList<>()).add(i);
            }
        }
        return tagIndex;
        //https://www.w3schools.com/java/ref_hashmap_computeifabsent.asp
    }
    //builds the slideshow
    static List<Integer> buildSlideshow(List<Slide> slides, Map<String, List<Integer>> tagIndex) {
        //new hashset for unused slides
        Set<Integer> unused = new HashSet<>();
        for (int i = 0; i < slides.size(); i++)
            unused.add(i);
        List<Integer> slideshow = new ArrayList<>();
        int current = unused.iterator().next();
        unused.remove(current);
        slideshow.add(current);
        int C = 100;
        //find slides with common tags
        while (!unused.isEmpty()) {
            Set<Integer> candidates = new HashSet<>();
            for (String tag : slides.get(current).tags) {
                List<Integer> list = tagIndex.get(tag);
                if (list != null) {
                    for (int s : list) {
                        if (unused.contains(s))
                            candidates.add(s);
                    }
                }
            }
            //if too many slides, then randomly sample
            List<Integer> candidateList = new ArrayList<>(candidates);
            if (candidateList.size() > C) {
                Collections.shuffle(candidateList);
                candidateList = candidateList.subList(0, C);
            }
            //find slides with highest score
            int bestSlide = -1;
            int bestScore = -1;
            for (int s : candidateList) {
                int score = interest(slides.get(current), slides.get(s));
                if (score > bestScore) {
                    bestScore = score;
                    bestSlide = s;
                }
            }
            if (bestSlide == -1) {
                bestSlide = unused.iterator().next();
            }
            slideshow.add(bestSlide);
            unused.remove(bestSlide);
            current = bestSlide;
        }
        return slideshow;
    }
    //creates the output
    static void writeOutput(String filename, List<Integer> slideshow, List<Slide> slides) throws Exception {
        //buffered writer to write the slideshow
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
        bw.write(String.valueOf(slideshow.size()));
        bw.newLine();
        for (int s : slideshow) {
            Slide slide = slides.get(s);
            //horizontal slides have 1 tag, vertical have 2
            if (slide.photoIds.size() == 1) {
                bw.write(String.valueOf(slide.photoIds.get(0)));
            } else {
                bw.write(slide.photoIds.get(0) + " " + slide.photoIds.get(1));
            }
            bw.newLine();
            }
        bw.close();
    }
}








