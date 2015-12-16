package uk.ac.cam.eyx20.fjava.tick2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FurtherJavaPreamble {
    enum Ticker {A, B, C, D, UNKNOWN};
    String author();
    String date();
    String crsid();
    String summary();
    Ticker ticker();
}
