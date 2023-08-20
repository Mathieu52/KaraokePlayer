module org.openjfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;
    requires java.desktop;
    requires java.logging;
    requires org.apache.commons.lang3;
    requires org.apache.commons.text;

    exports net.xz3ra.www.karaokeplayer;
    exports net.xz3ra.www.karaokeplayer.exceptions;
    exports net.xz3ra.www.karaokeplayer.util;
    exports net.xz3ra.www.karaokeplayer.manager;
    opens net.xz3ra.www.karaokeplayer;

    exports net.xz3ra.www.karaokeplayer.karaoke;
    opens net.xz3ra.www.karaokeplayer.karaoke;

    exports net.xz3ra.www.karaokeplayer.media;
    opens net.xz3ra.www.karaokeplayer.media;
}
