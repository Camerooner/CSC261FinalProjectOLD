/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.finalprojectpokedex;

import com.github.oscar0812.pokeapi.models.pokemon.Pokemon;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Cameron
 */

public class PokedexGUI extends javax.swing.JPanel {
    private List<String> searchHistory = new ArrayList<>();
    private final AudioPlayer audioPlayer = new AudioPlayer();
    private boolean isMusicPaused = false;
    private Pokemon currentPokemon;
    private int currentHistoryIndex = -1;

    /**
     * Creates new form PokedexGUI
     */
    
    public void displayPokemonInfo(Pokemon pokemon) {
        if (pokemon != null) {
            currentPokemon = pokemon;
            pokemonNameLabel.setText(pokemon.getName());

            // Convert the types to a List<String> directly
            List<String> types = pokemon.getTypes().stream()
                                        .map(t -> t.getType().getName())
                                        .collect(Collectors.toList());
            pokemonTypeTextField.setText(String.join(", ", types));
            
            // Calculate BST
            int bst = pokemon.getStats().stream()
                             .mapToInt(stat -> stat.getBaseStat())
                             .sum();

            pokemonInfoText.setText("BST (Base Stat Total): " + bst + "\n" +
                                    "Height: " + pokemon.getHeight() + "\n" +
                                    "Weight: " + pokemon.getWeight() + " lbs" + "\n"+
                                    "Base Experience: " + pokemon.getBaseExperience());  // Add BST info

            // Update the type effectiveness lists based on the Pokémon's types
            updateTypeEffectivenessLists(types);

            // Displays the Pokemons moves
            DefaultListModel<String> movesModel = new DefaultListModel<>();
            pokemon.getMoves().forEach(move -> movesModel.addElement(move.getMove().getName()));
            movesLearnedList.setModel(movesModel);

            // Displays the Pokemons abilities
            DefaultListModel<String> abilitiesModel = new DefaultListModel<>();
            pokemon.getAbilities().forEach(ability -> abilitiesModel.addElement(ability.getAbility().getName()));
            abilitiesList.setModel(abilitiesModel);

            // Displays the sprite based on if the shiny option is checked or not
            String spriteUrl = shinyCheckBox.getState() ? pokemon.getSprites().getFrontShiny() : pokemon.getSprites().getFrontDefault();
            if (spriteUrl != null && !spriteUrl.isEmpty()) {
                try {
                    BufferedImage bi = ImageIO.read(new URL(spriteUrl));
//                    ImageIcon icon = new ImageIcon(bi.getScaledInstance(bi.getWidth() * 2, bi.getHeight() * 2, BufferedImage.SCALE_SMOOTH));
//                    pokemonImage.setMaximumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
//                    pokemonImage.setIcon(icon);
                    updateImage(bi);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(PokedexGUI.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(this, "No new image available for this Pokémon!", "Image Error", JOptionPane.ERROR_MESSAGE);
                    pokemonImage.setIcon(null); // Clear the previous image
                } catch (IOException ex) {
                    Logger.getLogger(PokedexGUI.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(this, "Error while loading the image.", "IO Error", JOptionPane.ERROR_MESSAGE);
                    pokemonImage.setIcon(null); // Clear the previous image
                }
            } else {
                // Clear the previous image if there is no new image to display
                JOptionPane.showMessageDialog(this, "No image URL provided for this Pokémon.", "URL Error", JOptionPane.ERROR_MESSAGE);
                pokemonImage.setIcon(null);
            }
        }
    }
    private void updateImage(BufferedImage bi){
        ImageIcon icon = new ImageIcon(bi.getScaledInstance(-1, pokemonImage.getHeight(), BufferedImage.SCALE_SMOOTH));
        pokemonImage.setIcon(icon);
        pokemonImage.repaint();
        pokemonImage.revalidate();
    }
    private void updateTypeEffectivenessLists(List<String> types) {
        DefaultListModel<String> strengthsModel = new DefaultListModel<>();
        DefaultListModel<String> weaknessesModel = new DefaultListModel<>();
        DefaultListModel<String> immunitiesModel = new DefaultListModel<>();
        HashSet<String> totalWeaknesses = new HashSet<>();
        HashSet<String> totalResistances = new HashSet<>();
        HashSet<String> totalImmunities = new HashSet<>();

        // Collect weaknesses, resistances, and immunities for each type
        for (String type : types) {
            totalWeaknesses.addAll(TypeEffectiveness.getWeaknesses(type));
            totalResistances.addAll(TypeEffectiveness.getResistances(type));
            totalImmunities.addAll(TypeEffectiveness.getImmunities(type));
        }

        // For dual-typed Pokémon: Remove weaknesses if they are resisted by the other type or are immunities
        totalWeaknesses.removeAll(totalResistances);
        totalWeaknesses.removeAll(totalImmunities);

        for (String weakness : totalWeaknesses) {
            if (!weaknessesModel.contains(weakness)) weaknessesModel.addElement(weakness);
        }
        for (String immunity : totalImmunities) {
            if (!immunitiesModel.contains(immunity)) immunitiesModel.addElement(immunity);
        }
        for (String type : types) {
            List<String> strengths = TypeEffectiveness.getStrengths(type);
            for (String strength : strengths) {
                if (!strengthsModel.contains(strength) && !strength.equals("None")) strengthsModel.addElement(strength);
            }
        }

    // Applys the models to the JLists
    strengthsList.setModel(strengthsModel);
    weaknessesList.setModel(weaknessesModel);
    immunitiesList.setModel(immunitiesModel);
    }
    
    /**
    * getGenerationRange is a method that maps a generation to its corresponding range of Pokémon IDs.
    * Each Pokémon generation has a set range of IDs that are unique to that generation.
    * For example, Generation 1 contains Pokémon IDs from 1 to 151.
    * When a generation is selected in the JComboBox, this method is used
    * to determine the valid range of Pokémon IDs that can be searched!
    * "generation" is a string representing the selected generation from the JComboBox.
    */
    
    private int[] getGenerationRange(String generation) {
        switch(generation) {
            case "Gen 1": return new int[]{1, 151};  // Range for Generation 1 Pokémon
            case "Gen 2": return new int[]{152, 251};  // Range for Generation 2 Pokémon
            case "Gen 3": return new int[]{252, 386};  // Range for Generation 3 Pokémon
            case "Gen 4": return new int[]{387, 493};  // Range for Generation 4 Pokémon
            case "Gen 5": return new int[]{494, 649};  // Range for Generation 5 Pokémon
            case "Gen 6": return new int[]{650, 721};  // Range for Generation 6 Pokémon
            case "Gen 7": return new int[]{722, 809};  // Range for Generation 7 Pokémon
            case "Gen 8": return new int[]{810, 905};  // Range for Generation 8 Pokémon
            case "Gen 9": return new int[]{906, 1024};  // Range for Generation 9 Pokémon
            default: return new int[]{1, 1024}; // Defaults to All generations
        }
    }
    
    private void performSearch(String pokemonName) {
        Pokemon pokemon = PokedexApp.fetchPokemonDetailsByName(pokemonName);
        if (pokemon != null) {
            // Get the selected item from the generation chooser
            String selectedGeneration = (String) generationChooser.getSelectedItem();
            // Perform a different action if 'All' generations are selected
            if (!"All".equals(selectedGeneration)) {
                // Convert the selected generation into a start and end range
                int[] range = getGenerationRange(selectedGeneration);
                int startId = range[0];
                int endId = range[1];

                // Check if the Pokémon's ID falls within the selected generation's range
                if (pokemon.getId() < startId || pokemon.getId() > endId) {
                    JOptionPane.showMessageDialog(this, "This Pokémon does not exist in the selected generation.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // Display the Pokémon's info if the 'All' is selected or if the ID is in the range
            displayPokemonInfo(pokemon);
            // Adds to search history and updates the current index
            if (!searchHistory.contains(pokemonName)) {
                searchHistory.add(pokemonName);
                currentHistoryIndex = searchHistory.size() - 1;
            }
        }
    }
    
    public void playButtonPressSound() {
        audioPlayer.toggleMusic("Juniper Pokemon Lab.wav");
    }
    
    public PokedexGUI() {
        initComponents();
        this.setBackground(new java.awt.Color(217, 76, 56)); // Set the background color using RGB values
    }
    
    private void setPokemonImage(String spriteUrl) {
        try {
            BufferedImage originalImage = ImageIO.read(new URL(spriteUrl));
//            BufferedImage combinedImage = new BufferedImage(originalImage.getWidth() * 2, originalImage.getHeight() * 2, BufferedImage.TYPE_INT_ARGB);
//            ImageIcon icon = new ImageIcon(combinedImage);
//            pokemonImage.setIcon(icon);
//            pokemonImage.setMaximumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            updateImage(originalImage);
        } catch (IOException ex) {
            Logger.getLogger(PokedexGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jMenuItem1 = new javax.swing.JMenuItem();
        jDialog1 = new javax.swing.JDialog();
        jScrollPane4 = new javax.swing.JScrollPane();
        pokemonInfoText = new javax.swing.JTextArea();
        jScrollPane7 = new javax.swing.JScrollPane();
        immunitiesList = new javax.swing.JList<>();
        pokemonTypeTextField = new javax.swing.JTextField();
        previousPokemonButton = new javax.swing.JButton();
        movesLearnedLabel = new javax.swing.JLabel();
        pokemonSearchTextField = new javax.swing.JTextField();
        strengthsText = new javax.swing.JLabel();
        pokemonSearchText = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        weaknessesList = new javax.swing.JList<>();
        nextPokemonButton = new javax.swing.JButton();
        weaknessesText = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        strengthsList = new javax.swing.JList<>();
        shinyCheckBox = new java.awt.Checkbox();
        jScrollPane3 = new javax.swing.JScrollPane();
        movesLearnedList = new javax.swing.JList<>();
        generationChooser = new javax.swing.JComboBox<>();
        abilitiesText = new javax.swing.JLabel();
        pokemonNameLabel = new javax.swing.JTextField();
        immunitiesText = new javax.swing.JLabel();
        pokemonImage = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        abilitiesList = new javax.swing.JList<>();
        helpButton = new javax.swing.JButton();
        musicButton = new javax.swing.JToggleButton();

        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jList1);

        jMenuItem1.setText("jMenuItem1");

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pokemonInfoText.setColumns(20);
        pokemonInfoText.setFont(new java.awt.Font("Segoe UI Variable", 0, 12)); // NOI18N
        pokemonInfoText.setRows(5);
        pokemonInfoText.setText("\n");
        jScrollPane4.setViewportView(pokemonInfoText);

        immunitiesList.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12)); // NOI18N
        jScrollPane7.setViewportView(immunitiesList);

        pokemonTypeTextField.setFont(new java.awt.Font("Segoe UI Variable", 0, 12)); // NOI18N
        pokemonTypeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pokemonTypeTextFieldActionPerformed(evt);
            }
        });

        previousPokemonButton.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        previousPokemonButton.setText("Previous Pokémon");
        previousPokemonButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousPokemonButtonActionPerformed(evt);
            }
        });

        movesLearnedLabel.setFont(new java.awt.Font("Pokemon Solid", 0, 11)); // NOI18N
        movesLearnedLabel.setText("Moves Learned:");

        pokemonSearchTextField.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        pokemonSearchTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pokemonSearchTextFieldActionPerformed(evt);
            }
        });

        strengthsText.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        strengthsText.setText("Strong Against:");

        pokemonSearchText.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        pokemonSearchText.setText("Search for Pokémon:");

        weaknessesList.setFont(new java.awt.Font("Segoe UI Variable", 0, 12)); // NOI18N
        jScrollPane5.setViewportView(weaknessesList);

        nextPokemonButton.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        nextPokemonButton.setText("Next Pokémon");
        nextPokemonButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPokemonButtonActionPerformed(evt);
            }
        });

        weaknessesText.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        weaknessesText.setText("Weak Against:");

        strengthsList.setFont(new java.awt.Font("Segoe UI Variable", 0, 12)); // NOI18N
        jScrollPane6.setViewportView(strengthsList);

        shinyCheckBox.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        shinyCheckBox.setLabel("Make Shiny");
        shinyCheckBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shinyCheckBoxMouseClicked(evt);
            }
        });

        movesLearnedList.setFont(new java.awt.Font("Segoe UI Variable", 0, 12)); // NOI18N
        jScrollPane3.setViewportView(movesLearnedList);

        generationChooser.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        generationChooser.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Gen 1", "Gen 2", "Gen 3", "Gen 4", "Gen 5", "Gen 6", "Gen 7", "Gen 8", "Gen 9" }));
        generationChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generationChooserActionPerformed(evt);
            }
        });

        abilitiesText.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        abilitiesText.setText("Abilities:");

        pokemonNameLabel.setFont(new java.awt.Font("Segoe UI Variable", 0, 12)); // NOI18N
        pokemonNameLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pokemonNameLabelActionPerformed(evt);
            }
        });

        immunitiesText.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        immunitiesText.setText("Immunities:");

        pokemonImage.setBackground(new java.awt.Color(0, 0, 0));

        abilitiesList.setFont(new java.awt.Font("Segoe UI Variable", 0, 12)); // NOI18N
        jScrollPane8.setViewportView(abilitiesList);

        helpButton.setFont(new java.awt.Font("Pokemon Solid", 0, 14)); // NOI18N
        helpButton.setText("Help");
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        musicButton.setFont(new java.awt.Font("Pokemon Solid", 0, 12)); // NOI18N
        musicButton.setText("Play Music");
        musicButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                musicButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(generationChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(helpButton)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                    .addComponent(movesLearnedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(musicButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(previousPokemonButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nextPokemonButton))
                            .addComponent(pokemonImage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(shinyCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(strengthsText, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(weaknessesText)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(immunitiesText)
                                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pokemonNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pokemonSearchText))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pokemonSearchTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(pokemonTypeTextField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(abilitiesText, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pokemonSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pokemonSearchText)
                    .addComponent(abilitiesText)
                    .addComponent(helpButton))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(pokemonNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(pokemonTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(generationChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(movesLearnedLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pokemonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(102, 102, 102)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(previousPokemonButton)
                            .addComponent(nextPokemonButton)
                            .addComponent(musicButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(130, 130, 130)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(strengthsText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(weaknessesText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(immunitiesText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(6, 6, 6)
                        .addComponent(shinyCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 433, Short.MAX_VALUE))
        );
    }// </editor-fold>                        

    private void previousPokemonButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                      
        audioPlayer.playButtonSound("A Button BW.wav");
        if (currentPokemon != null) {
            String selectedGeneration = (String) generationChooser.getSelectedItem();
            int[] range = getGenerationRange(selectedGeneration);
            int startId = range[0];
            int endId = range[1];

            if (currentPokemon.getId() > startId && currentPokemon.getId() <= endId) {
                int newId = currentPokemon.getId() - 1;
                Pokemon pokemon = PokedexApp.fetchPokemonDetailsById(newId);
                if (pokemon != null) {
                    displayPokemonInfo(pokemon);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select the appropriate generation.", "Generation Mismatch", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }                                                     

    private void pokemonSearchTextFieldActionPerformed(java.awt.event.ActionEvent evt) {                                                       
        String pokemonName = pokemonSearchTextField.getText().trim();
        performSearch(pokemonName);
    }                                                      

    private void generationChooserActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        // TODO add your handling code here:
    }                                                 

    private void pokemonNameLabelActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        // TODO add your handling code here:
    }                                                

    private void pokemonTypeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        // TODO add your handling code here:
    }                                                    

    private void nextPokemonButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        audioPlayer.playButtonSound("A Button BW.wav");
        if (currentPokemon != null) {
            String selectedGeneration = (String) generationChooser.getSelectedItem();
            int[] range = getGenerationRange(selectedGeneration);
            int startId = range[0];
            int endId = range[1];

            if (currentPokemon.getId() >= startId && currentPokemon.getId() < endId) {
                int newId = currentPokemon.getId() + 1;
                Pokemon pokemon = PokedexApp.fetchPokemonDetailsById(newId);
                if (pokemon != null) {
                    displayPokemonInfo(pokemon);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select the appropriate generation.", "Generation Mismatch", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }                                                 

    private void shinyCheckBoxMouseClicked(java.awt.event.MouseEvent evt) {                                           
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (currentPokemon != null) {
                    String spriteUrl = shinyCheckBox.getState() ? 
                                       currentPokemon.getSprites().getFrontShiny() : 
                                       currentPokemon.getSprites().getFrontDefault();
                    if (spriteUrl != null && !spriteUrl.isEmpty()) {
                        try {
                            BufferedImage bi = ImageIO.read(new URL(spriteUrl));
                            updateImage(bi);
                        } catch (IOException ex) {
                            Logger.getLogger(PokedexGUI.class.getName()).log(Level.SEVERE, null, ex);
                            JOptionPane.showMessageDialog(null, "Error loading shiny sprite.", "Image Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "No shiny sprite available.", "Sprite Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }                                          

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        audioPlayer.playButtonSound("A Button BW.wav");
        JOptionPane.showMessageDialog(this, 
        "Welcome to the Pokédex! Here's how to get started:\n\n" +
        "1. Searching for Pokémon: Use the search bar to search for any Pokémon!.\n" +
        "2. For Pokémon with multiple forms, e.g. Zygarde or Lycanroc, type in the search bar: 'Zygarde-complete' and 'lycanroc-midnight', so the type of form is notated after a dash.\n" +
        "3. Pokémon info: When a valid Pokémon is searched, displaying information about that Pokémon will appear.\n" +
        "4. Use the Next and Previous Pokémon button to see the next or last Pokémon in Pokédex order.\n" +
        "5. Check the 'Make shiny' checkbox to see each Pokémons shiny form!\n"
        );
    }                                          

    private void musicButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        audioPlayer.playButtonSound("A Button BW.wav");
        audioPlayer.toggleMusic("Juniper Pokemon Lab.wav");
    }                                           


    // Variables declaration - do not modify                     
    private javax.swing.JList<String> abilitiesList;
    private javax.swing.JLabel abilitiesText;
    private javax.swing.JComboBox<String> generationChooser;
    private javax.swing.JButton helpButton;
    private javax.swing.JList<String> immunitiesList;
    private javax.swing.JLabel immunitiesText;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JLabel movesLearnedLabel;
    private javax.swing.JList<String> movesLearnedList;
    private javax.swing.JToggleButton musicButton;
    private javax.swing.JButton nextPokemonButton;
    private javax.swing.JLabel pokemonImage;
    private javax.swing.JTextArea pokemonInfoText;
    private javax.swing.JTextField pokemonNameLabel;
    private javax.swing.JLabel pokemonSearchText;
    private javax.swing.JTextField pokemonSearchTextField;
    private javax.swing.JTextField pokemonTypeTextField;
    private javax.swing.JButton previousPokemonButton;
    private java.awt.Checkbox shinyCheckBox;
    private javax.swing.JList<String> strengthsList;
    private javax.swing.JLabel strengthsText;
    private javax.swing.JList<String> weaknessesList;
    private javax.swing.JLabel weaknessesText;
    // End of variables declaration                   
}