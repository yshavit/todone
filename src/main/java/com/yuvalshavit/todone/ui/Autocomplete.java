package com.yuvalshavit.todone.ui;

import com.yuvalshavit.todone.util.PrefixSet;

import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

public class Autocomplete {
  private final PrefixSet autocompletes = new PrefixSet();

  public void addOption(String option) {
    autocompletes.addOption('#' + option);
  }

  public void wrap(TextField textField) {
    ContextMenu menu = new ContextMenu();
    menu.setHideOnEscape(true);
    menu.setAutoFix(true);

    textField.textProperty().addListener((text, oldVal, newVal) -> {
      int caretPosition = Math.min(textField.getCaretPosition() + 1, newVal.length());
      String hashtagPrefix = getHashtagPrefix(newVal, caretPosition);
      if (hashtagPrefix == null) {
        menu.hide();
      } else {
        Iterable<String> autocompleteOptions = autocompletes.optionsWithPrefix(hashtagPrefix);
        ObservableList<MenuItem> items = menu.getItems();
        items.clear();
        for (String autocompleteOption : autocompleteOptions) {
          MenuItem item = new MenuItem(autocompleteOption);
          item.setOnAction(event -> {
            int insertAt = getHashtagPrefixIndex(newVal, caretPosition);
            StringBuilder sb = new StringBuilder(newVal.length() + autocompleteOption.length());
            sb.append(newVal, 0, insertAt);
            sb.append(autocompleteOption);
            sb.append(newVal, caretPosition, newVal.length());
            textField.setText(sb.toString());
            textField.positionCaret(insertAt + autocompleteOption.length());
          });
          items.add(item);
        }
        menu.show(textField, Side.BOTTOM, 0, 0);
      }
    });
  }

  private static String getHashtagPrefix(String string, int caretPosition) {
    int start = getHashtagPrefixIndex(string, caretPosition);
    return start < 0 ? null : string.substring(start, caretPosition);
  }

  private static int getHashtagPrefixIndex(String string, int caretPosition) {
    int lastSpace = string.lastIndexOf(' ', caretPosition - 1);
    if (lastSpace < 0) {
      return string.isEmpty() || string.charAt(0) != '#'
        ? -1
        : 0;
    } else if (lastSpace + 1 < caretPosition && string.charAt(lastSpace + 1) == '#') {
      return lastSpace + 1;
    } else {
      return -1;
    }
  }

}
