package Services;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ToastNotification {

    public enum Type { SUCCESS, WARNING, DANGER, INFO }

    private static final List<AlertRecord> alertHistory = new ArrayList<>();

    public static void showWarning(Stage stage, String title, String message) {
        show(stage, title, message, Type.WARNING, 5000);
        addToHistory(title, message, Type.WARNING);
    }

    public static void showDanger(Stage stage, String title, String message) {
        show(stage, title, message, Type.DANGER, 6000);
        addToHistory(title, message, Type.DANGER);
    }

    public static void showSuccess(Stage stage, String title, String message) {
        show(stage, title, message, Type.SUCCESS, 3000);
        addToHistory(title, message, Type.SUCCESS);
    }

    public static void showInfo(Stage stage, String title, String message) {
        show(stage, title, message, Type.INFO, 3000);
        addToHistory(title, message, Type.INFO);
    }

    public static List<AlertRecord> getHistory() { return new ArrayList<>(alertHistory); }
    public static void clearHistory()            { alertHistory.clear(); }

    private static void show(Stage stage, String title, String message, Type type, int durationMs) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);

            HBox container = new HBox(14);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(16, 20, 16, 16));
            container.setMinWidth(380);
            container.setMaxWidth(420);

            String bgColor, borderColor, iconText;
            switch (type) {
                case WARNING: bgColor="#fffbeb"; borderColor="#f59e0b"; iconText="⚠️"; break;
                case DANGER:  bgColor="#fef2f2"; borderColor="#ef4444"; iconText="🚨"; break;
                case SUCCESS: bgColor="#f0fdf4"; borderColor="#10b981"; iconText="✅"; break;
                default:      bgColor="#eff6ff"; borderColor="#3b82f6"; iconText="ℹ️"; break;
            }

            container.setStyle(
                    "-fx-background-color:" + bgColor + ";" +
                            "-fx-background-radius:16;" +
                            "-fx-border-color:" + borderColor + ";" +
                            "-fx-border-width:0 0 0 5;" +
                            "-fx-border-radius:16;" +
                            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),20,0,0,6);"
            );

            Label icon = new Label(iconText);
            icon.setStyle("-fx-font-size:28;");

            VBox textBox = new VBox(4);
            Label lTitle = new Label(title);
            lTitle.setStyle("-fx-font-weight:700;-fx-font-size:14;-fx-text-fill:#0f172a;");
            Label lMsg = new Label(message);
            lMsg.setStyle("-fx-font-size:12;-fx-text-fill:#475569;-fx-wrap-text:true;");
            lMsg.setMaxWidth(280);
            Label lTime = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            lTime.setStyle("-fx-font-size:10;-fx-text-fill:#94a3b8;");
            textBox.getChildren().addAll(lTitle, lMsg, lTime);

            Label closeBtn = new Label("✕");
            closeBtn.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:14;-fx-cursor:hand;-fx-padding:0 0 0 8;");
            closeBtn.setOnMouseClicked(e -> fadeOut(popup, container));

            container.getChildren().addAll(icon, textBox, closeBtn);
            popup.getContent().add(container);

            double x = stage.getX() + stage.getWidth() - 440;
            double y = stage.getY() + stage.getHeight() - 130;
            popup.show(stage, x, y);

            container.setOpacity(0);
            container.setTranslateY(20);
            new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(container.opacityProperty(), 0),
                            new KeyValue(container.translateYProperty(), 20)),
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(container.opacityProperty(), 1, Interpolator.EASE_OUT),
                            new KeyValue(container.translateYProperty(), 0, Interpolator.EASE_OUT))
            ).play();

            PauseTransition pause = new PauseTransition(Duration.millis(durationMs));
            pause.setOnFinished(e -> fadeOut(popup, container));
            pause.play();

            try { if (type==Type.DANGER||type==Type.WARNING) java.awt.Toolkit.getDefaultToolkit().beep(); }
            catch (Exception ignored) {}
        });
    }

    private static void fadeOut(Popup popup, HBox container) {
        Timeline out = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(container.opacityProperty(), 1),
                        new KeyValue(container.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(container.opacityProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(container.translateYProperty(), -10, Interpolator.EASE_IN))
        );
        out.setOnFinished(e -> popup.hide());
        out.play();
    }

    private static void addToHistory(String title, String message, Type type) {
        alertHistory.add(0, new AlertRecord(title, message, type,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))));
        if (alertHistory.size() > 50) alertHistory.remove(alertHistory.size()-1);
    }

    public static class AlertRecord {
        public final String title, message, timestamp;
        public final Type type;
        public AlertRecord(String title, String message, Type type, String timestamp) {
            this.title=title; this.message=message; this.type=type; this.timestamp=timestamp;
        }
        public String getIcon()  { switch(type){case WARNING:return"⚠️";case DANGER:return"🚨";case SUCCESS:return"✅";default:return"ℹ️";} }
        public String getColor() { switch(type){case WARNING:return"#f59e0b";case DANGER:return"#ef4444";case SUCCESS:return"#10b981";default:return"#3b82f6";} }
    }
}