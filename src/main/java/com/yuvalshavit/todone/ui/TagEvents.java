package com.yuvalshavit.todone.ui;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class TagEvents {

  public static final EventType<TagEvent> TAG_ENTER = new EventType<>("todone.tagEnter");
  public static final EventType<TagEvent> TAG_EXIT = new EventType<>("todone.tagExit");

  public static class TagEvent extends Event {

    public final String tag;

    private TagEvent(EventType<TagEvent> eventType, String tag) {
      super(eventType);
      this.tag = tag;
    }
  }

  public static void fireOnEnterAndExit(Node node, String tag) {
    node.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEvent -> node.fireEvent(new TagEvent(TAG_ENTER, tag)));
    node.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> node.fireEvent(new TagEvent(TAG_EXIT, tag)));
  }
}
