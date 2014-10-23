/*
 * This file is part of 2048, licensed under the MIT License (MIT).
 *
 * Copyright (c) NeatMonster <neatmonster@hotmail.fr>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.neatmonster.genetics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Candidate implements Comparable {
    private static final int             SIZE       = 64;
    private static final Random          rnd        = new Random();
    private static final List<Candidate> population = new ArrayList<Candidate>();
    private static final Pixel[][]       src        = new Pixel[512][512];
    private static       int             generation = 1;

    static {
        try {
            final BufferedImage img = ImageIO.read(Candidate.class.getResourceAsStream("/assets/cherry.png"));
            for (int x = 0; x < 512; x++)
                for (int y = 0; y < 512; y++) {
                    final Color color = new Color(img.getRGB(x, y));
                    src[x][y] = new Pixel();
                    src[x][y].add(color.getRed(), color.getGreen(), color.getBlue());
                }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private final long[] chromosomes;
    private final Pixel[][]     pixels  = new Pixel[512][512];
    private       BufferedImage image   = null;
    private       int           fitness = -1;

    private Candidate(final long[] chromosomes) {
        this.chromosomes = chromosomes;
    }

    public static void init() {
        population.clear();
        for (int i = 0; i < 10; i++)
            population.add(Candidate.random());
        Collections.sort(population);
    }

    public static void evolve() {
        generation++;
        population.remove(population.size() - 1);
        population.remove(population.size() - 2);
        population.add(population.get(rnd.nextInt(population.size())).mutate());
        population.add(population.get(0).hybridize(population.get(1)));
        Collections.sort(population);
    }

    public static Candidate getCandidate(final int index) {
        return population.get(index);
    }

    public static int getGeneration() {
        return generation;
    }

    private static Candidate random() {
        final long[] chromosomes = new long[SIZE];
        for (int i = 0; i < SIZE; i++)
            chromosomes[i] = (long) (rnd.nextDouble() * 1152921504606846976L);
        return new Candidate(chromosomes);
    }

    public int getFitness() {
        if (fitness < 0) {
            fitness = 0;
            for (int i = 0; i < SIZE; i++) {
                final Rectangle rect = new Rectangle(chromosomes[i]);
                for (int x = Math.min(rect.x1, rect.x2); x < Math.max(rect.x1, rect.x2); x++)
                    for (int y = Math.min(rect.y1, rect.y2); y < Math.max(rect.y1, rect.y2); y++) {
                        if (pixels[x][y] == null)
                            pixels[x][y] = new Pixel();
                        pixels[x][y].add(rect.r, rect.g, rect.b);
                    }
            }
            for (int x = 0; x < 512; x++)
                for (int y = 0; y < 512; y++) {
                    final Pixel pixel = pixels[x][y];
                    final Pixel srcPixel = src[x][y];
                    if (pixel == null)
                        fitness += srcPixel.sum();
                    else
                        fitness += pixel.diff(srcPixel);
                }
        }
        return fitness;
    }

    public BufferedImage getImage() {
        if (image == null) {
            image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < 512; x++)
                for (int y = 0; y < 512; y++) {
                    if (pixels[x][y] == null)
                        pixels[x][y] = new Pixel();
                    image.setRGB(x, y, pixels[x][y].getColor().getRGB());
                }
        }
        return image;
    }

    private Candidate hybridize(final Candidate other) {
        String binary = "";
        String otherBinary = "";
        for (int i = 0; i < SIZE; i++) {
            binary += String.format("%60s", Long.toBinaryString(chromosomes[i])).replace(' ', '0');
            otherBinary += String.format("%60s", Long.toBinaryString(other.chromosomes[i])).replace(' ', '0');
        }
        final int position = rnd.nextInt(60 * SIZE);
        final String crossBinary = binary.substring(0, position) + otherBinary.substring(position, 60 * SIZE);
        final long[] crossChromosomes = new long[SIZE];
        for (int i = 0; i < SIZE; i++)
            crossChromosomes[i] = Long.valueOf(crossBinary.substring(i * 60, (i + 1) * 60), 2);
        return new Candidate(crossChromosomes);
    }

    private Candidate mutate() {
        final long[] mutateChromosomes = chromosomes.clone();
        mutateChromosomes[rnd.nextInt(SIZE)] ^= 1L << (long) (rnd.nextDouble() * 60L);
        return new Candidate(mutateChromosomes);
    }

    @Override
    public int compareTo(final Object o) {
        if (o instanceof Candidate)
            return getFitness() - ((Candidate) o).getFitness();
        return 0;
    }

    private static class Pixel {
        private int red   = 0;
        private int green = 0;
        private int blue  = 0;
        private int count = 0;

        private void add(final int red, final int green, final int blue) {
            this.red += red;
            this.green += green;
            this.blue += blue;
            count++;
        }

        private int diff(final Pixel pixel) {
            if (count > 0)
                return Math.abs(red / count - pixel.red) + Math.abs(green / count - pixel.green) + Math.abs(blue / count - pixel.blue);
            return pixel.red + pixel.green + pixel.blue;
        }

        private Color getColor() {
            if (count > 0)
                return new Color(red / count, green / count, blue / count);
            return new Color(0, 0, 0);
        }

        private int sum() {
            return red + green + blue;
        }
    }

    private static class Rectangle {
        private final int x1;
        private final int x2;
        private final int y1;
        private final int y2;
        private final int r;
        private final int g;
        private final int b;

        private Rectangle(final long chromosome) {
            x1 = (int) (chromosome % 512L);
            y1 = (int) (chromosome % 262144L / 512L);
            x2 = (int) (chromosome % 134217728L / 262144L);
            y2 = (int) (chromosome % 68719476736L / 134217728L);
            r = (int) (chromosome % 17592186044416L / 68719476736L);
            g = (int) (chromosome % 4503599627370496L / 17592186044416L);
            b = (int) (chromosome % 1152921504606846976L / 4503599627370496L);
        }
    }
}
