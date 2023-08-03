module org.openjfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;
    exports net.xz3ra.www.karaokeplayer;
    exports net.xz3ra.www.karaokeplayer.exceptions;
    exports net.xz3ra.www.karaokeplayer.util;
    exports net.xz3ra.www.karaokeplayer.manager;
    opens net.xz3ra.www.karaokeplayer;
    exports net.xz3ra.www.karaokeplayer.karaoke;
    opens net.xz3ra.www.karaokeplayer.karaoke;
    //exports net.xz3ra.www.karaoke.components;
}