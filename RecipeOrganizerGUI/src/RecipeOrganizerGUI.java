import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RecipeOrganizerGUI extends JFrame {
    private JTextField searchField;
    private final JButton searchButton;
    private JList<String> recipeList;
    private final JButton editButton;
    private final JButton deleteButton;
    private final JTextArea recipeDetails;
    private List<Recipe> recipes;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database_name";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public RecipeOrganizerGUI() {
        
        setTitle("Recipe Organizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        //Dummy Data
        recipes = new ArrayList<>();
        recipes.add(new Recipe("Pasta Carbonara", "Delicious pasta dish", "Pasta, Bacon, Eggs, Parmesan Cheese"));
        recipes.add(new Recipe("Chicken Curry", "Spicy chicken curry", "Chicken, Curry Paste, Coconut Milk"));
        recipes.add(new Recipe("Chocolate Chip Cookies", "Classic homemade cookies", "Flour, Sugar, Butter, Chocolate Chips"));
        recipes.add(new Recipe("Caesar Salad", "Fresh and flavorful salad", "Romaine Lettuce, Croutons, Parmesan Cheese, Caesar Dressing"));

        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel recipeListPanel = new JPanel(new BorderLayout());
        recipeList = new JList<>();
        updateRecipeList();
        JScrollPane recipeListScrollPane = new JScrollPane(recipeList);
        recipeListPanel.add(recipeListScrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);

        JPanel recipeDetailsPanel = new JPanel(new BorderLayout());
        recipeDetails = new JTextArea(10, 40);
        recipeDetails.setEditable(false);
        JScrollPane recipeDetailsScrollPane = new JScrollPane(recipeDetails);
        recipeDetailsPanel.add(recipeDetailsScrollPane, BorderLayout.CENTER);

        add(searchPanel, BorderLayout.NORTH);
        add(recipeListPanel, BorderLayout.WEST);
        add(buttonsPanel, BorderLayout.CENTER);
        add(recipeDetailsPanel, BorderLayout.EAST);

        searchButton.addActionListener((ActionEvent e) -> {
            String searchQuery = searchField.getText();
            // Perform the search operation based on the query
            List<Recipe> searchResults = searchRecipes(searchQuery);
            updateRecipeList(searchResults);
        });

        editButton.addActionListener((ActionEvent e) -> {
            int selectedIndex = recipeList.getSelectedIndex();
           
            if (selectedIndex != -1) {
                Recipe selectedRecipe = recipes.get(selectedIndex);
                Recipe modifiedRecipe = showEditDialog(selectedRecipe);
                if (modifiedRecipe != null) {
                    recipes.set(selectedIndex, modifiedRecipe);
                    updateRecipeList();
                    recipeList.setSelectedIndex(selectedIndex);
                    displayRecipeDetails(modifiedRecipe);

                    updateRecipe(modifiedRecipe);
                }
            } else {
                JOptionPane.showMessageDialog(RecipeOrganizerGUI.this, "Please select a recipe to edit.");
            }
        });

        deleteButton.addActionListener((ActionEvent e) -> {
            int selectedIndex = recipeList.getSelectedIndex();
           
            if (selectedIndex != -1) {
                Recipe selectedRecipe = recipes.get(selectedIndex);
                int confirmation = JOptionPane.showConfirmDialog(RecipeOrganizerGUI.this,
                        "Are you sure you want to delete the selected recipe?", "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);
                if (confirmation == JOptionPane.YES_OPTION) {
                    recipes.remove(selectedRecipe);
                    updateRecipeList();
                    clearRecipeDetails();

                    deleteRecipe(selectedRecipe);
                }
            } else {
                JOptionPane.showMessageDialog(RecipeOrganizerGUI.this, "Please select a recipe to delete.");
            }
        });

        recipeList.addListSelectionListener(e -> {
            int selectedIndex = recipeList.getSelectedIndex();
            if (selectedIndex != -1) {
                Recipe selectedRecipe = recipes.get(selectedIndex);
                displayRecipeDetails(selectedRecipe);
            }
        });
    }

    private void updateRecipeList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (Recipe recipe : recipes) {
            model.addElement(recipe.getName());
        }
        recipeList.setModel(model);
    }

    private void updateRecipeList(List<Recipe> searchResults) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (Recipe recipe : searchResults) {
            model.addElement(recipe.getName());
        }
        recipeList.setModel(model);
    }

    private List<Recipe> searchRecipes(String query) {
        List<Recipe> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getName().toLowerCase().contains(query.toLowerCase())) {
                results.add(recipe);
            }
        }
        return results;
    }

    private Recipe showEditDialog(Recipe recipe) {
        String modifiedName = JOptionPane.showInputDialog(RecipeOrganizerGUI.this, "Enter the modified name:", recipe.getName());
        String modifiedDescription = JOptionPane.showInputDialog(RecipeOrganizerGUI.this, "Enter the modified description:", recipe.getDescription());
        String modifiedIngredients = JOptionPane.showInputDialog(RecipeOrganizerGUI.this, "Enter the modified ingredients:", recipe.getIngredients());

        Recipe modifiedRecipe = new Recipe(modifiedName, modifiedDescription, modifiedIngredients);

        JOptionPane.showMessageDialog(RecipeOrganizerGUI.this, "Recipe has been successfully edited!");

        return modifiedRecipe;
    }

    private void displayRecipeDetails(Recipe recipe) {
        recipeDetails.setText("Name: " + recipe.getName() +
                "\nDescription: " + recipe.getDescription() +
                "\nIngredients: " + recipe.getIngredients());
    }

    private void clearRecipeDetails() {
        recipeDetails.setText("");
    }

    private void updateRecipe(Recipe recipe) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE recipes SET name = ?, description = ?, ingredients = ? WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, recipe.getName());
            statement.setString(2, recipe.getDescription());
            statement.setString(3, recipe.getIngredients());
            statement.setString(4, recipe.getName());
            statement.executeUpdate();
            System.out.println("Recipe updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating recipe: " + e.getMessage());
        }
    }

    private void deleteRecipe(Recipe recipe) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM recipes WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, recipe.getName());
            statement.executeUpdate();
            System.out.println("Recipe deleted successfully!");
        } catch (SQLException e) {
            System.out.println("Error deleting recipe: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RecipeOrganizerGUI recipeOrganizerGUI = new RecipeOrganizerGUI();
            recipeOrganizerGUI.setVisible(true);
        });
    }

    private static class Recipe {
        private final String name;
        private final String description;
        private final String ingredients;

        public Recipe(String name, String description, String ingredients) {
            this.name = name;
            this.description = description;
            this.ingredients = ingredients;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getIngredients() {
            return ingredients;
        }
    }
}
