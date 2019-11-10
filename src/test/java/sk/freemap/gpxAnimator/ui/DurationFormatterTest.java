package sk.freemap.gpxAnimator.ui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DurationFormatterTest {

    private static DurationFormatter df;
    private static Map<Long,String> edgeCases = new HashMap<>();

    @BeforeAll
    static void Setup(){

        df = new DurationFormatter();
        edgeCases.put(Long.MIN_VALUE,"-106751991167d -7h -12m -55s -808ms");
        edgeCases.put(Long.MAX_VALUE,"106751991167d 7h 12m 55s 807ms");
        edgeCases.put(-1L,"0d 0h 0m 0s -1ms");
        edgeCases.put(0L,"0d 0h 0m 0s 0ms");
        edgeCases.put(100L,"0d 0h 0m 0s 100ms");
        edgeCases.put(-100L,"0d 0h 0m 0s -100ms");
        edgeCases.put(null,"");

    }

    @Test
    void stringToValue(){

        for (Map.Entry<Long, String> edgeCase : edgeCases.entrySet()) {
            try {
                assertEquals(edgeCase.getKey(),df.stringToValue(edgeCase.getValue()));
            }catch(ParseException e){
                assert (false);
            }
        }

    }

    @Test
    void valueToString() throws ParseException {
        for (Map.Entry<Long, String> edgeCase : edgeCases.entrySet()) {
            assertEquals(edgeCase.getValue(), df.valueToString(edgeCase.getKey()));
        }
    }
}