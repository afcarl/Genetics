/*
 * This file is part of Genetics, licensed under the MIT License (MIT).
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Viewer extends JFrame {
    private final JLabel imgLabel;
    private final JLabel imgInfo;
    private int current = 0;
    private SimulThread thread;

    private Viewer() {
        setTitle("Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        setBounds(0, 0, 530, 600);
        final JPanel btnPane = new JPanel();
        add(btnPane, BorderLayout.NORTH);
        final JButton prevButton = new JButton("Prev");
        imgInfo = new JLabel();
        final JButton nextButton = new JButton("Next");
        prevButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                current--;
                if (current < 0)
                    current = 9;
                refresh();
            }
        });
        nextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                current++;
                if (current > 9)
                    current = 0;
                refresh();
            }
        });
        final JButton evolButton = new JButton("Run");
        evolButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (thread == null) {
                    thread = new SimulThread();
                    thread.start();
                    evolButton.setText("Pause");
                } else {
                    thread.stopThread();
                    thread = null;
                    evolButton.setText("Run");
                }
            }
        });
        final JPanel evolPane = new JPanel();
        btnPane.add(evolPane, BorderLayout.SOUTH);
        evolPane.add(evolButton);
        final JPanel infoPane = new JPanel();
        btnPane.add(infoPane, BorderLayout.CENTER);
        infoPane.add(imgInfo);
        final JPanel navPane = new JPanel();
        btnPane.add(navPane, BorderLayout.WEST);
        navPane.add(prevButton);
        navPane.add(nextButton);
        final JPanel imgPane = new JPanel();
        add(imgPane, BorderLayout.CENTER);
        imgLabel = new JLabel();
        imgPane.add(imgLabel);
        Candidate.init();
        refresh();
    }

    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                final Viewer frame = new Viewer();
                frame.setVisible(true);
                frame.setResizable(false);
            }
        });
    }

    void refresh() {
        final Candidate candidate = Candidate.getCandidate(current);
        imgInfo.setText("Gen. " + Candidate.getGeneration() + " - Chr. " + (current + 1) + " (" + candidate.getFitness() + ")");
        imgLabel.setIcon(new ImageIcon(candidate.getImage()));
    }

    class SimulThread extends Thread {
        private boolean exit = false;

        @Override
        public void run() {
            while (!exit) {
                Candidate.evolve();
                refresh();
            }
        }

        public void stopThread() {
            exit = true;
        }
    }
}
