package pgc;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Povinelli
 * Created 2018-Mar-28
 * In Pressure-Gradient-Calculator-for-MS-Stars
 */
public class StageCloseFactory {
  static class StageCloseKeyEventHandler implements EventHandler<KeyEvent> {

    private Stage stage;
    private List<KeyCode> keyCodes;
    private Runnable handler;

    public StageCloseKeyEventHandler(Stage stage) {
      this.stage = stage;
      keyCodes = Collections.singletonList(KeyCode.ESCAPE);
      handler = this.stage::close;
    }

    public StageCloseKeyEventHandler(Stage stage, KeyCode... codes) {
      keyCodes = Arrays.asList(codes);
      this.stage = stage;
      handler = this.stage::close;
    }

    public StageCloseKeyEventHandler(Stage stage, Runnable handler) {
      this.stage = stage;
      this.handler = handler;
    }

    public StageCloseKeyEventHandler(
        Stage stage, Runnable handler,
        KeyCode... keyCodes
    ) {
      this.stage = stage;
      this.keyCodes = Arrays.asList(keyCodes);
      this.handler = handler;
    }

    @Override
    public void handle(KeyEvent event) {
      if (keyCodes.stream().anyMatch(code -> code == event.getCode())) {
        handler.run();
      }
    }
  }

  public static StageCloseKeyEventHandler closeKEH(Stage stage) {
    return new StageCloseKeyEventHandler(stage);
  }

  public static StageCloseKeyEventHandler closeKEH(
      Stage stage,
      KeyCode... codes
  ) {
    return new StageCloseKeyEventHandler(stage, codes);
  }

  public static StageCloseKeyEventHandler closeKEH(
      Stage stage,
      Runnable runnable
  ) {
    return new StageCloseKeyEventHandler(stage, runnable);
  }

  public static StageCloseKeyEventHandler closeKEH(
      Stage stage,
      Runnable runnable,
      KeyCode... codes
  ) {
    return new StageCloseKeyEventHandler(stage, runnable, codes);
  }


}
