package ua.lpnu.deposits.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import ua.lpnu.deposits.model.Deposit;
import ua.lpnu.deposits.model.DepositType;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.UserSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GUI automation tests for the Search &amp; Filter screen.
 * Verifies filter application, sorting, and the reset action.
 */
@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchFilterViewTest {

    @Start
    void start(Stage stage) throws Exception {
        AppContext.initialize();
        UserSession.login("admin", "ADMIN");

        FXMLLoader loader = new FXMLLoader(
                SearchFilterController.class.getResource("/fxml/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 760);
        scene.getStylesheets().add(
                SearchFilterController.class.getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    @Order(1)
    void onLoad_resultsTableIsEmpty(FxRobot robot) {
        robot.clickOn("#navSearch");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> table = (TableView<Deposit>) robot.lookup("#resultsTable").query();
            assertTrue(table.getItems().isEmpty(),
                    "Results table must be empty before any search is applied");
        });
    }

    @Test
    @Order(2)
    void onLoad_resultCountLabelIsBlank(FxRobot robot) {
        robot.clickOn("#navSearch");

        robot.interact(() -> {
            Label label = robot.lookup("#resultCountLabel").queryAs(Label.class);
            assertTrue(label.getText().isBlank(),
                    "Result count label must be blank before any search");
        });
    }

    @Test
    @Order(3)
    void whenApplyWithNoFilters_allSeededDepositsAreShown(FxRobot robot) {
        robot.clickOn("#navSearch");
        robot.clickOn("✔  Застосувати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> table = (TableView<Deposit>) robot.lookup("#resultsTable").query();
            assertFalse(table.getItems().isEmpty(),
                    "Applying with no filters must return all deposits");
            assertTrue(table.getItems().size() >= 15,
                    "Must return at least the 15 seeded deposits");
        });
    }

    @Test
    @Order(4)
    void whenApplyWithNoFilters_resultCountLabelShowsTotal(FxRobot robot) {
        robot.clickOn("#navSearch");
        robot.clickOn("✔  Застосувати");

        robot.interact(() -> {
            Label label = robot.lookup("#resultCountLabel").queryAs(Label.class);
            assertFalse(label.getText().isBlank(),
                    "Result count label must be filled after applying filters");
            assertTrue(label.getText().startsWith("Знайдено:"),
                    "Count label must start with 'Знайдено:' but was: " + label.getText());
        });
    }

    @Test
    @Order(5)
    void whenTypeFilterSetToTerm_onlyTermDepositsAreReturned(FxRobot robot) {
        robot.clickOn("#navSearch");

        robot.clickOn("#typeFilterCombo");
        robot.clickOn("Строковий");
        robot.clickOn("✔  Застосувати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> table = (TableView<Deposit>) robot.lookup("#resultsTable").query();
            assertFalse(table.getItems().isEmpty(),
                    "Filtering by TERM type must return results");
            for (Deposit d : table.getItems()) {
                assertEquals(DepositType.TERM, d.getType(),
                        "Every result must be TERM type when TERM filter is applied");
            }
        });
    }

    @Test
    @Order(6)
    void whenTypeFilterSetToSavings_onlySavingsDepositsAreReturned(FxRobot robot) {
        robot.clickOn("#navSearch");

        robot.clickOn("#typeFilterCombo");
        robot.clickOn("Ощадний");
        robot.clickOn("✔  Застосувати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> table = (TableView<Deposit>) robot.lookup("#resultsTable").query();
            assertFalse(table.getItems().isEmpty(),
                    "Filtering by SAVINGS type must return results");
            for (Deposit d : table.getItems()) {
                assertEquals(DepositType.SAVINGS, d.getType(),
                        "Every result must be SAVINGS type when SAVINGS filter is applied");
            }
        });
    }

    @Test
    @Order(7)
    void whenWithdrawCheckboxSelected_onlyWithdrawableDepositsReturned(FxRobot robot) {
        robot.clickOn("#navSearch");
        robot.clickOn("#withdrawCheck");
        robot.clickOn("✔  Застосувати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> table = (TableView<Deposit>) robot.lookup("#resultsTable").query();
            assertFalse(table.getItems().isEmpty(),
                    "Filtering by early-withdrawal must return results");
            for (Deposit d : table.getItems()) {
                assertTrue(d.isCanWithdrawEarly(),
                        "Every result must support early withdrawal when that filter is active");
            }
        });
    }

    @Test
    @Order(8)
    void whenReplenishCheckboxSelected_onlyReplenishableDepositsReturned(FxRobot robot) {
        robot.clickOn("#navSearch");
        robot.clickOn("#replenishCheck");
        robot.clickOn("✔  Застосувати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> table = (TableView<Deposit>) robot.lookup("#resultsTable").query();
            assertFalse(table.getItems().isEmpty(),
                    "Filtering by replenishment must return results");
            for (Deposit d : table.getItems()) {
                assertTrue(d.isCanReplenish(),
                        "Every result must support replenishment when that filter is active");
            }
        });
    }

    @Test
    @Order(9)
    void whenSortedByRateDescending_firstResultHasHighestRate(FxRobot robot) {
        robot.clickOn("#navSearch");

        robot.clickOn("#sortFieldCombo");
        robot.clickOn("Ставка %");
        robot.clickOn("#descRadio");
        robot.clickOn("✔  Застосувати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> table = (TableView<Deposit>) robot.lookup("#resultsTable").query();
            assertTrue(table.getItems().size() >= 2,
                    "Must have at least 2 results to verify sort order");

            double firstRate  = table.getItems().get(0).getInterestRate();
            double secondRate = table.getItems().get(1).getInterestRate();
            assertTrue(firstRate >= secondRate,
                    "First result (" + firstRate + "%) must have rate >= second (" + secondRate + "%) when sorted descending");
        });
    }

    @Test
    @Order(10)
    void whenSortedByRateAscending_firstResultHasLowestRate(FxRobot robot) {
        robot.clickOn("#navSearch");

        robot.clickOn("#sortFieldCombo");
        robot.clickOn("Ставка %");
        robot.clickOn("#ascRadio");
        robot.clickOn("✔  Застосувати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> table = (TableView<Deposit>) robot.lookup("#resultsTable").query();
            assertTrue(table.getItems().size() >= 2,
                    "Must have at least 2 results to verify sort order");

            double firstRate  = table.getItems().get(0).getInterestRate();
            double secondRate = table.getItems().get(1).getInterestRate();
            assertTrue(firstRate <= secondRate,
                    "First result (" + firstRate + "%) must have rate <= second (" + secondRate + "%) when sorted ascending");
        });
    }

    @Test
    @Order(11)
    void whenRateRangeFilterApplied_onlyDepositsInRangeReturned(FxRobot robot) {
        robot.clickOn("#navSearch");

        robot.clickOn("#minRateField").write("15");
        robot.clickOn("#maxRateField").write("17");
        robot.clickOn("✔  Застосувати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> table = (TableView<Deposit>) robot.lookup("#resultsTable").query();
            for (Deposit d : table.getItems()) {
                double rate = d.getInterestRate();
                assertTrue(rate >= 15.0 && rate <= 17.0,
                        "Rate " + rate + "% must be within 15–17% range");
            }
        });
    }

    @Test
    @Order(12)
    void whenResetClicked_allFilterFieldsAreCleared(FxRobot robot) {
        robot.clickOn("#navSearch");

        robot.clickOn("#currencyFilterField").write("USD");
        robot.clickOn("#minRateField").write("5");
        robot.clickOn("#withdrawCheck");
        robot.clickOn("✔  Застосувати");
        robot.clickOn("✕  Скинути");

        robot.interact(() -> {
            TextField currency = robot.lookup("#currencyFilterField").queryAs(TextField.class);
            TextField minRate  = robot.lookup("#minRateField").queryAs(TextField.class);
            CheckBox  withdraw = robot.lookup("#withdrawCheck").queryAs(CheckBox.class);
            RadioButton asc   = robot.lookup("#ascRadio").queryAs(RadioButton.class);

            assertTrue(currency.getText().isEmpty(), "Currency field must be cleared after reset");
            assertTrue(minRate.getText().isEmpty(),  "Min-rate field must be cleared after reset");
            assertFalse(withdraw.isSelected(),       "Withdraw checkbox must be unchecked after reset");
            assertTrue(asc.isSelected(),             "Ascending radio must be reselected after reset");

            @SuppressWarnings("unchecked")
            TableView<Deposit> table = (TableView<Deposit>) robot.lookup("#resultsTable").query();
            assertTrue(table.getItems().isEmpty(), "Results table must be cleared after reset");

            Label countLabel = robot.lookup("#resultCountLabel").queryAs(Label.class);
            assertTrue(countLabel.getText().isBlank(), "Count label must be blank after reset");
        });
    }

    @Test
    @Order(13)
    void whenTypeComboReset_itReturnsToAllTypes(FxRobot robot) {
        robot.clickOn("#navSearch");

        robot.clickOn("#typeFilterCombo");
        robot.clickOn("Строковий");
        robot.clickOn("✕  Скинути");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            ComboBox<String> combo = (ComboBox<String>) robot.lookup("#typeFilterCombo").query();
            assertEquals("Всі типи", combo.getValue(),
                    "Type filter must reset to 'Всі типи' after reset");
        });
    }
}
