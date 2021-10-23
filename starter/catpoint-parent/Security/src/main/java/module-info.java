module com.udacity.catpoint.security {
    requires miglayout;
    requires transitive java.desktop;
    requires transitive com.google.gson;
    requires transitive com.google.common;
    requires java.prefs;
    requires transitive com.udacity.catpoint.image;
    opens com.udacity.catpoint.security.data to com.google.gson;
    opens com.udacity.catpoint.security.service to com.google.gson;
}