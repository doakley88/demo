package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


public class ImageUtils {

    private static final int SIMILARITY_SCORE_THRESHOLD = 50;
    private static final int MAX_WIDTH = 720;
    private static final int MAX_HEIGHT = 720;

    static Logger log = LoggerFactory.getLogger(ImageUtils.class);
    /*
    Helper that compares two images of the same dimensions
     */
    public static int getSimilarityScore(BufferedImage src, BufferedImage tar) {
        if ( src.getWidth() != tar.getWidth() || src.getHeight() != tar.getHeight()) {
            throw new IllegalArgumentException("Need to be images of the same size\n" +
                    "\twidth " + src.getWidth()+ " " + tar.getWidth() +
                    "\n\theight " + src.getHeight() + " " + tar.getHeight());
        }
        int similarityScore = 0;

        //As a minor optimization, we're only sampling each fragment in our comparisons.
        //Early results still seem like decent matches, but keep an eye on this
        for(int i = src.getWidth() - 1; i >= 0; i -= 2) {
            for (int j = src.getHeight() - 1 ; j >=0; j -=2) {
                Color srcColor = new Color(src.getRGB(i, j));
                Color tarColor = new Color(tar.getRGB(i, j));
                similarityScore += Math.abs(srcColor.getRed() - tarColor.getRed());
                similarityScore += Math.abs(srcColor.getGreen() - tarColor.getGreen());
                similarityScore += Math.abs(srcColor.getBlue() - tarColor.getBlue());

            }
        }
        similarityScore = similarityScore/(src.getWidth() * src.getHeight());
        return similarityScore;
    }

    /**
     * Constructs a representation of the 'model' image using only fragments found in the source
     * @param source
     * @param model
     * @param fragWidth
     * @param fragHeight
     * @return
     */
    public static BufferedImage remakeImage(BufferedImage source, BufferedImage model, int fragWidth, int fragHeight) {
        //If the model is too big for us to process quickly, scale it down
        BufferedImage normalizedModel = normalizeModel(model);
        //If the two images are different sizes, scale the source to the model
        BufferedImage transformedSource = transformSource(source, normalizedModel);

        BufferedImage result = new BufferedImage(normalizedModel.getWidth(), normalizedModel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        ArrayList<ArrayList<BufferedImage>> sourceFrags = breakdownImage(transformedSource, fragWidth, fragHeight);
        int columns = normalizedModel.getWidth()/fragWidth;
        int rows = normalizedModel.getHeight()/fragHeight;
        //The outer two loops fill one fragment each of the result image
        for(int i = columns - 1; i > 0; i--) {
            for(int j = rows - 1; j>0; j--) {
                //find a source fragment that looks close to our model fragment
                BufferedImage modelFrag = normalizedModel.getSubimage(i*fragWidth, j*fragHeight, fragWidth, fragHeight);
                BufferedImage goodMatch = null;
                int similarity = Integer.MAX_VALUE;
                int matchX = -1;
                int matchY = -1;
                //these two more loops (ugh jesus christ) find the best match fragment from our source
                boolean breakout = false;
                for (int k = 0; k <sourceFrags.size(); k++) {
                    if(breakout) {
                        break;
                    }
                    ArrayList<BufferedImage> inner = sourceFrags.get(k);
                    for (int l = 0; l < inner.size(); l++) {
                        BufferedImage temp = inner.get(l);
                        int tempSim = getSimilarityScore(temp, modelFrag);
                        if (tempSim > 0 && tempSim < similarity) {
                            goodMatch = temp;
                            matchX = k;
                            matchY = l;
                            similarity = tempSim;
                            if(similarity < SIMILARITY_SCORE_THRESHOLD) {
                                //preemptively declare victory instead of trying for a better match
                                breakout = true;
                                break;
                            }
                        }

                    }
                }
                graphics.drawImage(goodMatch, null, i * fragWidth, j * fragHeight);
                //Remove the fragment we found so we don't use it again
                sourceFrags.get(matchX).remove(matchY);
                if (sourceFrags.get(matchX).size() == 0) {
                    sourceFrags.remove(matchX);
                }
            }
        }
        return result;
    }


    /**
     * Break down a source image into fragments.
     * Using a 2D ArrayList so we can more easily pluck fragments later while composing the new image
     * @param source the source image
     * @param fragWidth the width of each fragment
     * @param fragHeight the height of each fragment
     * @return imageFragments, the ArrayList of ArrayLists containing our new fragments
     */
    public static ArrayList<ArrayList<BufferedImage>> breakdownImage(BufferedImage source, int fragWidth, int fragHeight) {
        int numColumns = source.getWidth()/fragWidth;
        int numRows = source.getHeight()/fragHeight;
        ArrayList<ArrayList<BufferedImage>> imageFragments = new ArrayList<ArrayList<BufferedImage>>();

        for(int i = 0; i < numColumns; i ++) {
            imageFragments.add(new ArrayList<>());
            for(int j = 0; j <numRows; j++) {
                BufferedImage frag = source.getSubimage(i * fragWidth, j * fragHeight, fragWidth, fragHeight);
                imageFragments.get(i).add(j, frag);
            }
        }
        return imageFragments;
    }

    //Helper to scale the source image to the model's dimensions
    public static BufferedImage transformSource(BufferedImage source, BufferedImage model) {
        //just give em the basics if we're already the same size
        if (source.getWidth() == model.getWidth() && source.getHeight() == model.getHeight()) {
            return source;
        }
        BufferedImage result = new BufferedImage(model.getWidth(), model.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        graphics.drawImage(source, 0, 0, model.getWidth(), model.getHeight(), null);
        return result;

    }
    //Helper to ensure that the model image isn't too big for us. Should try to make outdated at some point.
    public static BufferedImage normalizeModel(BufferedImage model) {
        BufferedImage result;
        if(model.getWidth() > MAX_WIDTH) {
            int scaledHeight = (MAX_WIDTH*model.getHeight())/model.getWidth();
            result = new BufferedImage(MAX_WIDTH, scaledHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = result.createGraphics();
            graphics.drawImage(model, 0, 0, MAX_WIDTH, scaledHeight, null);
            return normalizeModel(result);
        } else if(model.getHeight() > MAX_HEIGHT) {
            int scaledWidth = ((MAX_HEIGHT*model.getWidth())/model.getHeight());
            result = new BufferedImage(scaledWidth, MAX_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = result.createGraphics();
            graphics.drawImage(model, 0,0, scaledWidth, MAX_HEIGHT, null);
            return result;
        }
        return model;
    }
}

